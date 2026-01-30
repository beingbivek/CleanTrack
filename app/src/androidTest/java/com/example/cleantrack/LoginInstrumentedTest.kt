package com.example.cleantrack

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.example.cleantrack.view.auth.LoginActivity
import com.example.cleantrack.view.auth.RegistrationActivity
import com.example.cleantrack.view.user.UserDashboardActivity
import org.junit.*

class LoginInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<LoginActivity>()

    @Before fun setup() = Intents.init()
    @After fun tearDown() = Intents.release()

    @Test
    fun testNavigationToRegistration() {
        // Find the registration link and click
        composeRule.onNodeWithTag("go_to_registration").performClick()

        // Assert Intent
        Intents.intended(hasComponent(RegistrationActivity::class.java.name))
    }

    @Test
    fun testLoginFlowSuccess() {
        composeRule.onNodeWithTag("email_field").performTextInput("apple1@gmail.com")
        composeRule.onNodeWithTag("password_field").performTextInput("apple@12")
        composeRule.onNodeWithTag("login_button").performClick()

        // Wait up to 10s for navigation (Handles slow emulator/network)
        composeRule.waitUntil(10000) {
            try {
                Intents.intended(hasComponent(UserDashboardActivity::class.java.name))
                true
            } catch (e: Exception) { false }
        }
    }
}
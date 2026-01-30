package com.example.cleantrack

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cleantrack.view.auth.LoginActivity
import com.example.cleantrack.view.auth.RegistrationActivity
import com.example.cleantrack.view.user.UserDashboardActivity
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<LoginActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testNavigationToRegistration() {
        // Match teacher's style: Action -> Intent
        composeRule.onNodeWithTag("go_to_registration").performClick()

        Intents.intended(hasComponent(RegistrationActivity::class.java.name))
    }

    @Test
    fun testLoginFlowSuccess() {
        // 1. Enter Credentials
        composeRule.onNodeWithTag("email_field").performTextInput("apple1@gmail.com")
        composeRule.onNodeWithTag("password_field").performTextInput("apple@12")

        // 2. Click Login
        composeRule.onNodeWithTag("login_button").performClick()

        // 3. THE KEY FIX: We must wait for the Intent to exist
        // because Firebase is slower than the test runner.
        composeRule.waitUntil(10000) {
            try {
                Intents.intended(hasComponent(UserDashboardActivity::class.java.name))
                true // If it finds it, loop ends
            } catch (e: AssertionError) {
                false // If not found yet, loop continues
            }
        }
    }
}
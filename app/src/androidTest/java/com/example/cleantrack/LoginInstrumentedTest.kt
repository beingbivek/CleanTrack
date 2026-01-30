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
        // 1. Inputs
        composeRule.onNodeWithTag("email_field").performTextInput("apple1@gmail.com")
        composeRule.onNodeWithTag("password_field").performTextInput("apple@12")

        // 2. Click
        composeRule.onNodeWithTag("login_button").performClick()

        // 3. WAIT for the Dashboard UI to appear (e.g., a "Welcome" text or a unique Tag)
        // This gives Firebase time to finish and the Activity time to switch
        composeRule.waitUntil(timeoutMillis = 10000) {
            // Replace "dashboard_root" with a testTag you have in your UserDashboardActivity
            composeRule.onAllNodesWithTag("dashboard_root").fetchSemanticsNodes().isNotEmpty()
        }

        // 4. NOW check the intent (it will be in the recorded list now)
        Intents.intended(hasComponent(UserDashboardActivity::class.java.name))
    }
}
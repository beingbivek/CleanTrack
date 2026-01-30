package com.example.cleantrack

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cleantrack.view.auth.LoginActivity
import com.example.cleantrack.view.user.UserDashboardActivity
import com.example.cleantrack.view.auth.RegistrationActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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
    fun testLoginNavigation_ToUserDashboard() {
        // Type Email
        composeRule.onNodeWithTag("email_field")
            .performTextInput("testuser@example.com")

        // Type Password
        composeRule.onNodeWithTag("password_field")
            .performTextInput("password123")

        // Click Login Button
        composeRule.onNodeWithTag("login_button")
            .performClick()

        // Verify navigation to User Dashboard
        // Note: This assumes the repo returns a successful login for these credentials
        Intents.intended(hasComponent(UserDashboardActivity::class.java.name))
    }

    @Test
    fun testClickRegister_NavigatesToRegistration() {
        // Click the "Register" or "Sign Up" text/button
        composeRule.onNodeWithTag("go_to_registration")
            .performClick()

        // Verify navigation
        Intents.intended(hasComponent(RegistrationActivity::class.java.name))
    }
}
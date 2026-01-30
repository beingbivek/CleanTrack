package com.example.cleantrack

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cleantrack.view.auth.RegistrationActivity
import com.example.cleantrack.view.auth.LoginActivity
import com.example.cleantrack.view.auth.UserLocationMapActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<RegistrationActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testSuccessfulRegistration_NavigatesToMap() {
        // Fill in registration details
        composeRule.onNodeWithTag("reg_email").performTextInput("newuser@gmail.com")
        composeRule.onNodeWithTag("reg_password").performTextInput("password123")
        composeRule.onNodeWithTag("reg_fullname").performTextInput("John Doe")

        // Click Register
        composeRule.onNodeWithTag("register_button").performClick()

        // Based on common Clean Architecture flows, registration often leads
        // to a Map activity to set the home address
        Intents.intended(hasComponent(UserLocationMapActivity::class.java.name))
    }
}
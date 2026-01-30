package com.example.cleantrack

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cleantrack.view.auth.RegistrationActivity
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
    fun testFullRegistrationFlow_Success() {
        // 1. Fill Text Fields
        composeRule.onNodeWithTag("reg_fullname").performTextInput("Test User")
        composeRule.onNodeWithTag("reg_email").performTextInput("testuser@example.com")
        composeRule.onNodeWithTag("reg_phone").performTextInput("9800000000")

        // 2. Handle Dropdowns (Simulate selection)
        // Note: We use useUnmergedTree = true if the click target is deep in the Box
        composeRule.onNodeWithTag("province_dropdown").performClick()
        composeRule.onNodeWithText("Bagmati").performClick() // Matches your addressVM data

        composeRule.onNodeWithTag("district_dropdown").performClick()
        composeRule.onNodeWithText("Kathmandu").performClick()

        composeRule.onNodeWithTag("municipality_dropdown").performClick()
        composeRule.onNodeWithText("Kathmandu Metropolitan City").performClick()

        composeRule.onNodeWithTag("ward_dropdown").performClick()
        composeRule.onNodeWithText("1").performClick()

        // 3. Passwords
        composeRule.onNodeWithTag("reg_password").performTextInput("Password123!")
        composeRule.onNodeWithTag("reg_confirm_password").performTextInput("Password123!")

        // 4. Accept Terms & Register
        composeRule.onNodeWithTag("terms_checkbox").performClick()

        // Use scroll to ensure button is in view for smaller screens
        composeRule.onNodeWithTag("register_button").performScrollTo().performClick()

        // 5. Verify Navigation to Map Activity
        composeRule.waitUntil(10000) {
            try {
                Intents.intended(hasComponent(UserLocationMapActivity::class.java.name))
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    @Test
    fun testPasswordMismatch_ShowsError() {
        composeRule.onNodeWithTag("reg_fullname").performTextInput("Test User")
        composeRule.onNodeWithTag("reg_password").performTextInput("Pass1")
        composeRule.onNodeWithTag("reg_confirm_password").performTextInput("Pass2")

        composeRule.onNodeWithTag("register_button").performScrollTo().performClick()

        // Verify we are still on the same activity (Button still exists)
        composeRule.onNodeWithTag("register_button").assertIsDisplayed()
    }
}
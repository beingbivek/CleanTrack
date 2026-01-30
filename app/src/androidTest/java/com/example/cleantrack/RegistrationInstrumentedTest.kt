package com.example.cleantrack

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cleantrack.view.auth.RegistrationActivity
import com.example.cleantrack.view.auth.UserLocationMapActivity

import org.junit.*
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
    fun testRegistrationFlowSuccess() {
        val testEmail = "test${System.currentTimeMillis()}@gmail.com"

        // Fill basic info
        composeRule.onNodeWithTag("reg_fullname").performTextInput("John Doe")
        composeRule.onNodeWithTag("reg_email").performTextInput(testEmail)
        composeRule.onNodeWithTag("reg_phone").performTextInput("9800000000")

        // --- PROVINCE DROPDOWN ---
        composeRule.onNodeWithTag("province_dropdown").performClick()
        // Wait for the popup and click the text.
        // useUnmergedTree = true is MANDATORY for dropdown items
        composeRule.onNodeWithText("Koshi", useUnmergedTree = true)
            .performClick()

        // --- DISTRICT DROPDOWN ---
        composeRule.onNodeWithTag("district_dropdown").performClick()
        composeRule.onNodeWithText("Morang", useUnmergedTree = true)
            .performClick()

        // --- MUNICIPALITY DROPDOWN ---
        composeRule.onNodeWithTag("municipality_dropdown").performClick()
        composeRule.onNodeWithText("Biratnagar", useUnmergedTree = true)
            .performClick()

        // --- WARD DROPDOWN ---
        composeRule.onNodeWithTag("ward_dropdown").performClick()
        composeRule.onNodeWithText("1", useUnmergedTree = true)
            .performClick()

        // Password & Terms
        composeRule.onNodeWithTag("reg_password").performTextInput("Pass123!")
        composeRule.onNodeWithTag("reg_confirm_password").performTextInput("Pass123!")
        composeRule.onNodeWithTag("terms_checkbox").performClick()

        // Register
        composeRule.onNodeWithTag("register_button").performScrollTo().performClick()

        // Wait for navigation (Teacher Style)
        composeRule.waitUntil(15000) {
            try {
                Intents.intended(hasComponent(UserLocationMapActivity::class.java.name))
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}
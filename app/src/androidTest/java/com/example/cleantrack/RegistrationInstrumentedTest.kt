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
        // 1. Fill Text Fields
        // Use unique email to avoid "Email already exists" error
        val testEmail = "testuser${System.currentTimeMillis()}@gmail.com"

        composeRule.onNodeWithTag("reg_fullname").performTextInput("Test User")
        composeRule.onNodeWithTag("reg_email").performTextInput(testEmail)
        composeRule.onNodeWithTag("reg_phone").performTextInput("9841234567")

        // 2. Select Address Dropdowns
        // Province
        composeRule.onNodeWithTag("province_dropdown").performClick()
        composeRule.onNodeWithText("Koshi", useUnmergedTree = true).performClick()

        // District
        composeRule.onNodeWithTag("district_dropdown").performClick()
        composeRule.onNodeWithText("Morang", useUnmergedTree = true).performClick()

        // Municipality
        composeRule.onNodeWithTag("municipality_dropdown").performClick()
        composeRule.onNodeWithText("Biratnagar", useUnmergedTree = true).performClick()

        // Ward
        composeRule.onNodeWithTag("ward_dropdown").performClick()
        composeRule.onNodeWithText("1", useUnmergedTree = true).performClick()

        // 3. Fill Password
        composeRule.onNodeWithTag("reg_password").performTextInput("Password123")
        composeRule.onNodeWithTag("reg_confirm_password").performTextInput("Password123")

        // 4. Accept Terms and Register
        composeRule.onNodeWithTag("terms_checkbox").performClick()

        // Scroll to button to ensure it is clickable
        composeRule.onNodeWithTag("register_button").performScrollTo().performClick()

        // 5. WAIT for Firebase and check Intent (Teacher Style)
        composeRule.waitUntil(timeoutMillis = 15000) {
            try {
                Intents.intended(hasComponent(UserLocationMapActivity::class.java.name))
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}
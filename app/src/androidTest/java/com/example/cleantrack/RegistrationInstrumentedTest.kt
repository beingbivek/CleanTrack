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
        // Generate unique email to prevent "Email already in use" failure
        val testEmail = "test${System.currentTimeMillis()}@gmail.com"

        // 1. Fill basic info
        composeRule.onNodeWithTag("reg_fullname").performTextInput("John Doe")
        composeRule.onNodeWithTag("reg_email").performTextInput(testEmail)
        composeRule.onNodeWithTag("reg_phone").performTextInput("9800000000")

        // 2. Dropdowns - Using filterToOne(hasClickAction()) to handle duplicate tags

        // --- PROVINCE ---
        composeRule.onAllNodesWithTag("province_dropdown")
            .filterToOne(hasClickAction())
            .performClick()
        composeRule.onNodeWithText("Koshi", useUnmergedTree = true).performClick()

        // --- DISTRICT ---
        composeRule.onAllNodesWithTag("district_dropdown")
            .filterToOne(hasClickAction())
            .performClick()
        composeRule.onNodeWithText("Morang", useUnmergedTree = true).performClick()

        // --- MUNICIPALITY ---
        composeRule.onAllNodesWithTag("municipality_dropdown")
            .filterToOne(hasClickAction())
            .performClick()
        composeRule.onNodeWithText("Biratnagar", useUnmergedTree = true).performClick()

        // --- WARD ---
        composeRule.onAllNodesWithTag("ward_dropdown")
            .filterToOne(hasClickAction())
            .performClick()
        composeRule.onNodeWithText("1", useUnmergedTree = true).performClick()

        // 3. Password & Terms
        // Note: performScrollTo() is used because these items are at the bottom of a LazyColumn
        composeRule.onNodeWithTag("reg_password").performScrollTo().performTextInput("Pass123!")
        composeRule.onNodeWithTag("reg_confirm_password").performScrollTo().performTextInput("Pass123!")
        composeRule.onNodeWithTag("terms_checkbox").performScrollTo().performClick()

        // 4. Register Action
        composeRule.onNodeWithTag("register_button").performScrollTo().performClick()

        // 5. Wait for Firebase transition (Teacher Style)
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
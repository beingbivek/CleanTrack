package com.example.cleantrack

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cleantrack.view.admin.AdminVehicleSetupActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VehicleSetupInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<AdminVehicleSetupActivity>()

    @Test
    fun testAddVehicle_Success() {
        // 1. Fill Vehicle Number and CLOSE keyboard
        composeRule.onNodeWithTag("vehicle_number_field")
            .performTextInput("BA-777-XYZ")

        // Use this to ensure the keyboard doesn't block other elements
        composeRule.onNodeWithTag("vehicle_number_field").performImeAction()

        // 2. Select Type from Dropdown
        composeRule.onNodeWithTag("type_dropdown").performClick()
        // Wait for the dropdown item to actually appear before clicking
        composeRule.onNodeWithText("VAN").assertIsDisplayed().performClick()

        // 3. Fill Capacity and scroll to ensure button is visible
        composeRule.onNodeWithTag("capacity_field")
            .performTextInput("2500")

        // 4. Click Save (Add performScrollTo if the screen is small)
        composeRule.onNodeWithTag("save_vehicle_button")
            .performScrollTo()
            .performClick()

        // 5. WAIT with a longer timeout
        // Some Firebase operations take longer than 5 seconds on emulators
        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule
                .onAllNodesWithTag("save_vehicle_button")
                .fetchSemanticsNodes()
                .isEmpty()
        }
    }

        @Test
        fun testEmptyFields_ShowsToast() {
            // Leave fields blank and try to save
            composeRule.onNodeWithTag("save_vehicle_button").performClick()

            // The Activity should still be open (Save button still exists)
            composeRule.onNodeWithTag("save_vehicle_button").assertIsDisplayed()
        }
    }

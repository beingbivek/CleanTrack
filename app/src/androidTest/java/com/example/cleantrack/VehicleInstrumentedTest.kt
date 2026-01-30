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
        // 1. Fill Vehicle Number
        composeRule.onNodeWithTag("vehicle_number_field")
            .performTextInput("BA-777-XYZ")

        // 2. Select Type from Dropdown (TRUCK is default, change to VAN)
        composeRule.onNodeWithTag("type_dropdown").performClick()
        composeRule.onNodeWithText("VAN").performClick()

        // 3. Fill Capacity
        composeRule.onNodeWithTag("capacity_field")
            .performTextInput("2500")

        // 4. Click Save
        composeRule.onNodeWithTag("save_vehicle_button").performClick()

        // --- ADD THE FIX HERE ---
        // Wait until the button disappears (meaning activity.finish() was called)
        composeRule.waitUntil(timeoutMillis = 5000) {
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

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

        // 4. Toggle the Switch (Turn off active status)
        composeRule.onNodeWithTag("status_switch").performClick()

        // 5. Click Save
        composeRule.onNodeWithTag("save_vehicle_button").performClick()

        // Since the activity finishes on success, we verify it is closing or
        // that the "Save Vehicle" button no longer exists
        composeRule.onNodeWithTag("save_vehicle_button").assertDoesNotExist()
    }

    @Test
    fun testEmptyFields_ShowsToast() {
        // Leave fields blank and try to save
        composeRule.onNodeWithTag("save_vehicle_button").performClick()

        // The Activity should still be open (Save button still exists)
        composeRule.onNodeWithTag("save_vehicle_button").assertIsDisplayed()
    }
}
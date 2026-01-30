package com.example.cleantrack

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cleantrack.view.admin.AdminVehicleSetupActivity
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VehicleSetupInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<AdminVehicleSetupActivity>()

    // Teacher Style: Setup and TearDown for Intents
    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testAddVehicle_Success() {
        // 1. Fill Vehicle Number
        composeRule.onNodeWithTag("vehicle_number_field")
            .performTextInput("BA-777-XYZ")

        // Use IME action to clear keyboard
        composeRule.onNodeWithTag("vehicle_number_field").performImeAction()

        // 2. Select Type from Dropdown
        // Using filterToOne(hasClickAction) to avoid the "Found 2 nodes" error
        composeRule.onAllNodesWithTag("type_dropdown")
            .filterToOne(hasClickAction())
            .performClick()

        composeRule.onNodeWithText("VAN", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        // 3. Fill Capacity
        composeRule.onNodeWithTag("capacity_field")
            .performTextInput("2500")

        // Hide keyboard after typing numbers
        composeRule.onNodeWithTag("capacity_field").performImeAction()

        // 4. Click Save
        composeRule.onNodeWithTag("save_vehicle_button")
            .performScrollTo()
            .performClick()

        // 5. WAIT for Firebase and Activity Finish
        // Since it returns to the list, we wait for the save button to disappear
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
        composeRule.onNodeWithTag("save_vehicle_button")
            .performScrollTo()
            .performClick()

        // The Activity should still be open (Save button still exists)
        composeRule.onNodeWithTag("save_vehicle_button").assertIsDisplayed()
    }
}
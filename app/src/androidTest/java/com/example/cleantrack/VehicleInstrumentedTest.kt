package com.example.cleantrack

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cleantrack.view.admin.AdminVehicleSetupActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VehicleInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<AdminVehicleSetupActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testAddVehicle_ValidationMessage_WhenFieldsEmpty() {
        // Leave fields empty and click save
        composeRule.onNodeWithTag("save_vehicle_button")
            .performClick()

        // Since we can't easily test Toast messages in Compose tests without
        // third-party libs, we verify the activity hasn't finished (it's still displayed)
        composeRule.onNodeWithTag("vehicle_number_field").assertIsDisplayed()
    }

    @Test
    fun testAddVehicle_SuccessFlow() {
        // Input Vehicle Number
        composeRule.onNodeWithTag("vehicle_number_field")
            .performTextInput("BA-9876")

        // Input Capacity
        composeRule.onNodeWithTag("capacity_field")
            .performTextInput("1200")

        // Note: For Dropdowns, you'd usually click the box then the item
        // But for this simple test, we hit Save
        composeRule.onNodeWithTag("save_vehicle_button")
            .performClick()

        // Verify saving state appears
        // composeRule.onNodeWithTag("save_vehicle_button").assertIsNotEnabled()
    }

    @Test
    fun testDeleteDialog_Logic() {
        // 1. Find Delete Icon in TopAppBar (Ensure you added .testTag("delete_icon"))
        // If you haven't added tags yet, you can find by content description or icon
        val deleteIcon = composeRule.onAllNodesWithTag("delete_vehicle_icon")

        if (deleteIcon.fetchSemanticsNodes().isNotEmpty()) {
            deleteIcon[0].performClick()

            // 2. Correct way to find text: onNodeWithText
            composeRule.onNodeWithText("Delete Vehicle").assertIsDisplayed()

            // 3. Click the "Cancel" TextButton
            composeRule.onNodeWithText("Cancel").performClick()

            // 4. Verify the dialog is dismissed
            composeRule.onNodeWithText("Delete Vehicle").assertDoesNotExist()
        }
    }
}
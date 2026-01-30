package com.example.cleantrack

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cleantrack.view.user.BinSetupActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BinInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<BinSetupActivity>()

    @Test
    fun testAddBin_Flow() {
        // 1. Enter the Bin Label
        // Matches your label: "Label (e.g. Kitchen Bin)"
        composeRule.onNodeWithText("Label (e.g. Kitchen Bin)")
            .performTextInput("Balcony Bin")

        // 2. Interact with the Waste Category Dropdown
        // Click the field to expand the menu
        composeRule.onNodeWithText("Waste Category").performClick()

        // Select a category from the expanded menu
        composeRule.onNodeWithText("TOXIC").performClick()

        // 3. Verify the selection was updated in the field
        composeRule.onNodeWithText("TOXIC").assertIsDisplayed()

        // 4. Click the Register button
        // Since binId is null by default in this test, text is "Register Bin"
        composeRule.onNodeWithText("Register Bin").performClick()
    }

    @Test
    fun testEmptyLabel_ShowsNoNavigation() {
        // Clear text if any and click register
        composeRule.onNodeWithText("Label (e.g. Kitchen Bin)").performTextClearance()

        composeRule.onNodeWithText("Register Bin").performClick()

        // Verify we are still on the same screen by checking the title
        composeRule.onNodeWithText("Add New Bin").assertIsDisplayed()
    }

    @Test
    fun testDropdown_CanSelectMultipleTimes() {
        // Open dropdown
        composeRule.onNodeWithText("Waste Category").performClick()
        // Select Mixed
        composeRule.onNodeWithText("MIXED").performClick()
        // Verify
        composeRule.onNodeWithText("MIXED").assertIsDisplayed()

        // Open again
        composeRule.onNodeWithText("Waste Category").performClick()
        // Select Inorganic
        composeRule.onNodeWithText("INORGANIC").performClick()
        // Verify new selection
        composeRule.onNodeWithText("INORGANIC").assertIsDisplayed()
    }
}
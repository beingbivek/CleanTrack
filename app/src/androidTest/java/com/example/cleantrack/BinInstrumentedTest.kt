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
    fun testRegisterBin_Success() {
        // 1. Enter Bin Label
        composeRule.onNodeWithTag("bin_label_field")
            .performTextInput("Kitchen Organic Bin")

        // 2. Open Category Dropdown
        composeRule.onNodeWithTag("bin_category_dropdown")
            .performClick()

        // 3. Select 'TOXIC' from the dropdown list
        // Note: Dropdown items usually exist in the unmerged tree
        composeRule.onNodeWithText("TOXIC")
            .performClick()

        // 4. Verify selection reflected in the field
        composeRule.onNodeWithTag("bin_category_dropdown")
            .assert(hasText("TOXIC"))

        // 5. Click Register
        composeRule.onNodeWithTag("bin_save_button")
            .performClick()

        // 6. Verification: The activity should finish on success
        // We check that the 'Register Bin' button is no longer visible
        composeRule.onNodeWithTag("bin_save_button").assertDoesNotExist()
    }

    @Test
    fun testEmptyLabel_Validation() {
        // Leave label empty and click register
        composeRule.onNodeWithTag("bin_save_button").performClick()

        // The activity should still be open
        composeRule.onNodeWithTag("bin_save_button").assertIsDisplayed()
    }
}
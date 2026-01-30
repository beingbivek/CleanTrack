package com.example.cleantrack

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cleantrack.view.driver.BinCollectionActivity
import com.example.cleantrack.view.driver.DriverDashboardActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BinCollectionInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<BinCollectionActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testSuccessfulCollection_NavigatesBackToDashboard() {
        // 1. Enter the weight of the waste
        composeRule.onNodeWithTag("weight_input")
            .performTextInput("5.5")

        // 2. Select bin type or enter additional details if needed
        // composeRule.onNodeWithTag("bin_id_dropdown").performClick()

        // 3. Click the "Submit Collection" button
        composeRule.onNodeWithTag("submit_collection_button")
            .performClick()

        // 4. Verify navigation
        // After a successful collection, drivers usually go back to the dashboard
        // or the active trip screen.
        Intents.intended(hasComponent(DriverDashboardActivity::class.java.name))
    }
}
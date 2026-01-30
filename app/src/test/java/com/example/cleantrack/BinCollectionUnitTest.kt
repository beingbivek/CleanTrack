package com.example.cleantrack

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.cleantrack.model.BinCollectionModel
import com.example.cleantrack.repository.*
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class BinCollectionUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun add_bin_collection_success_test() {
        // 1. Setup - Mock ALL 5 repositories required by ActiveTripViewModel
        val activeTripRepo = mock<ActiveTripRepo>()
        val userRepo = mock<UserRepo>()
        val binRepo = mock<BinRepo>()
        val collectionRepo = mock<BinCollectionRepo>()
        val pointsRepo = mock<PointsRepo>()

        val viewModel = ActiveTripViewModel(
            activeTripRepo,
            userRepo,
            binRepo,
            collectionRepo,
            pointsRepo
        )

        // 2. Prepare mock data
        val testCollection = BinCollectionModel(
            binId = "BIN_001",
            driverId = "DRIVER_99",
            userId = "USER_123",
            tripId = "TRIP_456",
            weight = 5.5,
            pointsAwarded = 10
        )

        // 3. Define behavior for addBinCollection(model, callback)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Collection saved")
            null
        }.`when`(collectionRepo).addBinCollection(eq(testCollection), any())

        // 4. Execution
        var successResult = false
        var messageResult = ""

        viewModel.addBinCollection(testCollection) { success, msg ->
            successResult = success
            messageResult = msg
        }

        // 5. Assertions
        assertTrue("The collection should be saved successfully", successResult)
        assertEquals("Collection saved", messageResult)

        // 6. Verification
        verify(collectionRepo).addBinCollection(eq(testCollection), any())
    }
}
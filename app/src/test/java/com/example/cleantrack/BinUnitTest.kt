package com.example.cleantrack

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.cleantrack.model.BinModel
import com.example.cleantrack.repository.BinRepo
import com.example.cleantrack.viewmodel.BinViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class BinUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun add_bin_success_test() {
        // 1. Setup
        val repo = mock<BinRepo>()
        val viewModel = BinViewModel(repo)
        val testBin = BinModel(
            binId = "B001",
            ownerUserId = "user_apple_123",
            label = "Front Gate",
            category = "ORGANIC"
        )

        // 2. Mock Behavior: addBin(model, cb)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Bin added")
            null
        }.`when`(repo).addBin(eq(testBin), any())

        // 3. Execution
        var successResult = false
        var messageResult = ""

        viewModel.addBin(testBin) { success, msg ->
            successResult = success
            messageResult = msg
        }

        // 4. Assertions
        assertTrue(successResult)
        assertEquals("Bin added", messageResult)
        verify(repo).addBin(eq(testBin), any())
    }

    @Test
    fun load_user_bins_updates_livedata_test() {
        // 1. Setup
        val repo = mock<BinRepo>()
        val viewModel = BinViewModel(repo)
        val testUserId = "user_apple_123"
        val mockBinsList = listOf(
            BinModel(binId = "B1", ownerUserId = testUserId, label = "Kitchen"),
            BinModel(binId = "B2", ownerUserId = testUserId, label = "Garden")
        )

        // 2. Mock Behavior: getBinsByUser(userId, callback)
        // Callback signature: (Boolean, String, List<BinModel>?) -> Unit
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, List<BinModel>?) -> Unit>(1)
            callback(true, "Fetched", mockBinsList)
            null
        }.`when`(repo).getBinsByUser(eq(testUserId), any())

        // 3. Execution
        viewModel.loadUserBins(testUserId)

        // 4. Assertions
        assertEquals(mockBinsList, viewModel.bins.value)
        assertEquals(2, viewModel.bins.value?.size)
        // Verify loading was toggled off
        assertEquals(false, viewModel.loading.value)

        verify(repo).getBinsByUser(eq(testUserId), any())
    }
}
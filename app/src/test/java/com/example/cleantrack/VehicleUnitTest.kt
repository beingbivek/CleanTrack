package com.example.cleantrack

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.cleantrack.model.VehicleModel
import com.example.cleantrack.repository.VehicleRepo
import com.example.cleantrack.viewmodel.VehicleViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class VehicleUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun add_vehicle_success_test() {
        // 1. Setup with exact model fields
        val repo = mock<VehicleRepo>()
        val viewModel = VehicleViewModel(repo)
        val testVehicle = VehicleModel(
            vehicleId = "V101",
            vehicleNumber = "BA-1234",
            type = "TRUCK",
            capacity = "500kg",
            active = true
        )

        // 2. Mock Behavior: addVehicle(model, callback)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Vehicle added")
            null
        }.`when`(repo).addVehicle(eq(testVehicle), any())

        // 3. Execution
        var successResult = false
        var messageResult = ""

        viewModel.addVehicle(testVehicle) { success, msg ->
            successResult = success
            messageResult = msg
        }

        // 4. Assertions
        assertTrue("Expected success to be true", successResult)
        assertEquals("Vehicle added", messageResult)
        verify(repo).addVehicle(eq(testVehicle), any())
    }

    @Test
    fun get_all_vehicles_updates_livedata_test() {
        // 1. Setup
        val repo = mock<VehicleRepo>()
        val viewModel = VehicleViewModel(repo)
        val mockList = listOf(
            VehicleModel("V1", "BA-111", "TRUCK", "1000kg", true),
            VehicleModel("V2", "BA-222", "VAN", "500kg", false)
        )

        // 2. Mock Behavior for getAllVehicles
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, List<VehicleModel>) -> Unit>(0)
            callback(true, "Vehicles loaded", mockList)
            null
        }.`when`(repo).getAllVehicles(any())

        // 3. Execution
        viewModel.getAllVehicles()

        // 4. Assertions
        assertEquals(mockList, viewModel.vehicles.value)
        assertEquals(2, viewModel.vehicles.value?.size)
        assertEquals(false, viewModel.loading.value)
    }

    @Test
    fun update_vehicle_success_test() {
        // 1. Setup - Create a vehicle with modified data
        val repo = mock<VehicleRepo>()
        val viewModel = VehicleViewModel(repo)

        // Let's simulate changing the capacity of an existing vehicle
        val updatedVehicle = VehicleModel(
            vehicleId = "V101",
            vehicleNumber = "BA-1234",
            type = "TRUCK",
            capacity = "800kg", // Changed from 500kg
            active = true
        )

        // 2. Mock Behavior for updateVehicle(model, callback)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Vehicle updated")
            null
        }.`when`(repo).updateVehicle(eq(updatedVehicle), any())

        // 3. Execution
        var successResult = false
        var messageResult = ""

        viewModel.updateVehicle(updatedVehicle) { success, msg ->
            successResult = success
            messageResult = msg
        }

        // 4. Assertions
        assertTrue("Expected update to return true", successResult)
        assertEquals("Vehicle updated", messageResult)

        // 5. Verification - Ensure the repo's update method was called with the right object
        verify(repo).updateVehicle(eq(updatedVehicle), any())
    }
}
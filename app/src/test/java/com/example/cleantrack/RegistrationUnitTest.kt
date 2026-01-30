package com.example.cleantrack

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.cleantrack.repository.BinCollectionRepo
import com.example.cleantrack.repository.UserRepo
import com.example.cleantrack.viewmodel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class RegistrationUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun register_success_test() {
        // 1. Setup - Mock both repos to prevent "Method myPid not mocked" crash
        val repo = mock<UserRepo>()
        val collectionRepo = mock<BinCollectionRepo>()
        val viewModel = UserViewModel(repo, collectionRepo)

        val testEmail = "apple1@gmail.com"
        val testPassword = "apple@12"

        // 2. Mock Behavior: (Boolean, String, String) -> Unit
        // In register, index 0=email, 1=password, 2=callback
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(2)
            callback(true, "Registration Successful", "apple_user_id_001")
            null
        }.`when`(repo).register(eq(testEmail), eq(testPassword), any())

        // 3. Execution
        var successResult = false
        var messageResult = ""
        var userIdResult = ""

        viewModel.register(testEmail, testPassword) { success, msg, userId ->
            successResult = success
            messageResult = msg
            userIdResult = userId
        }

        // 4. Assertions
        assertTrue("Registration should be successful", successResult)
        assertEquals("Registration Successful", messageResult)
        assertEquals("apple_user_id_001", userIdResult)

        // Verify loading state was turned off
        assertEquals(false, viewModel.loading.value)

        // 5. Verification
        verify(repo).register(eq(testEmail), eq(testPassword), any())
    }
}
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

class LoginUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun login_success_test() {
        // 1. Mock BOTH repositories
        val repo = mock<UserRepo>()
        val collectionRepo = mock<BinCollectionRepo>()

        // 2. Initialize ViewModel by passing BOTH mocks
        // This prevents the code from trying to initialize BinCollectionRepoImpl()
        val viewModel = UserViewModel(repo, collectionRepo)

        val testEmail = "apple1@gmail.com"
        val testPassword = "apple@12"

        // 3. Define Mock Behavior
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String?, String?, String?) -> Unit>(2)
            callback(true, "Login success", "apple_user_id_001", "USER")
            null
        }.`when`(repo).login(eq(testEmail), eq(testPassword), any())

        // 4. Execution
        var successResult = false
        var messageResult: String? = ""

        viewModel.login(testEmail, testPassword) { success, msg, _, _ ->
            successResult = success
            messageResult = msg
        }

        // 5. Assertions
        assertTrue("The login should return true", successResult)
        assertEquals("Login success", messageResult)
        assertEquals(false, viewModel.loading.value)

        // 6. Verification
        verify(repo).login(eq(testEmail), eq(testPassword), any())
    }
}
package com.example.cleantrack.view.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cleantrack.repository.RouteRepoImpl
import com.example.cleantrack.viewModel.RouteViewModel

class AdminRouteSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val vm = RouteViewModel(RouteRepoImpl())

        setContent {
            AdminRouteSetupScreen(
                savedInstanceState = savedInstanceState,
                routeViewModel = vm
            )
        }
    }
}

package com.example.cleantrack.view.common

import android.app.Activity
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.cleantrack.view.auth.StartActivity
import com.example.cleantrack.viewmodel.UserViewModel

@Composable
fun LogoutDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    viewModel: UserViewModel
) {
    val context = LocalContext.current
    val activity = context as? Activity

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = "Sign Out") },
            text = { Text(text = "Are you sure you want to log out of CleanTrack?") },
            confirmButton = {
                TextButton(onClick = {
                    // 1. Sign out from Firebase
                    viewModel.logout()

                    // 2. Navigate and clear history
                    val intent = Intent(context, StartActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                    activity?.finish()
                }) {
                    Text("Yes, Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

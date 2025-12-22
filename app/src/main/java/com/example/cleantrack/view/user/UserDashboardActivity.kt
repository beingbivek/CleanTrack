package com.example.cleantrack.view.user

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.Red
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.view.auth.StartActivity
import com.example.cleantrack.view.common.LogoutDialog
import com.example.cleantrack.viewmodel.UserViewModel

class UserDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserDashboardBody()
        }
    }
}


@Composable
fun UserDashboardBody() {

    val context = LocalContext.current
    val activity = context as Activity

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LogoutDialog(
        showDialog = showLogoutDialog,
        onDismiss = { showLogoutDialog = false },
        viewModel = userViewModel
    )

//    val privacyText = getLatestPrivacyPolicyDesc()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showLogoutDialog = true
            }) {
               Icon(
                   Icons.Default.Logout,
                   contentDescription = "Logout",
                   tint = Red
               )
            }
        }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                "User Dashboard",
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(50.dp))

//            Text(text = privacyText)
        }
    }
}


//@Composable
//fun getLatestPrivacyPolicyDesc(): String {
//    val firestore = Firebase.firestore
//    val description = remember { mutableStateOf("Loading...") }
//
//    LaunchedEffect(Unit) {
//        firestore.collection("privacy_policy")
//            .orderBy("date", Query.Direction.DESCENDING)
//            .limit(1)  // Get only the latest document
//            .get()
//            .addOnSuccessListener { result ->
//                if (!result.isEmpty) {
//                    val doc = result.documents[0]
//                    description.value = doc.getString("description") ?: "No description found"
//                } else {
//                    description.value = "No documents found"
//                }
//            }
//            .addOnFailureListener {
//                description.value = "Failed to load data"
//            }
//    }

//    return description.value
//}

@Preview
@Composable
fun UserDashboardPreview(){
    UserDashboardBody()
}
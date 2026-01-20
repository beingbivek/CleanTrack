package com.example.cleantrack.view.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.R
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.ButtonColor
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.viewmodel.UserViewModel

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. This enables the drawing behind system bars
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            StartBody()
        }
    }
}

@Composable
fun StartBody() {
    val context = LocalContext.current
    val activity = context as Activity

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    var isCheckingSession by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        val currentUserId = userViewModel.getCurrentUserId()

        if (currentUserId != null) {
            userViewModel.checkAndNavigateAfterLogin(currentUserId, context, activity)
        } else {
            isCheckingSession = false
        }
    }

    if (isCheckingSession) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F7)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = null,
                    modifier = Modifier.size(150.dp)
                )
                Spacer(modifier = Modifier.height(50.dp))
                CircularProgressIndicator(color = Color(0xFF4F96D8))
            }
        }
    } else {
        // 2. contentWindowInsets = WindowInsets(0, 0, 0, 0) removes the "grey" bar padding
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = White
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    // We keep padding(padding) ONLY if we want to avoid overlapping icons,
                    // but for a true full-screen look, we use the background color on the Scaffold.
                    .background(color = White),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                item {
                    // Use a Spacer to manually adjust for the status bar height if needed
                    Spacer(modifier = Modifier.height(150.dp))

                    Text(
                        text = "Welcome to \nCleanTrack!",
                        style = TextStyle(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(bottom = 40.dp)
                    )
                }

                item {
                    Image(
                        painter = painterResource(R.drawable.app_logo),
                        contentDescription = null,
                        modifier = Modifier.size(180.dp)
                    )
                }

                item {
                    Text(
                        text = "Your journey to smarter,\ncooler recycling starts now",
                        style = TextStyle(
                            fontSize = 18.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier.padding(top = 40.dp, bottom = 40.dp),
                    )
                }

                item {
                    Button(
                        onClick = {
                            val intent = Intent(context, RegistrationActivity::class.java)
                            context.startActivity(intent)
                            activity.finish()
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(60.dp)
                            .background(
                                brush = Brush.horizontalGradient(colors = ButtonColor),
                                shape = RoundedCornerShape(15.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    ) {
                        Text(
                            "Sign Up",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)
                            activity.finish()
                        }
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4F96D8)
                        )
                    }
                }

                item {
                    // Bottom Spacer to ensure content doesn't hit the very bottom edge
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}
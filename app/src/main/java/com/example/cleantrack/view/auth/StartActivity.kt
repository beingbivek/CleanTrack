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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.R
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.ButtonColor
import com.example.cleantrack.viewmodel.UserViewModel
import androidx.compose.material3.CircularProgressIndicator

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

    // 1. State to control UI visibility
    var isCheckingSession by remember { mutableStateOf(true) }

    // 2. Run the check immediately on launch
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        val currentUserId = userViewModel.getCurrentUserId()

        if (currentUserId != null) {
            // User exists! Use your existing logic to handle role/location routing
            userViewModel.checkAndNavigateAfterLogin(currentUserId, context, activity)
            // We don't set isCheckingSession to false because we are navigating away
        } else {
            // No user found, show the Start UI
            isCheckingSession = false
        }
    }

    // 3. Conditional Rendering
    if (isCheckingSession) {
        // ✅ THIS IS YOUR "CONTINUED SPLASH"
        // It keeps the Splash look alive while the logic runs
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
                Spacer(modifier = Modifier.size(50.dp))
                CircularProgressIndicator(color = Color(0xFF4F96D8))
            }
        }
    } else {

        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(color = Color(0xFFF7F7F7))
                    .padding(horizontal = 24.dp, vertical = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {

                Text(
                    text = "Welcome to \nCleanTrack!",
                    style = TextStyle(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(bottom = 50.dp)
                )

                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = null
                )

                Text(
                    text = "Your journey to smarter,\ncooler recycling starts now",
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier.padding(top = 40.dp, bottom = 24.dp),
                )


                Button(
                    onClick = {
                        val intent = Intent(context, RegistrationActivity::class.java)
                        context.startActivity(intent)
                        activity.finish()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
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

                Spacer(modifier = Modifier.size(24.dp))

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

                Spacer(modifier = Modifier.weight(0.5f))
            }
        }
    }
}

@Preview
@Composable
fun StartPreview() {
    StartBody()

}

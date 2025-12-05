package com.example.cleantrack

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.example.cleantrack.ui.theme.ButtonColor

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
                Text("Sign Up", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
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

@Preview
@Composable
fun StartPreview() {
        StartBody()

}
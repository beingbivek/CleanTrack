package com.example.cleantrack.view.driver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.ButtonColor
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.Red
import com.example.cleantrack.ui.theme.TextBoxColor
import com.example.cleantrack.ui.theme.Transparent
import com.example.cleantrack.ui.theme.White

class DriverDashBoardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

        }
    }
}

@Composable
fun DriverDashboardScreen(
    driverName: String = "Ishan",
    routeName: String = "Route A - Ward 5",
    completed: Int = 12,
    skipped: Int = 2,
    totalStops: Int = 20,
    onStartRoute: () -> Unit = {},
    onViewRoute: () -> Unit = {}
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(White)
                .padding(20.dp)
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            // Screen Title
            Text(
                text = "Driver Dashboard",
                style = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Black,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Welcome Message
            Text(
                text = "Welcome, $driverName 👋",
                style = TextStyle(
                    fontSize = 18.sp,
                    color = Black,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(25.dp))

            // New Top Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TextBoxColor, shape = RoundedCornerShape(18.dp))
                    .padding(50.dp)
            ) {
                Column {
                    Text(
                        text = "Today's Overview",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Black
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "You have 20 stops today.\nMake sure to complete your route on time!",
                        fontSize = 16.sp,
                        color = Black,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    Button(
                        onClick = onViewRoute,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(60.dp)
                            .background(
                                brush = Brush.horizontalGradient(colors = ButtonColor),
                                shape = RoundedCornerShape(15.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = Transparent),
                    ) {
                        Text("View Route", color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))



            // Assigned Route Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TextBoxColor, shape = RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Assigned Route",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Black
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = routeName,
                        fontSize = 18.sp,
                        color = Green,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    // Progress Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RouteStat("Completed", completed.toString(), Green)
                        RouteStat("Skipped", skipped.toString(), color = Red)
                        RouteStat("Pending", (totalStops - completed - skipped).toString(), color = Red)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // View Route Button

                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Status Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFe8f5e9), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Status: Waiting to Start",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Green
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Start Route Button
            Button(
                onClick = onStartRoute,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Green, RoundedCornerShape(18.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Transparent),
            ) {
                Text(
                    text = "Start Route",
                    fontSize = 20.sp,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
            }

        }
    }
}

@Composable
fun RouteStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 14.sp, color = Black)
    }
}

@Preview(showBackground = true)
@Composable
fun DriverDashboardPreview() {
    DriverDashboardScreen()
}


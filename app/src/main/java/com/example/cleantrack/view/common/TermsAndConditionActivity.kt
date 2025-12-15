package com.example.cleantrack.view.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.ButtonColor
import com.example.cleantrack.ui.theme.White

class TermsAndConditionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TermsAndConditionBody()
        }
    }
}

@Composable
fun TermsAndConditionBody() {
    var checked by remember { mutableStateOf(true) }


    Scaffold { padding ->
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
                "Terms and Condition",
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFe4e6e5)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFe4e6e5))
                        .verticalScroll(rememberScrollState()),
                ) {

                    Card(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                            .height(950.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = White
                        )
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, top = 15.dp, end = 20.dp, bottom = 20.dp)
                        ) {

                            Text(
                                "1. Acceptance of Terms",
                                style = TextStyle(
                                    color = Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                "By creating an account on CleanTrack, you agree to follow these Terms and Conditions. " +
                                        "If you do not agree, please stop using the app immediately.",
                                style = TextStyle(color = Black, fontSize = 15.sp),
                                modifier = Modifier.padding(top = 3.dp)
                            )

                            Spacer(modifier = Modifier.height(15.dp))

                            Text(
                                "2. User Responsibilities",
                                style = TextStyle(
                                    color = Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                "You agree to: \n" +
                                        "• Provide accurate personal information\n" +
                                        "• Keep your login details secure\n" +
                                        "• Not misuse, hack, or exploit CleanTrack or its features\n" +
                                        "• Follow all recycling and waste-management guidelines within the app",
                                style = TextStyle(color = Black, fontSize = 15.sp),
                                modifier = Modifier.padding(top = 3.dp)
                            )

                            Spacer(modifier = Modifier.height(15.dp))

                            Text(
                                "3. Account Types (User, Admin, Driver)",
                                style = TextStyle(
                                    color = Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                "CleanTrack provides different access levels. Admins and Drivers must follow additional internal policies. " +
                                        "Misuse of assigned roles may result in account suspension.",
                                style = TextStyle(color = Black, fontSize = 15.sp),
                                modifier = Modifier.padding(top = 3.dp)
                            )

                            Spacer(modifier = Modifier.height(15.dp))

                            Text(
                                "4. Data Collection & Privacy",
                                style = TextStyle(
                                    color = Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                "We collect basic information like name, phone, email, and location (municipality & ward) " +
                                        "to provide better waste-tracking services. Your data is safe and never sold to third parties.",
                                style = TextStyle(color = Black, fontSize = 15.sp),
                                modifier = Modifier.padding(top = 3.dp)
                            )

                            Spacer(modifier = Modifier.height(15.dp))

                            Text(
                                "5. Service Availability",
                                style = TextStyle(
                                    color = Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                "CleanTrack may experience maintenance downtime or temporary unavailability. " +
                                        "We are not responsible for delays or missed pickups caused by technical issues.",
                                style = TextStyle(color = Black, fontSize = 15.sp),
                                modifier = Modifier.padding(top = 3.dp)
                            )

                            Spacer(modifier = Modifier.height(15.dp))

                            Text(
                                "6. Termination of Accounts",
                                style = TextStyle(
                                    color = Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                "We may suspend or delete accounts that violate these terms, attempt fraud, or cause harm to the platform.",
                                style = TextStyle(color = Black, fontSize = 15.sp),
                                modifier = Modifier.padding(top = 3.dp)
                            )

                            Spacer(modifier = Modifier.height(15.dp))

                            Text(
                                "7. Contact Information",
                                style = TextStyle(
                                    color = Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                "For any questions or concerns, please contact us at:\n" +
                                        "Email: rizzcycle@gmail.com",
                                style = TextStyle(color = Black, fontSize = 15.sp),
                                modifier = Modifier.padding(top = 3.dp)
                            )

                            Spacer(modifier = Modifier.height(15.dp))

                            Text(
                                "Last Updated: December 1, 2025",
                                style = TextStyle(
                                    color = Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                modifier = Modifier.padding(top = 10.dp)
                            )

                            Spacer(modifier = Modifier.height(25.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = { /* TODO: Implement 'Sign Up' action */ },
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
                                        "Agree",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }


                    }
                }




            }
        }
    }
}

@Preview
@Composable
fun TermsAndConditionPreview(){
    TermsAndConditionBody()
}
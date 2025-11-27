package com.example.cleantrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.CleanTrackTheme
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.White

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SettingsBody()
        }
    }
}

@Composable
fun SettingsBody() {
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
                "Settings",
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
                ){
                    Row(modifier = Modifier
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_account_circle_24),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(50.dp)
                                .padding(start = 10.dp)
                        )
                        Text("Account",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start=10.dp)
                        )


                    }

                    Card(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                            .height(160.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = White
                        )
                    ) {

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 15.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Edit Profile",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding( vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f)
                                )
                            }

                        }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Change Password",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding( vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f)
                                )
                            }

                        }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Delete Account",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                            }

                        }

                    }


                    Row(modifier = Modifier
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.home_icon),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                                .padding(start = 10.dp)
                        )
                        Text("Household",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start=10.dp)
                        )


                    }

                    Card(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                            .height(110.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = White
                        )
                    ) {

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 15.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Home Location",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )


                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding( vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f)
                                )
                            }

                        }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Municipality & Ward",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_account_circle_24),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(50.dp)
                                .padding(start = 10.dp)
                        )
                        Text("Notifications",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start=10.dp)
                        )


                    }

                    Card(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                            .height(310.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = White
                        )
                    ) {

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 15.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Truck Near Alerts",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )

                                Switch(
                                    checked = checked,
                                    modifier = Modifier.size(width = 40.dp, height = 24.dp)
                                        .padding(start=130.dp),
                                    onCheckedChange = {
                                        checked = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = White,
                                        checkedTrackColor = Green,
                                    )
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding( vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f)
                                )
                            }

                        }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Pickup Reminder",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                                Switch(
                                    checked = checked,
                                    modifier = Modifier.size(width = 40.dp, height = 24.dp)
                                        .padding(start=135.dp),
                                    onCheckedChange = {
                                        checked = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = White,
                                        checkedTrackColor = Green,
                                    )
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding( vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f)
                                )
                            }

                        }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Payment Alerts",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                                Switch(
                                    checked = checked,
                                    modifier = Modifier.size(width = 40.dp, height = 24.dp)
                                        .padding(start=150.dp),
                                    onCheckedChange = {
                                        checked = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = White,
                                        checkedTrackColor = Green,
                                    )
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding( vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f)
                                )
                            }

                        }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Waste Rating Notifications",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                                Switch(
                                    checked = checked,
                                    modifier = Modifier.size(width = 40.dp, height = 24.dp)
                                        .padding(start=50.dp),
                                    onCheckedChange = {
                                        checked = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = White,
                                        checkedTrackColor = Green,
                                    )
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding( vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f)
                                )
                            }

                        }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Municipality Announcements",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                                Switch(
                                    checked = checked,
                                    modifier = Modifier.size(width = 40.dp, height = 24.dp)
                                        .padding(start=20.dp),
                                    onCheckedChange = {
                                        checked = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = White,
                                        checkedTrackColor = Green,
                                    )
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding( vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f)
                                )
                            }

                        }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Toggle All Notifications",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                                Switch(
                                    checked = checked,
                                    modifier = Modifier.size(width = 40.dp, height = 24.dp)
                                        .padding(start=80.dp),
                                    onCheckedChange = {
                                        checked = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = White,
                                        checkedTrackColor = Green,
                                    )
                                )
                            }

                        }

                    }

                    Row(modifier = Modifier
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_account_circle_24),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(50.dp)
                                .padding(start = 10.dp)
                        )
                        Text("Privacy",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start=10.dp)
                        )


                    }

                    Card(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                            .height(110.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = White
                        )
                    ) {

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 15.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Privacy Policy",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding( vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f)
                                )
                            }

                        }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Terms & Conditions",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_account_circle_24),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(50.dp)
                                .padding(start = 10.dp)
                        )
                        Text("Help",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start=10.dp)
                        )


                    }

                    Card(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                            .height(110.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = White
                        )
                    ) {

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 15.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Contact Support",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding( vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f)
                                )
                            }

                        }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "FAQs",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                            }
                        }
                    }


                    Row(modifier = Modifier
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_account_circle_24),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(50.dp)
                                .padding(start = 10.dp)
                        )
                        Text("About",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start=10.dp)
                        )


                    }

                    Card(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                            .height(110.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = White
                        )
                    ) {

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 15.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "App Version",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding( vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f)
                                )
                            }

                        }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 20.dp, top = 5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "About CleanTrack",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))




                }

            }
        }
    }
}

@Preview
@Composable
fun SettingsPreview(){
    SettingsBody()
}
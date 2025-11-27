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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
                        .background(Color(0xFFe4e6e5)),
                ){
                    Row(modifier = Modifier
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_account_circle_24),
                            contentDescription = null,
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
                                    .padding(start = 20.dp, top = 15.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.LightGray
                                )

                                Text(
                                    "Settings",
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
                                    "Settings",
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
                                    "Settings",
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
                            modifier = Modifier.size(50.dp)
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
                                    "Settings",
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
                                    "Settings",
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
                            .height(300.dp).fillMaxWidth(),
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
                                    "Settings",
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
                                    "Settings",
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
                                    "Settings",
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
                                    "Settings",
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
                                    "Settings",
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
                                    "Settings",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start=10.dp)
                                )
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
fun SettingsPreview(){
    SettingsBody()
}
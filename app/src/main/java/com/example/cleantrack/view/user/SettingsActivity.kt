package com.example.cleantrack.view.user

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.R
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.view.common.ContactSupportActivity
import com.example.cleantrack.view.common.PrivacyPolicyActivity
import com.example.cleantrack.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SettingsBody()
        }
    }
}

//Reusable Composable Functions
@Composable
fun SettingsSectionHeader(iconResId: Int, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier
                .size(50.dp)
                .padding(start = 10.dp)
        )
        Text(
            title,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
fun SettingListItem(
    text: String,
    showDivider: Boolean = true,
    trailingContent: @Composable () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier.width(60.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                trailingContent()
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                thickness = 1.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SettingsCard(items: List<@Composable () -> Unit>) {
    Card(
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column {
            items.forEach { item ->
                item()
            }
        }
    }
}

@Composable
fun NotificationSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = White,
            checkedTrackColor = Green,
        )
    )
}

// Main Composable

@Composable
fun SettingsBody() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var truckNearAlerts by remember { mutableStateOf(true) }
    var pickupReminder by remember { mutableStateOf(true) }
    var paymentAlerts by remember { mutableStateOf(true) }
    var wasteRatingNotifications by remember { mutableStateOf(true) }
    var municipalityAnnouncements by remember { mutableStateOf(true) }


    // 2. State variable for the "Toggle All" switch
    val allNotificationsChecked = truckNearAlerts && pickupReminder && paymentAlerts && wasteRatingNotifications && municipalityAnnouncements

    val onToggleAll: (Boolean) -> Unit = { isChecked ->
        truckNearAlerts = isChecked
        pickupReminder = isChecked
        paymentAlerts = isChecked
        wasteRatingNotifications = isChecked
        municipalityAnnouncements = isChecked
    }

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
                    .background(Color(0xFFe4e6e5))
                    .verticalScroll(rememberScrollState()),
            ) {
                // Account Section
                SettingsSectionHeader(R.drawable.baseline_account_circle_24, "Account")
                SettingsCard(
                    items = listOf(
                        { SettingListItem("Edit Profile") },
                        { SettingListItem("Change Password") },
                        { SettingListItem("Delete Account", showDivider = false) }
                    )
                )

                // Household Section
                SettingsSectionHeader(R.drawable.baseline_home_24, "Household")
                SettingsCard(
                    items = listOf(
                        { SettingListItem("Home Location") },
                        { SettingListItem("Municipality & Ward", showDivider = false) }
                    )
                )

                // Notifications Section
                SettingsSectionHeader(R.drawable.baseline_notifications_24, "Notifications")
                SettingsCard(
                    items = listOf(

                        {
                            SettingListItem("Toggle All Notifications", trailingContent = {
                                // Toggle All state is derived, and its action updates all other states
                                NotificationSwitch(allNotificationsChecked, onToggleAll)
                            })
                        },

                        {
                            SettingListItem("Truck Near Alerts", trailingContent = {
                                NotificationSwitch(truckNearAlerts) { truckNearAlerts = it }
                            })
                        },
                        {
                            SettingListItem("Pickup Reminder", trailingContent = {
                                NotificationSwitch(pickupReminder) { pickupReminder = it }
                            })
                        },
                        {
                            SettingListItem("Payment Alerts", trailingContent = {
                                NotificationSwitch(paymentAlerts) { paymentAlerts = it }
                            })
                        },
                        {
                            SettingListItem("Waste Rating Notifications", trailingContent = {
                                NotificationSwitch(wasteRatingNotifications) { wasteRatingNotifications = it }
                            })
                        },
                        {
                            SettingListItem("Municipality Announcements", showDivider = false, trailingContent = {
                                NotificationSwitch(municipalityAnnouncements) { municipalityAnnouncements = it }
                            })
                        }
                    )
                )

                // Privacy Section
                SettingsSectionHeader(R.drawable.baseline_lock_24, "Privacy")
                SettingsCard(
                    items = listOf(
                        {
                            Box(
                                modifier = Modifier.clickable {
                                    val intent = Intent(context, PrivacyPolicyActivity::class.java)
                                    context.startActivity(intent)
                                }
                            ) {
                                SettingListItem("Privacy Policy")
                            }
                        },

                        { SettingListItem("Terms & Conditions", showDivider = false) }
                    )
                )

                // Help Section
                SettingsSectionHeader(R.drawable.baseline_help_24, "Help")
                SettingsCard(
                    items = listOf(
                        {
                            Box(
                                modifier = Modifier.clickable {
                                    val intent = Intent(context, ContactSupportActivity::class.java)
                                    intent.putExtra("USER_ID", currentUser?.uid)
                                    context.startActivity(intent)
                                }
                            ) {
                                SettingListItem("Contact Support")
                            }
                        },
                        { SettingListItem("FAQs", showDivider = false) }
                    )
                )


                // About Section
                SettingsSectionHeader(R.drawable.baseline_info_24, "About")
                SettingsCard(
                    items = listOf(
                        { SettingListItem("App Version") },
                        { SettingListItem("About CleanTrack", showDivider = false) }
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Preview
@Composable
fun SettingsPreview() {
    SettingsBody()
}
package com.example.cleantrack.view.user

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cleantrack.model.BinModel
import com.example.cleantrack.repository.BinRepoImpl
import com.example.cleantrack.viewmodel.BinViewModel
import com.example.cleantrack.util.QrUtil
import androidx.compose.ui.graphics.asImageBitmap

class UserBinListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UserBinListScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserBinListScreen() {
    val context = LocalContext.current
    val userId = "LOGGED_IN_USER_ID" // Replace with actual Auth logic

    val vm = remember { BinViewModel(BinRepoImpl()) }
    val bins by vm.bins.observeAsState(emptyList())
    val loading by vm.loading.observeAsState(false)

    // --- State for Floating QR Card ---
    var selectedBinForQr by remember { mutableStateOf<com.example.cleantrack.model.BinModel?>(null) }
    var showQrSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.loadUserBins(userId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Bins") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                context.startActivity(Intent(context, BinSetupActivity::class.java))
            }) { Icon(Icons.Default.Add, null) }
        }
    ) { pad ->
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (bins.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No bins added yet")
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(pad)
            ) {
                items(bins) { bin ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable {
                                // 1. Set the selected bin
                                selectedBinForQr = bin
                                // 2. Show the floating QR sheet
                                showQrSheet = true
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(bin.label, style = MaterialTheme.typography.titleLarge)
                            Text("Category: ${bin.category}", color = androidx.compose.ui.graphics.Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            Text("Click to show QR for Collection",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // --- THE FLOATING QR CARD (ModalBottomSheet) ---
            if (showQrSheet && selectedBinForQr != null) {
                ModalBottomSheet(
                    onDismissRequest = { showQrSheet = false },
                    containerColor = androidx.compose.ui.graphics.Color.White
                ) {
                    BinQrContent(selectedBinForQr!!)
                }
            }
        }
    }
}

@Composable
fun BinQrContent(bin: com.example.cleantrack.model.BinModel) {
    val context = LocalContext.current

    // Generate QR Bitmap using the Bin ID
    val qrBitmap: android.graphics.Bitmap = remember(bin.binId) {
        com.example.cleantrack.util.QrUtil.generateQrBitmap(bin.binId)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bin Collection QR",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Text(
            text = bin.label,
            style = MaterialTheme.typography.bodyLarge,
            color = androidx.compose.ui.graphics.Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Display the Generated QR
        androidx.compose.foundation.Image(
            bitmap = qrBitmap.asImageBitmap(),
            contentDescription = "Bin QR Code",
            modifier = Modifier.size(250.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Show this code to the driver during collection",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

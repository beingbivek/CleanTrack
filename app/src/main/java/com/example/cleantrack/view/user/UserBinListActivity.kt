package com.example.cleantrack.view.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.cleantrack.model.BinModel
import com.example.cleantrack.repository.BinRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.viewmodel.BinViewModel
import com.example.cleantrack.viewmodel.UserViewModel

class UserBinListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { UserBinListScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserBinListScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val vm = remember { BinViewModel(BinRepoImpl()) }

    var currentUserId by remember { mutableStateOf("") }
    val bins by vm.bins.observeAsState(emptyList())
    val loading by vm.loading.observeAsState(false)

    var selectedBinForQr by remember { mutableStateOf<BinModel?>(null) }
    var showQrSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        currentUserId = userViewModel.getCurrentUserId() ?: ""
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (currentUserId.isNotEmpty()) {
                    vm.loadUserBins(currentUserId)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) vm.loadUserBins(currentUserId)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(
            Brush.verticalGradient(listOf(Green, Green.copy(alpha = 0.6f), Color.Transparent))
        ))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("My Disposal Bins", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { (context as? Activity)?.finish() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { context.startActivity(Intent(context, BinSetupActivity::class.java)) },
                    containerColor = Green,
                    contentColor = Color.White,
                    shape = CircleShape
                ) { Icon(Icons.Default.Add, "Add Bin") }
            }
        ) { pad ->
            Column(modifier = Modifier.padding(pad).fillMaxSize()) {
                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Green)
                    }
                } else if (bins.isEmpty()) {
                    EmptyBinState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bins) { bin ->
                            BinCard(bin) {
                                selectedBinForQr = bin
                                showQrSheet = true
                            }
                        }
                    }
                }
            }

            if (showQrSheet && selectedBinForQr != null) {
                ModalBottomSheet(
                    onDismissRequest = { showQrSheet = false },
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    BinQrContent(selectedBinForQr!!)
                }
            }
        }
    }
}

// BinCard, EmptyBinState, and BinQrContent remain the same as your themed version...
@Composable
fun BinCard(bin: BinModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(Green.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null, tint = Green)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = bin.label, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = bin.category, fontSize = 14.sp, color = Color.Gray)
            }
            Text(
                text = "View QR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Green,
                modifier = Modifier.background(Green.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun EmptyBinState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
        Spacer(Modifier.height(16.dp))
        Text("No bins registered", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
        Text("Tap the + button to add your first bin for collection.", textAlign = TextAlign.Center, color = Color.Gray)
    }
}

@Composable
fun BinQrContent(bin: BinModel) {
    val qrBitmap = remember(bin.binId) { com.example.cleantrack.util.QrUtil.generateQrBitmap(bin.binId) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(40.dp, 4.dp).clip(CircleShape).background(Color.LightGray))
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = bin.label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "Category: ${bin.category}", color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            modifier = Modifier.padding(10.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, Green.copy(alpha = 0.2f))
        ) {
            androidx.compose.foundation.Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "Bin QR Code",
                modifier = Modifier.size(220.dp).padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Present this QR code to the collection driver.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
    }
}
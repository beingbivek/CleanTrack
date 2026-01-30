package com.example.cleantrack.view.user

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.BinModel
import com.example.cleantrack.repository.BinRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.viewmodel.BinViewModel
import com.example.cleantrack.viewmodel.UserViewModel

class BinSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binId = intent.getStringExtra("BIN_ID")
        setContent { BinSetupScreen(binId) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinSetupScreen(binId: String?) {
    val context = LocalContext.current
    val activity = context as Activity

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val vm = remember { BinViewModel(BinRepoImpl()) }

    val selected by vm.bin.observeAsState(null)
    val loading by vm.loading.observeAsState(false)

    var currentUserId by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ORGANIC") }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf("ORGANIC", "INORGANIC", "TOXIC", "MIXED")

    LaunchedEffect(binId) {
        binId?.let { vm.getBinById(it) }
        currentUserId = userViewModel.getCurrentUserId() ?: ""
    }

    LaunchedEffect(selected) {
        selected?.let {
            label = it.label
            category = it.category
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        // Upper Gradient Background
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(
            Brush.verticalGradient(listOf(Green, Green.copy(alpha = 0.7f), Color.Transparent))
        ))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            if (binId == null) "Add New Bin" else "Edit Bin Details",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { activity.finish() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { pad ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Icon Header
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = Green,
                        modifier = Modifier.padding(16.dp).size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Form Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Bin Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.DarkGray
                        )

                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text("Label (e.g. Kitchen Bin)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Green,
                                focusedLabelColor = Green
                            )
                        )

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Waste Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Green,
                                    focusedLabelColor = Green
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                categories.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = {
                                            category = item
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green),
                            onClick = {
                                if (label.isBlank()) {
                                    Toast.makeText(context, "Enter label", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val model = BinModel(
                                    binId = binId ?: "",
                                    ownerUserId = currentUserId,
                                    label = label,
                                    category = category
                                )

                                if (binId == null) {
                                    vm.addBin(model) { ok, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (ok) activity.finish()
                                    }
                                } else {
                                    vm.updateBin(model) { ok, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (ok) activity.finish()
                                    }
                                }
                            }
                        ) {
                            if (loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    if (binId == null) "Register Bin" else "Update Details",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
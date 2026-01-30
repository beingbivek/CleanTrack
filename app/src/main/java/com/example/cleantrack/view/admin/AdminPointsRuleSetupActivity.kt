package com.example.cleantrack.view.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.PointsRuleModel
import com.example.cleantrack.repository.PointsRuleRepoImpl
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.viewmodel.PointsRuleViewModel

class AdminPointsRuleSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val ruleId = intent.getStringExtra("RULE_ID")
        setContent {
            AdminPointsRuleSetupScreen(ruleId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPointsRuleSetupScreen(ruleId: String?) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val vm = remember { PointsRuleViewModel(PointsRuleRepoImpl()) }

    // DATA STATE
    val rules by vm.rules.observeAsState(emptyList())
    val isInitialLoading by vm.loading.observeAsState(false)

    // UI STATE
    var binType by remember { mutableStateOf("ORGANIC") }
    var segregatedCorrectly by remember { mutableStateOf(true) }
    var points by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val binTypes = listOf("ORGANIC", "INORGANIC", "TOXIC")

    // PREFILL FOR EDIT
    LaunchedEffect(ruleId) {
        if (ruleId != null) {
            vm.loadRules()
        }
    }

    LaunchedEffect(rules) {
        if (ruleId != null) {
            rules?.find { it.ruleId == ruleId }?.let {
                binType = it.binType
                segregatedCorrectly = it.segregatedCorrectly
                points = it.points.toString()
                isActive = it.isActive
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Green, Color.White),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            if (ruleId == null) "Add Points Rule" else "Edit Points Rule",
                            style = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { activity.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { pad ->
            if (isInitialLoading) {
                Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(pad)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.height(8.dp))

                    // 🔽 BIN TYPE
                    DropdownField(
                        label = "Bin Type",
                        value = binType,
                        options = binTypes,
                        onSelect = { binType = it }
                    )

                    // 🔽 SEGREGATION STATUS
                    DropdownField(
                        label = "Segregated Correctly",
                        value = if (segregatedCorrectly) "Yes" else "No",
                        options = listOf("Yes", "No"),
                        onSelect = { segregatedCorrectly = it == "Yes" }
                    )

                    // 🔢 POINTS
                    OutlinedTextField(
                        value = points,
                        onValueChange = { points = it },
                        label = { Text("Points") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green,
                            focusedLabelColor = Green
                        )
                    )

                    // 🔘 ACTIVE SWITCH
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rule Status (Active)", fontWeight = FontWeight.Medium)
                            Switch(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Green)
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // 💾 SAVE BUTTON
                    Button(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        enabled = !isSaving,
                        onClick = {
                            val pts = points.toIntOrNull()
                            if (pts == null) {
                                Toast.makeText(context, "Enter valid points", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSaving = true
                            val model = PointsRuleModel(
                                ruleId = ruleId ?: "",
                                binType = binType,
                                segregatedCorrectly = segregatedCorrectly,
                                points = pts,
                                isActive = isActive
                            )

                            val callback: (Boolean, String) -> Unit = { success, msg ->
                                isSaving = false
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) activity.finish()
                            }

                            if (ruleId == null) vm.addRule(model, callback)
                            else vm.updateRule(model, callback)
                        }
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (ruleId == null) "Save Rule" else "Update Rule", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green, focusedLabelColor = Green)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = { onSelect(it); expanded = false })
            }
        }
    }
}
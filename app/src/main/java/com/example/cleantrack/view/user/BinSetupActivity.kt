package com.example.cleantrack.view.user

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cleantrack.model.BinModel
import com.example.cleantrack.repository.BinRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.viewmodel.BinViewModel
import com.example.cleantrack.viewmodel.UserViewModel

class BinSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    var currentUserId by remember { mutableStateOf("") }



    val vm = remember { BinViewModel(BinRepoImpl()) }
    val selected by vm.bin.observeAsState(null)

    var label by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ORGANIC") }

    var expanded by remember { mutableStateOf(false) }

    val categories = listOf("ORGANIC", "INORGANIC", "TOXIC", "MIXED")

    LaunchedEffect(binId) {
        binId?.let { vm.getBinById(it) }

        // 🔹 Get the logged-in driver's ID
        currentUserId = userViewModel.getCurrentUserId() ?: ""
    }

    LaunchedEffect(selected) {
        selected?.let {
            label = it.label
            category = it.category
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (binId == null) "Add Bin" else "Edit Bin") })
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Bin Label") },
                modifier = Modifier.fillMaxWidth()
            )

            // 2. Wrap in the correct MenuBox logic
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded } // 3. Toggle state
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor() // 4. VERY IMPORTANT: Anchors menu to field
                )

                ExposedDropdownMenu( // 5. Use ExposedDropdownMenu instead of DropdownMenu
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                category = item
                                expanded = false // 6. Close menu after selection
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
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
                Text(if (binId == null) "Save Bin" else "Update Bin")
            }
        }
    }
}

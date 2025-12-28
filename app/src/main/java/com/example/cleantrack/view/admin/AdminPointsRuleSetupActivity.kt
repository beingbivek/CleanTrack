package com.example.cleantrack.view.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cleantrack.model.PointsRuleModel
import com.example.cleantrack.repository.PointsRuleRepoImpl
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

    val vm = remember {
        PointsRuleViewModel(PointsRuleRepoImpl())
    }

    val rules by vm.rules.observeAsState(emptyList())

    // UI STATE
    var binType by remember { mutableStateOf("ORGANIC") }
    var segregatedCorrectly by remember { mutableStateOf(true) }
    var points by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    val binTypes = listOf("ORGANIC", "INORGANIC", "TOXIC")

    // PREFILL FOR EDIT
    LaunchedEffect(ruleId) {
        if (ruleId != null) {
            vm.loadRules()
        }
    }

    LaunchedEffect(rules) {
        if (ruleId != null) {
            rules!!.firstOrNull { it.ruleId == ruleId }?.let {
                binType = it.binType
                segregatedCorrectly = it.segregatedCorrectly
                points = it.points.toString()
                isActive = it.isActive
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (ruleId == null) "Add Points Rule" else "Edit Points Rule")
                }
            )
        }
    ) { pad ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

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
                onSelect = {
                    segregatedCorrectly = it == "Yes"
                }
            )

            // 🔢 POINTS
            OutlinedTextField(
                value = points,
                onValueChange = { points = it },
                label = { Text("Points") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // 🔘 ACTIVE SWITCH
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Active")
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }

            Spacer(Modifier.height(20.dp))

            // 💾 SAVE BUTTON
            Button(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                onClick = {

                    val pts = points.toIntOrNull()
                    if (pts == null) {
                        Toast.makeText(context, "Enter valid points", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val model = PointsRuleModel(
                        ruleId = ruleId ?: "",
                        binType = binType,
                        segregatedCorrectly = segregatedCorrectly,
                        points = pts,
                        isActive = isActive
                    )

                    if (ruleId == null) {
                        vm.addRule(model) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) activity.finish()
                        }
                    } else {
                        vm.updateRule(model) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) activity.finish()
                        }
                    }
                }
            ) {
                Text(if (ruleId == null) "Save Rule" else "Update Rule")
            }
        }
    }
}

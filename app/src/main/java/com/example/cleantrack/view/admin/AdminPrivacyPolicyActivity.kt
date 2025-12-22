package com.example.cleantrack.view.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cleantrack.repository.PrivacyPolicyRepoImpl
import com.example.cleantrack.viewmodel.PrivacyPolicyViewModel

class AdminPrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AdminPrivacyPolicyScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPrivacyPolicyScreen() {
    val context = LocalContext.current
    val vm = remember { PrivacyPolicyViewModel(PrivacyPolicyRepoImpl()) }

    val policy by vm.policy.observeAsState()
    val loading by vm.loading.observeAsState(false)

    var text by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadPrivacyPolicy() }

    // When policy loads, fill text once
    LaunchedEffect(policy) {
        if (text.isBlank()) {
            text = policy?.description ?: ""
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Admin: Privacy Policy") }) }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Privacy Policy") },
                modifier = Modifier.fillMaxWidth().weight(1f),
                singleLine = false
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (text.isBlank()) {
                        Toast.makeText(context, "Policy cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    vm.savePrivacyPolicy(text) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

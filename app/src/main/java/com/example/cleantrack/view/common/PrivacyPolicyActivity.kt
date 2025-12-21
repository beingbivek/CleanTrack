package com.example.cleantrack.view.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cleantrack.repository.PrivacyPolicyRepoImpl
import com.example.cleantrack.viewmodel.PrivacyPolicyViewModel

class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PrivacyPolicyScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen() {
    val vm = remember { PrivacyPolicyViewModel(PrivacyPolicyRepoImpl()) }

    val policy by vm.policy.observeAsState()
    val loading by vm.loading.observeAsState(false)

    LaunchedEffect(Unit) { vm.loadPrivacyPolicy() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Privacy Policy") }) }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            Text(
                text = policy?.description?.ifBlank { "Privacy policy not available yet." }
                    ?: "Privacy policy not available yet.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

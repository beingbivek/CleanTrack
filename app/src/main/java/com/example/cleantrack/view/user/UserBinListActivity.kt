package com.example.cleantrack.view.user

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.cleantrack.repository.BinRepoImpl
import com.example.cleantrack.viewmodel.BinViewModel

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
    val userId = "LOGGED_IN_USER_ID" // replace with auth user

    val vm = remember { BinViewModel(BinRepoImpl()) }
    val bins by vm.bins.observeAsState(emptyList())
    val loading by vm.loading.observeAsState(false)

    LaunchedEffect(Unit) {
        vm.loadUserBins(userId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Bins") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                context.startActivity(
                    Intent(context, BinSetupActivity::class.java)
                )
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
                Modifier
                    .fillMaxSize()
                    .padding(pad)
            ) {
                items(bins) { bin ->
                    Card(Modifier.padding(12.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(bin.label, style = MaterialTheme.typography.titleMedium)
                            Text("Category: ${bin.category}")
                        }
                    }
                }
            }
        }
    }
}

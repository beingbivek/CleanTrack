package com.example.cleantrack.view.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Essential import
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cleantrack.repository.PointsRuleRepoImpl
import com.example.cleantrack.viewmodel.PointsRuleViewModel

class AdminPointsRuleListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminPointsRuleListScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPointsRuleListScreen() {

    val vm = remember {
        PointsRuleViewModel(PointsRuleRepoImpl())
    }

    // Even with a default value, if the LiveData holds a nullable type, 'rules' might be inferred as nullable.
    val rules by vm.rules.observeAsState(emptyList())
    val loading by vm.loading.observeAsState(false)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.loadRules()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Points Rules") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                context.startActivity(
                    Intent(context, AdminPointsRuleSetupActivity::class.java)
                )
            }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { pad ->

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
            ) {
                // FIX: Added '?: emptyList()' to ensure the list is never null
                items(rules ?: emptyList()) { rule ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {

                            Text(
                                "${rule.binType} | ${if (rule.segregatedCorrectly) "Correct" else "Wrong"}",
                                fontWeight = FontWeight.Bold
                            )

                            Text("Points: ${rule.points}")
                            Text("Active: ${rule.isActive}")

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = {
                                    val i = Intent(
                                        context,
                                        AdminPointsRuleSetupActivity::class.java
                                    )
                                    i.putExtra("RULE_ID", rule.ruleId)
                                    context.startActivity(i)
                                }) {
                                    Icon(Icons.Default.Edit, null)
                                }

                                IconButton(onClick = {
                                    vm.deleteRule(rule.ruleId) { _, _ -> }
                                }) {
                                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
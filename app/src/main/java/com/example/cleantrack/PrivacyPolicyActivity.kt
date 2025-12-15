package com.example.cleantrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.viewModel.PrivacyPolicyViewModel

class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrivacyPolicyBody()
               }
            }
        }


@Composable
fun PrivacyPolicyBody(viewModel: PrivacyPolicyViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {

    val policy = viewModel.privacyPolicy.collectAsState().value

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            item {
                Spacer(modifier = Modifier.height(50.dp))

                Text(
                    "Privacy Policy",
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        color = Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Show loading state
                if (policy == null) {
                    Text(
                        text = "Loading...",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp
                    )
                } else {
                    Text(
                        text = "Date: ${policy.date}",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = policy.description,
                        modifier = Modifier
                            .padding(16.dp),
                        fontSize = 16.sp,
                        color = Black
                    )
                }
            }


        }
    }
}




@Preview
@Composable
fun PrivacyPolicyPreview(){
    PrivacyPolicyBody()
}
package com.example.cleantrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.CleanTrackTheme
import com.example.cleantrack.ui.theme.White
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class UserDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserDashboardBody()
        }
    }
}


@Composable
fun UserDashboardBody() {

//    val privacyText = getLatestPrivacyPolicyDesc()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                "User Dashboard",
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(50.dp))

//            Text(text = privacyText)
        }
    }
}


//@Composable
//fun getLatestPrivacyPolicyDesc(): String {
//    val firestore = Firebase.firestore
//    val description = remember { mutableStateOf("Loading...") }
//
//    LaunchedEffect(Unit) {
//        firestore.collection("privacy_policy")
//            .orderBy("date", Query.Direction.DESCENDING)
//            .limit(1)  // Get only the latest document
//            .get()
//            .addOnSuccessListener { result ->
//                if (!result.isEmpty) {
//                    val doc = result.documents[0]
//                    description.value = doc.getString("description") ?: "No description found"
//                } else {
//                    description.value = "No documents found"
//                }
//            }
//            .addOnFailureListener {
//                description.value = "Failed to load data"
//            }
//    }

//    return description.value
//}

@Preview
@Composable
fun UserDashboardPreview(){
    UserDashboardBody()
}
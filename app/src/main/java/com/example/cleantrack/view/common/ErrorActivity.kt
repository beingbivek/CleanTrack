package com.example.cleantrack.view.common

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.cleantrack.R
import com.example.cleantrack.ui.theme.CleanTrackTheme

class ErrorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ErrorBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorBody(){
    var context = LocalContext.current
    var activity = context as Activity
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { "Error" },
                navigationIcon = {IconButton(
                    onClick = {
                        activity.finish()
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_arrow_back_24),null
                    )
                }}
            )
        }
    ) {
        pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_error_outline_24),null
            )
            Text("Error 404")
        }
    }
}

@Preview
@Composable
fun ErrorPreview(){
    ErrorBody()
}
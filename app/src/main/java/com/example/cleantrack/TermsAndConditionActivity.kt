package com.example.cleantrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.White

class TermsAndConditionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TermsAndConditionBody()
        }
    }
}

@Composable
fun TermsAndConditionBody() {
    var checked by remember { mutableStateOf(true) }


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
                "Terms and Condition",
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFe4e6e5)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFe4e6e5))
                        .verticalScroll(rememberScrollState()),
                ) {

                    Card(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                            .height(1000.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = White
                        )
                    ) {

                        Column (
                            modifier = Modifier.fillMaxWidth()
                                .padding(start = 20.dp, top = 15.dp),
                        ){
                            Text(
                                "1. Acceptance of Terms",
                                style = TextStyle(
                                    color = Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                "Bla bla bla ble ble ble blu blu blu afeafaefaefaef afafaefeafefgfg",
                                style = TextStyle(
                                    color = Black,
                                    fontSize = 15.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                                    .padding(top = 3.dp)
                            )


                        }


                    }
                }




            }
        }
    }
}

@Preview
@Composable
fun TermsAndConditionPreview(){
    TermsAndConditionBody()
}
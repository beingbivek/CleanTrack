package com.example.cleantrack.view.common

import ContactSupportRepoImpl
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.cleantrack.R
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.ButtonColor
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.TextBoxColor
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.viewmodel.ContactSupportViewModel
import com.example.cleantrack.viewmodel.UserViewModel
import coil.compose.AsyncImage

class ContactSupportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userId = intent.getStringExtra("USER_ID")

        setContent {
            ContactSupportScreen(userId)
        }
    }
}

@Composable
fun ContactSupportScreen(userId: String?) {
    val supportViewModel = remember { ContactSupportViewModel(ContactSupportRepoImpl()) }
    val userData by supportViewModel.currentUserData.observeAsState()

    LaunchedEffect(Unit) {
        supportViewModel.fetchInitialData()
    }



    val fullname = userData?.fullname ?: ""
    val email = userData?.email ?: ""
    val userType = userData?.userType ?: "GUEST"

    ContactSupportBody(
        initialName = fullname,
        initialEmail = email,
        isReadOnly = userId != null,
        userId = userId ?: "",
        userType = userType,
        viewModel = supportViewModel // Pass ViewModel to handle the submit action
    )
}



@Composable
fun ContactSupportBody(
    initialName: String,
    initialEmail: String,
    isReadOnly: Boolean,
    userId: String,
    userType: String,
    viewModel: ContactSupportViewModel
) {
    val context = LocalContext.current

    // --- NEW IMAGE PICKER LOGIC ---
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    var fullname by remember(initialName) {
        mutableStateOf(initialName)
    }

    var email by remember(initialEmail) {
        mutableStateOf(initialEmail)
    }

    var message by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf("Select Issues") }
    val issueCategories = listOf(
        "App & Technical Issues",
        "Login & Technical Issues",
        "Service-Related Issues",
        "Payments & Billing",
        "Account & Profile",
        "Location & Map",
        "Feedback & Others"
    )
    var textFieldSize by remember { mutableStateOf(Size.Zero) }



    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(White)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                "Contact Support",
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(15.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, end = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Image(
                    painter = painterResource(R.drawable.contact_support_logo),
                    contentDescription = null,
                    modifier = Modifier.size(150.dp)
                )
                Spacer(modifier = Modifier.size(15.dp))
                Text(
                    text = "Thank you for reaching out. If you need any help or have any questions, our support team is here for you.",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            Spacer(modifier = Modifier.size(24.dp))


            OutlinedTextField(
                value = fullname,
                onValueChange = { if (!isReadOnly) fullname = it },
                readOnly = isReadOnly,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                shape = RoundedCornerShape(15.dp),
                placeholder = { Text("Enter your full name") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextBoxColor,
                    unfocusedContainerColor = TextBoxColor,
                    focusedIndicatorColor = if (isReadOnly) Color.Transparent else Green,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )


            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { if (!isReadOnly) email = it },
                readOnly = isReadOnly,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                shape = RoundedCornerShape(15.dp),
                placeholder = { Text("Enter your email") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextBoxColor,
                    unfocusedContainerColor = TextBoxColor,
                    focusedIndicatorColor = if (isReadOnly) Color.Transparent else Green,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )



            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)) {
                    OutlinedTextField(
                        value = selectedOptionText,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                // capture the size of the TextField
                                textFieldSize = coordinates.size.toSize()
                            }
                            .clickable { expanded = true },
                        placeholder = { Text("Select Issue") },
                        enabled = false, // prevent manual typing
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                    ) {
                        issueCategories.forEach { issueCategories ->
                            DropdownMenuItem(
                                text = { Text(issueCategories) },
                                onClick = {
                                    selectedOptionText = issueCategories
                                    expanded = false
                                }
                            )
                        }
                    }
                }


            }

            OutlinedTextField(
                value = message,
                onValueChange = { data ->
                    message = data
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                shape = RoundedCornerShape(15.dp),
                placeholder = {
                    Text("Enter your message")
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextBoxColor,
                    unfocusedContainerColor = TextBoxColor,
                    focusedIndicatorColor = Green,
                    unfocusedIndicatorColor = Color.Transparent
                )

            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    onClick = {launcher.launch("image/*")},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .height(60.dp)
                        .background(
                            color = Green,
                            shape = RoundedCornerShape(15.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 15.dp
                    ),
                ) {
                    Text("Add Attachment", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }

            }

            // --- DISPLAY SELECTED IMAGE ---
            if (selectedImageUri != null) {
                Spacer(modifier = Modifier.height(15.dp))
                Box(
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(TextBoxColor, RoundedCornerShape(15.dp))
                ) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Attachment",
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentScale = ContentScale.Fit
                    )

                    // Simple "X" button to remove image
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clickable { selectedImageUri = null }
                            .background(Color.Black.copy(0.4f), RoundedCornerShape(50))
                            .padding(4.dp),
                        tint = Color.White
                    )
                }
            }
            // ------------------------------

            Spacer(modifier = Modifier.height(20.dp))



            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    onClick = {
                        if (selectedOptionText == "Select Issues" || message.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.submitTicket(
                                fullname = fullname,
                                email = email,
                                category = selectedOptionText,
                                message = message,
                                userId = userId,
                                userType = userType
                            ) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    message = ""
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .height(60.dp)
                        .background(
                            brush = Brush.horizontalGradient(colors = ButtonColor),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 15.dp
                    ),
                ) {
                    Text("Submit", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }

            }

        }

    }
}


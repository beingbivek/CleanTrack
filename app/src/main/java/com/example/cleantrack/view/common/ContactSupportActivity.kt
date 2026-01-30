package com.example.cleantrack.view.common

import ContactSupportRepoImpl
import android.app.Activity
import android.content.Intent
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import coil.compose.AsyncImage
import com.example.cleantrack.R
import com.example.cleantrack.model.NotificationPayload
import com.example.cleantrack.repository.CommonImageRepoImpl
import com.example.cleantrack.repository.NotificationRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.ButtonColor
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.TextBoxColor
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.viewmodel.CommonImageViewModel
import com.example.cleantrack.viewmodel.ContactSupportViewModel
import com.example.cleantrack.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

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
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl(), UserRepoImpl()) }
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
        viewModel = supportViewModel,
        notificationViewModel = notificationViewModel
    )
}

@Composable
fun ContactSupportBody(
    initialName: String,
    initialEmail: String,
    isReadOnly: Boolean,
    userId: String,
    userType: String,
    viewModel: ContactSupportViewModel,
    notificationViewModel: NotificationViewModel
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val commonImageViewModel = remember { CommonImageViewModel(CommonImageRepoImpl()) }

    // States
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var fullname by remember(initialName) { mutableStateOf(initialName) }
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var message by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf("Select Issues") }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    // --- NEW LOADING STATE ---
    var isSubmitting by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    val issueCategories = listOf(
        "App & Technical Issues", "Login & Technical Issues", "Service-Related Issues",
        "Payments & Billing", "Account & Profile", "Location & Map", "Feedback & Others"
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            // ... (Top Bar and Header items kept the same)
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp, bottom = 10.dp)) {
                    IconButton(
                        onClick = { activity?.finish() },
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 15.dp).size(45.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Green)
                    }

                    Text(
                        "Contact Support",
                        style = TextStyle(textAlign = TextAlign.Center, color = Black, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    IconButton(
                        onClick = { context.startActivity(Intent(context, IssuesViewActivity::class.java)) },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 15.dp)
                            .background(TextBoxColor, RoundedCornerShape(12.dp))
                            .size(45.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = "My Tickets", tint = Green)
                    }
                }
                Spacer(modifier = Modifier.size(15.dp))
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.contact_support_logo),
                        contentDescription = null, modifier = Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.size(15.dp))
                    Text(
                        text = "Thank you for reaching out. If you need any help or have any questions, our support team is here for you.",
                        style = TextStyle(fontSize = 16.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                    )
                }
                Spacer(modifier = Modifier.size(24.dp))
            }

            // ... (Input fields items kept the same)
            item {
                OutlinedTextField(
                    value = fullname,
                    onValueChange = { if (!isReadOnly) fullname = it },
                    readOnly = isReadOnly,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                    shape = RoundedCornerShape(15.dp),
                    placeholder = { Text("Enter your full name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TextBoxColor, unfocusedContainerColor = TextBoxColor,
                        focusedBorderColor = if (isReadOnly) Color.Transparent else Green,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { if (!isReadOnly) email = it },
                    readOnly = isReadOnly,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                    shape = RoundedCornerShape(15.dp),
                    placeholder = { Text("Enter your email") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TextBoxColor, unfocusedContainerColor = TextBoxColor,
                        focusedBorderColor = if (isReadOnly) Color.Transparent else Green,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().padding(15.dp)) {
                    OutlinedTextField(
                        value = selectedOptionText,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth()
                            .onGloballyPositioned { textFieldSize = it.size.toSize() }
                            .clickable { expanded = true },
                        placeholder = { Text("Select Issue") },
                        enabled = false,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Green, disabledTextColor = Black
                        )
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                    ) {
                        issueCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedOptionText = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp).height(120.dp),
                    shape = RoundedCornerShape(15.dp),
                    placeholder = { Text("Enter your message") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TextBoxColor, unfocusedContainerColor = TextBoxColor,
                        focusedBorderColor = Green, unfocusedBorderColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp).height(55.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Text("Add Attachment", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = White)
                }
            }

            item {
                if (selectedImageUri != null) {
                    Spacer(modifier = Modifier.height(15.dp))
                    Box(
                        modifier = Modifier.padding(horizontal = 15.dp).fillMaxWidth().height(200.dp)
                            .background(TextBoxColor, RoundedCornerShape(15.dp))
                    ) {
                        AsyncImage(
                            model = selectedImageUri, contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(8.dp), contentScale = ContentScale.Fit
                        )
                        Icon(
                            Icons.Default.Close, contentDescription = "Remove",
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                .clickable { selectedImageUri = null }
                                .background(Color.Black.copy(0.4f), RoundedCornerShape(50)).padding(4.dp),
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- UPDATED SUBMIT BUTTON ---
            item {
                Button(
                    onClick = {
                        if (selectedOptionText == "Select Issues" || message.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        } else {
                            isSubmitting = true // START LOADING
                            val timestamp = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
                            val formattedMsg = "[$fullname @ $timestamp]: $message"

                            val onComplete: (Boolean, String) -> Unit = { success, msg ->
                                isSubmitting = false // STOP LOADING
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    notificationViewModel.notifyAllAdmins(
                                        NotificationPayload("New support ticket", "$fullname submitted a request.", "support_ticket", "ticket_detail")
                                    )
                                    message = ""
                                    selectedImageUri = null
                                }
                            }

                            if (selectedImageUri != null) {
                                commonImageViewModel.uploadImage(context, selectedImageUri!!) { url ->
                                    if (url != null) {
                                        viewModel.submitTicket(fullname, email, selectedOptionText, formattedMsg, userId, userType, url, onComplete)
                                    } else {
                                        isSubmitting = false
                                        Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                viewModel.submitTicket(fullname, email, selectedOptionText, formattedMsg, userId, userType, "", onComplete)
                            }
                        }
                    },
                    enabled = !isSubmitting, // DISABLE BUTTON WHILE LOADING
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp).height(60.dp)
                        .background(brush = Brush.horizontalGradient(colors = ButtonColor), shape = RoundedCornerShape(15.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Submit", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = White)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
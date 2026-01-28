package com.example.cleantrack.view.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.R
import com.example.cleantrack.repository.ScheduleRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.Blue
import com.example.cleantrack.ui.theme.ButtonColor
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.TextBoxColor
import com.example.cleantrack.ui.theme.Red
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.util.AppUtil
import com.example.cleantrack.view.common.ContactSupportActivity
import com.example.cleantrack.viewmodel.ScheduleViewModel
import com.example.cleantrack.viewmodel.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginBody()
        }
    }
}

@Composable
fun LoginBody() {
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    val scheduleViewModel   = remember { ScheduleViewModel(ScheduleRepoImpl()) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordvisibility by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as Activity
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    val webClientId = stringResource(id = R.string.default_web_client_id)

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    userViewModel.signInWithGoogle(idToken, context, activity, scheduleViewModel) { success, errorMessage ->
                        if (success) {
                            if (errorMessage != null && errorMessage != "Login successful!") {
                                AppUtil.showToast(context, errorMessage)
                            }
                        } else {
                            AppUtil.showToast(context, errorMessage ?: "Google Sign-In process failed.")
                        }
                    }
                } else {
                    AppUtil.showToast(context, "Google Sign-In token missing.")
                }
            } catch (e: ApiException) {
                AppUtil.showToast(context, "Google Sign-In failed: ${e.statusCode}")
            }
        } else {
            AppUtil.showToast(context, "Google Sign-In cancelled.")
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            item {
                // Large top spacer for status bar + visual breathing room
                Spacer(modifier = Modifier.height(100.dp))

                Text(
                    "Log Into CleanTrack",
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        color = Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.size(40.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.user_logo),
                        contentDescription = null,
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.size(15.dp))
                    Text(
                        text = "Your journey to smarter,\ncooler recycling starts now",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
                Spacer(modifier = Modifier.size(40.dp))
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp),
                    shape = RoundedCornerShape(15.dp),
                    placeholder = { Text("Enter your email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TextBoxColor,
                        unfocusedContainerColor = TextBoxColor,
                        focusedIndicatorColor = Green,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    trailingIcon = {
                        IconButton(onClick = { passwordvisibility = !passwordvisibility }) {
                            Icon(
                                painter = if (passwordvisibility)
                                    painterResource(R.drawable.baseline_visibility_off_24)
                                else
                                    painterResource(R.drawable.baseline_visibility_24),
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordvisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp),
                    shape = RoundedCornerShape(15.dp),
                    placeholder = { Text("Enter your password") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TextBoxColor,
                        unfocusedContainerColor = TextBoxColor,
                        focusedIndicatorColor = Green,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Text(
                    "Forgot Password?",
                    style = TextStyle(color = Red, textAlign = TextAlign.End),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .clickable {
                            forgotPasswordEmail = email.trim()
                            showForgotPasswordDialog = true
                        }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Button(
                    onClick = {
                        userViewModel.login(email.trim(), password.trim()) { success, errorMessage, _, userId ->
                            if (success && userId != null) {
                                // 1. Sync User Pro status
                                userViewModel.syncOfflineUserData(userId, context) { routeId ->
                                    // 2. If the callback runs, the user is Pro, so cache their schedules
                                    scheduleViewModel.cacheSchedulesForOffline(routeId, context)
                                }

                                userViewModel.checkAndNavigateAfterLogin(userId, context, activity)
                            } else {
                                AppUtil.showToast(context, errorMessage ?: "Login failed.")
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
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 15.dp),
                ) {
                    Text("Login", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            item {
                Text(buildAnnotatedString {
                    withStyle(SpanStyle(color = Blue)) { append("Haven't made an account yet? ") }
                    withStyle(SpanStyle(color = Green)) { append("Sign Up") }
                }, modifier = Modifier
                    .padding(horizontal = 15.dp, vertical = 15.dp)
                    .clickable {
                        context.startActivity(Intent(context, RegistrationActivity::class.java))
                    })
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text("OR", modifier = Modifier.padding(horizontal = 15.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
            }

            item {
                Button(
                    onClick = {
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(0.5.dp, Color.Gray),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Log in with Google",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                TextButton(
                    onClick = {
                        val intent = Intent(context, ContactSupportActivity::class.java)
                        intent.putExtra("IS_LOGGED_IN", false)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_help_24),
                            contentDescription = null,
                            tint = Color(0xFF4F96D8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Need help? Contact Support",
                            color = Color(0xFF4F96D8),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                initialEmail = forgotPasswordEmail,
                onDimiss = { showForgotPasswordDialog = false },
                onSendReset = { enteredEmail ->
                    forgotPasswordEmail = enteredEmail
                    showForgotPasswordDialog = false
                    userViewModel.forgotPassword(enteredEmail) { success, errorMessage ->
                        if (success) {
                            AppUtil.showToast(context, "Password reset email sent to $enteredEmail. Check your inbox")
                        } else {
                            AppUtil.showToast(context, errorMessage ?: "Failed to send password reset email.")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    initialEmail: String,
    onDimiss: () -> Unit,
    onSendReset: (String) -> Unit
) {
    var emailInput by remember { mutableStateOf(initialEmail) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDimiss,
        title = { Text("Reset Password") },
        text = {
            Column {
                Text("Enter the email address associated with your account to receive a password reset link.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (emailInput.isNotBlank()) {
                        onSendReset(emailInput)
                    } else {
                        AppUtil.showToast(context, "Email fields cannot be empty.")
                    }
                }
            ) {
                Text("Send Reset Link")
            }
        },
        dismissButton = {
            Button(onClick = onDimiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview
@Composable
fun LoginPreview() {
    LoginBody()
}
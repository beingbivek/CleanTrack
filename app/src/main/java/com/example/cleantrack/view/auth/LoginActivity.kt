package com.example.cleantrack.view.auth

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.core.app.ActivityCompat
import com.example.cleantrack.R
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.Blue
import com.example.cleantrack.ui.theme.ButtonColor
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.Red
import com.example.cleantrack.ui.theme.TextBoxColor
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.util.AppUtil
import com.example.cleantrack.view.admin.AdminDashboardActivity
import com.example.cleantrack.view.common.ErrorActivity
import com.example.cleantrack.view.user.UserDashboardActivity
import com.example.cleantrack.view.driver.DriverDashBoardActivity
import com.example.cleantrack.viewmodel.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.messaging.FirebaseMessaging


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        enableEdgeToEdge()
        setContent {
            LoginBody()
        }
    }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }
    }
}


@Composable
fun LoginBody() {

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }


    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordvisibility by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val activity = context as Activity

    var showForgotPasswordDialog by remember { mutableStateOf(false ) }
    var forgotPasswordEmail by remember { mutableStateOf("") }

    // 1. Configure Google Sign-In Options
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)

        // IMPORTANT: Request the ID token for Firebase authentication i.e. sign with google
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso ) }

    // 2. Activity Result Launcher for google sign-in intent
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        result ->
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {

                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null){

                    userViewModel.signInWithGoogle(idToken ){
                        Success, errorMessage, role ->
                        if (Success){
                            getFCMToken()
                            val destinationActivity = when (role) {
                                "ADMIN" -> AdminDashboardActivity::class.java
                                "DRIVER" -> DriverDashBoardActivity::class.java // Use DriverDashboardActivity
                                "USER" -> UserDashboardActivity::class.java
                                else -> UserDashboardActivity::class.java
                            }
                            val intent = Intent(context, destinationActivity)
                            context.startActivity(intent)
                            activity.finish()

                        }else{
                            AppUtil.showToast(context, errorMessage ?: "Google Sign-In failed.")

                        }
                    }

                }else{
                    AppUtil.showToast(context  ,"Google Sign-In token missing.")
                }

            } catch (e: ApiException){
                // Handle exceptions (e.g., user cancelled sign-in)
                AppUtil.showToast(context , "Google Sign-In failed: ${e.statusCode}")
            }

        }else{
            // Sign-in intent failed/cancelled
            AppUtil.showToast(context, "Google Sign-In cancelled.")
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(White)
        ) {
            Spacer(modifier = Modifier.height(80.dp))

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

            Spacer(modifier = Modifier.size(50.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, end = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
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

            Spacer(modifier = Modifier.size(50.dp))


            OutlinedTextField(
                value = email,
                onValueChange = { data ->
                    email = data
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                shape = RoundedCornerShape(15.dp),
                placeholder = {
                    Text("Enter your email")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextBoxColor,
                    unfocusedContainerColor = TextBoxColor,
                    focusedIndicatorColor = Green,
                    unfocusedIndicatorColor = Color.Transparent
                )

            )

            Spacer(modifier = Modifier.height(20.dp))


            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                },
                trailingIcon = {
                    IconButton(onClick = {
                        passwordvisibility = !passwordvisibility
                    }) {
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
                placeholder = {
                    Text("Enter your password")
                },

                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextBoxColor,
                    unfocusedContainerColor = TextBoxColor,
                    focusedIndicatorColor = Green,
                    unfocusedIndicatorColor = Color.Transparent
                )

            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Forgot Password?",
                style = TextStyle(
                    color = Red,
                    textAlign = TextAlign.End
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
                    .clickable{

                        forgotPasswordEmail = email
                        showForgotPasswordDialog = true
                    }
            )

            Spacer(modifier = Modifier.height(20.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    onClick = {
                       userViewModel.login(email, password){
                           Success, errorMessage, role->
                           if (Success){
                               getFCMToken()
                               val destinationActivity = when (role){
                                   "ADMIN"-> AdminDashboardActivity::class.java
                                   "DRIVER"-> DriverDashBoardActivity::class.java
                                   "USER"-> UserDashboardActivity::class.java
                                   else -> ErrorActivity::class.java

                               }

                               val intent = Intent(context, destinationActivity)

                               context.startActivity(intent)
                               activity.finish()



                           }else    {
                               AppUtil.showToast(context, errorMessage?:"Login failed. Please check your credentials.")
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
                    Text("Login", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }

            }
            Text(buildAnnotatedString {

                withStyle(SpanStyle(color = Blue)
                ){
                    append("Haven't made an account yet? ")
                }

                withStyle(SpanStyle(color = Green)) {
                    append("Sign Up")
                }
            }
                , modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
                    .clickable{
                        val intent = Intent(context, RegistrationActivity::class.java)

                        context.startActivity(intent)

                    })


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f)
                )
                Text("OR", modifier = Modifier.padding(horizontal = 15.dp))

                HorizontalDivider(
                    modifier = Modifier.weight(1f)
                )
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    onClick = {
                        val signIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(0.5.dp, Color.Gray),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

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
            }

        }

        if (showForgotPasswordDialog){
            ForgotPasswordDialog(
                initialEmail = forgotPasswordEmail,
                onDimiss = { showForgotPasswordDialog = false},
                onSendReset = { enteredEmail ->
                    forgotPasswordEmail = enteredEmail
                    showForgotPasswordDialog = false

                    userViewModel.forgotPassword(enteredEmail){
                        Success, errorMessage ->
                        if (Success) {
                            AppUtil.showToast(
                                context,
                                "Password reset email sent to $enteredEmail. Check your inbox"
                            )
                        } else  {
                            AppUtil.showToast(
                                context,
                                errorMessage ?: "Failed to send password reset email."
                            )
                        }
                    }
                }
            )

        }

    }
}

fun getFCMToken() {
    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log it (or send to your server)
            Log.d("FCM_TOKEN", "Token: $token")
        }
}

@Composable
fun ForgotPasswordDialog(
    initialEmail: String,
    onDimiss: () -> Unit,
    onSendReset: (String) -> Unit
){
    var emailInput by remember { mutableStateOf(initialEmail) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDimiss,
        title = {Text("Reset Password")},
        text = {
            Column() {
                Text("Enter the email address associated with your account to receive a password reset link.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it},
                    label = { Text("Email")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (emailInput.isNotBlank()){
                        onSendReset(emailInput)
                    }else{
                        AppUtil.showToast(context , "Email fiels cannot be empty.")
                    }
                }
            ){
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
fun LoginPreview(){
    LoginBody()
}
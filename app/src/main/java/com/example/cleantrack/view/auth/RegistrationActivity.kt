package com.example.cleantrack.view.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cleantrack.R
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.Blue
import com.example.cleantrack.ui.theme.ButtonColor
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.TextBoxColor
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.util.AppUtil
import com.example.cleantrack.viewModel.UserAddressViewModel
import com.example.cleantrack.viewModel.UserViewModel


class RegistrationActivity : ComponentActivity() {

    // Member variable to hold the model if passed by Google Sign-In
    private var googleUserModel: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. CHECK INTENT FOR GOOGLE USER MODEL
        @Suppress("DEPRECATION")
        googleUserModel = intent.getParcelableExtra("Google_UserModel")

        // Pass the model to the Composable
        setContent {
            RegisterBody(googleUserModel)
        }
    }
}


@Composable
// FIX: The Composable function must accept the parameter passed by the Activity
fun RegisterBody(googleUserModel: UserModel? = null ) {


    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    val addressVM: UserAddressViewModel = viewModel()

    val isGoogleSignInFlow = googleUserModel != null

    var fullname by remember { mutableStateOf(googleUserModel?.fullname?:"") }
    var number by remember { mutableStateOf(googleUserModel?.number?:"") }
    var email by remember { mutableStateOf(googleUserModel?.email?:"") }
    var password by remember { mutableStateOf("") }
    var confirmpassword by remember { mutableStateOf("") }
    var passwordvisibility by remember { mutableStateOf(false) }
    var confirmpasswordvisibility by remember { mutableStateOf(false) }
    var terms by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val provinces = addressVM.provinces
    val districts = addressVM.districts
    val municipalities = addressVM.municipalities
    val wards = addressVM.wards

    var provinceFieldSize by remember { mutableStateOf(Size.Zero) }
    var districtFieldSize by remember { mutableStateOf(Size.Zero) }
    var municipalityFieldSize by remember { mutableStateOf(Size.Zero) }
    var wardFieldSize by remember { mutableStateOf(Size.Zero) }

    val expandedProvince = remember { mutableStateOf(false) }
    val expandedDistrict = remember { mutableStateOf(false) }
    val expandedMunicipality = remember { mutableStateOf(false) }
    val expandedWard = remember { mutableStateOf(false) }


    val activity = context as Activity

    // 3. DEFINE CORE REGISTRATION/UPDATE LOGIC
    val onRegisterOrUpdate: () -> Unit = myValidationCheck@{ // <-- 1. Define the label here

        // Basic Validation
        if (fullname.isEmpty() || email.isEmpty() || number.isEmpty()) {
            AppUtil.showToast(context, "Please fill in Name, Email, and Phone Number.")
            return@myValidationCheck // <-- 2. Use the label to exit the lambda
        }

        if (!isGoogleSignInFlow) {
            // Standard registration requires password validation
            if (password.isEmpty() || confirmpassword.isEmpty()) {
                AppUtil.showToast(context, "Please enter and confirm your password.")
                return@myValidationCheck
            }
            if (confirmpassword != password) {
                AppUtil.showToast(context, "Passwords do not match.")
                return@myValidationCheck
            }
            if (!terms) {
                AppUtil.showToast(context, "You must agree to the Terms and Conditions.")
                return@myValidationCheck
            }
        }

        if (isGoogleSignInFlow) {
            // --- GOOGLE SIGN-IN FLOW: UPDATE EXISTING AUTH USER'S DB PROFILE (Req 2 & 3) ---

            // This is safe because if isGoogleSignInFlow is true, googleUserModel is not null
            val userId = googleUserModel!!.userId
            val updatedModel = googleUserModel.copy(
                fullname = fullname,
                email = email,
                number = number // This completes the profile required field
                // Role remains "USER"
            )

            userViewModel.addUserToDatabase(userId, updatedModel) { success, message ->
                if (success) {
                    AppUtil.showToast(context, "Profile Updated. Finding location...")

                    // Navigate to Map Activity
                    val intent = Intent(context, UserLocationMapActivity::class.java).apply {
                        putExtra("userId", userId)
                        // Treat as continuation of login flow (not a brand new registration)
                        putExtra("IS_NEW_REGISTRATION", false)
                    }
                    context.startActivity(intent)
                    activity.finish()
                } else {
                    AppUtil.showToast(context, "Profile update failed: $message")
                }
            }

        } else {
            // --- STANDARD EMAIL/PASSWORD REGISTRATION FLOW ---
            userViewModel.register(email, password) { success, message, userId ->
                if (success) {
                    val model = UserModel(
                        userId = userId,
                        email = email,
                        fullname = fullname,
                        number = number,
                        role = "USER"
                    )

                    userViewModel.addUserToDatabase(userId, model) { addSuccess, addMessage ->
                        if (addSuccess) {
                            AppUtil.showToast(context , addMessage )

                            // Navigate to Map Activity
                            val intent = Intent(context, UserLocationMapActivity::class.java)
                                .apply {
                                    putExtra("userId", userId)
                                    putExtra("IS_NEW_REGISTRATION", true) // New registration
                                }

                            context.startActivity(intent)
                            activity.finish()
                        } else {
                            AppUtil.showToast(context, addMessage)
                        }
                    }
                } else {
                    AppUtil.showToast(context, message)
                }
            }
        }
    }


    val scrollState = rememberScrollState()






    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .background(White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                // Update title based on flow
                if (isGoogleSignInFlow) "Complete Your Profile" else "Create A New Account",
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

            Spacer(modifier = Modifier.size(24.dp))


            OutlinedTextField(
                value = fullname,
                onValueChange = { data ->
                    fullname = data
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                shape = RoundedCornerShape(15.dp),
                placeholder = {
                    Text("Enter your full name")
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextBoxColor,
                    unfocusedContainerColor = TextBoxColor,
                    focusedIndicatorColor = Green,
                    unfocusedIndicatorColor = Color.Transparent
                )

            )

            Spacer(modifier = Modifier.height(20.dp))
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
                enabled = !isGoogleSignInFlow, // Disable editing for Google users
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextBoxColor,
                    unfocusedContainerColor = TextBoxColor,
                    focusedIndicatorColor = Green,
                    unfocusedIndicatorColor = Color.Transparent
                )

            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = number,
                onValueChange = { data ->
                    number = data
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                shape = RoundedCornerShape(15.dp),
                placeholder = {
                    Text("Enter your phone number")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextBoxColor,
                    unfocusedContainerColor = TextBoxColor,
                    focusedIndicatorColor = Green,
                    unfocusedIndicatorColor = Color.Transparent
                )

            )

            Spacer(modifier = Modifier.height(20.dp))

            DropdownField(
                label = addressVM.selectedProvinceName,
                placeholder = "Select Province",
                expanded = expandedProvince.value,
                onExpand = { expandedProvince.value = true },
                onDismiss = { expandedProvince.value = false },
                items = provinces.map { it.name },
                onItemSelectedText = {
                    provinces.firstOrNull { p -> p.name == it }?.let(addressVM::onProvinceSelected)
                    expandedProvince.value = false
                },
                fieldSize = provinceFieldSize,
                onSizeChange = { provinceFieldSize = it }
            )

            DropdownField(
                label = addressVM.selectedDistrictName,
                placeholder = "Select District",
                expanded = expandedDistrict.value,
                onExpand = { expandedDistrict.value = true },
                onDismiss = { expandedDistrict.value = false },
                items = districts.map { it.name },
                onItemSelectedText = {
                    districts.firstOrNull { d -> d.name == it }?.let(addressVM::onDistrictSelected)
                    expandedDistrict.value = false
                },
                fieldSize = districtFieldSize,
                onSizeChange = { districtFieldSize = it }
            )

            DropdownField(
                label = addressVM.selectedMunicipalityName,
                placeholder = "Select Municipality",
                expanded = expandedMunicipality.value,
                onExpand = { expandedMunicipality.value = true },
                onDismiss = { expandedMunicipality.value = false },
                items = municipalities.map { it.name },
                onItemSelectedText = {
                    municipalities.firstOrNull { m -> m.name == it }?.let(addressVM::onMunicipalitySelected)
                    expandedMunicipality.value = false
                },
                fieldSize = municipalityFieldSize,
                onSizeChange = { municipalityFieldSize = it }
            )

            DropdownField(
                label = addressVM.selectedWardName,
                placeholder = "Select Ward",
                expanded = expandedWard.value,
                onExpand = { expandedWard.value = true },
                onDismiss = { expandedWard.value = false },
                items = wards,
                onItemSelectedText = {
                    addressVM.onWardSelected(it)
                    expandedWard.value = false
                },
                fieldSize = wardFieldSize,
                onSizeChange = { wardFieldSize = it }
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
                                painterResource(R.drawable.baseline_visibility_24)
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

            OutlinedTextField(
                value = confirmpassword,
                onValueChange = {
                    confirmpassword = it
                },
                trailingIcon = {
                    IconButton(onClick = {
                        confirmpasswordvisibility = !confirmpasswordvisibility
                    }) {
                        Icon(
                            painter = if (confirmpasswordvisibility)
                                painterResource(R.drawable.baseline_visibility_off_24)
                            else
                                painterResource(R.drawable.baseline_visibility_24),
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (confirmpasswordvisibility) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                shape = RoundedCornerShape(15.dp),
                placeholder = {
                    Text("Confirm your password")
                },

                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextBoxColor,
                    unfocusedContainerColor = TextBoxColor,
                    focusedIndicatorColor = Green,
                    unfocusedIndicatorColor = Color.Transparent
                )

            )




            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Conditional Checkbox (only for standard registration)
                if (!isGoogleSignInFlow) {
                    Checkbox(
                        checked = terms,
                        onCheckedChange = {
                            terms = it
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Green,
                            checkmarkColor = White
                        )
                    )
                }
                Text(buildAnnotatedString {

                    if (isGoogleSignInFlow) {
                        withStyle(SpanStyle(color = Black)) {
                            append("Please fill the phone number to complete your profile.")
                        }
                    } else {
                        // Existing text for standard registration
                        withStyle(SpanStyle(color = Black)){ append("By checking this box, you agree to our") }
                        withStyle(SpanStyle(color = Blue)) { append(" Terms and Condition") }
                        withStyle(SpanStyle(color = Black)) { append(" and") }
                        withStyle(SpanStyle(color = Blue)) { append(" Privacy Policy") }
                    }

                }, modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp).clickable {
                    // Allow navigation to Login only if not in the middle of Google flow completion
                    if (!isGoogleSignInFlow) {
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        activity.finish()
                    }
                })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    onClick =  onRegisterOrUpdate, // Use the unified function,
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
                    Text(// Update button text based on flow
                        if (isGoogleSignInFlow) "Save & Continue" else "Register"
                        , fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White)
                }

            }
            Text(buildAnnotatedString {

                withStyle(SpanStyle(color = Blue)){
                    append("Already have account? ")
                }

                withStyle(SpanStyle(color = Green)) {
                    append("Login")
                }
            }, modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp))
        }

    }
}

@Composable
fun DropdownField(
    label: String,
    placeholder: String,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    items: List<String>,
    onItemSelectedText: (String) -> Unit,
    fieldSize: Size,
    onSizeChange: (Size) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    onSizeChange(coordinates.size.toSize())
                }
                .clickable { onExpand() },
            placeholder = { Text(placeholder) },
            enabled = false,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onDismiss() },
            modifier = Modifier.width(with(LocalDensity.current) { fieldSize.width.toDp() })
        ) {
            if (items.isEmpty()) {
                // show a disabled placeholder item
                DropdownMenuItem(text = { Text("No items") }, onClick = {})
            } else {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = { onItemSelectedText(item) }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun RegisterPreview(){
    RegisterBody()
}
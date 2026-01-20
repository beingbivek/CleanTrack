package com.example.cleantrack.view.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.cleantrack.view.common.PrivacyPolicyActivity
import com.example.cleantrack.view.common.TermsAndConditionActivity
import com.example.cleantrack.viewmodel.UserAddressViewModel
import com.example.cleantrack.viewmodel.UserViewModel

class RegistrationActivity : ComponentActivity() {
    private var googleUserModel: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        @Suppress("DEPRECATION")
        googleUserModel = intent.getParcelableExtra("Google_UserModel")
        setContent {
            RegisterBody(googleUserModel)
        }
    }
}

@Composable
fun RegisterBody(googleUserModel: UserModel? = null) {
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val addressVM: UserAddressViewModel = viewModel()
    val isGoogleSignInFlow = googleUserModel != null

    var fullname by remember { mutableStateOf(googleUserModel?.fullname ?: "") }
    var number by remember { mutableStateOf(googleUserModel?.number ?: "") }
    var email by remember { mutableStateOf(googleUserModel?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var confirmpassword by remember { mutableStateOf("") }
    var passwordvisibility by remember { mutableStateOf(false) }
    var confirmpasswordvisibility by remember { mutableStateOf(false) }
    var terms by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as Activity

    val provinces = addressVM.provinces
    val districts = addressVM.districts
    val municipalities = addressVM.municipalities
    val wards = addressVM.wards

    val expandedProvince = remember { mutableStateOf(false) }
    val expandedDistrict = remember { mutableStateOf(false) }
    val expandedMunicipality = remember { mutableStateOf(false) }
    val expandedWard = remember { mutableStateOf(false) }

    val onRegisterOrUpdate: () -> Unit = myValidationCheck@{
        if (fullname.isEmpty() || email.isEmpty() || number.isEmpty()) {
            AppUtil.showToast(context, "Please fill in Name, Email, and Phone Number.")
            return@myValidationCheck
        }
        if (addressVM.selectedProvinceName.isEmpty() ||
            addressVM.selectedDistrictName.isEmpty() ||
            addressVM.selectedMunicipalityName.isEmpty() ||
            addressVM.selectedWardName.isEmpty()) {
            AppUtil.showToast(context, "Please select your complete address.")
            return@myValidationCheck
        }

        if (!isGoogleSignInFlow) {
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
            val userId = googleUserModel!!.userId
            val updatedModel = googleUserModel.copy(
                fullname = fullname,
                email = email.trim(),
                number = number,
                province = addressVM.selectedProvinceName,
                district = addressVM.selectedDistrictName,
                municipality = addressVM.selectedMunicipalityName,
                ward = addressVM.selectedWardName
            )
            userViewModel.addUserToDatabase(userId, updatedModel) { success, message ->
                if (success) {
                    val intent = Intent(context, UserLocationMapActivity::class.java).apply {
                        putExtra("userId", userId)
                        putExtra("IS_NEW_REGISTRATION", false)
                    }
                    context.startActivity(intent)
                    activity.finish()
                } else {
                    AppUtil.showToast(context, "Profile update failed: $message")
                }
            }
        } else {
            userViewModel.register(email.trim(), password.trim()) { success, message, userId ->
                if (success) {
                    val model = UserModel(
                        userId = userId, email = email.trim(), fullname = fullname, number = number,
                        role = "USER", province = addressVM.selectedProvinceName,
                        district = addressVM.selectedDistrictName, municipality = addressVM.selectedMunicipalityName,
                        ward = addressVM.selectedWardName
                    )
                    userViewModel.addUserToDatabase(userId, model) { addSuccess, addMessage ->
                        if (addSuccess) {
                            val intent = Intent(context, UserLocationMapActivity::class.java).apply {
                                putExtra("userId", userId)
                                putExtra("IS_NEW_REGISTRATION", true)
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
            item {
                Spacer(modifier = Modifier.height(80.dp))
                Text(
                    text = if (isGoogleSignInFlow) "Complete Your Profile" else "Create A New Account",
                    style = TextStyle(
                        textAlign = TextAlign.Center, color = Black,
                        fontWeight = FontWeight.ExtraBold, fontSize = 30.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.size(15.dp))
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.user_logo),
                        contentDescription = null, modifier = Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.size(15.dp))
                    Text(
                        text = "Your journey to smarter,\ncooler recycling starts now",
                        style = TextStyle(
                            fontSize = 16.sp, color = Color.DarkGray,
                            textAlign = TextAlign.Center, fontWeight = FontWeight.Normal
                        )
                    )
                }
                Spacer(modifier = Modifier.size(24.dp))
            }

            item {
                OutlinedTextField(
                    value = fullname, onValueChange = { fullname = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                    shape = RoundedCornerShape(15.dp), placeholder = { Text("Enter your full name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TextBoxColor, unfocusedContainerColor = TextBoxColor,
                        focusedIndicatorColor = Green, unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                    shape = RoundedCornerShape(15.dp), placeholder = { Text("Enter your email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !isGoogleSignInFlow,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TextBoxColor, unfocusedContainerColor = TextBoxColor,
                        focusedIndicatorColor = Green, unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                OutlinedTextField(
                    value = number, onValueChange = { number = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                    shape = RoundedCornerShape(15.dp), placeholder = { Text("Enter your phone number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TextBoxColor, unfocusedContainerColor = TextBoxColor,
                        focusedIndicatorColor = Green, unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Dropdown Items
            item {
                DropdownField(
                    label = addressVM.selectedProvinceName, placeholder = "Select Province",
                    expanded = expandedProvince.value, onExpand = { expandedProvince.value = true },
                    onDismiss = { expandedProvince.value = false }, items = provinces.map { it.name },
                    onItemSelectedText = {
                        provinces.firstOrNull { p -> p.name == it }?.let(addressVM::onProvinceSelected)
                        expandedProvince.value = false
                    }
                )
            }

            item {
                DropdownField(
                    label = addressVM.selectedDistrictName, placeholder = "Select District",
                    expanded = expandedDistrict.value, onExpand = { expandedDistrict.value = true },
                    onDismiss = { expandedDistrict.value = false }, items = districts.map { it.name },
                    onItemSelectedText = {
                        districts.firstOrNull { d -> d.name == it }?.let(addressVM::onDistrictSelected)
                        expandedDistrict.value = false
                    }
                )
            }

            item {
                DropdownField(
                    label = addressVM.selectedMunicipalityName, placeholder = "Select Municipality",
                    expanded = expandedMunicipality.value, onExpand = { expandedMunicipality.value = true },
                    onDismiss = { expandedMunicipality.value = false }, items = municipalities.map { it.name },
                    onItemSelectedText = {
                        municipalities.firstOrNull { m -> m.name == it }?.let(addressVM::onMunicipalitySelected)
                        expandedMunicipality.value = false
                    }
                )
            }

            item {
                DropdownField(
                    label = addressVM.selectedWardName, placeholder = "Select Ward",
                    expanded = expandedWard.value, onExpand = { expandedWard.value = true },
                    onDismiss = { expandedWard.value = false }, items = wards,
                    onItemSelectedText = {
                        addressVM.onWardSelected(it)
                        expandedWard.value = false
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (!isGoogleSignInFlow) {
                item {
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        trailingIcon = {
                            IconButton(onClick = { passwordvisibility = !passwordvisibility }) {
                                Icon(painterResource(if (passwordvisibility) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_24), null)
                            }
                        },
                        visualTransformation = if (passwordvisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                        shape = RoundedCornerShape(15.dp), placeholder = { Text("Enter your password") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = TextBoxColor, unfocusedContainerColor = TextBoxColor,
                            focusedIndicatorColor = Green, unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                item {
                    OutlinedTextField(
                        value = confirmpassword, onValueChange = { confirmpassword = it },
                        trailingIcon = {
                            IconButton(onClick = { confirmpasswordvisibility = !confirmpasswordvisibility }) {
                                Icon(painterResource(if (confirmpasswordvisibility) R.drawable.baseline_visibility_off_24 else R.drawable.baseline_visibility_24), null)
                            }
                        },
                        visualTransformation = if (confirmpasswordvisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                        shape = RoundedCornerShape(15.dp), placeholder = { Text("Confirm your password") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = TextBoxColor, unfocusedContainerColor = TextBoxColor,
                            focusedIndicatorColor = Green, unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isGoogleSignInFlow) {
                        Checkbox(
                            checked = terms,
                            onCheckedChange = { terms = it },
                            colors = CheckboxDefaults.colors(checkedColor = Green, checkmarkColor = White)
                        )
                    }

                    val annotatedText = buildAnnotatedString {
                        if (isGoogleSignInFlow) {
                            withStyle(SpanStyle(color = Black)) {
                                append("Please fill the phone number and address to complete your profile.")
                            }
                        } else {
                            withStyle(SpanStyle(color = Black)) { append("By checking this box, you agree to our") }

                            // Annotated part for Terms
                            pushStringAnnotation(tag = "TERMS", annotation = "terms")
                            withStyle(SpanStyle(color = Blue, fontWeight = FontWeight.Bold)) {
                                append(" Terms and Condition")
                            }
                            pop()

                            withStyle(SpanStyle(color = Black)) { append(" and") }

                            // Annotated part for Privacy
                            pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
                            withStyle(SpanStyle(color = Blue, fontWeight = FontWeight.Bold)) {
                                append(" Privacy Policy")
                            }
                            pop()
                        }
                    }

                    // Use ClickableText-like behavior on a normal Text via Modifier
                    androidx.compose.foundation.text.ClickableText(
                        text = annotatedText,
                        style = TextStyle(fontSize = 14.sp),
                        modifier = Modifier.padding(vertical = 10.dp),
                        onClick = { offset ->
                            // Check if "TERMS" was clicked
                            annotatedText.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                                .firstOrNull()?.let {
                                    context.startActivity(Intent(context, TermsAndConditionActivity::class.java))
                                }

                            // Check if "PRIVACY" was clicked
                            annotatedText.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                                .firstOrNull()?.let {
                                    context.startActivity(Intent(context, PrivacyPolicyActivity::class.java))
                                }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Button(
                    onClick = onRegisterOrUpdate,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp).height(60.dp)
                        .background(brush = Brush.horizontalGradient(colors = ButtonColor), shape = RoundedCornerShape(15.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 15.dp),
                ) {
                    Text(if (isGoogleSignInFlow) "Save & Continue" else "Register", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            item {
                Text(buildAnnotatedString {
                    withStyle(SpanStyle(color = Blue)) { append("Already have account? ") }
                    withStyle(SpanStyle(color = Green)) { append("Login") }
                }, modifier = Modifier.padding(horizontal = 15.dp, vertical = 20.dp).clickable {
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    activity.finish()
                })
                Spacer(modifier = Modifier.height(40.dp))
            }
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
    onItemSelectedText: (String) -> Unit
) {
    var fieldSize by remember { mutableStateOf(Size.Zero) }

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 6.dp)) {
        OutlinedTextField(
            value = label, onValueChange = {}, enabled = false,
            modifier = Modifier.fillMaxWidth()
                .onGloballyPositioned { fieldSize = it.size.toSize() }
                .clickable { onExpand() },
            placeholder = { Text(text = placeholder, color = Green) },
            textStyle = TextStyle(color = Green),
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = Green) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Green, disabledTextColor = Green,
                disabledPlaceholderColor = Green, disabledTrailingIconColor = Green
            )
        )
        DropdownMenu(
            expanded = expanded, onDismissRequest = onDismiss,
            modifier = Modifier.width(with(LocalDensity.current) { fieldSize.width.toDp() })
        ) {
            items.forEach {
                DropdownMenuItem(
                    text = { Text(it, color = Green) },
                    onClick = { onItemSelectedText(it) }
                )
            }
        }
    }
}
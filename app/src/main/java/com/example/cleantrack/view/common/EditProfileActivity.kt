package com.example.cleantrack.view.common

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cleantrack.R
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.repository.CommonImageRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.ButtonColor
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.TextBoxColor
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.util.AppUtil
import com.example.cleantrack.viewmodel.CommonImageViewModel
import com.example.cleantrack.viewmodel.UserAddressViewModel
import com.example.cleantrack.viewmodel.UserViewModel

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EditProfileScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen() {
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val userIdFromIntent = activity?.intent?.getStringExtra("userId") ?: ""
    val userId = remember { userIdFromIntent.ifEmpty { userViewModel.getCurrentUserId() ?: "" } }

    val addressVM: UserAddressViewModel = viewModel()
    val commonImageViewModel = remember { CommonImageViewModel(CommonImageRepoImpl()) }
    val userData by userViewModel.user.observeAsState(null)

    var fullname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var profileImageUriString by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    val expandedProvince = remember { mutableStateOf(false) }
    val expandedDistrict = remember { mutableStateOf(false) }
    val expandedMunicipality = remember { mutableStateOf(false) }
    val expandedWard = remember { mutableStateOf(false) }

    var provinceFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var districtFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var municipalityFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var wardFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId)
        }
    }

    LaunchedEffect(userData) {
        userData?.let { user ->
            fullname = user.fullname
            email = user.email
            number = user.number
            profileImageUriString = user.profileImageUrl ?: ""
            addressVM.initializeAddress(user.province, user.district, user.municipality, user.ward)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> profileImageUri = uri }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = Black) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(White),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            item {
                Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = profileImageUri ?: if (profileImageUriString.isNotEmpty()) profileImageUriString else R.drawable.user_logo,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, Green, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(Green)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = White, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }

            item { ProfileInput(value = fullname, onValueChange = { fullname = it }, placeholder = "Full Name")
                Spacer(modifier = Modifier.height(15.dp)) }
            item { ProfileInput(value = email, onValueChange = { email = it }, placeholder = "Email", keyboardType = KeyboardType.Email)
                Spacer(modifier = Modifier.height(15.dp)) }
            item { ProfileInput(value = number, onValueChange = { number = it }, placeholder = "Phone Number", keyboardType = KeyboardType.Number)
                Spacer(modifier = Modifier.height(20.dp)) }

            item {
                Box(modifier = Modifier.zIndex(if (expandedProvince.value) 1f else 0f)) {
                    DropdownField(
                        label = addressVM.selectedProvinceName,
                        placeholder = "Select Province",
                        expanded = expandedProvince.value,
                        onExpand = { expandedProvince.value = true },
                        onDismiss = { expandedProvince.value = false },
                        items = addressVM.provinces.map { it.name },
                        onItemSelectedText = { name ->
                            addressVM.provinces.find { it.name == name }?.let { addressVM.onProvinceSelected(it) }
                            expandedProvince.value = false
                        },
                        fieldSize = provinceFieldSize,
                        onSizeChange = { provinceFieldSize = it }
                    )
                }
            }

            item {
                Box(modifier = Modifier.zIndex(if (expandedDistrict.value) 1f else 0f)) {
                    DropdownField(
                        label = addressVM.selectedDistrictName,
                        placeholder = "Select District",
                        expanded = expandedDistrict.value,
                        onExpand = { expandedDistrict.value = true },
                        onDismiss = { expandedDistrict.value = false },
                        items = addressVM.districts.map { it.name },
                        onItemSelectedText = { name ->
                            addressVM.districts.find { it.name == name }?.let { addressVM.onDistrictSelected(it) }
                            expandedDistrict.value = false
                        },
                        fieldSize = districtFieldSize,
                        onSizeChange = { districtFieldSize = it }
                    )
                }
            }

            item {
                Box(modifier = Modifier.zIndex(if (expandedMunicipality.value) 1f else 0f)) {
                    DropdownField(
                        label = addressVM.selectedMunicipalityName,
                        placeholder = "Select Municipality",
                        expanded = expandedMunicipality.value,
                        onExpand = { expandedMunicipality.value = true },
                        onDismiss = { expandedMunicipality.value = false },
                        items = addressVM.municipalities.map { it.name },
                        onItemSelectedText = { name ->
                            addressVM.municipalities.find { it.name == name }?.let { addressVM.onMunicipalitySelected(it) }
                            expandedMunicipality.value = false
                        },
                        fieldSize = municipalityFieldSize,
                        onSizeChange = { municipalityFieldSize = it }
                    )
                }
            }

            item {
                Box(modifier = Modifier.zIndex(if (expandedWard.value) 1f else 0f)) {
                    DropdownField(
                        label = addressVM.selectedWardName,
                        placeholder = "Select Ward",
                        expanded = expandedWard.value,
                        onExpand = { expandedWard.value = true },
                        onDismiss = { expandedWard.value = false },
                        items = addressVM.wards,
                        onItemSelectedText = {
                            addressVM.onWardSelected(it)
                            expandedWard.value = false
                        },
                        fieldSize = wardFieldSize,
                        onSizeChange = { wardFieldSize = it }
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .height(60.dp)
                        .background(brush = Brush.horizontalGradient(colors = ButtonColor), shape = RoundedCornerShape(15.dp))
                ) {
                    Button(
                        onClick = {
                            if (userId.isEmpty()) {
                                AppUtil.showToast(context, "User ID not found")
                                return@Button
                            }

                            val saveUserData: (String) -> Unit = { uploadedImageUrl ->
                                val updatedUser = UserModel(
                                    userId = userId,
                                    fullname = fullname,
                                    email = email,
                                    number = number,
                                    province = addressVM.selectedProvinceName,
                                    district = addressVM.selectedDistrictName,
                                    municipality = addressVM.selectedMunicipalityName,
                                    ward = addressVM.selectedWardName,
                                    profileImageUrl = uploadedImageUrl,
                                    role = userData?.role ?: "USER",
                                    latitude = userData?.latitude,
                                    longitude = userData?.longitude
                                )
                                userViewModel.editUserProfile(userId, updatedUser) { success, message ->
                                    AppUtil.showToast(context, message)
                                    if (success) activity?.finish()
                                }
                            }

                            if (profileImageUri != null) {
                                commonImageViewModel.uploadImage(context, profileImageUri!!) { imageUrl ->
                                    if (imageUrl != null) saveUserData(imageUrl)
                                    else AppUtil.showToast(context, "Image upload failed")
                                }
                            } else {
                                saveUserData(profileImageUriString)
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text("Update Profile", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = White)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInput(value: String, onValueChange: (String) -> Unit, placeholder: String, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
        shape = RoundedCornerShape(15.dp),
        placeholder = { Text(placeholder) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = TextBoxColor,
            unfocusedContainerColor = TextBoxColor,
            focusedIndicatorColor = Green,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
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
    fieldSize: androidx.compose.ui.geometry.Size,
    onSizeChange: (androidx.compose.ui.geometry.Size) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { onSizeChange(it.size.toSize()) }
                .clickable { onExpand() },
            placeholder = { Text(text = placeholder, color = Green) },
            textStyle = TextStyle(color = Green),
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Green) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Green,
                disabledTextColor = Green,
                disabledPlaceholderColor = Green,
                disabledTrailingIconColor = Green
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .width(with(LocalDensity.current) { fieldSize.width.toDp() })
                .background(White)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = Green) },
                    onClick = { onItemSelectedText(item) }
                )
            }
        }
    }
}
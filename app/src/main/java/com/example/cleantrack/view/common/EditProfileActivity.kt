package com.example.cleantrack.view.common

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.cleantrack.ui.theme.*
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
    val activity = LocalActivity.current // Fixed: Replaced 'as Activity' cast

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val commonImageViewModel = remember { CommonImageViewModel(CommonImageRepoImpl()) }
    val addressVM: UserAddressViewModel = viewModel()

    val userIdFromIntent = activity?.intent?.getStringExtra("userId") ?: ""
    val userId = remember { userIdFromIntent.ifEmpty { userViewModel.getCurrentUserId() ?: "" } }

    val userData by userViewModel.user.observeAsState(null)
    val isUserLoading by userViewModel.loading.observeAsState(false)
    val isImageLoading by commonImageViewModel.loading.observeAsState(false)

    // Form States
    var fullname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var profileImageUriString by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    // Dropdown UI States
    val expandedProvince = remember { mutableStateOf(false) }
    val expandedDistrict = remember { mutableStateOf(false) }
    val expandedMunicipality = remember { mutableStateOf(false) }
    val expandedWard = remember { mutableStateOf(false) }

    var provinceFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var districtFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var municipalityFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var wardFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) userViewModel.getUserById(userId)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Blue, Green, White), endY = 1000f))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Edit Profile", fontWeight = FontWeight.ExtraBold, color = White) },
                    navigationIcon = {
                        IconButton(onClick = { activity?.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            if (isUserLoading && userData == null) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green, strokeWidth = 4.dp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 40.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = profileImageUri ?: if (profileImageUriString.isNotEmpty()) profileImageUriString else R.drawable.user_logo,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(4.dp, White, CircleShape)
                                    .background(White),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { launcher.launch("image/*") },
                                modifier = Modifier.size(42.dp).clip(CircleShape).background(Green).border(2.dp, White, CircleShape)
                            ) {
                                Icon(Icons.Default.CameraAlt, null, tint = White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Personal Information", fontWeight = FontWeight.Bold, color = Green, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                ProfileInput(fullname, { fullname = it }, "Full Name")
                                Spacer(modifier = Modifier.height(12.dp))
                                ProfileInput(email, { email = it }, "Email", KeyboardType.Email)
                                Spacer(modifier = Modifier.height(12.dp))
                                ProfileInput(number, { number = it }, "Phone Number", KeyboardType.Number)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Address Details", fontWeight = FontWeight.Bold, color = Green, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                AddressDropdownGroup(addressVM, expandedProvince, expandedDistrict, expandedMunicipality, expandedWard,
                                    provinceFieldSize, districtFieldSize, municipalityFieldSize, wardFieldSize,
                                    { provinceFieldSize = it }, { districtFieldSize = it }, { municipalityFieldSize = it }, { wardFieldSize = it })
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                        Button(
                            onClick = {
                                if (userId.isEmpty()) {
                                    AppUtil.showToast(context, "User ID not found")
                                    return@Button
                                }

                                val saveUserData: (String) -> Unit = { uploadedImageUrl ->
                                    // CACHE BUSTING: Append timestamp so Coil reloads the image immediately
                                    val timestampedUrl = if (uploadedImageUrl.isNotEmpty()) {
                                        if (uploadedImageUrl.contains("?")) "$uploadedImageUrl&t=${System.currentTimeMillis()}"
                                        else "$uploadedImageUrl?t=${System.currentTimeMillis()}"
                                    } else uploadedImageUrl

                                    val updatedUser = UserModel(
                                        userId = userId,
                                        fullname = fullname, email = email, number = number,
                                        province = addressVM.selectedProvinceName,
                                        district = addressVM.selectedDistrictName,
                                        municipality = addressVM.selectedMunicipalityName,
                                        ward = addressVM.selectedWardName,
                                        profileImageUrl = timestampedUrl,
                                        role = userData?.role ?: "USER",
                                        latitude = userData?.latitude,
                                        longitude = userData?.longitude
                                    )

                                    userViewModel.editUserProfile(userId, updatedUser) { success, message ->
                                        if (success) {
                                            // Signal Dashboards to refresh
                                            activity?.setResult(Activity.RESULT_OK)
                                            AppUtil.showToast(context, "Profile updated successfully")
                                            activity?.finish()
                                        } else {
                                            AppUtil.showToast(context, message)
                                        }
                                    }
                                }

                                if (profileImageUri != null) {
                                    commonImageViewModel.uploadImage(context, profileImageUri!!) { imageUrl ->
                                        if (imageUrl != null) saveUserData(imageUrl) else AppUtil.showToast(context, "Upload failed")
                                    }
                                } else {
                                    saveUserData(profileImageUriString)
                                }
                            },
                            enabled = !isImageLoading,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green)
                        ) {
                            if (isImageLoading) {
                                CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Save Changes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ... ProfileInput, DropdownField, and AddressDropdownGroup remain the same as your provided code ...



@Composable
fun AddressDropdownGroup(
    addressVM: UserAddressViewModel,
    expandedProvince: MutableState<Boolean>,
    expandedDistrict: MutableState<Boolean>,
    expandedMunicipality: MutableState<Boolean>,
    expandedWard: MutableState<Boolean>,
    pSize: androidx.compose.ui.geometry.Size, dSize: androidx.compose.ui.geometry.Size, mSize: androidx.compose.ui.geometry.Size, wSize: androidx.compose.ui.geometry.Size,
    onPSize: (androidx.compose.ui.geometry.Size) -> Unit, onDSize: (androidx.compose.ui.geometry.Size) -> Unit, onMSize: (androidx.compose.ui.geometry.Size) -> Unit, onWSize: (androidx.compose.ui.geometry.Size) -> Unit
) {
    val dropdowns = listOf(
        Triple(expandedProvince, addressVM.selectedProvinceName to "Select Province", addressVM.provinces.map { it.name }),
        Triple(expandedDistrict, addressVM.selectedDistrictName to "Select District", addressVM.districts.map { it.name }),
        Triple(expandedMunicipality, addressVM.selectedMunicipalityName to "Select Municipality", addressVM.municipalities.map { it.name }),
        Triple(expandedWard, addressVM.selectedWardName to "Select Ward", addressVM.wards)
    )

    dropdowns.forEachIndexed { index, data ->
        Box(modifier = Modifier.zIndex(if (data.first.value) 10f else (4 - index).toFloat())) {
            DropdownField(
                label = data.second.first,
                placeholder = data.second.second,
                expanded = data.first.value,
                onExpand = { data.first.value = true },
                onDismiss = { data.first.value = false },
                items = data.third,
                onItemSelectedText = { text ->
                    when(index) {
                        0 -> addressVM.provinces.firstOrNull { it.name == text }?.let(addressVM::onProvinceSelected)
                        1 -> addressVM.districts.firstOrNull { it.name == text }?.let(addressVM::onDistrictSelected)
                        2 -> addressVM.municipalities.firstOrNull { it.name == text }?.let(addressVM::onMunicipalitySelected)
                        3 -> addressVM.onWardSelected(text)
                    }
                    data.first.value = false
                },
                fieldSize = when(index) { 0 -> pSize 1 -> dSize 2 -> mSize else -> wSize },
                onSizeChange = when(index) { 0 -> onPSize 1 -> onDSize 2 -> onMSize else -> onWSize }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ProfileInput(value: String, onValueChange: (String) -> Unit, placeholder: String, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        label = { Text(placeholder, color = Color.Gray) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Green,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = Green
        )
    )
}

@Composable
fun DropdownField(
    label: String, placeholder: String, expanded: Boolean, onExpand: () -> Unit, onDismiss: () -> Unit,
    items: List<String>, onItemSelectedText: (String) -> Unit, fieldSize: androidx.compose.ui.geometry.Size, onSizeChange: (androidx.compose.ui.geometry.Size) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth().onGloballyPositioned { onSizeChange(it.size.toSize()) }.clickable { onExpand() },
            placeholder = { Text(text = placeholder) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = Green) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.LightGray,
                disabledTextColor = Black,
                disabledPlaceholderColor = Color.Gray
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.width(with(LocalDensity.current) { fieldSize.width.toDp() }).background(White)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = Black) },
                    onClick = { onItemSelectedText(item) }
                )
            }
        }
    }
}
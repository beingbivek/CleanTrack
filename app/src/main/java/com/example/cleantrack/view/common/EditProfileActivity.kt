package com.example.cleantrack.view.common

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cleantrack.repository.CommonImageRepoImpl

import com.example.cleantrack.viewmodel.CommonImageViewModel
import com.example.cleantrack.viewmodel.UserAddressViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults

import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.cleantrack.R
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.ButtonColor
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.TextBoxColor
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.view.auth.DropdownField


class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
                EditProfileScreen()
        }
    }
}
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class) // FIX 1: Required for TopAppBar
@Composable
fun EditProfileScreen() {
    val context = LocalContext.current
    val addressVM: UserAddressViewModel = viewModel()
    // Ensure this matches your actual ViewModel/Repo name
    val commonImageViewModel = remember { CommonImageViewModel(CommonImageRepoImpl()) }

    var fullname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    // Dropdown states
    val expandedProvince = remember { mutableStateOf(false) }
    val expandedDistrict = remember { mutableStateOf(false) }
    val expandedMunicipality = remember { mutableStateOf(false) }
    val expandedWard = remember { mutableStateOf(false) }

    // Size states for dropdown matching
    var provinceFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var districtFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var municipalityFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var wardFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> profileImageUri = uri }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar( // CenterAligned looks cleaner for Edit Profile
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
            // 1. Profile Image Section
            item {
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    AsyncImage(
                        model = profileImageUri ?: R.drawable.user_logo,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, Green, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Green)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = White, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }

            // 2. Input Fields (Individual items for better performance)
            item {
                ProfileInput(value = fullname, onValueChange = { fullname = it }, placeholder = "Full Name")
                Spacer(modifier = Modifier.height(15.dp))
            }
            item {
                ProfileInput(value = email, onValueChange = { email = it }, placeholder = "Email", keyboardType = KeyboardType.Email)
                Spacer(modifier = Modifier.height(15.dp))
            }
            item {
                ProfileInput(value = number, onValueChange = { number = it }, placeholder = "Phone Number", keyboardType = KeyboardType.Number)
                Spacer(modifier = Modifier.height(20.dp))
            }


// --- ADDRESS SECTION WITH Z-INDEX FIX ---

            item {
                Box(modifier = Modifier.zIndex(if (expandedProvince.value) 1f else 0f)) {
                    DropdownField(
                        label = addressVM.selectedProvinceName,
                        placeholder = "Select Province",
                        expanded = expandedProvince.value,
                        onExpand = { expandedProvince.value = true },
                        onDismiss = { expandedProvince.value = false },
                        items = addressVM.provinces.map { it.name },
                        onItemSelectedText = {
                            addressVM.provinces.firstOrNull { p -> p.name == it }?.let(addressVM::onProvinceSelected)
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
                        onItemSelectedText = {
                            addressVM.districts.firstOrNull { d -> d.name == it }?.let(addressVM::onDistrictSelected)
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
                        onItemSelectedText = {
                            addressVM.municipalities.firstOrNull { m -> m.name == it }?.let(addressVM::onMunicipalitySelected)
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

            // 4. Update Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .height(60.dp)
                        .background(brush = Brush.horizontalGradient(colors = ButtonColor), shape = RoundedCornerShape(15.dp))
                ) {
                    Button(
                        onClick = { /* TODO: Save Logic */ },
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
// Exactly like your Registration Dropdown
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
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
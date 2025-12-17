package com.example.cleantrack.view.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cleantrack.R
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.ButtonColor
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.viewModel.UserAddressViewModel

class UserAddressActivtiy : ComponentActivity() {

    private val vm: UserAddressViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserAddressBody(vm)
        }
    }
}

@Preview
@Composable
fun UserAddressBody(vm: UserAddressViewModel = viewModel()) {
    val provinces = vm.provinces
    val districts = vm.districts
    val municipalities = vm.municipalities
    val wards = vm.wards

    var provinceFieldSize = remember { Size.Zero }
    var districtFieldSize = remember { Size.Zero }
    var municipalityFieldSize = remember { Size.Zero }
    var wardFieldSize = remember { Size.Zero }

    var expandedProvince = remember { mutableStateOf(false) }
    var expandedDistrict = remember { mutableStateOf(false) }
    var expandedMunicipality = remember { mutableStateOf(false) }
    var expandedWard = remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                "User Address",
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.home_logo),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp)
                )


                Text(
                    text = "Select your exact location from the dropdown below to continue",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.Center)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Province dropdown
            DropdownField(
                label = vm.selectedProvinceName,
                placeholder = "Select Province",
                expanded = expandedProvince.value,
                onExpand = { expandedProvince.value = true },
                onDismiss = { expandedProvince.value = false },
                items = provinces.map { it.name },
                onItemSelectedText = { name ->
                    // find province object and notify VM
                    provinces.firstOrNull { it.name == name }?.let { vm.onProvinceSelected(it) }
                    expandedProvince.value = false
                },
                fieldSize = provinceFieldSize,
                onSizeChange = { provinceFieldSize = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // District dropdown
            DropdownField(
                label = vm.selectedDistrictName,
                placeholder = "Select District",
                expanded = expandedDistrict.value,
                onExpand = { expandedDistrict.value = true },
                onDismiss = { expandedDistrict.value = false },
                items = districts.map { it.name },
                onItemSelectedText = { name ->
                    districts.firstOrNull { it.name == name }?.let { vm.onDistrictSelected(it) }
                    expandedDistrict.value = false
                },
                fieldSize = districtFieldSize,
                onSizeChange = { districtFieldSize = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Municipality dropdown
            DropdownField(
                label = vm.selectedMunicipalityName,
                placeholder = "Select Municipality",
                expanded = expandedMunicipality.value,
                onExpand = { expandedMunicipality.value = true },
                onDismiss = { expandedMunicipality.value = false },
                items = municipalities.map { it.name },
                onItemSelectedText = { name ->
                    municipalities.firstOrNull { it.name == name }?.let { vm.onMunicipalitySelected(it) }
                    expandedMunicipality.value = false
                },
                fieldSize = municipalityFieldSize,
                onSizeChange = { municipalityFieldSize = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Ward dropdown
            DropdownField(
                label = vm.selectedWardName,
                placeholder = "Select Ward",
                expanded = expandedWard.value,
                onExpand = { expandedWard.value = true },
                onDismiss = { expandedWard.value = false },
                items = wards,
                onItemSelectedText = { name ->
                    vm.onWardSelected(name)
                    expandedWard.value = false
                },
                fieldSize = wardFieldSize,
                onSizeChange = { wardFieldSize = it }
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val selection = vm.currentSelection()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp)
                    .background(
                        brush = Brush.horizontalGradient(ButtonColor),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 15.dp)
            ) {
                Text("Submit", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

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
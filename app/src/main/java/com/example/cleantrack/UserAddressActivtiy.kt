package com.example.cleantrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.ButtonColor
import com.example.cleantrack.ui.theme.White


class UserAddressActivtiy : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserAddressBody()
        }
    }
}

@Composable
fun UserAddressBody() {

    // PROVINCE
    var expandedProvince by remember { mutableStateOf(false) }
    var selectedProvince by remember { mutableStateOf("Select Province") }
    val provinces = listOf("Province 1", "Province 2", "Bagmati", "Gandaki", "Lumbini", "Karnali", "Sudurpashchim")
    var provinceFieldSize by remember { mutableStateOf(Size.Zero) }

    // DISTRICT
    var expandedDistrict by remember { mutableStateOf(false) }
    var selectedDistrict by remember { mutableStateOf("Select District") }
    val districts = listOf("Kathmandu", "Lalitpur", "Bhaktapur", "Pokhara", "Chitwan", "Jhapa")
    var districtFieldSize by remember { mutableStateOf(Size.Zero) }

    // MUNICIPALITY
    var expandedMunicipality by remember { mutableStateOf(false) }
    var selectedMunicipality by remember { mutableStateOf("Select Municipality") }
    val municipalities = listOf("KMC", "LMC", "BMC", "Tokha", "Hetauda", "Dharan")
    var municipalityFieldSize by remember { mutableStateOf(Size.Zero) }

    // WARD
    var expandedWard by remember { mutableStateOf(false) }
    var selectedWard by remember { mutableStateOf("Select Ward") }
    val wards = (1..32).map { "Ward $it" }
    var wardFieldSize by remember { mutableStateOf(Size.Zero) }


    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                "User Address",
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
            ) {
                Image(
                    painter = painterResource(R.drawable.contact_support_logo),
                    contentDescription = null,
                    modifier = Modifier.size(150.dp)
                )
                Spacer(modifier = Modifier.size(15.dp))
                Text(
                    text = "Select your exact location from the dropdown below to continue",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            Spacer(modifier = Modifier.size(24.dp))

            DropdownField(
                label = selectedProvince,
                placeholder = "Select Province",
                expanded = expandedProvince,
                onExpand = { expandedProvince = true },
                onDismiss = { expandedProvince = false },
                items = provinces,
                onItemSelected = {
                    selectedProvince = it
                    expandedProvince = false
                },
                fieldSize = provinceFieldSize,
                onSizeChange = { provinceFieldSize = it }
            )

            Spacer(modifier = Modifier.size(16.dp))

            DropdownField(
                label = selectedDistrict,
                placeholder = "Select District",
                expanded = expandedDistrict,
                onExpand = { expandedDistrict = true },
                onDismiss = { expandedDistrict = false },
                items = districts,
                onItemSelected = {
                    selectedDistrict = it
                    expandedDistrict = false
                },
                fieldSize = districtFieldSize,
                onSizeChange = { districtFieldSize = it }
            )

            Spacer(modifier = Modifier.size(16.dp))

            DropdownField(
                label = selectedMunicipality,
                placeholder = "Select Municipality",
                expanded = expandedMunicipality,
                onExpand = { expandedMunicipality = true },
                onDismiss = { expandedMunicipality = false },
                items = municipalities,
                onItemSelected = {
                    selectedMunicipality = it
                    expandedMunicipality = false
                },
                fieldSize = municipalityFieldSize,
                onSizeChange = { municipalityFieldSize = it }
            )

            Spacer(modifier = Modifier.size(16.dp))

            DropdownField(
                label = selectedWard,
                placeholder = "Select Ward",
                expanded = expandedWard,
                onExpand = { expandedWard = true },
                onDismiss = { expandedWard = false },
                items = wards,
                onItemSelected = {
                    selectedWard = it
                    expandedWard = false
                },
                fieldSize = wardFieldSize,
                onSizeChange = { wardFieldSize = it }
            )


            Spacer(modifier = Modifier.size(16.dp))

            Button(
                onClick = {},
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


@Composable
fun DropdownField(
    label: String,
    placeholder: String,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    fieldSize: Size,
    onSizeChange: (Size) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
            modifier = Modifier
                .width(with(LocalDensity.current) { fieldSize.width.toDp() })
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = { onItemSelected(item) }
                )
            }
        }
    }
}


@Preview
@Composable
fun UserAddressPreview() {
    UserAddressBody()
}

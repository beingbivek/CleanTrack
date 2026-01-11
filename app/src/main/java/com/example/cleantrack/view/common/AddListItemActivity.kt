package com.example.cleantrack.view.common

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import coil.compose.AsyncImage
import com.example.cleantrack.model.ProductModel
import com.example.cleantrack.repository.CommonImageRepoImpl
import com.example.cleantrack.repository.ProductRepoImpl
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.viewmodel.CommonImageViewModel
import com.example.cleantrack.viewmodel.ProductViewModel

class AddListItemActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Get the logged-in user's ID passed from the previous screen
        val userId = intent.getStringExtra("USER_ID") ?: "UNKNOWN_USER"

        setContent {
            AddListItemScreen(userId)
        }
    }
}

@Composable
fun AddListItemScreen(userId: String) {
    val context = LocalContext.current

    // ViewModels
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val commonImageViewModel = remember { CommonImageViewModel(CommonImageRepoImpl()) }

    // Form States
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startingPrice by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Select Category") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // UI States
    var isUploading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val categories = listOf("Plastic", "Metal", "Electronics", "Paper", "Glass", "Other")

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(White)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "List New Product",
                    style = TextStyle(textAlign = TextAlign.Center, color = Black, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp),
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Image Selection Box
                Box(
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(TextBoxColor, RoundedCornerShape(15.dp))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tap to add product image", color = Color.Gray)
                        }
                    } else {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Icon(Icons.Default.Close, "Remove", tint = Color.Red)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Input: Product Name
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green, unfocusedBorderColor = Color.Transparent, focusedContainerColor = TextBoxColor, unfocusedContainerColor = TextBoxColor)
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Dropdown: Category
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp)) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().onGloballyPositioned { textFieldSize = it.size.toSize() }.clickable { expanded = true },
                        label = { Text("Category") },
                        enabled = false,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Black, disabledBorderColor = Color.Transparent, disabledContainerColor = TextBoxColor, disabledLabelColor = Color.Gray)
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(text = { Text(category) }, onClick = { selectedCategory = category; expanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                // Input: Price
                OutlinedTextField(
                    value = startingPrice,
                    onValueChange = { startingPrice = it },
                    label = { Text("Starting Bid Price ($)") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green, unfocusedBorderColor = Color.Transparent, focusedContainerColor = TextBoxColor, unfocusedContainerColor = TextBoxColor)
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Input: Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Product Description") },
                    modifier = Modifier.fillMaxWidth().height(120.dp).padding(horizontal = 15.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green, unfocusedBorderColor = Color.Transparent, focusedContainerColor = TextBoxColor, unfocusedContainerColor = TextBoxColor)
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Submit Button
                Button(
                    onClick = {
                        val price = startingPrice.toDoubleOrNull()
                        if (productName.isEmpty() || price == null || selectedCategory == "Select Category" || selectedImageUri == null) {
                            Toast.makeText(context, "Please complete all fields", Toast.LENGTH_SHORT).show()
                        } else {
                            isUploading = true
                            commonImageViewModel.uploadImage(context, selectedImageUri!!) { imageUrl ->
                                if (imageUrl != null) {
                                    val newProduct = ProductModel(
                                        productName = productName,
                                        pDescription = description,
                                        pImageUrl = imageUrl,
                                        pCategory = selectedCategory,
                                        startingBidPrice = price,
                                        currentBidPrice = price,
                                        sellerId = userId,
                                        auctionEndTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // Default 24 hours
                                    )
                                    productViewModel.addProduct(newProduct) { success, message ->
                                        isUploading = false
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        if (success) (context as Activity).finish()
                                    }
                                } else {
                                    isUploading = false
                                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .height(60.dp)
                        .background(brush = Brush.horizontalGradient(colors = ButtonColor), shape = RoundedCornerShape(15.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("List Item Now", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Full screen loading overlay
            if (isUploading) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).clickable(enabled = false) {})
            }
        }
    }
}
package com.example.cleantrack.view.common

import android.app.Activity
import android.app.DatePickerDialog
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cleantrack.model.ProductModel
import com.example.cleantrack.repository.CommonImageRepoImpl
import com.example.cleantrack.repository.ProductRepoImpl
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.viewmodel.CommonImageViewModel
import com.example.cleantrack.viewmodel.ProductViewModel
import java.util.*
import java.text.SimpleDateFormat

class AddListItemActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val productId = intent.getStringExtra("PRODUCT_ID")

        setContent { AddListItemScreen(userId, productId) }
    }
}

@Composable
fun AddListItemScreen(userId: String, productId: String?) {
    val context = LocalContext.current
    val isEditMode = productId != null

    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val imageViewModel = remember { CommonImageViewModel(CommonImageRepoImpl()) }

    // Form States
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startingPrice by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Select Category") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    // New Auction Date States
    var auctionEndTimeStamp by remember { mutableLongStateOf(0L) }
    var auctionDateDisplay by remember { mutableStateOf("Select End Date") }

    // Date Formatter
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Setup DatePickerDialog
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, day, 23, 59, 59)
            auctionEndTimeStamp = selectedCalendar.timeInMillis
            auctionDateDisplay = sdf.format(selectedCalendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = System.currentTimeMillis()

    // FETCH DATA IF EDITING
    LaunchedEffect(productId) {
        if (isEditMode) productViewModel.getProductById(productId!!)
    }

    val fetchedProduct by productViewModel.product.observeAsState()
    LaunchedEffect(fetchedProduct) {
        fetchedProduct?.let {
            productName = it.productName
            description = it.pDescription
            startingPrice = it.startingBidPrice.toString()
            selectedCategory = it.pCategory
            existingImageUrl = it.pImageUrl
            auctionEndTimeStamp = it.auctionEndTime
            auctionDateDisplay = sdf.format(Date(it.auctionEndTime))
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedImageUri = it }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {

            // --- BACK BUTTON AND TITLE ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { (context as? Activity)?.finish() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) "Edit Product" else "List New Product",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Image Selector
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).background(TextBoxColor, RoundedCornerShape(15.dp)).clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                } else if (existingImageUrl != null) {
                    AsyncImage(model = existingImageUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
                } else {
                    Text("Tap to add image", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(15.dp))

            // Category Picker
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory, onValueChange = {}, label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                    enabled = false, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Black, disabledBorderColor = Color.Gray)
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("Plastic", "Metal", "Electronics", "Paper", "Glass", "Other").forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCategory = cat; expanded = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // Auction End Date Picker UI
            OutlinedTextField(
                value = auctionDateDisplay,
                onValueChange = {},
                label = { Text("Auction End Date") },
                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                enabled = false,
                trailingIcon = { Icon(Icons.Default.DateRange, null, tint = Green) },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Black,
                    disabledBorderColor = Color.Gray,
                    disabledLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(value = startingPrice, onValueChange = { startingPrice = it }, label = { Text("Starting Price (Rs.)") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(120.dp))

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    if (auctionEndTimeStamp == 0L) {
                        Toast.makeText(context, "Please select an auction end date", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val price = startingPrice.toDoubleOrNull() ?: 0.0
                    isUploading = true

                    val saveProduct: (String) -> Unit = { finalUrl ->
                        if (isEditMode) {
                            val updateData = mutableMapOf(
                                "productName" to productName,
                                "pDescription" to description,
                                "pCategory" to selectedCategory,
                                "startingBidPrice" to price,
                                "currentBidPrice" to price,
                                "pImageUrl" to finalUrl,
                                "auctionEndTime" to auctionEndTimeStamp
                            )
                            productViewModel.updateProduct(productId!!, updateData) { success, msg ->
                                isUploading = false
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) (context as Activity).finish()
                            }
                        } else {
                            val newProduct = ProductModel(
                                productName = productName,
                                pDescription = description,
                                pImageUrl = finalUrl,
                                pCategory = selectedCategory,
                                startingBidPrice = price,
                                currentBidPrice = price,
                                sellerId = userId,
                                auctionEndTime = auctionEndTimeStamp,
                                productStatus = "active"
                            )
                            productViewModel.addProduct(newProduct) { success, msg ->
                                isUploading = false
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) (context as Activity).finish()
                            }
                        }
                    }

                    if (selectedImageUri != null) imageViewModel.uploadImage(context, selectedImageUri!!) { saveProduct(it ?: "") }
                    else if (existingImageUrl != null) saveProduct(existingImageUrl!!)
                    else { isUploading = false; Toast.makeText(context, "Image required", Toast.LENGTH_SHORT).show() }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                enabled = !isUploading
            ) {
                if (isUploading) CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                else Text(if (isEditMode) "Update Listing" else "List Item Now", fontWeight = FontWeight.Bold)
            }
        }
    }
}
package com.example.cleantrack.view.common

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cleantrack.model.NotificationPayload
import com.example.cleantrack.model.ProductModel
import com.example.cleantrack.repository.NotificationRepoImpl
import com.example.cleantrack.repository.ProductRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.viewmodel.NotificationViewModel
import com.example.cleantrack.viewmodel.ProductViewModel
import java.util.Calendar

class MarketplaceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val currentUserId = intent.getStringExtra("USER_ID") ?: ""
        val isAdmin = intent.getBooleanExtra("IS_ADMIN", false)
        setContent { MarketplaceScreen(currentUserId, isAdmin) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(currentUserId: String, isAdmin: Boolean) {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl(), UserRepoImpl()) }

    // FIX: Start as null to distinguish between 'loading' and 'empty'
    val products by productViewModel.allProducts.observeAsState(null)
    val isLoading by productViewModel.loading.observeAsState(false)

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var productToDelete by remember { mutableStateOf<ProductModel?>(null) }
    var productToMarkSold by remember { mutableStateOf<ProductModel?>(null) }

    LaunchedEffect(Unit) { productViewModel.fetchAllProducts() }

    // --- DIALOGS (Keep existing logic) ---
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to remove '${productToDelete?.productName}'?") },
            confirmButton = {
                TextButton(onClick = {
                    productViewModel.deleteProduct(productToDelete!!.productId) { _, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        productToDelete = null
                        productViewModel.fetchAllProducts() // Refresh
                    }
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { productToDelete = null }) { Text("Cancel") } }
        )
    }

    if (productToMarkSold != null) {
        AlertDialog(
            onDismissRequest = { productToMarkSold = null },
            title = { Text("Sell Item Now?") },
            text = { Text("End auction early for '${productToMarkSold?.productName}'?") },
            confirmButton = {
                TextButton(onClick = {
                    productViewModel.updateStatus(productToMarkSold!!.productId, "sold") { _, _ ->
                        Toast.makeText(context, "Item marked as Sold!", Toast.LENGTH_SHORT).show()
                        productViewModel.fetchAllProducts() // Refresh list
                        productToMarkSold = null
                    }
                }) { Text("Confirm Sale", color = Green) }
            },
            dismissButton = { TextButton(onClick = { productToMarkSold = null }) { Text("Wait") } }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CleanTrack Market", color = White, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    context.startActivity(Intent(context, AddListItemActivity::class.java).apply {
                        putExtra("USER_ID", currentUserId)
                    })
                },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("List Item") },
                containerColor = Green,
                contentColor = White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Blue, Green, White), endY = 1500f))
        ) {
            Column(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search items...", color = White.copy(0.7f)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedBorderColor = White,
                        unfocusedBorderColor = White.copy(0.5f)
                    )
                )

                // --- Tabs ---
                Row(
                    modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf("All Items", "My Listings", "Purchased")
                    tabs.forEachIndexed { index, title ->
                        FilterChip(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            label = { Text(title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green,
                                selectedLabelColor = White,
                                labelColor = White,
                                containerColor = Color.Transparent
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, // This ensures the border parameter is valid
                                selected = selectedTab == index,
                                borderColor = White.copy(0.5f),
                                selectedBorderColor = Green,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.dp
                            )
                        )
                    }
                }

                // Filtering Logic
                val filteredProducts = products?.filter {
                    val matchesSearch = it.productName.contains(searchQuery, ignoreCase = true)
                    when(selectedTab) {
                        0 -> matchesSearch && it.productStatus == "active" && it.auctionEndTime > System.currentTimeMillis()
                        1 -> matchesSearch && it.sellerId == currentUserId
                        2 -> matchesSearch && it.highestBidderId == currentUserId && it.productStatus == "sold"
                        else -> false
                    }
                } ?: emptyList()

                // Content Display Logic
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLoading || products == null) {
                        // FIX: Use Green color so it's visible on the white-gradient bottom
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Green,
                            strokeWidth = 4.dp
                        )
                    } else if (filteredProducts.isEmpty()) {
                        Text(
                            "No products found",
                            modifier = Modifier.align(Alignment.Center),
                            color = White.copy(0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredProducts) { product ->
                                ProductCard(
                                    product = product,
                                    isOwner = product.sellerId == currentUserId,
                                    isAdmin = isAdmin,
                                    currentUserId = currentUserId,
                                    isMyListingsTab = (selectedTab == 1),
                                    productViewModel = productViewModel,
                                    onEdit = {
                                        context.startActivity(Intent(context, AddListItemActivity::class.java).apply {
                                            putExtra("USER_ID", currentUserId)
                                            putExtra("PRODUCT_ID", product.productId)
                                        })
                                    },
                                    onDelete = { productToDelete = product },
                                    onMarkSold = { productToMarkSold = product },
                                    onClick = {
                                        context.startActivity(Intent(context, ProductDetailActivity::class.java).apply {
                                            putExtra("PRODUCT_ID", product.productId)
                                            putExtra("USER_ID", currentUserId)
                                        })
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductModel,
    isOwner: Boolean,
    isAdmin: Boolean,
    currentUserId: String,
    isMyListingsTab: Boolean,
    productViewModel: ProductViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkSold: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isExpired = product.auctionEndTime < System.currentTimeMillis()
    val hasBids = !product.highestBidderId.isNullOrBlank()

    // --- RELIST DATE PICKER ---
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, day, 23, 59, 59)
            productViewModel.relistProduct(product.productId, selectedCalendar.timeInMillis) { _, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                productViewModel.fetchAllProducts()
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = product.pImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.2f).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                if (product.productStatus == "sold" && product.highestBidderId == currentUserId) {
                    Surface(color = Blue, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp), shape = RoundedCornerShape(4.dp)) {
                        Text("WON", color = White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = product.productName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "Rs. ${product.currentBidPrice}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Green)

                val timeLeft = formatTimeRemaining(product.auctionEndTime - System.currentTimeMillis())
                Text(
                    text = if (product.productStatus == "sold") "Sold" else timeLeft,
                    fontSize = 11.sp,
                    color = if (timeLeft == "Ended" || product.productStatus == "sold") Color.Red else Color.Gray
                )

                if (isOwner && isMyListingsTab) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isExpired && !hasBids) {
                        Button(
                            onClick = { datePickerDialog.show() },
                            modifier = Modifier.fillMaxWidth().height(32.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Relist Item", fontSize = 11.sp, color = White) }
                    }
                    if (hasBids && product.productStatus != "sold") {
                        Button(
                            onClick = onMarkSold,
                            modifier = Modifier.fillMaxWidth().height(32.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Mark Sold", fontSize = 11.sp, color = White) }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (product.productStatus == "active") {
                            IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Edit, null, tint = Blue, modifier = Modifier.size(18.dp)) }
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(18.dp)) }
                    }
                } else if (isAdmin) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(18.dp)) }
                    }
                }
            }
        }
    }
}

// Helper (keep as is)
fun formatTimeRemaining(milliseconds: Long): String {
    if (milliseconds <= 0) return "Ended"
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        days > 0 -> "${days}d left"
        hours > 0 -> "${hours}h left"
        minutes > 0 -> "${minutes}m left"
        else -> "${seconds}s left"
    }
}
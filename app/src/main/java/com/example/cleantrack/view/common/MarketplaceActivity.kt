package com.example.cleantrack.view.common

import android.app.Activity
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
import com.example.cleantrack.model.ProductModel
import com.example.cleantrack.repository.ProductRepoImpl
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.viewmodel.ProductViewModel

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
    val products by productViewModel.allProducts.observeAsState(emptyList())
    val isLoading by productViewModel.loading.observeAsState(false)

    var searchQuery by remember { mutableStateOf("") }

    // Updated: 0 = All, 1 = My Listings, 2 = Purchased
    var selectedTab by remember { mutableIntStateOf(0) }

    var productToDelete by remember { mutableStateOf<ProductModel?>(null) }

    LaunchedEffect(Unit) { productViewModel.fetchAllProducts() }

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to remove '${productToDelete?.productName}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    productViewModel.deleteProduct(productToDelete!!.productId) { _, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        productToDelete = null
                    }
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { productToDelete = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CleanTrack Market", color = White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                contentColor = White
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Blue, Green, White), endY = 1300f))) {
            Column(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search for items...", color = White.copy(0.7f)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(12.dp)),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = White.copy(0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = White, unfocusedTextColor = White)
                )

                // Updated: Filter row with three options
                Row(
                    modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("All Items") },
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedTab == 0) Green else White.copy(0.5f))
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("My Listings") },
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedTab == 1) Green else White.copy(0.5f))
                    )
                    FilterChip(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = { Text("Purchased") },
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedTab == 2) Green else White.copy(0.5f))
                    )
                }

                val filteredProducts = products?.filter {
                    val matchesSearch = it.productName.contains(searchQuery, ignoreCase = true)

                    when(selectedTab) {
                        0 -> { // ALL ITEMS: Active and not expired
                            val isNotExpired = it.auctionEndTime > System.currentTimeMillis()
                            matchesSearch && it.productStatus == "active" && isNotExpired
                        }
                        1 -> { // MY LISTINGS: Created by me
                            matchesSearch && it.sellerId == currentUserId
                        }
                        2 -> { // PURCHASED: I am the highest bidder and it is marked SOLD
                            matchesSearch && it.highestBidderId == currentUserId && it.productStatus == "sold"
                        }
                        else -> false
                    }
                } ?: emptyList()

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = White) }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(bottom = 80.dp),
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
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = product.pImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.2f).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )

                // Overlay Badge for Purchased Items
                if (product.productStatus == "sold" && product.highestBidderId == currentUserId) {
                    Surface(
                        color = Blue,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("WON", color = White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = product.productName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "Rs. ${product.currentBidPrice}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Green)

                    val timeLeft = formatTimeRemaining(product.auctionEndTime - System.currentTimeMillis())
                    Text(
                        text = if (product.productStatus == "sold") "Sold" else timeLeft,
                        fontSize = 10.sp,
                        color = if (timeLeft == "Ended" || product.productStatus == "sold") Color.Red else Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (isOwner && isMyListingsTab) {
                        val isExpired = product.auctionEndTime < System.currentTimeMillis()
                        val hasBids = !product.highestBidderId.isNullOrBlank()

                        if (isExpired && !hasBids) {
                            TextButton(onClick = {
                                val newTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                                productViewModel.relistProduct(product.productId, newTime) { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("Relist", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (isExpired && hasBids && product.productStatus != "sold") {
                            Button(
                                onClick = { productViewModel.updateStatus(product.productId, "sold") { _, _ -> } },
                                colors = ButtonDefaults.buttonColors(containerColor = Green),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Mark Sold", fontSize = 10.sp, color = White)
                            }
                        }

                        if (product.productStatus == "active") {
                            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, "Edit", tint = Blue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    if ((isOwner && isMyListingsTab) || isAdmin) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

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
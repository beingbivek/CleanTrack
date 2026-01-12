package com.example.cleantrack.view.common

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.Blue
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.White
// Ensure this import exists
import com.example.cleantrack.viewmodel.ProductViewModel

class MarketplaceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Retrieve the USER_ID passed from the Login/Dashboard
        val currentUserId = intent.getStringExtra("USER_ID") ?: ""

        setContent {
            MarketplaceScreen(currentUserId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(currentUserId: String) {
    val context = LocalContext.current

    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val products by productViewModel.allProducts.observeAsState(emptyList())
    val isLoading by productViewModel.loading.observeAsState(false)

    var searchQuery by remember { mutableStateOf("") }

    // NEW: State for the seller filter (false = show all, true = show mine)
    var showOnlyMyListings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        productViewModel.fetchAllProducts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CleanTrack Market", color = White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val intent = Intent(context, AddListItemActivity::class.java).apply {
                        putExtra("USER_ID", currentUserId)
                    }
                    context.startActivity(intent)
                },
                icon = { Icon(Icons.Filled.Add, "List Item") },
                text = { Text("List Item", fontWeight = FontWeight.SemiBold) },
                containerColor = Green,
                contentColor = White,
                shape = RoundedCornerShape(50)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Blue, Green, Color.White),
                        startY = 0f,
                        endY = 1300f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search for items...", color = White.copy(alpha = 0.7f)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = White.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = White,
                        unfocusedBorderColor = White.copy(alpha = 0.5f),
                        cursorColor = White,
                        focusedTextColor = White,
                        unfocusedTextColor = White
                    )
                )

                // --- UPDATED: FILTER CHIPS SECTION ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !showOnlyMyListings,
                        onClick = { showOnlyMyListings = false },
                        label = { Text("All Items") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Green,
                            selectedLabelColor = White,
                            containerColor = White.copy(alpha = 0.2f),
                            labelColor = White
                        ),
                        // FIXED: Using BorderStroke directly to avoid the "enabled" parameter error
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (!showOnlyMyListings) Green else White.copy(alpha = 0.5f)
                        )
                    )

                    FilterChip(
                        selected = showOnlyMyListings,
                        onClick = { showOnlyMyListings = true },
                        label = { Text("My Listings") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Green,
                            selectedLabelColor = White,
                            containerColor = White.copy(alpha = 0.2f),
                            labelColor = White
                        ),
                        // FIXED: Using BorderStroke directly
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (showOnlyMyListings) Green else White.copy(alpha = 0.5f)
                        )
                    )
                }

                // Updated combined filtering logic
                val filteredProducts = products?.filter { product ->
                    val matchesSearch = product.productName.contains(searchQuery, ignoreCase = true)
                    val matchesSeller = if (showOnlyMyListings) product.sellerId == currentUserId else true
                    matchesSearch && matchesSeller
                } ?: emptyList()

                // Content Area
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = White)
                    }
                } else {
                    if (filteredProducts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No items found", color = White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredProducts) { product ->
                                ProductCard(product = product) {
                                    context.startActivity(Intent(context, ProductDetailActivity::class.java).apply {
                                        putExtra("PRODUCT_ID", product.productId)
                                        putExtra("USER_ID", currentUserId)
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: ProductModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = product.pImageUrl,
                contentDescription = product.productName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.productName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Current Bid: $${product.currentBidPrice}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Green
            )

            val timeLeft = formatTimeRemaining(product.auctionEndTime - System.currentTimeMillis())
            Text(
                text = timeLeft,
                fontSize = 11.sp,
                color = if (timeLeft == "Ended") Color.Red else Color.Gray
            )
        }
    }
}

// Keep your existing FilterButton and formatTimeRemaining functions here...
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
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
import com.example.cleantrack.model.ProductModel // You'll create this model
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.Blue
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.White
//import com.example.cleantrack.view.common.LoadingIndicator // Assuming you have a common LoadingIndicator

class MarketplaceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarketplaceScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    // Replace with your actual ViewModel later
    val products = remember { mutableStateListOf<ProductModel>() } // Example dummy data
    // Add some dummy data for now
    LaunchedEffect(Unit) {
        products.addAll(
            listOf(
                ProductModel("1", "Recycled PET Bottles", "High-quality plastic bottles, cleaned and sorted.", "https://via.placeholder.com/150/0000FF/FFFFFF?text=PET+Bottles", "Plastic", 15.0, 20.0, "active", System.currentTimeMillis() + 7200000), // 2 hours left
                ProductModel("2", "Copper Wire (5kg)", "Scrap copper wire, ideal for recycling.", "https://via.placeholder.com/150/FF0000/FFFFFF?text=Copper+Wire", "Metal", 30.0, 45.0, "active", System.currentTimeMillis() + 432000000), // 5 days left
                ProductModel("3", "E-Waste Circuit Boards", "Assorted circuit boards for component recovery.", "https://via.placeholder.com/150/00FF00/FFFFFF?text=Circuit+Boards", "Electronics", 22.0, 30.0, "active", System.currentTimeMillis() + 36000000), // 10 hours left
                ProductModel("4", "Old Smartphone Parts", "Various parts from broken smartphones.", "https://via.placeholder.com/150/FFFF00/000000?text=Smartphone+Parts", "Electronics", 10.0, 15.0, "active", System.currentTimeMillis() + 86400000), // 1 day left
                ProductModel("5", "Used Car Tires (4 pcs)", "Set of 4 used car tires, good for upcycling.", "https://via.placeholder.com/150/800080/FFFFFF?text=Car+Tires", "Rubber", 50.0, 60.0, "active", System.currentTimeMillis() + 172800000) // 2 days left
            )
        )
    }

    // TODO: Integrate actual ViewModel to fetch products

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
                onClick = { /* TODO: Navigate to List New Item Screen */ },
                icon = { Icon(Icons.Filled.Add, "List Item") },
                text = { Text("List Item", fontWeight = FontWeight.SemiBold) },
                containerColor = Green,
                contentColor = White,
                shape = RoundedCornerShape(50)
            )
        },
        floatingActionButtonPosition = FabPosition.End, // Position FAB
        containerColor = Color.Transparent // Allow gradient to show
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Blue, Green, Color.White),
                        startY = 0f,
                        endY = 1300f
                    )
                )
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
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = White.copy(alpha = 0.7f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = White,
                    unfocusedBorderColor = White.copy(alpha = 0.5f),
                    cursorColor = White,
                    focusedLabelColor = White,
                    unfocusedLabelColor = White.copy(alpha = 0.7f),
                    focusedContainerColor = White.copy(alpha = 0.1f),
                    unfocusedContainerColor = White.copy(alpha = 0.05f),
                    focusedTextColor = White,
                    unfocusedTextColor = White
                )
            )

            // Filter/Sort Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                FilterButton("Categories") { /* TODO: Implement category filter */ }
                FilterButton("Sort By") { /* TODO: Implement sort options */ }
                FilterButton("Filters") { /* TODO: Implement advanced filters */ }
            }

            // Product Grid
            if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No products listed yet.", color = White.copy(alpha = 0.7f), fontSize = 16.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products) { product ->
                        ProductCard(product = product) {
                            // TODO: Navigate to ProductDetailActivity
                            context.startActivity(Intent(context, ProductDetailActivity::class.java).apply {
                                putExtra("PRODUCT_ID", product.id)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Green.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, color = White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ProductCard(product: ProductModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), // Rounded corners for product cards
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Current Bid: $${product.currentBidPrice}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Green
            )
            Spacer(modifier = Modifier.height(4.dp))
            val timeLeft = formatTimeRemaining(product.auctionEndTime - System.currentTimeMillis())
            Text(
                text = "Time Left: $timeLeft",
                fontSize = 11.sp,
                color = Color.Gray
            )
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
        days > 0 -> "${days}d ${hours % 24}h"
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}
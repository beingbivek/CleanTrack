package com.example.cleantrack.view.common

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cleantrack.model.ProductModel
import com.example.cleantrack.ui.theme.*

class ProductDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // In a real app, you would fetch the product from your ViewModel using the ID
        val productId = intent.getStringExtra("PRODUCT_ID") ?: ""

        setContent {
            ProductDetailScreen(productId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(productId: String) {
    val context = LocalContext.current
    var bidAmount by remember { mutableStateOf("") }

    // Dummy Data for Preview
    val product = remember {
        ProductModel(
            productId = productId,
            productName = "Recycled PET Bottles (50kg)",
            pDescription = "High-quality, industrial-grade PET bottles. All bottles have been washed, caps removed, and sorted by color. Perfect for plastic manufacturing or upcycling projects.",
            pImageUrl = "https://via.placeholder.com/600/0000FF/FFFFFF?text=PET+Bottles",
            pCategory = "Plastic",
            startingBidPrice = 10.0,
            currentBidPrice = 15.5,
            auctionEndTime = System.currentTimeMillis() + 3600000, // 1 hour left
            sellerId = "seller123"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Blue, Green, Color.White),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Product Details", fontWeight = FontWeight.Bold, color = White) },
                    navigationIcon = {
                        IconButton(onClick = { (context as? ProductDetailActivity)?.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Product Image
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(20.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    AsyncImage(
                        model = product.pImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // 2. Info Content Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Green.copy(0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    product.pCategory.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Green
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Text(" 2h 30m left", fontSize = 12.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(product.productName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Black)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(product.pDescription, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), thickness = 0.5.dp)

                        // 3. Bidding Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Current Bid", fontSize = 12.sp, color = Color.Gray)
                                Text("$${product.currentBidPrice}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Green)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Started at", fontSize = 12.sp, color = Color.Gray)
                                Text("$${product.startingBidPrice}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Bid Input
                        OutlinedTextField(
                            value = bidAmount,
                            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) bidAmount = it },
                            label = { Text("Enter your bid amount") },
                            prefix = { Text("$ ") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val enteredBid = bidAmount.toDoubleOrNull() ?: 0.0
                                if (enteredBid > product.currentBidPrice) {
                                    Toast.makeText(context, "Bid Placed Successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Bid must be higher than current price", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green)
                        ) {
                            Icon(Icons.Default.Gavel, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Place My Bid", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 4. Seller Info
                        Text("Seller Information", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color.LightGray, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("John Doe Recycling", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Verified Seller", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}
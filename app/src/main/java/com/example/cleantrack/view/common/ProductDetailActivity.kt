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
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.cleantrack.repository.ProductRepoImpl
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.viewmodel.ProductViewModel

class ProductDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val productId = intent.getStringExtra("PRODUCT_ID") ?: ""
        val userId = intent.getStringExtra("USER_ID") ?: ""

        setContent {
            ProductDetailScreen(productId, userId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(productId: String, userId: String) {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }

    // Observe the real product from Firebase
    val product by productViewModel.product.observeAsState()
    var bidAmount by remember { mutableStateOf("") }

    // Fetch product details on load
    LaunchedEffect(productId) {
        productViewModel.getProductById(productId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(Blue, Green, White), startY = 0f, endY = 1000f))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Product Details", fontWeight = FontWeight.Bold, color = White) },
                    navigationIcon = {
                        IconButton(onClick = { (context as? ProductDetailActivity)?.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            product?.let { currentProduct ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. Product Image
                    Card(
                        modifier = Modifier.fillMaxWidth().height(300.dp).padding(20.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        AsyncImage(
                            model = currentProduct.pImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // 2. Info Content Card
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = Green.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        currentProduct.pCategory.uppercase(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Green
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Timer, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                val timeLeft = formatTimeRemaining(currentProduct.auctionEndTime - System.currentTimeMillis())
                                Text(" $timeLeft", fontSize = 12.sp, color = if(timeLeft == "Ended") Color.Red else Color.Gray)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(currentProduct.productName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(currentProduct.pDescription, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)

                            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), thickness = 0.5.dp)

                            // 3. Bidding Section
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Current Bid", fontSize = 12.sp, color = Color.Gray)
                                    Text("$${currentProduct.currentBidPrice}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Green)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Started at", fontSize = 12.sp, color = Color.Gray)
                                    Text("$${currentProduct.startingBidPrice}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Black)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            if (currentProduct.sellerId == userId) {
                                // Seller cannot bid on their own product
                                Text("You are the seller of this item.", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
                            } else if (currentProduct.auctionEndTime < System.currentTimeMillis()) {
                                Text("This auction has ended.", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Red, fontWeight = FontWeight.Bold)
                            } else {
                                OutlinedTextField(
                                    value = bidAmount,
                                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) bidAmount = it },
                                    label = { Text("Enter higher bid") },
                                    prefix = { Text("$ ") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val enteredBid = bidAmount.toDoubleOrNull() ?: 0.0
                                        if (enteredBid > currentProduct.currentBidPrice) {
                                            productViewModel.updateBid(productId, userId, enteredBid) { success, msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                if (success) bidAmount = ""
                                            }
                                        } else {
                                            Toast.makeText(context, "Bid must be higher than current price", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                                ) {
                                    Icon(Icons.Default.Gavel, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Place My Bid", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Seller ID: ${currentProduct.sellerId}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = White)
            }
        }
    }
}
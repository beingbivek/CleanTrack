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
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.viewmodel.ProductViewModel
import com.example.cleantrack.viewmodel.UserViewModel

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
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    val product by productViewModel.product.observeAsState()
    val seller by userViewModel.sellerData.observeAsState()
    val highestBidder by userViewModel.highestBidderData.observeAsState()

    var bidAmount by remember { mutableStateOf("") }

    // 1. Initial Fetch of the product
    LaunchedEffect(productId) {
        productViewModel.getProductById(productId)
    }

    // 2. Fetch Seller details when product loads
    LaunchedEffect(product?.sellerId) {
        product?.sellerId?.let { userViewModel.getSellerInfo(it) }
    }

    // 3. Fetch Bidder details whenever the bidder ID changes
    LaunchedEffect(product?.highestBidderId) {
        val bidderId = product?.highestBidderId
        if (!bidderId.isNullOrBlank()) {
            userViewModel.getHighestBidderInfo(bidderId)
        }
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
                        modifier = Modifier.fillMaxWidth().height(320.dp).padding(20.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(10.dp)
                    ) {
                        AsyncImage(
                            model = currentProduct.pImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // 2. Main Content Card
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
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Green
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Timer, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                val timeLeft = formatTimeRemaining(currentProduct.auctionEndTime - System.currentTimeMillis())
                                Text(" $timeLeft", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if(timeLeft == "Ended") Color.Red else Color.Gray)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(currentProduct.productName, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Black, lineHeight = 34.sp)

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(currentProduct.pDescription, fontSize = 15.sp, color = Color.DarkGray, lineHeight = 22.sp)

                            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), thickness = 0.8.dp)

                            // 3. COMPARISON SECTION
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text("Initial Bid", fontSize = 13.sp, color = Color.Gray)
                                    Text("$${currentProduct.startingBidPrice}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Current Bid", fontSize = 14.sp, color = Color.Gray)
                                    Text("$${currentProduct.currentBidPrice}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Green)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Bidders", fontSize = 14.sp, color = Color.Gray)
                                    val totalBidders = currentProduct.bids?.size ?: 0
                                    Text("$totalBidders", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Blue)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // 4. HIGHEST BIDDER INFORMATION (UPDATED LOGIC)
                            Column {
                                if (currentProduct.highestBidderId.isNullOrBlank()) {
                                    Text(
                                        "No bids yet. Be the first to bid!",
                                        modifier = Modifier.padding(bottom = 20.dp),
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    highestBidder?.let { bidder ->
                                        Surface(
                                            color = Green.copy(0.1f),
                                            shape = RoundedCornerShape(16.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Green.copy(0.2f)),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                                        ) {
                                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text("🏆", fontSize = 24.sp)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text("Highest Bidder", fontSize = 14.sp, color = Green, fontWeight = FontWeight.Bold)
                                                    Text(bidder.fullname, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Black)
                                                    Text("📞 ${bidder.number ?: "No Phone"}", color = Color.Gray, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                                }
                                            }
                                        }
                                    } ?: Text("Loading bidder details...", color = Color.Gray, modifier = Modifier.padding(bottom = 20.dp))
                                }
                            }

                            // Bidding Input Logic
                            if (currentProduct.sellerId == userId) {
                                Text("You listed this item.", modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray, fontWeight = FontWeight.Bold)
                            } else if (currentProduct.auctionEndTime < System.currentTimeMillis()) {
                                Text("AUCTION CLOSED", modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Red, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            } else {
                                OutlinedTextField(
                                    value = bidAmount,
                                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) bidAmount = it },
                                    label = { Text("Your Bid Amount", fontWeight = FontWeight.Bold) },
                                    prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    shape = RoundedCornerShape(16.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val enteredBid = bidAmount.toDoubleOrNull() ?: 0.0
                                        if (enteredBid > currentProduct.currentBidPrice) {
                                            productViewModel.updateBid(productId, userId, enteredBid) { _, msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                if (msg.contains("success", true)) {
                                                    bidAmount = ""
                                                    // Re-fetch product to get the new highestBidderId immediately
                                                    productViewModel.getProductById(productId)
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Bid must be higher than $${currentProduct.currentBidPrice}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(60.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                                ) {
                                    Icon(Icons.Default.Gavel, null)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("PLACE MY BID", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                            HorizontalDivider(thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(24.dp))

                            // 5. Seller Details
                            Text("Seller Information", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = Blue.copy(0.1f), modifier = Modifier.size(56.dp)) {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.padding(12.dp), tint = Blue)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(seller?.fullname ?: "Loading Seller...", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                                    Text("📞 ${seller?.number ?: "No Phone"}", color = Color.Gray, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(modifier = Modifier.height(50.dp))
                        }
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = White)
            }
        }
    }
}
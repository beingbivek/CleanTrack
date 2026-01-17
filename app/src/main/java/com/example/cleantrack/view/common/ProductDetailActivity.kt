package com.example.cleantrack.view.common

import android.app.Activity
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
        setContent { ProductDetailScreen(productId, userId) }
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

    LaunchedEffect(productId) { productViewModel.getProductById(productId) }
    LaunchedEffect(product?.sellerId) { product?.sellerId?.let { userViewModel.getSellerInfo(it) } }

    // Refresh bidder info whenever the bidder ID changes (after a new bid)
    LaunchedEffect(product?.highestBidderId) {
        product?.highestBidderId?.let {
            if(it.isNotEmpty()) userViewModel.getHighestBidderInfo(it)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Blue, Green, White), endY = 1000f))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Product Details", fontWeight = FontWeight.Bold, color = White) },
                    navigationIcon = {
                        IconButton(onClick = { (context as? Activity)?.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            product?.let { currentProduct ->
                val isExpired = currentProduct.auctionEndTime < System.currentTimeMillis()
                val isSold = currentProduct.productStatus == "sold"
                val isOwner = currentProduct.sellerId == userId

                Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
                    Card(modifier = Modifier.fillMaxWidth().height(320.dp).padding(20.dp), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(10.dp)) {
                        AsyncImage(model = currentProduct.pImageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }

                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            if (isSold) {
                                Surface(color = Blue.copy(0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                    Text("SALE COMPLETED", modifier = Modifier.padding(8.dp), color = Blue, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = Green.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                                    Text(currentProduct.pCategory.uppercase(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Green)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Timer, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                val timeLeft = formatTimeRemaining(currentProduct.auctionEndTime - System.currentTimeMillis())
                                Text(" ${if(isSold) "Ended" else timeLeft}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if(timeLeft == "Ended" || isSold) Color.Red else Color.Gray)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(currentProduct.productName, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Black)
                            Text(currentProduct.pDescription, fontSize = 15.sp, color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

                            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Initial Bid", fontSize = 13.sp, color = Color.Gray)
                                    Text("Rs. ${currentProduct.startingBidPrice}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if(isSold) "Sold For" else "Current Bid", fontSize = 14.sp, color = Color.Gray)
                                    Text("Rs. ${currentProduct.currentBidPrice}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = if(isSold) Blue else Green)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Bids", fontSize = 14.sp, color = Color.Gray)
                                    Text("${currentProduct.bids?.size ?: 0}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Blue)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // --- HIGHEST BIDDER CARD (FIXED) ---
                            if (currentProduct.highestBidderId.isNullOrBlank()) {
                                Text(if(isExpired) "Ended with no bids" else "No bids yet", color = Color.Gray)
                            } else {
                                highestBidder?.let { bidder ->
                                    Surface(
                                        color = if(isExpired || isSold) Green.copy(0.1f) else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if(isExpired) Green else Color.LightGray),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(if(isExpired || isSold) "👑" else "🏆", fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(if(isExpired || isSold) "Winner" else "Highest Bidder", fontSize = 12.sp, color = Green, fontWeight = FontWeight.Bold)
                                                Text(bidder.fullname, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                                                // Check if field is called .number or .phoneNumber in your Model
                                                // I removed the IF check so it shows for everyone.
                                                // Adjust back if you want privacy.
                                                Text("📞 ${bidder.number}", color = Color.Gray, fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            // --- BIDDING INPUT LOGIC ---
                            if (!isSold && !isOwner && !isExpired) {
                                OutlinedTextField(
                                    value = bidAmount,
                                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) bidAmount = it },
                                    label = { Text("Your Bid") },
                                    prefix = { Text("Rs. ") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        val bid = bidAmount.toDoubleOrNull() ?: 0.0
                                        if (bid > currentProduct.currentBidPrice) {
                                            productViewModel.updateBid(productId, userId, bid) { _, msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                if(msg.contains("success")) {
                                                    bidAmount = ""
                                                    productViewModel.getProductById(productId)
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Bid must be higher than Rs. ${currentProduct.currentBidPrice}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                                ) {
                                    Text("PLACE BID", fontWeight = FontWeight.Bold)
                                }
                            } else if (isExpired && !isSold) {
                                Text("AUCTION CLOSED", color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }

                            Spacer(modifier = Modifier.height(30.dp))
                            Text("Seller Information", fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = Blue.copy(0.1f), modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Person, null, modifier = Modifier.padding(8.dp), tint = Blue) }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(seller?.fullname ?: "Loading...", fontWeight = FontWeight.Bold)
                                    Text("📞 ${seller?.number ?: ""}", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
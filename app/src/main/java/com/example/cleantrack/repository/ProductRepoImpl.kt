package com.example.cleantrack.repository


import com.example.cleantrack.model.ProductModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.cleantrack.util.NotificationTypes

class ProductRepoImpl : ProductRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Products")
    private val notificationRepo = NotificationRepoImpl()





    override fun addProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        val id = ref.push().key ?: return callback(false, "Failed to generate ID")
        model.productId = id

        // Using toMap() ensures the data structure matches Firebase expectations
        ref.child(id).setValue(model.toMap())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Product added Successfully.")
                } else {
                    callback(false, it.exception?.message ?: "Unknown error")
                }
            }
    }

    override fun getAllProducts(callback: (Boolean, String, List<ProductModel>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allProducts = mutableListOf<ProductModel>()
                for (data in snapshot.children) {
                    val product = data.getValue(ProductModel::class.java)
                    if (product != null) {
                        allProducts.add(product)
                    }
                }
                callback(true, "Products fetched", allProducts)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }


    override fun updateBid(productId: String, bidderId: String, bidAmount: Double, callback: (Boolean, String) -> Unit) {
        ref.child(productId).get().addOnSuccessListener { snapshot ->
            val product = snapshot.getValue(ProductModel::class.java)
            val sellerId = product?.sellerId.orEmpty()
            val productName = product?.productName.orEmpty().ifBlank { "item" }

            val updates = HashMap<String, Any>()
            updates["currentBidPrice"] = bidAmount
            updates["highestBidderId"] = bidderId
            updates["bids/$bidderId"] = bidAmount

            ref.child(productId).updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (sellerId.isNotBlank() && sellerId != bidderId) {
                        notificationRepo.sendNotification(
                            recipientId = sellerId,
                            recipientRole = "USER",
                            title = "New bid placed",
                            message = "Someone placed a bid on your $productName.",
                            type = NotificationTypes.BID_PLACED,
                            metadata = mapOf("productId" to productId)
                        )
                    }
                    callback(true, "Bid placed successfully!")
                } else {
                    callback(false, task.exception?.message ?: "Failed to place bid")
                }
            }
        }.addOnFailureListener { error ->
            callback(false, error.message ?: "Failed to place bid")
        }
    }

    override fun getProductById(
        productId: String,
        callback: (Boolean, String, ProductModel?) -> Unit
    ) {
        ref.child(productId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val product = snapshot.getValue(ProductModel::class.java)
                    callback(true, "Product fetched", product)
                } else {
                    callback(false, "Product not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    // Add these to ProductRepoImpl.kt

    override fun updateProduct(productId: String, model: Map<String, Any>, callback: (Boolean, String) -> Unit) {
        ref.child(productId).updateChildren(model)
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Product updated successfully")
                else callback(false, it.exception?.message ?: "Update failed")
            }
    }

    override fun deleteProduct(productId: String, callback: (Boolean, String) -> Unit) {
        ref.child(productId).removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Product deleted")
                else callback(false, it.exception?.message ?: "Delete failed")
            }
    }

    override fun updateProductFields(productId: String, fields: Map<String, Any>, callback: (Boolean, String) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Products").child(productId)
        val newStatus = fields["productStatus"] as? String

        if (newStatus == "sold") {
            dbRef.get().addOnSuccessListener { snapshot ->
                val product = snapshot.getValue(ProductModel::class.java)
                dbRef.updateChildren(fields).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        product?.let { current ->
                            val bidders = (current.bids?.keys ?: emptySet()).toMutableSet()
                            if (current.highestBidderId.isNotBlank()) {
                                bidders.add(current.highestBidderId)
                            }
                            bidders.forEach { bidderId ->
                                val isWinner = bidderId == current.highestBidderId
                                notificationRepo.sendNotification(
                                    recipientId = bidderId,
                                    recipientRole = "USER",
                                    title = if (isWinner) "You won the bid!" else "Auction ended",
                                    message = if (isWinner) {
                                        "You won ${current.productName} for Rs. ${current.currentBidPrice}."
                                    } else {
                                        "${current.productName} was sold. Thanks for participating."
                                    },
                                    type = NotificationTypes.BID_WON,
                                    metadata = mapOf("productId" to productId)
                                )
                            }
                        }
                        callback(true, "Product updated successfully")
                    } else {
                        callback(false, task.exception?.message ?: "Update failed")
                    }
                }
            }.addOnFailureListener { error ->
                callback(false, error.message ?: "Update failed")
            }
        } else {
            dbRef.updateChildren(fields).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Product updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Update failed")
                }
            }
        }
    }


}

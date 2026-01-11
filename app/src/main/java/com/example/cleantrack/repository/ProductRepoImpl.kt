package com.example.cleantrack.repository


import com.example.cleantrack.model.ProductModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductRepoImpl : ProductRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Products")





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
        // Create a map for multiple path updates
        val updates = HashMap<String, Any>()

        // Update top-level product fields
        updates["currentBidPrice"] = bidAmount
        updates["highestBidderId"] = bidderId

        // Update the specific bidder in the 'bids' child (Your Bidders List)
        // This will appear as Products -> productId -> bids -> bidderId : amount
        updates["bids/$bidderId"] = bidAmount

        ref.child(productId).updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Bid placed successfully!")
            } else {
                callback(false, task.exception?.message ?: "Failed to place bid")
            }
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





}
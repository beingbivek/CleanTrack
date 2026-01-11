package com.example.cleantrack.repository

import com.example.cleantrack.model.ProductModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductRepoImpl : ProductRepo {


    val database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref : DatabaseReference = database.getReference("Products")



    override fun addProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        val id = ref.push().key.toString()
        model.productId = id // Assigning the generated ID to the model field

        ref.child(id).setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(true, "Product added Successfully.")

                }else{
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun getAllProducts(callback: (Boolean, String, List<ProductModel>?) -> Unit) {
        // Real-time listener to keep the Marketplace updated automatically
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){

                    var allProducts = mutableListOf<ProductModel>()

                    for (data in snapshot.children){
                        val product = data.getValue(ProductModel::class.java)
                        if (product != null){
                            allProducts.add(product)
                        }
                    }

                    callback(true, "All product fetched", allProducts)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "${error.message}", emptyList())
            }
        })

    }


    override fun updateBid(productId: String, bidderId: String, bidAmount: Double, callback: (Boolean, String) -> Unit) {
        val updates = mapOf(
            "currentBidPrice" to bidAmount,
            "highestBidderId" to bidderId
        )
        ref.child(productId).updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Bid placed!")
            } else {
                callback(false, task.exception?.message ?: "Failed to place bid")
            }
        }
    }

    override fun getProductById(
        productId: String,
        callback: (Boolean, String, ProductModel?) -> Unit
    ) {
        ref.child(productId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){

                    val product = snapshot.getValue(ProductModel::class.java)
                    if (product != null){
                        callback(true, "Product fetched", product)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "${error.message}", null)
            }
        })
    }
}
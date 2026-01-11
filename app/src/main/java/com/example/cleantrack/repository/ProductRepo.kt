package com.example.cleantrack.repository

import com.example.cleantrack.model.ProductModel

interface ProductRepo {
    fun addProduct(product: ProductModel, callback: (Boolean, String) -> Unit)

    fun getAllProducts(callback: (Boolean, String, List<ProductModel>?) -> Unit)

    fun updateBid(productId: String, bidderId: String, bidAmount: Double, callback: (Boolean, String) -> Unit)

    fun getProductById(productId: String, callback: (Boolean, String, ProductModel?) -> Unit)
}
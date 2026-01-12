package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.ProductModel
import com.example.cleantrack.repository.ProductRepo

class ProductViewModel(val repo: ProductRepo) : ViewModel() {

    private val _allProducts = MutableLiveData<List<ProductModel>?>()
    val allProducts: MutableLiveData<List<ProductModel>?> get() = _allProducts

    // Observed by ProductDetailActivity to show real-time price and bidder updates
    private val _product = MutableLiveData<ProductModel?>()
    val product: MutableLiveData<ProductModel?> get() = _product

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    /**
     * Used in AddListItemActivity to list new waste items
     */
    fun addProduct(model: ProductModel, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        repo.addProduct(model) { success, message ->
            _loading.postValue(false)
            callback(success, message)
        }
    }

    /**
     * Used in MarketplaceActivity to populate the grid
     */
    fun fetchAllProducts() {
        _loading.postValue(true)
        repo.getAllProducts { success, _, list ->
            if (success) {
                _allProducts.postValue(list)
            }
            _loading.postValue(false)
        }
    }

    /**
     * Used in ProductDetailActivity to load details for a specific item
     */
    fun getProductById(productId: String) {
        _loading.postValue(true)
        repo.getProductById(productId) { success, _, productData ->
            if (success) {
                _product.postValue(productData)
            }
            _loading.postValue(false)
        }
    }

    /**
     * Standardized function to handle placing bids.
     * Updates current price, highest bidder, and adds to the bidders list.
     */
    fun updateBid(productId: String, bidderId: String, amount: Double, callback: (Boolean, String) -> Unit) {
        repo.updateBid(productId, bidderId, amount) { success, message ->
            // Note: We don't manually update _product here because the
            // ValueEventListener in the Repo handles real-time updates.
            callback(success, message)
        }
    }

    // Add these to ProductViewModel.kt

    fun updateProduct(productId: String, model: Map<String, Any>, callback: (Boolean, String) -> Unit) {
        repo.updateProduct(productId, model, callback)
    }

    fun deleteProduct(productId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteProduct(productId, callback)
    }

    // Inside ProductViewModel.kt
    fun updateStatus(productId: String, status: String, callback: (Boolean, String) -> Unit) {
        repo.updateProductFields(productId, mapOf("productStatus" to status), callback)
    }

    fun relistProduct(productId: String, newEndTime: Long, callback: (Boolean, String) -> Unit) {
        val updates = mapOf(
            "auctionEndTime" to newEndTime,
            "productStatus" to "active"
        )
        repo.updateProductFields(productId, updates, callback)
    }
}
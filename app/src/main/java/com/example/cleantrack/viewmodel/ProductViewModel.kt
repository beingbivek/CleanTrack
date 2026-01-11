package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.ProductModel
import com.example.cleantrack.repository.ProductRepo

class ProductViewModel(val repo: ProductRepo) : ViewModel() {

    private val _allProducts = MutableLiveData<List<ProductModel>?>()
    val allProducts: MutableLiveData<List<ProductModel>?> get() = _allProducts

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    // Function to list a product (Used in Add Product Screen)
    fun addProduct(model: ProductModel, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        repo.addProduct(model) { success, message ->
            _loading.postValue(false)
            callback(success, message)
        }
    }

    // Function to fetch all products (Used in Marketplace Screen)
    fun fetchAllProducts() {
        _loading.postValue(true)
        repo.getAllProducts { success, message, list ->
            if (success) {
                _allProducts.postValue(list)
            }
            _loading.postValue(false)
        }
    }

    // Function to update bidding (Used in Product Details Screen)
    fun placeBid(productId: String, bidderId: String, amount: Double, callback: (Boolean, String) -> Unit) {
        repo.updateBid(productId, bidderId, amount, callback)
    }
}
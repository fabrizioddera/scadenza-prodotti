package com.example.scadenzaprodotti.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.scadenzaprodotti.data.AppDatabase
import com.example.scadenzaprodotti.data.Product
import com.example.scadenzaprodotti.data.ProductRepository
import kotlinx.coroutines.launch

class ProductViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ProductRepository(AppDatabase.getInstance(app).productDao())
    val products: LiveData<List<Product>> = repo.allProducts.asLiveData()

    fun insert(product: Product) = viewModelScope.launch { repo.insert(product) }
    fun update(product: Product) = viewModelScope.launch { repo.update(product) }
    fun delete(product: Product) = viewModelScope.launch { repo.delete(product) }

    suspend fun getProductById(id: Int): Product? = repo.getById(id)
}

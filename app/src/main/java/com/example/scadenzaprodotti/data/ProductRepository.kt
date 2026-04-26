package com.example.scadenzaprodotti.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(private val dao: ProductDao) {
    val allProducts: Flow<List<Product>> = dao.getAllProducts()

    suspend fun insert(product: Product) = dao.insert(product)
    suspend fun update(product: Product) = dao.update(product)
    suspend fun delete(product: Product) = dao.delete(product)
    suspend fun getById(id: Int): Product? = dao.getById(id)
    suspend fun getAllSync(): List<Product> = dao.getAllProductsSync()
}

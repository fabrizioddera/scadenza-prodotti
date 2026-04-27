package com.example.scadenzaprodotti.network

import com.example.scadenzaprodotti.dto.ProductDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object ProductLookupService {
    private val client = OkHttpClient()

    suspend fun lookupBarcode(barcode: String): ProductDto? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://world.openfoodfacts.org/api/v0/product/$barcode.json")
                .header("User-Agent", "ScadenzaProdotti/1.0 (Android)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val body = response.body.string()
            val json = JSONObject(body)
            if (json.optInt("status") != 1) return@withContext null
            val product = json.optJSONObject("product") ?: return@withContext null
            val name = product.optString("product_name_it").takeIf { it.isNotBlank() }
                ?: product.optString("product_name").takeIf { it.isNotBlank() }

            val imageUrl = product.optString("image_front_small_url").takeIf { it.isNotBlank() }

            ProductDto(name, imageUrl)
        } catch (_: Exception) {
            null
        }
    }
}
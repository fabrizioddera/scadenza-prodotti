package com.fabrizioddera.scadenzaprodotti.data

import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    fun toJson(products: List<Product>): String {
        val array = JSONArray()
        products.forEach { p ->
            val obj = JSONObject()
            obj.put("name", p.name)
            obj.put("barcode", p.barcode ?: JSONObject.NULL)
            obj.put("expiryDate", p.expiryDate)
            obj.put("quantity", p.quantity)
            obj.put("notes", p.notes ?: JSONObject.NULL)
            obj.put("daysBeforeNotify", p.daysBeforeNotify)
            obj.put("openedDate", p.openedDate ?: JSONObject.NULL)
            obj.put("daysUntilBadAfterOpening", p.daysUntilBadAfterOpening ?: JSONObject.NULL)
            array.put(obj)
        }
        return array.toString(2)
    }

    fun fromJson(json: String): List<Product> {
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            Product(
                id = 0,
                name = obj.getString("name"),
                barcode = obj.optString("barcode", "").takeIf { it.isNotEmpty() },
                expiryDate = obj.getLong("expiryDate"),
                quantity = obj.optInt("quantity", 1),
                notes = obj.optString("notes", "").takeIf { it.isNotEmpty() },
                daysBeforeNotify = obj.optInt("daysBeforeNotify", 3),
                imageUrl = obj.optString("imageUrl", ""),
                openedDate = if (obj.isNull("openedDate")) null else obj.getLong("openedDate"),
                daysUntilBadAfterOpening = if (obj.isNull("daysUntilBadAfterOpening")) null else obj.getInt("daysUntilBadAfterOpening")
            )
        }
    }
}

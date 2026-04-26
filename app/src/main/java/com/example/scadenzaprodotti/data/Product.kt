package com.example.scadenzaprodotti.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val barcode: String? = null,
    val expiryDate: Long,
    val quantity: Int = 1,
    val notes: String? = null,
    val daysBeforeNotify: Int = 3
) {
    val localExpiryDate: LocalDate get() = LocalDate.ofEpochDay(expiryDate)

    val daysUntilExpiry: Long get() = ChronoUnit.DAYS.between(LocalDate.now(), localExpiryDate)
}

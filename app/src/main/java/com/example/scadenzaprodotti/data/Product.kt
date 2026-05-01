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
    val daysBeforeNotify: Int = 3,
    val imageUrl: String?,
    val openedDate: Long? = null,
    val daysUntilBadAfterOpening: Int? = null
) {
    val localExpiryDate: LocalDate get() = LocalDate.ofEpochDay(expiryDate)

    val isOpened: Boolean get() = openedDate != null

    val openedLocalDate: LocalDate? get() = openedDate?.let { LocalDate.ofEpochDay(it) }

    val effectiveExpiryDate: LocalDate get() {
        val openExpiry = if (openedDate != null && daysUntilBadAfterOpening != null) {
            LocalDate.ofEpochDay(openedDate).plusDays(daysUntilBadAfterOpening.toLong())
        } else null
        return if (openExpiry != null && openExpiry.isBefore(localExpiryDate)) openExpiry else localExpiryDate
    }

    val daysUntilExpiry: Long get() = ChronoUnit.DAYS.between(LocalDate.now(), effectiveExpiryDate)
}

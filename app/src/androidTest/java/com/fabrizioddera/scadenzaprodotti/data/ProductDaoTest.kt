package com.fabrizioddera.scadenzaprodotti.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ProductDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ProductDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.productDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun product(
        id: Int = 0,
        name: String = "Latte",
        expiryDate: Long = LocalDate.now().plusDays(10).toEpochDay(),
        quantity: Int = 1
    ) = Product(
        id = id,
        name = name,
        expiryDate = expiryDate,
        quantity = quantity,
        imageUrl = null
    )

    @Test
    fun insert_and_getById_returns_product() = runTest {
        val p = product(name = "Latte")
        dao.insert(p)
        val all = dao.getAllProductsSync()
        assertEquals(1, all.size)
        val saved = dao.getById(all.first().id)
        assertNotNull(saved)
        assertEquals("Latte", saved!!.name)
    }

    @Test
    fun getById_nonexistent_returns_null() = runTest {
        val result = dao.getById(999)
        assertNull(result)
    }

    @Test
    fun insert_replace_conflict_updates_existing() = runTest {
        dao.insert(product(name = "Latte"))
        val id = dao.getAllProductsSync().first().id
        dao.insert(product(id = id, name = "Latte UHT"))
        val all = dao.getAllProductsSync()
        assertEquals(1, all.size)
        assertEquals("Latte UHT", all.first().name)
    }

    @Test
    fun update_persists_changes() = runTest {
        dao.insert(product(name = "Yogurt"))
        val saved = dao.getAllProductsSync().first()
        dao.update(saved.copy(name = "Yogurt Greco", quantity = 3))
        val updated = dao.getById(saved.id)
        assertEquals("Yogurt Greco", updated!!.name)
        assertEquals(3, updated.quantity)
    }

    @Test
    fun delete_removes_product() = runTest {
        dao.insert(product(name = "Burro"))
        val saved = dao.getAllProductsSync().first()
        dao.delete(saved)
        assertNull(dao.getById(saved.id))
        assertEquals(0, dao.getAllProductsSync().size)
    }

    @Test
    fun insertAll_inserts_multiple_products() = runTest {
        dao.insertAll(listOf(
            product(name = "A"),
            product(name = "B"),
            product(name = "C")
        ))
        assertEquals(3, dao.getAllProductsSync().size)
    }

    @Test
    fun getAllProducts_ordered_by_expiryDate_asc() = runTest {
        val today = LocalDate.now().toEpochDay()
        dao.insertAll(listOf(
            product(name = "Scade tardi", expiryDate = today + 30),
            product(name = "Scade presto", expiryDate = today + 1),
            product(name = "Scade medio", expiryDate = today + 10)
        ))
        dao.getAllProducts().test {
            val list = awaitItem()
            assertEquals(3, list.size)
            assertTrue(list[0].expiryDate <= list[1].expiryDate)
            assertTrue(list[1].expiryDate <= list[2].expiryDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllProducts_flow_emits_on_insert() = runTest {
        dao.getAllProducts().test {
            awaitItem() // emissione iniziale (lista vuota)
            dao.insert(product(name = "Panna"))
            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals("Panna", updated.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllProductsSync_returns_all_products() = runTest {
        dao.insert(product(name = "X"))
        dao.insert(product(name = "Y"))
        assertEquals(2, dao.getAllProductsSync().size)
    }
}

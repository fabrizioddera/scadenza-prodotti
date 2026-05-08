package com.fabrizioddera.scadenzaprodotti.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import com.fabrizioddera.scadenzaprodotti.data.Product
import com.fabrizioddera.scadenzaprodotti.data.ProductRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProductViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val dispatcher = UnconfinedTestDispatcher()
    private val repo: ProductRepository = mockk()
    private lateinit var vm: ProductViewModel

    private fun product(
        id: Int = 1,
        name: String = "Latte",
        expiryDate: Long = LocalDate.now().plusDays(5).toEpochDay()
    ) = Product(id = id, name = name, expiryDate = expiryDate, imageUrl = null)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { repo.allProducts } returns flowOf(emptyList())
        val app = ApplicationProvider.getApplicationContext<Application>()
        vm = ProductViewModel(app, repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun products_livedata_emits_from_repo_flow() {
        val expected = listOf(product(name = "Latte"), product(id = 2, name = "Yogurt"))
        every { repo.allProducts } returns flowOf(expected)
        val app = ApplicationProvider.getApplicationContext<Application>()
        val localVm = ProductViewModel(app, repo)

        val observer = mockk<Observer<List<Product>>>(relaxed = true)
        localVm.products.observeForever(observer)

        verify { observer.onChanged(expected) }
        localVm.products.removeObserver(observer)
    }

    @Test
    fun insert_calls_repo_insert() = runTest {
        val p = product()
        coEvery { repo.insert(p) } just Runs
        vm.insert(p)
        coVerify { repo.insert(p) }
    }

    @Test
    fun update_calls_repo_update() = runTest {
        val p = product()
        coEvery { repo.update(p) } just Runs
        vm.update(p)
        coVerify { repo.update(p) }
    }

    @Test
    fun delete_calls_repo_delete() = runTest {
        val p = product()
        coEvery { repo.delete(p) } just Runs
        vm.delete(p)
        coVerify { repo.delete(p) }
    }

    @Test
    fun getProductById_returns_product() = runTest {
        val p = product(id = 42)
        coEvery { repo.getById(42) } returns p
        val result = vm.getProductById(42)
        assertEquals(p, result)
    }

    @Test
    fun getProductById_nonexistent_returns_null() = runTest {
        coEvery { repo.getById(999) } returns null
        assertNull(vm.getProductById(999))
    }

    @Test
    fun getAllProducts_returns_repo_list() = runTest {
        val list = listOf(product(id = 1), product(id = 2))
        coEvery { repo.getAllSync() } returns list
        val result = vm.getAllProducts()
        assertEquals(list, result)
    }

    @Test
    fun insertAll_calls_repo_insertAll() = runTest {
        val list = listOf(product(id = 1), product(id = 2))
        coEvery { repo.insertAll(list) } just Runs
        vm.insertAll(list)
        coVerify { repo.insertAll(list) }
    }
}

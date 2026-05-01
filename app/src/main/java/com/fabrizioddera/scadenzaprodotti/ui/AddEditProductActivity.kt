package com.fabrizioddera.scadenzaprodotti.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.fabrizioddera.scadenzaprodotti.data.Product
import com.fabrizioddera.scadenzaprodotti.databinding.ActivityAddEditProductBinding
import com.fabrizioddera.scadenzaprodotti.network.ProductLookupService
import com.fabrizioddera.scadenzaprodotti.viewmodel.ProductViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddEditProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditProductBinding
    private val viewModel: ProductViewModel by viewModels()
    private var selectedDate: LocalDate = LocalDate.now().plusMonths(1)
    private var editingProduct: Product? = null
    private var scannedBarcode: String? = null
    private var currentImageUrl: String? = null
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            scannedBarcode = result.contents
            binding.textBarcode.text = result.contents
            lookupProductOnline(result.contents)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityAddEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
        if (productId != -1) {
            title = "Modifica prodotto"
            lifecycleScope.launch {
                viewModel.getProductById(productId)?.let { product ->
                    editingProduct = product
                    populateFields(product)
                }
            }
        } else {
            title = "Aggiungi prodotto"
        }

        updateDateDisplay()

        binding.btnScanBarcode.setOnClickListener {
            barcodeLauncher.launch(ScanOptions().apply {
                setPrompt("Inquadra il barcode del prodotto")
                setBeepEnabled(true)
                setOrientationLocked(true)
            })
        }

        binding.btnPickDate.setOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { saveProduct() }
    }

    private fun lookupProductOnline(barcode: String) {
        lifecycleScope.launch {
            binding.btnScanBarcode.isEnabled = false
            binding.textBarcode.text = "$barcode (ricerca in corso…)"
            val product = ProductLookupService.lookupBarcode(barcode)
            binding.btnScanBarcode.isEnabled = true
            binding.textBarcode.text = barcode
            if (product != null) {
                if (binding.editName.text.isNullOrBlank()) {
                    binding.editName.setText(product.name)
                }
                loadAvatar(product.url)
                Toast.makeText(this@AddEditProductActivity, "Trovato: ${product.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@AddEditProductActivity, "Prodotto non trovato online", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateFields(product: Product) {
        binding.editName.setText(product.name)
        binding.editQuantity.setText(product.quantity.toString())
        binding.editNotes.setText(product.notes ?: "")
        binding.editDaysNotify.setText(product.daysBeforeNotify.toString())
        binding.editDaysAfterOpening.setText(product.daysUntilBadAfterOpening?.toString() ?: "")
        scannedBarcode = product.barcode
        binding.textBarcode.text = product.barcode ?: "Nessun barcode"
        selectedDate = product.localExpiryDate
        loadAvatar(product.imageUrl)
        updateDateDisplay()
    }

    private fun loadAvatar(url: String?) {
        currentImageUrl = url
        if (url != null) {
            binding.imageAvatar.visibility = View.VISIBLE
            binding.imageAvatar.load(url) {
                transformations(CircleCropTransformation())
            }
        } else {
            binding.imageAvatar.visibility = View.GONE
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate = LocalDate.of(year, month + 1, day)
                updateDateDisplay()
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).show()
    }

    private fun updateDateDisplay() {
        binding.textSelectedDate.text = selectedDate.format(dateFormatter)
    }

    private fun saveProduct() {
        val name = binding.editName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Inserisci il nome del prodotto", Toast.LENGTH_SHORT).show()
            return
        }
        val quantity = binding.editQuantity.text.toString().toIntOrNull() ?: 1
        val daysNotify = binding.editDaysNotify.text.toString().toIntOrNull() ?: 3
        val daysAfterOpening = binding.editDaysAfterOpening.text.toString().toIntOrNull()
        val notes = binding.editNotes.text.toString().trim().ifEmpty { null }
        val imageUrl = currentImageUrl

        val product = Product(
            id = editingProduct?.id ?: 0,
            name = name,
            barcode = scannedBarcode,
            expiryDate = selectedDate.toEpochDay(),
            quantity = quantity,
            notes = notes,
            daysBeforeNotify = daysNotify,
            imageUrl = imageUrl,
            openedDate = editingProduct?.openedDate,
            daysUntilBadAfterOpening = daysAfterOpening
        )
        if (editingProduct != null) viewModel.update(product) else viewModel.insert(product)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }
}

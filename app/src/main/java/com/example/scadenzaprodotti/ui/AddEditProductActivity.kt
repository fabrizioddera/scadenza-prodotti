package com.example.scadenzaprodotti.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.scadenzaprodotti.data.Product
import com.example.scadenzaprodotti.databinding.ActivityAddEditProductBinding
import com.example.scadenzaprodotti.network.ProductLookupService
import com.example.scadenzaprodotti.viewmodel.ProductViewModel
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
            val name = ProductLookupService.lookupBarcode(barcode)
            binding.btnScanBarcode.isEnabled = true
            binding.textBarcode.text = barcode
            if (name != null) {
                if (binding.editName.text.isNullOrBlank()) {
                    binding.editName.setText(name)
                }
                Toast.makeText(this@AddEditProductActivity, "Trovato: $name", Toast.LENGTH_SHORT).show()
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
        scannedBarcode = product.barcode
        binding.textBarcode.text = product.barcode ?: "Nessun barcode"
        selectedDate = product.localExpiryDate
        updateDateDisplay()
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
        val notes = binding.editNotes.text.toString().trim().ifEmpty { null }

        val product = Product(
            id = editingProduct?.id ?: 0,
            name = name,
            barcode = scannedBarcode,
            expiryDate = selectedDate.toEpochDay(),
            quantity = quantity,
            notes = notes,
            daysBeforeNotify = daysNotify
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

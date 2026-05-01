package com.fabrizioddera.scadenzaprodotti.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fabrizioddera.scadenzaprodotti.R
import com.fabrizioddera.scadenzaprodotti.data.BackupManager
import com.fabrizioddera.scadenzaprodotti.databinding.ActivityMainBinding
import com.fabrizioddera.scadenzaprodotti.viewmodel.ProductViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch {
            try {
                val products = viewModel.getAllProducts()
                val json = BackupManager.toJson(products)
                contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                Toast.makeText(
                    this@MainActivity,
                    "Backup salvato (${products.size} prodotti)",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (_: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Errore durante l'esportazione",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch {
            try {
                val json = contentResolver.openInputStream(uri)?.use { it.reader().readText() }
                    ?: return@launch
                val products = BackupManager.fromJson(json)
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Importa backup")
                    .setMessage("Trovati ${products.size} prodotti. Vuoi aggiungerli a quelli esistenti?")
                    .setPositiveButton("Aggiungi") { _, _ ->
                        viewModel.insertAll(products)
                        Toast.makeText(
                            this@MainActivity,
                            "${products.size} prodotti importati",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            } catch (_: Exception) {
                Toast.makeText(this@MainActivity, "File non valido o corrotto", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeProducts()

        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddEditProductActivity::class.java))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> {
                val fileName = "scadenze_${
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                }.json"
                exportLauncher.launch(fileName)
                true
            }

            R.id.action_import -> {
                importLauncher.launch(arrayOf("application/json", "text/plain"))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onItemClick = { product ->
                startActivity(
                    Intent(this, AddEditProductActivity::class.java).apply {
                        putExtra(AddEditProductActivity.EXTRA_PRODUCT_ID, product.id)
                    }
                )
            },
            onMarkOpened = { product ->
                val opened = product.copy(openedDate = LocalDate.now().toEpochDay())
                viewModel.update(opened)
                val msg = if (product.daysUntilBadAfterOpening != null) {
                    "${product.name} aperto · scade tra ${opened.daysUntilExpiry} giorni"
                } else {
                    "${product.name} segnato come aperto"
                }
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
                    .setAction("Annulla") { viewModel.update(product.copy(openedDate = null)) }
                    .show()
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                t: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val product = adapter.getProductAt(viewHolder.bindingAdapterPosition)
                viewModel.delete(product)
                Snackbar.make(binding.root, "${product.name} eliminato", Snackbar.LENGTH_LONG)
                    .setAction("Annulla") { viewModel.insert(product) }
                    .show()
            }
        }).attachToRecyclerView(binding.recyclerView)
    }

    private fun observeProducts() {
        viewModel.products.observe(this) { products ->
            adapter.submitList(products)
            binding.emptyView.visibility =
                if (products.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }
}

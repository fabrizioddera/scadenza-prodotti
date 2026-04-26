package com.example.scadenzaprodotti.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scadenzaprodotti.databinding.ActivityMainBinding
import com.example.scadenzaprodotti.viewmodel.ProductViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun setupRecyclerView() {
        adapter = ProductAdapter { product ->
            startActivity(
                Intent(this, AddEditProductActivity::class.java).apply {
                    putExtra(AddEditProductActivity.EXTRA_PRODUCT_ID, product.id)
                }
            )
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val product = adapter.getProductAt(viewHolder.adapterPosition)
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

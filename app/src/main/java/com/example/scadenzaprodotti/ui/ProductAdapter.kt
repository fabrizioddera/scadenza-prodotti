package com.example.scadenzaprodotti.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.scadenzaprodotti.data.Product
import com.example.scadenzaprodotti.databinding.ItemProductBinding
import java.time.format.DateTimeFormatter
import androidx.core.graphics.toColorInt

class ProductAdapter(
    private val onItemClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    inner class ViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.textName.text = product.name
            binding.textExpiry.text = "Scade: ${product.localExpiryDate.format(dateFormatter)}"
            binding.textQuantity.text = "Qtà: ${product.quantity}"

            val days = product.daysUntilExpiry
            binding.textDaysLeft.text = when {
                days < 0 -> "Scaduto ${-days} giorni fa"
                days == 0L -> "Scade oggi!"
                days == 1L -> "Scade domani"
                else -> "Scade tra $days giorni"
            }

            val bgColor = when {
                days < 0 -> "#FFCDD2".toColorInt()
                days <= 3 -> "#FFE0B2".toColorInt()
                else -> "#C8E6C9".toColorInt()
            }
            binding.root.setCardBackgroundColor(bgColor)
            binding.root.setOnClickListener { onItemClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    fun getProductAt(position: Int): Product = getItem(position)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(old: Product, new: Product) = old.id == new.id
            override fun areContentsTheSame(old: Product, new: Product) = old == new
        }
    }
}

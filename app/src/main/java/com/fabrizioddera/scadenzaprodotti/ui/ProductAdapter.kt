package com.fabrizioddera.scadenzaprodotti.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.fabrizioddera.scadenzaprodotti.R
import com.fabrizioddera.scadenzaprodotti.data.Product
import com.fabrizioddera.scadenzaprodotti.databinding.ItemProductBinding
import java.time.format.DateTimeFormatter

class ProductAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onMarkOpened: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    inner class ViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.textName.text = product.name
            binding.textExpiry.text = "Scade: ${product.effectiveExpiryDate.format(dateFormatter)}"
            binding.textQuantity.text = "Qtà: ${product.quantity}"

            val days = product.daysUntilExpiry
            binding.textDaysLeft.text = when {
                days < 0 -> "Scaduto ${-days} giorni fa"
                days == 0L -> "Scade oggi!"
                days == 1L -> "Scade domani"
                else -> "Scade tra $days giorni"
            }

            val ctx = binding.root.context
            val (bgColor, stripeColor, textColor) = when {
                days < 0 -> Triple(
                    ContextCompat.getColor(ctx, R.color.status_expired_bg),
                    ContextCompat.getColor(ctx, R.color.status_expired_stripe),
                    ContextCompat.getColor(ctx, R.color.status_expired_stripe)
                )
                days <= 3 -> Triple(
                    ContextCompat.getColor(ctx, R.color.status_warning_bg),
                    ContextCompat.getColor(ctx, R.color.status_warning_stripe),
                    ContextCompat.getColor(ctx, R.color.status_warning_stripe)
                )
                else -> Triple(
                    ContextCompat.getColor(ctx, R.color.status_ok_bg),
                    ContextCompat.getColor(ctx, R.color.status_ok_stripe),
                    ContextCompat.getColor(ctx, R.color.status_ok_stripe)
                )
            }
            binding.root.setCardBackgroundColor(bgColor)
            binding.statusStripe.setBackgroundColor(stripeColor)
            binding.textDaysLeft.setTextColor(textColor)

            if (product.isOpened) {
                val openedStr = product.openedLocalDate!!.format(dateFormatter)
                binding.textOpenedInfo.visibility = View.VISIBLE
                binding.textOpenedInfo.text = if (product.daysUntilBadAfterOpening != null) {
                    "Aperto il $openedStr · scade ${product.effectiveExpiryDate.format(dateFormatter)}"
                } else {
                    "Aperto il $openedStr"
                }
                binding.btnMarkOpened.visibility = View.GONE
            } else {
                binding.textOpenedInfo.visibility = View.GONE
                binding.btnMarkOpened.visibility = View.VISIBLE
                binding.btnMarkOpened.setOnClickListener { onMarkOpened(product) }
            }

            if (product.imageUrl != null) {
                binding.imageAvatar.visibility = View.VISIBLE
                binding.imageAvatar.load(product.imageUrl) {
                    transformations(CircleCropTransformation())
                }
            } else {
                binding.imageAvatar.visibility = View.GONE
            }

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

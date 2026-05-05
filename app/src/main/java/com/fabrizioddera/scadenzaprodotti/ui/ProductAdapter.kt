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
            val ctx = binding.root.context
            binding.textName.text = product.name
            binding.textExpiry.text = ctx.getString(R.string.product_expiry, product.effectiveExpiryDate.format(dateFormatter))
            binding.textQuantity.text = ctx.getString(R.string.product_quantity, product.quantity)

            val days = product.daysUntilExpiry
            binding.textDaysLeft.text = when {
                days < 0 -> ctx.getString(R.string.status_expired, (-days).toInt())
                days == 0L -> ctx.getString(R.string.status_expires_today)
                days == 1L -> ctx.getString(R.string.status_expires_tomorrow)
                else -> ctx.getString(R.string.status_expires_in, days.toInt())
            }

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
                    ctx.getString(R.string.product_opened_with_expiry, openedStr, product.effectiveExpiryDate.format(dateFormatter))
                } else {
                    ctx.getString(R.string.product_opened, openedStr)
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

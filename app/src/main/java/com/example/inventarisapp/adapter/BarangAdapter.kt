package com.example.inventarisapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.inventarisapp.databinding.ItemBarangBinding
import com.example.inventarisapp.entity.Barang

class BarangAdapter(
    private var items: List<Barang>,
    private val onTransaksi: (Barang) -> Unit,
    private val onEdit: (Barang) -> Unit,
    private val onDelete: (Barang) -> Unit
) : RecyclerView.Adapter<BarangAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemBarangBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Barang) {
            binding.tvNamaBarang.text = item.namaBarang
            binding.tvKategori.text = item.kategori
            binding.tvDeskripsi.text = item.deskripsi
            binding.tvStok.text = "Stok: ${item.stok}"

            val imageUrl = "http://192.168.18.111:8000/storage/images/${item.images}"
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(android.R.color.darker_gray)
                .into(binding.imgBarangPlaceholder)

            binding.btnTransaksi.setOnClickListener { onTransaksi(item) }
            binding.btnEdit.setOnClickListener { onEdit(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBarangBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<Barang>) {
        items = newList
        notifyDataSetChanged()
    }
}

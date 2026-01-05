package com.example.inventarisapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inventarisapp.R
import com.example.inventarisapp.entity.Transaksi
import android.transition.AutoTransition
import android.transition.TransitionManager

class TransaksiAdapter(
    private val list: List<Transaksi>
) : RecyclerView.Adapter<TransaksiAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama = view.findViewById<TextView>(R.id.tvNamaBarang)
        val tvJenis = view.findViewById<TextView>(R.id.tvJenis)
        val tvDetail = view.findViewById<TextView>(R.id.tvDetail)
        val tvTanggal = view.findViewById<TextView>(R.id.tvTanggal)
        val tvCatatan: TextView = itemView.findViewById(R.id.tvCatatan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaksi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val t = list[position]

        holder.tvNama.text = t.nama_barang
        holder.tvTanggal.text = t.tanggal
        holder.tvDetail.text = "Qty: ${t.qty} | Stok: ${t.stok_sebelum} → ${t.stok_sesudah}"

        // Jenis transaksi
        if (t.jenis_transaksi == "masuk") {
            holder.tvJenis.text = "MASUK"
            holder.tvJenis.setBackgroundResource(R.drawable.bg_badge_masuk)
        } else {
            holder.tvJenis.text = "KELUAR"
            holder.tvJenis.setBackgroundResource(R.drawable.bg_badge_keluar)
        }

        holder.tvCatatan.text = t.catatan ?: "Tidak ada catatan"
        holder.tvCatatan.visibility = View.GONE

        holder.itemView.setOnClickListener {
            val transition = AutoTransition()
            transition.duration = 200
            TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup, transition)

            holder.tvCatatan.visibility =
                if (holder.tvCatatan.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    override fun getItemCount() = list.size
}

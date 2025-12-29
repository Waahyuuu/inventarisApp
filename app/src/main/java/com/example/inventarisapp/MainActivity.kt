package com.example.inventarisapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventarisapp.adapter.BarangAdapter
import com.example.inventarisapp.databinding.ActivityMainBinding
import com.example.inventarisapp.entity.Barang
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private var listBarangOriginal: List<Barang> = ArrayList()
    private lateinit var adapter: BarangAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvBarang.layoutManager = LinearLayoutManager(this)

        adapter = BarangAdapter(emptyList(),
            onTransaksi = { openTransaksi(it) },
            onEdit = { editBarang(it) },
            onDelete = { showDeleteConfirmation(it) }
        )
        binding.rvBarang.adapter = adapter

        // Observasi data
        viewModel.listBarang.observe(this) { listData ->
            listBarangOriginal = listData
            adapter.updateList(listData)
            updateStats()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.getListBarang()

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddBarangActivity::class.java))
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterData(s.toString())
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.getListBarang()
    }

    private fun filterData(query: String) {
        val filtered = listBarangOriginal.filter {
            it.namaBarang.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
        }
        adapter.updateList(filtered)
    }

    private fun updateStats() {
        binding.tvTotalBarang.text = listBarangOriginal.size.toString()
        val stokRendah = listBarangOriginal.count { it.stok < 5 }
        binding.tvStokRendah.text = stokRendah.toString()
    }

    private fun showDeleteConfirmation(barang: Barang) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Hapus Barang")
            .setMessage("Yakin mau menghapus ${barang.namaBarang}?")
            .setPositiveButton("Hapus") { _, _ ->
                barang.id?.let { viewModel.deleteBarang(it) }
                Toast.makeText(this, "Menghapus...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun editBarang(barang: Barang) {
        val intent = Intent(this, AddBarangActivity::class.java)
        intent.putExtra("id", barang.id.toString())
        intent.putExtra("nama", barang.namaBarang)
        intent.putExtra("kategori", barang.kategori)
        intent.putExtra("stok", barang.stok)
        intent.putExtra("deskripsi", barang.deskripsi)
        intent.putExtra("images", barang.images)
        startActivity(intent)
    }

    private fun openTransaksi(barang: Barang) {
        val intent = Intent(this, TransaksiActivity::class.java)
        intent.putExtra("id", barang.id.toString())
        intent.putExtra("nama", barang.namaBarang)
        intent.putExtra("kategori", barang.kategori)
        intent.putExtra("stok", barang.stok)
        intent.putExtra("deskripsi", barang.deskripsi)
        intent.putExtra("images", barang.images)
        startActivity(intent)
    }

}

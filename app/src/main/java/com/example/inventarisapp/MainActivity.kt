package com.example.inventarisapp

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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
import com.example.inventarisapp.utils.NetworkUtils
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private var listBarangOriginal: List<Barang> = emptyList()
    private lateinit var adapter: BarangAdapter

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()
        observeNetworkState()
        setupAction()

        handleInternetState()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            viewModel.getListBarang() // 🔥 refresh stok
        }
    }

    override fun onResume() {
        super.onResume()
        handleInternetState()
    }

    private fun setupRecyclerView() {
        adapter = BarangAdapter(
            emptyList(),
            onTransaksi = { openTransaksi(it) },
            onEdit = { editBarang(it) },
            onDelete = { showDeleteConfirmation(it) }
        )

        binding.rvBarang.layoutManager = LinearLayoutManager(this)
        binding.rvBarang.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.listBarang.observe(this) { listData ->
            listBarangOriginal = listData
            adapter.updateList(listData)
            updateStats()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupAction() {
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

        binding.noInternetView.btnRetry.setOnClickListener {
            if (NetworkUtils.isOnline(this)) {
                handleInternetState()
            } else {
                showNoInternetAlert()
            }
        }
    }

    private fun handleInternetState() {
        val isOnline = NetworkUtils.isOnline(this)

        binding.layoutMain.visibility =
            if (isOnline) View.VISIBLE else View.GONE

        binding.noInternetView.root.visibility =
            if (isOnline) View.GONE else View.VISIBLE

        if (isOnline) {
            viewModel.getListBarang()
        }
    }

    private fun filterData(query: String) {
        val filtered = listBarangOriginal.filter {
            it.namaBarang.lowercase(Locale.getDefault())
                .contains(query.lowercase(Locale.getDefault()))
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
        intent.putExtra("images", barang.images)

        startActivityForResult(intent, 1001)
    }

    private fun observeNetworkState() {
        connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread { handleInternetState() }
            }

            override fun onLost(network: Network) {
                runOnUiThread { handleInternetState() }
            }
        }

        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun showNoInternetAlert() {
        Snackbar.make(
            binding.root,
            "Periksa lagi jaringan internet anda",
            Snackbar.LENGTH_SHORT
        ).show()
    }
}
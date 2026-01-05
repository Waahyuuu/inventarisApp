package com.example.inventarisapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventarisapp.adapter.TransaksiAdapter
import com.example.inventarisapp.api.ApiClient
import com.example.inventarisapp.databinding.ActivityRiwayatTransaksiBinding
import com.example.inventarisapp.entity.Transaksi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class RiwayatTransaksiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatTransaksiBinding
    private val listTransaksi = mutableListOf<Transaksi>()
    private lateinit var adapter: TransaksiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupAction()
        loadTransaksi()
    }

    private fun setupRecycler() {
        adapter = TransaksiAdapter(listTransaksi)
        binding.rvTransaksi.layoutManager = LinearLayoutManager(this)
        binding.rvTransaksi.adapter = adapter
    }

    private fun setupAction() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.etDateFrom.setOnClickListener {
            showDatePicker { date ->
                binding.etDateFrom.setText(date)
            }
        }

        binding.etDateTo.setOnClickListener {
            showDatePicker { date ->
                binding.etDateTo.setText(date)
            }
        }
    }

    private fun loadTransaksi() {
        ApiClient.dataBarang.getTransaksi().enqueue(object : Callback<List<Transaksi>> {
            override fun onResponse(
                call: Call<List<Transaksi>>,
                response: Response<List<Transaksi>>
            ) {
                if (response.isSuccessful) {
                    listTransaksi.clear()
                    listTransaksi.addAll(response.body() ?: emptyList())
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<Transaksi>>, t: Throwable) {
                Toast.makeText(this@RiwayatTransaksiActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDatePicker(onSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d ->
                onSelected(String.format("%02d-%02d-%04d", d, m + 1, y))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}

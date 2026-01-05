package com.example.inventarisapp

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.inventarisapp.api.ApiClient
import com.example.inventarisapp.databinding.ActivityTransaksiBinding
import com.example.inventarisapp.entity.TransaksiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransaksiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransaksiBinding

    private var idBarang: String = ""
    private var namaBarang: String = ""
    private var kategori: String = ""
    private var stokSaatIni: Int = 0
    private var imageUrl: String? = null

    private var isBarangMasuk = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ambilDataIntent()

        if (idBarang.isEmpty()) {
            Toast.makeText(this, "Data barang tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupListener()
        updateKalkulasi()
    }

    private fun ambilDataIntent() {
        idBarang = intent.getStringExtra("id") ?: ""
        namaBarang = intent.getStringExtra("nama") ?: ""
        kategori = intent.getStringExtra("kategori") ?: ""
        stokSaatIni = intent.getIntExtra("stok", 0)
        imageUrl = intent.getStringExtra("images")
    }

    private fun setupUI() {
        binding.tvNamaBarang.text = namaBarang
        binding.tvKategori.text = kategori
        binding.tvStokTersedia.text = "Stok tersedia: $stokSaatIni"
        binding.tvStokAwalRingkasan.text = stokSaatIni.toString()

        loadImage()
        setModeMasuk()
    }

    private fun setupListener() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnBatal.setOnClickListener { finish() }

        binding.btnMasuk.setOnClickListener {
            if (!isBarangMasuk) setModeMasuk()
        }

        binding.btnKeluar.setOnClickListener {
            if (isBarangMasuk) setModeKeluar()
        }

        binding.edtJumlah.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateKalkulasi()
            }
        })

        binding.btnSimpanTransaksi.setOnClickListener {
            val qty = binding.edtJumlah.text.toString().toIntOrNull() ?: 0

            if (qty <= 0) {
                Toast.makeText(this, "Jumlah tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isBarangMasuk && qty > stokSaatIni) {
                Toast.makeText(this, "Stok tidak mencukupi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            simpanTransaksi(qty)
        }
    }

    private fun setModeMasuk() {
        isBarangMasuk = true

        ubahBorder(binding.btnMasuk, "#059669", "#ECFDF5")
        ubahBorder(binding.btnKeluar, "#E5E7EB", "#FFFFFF")

        binding.tvLabelPerubahan.text = "Ditambah:"
        binding.tvAngkaPerubahan.setTextColor(Color.parseColor("#059669"))

        binding.btnSimpanTransaksi.backgroundTintList =
            ContextCompat.getColorStateList(this, android.R.color.holo_green_dark)

        updateKalkulasi()
    }

    private fun setModeKeluar() {
        isBarangMasuk = false

        ubahBorder(binding.btnKeluar, "#DC2626", "#FEF2F2")
        ubahBorder(binding.btnMasuk, "#E5E7EB", "#FFFFFF")

        binding.tvLabelPerubahan.text = "Dikurang:"
        binding.tvAngkaPerubahan.setTextColor(Color.parseColor("#DC2626"))

        binding.btnSimpanTransaksi.backgroundTintList =
            ContextCompat.getColorStateList(this, android.R.color.holo_red_dark)

        updateKalkulasi()
    }

    private fun ubahBorder(view: View, stroke: String, solid: String) {
        val drawable = GradientDrawable()
        drawable.cornerRadius = 20f
        drawable.setStroke(3, Color.parseColor(stroke))
        drawable.setColor(Color.parseColor(solid))
        view.background = drawable
    }

    private fun updateKalkulasi() {
        val qty = binding.edtJumlah.text.toString().toIntOrNull() ?: 0
        val hasil = if (isBarangMasuk) stokSaatIni + qty else stokSaatIni - qty

        binding.tvAngkaPerubahan.text =
            if (isBarangMasuk) "+ $qty" else "- $qty"

        binding.tvStokAkhir.text = hasil.toString()
        binding.tvStokAkhir.setTextColor(if (hasil < 0) Color.RED else Color.BLACK)
    }

    private fun simpanTransaksi(qty: Int) {
        ApiClient.dataBarang.simpanTransaksi(
            barangId = idBarang,
            qty = qty,
            jenis = if (isBarangMasuk) "masuk" else "keluar",
            catatan = binding.edtCatatan.text.toString().ifEmpty { null }
        ).enqueue(object : Callback<TransaksiResponse> {

            override fun onResponse(
                call: Call<TransaksiResponse>,
                response: Response<TransaksiResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@TransaksiActivity,
                        "Transaksi berhasil",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@TransaksiActivity,
                        "Gagal menyimpan transaksi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<TransaksiResponse>, t: Throwable) {
                Toast.makeText(this@TransaksiActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadImage() {
        if (imageUrl.isNullOrEmpty()) {
            binding.iconPlaceholder.visibility = View.VISIBLE
            return
        }

        Glide.with(this)
            .load("http://192.168.18.11:8000/storage/images/$imageUrl")
            .centerCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?, model: Any?, target: Target<Drawable>, isFirst: Boolean
                ): Boolean {
                    binding.iconPlaceholder.visibility = View.VISIBLE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable, model: Any, target: Target<Drawable>,
                    dataSource: DataSource, isFirst: Boolean
                ): Boolean {
                    binding.iconPlaceholder.visibility = View.GONE
                    return false
                }
            })
            .into(binding.imgBarangPlaceholder)
    }
}

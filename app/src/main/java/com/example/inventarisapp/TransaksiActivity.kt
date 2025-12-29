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
import com.example.inventarisapp.entity.Barang
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransaksiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransaksiBinding

    // Data Barang
    private var idBarang: String? = null
    private var namaBarang: String = ""
    private var kategori: String = ""
    private var deskripsi: String = ""
    private var stokSaatIni: Int = 0
    private var imageUrl: String? = null

    private var isBarangMasuk = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idBarang = intent.getStringExtra("id")
        namaBarang = intent.getStringExtra("nama") ?: ""
        kategori = intent.getStringExtra("kategori") ?: ""
        deskripsi = intent.getStringExtra("deskripsi") ?: ""
        stokSaatIni = intent.getIntExtra("stok", 0)
        imageUrl = intent.getStringExtra("images") // URL gambar dari intent

        setupUI()
        setupListeners()
        updateKalkulasi()
    }

    private fun setupUI() {
        binding.tvNamaBarang.text = namaBarang
        binding.tvKategori.text = kategori
        binding.tvStokTersedia.text = "Stok Tersedia: $stokSaatIni"
        binding.tvStokAwalRingkasan.text = "$stokSaatIni"

        loadImageBarang()

        setModeMasuk()
    }

    private fun setupListeners() {
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
            val inputStr = binding.edtJumlah.text.toString()
            if (inputStr.isEmpty()) return@setOnClickListener

            val inputJumlah = inputStr.toInt()
            var stokBaru = 0

            if (isBarangMasuk) {
                stokBaru = stokSaatIni + inputJumlah
            } else {
                stokBaru = stokSaatIni - inputJumlah
                if (stokBaru < 0) {
                    Toast.makeText(this, "Stok tidak cukup!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            simpanStokBaru(stokBaru)
        }
    }

    private fun setModeMasuk() {
        isBarangMasuk = true
        ubahBorder(binding.btnMasuk, "#059669", "#ECFDF5")
        binding.iconMasuk.setColorFilter(Color.parseColor("#059669"))
        binding.tvMasuk.setTextColor(Color.parseColor("#059669"))

        ubahBorder(binding.btnKeluar, "#E5E7EB", "#FFFFFF")
        binding.iconKeluar.setColorFilter(Color.parseColor("#6B7280"))
        binding.tvKeluar.setTextColor(Color.parseColor("#6B7280"))

        binding.tvLabelPerubahan.text = "Ditambah:"
        binding.tvAngkaPerubahan.setTextColor(Color.parseColor("#059669"))
        binding.btnSimpanTransaksi.backgroundTintList =
            ContextCompat.getColorStateList(this, android.R.color.holo_green_dark)
        binding.btnSimpanTransaksi.text = "Simpan Transaksi"

        updateKalkulasi()
    }

    private fun setModeKeluar() {
        isBarangMasuk = false
        ubahBorder(binding.btnKeluar, "#DC2626", "#FEF2F2")
        binding.iconKeluar.setColorFilter(Color.parseColor("#DC2626"))
        binding.tvKeluar.setTextColor(Color.parseColor("#DC2626"))

        ubahBorder(binding.btnMasuk, "#E5E7EB", "#FFFFFF")
        binding.iconMasuk.setColorFilter(Color.parseColor("#6B7280"))
        binding.tvMasuk.setTextColor(Color.parseColor("#6B7280"))

        binding.tvLabelPerubahan.text = "Dikurang:"
        binding.tvAngkaPerubahan.setTextColor(Color.parseColor("#DC2626"))
        binding.btnSimpanTransaksi.backgroundTintList =
            ContextCompat.getColorStateList(this, android.R.color.holo_red_dark)

        updateKalkulasi()
    }

    private fun ubahBorder(view: View, strokeColor: String, solidColor: String) {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = 20f
        drawable.setStroke(3, Color.parseColor(strokeColor))
        drawable.setColor(Color.parseColor(solidColor))
        view.background = drawable
    }

    private fun updateKalkulasi() {
        val inputStr = binding.edtJumlah.text.toString()
        val input = if (inputStr.isNotEmpty()) inputStr.toInt() else 0

        if (isBarangMasuk) {
            binding.tvAngkaPerubahan.text = "+ $input"
            binding.tvStokAkhir.text = "${stokSaatIni + input}"
            binding.tvStokAkhir.setTextColor(Color.BLACK)
        } else {
            binding.tvAngkaPerubahan.text = "- $input"
            val sisa = stokSaatIni - input
            binding.tvStokAkhir.text = "$sisa"
            if (sisa < 0) binding.tvStokAkhir.setTextColor(Color.RED)
            else binding.tvStokAkhir.setTextColor(Color.BLACK)
        }
    }

    private fun simpanStokBaru(stokBaru: Int) {
        if (idBarang == null) return

        val rbNama = namaBarang.toRequestBody("text/plain".toMediaType())
        val rbKategori = kategori.toRequestBody("text/plain".toMediaType())
        val rbStok = stokBaru.toString().toRequestBody("text/plain".toMediaType())
        val rbDeskripsi = deskripsi.toRequestBody("text/plain".toMediaType())

        val emptyImage: MultipartBody.Part? = null

        ApiClient.dataBarang.updateBarang(
            idBarang!!,
            rbNama,
            rbKategori,
            rbStok,
            rbDeskripsi,
            emptyImage
        ).enqueue(object : Callback<Barang> {
            override fun onResponse(call: Call<Barang>, response: Response<Barang>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@TransaksiActivity, "Transaksi berhasil!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@TransaksiActivity, "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Barang>, t: Throwable) {
                Toast.makeText(this@TransaksiActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadImageBarang() {
        if (imageUrl.isNullOrEmpty()) {
            binding.iconPlaceholder.visibility = View.VISIBLE
            binding.imgBarangPlaceholder.setImageDrawable(null)
            return
        }

        val fullImageUrl = "http://192.168.18.111:8000/storage/images/$imageUrl"

        Glide.with(this)
            .load(fullImageUrl)
            .centerCrop()
            .placeholder(android.R.color.darker_gray)
            .error(android.R.color.darker_gray)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.iconPlaceholder.visibility = View.VISIBLE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.iconPlaceholder.visibility = View.GONE
                    return false
                }
            })
            .into(binding.imgBarangPlaceholder)
    }

}

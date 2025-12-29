package com.example.inventarisapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.inventarisapp.api.ApiClient
import com.example.inventarisapp.databinding.ActivityAddBarangBinding
import com.example.inventarisapp.entity.Barang
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBarangBinding
    private var idBarang: String? = null
    private var selectedImageUri: Uri? = null
    private var listKategori: List<String> = emptyList()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                selectedImageUri = result.data?.data

                binding.imgPreview.visibility = View.VISIBLE
                binding.iconCamera.visibility = View.GONE
                binding.textTambahFoto.visibility = View.GONE

                Glide.with(this)
                    .load(selectedImageUri)
                    .into(binding.imgPreview)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ambilKategoriDariAPI()

        idBarang = intent.getStringExtra("id")

        if (idBarang != null) {
            binding.tvTitle.text = "Edit Barang"
            binding.btnSimpan.text = "Simpan"

            binding.edtNama.setText(intent.getStringExtra("nama"))
            binding.edtKategori.setText(intent.getStringExtra("kategori"))
            binding.edtStok.setText(intent.getIntExtra("stok", 0).toString())
            binding.edtDeskripsi.setText(intent.getStringExtra("deskripsi"))

            val imageUrl = intent.getStringExtra("images")

            if (!imageUrl.isNullOrEmpty()) {
                binding.imgPreview.visibility = View.VISIBLE
                binding.iconCamera.visibility = View.GONE
                binding.textTambahFoto.visibility = View.GONE

                Glide.with(this)
                    .load("http://192.168.18.111:8000/storage/images/$imageUrl")
                    .into(binding.imgPreview)
            }
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnBatal.setOnClickListener { finish() }

        binding.containerFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        binding.edtKategori.setOnClickListener { showKategoriDropdown() }
        binding.iconDropdownKategori.setOnClickListener { showKategoriDropdown() }

        binding.btnSimpan.setOnClickListener {
            val nama = binding.edtNama.text.toString()
            val kategori = binding.edtKategori.text.toString()
            val stokStr = binding.edtStok.text.toString()
            val deskripsi = binding.edtDeskripsi.text.toString()

            if (nama.isEmpty() || kategori.isEmpty() || stokStr.isEmpty()) {
                Toast.makeText(this, "Mohon isi semua data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (idBarang != null) {
                updateBarang(idBarang!!, nama, kategori, stokStr.toInt(), deskripsi)
            } else {
                simpanBarang(nama, kategori, stokStr.toInt(), deskripsi)
            }
        }
    }

    private fun ambilKategoriDariAPI() {
        ApiClient.dataBarang.getAllBarang().enqueue(object : Callback<List<Barang>> {
            override fun onResponse(
                call: Call<List<Barang>>,
                response: Response<List<Barang>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    listKategori = data.map { it.kategori }.distinct()
                }
            }

            override fun onFailure(call: Call<List<Barang>>, t: Throwable) {
            }
        })
    }

    private fun showKategoriDropdown() {
        val popup = PopupMenu(this, binding.edtKategori)
        popup.menu.clear()

        if (listKategori.isEmpty()) {
            popup.menu.add("Belum ada kategori").isEnabled = false
        } else {
            listKategori.forEach { popup.menu.add(it) }
        }

        popup.setOnMenuItemClickListener { item ->
            binding.edtKategori.setText(item.title)
            true
        }

        popup.show()
    }

    private fun simpanBarang(nama: String, kategori: String, stok: Int, deskripsi: String) {
        loading(true)

        val rbNama = nama.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbKategori = kategori.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbStok = stok.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val rbDeskripsi = deskripsi.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = prepareImage()

        ApiClient.dataBarang.addBarang(
            rbNama, rbKategori, rbStok, rbDeskripsi, imagePart
        ).enqueue(object : Callback<Barang> {
            override fun onResponse(call: Call<Barang>, response: Response<Barang>) {
                loading(false)
                if (response.isSuccessful) {
                    Toast.makeText(this@AddBarangActivity, "Berhasil tambah barang!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    showError("Gagal menambah barang")
                }
            }

            override fun onFailure(call: Call<Barang>, t: Throwable) {
                loading(false)
                showError(t.message)
            }
        })
    }

    private fun updateBarang(id: String, nama: String, kategori: String, stok: Int, deskripsi: String) {
        loading(true)

        val rbNama = nama.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbKategori = kategori.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbStok = stok.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val rbDeskripsi = deskripsi.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = prepareImage()

        ApiClient.dataBarang.updateBarang(
            id, rbNama, rbKategori, rbStok, rbDeskripsi, imagePart
        ).enqueue(object : Callback<Barang> {
            override fun onResponse(call: Call<Barang>, response: Response<Barang>) {
                loading(false)
                if (response.isSuccessful) {
                    Toast.makeText(this@AddBarangActivity, "Berhasil update barang!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    showError("Gagal update barang")
                }
            }

            override fun onFailure(call: Call<Barang>, t: Throwable) {
                loading(false)
                showError(t.message)
            }
        })

    }

    private fun prepareImage(): MultipartBody.Part? {
        return selectedImageUri?.let { uri ->
            val file = uriToFile(uri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("images", file.name, requestFile)
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)!!
        val tempFile = File(cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")
        val outputStream = tempFile.outputStream()

        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()

        return tempFile
    }

    private fun loading(state: Boolean) {
        binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE
        binding.btnSimpan.isEnabled = !state
    }

    private fun showError(msg: String?) {
        Toast.makeText(this, "Error: $msg", Toast.LENGTH_SHORT).show()
    }
}

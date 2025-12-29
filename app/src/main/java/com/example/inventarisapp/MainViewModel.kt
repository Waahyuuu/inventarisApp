package com.example.inventarisapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.inventarisapp.api.ApiClient
import com.example.inventarisapp.entity.Barang
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {

    private val _listBarang = MutableLiveData<List<Barang>>()
    val listBarang: LiveData<List<Barang>> = _listBarang

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun getListBarang() {
        _isLoading.value = true

        ApiClient.dataBarang.getAllBarang()
            .enqueue(object : Callback<List<Barang>> {
                override fun onResponse(
                    call: Call<List<Barang>>,
                    response: Response<List<Barang>>
                ) {
                    _isLoading.value = false

                    if (response.isSuccessful) {
                        _listBarang.value = response.body() ?: emptyList()
                    } else {
                        _errorMessage.value = "Gagal memuat data"
                    }
                }

                override fun onFailure(call: Call<List<Barang>>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = t.message ?: "Kesalahan tidak diketahui"
                }
            })
    }

    fun deleteBarang(id: Int) {
        _isLoading.value = true

        ApiClient.dataBarang.deleteBarang(id)
            .enqueue(object : Callback<Void> {
                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    _isLoading.value = false

                    if (response.isSuccessful) {
                        getListBarang()
                    } else {
                        _errorMessage.value = "Gagal menghapus data"
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = t.message ?: "Kesalahan tidak diketahui"
                }
            })
    }
}

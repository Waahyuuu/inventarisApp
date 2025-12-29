package com.example.inventarisapp.api

import com.example.inventarisapp.entity.Barang
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("barang")
    fun getAllBarang(): Call<List<Barang>>

    @Multipart
    @POST("barang")
    fun addBarang(
        @Part("namaBarang") namaBarang: RequestBody,
        @Part("kategori") kategori: RequestBody,
        @Part("stok") stok: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part images: MultipartBody.Part?
    ): Call<Barang>

    @Multipart
    @PUT("barang/{id}")
    fun updateBarang(
        @Path("id") id: String,
        @Part("namaBarang") namaBarang: RequestBody,
        @Part("kategori") kategori: RequestBody,
        @Part("stok") stok: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part images: MultipartBody.Part?
    ): Call<Barang>

    @DELETE("barang/{id}")
    fun deleteBarang(
        @Path("id") id: Int
    ): Call<Void>

    @GET("barang/{id}")
    fun getBarangById(
        @Path("id") id: String
    ): Call<Barang>
}
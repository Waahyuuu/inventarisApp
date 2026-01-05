package com.example.inventarisapp.api

import com.example.inventarisapp.entity.Barang
import com.example.inventarisapp.entity.HasTransaksiResponse
import com.example.inventarisapp.entity.Transaksi
import com.example.inventarisapp.entity.TransaksiResponse
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
        @Part("stok") stok: RequestBody?,
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

    @GET("barang/{id}/has-transaksi")
    fun hasTransaksi(
        @Path("id") id: String
    ): Call<HasTransaksiResponse>

    @FormUrlEncoded
    @POST("transaksi")
    fun simpanTransaksi(
        @Field("barang_id") barangId: String,
        @Field("qty") qty: Int,
        @Field("jenis_transaksi") jenis: String,
        @Field("catatan") catatan: String?
    ): Call<TransaksiResponse>

    @GET("transaksi")
    fun getTransaksi(): Call<List<Transaksi>>
}
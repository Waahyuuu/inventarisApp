package com.example.inventarisapp.entity

data class Barang (
    val id: Int?,
    val namaBarang: String,
    val kategori: String,
    val stok: Int,
    val deskripsi: String,
    val images: String?
)

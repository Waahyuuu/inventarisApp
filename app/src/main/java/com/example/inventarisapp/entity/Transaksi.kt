package com.example.inventarisapp.entity

data class Transaksi(
    val id: Int,
    val nama_barang: String,
    val qty: Int,
    val jenis_transaksi: String,
    val stok_sebelum: Int,
    val stok_sesudah: Int,
    val catatan: String?,
    val tanggal: String
)

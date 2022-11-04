package com.app.pengaduanjaringan.pengaduan

data class PengaduanModel(
    var id : String ?=null, var idUser :String?=null, var tanggal :Long?=0,
    var judul :String?=null, var isi:String?=null, var alamat:String?=null, var status :Int?=0,
    var isRead :Boolean=false)

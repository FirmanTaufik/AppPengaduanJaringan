package com.app.pengaduanjaringan.user

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class UserModel(val nama :String?="", val email:String?="",
                     val username :String?="", val password:String?="",
                     val role :Int?=1, var id :String?=null,
                     var approved :Boolean?=true)

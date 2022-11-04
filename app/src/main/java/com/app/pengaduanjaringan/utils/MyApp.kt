package com.app.pengaduanjaringan.utils

import android.app.Application
import com.androidisland.vita.startVita

class MyApp : Application (){
    override fun onCreate() {
        super.onCreate()
        startVita()
    }

}
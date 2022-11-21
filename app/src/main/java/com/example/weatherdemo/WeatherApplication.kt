package com.example.weatherdemo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.InputStreamReader

class WeatherApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var noConstTOKEN: String
        const val TOKEN = "qRAzNzgluiHbsTnJ"

//        var ad = BufferedReader(InputStreamReader(context.assets.open("token"))).readText()
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
//        context = baseContext

        noConstTOKEN = BufferedReader(InputStreamReader(assets.open("token"))).readText()
    }
}
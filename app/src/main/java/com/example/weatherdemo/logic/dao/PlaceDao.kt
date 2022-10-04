package com.example.weatherdemo.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.example.weatherdemo.WeatherApplication
import com.example.weatherdemo.logic.model.Place
import com.google.gson.Gson

object PlaceDao {
    private fun sharedPreferences() =
        WeatherApplication.context.getSharedPreferences("weather_app", Context.MODE_PRIVATE)

    fun savePlace(place: Place) {
        sharedPreferences().edit {
            putString("place", Gson().toJson(place))
        }
    }

    fun getPlace(): Place {
        val placeJson = sharedPreferences().getString("place", "")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    fun isPlaceSaved() = sharedPreferences().contains("place")
}
package com.example.weatherdemo.logic.model

import com.google.gson.annotations.SerializedName

data class RealtimeResult(val status: String, val places: List<Place>)

data class Place(val name: String,
                 val location: Location,
                 @SerializedName("formatted_address")
                 val address: String)

data class Location(val lng: String, val lat: String)
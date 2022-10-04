package com.example.weatherdemo.logic.model

class PlaceResponse(
    val status: String,
    val query: String,
    val places: List<Place>
)
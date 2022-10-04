package com.example.weatherdemo.logic.model

import retrofit2.http.POST
import java.net.HttpURLConnection
import java.net.URL

data class Weather(val realtime: RealtimeResponse.Realtime, val daily: DailyResponse.Daily)
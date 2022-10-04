package com.example.weatherdemo.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// WHOLE APP's Retrofit object are united construct here
object WeatherAppNetwork {
    // use BASE RETROFIT CONSTRUCTOR to create Retrofit object
    val placeService: PlaceService = ServiceCreator.create()
    val weatherService: WeatherService = ServiceCreator.create()

    // network request execute here
    suspend fun searchPlaces(query: String) = placeService.searchPlace(query).await()
    suspend fun getDailyWeather(lng: String, lat: String) = weatherService.getDailyWeather(lng, lat).await()
    suspend fun getRealtimeWeather(lng: String, lat: String) = weatherService.getRealtimeWeather(lng, lat).await()

    // Callback here(use coroutine 'suspend function')
    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(RuntimeException("response body is null"))
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}
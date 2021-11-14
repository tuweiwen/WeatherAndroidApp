package com.sunnyweather.android.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


// 编写通用的网络请求单例对象(对网络请求API进行封装)
object SunnyWeatherNetwork {

    // 创建了一个(对地点搜索的)接口动态代理对象
    // 有了动态代理对象后，可以随意调用接口中定义的所有方法，retrofit会自动执行具体的处理
    private val placeService = ServiceCreator.create(PlaceService::class.java)

    // 声明为挂起函数，可以在挂起函数之间相互调用
    // 此处简化了retrofit回调(声明为挂起函数，见上)，使用协程
    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()

    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object: Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(
                        RuntimeException("response body is null"))
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })

        }
    }
}
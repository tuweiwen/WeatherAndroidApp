package com.sunnyweather.android.logic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 单例类
object ServiceCreator {

    // 接口基地址
    private const val BASE_URL = "https://api.caiyunapp.com/"

    // 创建了retrofit的对象
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
            // 指定解析json所使用的转换库
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 使用泛型，可以在创建对象时选择需要的请求对象类
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    // 泛型实化
    inline fun <reified T> create(): T = create(T::class.java)
}
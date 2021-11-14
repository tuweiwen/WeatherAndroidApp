package com.sunnyweather.android.logic

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers

// 仓库层，判断调用方请求的数据应该是从本地数据源中获取还是从网络数据源中获取
// 并将获得的数据返回给调用方，类似于数据获取和缓存的中间层
// (此处使用liveData，作为仓库层的统一封装入口，在它的代码块中提供了挂起函数的上下文)
object Repository {
    // TODO: 此处和书上不同，我手动增加了返回值类型
    fun <T> searchPlaces(query: String): LiveData<Result<T>> = liveData(Dispatchers.IO) {
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok") {
                val places = placeResponse.places
                Result.success(places)
            } else {
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // 将包装的结果发射出去(类似于调用setValue方法通知数据变化)
        emit(result as Result<T>)
    }
}
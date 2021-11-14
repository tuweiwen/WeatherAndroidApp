package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

// 以下数据类均是根据接口的返回json数据进行定义的

// 存储地区查找结果
data class PlaceResponse (val status: String, val places: List<Place>)

// 定义Place
data class Place (val name: String,
                  val location: Location,
                  @SerializedName("formatted_address")
                  val address: String)

// 定义Location
data class Location (val lng: String, val lat: String)

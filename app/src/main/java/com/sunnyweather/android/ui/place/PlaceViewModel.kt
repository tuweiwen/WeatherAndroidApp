package com.sunnyweather.android.ui.place

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

class PlaceViewModel: ViewModel() {
    private val searchLiveData = MutableLiveData<String>()

    val placeList = ArrayList<Place>()

    // TODO: 此处和书上不同，我手动增加了返回值类型
    val placeLiveData: LiveData<Result<List<Place>>> = Transformations.switchMap(searchLiveData) { query ->
        Repository.searchPlaces(query)
    }

    fun searchPlace(query: String) {
        searchLiveData.value = query
    }
}
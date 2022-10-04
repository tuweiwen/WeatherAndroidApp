package com.example.weatherdemo.ui.weather

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.weatherdemo.R
import com.example.weatherdemo.databinding.ActivityWeatherBinding
import com.example.weatherdemo.logic.model.Weather
import com.example.weatherdemo.logic.model.getSky
import com.example.weatherdemo.ui.AboutActivity
import com.example.weatherdemo.ui.PlaceActivity
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {
    val viewModel by lazy { ViewModelProviders.of(this).get(WeatherViewModel::class.java) }
    lateinit var binding: ActivityWeatherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set transparent statues bar
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT

        binding = DataBindingUtil.setContentView(this, R.layout.activity_weather)
        setContentView(binding.root)

        // get view here
        val navBtn: Button = findViewById(R.id.navBtn)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.swipeRefreshLayout.setColorSchemeColors(getColor(R.color.blue_500))
        }

        // set livedata observe here
        viewModel.weatherLiveData.observe(this) { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "can not get weather info", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }

        // set listener here
        navBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshWeather()
        }
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(
                    drawerView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                }
                R.id.nav_location -> {
                    startActivity(Intent(this, PlaceActivity::class.java))
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val lngFromIntent = intent.getStringExtra("location_lng")
        val latFromIntent = intent.getStringExtra("location_lat")
        val placeNameFromIntent = intent.getStringExtra("place_name")
        if (lngFromIntent != null && latFromIntent != null && placeNameFromIntent != null) {
            viewModel.locationLng = lngFromIntent
            viewModel.locationLat = latFromIntent
            viewModel.placeName = placeNameFromIntent
        }
        refreshWeather()
    }

    private fun showWeatherInfo(weather: Weather) {
        val placeName: TextView = findViewById(R.id.placeName)
        val currentTemp: TextView = findViewById(R.id.currentTemp)
        val currentSky: TextView = findViewById(R.id.currentSky)
        val currentAQI: TextView = findViewById(R.id.currentAQI)
        val nowLayout: RelativeLayout = findViewById(R.id.nowLayout)
        val forecastLayout: LinearLayout = findViewById(R.id.forecastLayout)
        val coldRiskText: TextView = findViewById(R.id.coldRiskText)
        val dressingText: TextView = findViewById(R.id.dressintText)
        val ultravioletText: TextView = findViewById(R.id.ultravioletText)
        val carWashingText: TextView = findViewById(R.id.carWashingText)
        val weatherLayout: ScrollView = findViewById(R.id.weatherLayout)

        val realtime = weather.realtime
        val daily = weather.daily
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        val currentPM25Text = "air quality ${realtime.airQuality.aqi.chn.toInt()}"

        placeName.text = viewModel.placeName
        currentTemp.text = currentTempText
        currentSky.text = getSky(realtime.skycon).info
        currentAQI.text = currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view =
                LayoutInflater.from(this).inflate(R.layout.item_forecast, forecastLayout, false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sky = getSky(skycon.value)
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"

            dateInfo.text = simpleDateFormat.format(skycon.date)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            temperatureInfo.text = tempText

            forecastLayout.addView(view)
        }
        val lifeIndex = daily.lifeIndex
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc

//        weatherLayout.visibility = View.VISIBLE
    }

    private fun refreshWeather() {
        val swipeRefresh: SwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        swipeRefresh.isRefreshing = true
    }
}
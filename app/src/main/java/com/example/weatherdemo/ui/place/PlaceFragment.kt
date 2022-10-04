package com.example.weatherdemo.ui.place

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherdemo.MainActivity
import com.example.weatherdemo.R
import com.example.weatherdemo.logic.model.Place
import com.example.weatherdemo.ui.weather.WeatherActivity

class PlaceFragment : Fragment() {
    val viewModel by lazy { ViewModelProviders.of(this).get(PlaceViewModel::class.java) }
    lateinit var permissionRequestLaunch: ActivityResultLauncher<String>

    private lateinit var adapter: PlaceRvAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // register for permission request & set callback
        permissionRequestLaunch =
            registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    getLocationUtils()
                } else {
                    doPermissionDenied()
                }
            }

        return inflater.inflate(R.layout.fragment_place, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // determine whether get place already
        if (activity is MainActivity && viewModel.isPlaceSaved()) {
            val place = viewModel.getSavedPlace()
            val intent = Intent(context, WeatherActivity::class.java).apply {
                putExtra("location_lng", place.location.lng)
                putExtra("location_lat", place.location.lat)
                putExtra("place_name", place.name)
            }
            startActivity(intent)
            activity?.finish()
            return
        }

        val view: View = requireView()
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val bgImageView: ImageView = view.findViewById(R.id.bgImageView)
        val searchPlaceEdit: EditText = view.findViewById(R.id.searchPlaceEdit)
        val getPositionBtn: Button = view.findViewById(R.id.get_position_btn)

        getPositionBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //doGetPosition()
                getLocationUtils()
            } else {
                permissionRequestLaunch.launch("android.permission.ACCESS_COARSE_LOCATION")
            }
        }

        val layoutManager = LinearLayoutManager(activity)
        adapter = PlaceRvAdapter(this, viewModel.placeList)
        // set layoutManager&adapter for recyclerView here
        recyclerView.let {
            it.layoutManager = layoutManager
            it.adapter = adapter
        }
        searchPlaceEdit.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.isNotEmpty()) {
                viewModel.searchPlaces(content)
                getPositionBtn.visibility = View.GONE
            } else {
                recyclerView.visibility = View.GONE
                bgImageView.visibility = View.VISIBLE
                viewModel.placeList.clear()
                adapter.notifyDataSetChanged()
            }
            viewModel.placeLiveData.observe(viewLifecycleOwner) { result ->
                val places = result.getOrNull()
                if (places != null) {
                    recyclerView.visibility = View.VISIBLE
                    bgImageView.visibility = View.GONE
                    viewModel.placeList.clear()
                    viewModel.placeList.addAll(places)
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(activity, "can not find any result", Toast.LENGTH_SHORT).show()
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }
    }

    private fun getLocationUtils() {
        var location: Location?
        val locationMng: LocationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers: List<String> = locationMng.getProviders(true)
        val locationListener = LocationListener {
            location = it
        }

        // get PROVIDER
        val locationProvider: String = if (providers.contains(LocationManager.GPS_PROVIDER)) {
            LocationManager.GPS_PROVIDER
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            LocationManager.NETWORK_PROVIDER
        } else {
            Toast.makeText(
                requireContext(),
                "can't get position, please turn on GPS in system settings",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            // STEP1: quire position first time(last location)
            location = locationMng.getLastKnownLocation(locationProvider)
            // STEP2: quire position second time(request location forwardly)
            if (location == null) {
                locationMng.requestLocationUpdates(
                    locationProvider,
                    3000L,
                    0.0001.toFloat(),
                    locationListener
                )
            }
            // STEP3: jump to weather details page
            if (location != null) {
                val place = Place(
                    "current location",
                    com.example.weatherdemo.logic.model.Location(
                        location!!.longitude.toString(),
                        location!!.latitude.toString()
                    ),
                    "current location"
                )
                val intent = Intent(context, WeatherActivity::class.java).apply {
                    putExtra("location_lng", place.location.lng)
                    putExtra("location_lat", place.location.lat)
                    putExtra("place_name", "current location")
                }
                startActivity(intent)
                activity?.finish()
                viewModel.savePlace(place)
            } else {
                Toast.makeText(
                    context,
                    "can't get location right now, please choose location manually",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            return
        }
    }

    private fun doPermissionDenied() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            Toast.makeText(
                context,
                "permission denied!(still can get permission)",
                Toast.LENGTH_SHORT
            ).show()
            permissionRequestLaunch.launch("android.permission.ACCESS_COARSE_LOCATION")
        } else
            Toast.makeText(context, "permission denied!(go to settings)", Toast.LENGTH_SHORT).show()
    }
}
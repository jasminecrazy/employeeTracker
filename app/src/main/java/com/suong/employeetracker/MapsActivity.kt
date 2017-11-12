package com.suong.employeetracker

import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.nbhung.testcallapi.DateOfDate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.suong.model.SharedPreferencesManager
import com.suong.model.sendEmployeess
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MapsActivity : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    val LOCATION_UPDATE_MIN_DISTANCE: Float = 500f
    val LOCATION_UPDATE_MIN_TIME: Long = 60000*30
    private var location: Location? = null
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var dialog: ProgressDialog? = null
    val IEmployee by lazy {
        com.suong.Api.ApiApp.create()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = layoutInflater.inflate(R.layout.activity_maps, container, false)
        locationManager = activity.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)
        dialog = ProgressDialog(activity)
        dialog!!.setMessage("Please wait")
        dialog!!.setTitle("Loading")
        dialog!!.setCancelable(false)
        dialog!!.show()
        return view
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.clear()

        mMap.isMyLocationEnabled = true
        if (Utils.isNetWorkConnnected(activity)) {
            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return
            }
            location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (location != null)
                eventUpdateLocation()
        } else {
            if (checkGps()) {
                location = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null)
                    eventUpdateLocation()

            }
        }

     /*   if (location == null) Toast.makeText(activity, "Please enable GPS", Toast.LENGTH_LONG).show()
        eventUpdateLocation()*/

        if (location == null) Toast.makeText(activity, "Please enable GPS", Toast.LENGTH_SHORT).show()

    }

    fun checkGps(): Boolean {
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onResume() {
        super.onResume()
        if (dialog!!.isShowing){
            dialog!!.dismiss()
        }
    }
    fun updateLocation(locationMap: Location) {
        mMap.clear()
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationMap.latitude, locationMap.longitude), 15f))
        val latlng: LatLng = LatLng(locationMap.latitude, locationMap.longitude)
      //  Log.d("location", latlng.toString())
       // Log.e("addddđ", Utils.convertAddr(latlng, activity))
        mMap.addMarker(MarkerOptions().title(Utils.convertAddr(latlng, activity)).position(LatLng(locationMap.latitude, locationMap.longitude))).showInfoWindow()
         sendLocation()

    }
    fun sendLocation() {
        var myAdd: String = Utils.convertAddr(LatLng(location!!.latitude, location!!.longitude), activity)
        Log.e("address", myAdd)
        val response = IEmployee
        val id:Int = SharedPreferencesManager.getIdUser(activity)!!.toInt()
        val userLogin = com.suong.model.sendLocation(sendEmployeess(id), location!!.longitude, location!!.latitude, myAdd, DateOfDate.getTimeGloba(), DateOfDate.getDay(), "")
        response.sendLocation(userLogin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Toast.makeText(activity, "mapsacitivity send success", Toast.LENGTH_SHORT).show()
                    Log.e("send mapsacitivity", "success")


                }, { error ->
                    Log.e("error mapsacitivity", error.message)
                    Toast.makeText(activity, " mapsacitivity send Failed", Toast.LENGTH_SHORT).show()
                })
        //   refreshCamera()
    }


    fun eventUpdateLocation() {
        locationListener = object : LocationListener {
            override fun onLocationChanged(p0: Location?) {
                if (p0 != null) {
                    updateLocation(p0)
                }

            }

            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                eventUpdateLocation()
            }

            override fun onProviderEnabled(p0: String?) {
            }

            override fun onProviderDisabled(p0: String?) {
            }

        }
        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, locationListener)

    }

    override fun onPause() {
        super.onPause()
        if (locationListener != null) locationManager!!.removeUpdates(locationListener)

    }

}

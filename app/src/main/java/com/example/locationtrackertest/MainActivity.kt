package com.example.locationtrackertest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.lang.Exception


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private var map: GoogleMap? = null
    var messenger: Messenger = Messenger(IncomingHandler())
    val locationList = arrayListOf<LatLng>()
    var isFirstTime = true
    var start = true
    lateinit var locationMarker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
            requestPermissions()
        else {
            if (start) start_stop.text = "Start"
            else start_stop.text = "Stop"
            start_stop.setOnClickListener {
                if (start) {
                    start = false
                    start_stop.text = "Stop"
                    (supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?)!!.getMapAsync { googleMap ->
                        map = googleMap
                        try {
                            startService(Intent(this, LocationTrackService::class.java).also {
                                it.action = Constant.START_RESUME
                                it.putExtra("messenger", messenger)
                            })
                        } catch (ex: Exception) {
                            Log.e("Exception", ex.toString())
                        }
                    }
                }
                else {
                    startService(Intent(this, LocationTrackService::class.java).also {
                        it.action = Constant.STOP
                        it.putExtra("messenger", messenger)
                    })
                    stopService(Intent(this, LocationTrackService::class.java))
                    start = true
                    start_stop.text = "Start"
                }
            }
        }


    }


    private fun addLatestPolyline() {
        if (locationList.isNotEmpty() && locationList.size > 1) {
            val preLastLatLng = locationList[locationList.size - 2]
            val lastLatLng = locationList.last()
            val polylineOptions = PolylineOptions()
                .color(Constant.POLYLINE_COLOR)
                .width(Constant.POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun moveCameraToUser() {

        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                locationList.last(),
                Constant.MAP_ZOOM
            )
        )

    }


    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            val bundle: Bundle = msg.getData()
            bundle.getParcelable<Location>("location_coord")
                ?.let { addPoints(LatLng(it.latitude, it.longitude)) }
        }
    }

    fun addPoints(point: LatLng) {
        Log.e("lat long", "${point.latitude}  ${point.longitude}")
        locationList.add(point)
        moveCameraToUser()
        addLatestPolyline()

        if (!isFirstTime) locationMarker.remove()

        val markerOptions = MarkerOptions()
        markerOptions.position(point)
        markerOptions.title("Current Position")
        markerOptions.icon(
            BitmapDescriptorFactory.defaultMarker(
                Constant.MARKER_COLOR
            )
        )
        locationMarker = map?.addMarker(markerOptions)!!
        isFirstTime = false
    }


    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(this)) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                100,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                100,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}
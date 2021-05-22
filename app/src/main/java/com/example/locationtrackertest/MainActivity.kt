package com.example.locationtrackertest

import android.Manifest
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.lang.Exception


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private var map: GoogleMap? = null
    var messenger: Messenger = Messenger(IncomingHandler())
    val locationList = arrayListOf<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()

        // Try to obtain the map from the SupportMapFragment.
        (supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?)!!.getMapAsync { googleMap ->
            map = googleMap
            try {
                startService(Intent(this,LocationTrackService::class.java).also {
                    it.action = Constant.START_RESUME
                    it.putExtra("messenger",messenger)
                })
            }catch (ex: Exception) {
                Log.e("Exception", ex.toString() )
            }
        }



    }



    internal class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            val bundle: Bundle = msg.getData()
            Log.e("test", bundle.getDouble("latitude").toString());

        }
    }

    fun addPoints(point: LatLng) {

    }

    private fun requestPermissions() {
        if(TrackingUtility.hasLocationPermissions(this)) {
            return
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
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
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
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
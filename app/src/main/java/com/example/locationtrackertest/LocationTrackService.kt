package com.example.locationtrackertest

import android.Manifest
import android.R.attr.data
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.locationtrackertest.Constant.NOTIFICATION_ID
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult


class LocationTrackService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
       return binder
    }

    var isFirstTime = true
    var isTrackingLocation = false
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val binder: IBinder = LocalBinder()
    lateinit var serviceMessenger: Messenger

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        Log.e("service", "on create!!!" )
        updateLocationTracking(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            Log.e("service", "on start!!!" )
            serviceMessenger = intent.getParcelableExtra<Messenger>("messenger")!!
            when(it.action) {
                Constant.START_RESUME -> {
                    if(isFirstTime) {
                        isFirstTime = false
                        statService()
                    }
                    else
                        statService()
                }

                Constant.PAUSE -> {
                    pauseService()
                }

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun statService() {

        isTrackingLocation = true

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, Constant.NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Location Tracker")
                .setContentText("Tracking your location!!")
                .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking) {
            if(TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = Constant.LOCATION_UPDATE_INTERVAL
                    fastestInterval = Constant.FASTEST_LOCATION_INTERVAL
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    return
                fusedLocationProviderClient.requestLocationUpdates(
                        request,
                        locationCallback,
                        Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
                Constant.NOTIFICATION_CHANNEL_ID,
                Constant.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun pauseService() {
        isTrackingLocation = false
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if(isTrackingLocation) {
                result?.locations?.let { locations ->
                    for(location in locations) {
                        val msg: Message = Message.obtain()
                        val bundle = Bundle()
                        bundle.putDouble("latitude", location.latitude)
                        bundle.putDouble("longitude", location.longitude)
                        msg.setData(bundle)
                        try {
                            serviceMessenger.send(msg)
                        } catch (e: RemoteException) {
                            Log.e("exception", e.toString())
                        }
                    }
                }
            }
        }
    }

    internal class LocalBinder : Binder() {
        fun getService(): LocalBinder {
            return this
        }
    }

}
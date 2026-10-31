package com.example.emergency

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

private const val TAG = "EmergencyLocationService"

data class EmergencyLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    val mapsUrl: String
        get() = "https://maps.google.com/?q=$latitude,$longitude"

    fun formattedCoordinates(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lngDir = if (longitude >= 0) "E" else "W"
        return "%.5f° %s, %.5f° %s (±%.0fm)".format(
            Math.abs(latitude), latDir,
            Math.abs(longitude), lngDir,
            accuracyMeters
        )
    }
}

class EmergencyLocationService(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentEmergencyLocation(): EmergencyLocation? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted for emergency fetch")
            return@withContext null
        }

        // Try getting high-accuracy location within a 15-second timeout window
        val location = withTimeoutOrNull(15000L) {
            getFusedHighAccuracyLocation() ?: getFallbackSystemLocation()
        }

        location?.let {
            EmergencyLocation(
                latitude = it.latitude,
                longitude = it.longitude,
                accuracyMeters = if (it.hasAccuracy()) it.accuracy else 25f,
                timestamp = it.time.takeIf { t -> t > 0 } ?: System.currentTimeMillis()
            )
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getFusedHighAccuracyLocation(): Location? = suspendCancellableCoroutine { cont ->
        val cts = CancellationTokenSource()
        cont.invokeOnCancellation { cts.cancel() }

        try {
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        cont.resume(loc)
                    } else {
                        // Fallback to last known location
                        fusedClient.lastLocation
                            .addOnSuccessListener { lastLoc -> cont.resume(lastLoc) }
                            .addOnFailureListener { cont.resume(null) }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Fused location error", e)
                    cont.resume(null)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception requesting fused location", e)
            cont.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getFallbackSystemLocation(): Location? {
        val lm = locationManager ?: return null
        return try {
            val gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            when {
                gpsLoc != null && netLoc != null -> if (gpsLoc.time >= netLoc.time) gpsLoc else netLoc
                gpsLoc != null -> gpsLoc
                else -> netLoc
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting fallback system location", e)
            null
        }
    }
}

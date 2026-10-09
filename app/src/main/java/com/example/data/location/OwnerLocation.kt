package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** The phone's current position, used to pin the salon on the map. Null when it can't be found. */
object OwnerLocation {
    @SuppressLint("MissingPermission")
    suspend fun current(context: Context): Pair<Double, Double>? {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val fresh = withTimeoutOrNull(20_000) {
            suspendCancellableCoroutine { cont ->
                val cts = CancellationTokenSource()
                try {
                    client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                        .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc?.let { it.latitude to it.longitude }) }
                        .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                } catch (_: SecurityException) {
                    if (cont.isActive) cont.resume(null)
                }
                cont.invokeOnCancellation { cts.cancel() }
            }
        }
        if (fresh != null) return fresh
        return suspendCancellableCoroutine { cont ->
            try {
                client.lastLocation
                    .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc?.let { it.latitude to it.longitude }) }
                    .addOnFailureListener { if (cont.isActive) cont.resume(null) }
            } catch (_: SecurityException) {
                if (cont.isActive) cont.resume(null)
            }
        }
    }
}

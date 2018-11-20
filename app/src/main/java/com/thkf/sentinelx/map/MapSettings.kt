package com.thkf.sentinelx.map

import android.app.Activity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory


const val DEFAULT_ME = 200f
const val DEFAULT_ONLINE_MARKER = 160f
const val DEFAULT_OFFLINE_MARKER = 15f

//const val ME = R.drawable.person_pin
//const val ONLINE_MARKER = R.drawable.person_pin_circle_online
//const val OFFLINE_MARKER = R.drawable.person_pin_circle_offline



//val ME : BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.navigation) ?: BitmapDescriptorFactory.defaultMarker(DEFAULT_ME)
//val ONLINE_MARKER : BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.navigation_online) ?: BitmapDescriptorFactory.defaultMarker(DEFAULT_ONLINE_MARKER)
//val OFFLINE_MARKER : BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.navigation_offline) ?: BitmapDescriptorFactory.defaultMarker(DEFAULT_OFFLINE_MARKER)

val ME : BitmapDescriptor = BitmapDescriptorFactory.defaultMarker(DEFAULT_ME)
val ONLINE_MARKER : BitmapDescriptor = BitmapDescriptorFactory.defaultMarker(DEFAULT_ONLINE_MARKER)
val OFFLINE_MARKER : BitmapDescriptor = BitmapDescriptorFactory.defaultMarker(DEFAULT_OFFLINE_MARKER)





//val ONLINE_MARKER : BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.online_marker) ?:
//            BitmapDescriptorFactory.defaultMarker(DEFAULT_ONLINE_MARKER)
//val OFFLINE_MARKER : BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.offline_marker) ?:
//            BitmapDescriptorFactory.defaultMarker(DEFAULT_OFFLINE_MARKER)



object GooglePlayServicesChecker {

    fun isGooglePlayServiceInstalled(activity: Activity): Boolean {
        val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)
        return if(available == ConnectionResult.SUCCESS)
            true
        else {
            GoogleApiAvailability.getInstance().getErrorDialog(activity, available, 10).show()
            false
        }
    }

}


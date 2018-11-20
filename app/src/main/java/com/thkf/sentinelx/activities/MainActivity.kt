package com.thkf.sentinelx.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.nostra13.universalimageloader.core.ImageLoader
import com.thkf.sentinelx.R
import com.thkf.sentinelx.activities.prefActivity.PreferencesActivity
import com.thkf.sentinelx.adaptors.RecyclerItemClickListener
import com.thkf.sentinelx.adaptors.UsersListRecyclerAdaptor
import com.thkf.sentinelx.commons.BEARING
import com.thkf.sentinelx.commons.EMAIL
import com.thkf.sentinelx.commons.IMAGE
import com.thkf.sentinelx.commons.LAST_UPDATE
import com.thkf.sentinelx.commons.LAT
import com.thkf.sentinelx.commons.LON
import com.thkf.sentinelx.commons.NAME
import com.thkf.sentinelx.commons.OFFLINE
import com.thkf.sentinelx.commons.ONLINE
import com.thkf.sentinelx.commons.PASSWORD
import com.thkf.sentinelx.commons.ROLE
import com.thkf.sentinelx.commons.ROOT
import com.thkf.sentinelx.commons.STATUS
import com.thkf.sentinelx.commons.UID
import com.thkf.sentinelx.commons.USER
import com.thkf.sentinelx.commons.auth
import com.thkf.sentinelx.commons.firestore
import com.thkf.sentinelx.commons.getPixelsFromDp
import com.thkf.sentinelx.commons.signOut
import com.thkf.sentinelx.commons.timeDifference
import com.thkf.sentinelx.extensions.Prefs
import com.thkf.sentinelx.extensions.floatPref
import com.thkf.sentinelx.extensions.logE
import com.thkf.sentinelx.extensions.logI
import com.thkf.sentinelx.extensions.toastLong
import com.thkf.sentinelx.map.AsyncResponse
import com.thkf.sentinelx.map.GetDirectionsData
import com.thkf.sentinelx.map.GooglePlayServicesChecker
import com.thkf.sentinelx.map.ME
import com.thkf.sentinelx.map.OFFLINE_MARKER
import com.thkf.sentinelx.map.OK
import com.thkf.sentinelx.map.ONLINE_MARKER
import com.thkf.sentinelx.map.OnInfoWindowElemTouchListener
import com.thkf.sentinelx.map.Path
import com.thkf.sentinelx.map.getDirectionUrl
import com.thkf.sentinelx.models.User
import com.thkf.sentinelx.prefs.ALTERNATIVES_PATH
import com.thkf.sentinelx.prefs.FAST_INTERVAL
import com.thkf.sentinelx.prefs.FOCUS_ON_MARKER
import com.thkf.sentinelx.prefs.MAP_MODE
import com.thkf.sentinelx.prefs.MAP_TYPE
import com.thkf.sentinelx.prefs.ROTATE_MAP
import com.thkf.sentinelx.prefs.ZOOM_LEVEL
import com.thkf.sentinelx.receivers.NetworkStateChangeReceiver
import com.thkf.sentinelx.receivers.OnlineStatusListiner
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.util.*


class MainActivity : AppCompatActivity(),
        RecyclerItemClickListener,
        OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnlineStatusListiner,
        AsyncResponse {

    companion object {
        var LOGIN_STATUS = "login_status"
        var LOGIN = false
        private const val REQUEST_CHECK_SETTINGS = 300
        private const val ACCESS_FINE_LOCATION_INTENT_ID = 303
        private const val CHANGE_PROFILE_IMAGE = 306
    }

    private val uid by lazy { Prefs(this).get(UID, "") }
    private val username by lazy { Prefs(this).get(NAME, "") }
    private val email by lazy { Prefs(this).get(EMAIL, "") }
    private var focus = true
    private var mRotateMap = true
    private var mAlternativePaths = true
    private var fastInterval: Long = 15
    private var mMapType = 1
    private var mMapMode = 1

    private lateinit var listener: ListenerRegistration
    private var usersList = listOf<User>()

    private val adaptor: UsersListRecyclerAdaptor by lazy {
        UsersListRecyclerAdaptor(this@MainActivity, usersList)
    }

    private val markers = mutableMapOf<String, Marker>()

    private var marker: Marker? = null
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var locationRequest: LocationRequest
    private var googleApiClient: GoogleApiClient? = null
    private var focusOnMarker: Marker? = null
    private var zoomLevel by floatPref(this, ZOOM_LEVEL, 16.0f)
    private var mPolyLines: MutableList<Polyline> = mutableListOf()

    private lateinit var location: Location
    private lateinit var pathUserPosition: LatLng

    private val receiver = NetworkStateChangeReceiver()

    private var online = false

    private lateinit var infoProfile: CircleImageView
    private lateinit var infoTitle: TextView
    private lateinit var infoSnippet: TextView
    private lateinit var infoLastSeen: TextView
    private lateinit var infoLocation: TextView
    private lateinit var infoAddress: TextView
    private lateinit var infoButton: ImageView
    private lateinit var infoButtonListener: OnInfoWindowElemTouchListener

    private val dialog: MaterialDialog by lazy {
        MaterialDialog.Builder(this)
                .content("Please wait...")
                .progress(true, 0)
                .theme(Theme.LIGHT)
                .cancelable(false)
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        logI("onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (GooglePlayServicesChecker.isGooglePlayServiceInstalled(this)) {
            val fragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            fragment.getMapAsync(this)
        }

        with(users_list) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = adaptor
        }

        userListListener()

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        tv_username.text = username
        tv_email.text = email

        if (savedInstanceState != null) {
            LOGIN = savedInstanceState.getBoolean(LOGIN_STATUS, false)
        }

        un_follow.setOnClickListener {
            unFollowUser()
        }

    }

    private fun userListListener() {
        listener = firestore().collection(ROOT).whereEqualTo(ROLE, USER)
                .addSnapshotListener { snapshot, exception ->

                    if (exception != null) logI("Error: " + exception.message)

                    if(snapshot != null) {
                        usersList = snapshot.filter { it.id != uid }
                                .map {
                                    it.toObject(User::class.java)
                                }
                                .toList()
                                .sortedWith(compareBy({ it.status == OFFLINE }, { it.name }))
                        adaptor.changeData(usersList)
                        userMarks()
                    }
                }
    }

    private fun userMarks() {
        logI("usersMarkers")
        logI("User list size: ${usersList.size}")
        usersList.forEach {

            if (markers.containsKey(it.uid)) {
                markers[it.uid]?.position = LatLng(it.lat, it.lon)
                markers[it.uid]?.rotation = it.bearing
                markers[it.uid]?.setIcon(if (it.status == ONLINE) ONLINE_MARKER else OFFLINE_MARKER)
                markers[it.uid]?.tag = it
            } else {
                val mark = mGoogleMap.addMarker(MarkerOptions()
                        .position(LatLng(it.lat, it.lon))
                        .rotation(it.bearing)
                        .title(it.name)
                        .anchor(0.5f, 0.6f)
                        .flat(true)
                        .snippet(it.email)
                        .icon(if (it.status == ONLINE) ONLINE_MARKER else OFFLINE_MARKER))
//                            BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(R.drawable.navigation_online))
//                            else
//                            BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(R.drawable.navigation_offline))))
//                        .icon(if (it.status == ONLINE) ONLINE_MARKER else OFFLINE_MARKER))
                mark.tag = it
                markers[it.uid] = mark
            }

        }
    }

    override fun onRecyclerItemClick(item: User, pos: Int) {
        drawer_layout.closeDrawer(GravityCompat.START)
        logI("Position: $pos")
        logI("User: $item")
        focusOnMarker = markers[item.uid]
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(focusOnMarker?.position, zoomLevel))
        markerDetails(item)

    }

    private fun markerDetails(item: User) {
        Glide.with(this).load(item.image)
                .apply(RequestOptions()
                        .placeholder(R.mipmap.ic_launcher_foreground).error(R.mipmap.ic_launcher_foreground)).into(infoProfile)
        infoSnippet.text = item.email
        infoLastSeen.text = if(item.status == ONLINE) "Status: Online" else "Last Seen: " + timeDifference(item.last_update)
        infoLocation.text = getString(R.string.location_2_placeholder, item.lat.toString(), item.lon.toString())
        infoAddress.text = getString(R.string.address_1_placeholder, getAddress(item.lat, item.lon))
        focusOnMarker?.showInfoWindow()
    }

    private fun followUser(latLng: LatLng) {
        unFollowUser()
        if (online) {
            dialog.show()
            pathUserPosition = latLng
            val directionUrl = getDirectionUrl(LatLng(location.latitude, location.longitude), latLng, mAlternativePaths, mMapMode)
            logI("Direction URL: $directionUrl")
            val directionData = GetDirectionsData(this)
            directionData.execute(directionUrl)
            un_follow.visibility = View.VISIBLE
            focusOnMarker = marker
        } else {
            toastLong(this, "Not connected to internet")
        }
    }

    private fun unFollowUser() {
        un_follow.visibility = View.GONE
        mPolyLines.forEach {
            it.remove()
        }
    }

    private fun getAddress(lat: Double, lon: Double): String {
        try {
            val geoCoder = Geocoder(this, Locale.getDefault())
            val address = geoCoder.getFromLocation(lat, lon, 1)
            if (address.size > 0) {
                val lines = address[0].maxAddressLineIndex
                var completeAddress = ""
                (0..lines).forEach {
                    completeAddress += address[0].getAddressLine(it)
                }
                return completeAddress
            }
        } catch (e: Exception) {
            logE("Error: ${e.stackTrace}")
        }
        return "Not Available"
    }

    override fun onProcessFinish(data: Pair<String, List<Path>?>) {
        if (data.first == OK) {
            val polyList = data.second
            if (polyList != null && polyList.isNotEmpty()) {
                polyList.forEach {
                    it.polylineOptions.forEach {
                        mPolyLines.add(mGoogleMap.addPolyline(it))
                    }
                    logE("Info: ${it.info}")
                }
                zoomRoute()
            } else {
                logE("No Directions found")
            }
        } else {
            logE("Cannot found Directions")
        }
        dialog.dismiss()
    }

    private fun zoomRoute() {

        val bounds = LatLngBounds.Builder().include(marker?.position).include(pathUserPosition).build()

        val routePadding = 150

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, routePadding))

    }

    override fun onBackPressed() {
        logI("onBackPressed")
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if (online)
                signOut()
            super.onBackPressed()
        }
    }

    override fun onPause() {
        logI("onPause")
        listener.remove()
        if (googleApiClient != null && googleApiClient?.isConnected!!)
            stopLocationUpdates()
        super.onPause()
    }

    override fun onResume() {
        logI("onResume")
        super.onResume()
        focus = Prefs(this).getBoolean(FOCUS_ON_MARKER, true)
        mRotateMap = Prefs(this).getBoolean(ROTATE_MAP, true)
        fastInterval = Prefs(this).get(FAST_INTERVAL, "15").toLong()
        mMapType = Prefs(this).get(MAP_TYPE, "1").toInt()
        mMapMode = Prefs(this).get(MAP_MODE, "1").toInt()
        mAlternativePaths = Prefs(this).get(ALTERNATIVES_PATH, true)

        if (::mGoogleMap.isInitialized) {
            mGoogleMap.mapType = mMapType
        }

        registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        if (googleApiClient != null && googleApiClient?.isConnected!!) {
            startLocationUpdates()
        } else {
            initGoogleAPIClient()
        }
        checkPermissions()
        ImageLoader.getInstance().displayImage(Prefs(this).get(IMAGE, ""), profile_image)
    }

    override fun onStop() {
        logI("onStop")
        googleApiClient?.disconnect()
        unregisterReceiver(receiver)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                if (online) {

                    val data = mapOf(STATUS to OFFLINE)
                    firestore().collection(ROOT)
                            .document(Prefs(this).get(UID, ""))
                            .update(data)

                    signOut()
                }
                finish()
                true
            }
            R.id.action_user_profile -> {
                val intent = Intent(this@MainActivity, UserProfileActivity::class.java)
                if (::location.isInitialized) {
                    logE("Location is set...")
                    intent.putExtra(LAT, location.latitude)
                    intent.putExtra(LON, location.longitude)
                }
                startActivityForResult(intent, CHANGE_PROFILE_IMAGE)
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this@MainActivity, PreferencesActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        outState?.putBoolean(LOGIN_STATUS, LOGIN)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun isOnline(online: Boolean) {
        this.online = online
        if (online) {
            offlineView.visibility = View.GONE
            if (!MainActivity.LOGIN) {
                login()
            }
        } else {
            offlineView.visibility = View.VISIBLE
        }
        userListListener()
    }

    private fun login() {
        val email = Prefs(this).get(EMAIL, "")
        val pass = Prefs(this).get(PASSWORD, "")

        if (email.isNotEmpty() && pass.isNotEmpty()) {
            auth().signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            MainActivity.LOGIN = true
                        } else {
                            MainActivity.LOGIN = false
                            toastLong("Something went wrong pleas restart the application.")
                        }
                    }
                    .addOnFailureListener { _ ->
                        MainActivity.LOGIN = false
                        toastLong("Something went wrong pleas restart the application.")
                    }
        }
    }

    override fun onMapReady(m: GoogleMap) {
        logI("onMapReady")
        mGoogleMap = m
        mGoogleMap.apply {
            uiSettings.apply {
                isZoomGesturesEnabled = true
                setAllGesturesEnabled(true)
                isMapToolbarEnabled = true
                isCompassEnabled = true
                isMyLocationButtonEnabled = true
                isRotateGesturesEnabled = true
                isZoomControlsEnabled = true
            }
            mapType = mMapType
        }
        enableCurrentPositionButton()

        mGoogleMap.setOnMyLocationButtonClickListener {
            logI("onMyLocationButtonClick")
            if (marker != null) {
                focusOnMarker = marker
            }
            false
        }

        mGoogleMap.setOnMarkerClickListener { marker ->
            if (marker != null) {
                focusOnMarker = marker
                if(marker.tag != null) {
                    val user: User = marker.tag as User
                    markerDetails(user)
                }
            }
            true
        }

        mGoogleMap.setOnCameraMoveListener {
            zoomLevel = mGoogleMap.cameraPosition.zoom
        }

        mGoogleMap.setOnMapClickListener {
        }

        customInfoWindowInit()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun customInfoWindowInit() {
        mapWrapperLayout.init(mGoogleMap, getPixelsFromDp(this, (39 + 20).toFloat()))
        val infoWindow: ViewGroup = layoutInflater.inflate(R.layout.info_window, null) as ViewGroup

        infoProfile = infoWindow.findViewById(R.id.info_user_profile_image) as CircleImageView
        infoTitle = infoWindow.findViewById(R.id.info_title) as TextView
        infoSnippet = infoWindow.findViewById(R.id.info_snippet) as TextView
        infoLastSeen = infoWindow.findViewById(R.id.info_last_seen) as TextView
        infoLocation = infoWindow.findViewById(R.id.info_location) as TextView
        infoAddress = infoWindow.findViewById(R.id.info_address) as TextView
        infoButton = infoWindow.findViewById(R.id.info_follow) as ImageView

        infoButtonListener = object : OnInfoWindowElemTouchListener(infoButton) {
                    override fun onClickConfirmed(v: View, marker: Marker?) {
                        marker?.hideInfoWindow()
                        followUser(marker?.position!!)
                    }
                }

        infoButton.setOnTouchListener(infoButtonListener)

        mGoogleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(mark: Marker): View {
                infoTitle.text = mark.title
                infoSnippet.text = mark.snippet
                infoButtonListener.setMarker(mark)
                mapWrapperLayout.setMarkerWithInfoWindow(mark, infoWindow)
                return infoWindow
            }

            override fun getInfoWindow(p0: Marker?): View? {
                return null
            }
        })
    }

    override fun onLocationChanged(location: Location) {
        logI("onLocationChanged")
        logI("Lat: ${location.latitude}, Lon: ${location.longitude}, Bearing: ${location.bearing}")
        this.location = location
        updateCurrentMarker(LatLng(location.latitude, location.longitude))

        // TODO location update with history
        val data = mapOf(LAT to location.latitude, LON to location.longitude, BEARING to location.bearing,
                STATUS to ONLINE,
                LAST_UPDATE to FieldValue.serverTimestamp())
        firestore().collection(ROOT)
                .document(Prefs(this).get(UID, ""))
                .update(data)

    }

    private fun updateCurrentMarker(latLng: LatLng) {
        logI("updateCurrentMarker")
        if (marker == null) {
            marker = mGoogleMap.addMarker(MarkerOptions()
                    .title("Your are here")
                    .position(latLng)
                    .anchor(0.5f, 0.6f)
                    .icon(ME)
                    .flat(true))
//                    .icon(ME).flat(true))
        } else {
            marker?.position = latLng
            marker?.rotation = location.bearing
        }

        if (focusOnMarker == null) {
            focusOnMarker = marker
        }

        if (focus) {
            if (mRotateMap) {
                focusOnMarkerWithRotation()
            } else {
                focusOnMarker()
            }
        }
    }

    private fun focusOnMarkerWithRotation() {
        val cameraPosition = CameraPosition.Builder()
                .target(focusOnMarker?.position)
                .bearing(focusOnMarker?.rotation!!)
                .zoom(zoomLevel)
                .build()
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun focusOnMarker() {
        val cameraPosition = CameraPosition.Builder()
                .target(focusOnMarker?.position)
                .zoom(zoomLevel)
                .build()
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun initGoogleAPIClient() {
        logI("initGoogleAPIClient")
        googleApiClient = GoogleApiClient.Builder(this@MainActivity)
                .addConnectionCallbacks(this@MainActivity)
                .addOnConnectionFailedListener(this@MainActivity)
                .addApi(LocationServices.API)
                .build()
        googleApiClient?.connect()
    }

    private fun enableCurrentPositionButton() {
        logI("enableCurrentPositionButton")
        if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
        } else {
            mGoogleMap.uiSettings.isMyLocationButtonEnabled = true
            mGoogleMap.isMyLocationEnabled = true
        }
    }

    private fun requestLocationPermission() {
        logI("requestLocationPermission")
        if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    ACCESS_FINE_LOCATION_INTENT_ID)

        } else {
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    ACCESS_FINE_LOCATION_INTENT_ID)
        }
    }

    private fun startLocationUpdates() {
        logI("startLocationUpdates")
        if (ContextCompat.checkSelfPermission(this@MainActivity,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            requestLocationPermission()
        else
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this@MainActivity)
    }

    private fun stopLocationUpdates() {
        logI("stopLocationUpdates")
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
    }

    private fun checkPermissions() {
        logI("checkPermissions")
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this@MainActivity,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                requestLocationPermission()
            else
                showSettingDialog()
        } else
            showSettingDialog()
    }

    private fun showSettingDialog() {
        logI("showSettingDialog")
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 30 * 1000
        locationRequest.fastestInterval = fastInterval * 1000
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result: PendingResult<LocationSettingsResult> =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback {
            @Override
            fun onResult(result: LocationSettingsResult) {
                val status = result.status
//                val state = result.locationSettingsStates
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> {
                        logI("SUCCESS")
                    }
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        status.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTINGS)
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        logI("SETTINGS_CHANGE_UNAVAILABLE")
                    }
                }
            }
        }
    }

    override fun onConnected(p0: Bundle?) {
        logI("onConnected")
        if (ContextCompat.checkSelfPermission(this@MainActivity,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            requestLocationPermission()
        else {
            val locationProviderClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
            locationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    this.location = location
                    logI("Lat: ${location.latitude}, Lon: ${location.longitude}")
                } else {
                    logE("Location is NULL")
                }
            }
        }
        startLocationUpdates()
    }

    override fun onConnectionSuspended(p0: Int) {
        logI("onConnectionSuspended")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        logI("onConnectionFailed")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        logI("onActivityResult")
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        startLocationUpdates()
                    }
                    Activity.RESULT_CANCELED -> {
                        logE("Location access denied...")
                    }
                }
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        logI("onRequestPermissionsResult")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ACCESS_FINE_LOCATION_INTENT_ID -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (googleApiClient == null) {
                        initGoogleAPIClient()
                        showSettingDialog()
                    } else {
                        showSettingDialog()
                    }

                    enableCurrentPositionButton()
                }
            }
        }
    }

}



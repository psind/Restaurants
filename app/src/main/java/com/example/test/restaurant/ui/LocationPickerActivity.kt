package com.example.test.restaurant.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.LightingColorFilter
import android.location.*
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.Toast
import com.example.test.restaurant.R
import com.example.test.restaurant.data.SnackBarListener
import com.example.test.restaurant.data.Utils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Places
import kotlinx.android.synthetic.main.activity_location_picker.*
import java.io.IOException
import java.util.*

class LocationPickerActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    val REQUEST_CHECK_SETTINGS = 9989
    private val UPDATE_LOCATION_INTERVAL: Long = 10000
    private val FASTEST_LOCATION_INTERVAL: Long = 5000
    private val CITY_PICKER_GOOGLE_CLIENT_ID = 2

    private var mGoogleApiClient: GoogleApiClient? = null
    private var placeAutocompleteAdapter: PlaceAutocompleteAdapter? = null
    private var locationManager: LocationManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_picker)

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        progressBar?.indeterminateDrawable?.colorFilter = LightingColorFilter(0x1000000,
                ContextCompat.getColor(this, R.color.colorAccent))

        locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        currentLocationText.setOnClickListener {
            getCurrentLocation()
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            if (mGoogleApiClient == null)
                mGoogleApiClient = GoogleApiClient.Builder(this)
                        .enableAutoManage(this as FragmentActivity, CITY_PICKER_GOOGLE_CLIENT_ID, this)
                        .addApi(Places.GEO_DATA_API)
                        .addApi(LocationServices.API)
                        .build()
        } catch (e: IllegalStateException) {
        }
    }

    override fun onResume() {
        super.onResume()
        setListeners()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) {
            mGoogleApiClient?.stopAutoManage(this as FragmentActivity)
            mGoogleApiClient?.disconnect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(locationListener)
    }

    /**
     * This method request User Location from Settings, If already enabled it fetches location.
     * Else, it asks the user to go to Settings and enable location
     *
     * @param activity Activity context
     */
    private fun requestUserLocation(activity: Activity?) {
        try {
            if (activity != null) {
                val mLocationRequest = LocationRequest()
                mLocationRequest.interval = UPDATE_LOCATION_INTERVAL
                mLocationRequest.fastestInterval = FASTEST_LOCATION_INTERVAL
                mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
                val result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build())

                result.setResultCallback { locationSettingsResult ->
                    val status = locationSettingsResult.status

                    when (status.statusCode) {
                        LocationSettingsStatusCodes.SUCCESS ->
                            if (Utils.checkInternet(this)) {
                                getCurrentLocation()
                            } else {
                                progressBar?.visibility = GONE
                                Utils.showSnackBar(this, object : SnackBarListener {
                                    override fun onRetryClickedFromSnackBar() {
                                        getCurrentLocation()
                                    }
                                }, getString(R.string.snackBar_internet_connection), autoCompleteTV, true)
                            }
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            progressBar?.visibility = GONE
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                            } catch (e: IntentSender.SendIntentException) {
                                // Ignore the error.
                            }

                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            Toast.makeText(this, "Unable to fetch Location", Toast.LENGTH_LONG).show()
                            progressBar?.visibility = GONE
                        }
                    }
                }
            }
        } catch (e: Exception) {
//            Tracer.error(e)
//            CustomToast.showLongToast(this, getString(R.string.unable_to_fetch_location))
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        getCurrentLocation()
        //show dialog
    }

    @SuppressLint("StaticFieldLeak")
    private fun setLocationData(mLastLocation: Location, context: Context?) {
        if (context != null) {
            object : AsyncTask<Void, Void, Address>() {

                override fun doInBackground(vararg params: Void): Address? {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses: List<Address> // Only retrieve 1 address
                    try {
                        addresses = geocoder.getFromLocation(mLastLocation.latitude, mLastLocation.longitude, 1)
                        if (!addresses.isEmpty()) {
                            return addresses[0]
                        }
                    } catch (e: IOException) {
//                        Tracer.error(e)
                    }

                    return null
                }

                override fun onPostExecute(address: Address?) {
                    try {
//                        ProgressHelper.hideProgressDialog()
                        if (address != null) {
                            onLocationSelected(address, address.getAddressLine(0))
                        } else {
//                            CustomToast.showLongToast(this, getString(R.string.unable_to_fetch_location))
                        }
                    } catch (ignored: Exception) {
//                            CustomToast.showLongToast(this, getString(R.string.unable_to_fetch_location))
                    }

                }
            }.execute()
        }
    }

    private fun setListeners() {
        // Register a listener that receives callbacks when a suggestion has been selected
        autoCompleteTV?.onItemClickListener = mAutocompleteClickListener
        // Create a filter for API
        // We want only city names
        val mPlaceFilter = AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .setCountry("IN")
                .build()

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        placeAutocompleteAdapter = PlaceAutocompleteAdapter(this,
                mGoogleApiClient!!, null, mPlaceFilter)
        autoCompleteTV?.setAdapter(placeAutocompleteAdapter)
        autoCompleteTV?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    autoCompleteTV?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_grey_24dp, 0, 0, 0)
                } else {
                    autoCompleteTV?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_grey_24dp, 0, R.drawable.ic_cancel_grey_24dp, 0)
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        autoCompleteTV?.setOnTouchListener(View.OnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2

            try {
                if (autoCompleteTV != null && event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= autoCompleteTV?.right!! -
                            autoCompleteTV?.compoundDrawables?.get(DRAWABLE_RIGHT)?.bounds?.width()!!) {
                        autoCompleteTV?.setText("")
                        autoCompleteTV?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_grey_24dp, 0, 0, 0)
                        return@OnTouchListener true
                    }
                }
            } catch (e: Exception) {
//                Tracer.error(e)
            }

            false
        })
    }

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see GeoDataApi.getPlaceById
     */
    private val mAutocompleteClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
        /*
         Retrieve the place ID of the selected item from the FragmentAdapter.
         The adapter stores each Place suggestion in a AutocompletePrediction from which we
         read the place ID and title.
          */
        val item = placeAutocompleteAdapter?.getItem(position)
        Utils.hideKeyBoard(this)
        onLocationSelected(null, item?.getFullText(null).toString())

    }

    private fun onLocationSelected(address: Address?, cityName: String) {
        val intent = Intent()
        intent.putExtra(Utils.ADDRESS_NAME, cityName)
        intent.putExtra(Utils.LANG_LANT, "${address?.latitude},${address?.longitude}")
        setResult(Utils.LOCATION_CODE, intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CHECK_SETTINGS) {
            requestUserLocation(this)
        }
    }

    private fun getCurrentLocation() {
        if (locationManager == null)
            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager?

        progressBar?.visibility = VISIBLE

        var location: Location? = null
        // getting GPS status
        val isGPSEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)

        // getting network status
        val isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isNetworkEnabled!!) {
            // no network provider is enabled
            Utils.showSnackBar(this, object : SnackBarListener {
                override fun onRetryClickedFromSnackBar() {
                    getCurrentLocation()
                }

            }, getString(R.string.snackBar_internet_connection), locationPickerLayout, true)
            progressBar?.visibility = GONE
        }else if(!isGPSEnabled!!){
            Utils.showSnackBar(this, object : SnackBarListener {
                override fun onRetryClickedFromSnackBar() {
                    getCurrentLocation()
                }

            }, getString(R.string.gps_connection), locationPickerLayout, true)
            progressBar?.visibility = GONE
        } else {
            if (isNetworkEnabled) {
                if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION), 1)
                    return
                }
                locationManager?.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 10000, 3000f, locationListener)

                if (locationManager != null) {
                    location = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (location != null) {
                        setLocationData(location, this)
                    }
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                if (location == null) {
                    locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 10000, 3000f, locationListener)
                    location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (location != null) {
                        setLocationData(location, this)
                    }
                }
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this, "Unable to connect to Google Services", Toast.LENGTH_LONG).show()
    }

    private var locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            if (location != null) {
                setLocationData(location, applicationContext)
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

        }

        override fun onProviderEnabled(provider: String) {

        }

        override fun onProviderDisabled(provider: String) {

        }
    }

}

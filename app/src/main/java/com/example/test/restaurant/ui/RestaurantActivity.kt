package com.example.test.restaurant.ui

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.test.restaurant.data.DialogListener
import com.example.test.restaurant.R
import com.example.test.restaurant.data.Utils
import kotlinx.android.synthetic.main.activity_restaurant.*

class RestaurantActivity : AppCompatActivity() {

    private var latitudeAndLongitude = ""
    private var address = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    override fun onStart() {
        super.onStart()

        setUpTabLayout()
        setAdapter()

    }

    private fun setUpTabLayout() {
        tabs?.setupWithViewPager(viewPager)
        tabs.getTabAt(0)?.text = getString(R.string.explore)
        tabs.getTabAt(1)?.text = getString(R.string.search)
    }

    private fun setAdapter() {
        val adapter = ViewPagerAdapter(supportFragmentManager, arrayListOf(ExploreTabFragment(), SearchTabFragment()),
                arrayListOf(getString(R.string.explore), getString(R.string.search)))
        viewPager?.adapter = adapter
        viewPager?.currentItem = 0
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_location, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_location) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)

            } else
                startActivityForResult(Intent(this, LocationPickerActivity::class.java), Utils.LOCATION_CODE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Utils.LOCATION_CODE) {
            latitudeAndLongitude = data?.getStringExtra(Utils.LANG_LANT) ?: ""
            address = data?.getStringExtra(Utils.ADDRESS_NAME) ?: ""
            Toast.makeText(this, data.toString(), Toast.LENGTH_LONG).show()
        }
    }

    fun getLongAndLat(): String = if (!TextUtils.isEmpty(latitudeAndLongitude)) latitudeAndLongitude
                                    else "28.592140,77.046051"


    fun getAddress(): String = if (!TextUtils.isEmpty(address)) address
                                else "Delhi, India"

    // Handling user response from location permission
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty()) {

            if (ContextCompat.checkSelfPermission(applicationContext, ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission
            (applicationContext, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (!shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION) &&
                        !shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {

                    Utils.showDialog(this, object : DialogListener {
                        override fun onPositiveClickedFromDialog() {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }

                    }, "", getString(R.string.location_permission_message),
                            getString(R.string.settings), getString(R.string.cancel))
                }
            } else
                startActivityForResult(Intent(this, LocationPickerActivity::class.java), Utils.LOCATION_CODE)
        }
    }

    inner class ViewPagerAdapter(fm: FragmentManager,
                                 private val list: ArrayList<Fragment> = ArrayList(),
                                 private val titleList: List<String> = ArrayList()) :
            FragmentPagerAdapter(fm) {

        override fun getItem(p0: Int): Fragment = list[p0]

        override fun getCount(): Int = list.size

        override fun getPageTitle(position: Int): CharSequence = titleList[position]
    }

}

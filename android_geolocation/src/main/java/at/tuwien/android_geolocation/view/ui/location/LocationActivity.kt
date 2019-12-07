package at.tuwien.android_geolocation.view.ui.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tuwien.geolocation_android.R

class LocationActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_ACCESS_LOCATION = 100
        const val REQUEST_ACCESS_WIFI_STATE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (
            /* checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || */checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@LocationActivity,
                arrayOf(/* Manifest.permission.ACCESS_COARSE_LOCATION, */Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_ACCESS_LOCATION
            )
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@LocationActivity,
                arrayOf(Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE),
                REQUEST_ACCESS_WIFI_STATE
            )
        }
    }

}

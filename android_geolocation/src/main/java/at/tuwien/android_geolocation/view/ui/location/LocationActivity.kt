package at.tuwien.android_geolocation.view.ui.location

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tuwien.geolocation_android.R

class LocationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
    }
}

package at.tuwien.android_geolocation.view.ui.location

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import at.tuwien.android_geolocation.service.MozillaLocationService
import com.tuwien.geolocation_android.R

class LocationActivity : AppCompatActivity() {

    private var locationService: MozillaLocationService?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        val connection: ServiceConnection
        connection = object : ServiceConnection {
            override fun onServiceDisconnected(p0: ComponentName?) {
                locationService=null
            }
            override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
                locationService=(service as MozillaLocationService.MozillaLocationBinder).getService()
            }
        }
        locationService?.bindService(Intent(this, MozillaLocationService::class.java), connection, Context.BIND_AUTO_CREATE)
    }

}

package at.tuwien.android_geolocation

import android.app.Application
import at.tuwien.android_geolocation.service.LocationServiceProvider
import at.tuwien.android_geolocation.service.repository.LocationRepository
import net.danlew.android.joda.JodaTimeAndroid

class GeolocationApplication : Application() {
    val locationRepository: LocationRepository
        get() = LocationServiceProvider.provideRepository(this)

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }

    fun activateSecureMode(passphrase: ByteArray): Boolean{
        return LocationServiceProvider.encryptDb(this, passphrase)
    }
}
package at.tuwien.android_geolocation

import android.annotation.SuppressLint
import android.app.Application
import at.tuwien.android_geolocation.service.LocationServiceProvider
import at.tuwien.android_geolocation.service.repository.LocationRepository

class GeolocationApplication : Application() {
    val locationRepository: LocationRepository
        get() = LocationServiceProvider.provideRepository(this)
}
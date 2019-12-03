package at.tuwien.android_geolocation.service.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.tuwien.android_geolocation.service.model.Location
import at.tuwien.android_geolocation.service.model.LocationDao
import kotlinx.coroutines.Dispatchers

class LocationRepository(private val dao: LocationDao,
                         private val locationService: MozillaLocationService) {
    private val _locations = MutableLiveData<List<Location>>().apply { value = emptyList() }
    val locations: LiveData<List<Location>> = _locations


}
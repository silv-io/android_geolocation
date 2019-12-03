package at.tuwien.android_geolocation.viewmodel.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import at.tuwien.android_geolocation.service.model.Location
import at.tuwien.android_geolocation.service.repository.LocationRepository


class LocationListViewModel(private val locationRepository: LocationRepository) : ViewModel() {
    //val locations: LiveData<List<Location>> = repository.locations
}

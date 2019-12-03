package at.tuwien.android_geolocation.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import at.tuwien.android_geolocation.service.repository.LocationRepository
import at.tuwien.android_geolocation.viewmodel.location.LocationDetailsViewModel
import at.tuwien.android_geolocation.viewmodel.location.LocationListViewModel

// Adapted from Android architecture example. Is useful for creating Viewmodels which are connected
// to Repository without using dependency injection, which would be overkill for our small architecture
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val locationRepository: LocationRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(LocationListViewModel::class.java) ->
                    LocationListViewModel(locationRepository)
                isAssignableFrom(LocationDetailsViewModel::class.java) ->
                    LocationDetailsViewModel(locationRepository)
                else ->
                    throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
            }
        } as T
}
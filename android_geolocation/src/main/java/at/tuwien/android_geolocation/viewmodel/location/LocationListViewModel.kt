package at.tuwien.android_geolocation.viewmodel.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.tuwien.android_geolocation.service.model.Location
import at.tuwien.android_geolocation.service.repository.LocationRepository
import at.tuwien.android_geolocation.util.Event
import at.tuwien.android_geolocation.util.Result
import kotlinx.coroutines.launch


class LocationListViewModel(private val locationRepository: LocationRepository) : ViewModel() {
    private val _items = MutableLiveData<List<Location>>().apply { value = emptyList() }
    val items: LiveData<List<Location>> = _items

    private val _openLocationEvent = MutableLiveData<Event<Long>>()
    val openLocationEvent: LiveData<Event<Long>> = _openLocationEvent

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText


    fun loadLocations() {
        viewModelScope.launch {
            val result = locationRepository.getLocations()
            (result as? Result.Success)?.let {
                _items.value = result.data
            }
        }
    }

    fun shortPressLocation(locationId: Long) {
        _openLocationEvent.value = Event(locationId)
    }

    fun fabClick() = viewModelScope.launch {
        val result = locationRepository.newLocation()
        (result as? Result.Success)?.let { _openLocationEvent.value = Event(it.data) }
    }
}

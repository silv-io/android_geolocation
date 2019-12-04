package at.tuwien.android_geolocation.viewmodel.location

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import at.tuwien.android_geolocation.service.model.Location
import at.tuwien.android_geolocation.service.model.Position
import at.tuwien.android_geolocation.service.repository.LocationRepository
import at.tuwien.android_geolocation.util.Event
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class LocationListViewModel(private val locationRepository: LocationRepository) : ViewModel() {
    private val _items = MutableLiveData<List<Location>>().apply { value = emptyList() }
    val items: LiveData<List<Location>> = _items

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _openLocationEvent = MutableLiveData<Event<Long>>()
    val openLocationEvent: LiveData<Event<Long>> = _openLocationEvent

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private var idCounter = 1L
    private val mockArrayList = mutableListOf(
        Location(
            idCounter++,
            Position(1.2, 2.3, 4.5),
            Position(6.7, 8.9, 0.1),
            Date(2019, 12, 12, 23, 54, 34),
            HashMap<String, String>()
        )
    )

    fun loadLocations() {
        Log.println(Log.INFO, "location_list_vm", "loading locations: $mockArrayList")
        val copy = mockArrayList.toMutableList()
        _items.value = copy
        Log.println(Log.INFO, "location_list_vm", "itemsvalue: ${items.value}")
    }

    fun longPressLocation(locationId: Long) {

    }

    fun shortPressLocation(locationId: Long) {
        _openLocationEvent.value = Event(locationId)
    }

    fun fabClick() {
        Log.println(Log.INFO, "location_list_vm", "fab click")
        mockArrayList.add(newLocation())
        loadLocations()
    }

    private fun newLocation(): Location {
        return Location(
            idCounter++,
            Position(1.2, 2.3, 4.5),
            Position(6.7, 8.9, 0.1),
            Date(2019, 12, 12, 23, 54, 34),
            HashMap<String, String>()
        )
    }

    private fun deleteLocations() {

    }
    //val locations: LiveData<List<Location>> = repository.locations
}

package at.tuwien.android_geolocation.viewmodel.location

import android.app.Application
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import at.tuwien.android_geolocation.GeolocationApplication
import at.tuwien.android_geolocation.service.MozillaLocationService
import at.tuwien.android_geolocation.service.model.Location
import at.tuwien.android_geolocation.service.repository.LocationRepository
import at.tuwien.android_geolocation.util.Event
import at.tuwien.android_geolocation.util.Result
import com.tuwien.geolocation_android.R
import kotlinx.coroutines.launch


class LocationListViewModel(
    private val locationRepository: LocationRepository,
    application: Application
) : AndroidViewModel(application) {
    private val _items = MutableLiveData<List<Location>>().apply { value = emptyList() }
    val items: LiveData<List<Location>> = _items

    private val _openLocationEvent = MutableLiveData<Event<Long>>()
    val openLocationEvent: LiveData<Event<Long>> = _openLocationEvent

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private var mozillaLocationService: MozillaLocationService? = null

    fun setMozillaLocationService(mozillaLocationService: MozillaLocationService?) {
        this.mozillaLocationService = mozillaLocationService
    }

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
        val mlsInfo = mozillaLocationService?.getMLSInfo()
        val gpsInfo = mozillaLocationService?.getGPSInfo()

        if (mlsInfo != null && gpsInfo != null) {
            val result = locationRepository.newLocation(mlsInfo, gpsInfo)
            (result as? Result.Success)?.let { _openLocationEvent.value = Event(it.data) }
        } else {
            showSnackbarMessage(R.string.snackbar_permission_error)
        }
    }

    //TODO: use somewhere
    fun secure(passphrase: ByteArray) {
        getApplication<GeolocationApplication>().activateSecureMode(passphrase)
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }
}

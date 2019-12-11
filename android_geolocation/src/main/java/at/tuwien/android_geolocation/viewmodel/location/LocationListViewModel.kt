package at.tuwien.android_geolocation.viewmodel.location

import android.app.Application
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import at.tuwien.android_geolocation.service.AntennaService
import at.tuwien.android_geolocation.service.mls.MLSRequest
import at.tuwien.android_geolocation.service.model.Location
import at.tuwien.android_geolocation.service.model.Position
import at.tuwien.android_geolocation.service.repository.LocationRepository
import at.tuwien.android_geolocation.util.*
import com.tuwien.geolocation_android.R
import kotlinx.coroutines.launch

class LocationListViewModel(
    private val locationRepository: LocationRepository,
    application: Application
) : AndroidViewModel(application) {
    private val _secureModeEnabled = MutableLiveData<Boolean>().apply {  value = false }
    val secureModeEnabled: LiveData<Boolean> = _secureModeEnabled

    private val _items = MutableLiveData<List<Location>>().apply { value = emptyList() }
    val items: LiveData<List<Location>> = _items

    private val _openLocationEvent = MutableLiveData<Event<Long>>()
    val openLocationEvent: LiveData<Event<Long>> = _openLocationEvent

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val _progressBar = MutableLiveData<Boolean>().apply { value = false }
    val progressBar: LiveData<Boolean> = _progressBar

    private var antennaService: AntennaService? = null

    fun setAntennaService(antennaService: AntennaService?) {
        this.antennaService = antennaService
    }

    fun loadLocations() {
        _secureModeEnabled.value = locationRepository.isEncryptedDatabaseActive()

        viewModelScope.launch {
            val result = getLocationsFromRepository()
            (result as? Result.Success)?.let {
                _items.value = result.data
            }
        }
    }

    private suspend fun getLocationsFromRepository(): Result<List<Location>>? {
        return locationRepository.getLocations()
    }

    fun shortPressLocation(locationId: Long) {
        _openLocationEvent.value = Event(locationId)
    }

    fun fabClick() = viewModelScope.launch {
        if (progressBar.value == true) {
            showSnackbarMessage(R.string.snackbar_loading_location)
        } else {
            _progressBar.value = true
            var mlsInfo: MLSRequest? = null
            var gpsInfo: Position? = null

            try {
                mlsInfo = antennaService?.getMLSInfo()
            } catch (e: MissingPermissionException) {
                showSnackbarMessage(R.string.snackbar_permission_error)
            } catch (e: NoCellTowerOrWifiInfoFound) {
                showSnackbarMessage(R.string.snackbar_no_gps_or_wifi_info_found)
            } catch (e: NetworkProviderException) {
                showSnackbarMessage(R.string.snackbar_network_provider_error)
            }

            try {
                gpsInfo = antennaService?.getGPSInfo()
            } catch (e: MissingPermissionException) {
                showSnackbarMessage(R.string.snackbar_permission_error)
            } catch (e: GpsProviderException) {
                showSnackbarMessage(R.string.snackbar_gps_provider_error)
            }


            Log.d("LocationListViewModel", "gps info is $gpsInfo, mlsinfo is $mlsInfo")

            if (mlsInfo != null && gpsInfo != null) {
                val result = locationRepository.newLocation(mlsInfo, gpsInfo)
                _progressBar.value = false
                when (result) {
                    is Result.Success -> _openLocationEvent.value = Event(result.data)
                    else -> showSnackbarMessage(R.string.snackbar_internet_error)
                }
            } else {
                _progressBar.value = false
            }
        }
    }

    fun isSecurityEnabled(): Boolean {
        return locationRepository.isEncryptedDatabaseActive()
    }

    fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }
}
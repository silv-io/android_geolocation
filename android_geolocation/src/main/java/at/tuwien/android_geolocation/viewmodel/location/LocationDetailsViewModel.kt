package at.tuwien.android_geolocation.viewmodel.location

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.tuwien.android_geolocation.service.model.Location
import at.tuwien.android_geolocation.service.model.Position
import at.tuwien.android_geolocation.service.repository.LocationRepository
import at.tuwien.android_geolocation.util.Event
import com.tuwien.geolocation_android.R
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

class LocationDetailsViewModel(private val locationRepository: LocationRepository) : ViewModel() {


    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private val _deleteLocationEvent = MutableLiveData<Event<Unit>>()
    val deleteLocationEvent: LiveData<Event<Unit>> = _deleteLocationEvent

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    fun init(locationId: Int) {
        this._location.value = Location(
            1,
            Position(1.2, 2.3, 4.5),
            Position(6.7, 8.9, 0.1),
            Date(2019, 12, 12, 23, 54, 34),
            HashMap<String, String>()
        )
    }

    fun deleteLocation() = viewModelScope.launch {
        showSnackbarMessage(R.string.snackbar_deleted_location)
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }

}

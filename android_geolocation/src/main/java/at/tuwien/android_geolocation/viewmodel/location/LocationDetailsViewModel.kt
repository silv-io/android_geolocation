package at.tuwien.android_geolocation.viewmodel.location

import android.app.Application
import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import at.tuwien.android_geolocation.service.model.Location
import at.tuwien.android_geolocation.service.repository.LocationRepository
import at.tuwien.android_geolocation.util.Event
import at.tuwien.android_geolocation.util.Result
import kotlinx.coroutines.launch

class LocationDetailsViewModel(
    private val locationRepository: LocationRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private val _deleteLocationEvent = MutableLiveData<Event<Unit>>()
    val deleteLocationEvent: LiveData<Event<Unit>> = _deleteLocationEvent

    private val _backEvent = MutableLiveData<Event<Any>>()
    val backEvent: LiveData<Event<Any>> = _backEvent

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    fun init(locationId: Long) = viewModelScope.launch {
        val result = locationRepository.getLocation(locationId)
        (result as? Result.Success)?.let { _location.value = it.data }
    }

    fun backClick() = viewModelScope.launch {
        _backEvent.value = Event(Any())
    }

    fun deleteLocation(locationId: Long) = viewModelScope.launch {
        _deleteLocationEvent.value = Event(locationRepository.deleteLocation(locationId))
    }

    fun sendLocation() = viewModelScope.launch {
        val context = getApplication<Application>().applicationContext

        val uri = locationRepository.createTemporaryLocationPlaintextFile(_location.value!!.toPlaintextString())

        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri)

        context.startActivity(emailIntent)
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }

}

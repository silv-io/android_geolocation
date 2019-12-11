package at.tuwien.android_geolocation.viewmodel.location

import android.app.Application
import android.text.Editable
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import at.tuwien.android_geolocation.service.AntennaService
import at.tuwien.android_geolocation.service.model.Location
import at.tuwien.android_geolocation.service.repository.LocationRepository
import at.tuwien.android_geolocation.util.Event
import at.tuwien.android_geolocation.util.Result
import at.tuwien.android_geolocation.util.succeeded
import com.tuwien.geolocation_android.R
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets


class LocationListViewModel(
    private val locationRepository: LocationRepository,
    application: Application
) : AndroidViewModel(application) {
    private val _securityPopupVisibility = MutableLiveData<Boolean>().apply {  value = false }
    val securityPopupVisibility: LiveData<Boolean> = _securityPopupVisibility

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

    fun loadLocations(): Boolean {
        var isSuccessful = false

        viewModelScope.launch {
            val result = getLocationsFromRepository()
            if (result?.succeeded!!) { isSuccessful = true }
            (result as? Result.Success)?.let {
                _items.value = result.data
            }
        }

        return isSuccessful
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
            val mlsInfo = antennaService?.getMLSInfo()
            val gpsInfo = antennaService?.getGPSInfo()
            Log.d("LocationListViewModel", "gps info is $gpsInfo, mlsinfo is $mlsInfo")

            if (mlsInfo != null && gpsInfo != null) {
                val result = locationRepository.newLocation(mlsInfo, gpsInfo)
                _progressBar.value = false
                (result as? Result.Success)?.let { _openLocationEvent.value = Event(it.data) }
            } else {
                _progressBar.value = false
                showSnackbarMessage(R.string.snackbar_permission_error)
            }
        }
    }

    fun startEnableSecurity() {
        if (this._secureModeEnabled.value!!) {
            showSnackbarMessage(R.string.snackbar_security_already_enabled)
        } else {
            _securityPopupVisibility.value = true
        }
    }

    fun cancelEnableSecurity() {
        _securityPopupVisibility.value = false
    }

    fun secureDatabase(pwd: Editable) {
        val passphrase = CharArray(pwd.length)
        pwd.getChars(0, pwd.length, passphrase, 0)
        pwd.clear()

        val passphraseBytes = passphrase.toByteArray()

        for (i in passphrase.indices) {
            passphrase[i] = 0.toChar()
        }

        locationRepository.openEncryptedDatabase(passphraseBytes)

        if (this.loadLocations()) {
            _secureModeEnabled.value = true
            _securityPopupVisibility.value = false
        } else {
            showSnackbarMessage(R.string.snackbar_security_error_connecting)
        }
    }

    private fun CharArray.toByteArray(): ByteArray {
        val charBuf: CharBuffer = CharBuffer.wrap(this)
        val byteBuf: ByteBuffer = StandardCharsets.UTF_8.encode(charBuf)
        val bytes = ByteArray(byteBuf.remaining())
        byteBuf.get(bytes, 0, bytes.size)
        charBuf.clear()
        byteBuf.clear()

        return bytes
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }
}

package at.tuwien.android_geolocation.viewmodel.location

import android.app.Application
import android.text.Editable
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import at.tuwien.android_geolocation.service.repository.LocationRepository
import at.tuwien.android_geolocation.util.Event
import at.tuwien.android_geolocation.util.Result
import com.tuwien.geolocation_android.R
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets

class EnableSecurityViewModel(
    private val locationRepository: LocationRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val _backEvent = MutableLiveData<Event<Any>>()
    val backEvent: LiveData<Event<Any>> = _backEvent

    fun backClick() = viewModelScope.launch {
        _backEvent.value = Event(Any())
    }

    fun cancelEnableSecurity() {
        backClick()
    }

    fun secureDatabase(pwd: Editable) {
        if (pwd.isBlank() || pwd.isEmpty()) {
            showSnackbarMessage(R.string.snackbar_security_empty_password)
            return
        }

        val passphrase = CharArray(pwd.length)
        pwd.getChars(0, pwd.length, passphrase, 0)
        pwd.clear()

        val passphraseBytes = passphrase.toByteArray()

        for (i in passphrase.indices) {
            passphrase[i] = 0.toChar()
        }

        locationRepository.openEncryptedDatabase(passphraseBytes)

        viewModelScope.launch {
            val result = locationRepository.getLocations()
            (result as? Result.Success)?.let {
                backClick()
                return@launch
            }
            locationRepository.closeEncryptedDatabase()
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
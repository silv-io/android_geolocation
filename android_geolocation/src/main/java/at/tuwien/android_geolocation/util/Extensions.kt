package at.tuwien.android_geolocation.util

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import at.tuwien.android_geolocation.GeolocationApplication
import com.google.android.material.snackbar.Snackbar

fun Fragment.getViewModelFactory(): ViewModelFactory {
    val repository =
        (requireContext().applicationContext as GeolocationApplication).locationRepository
    return ViewModelFactory(repository)
}

fun View.setupSnackbar(
    lifecycleOwner: LifecycleOwner,
    snackbarEvent: LiveData<Event<Int>>,
    timeLength: Int
) {

    snackbarEvent.observe(lifecycleOwner, Observer { event ->
        event.getContentIfNotHandled()?.let {
            showSnackbar(context.getString(it), timeLength)
        }
    })
}

fun View.showSnackbar(snackbarText: String, timeLength: Int) {
    Snackbar.make(this, snackbarText, timeLength).run {
//        addCallback(object : Snackbar.Callback() {
//            override fun onShown(sb: Snackbar?) {
//                EspressoIdlingResource.increment()
//            }
//
//            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
//                EspressoIdlingResource.decrement()
//            }
//        })
        show()
    }
}
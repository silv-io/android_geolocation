package at.tuwien.android_geolocation

import android.widget.TextView
import androidx.test.espresso.IdlingResource
import androidx.test.rule.ActivityTestRule
import at.tuwien.android_geolocation.view.ui.location.LocationActivity
import com.tuwien.geolocation_android.R


class FragmentIdlingResource(
    private val activityTestRule: ActivityTestRule<LocationActivity>
) : IdlingResource {
    private var resourceCallback: IdlingResource.ResourceCallback? = null
    override fun getName(): String {
        return "idling fragment resource"
    }

    override fun isIdleNow(): Boolean {

        val view = activityTestRule.activity.findViewById<TextView>(R.id.txt_capture_time)

        val idle = view != null && view.text.isNotEmpty() && view.text.isNotBlank()

        if (idle) {
            resourceCallback?.onTransitionToIdle()
        }
        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }

}
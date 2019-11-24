package at.tuwien.geolocation_android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.tuwien.geolocation_android.R


class LocationDetails : Fragment() {

    companion object {
        fun newInstance() = LocationDetails()
    }

    private lateinit var viewModel: LocationDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.fragment_location_details, container, false)

        val fab_delete: FloatingActionButton = v.findViewById(R.id.fab_delete)
        val toolbar: androidx.appcompat.widget.Toolbar = v.findViewById(R.id.detail_toolbar)

        fab_delete.setOnClickListener {
            Snackbar.make(v, "Delete action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigate(R.id.action_locationDetails_pop)
        }

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LocationDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}

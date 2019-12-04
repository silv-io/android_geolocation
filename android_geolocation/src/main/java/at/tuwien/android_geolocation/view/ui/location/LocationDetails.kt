package at.tuwien.android_geolocation.view.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import at.tuwien.android_geolocation.util.EventObserver
import at.tuwien.android_geolocation.util.getViewModelFactory
import at.tuwien.android_geolocation.util.setupSnackbar
import at.tuwien.android_geolocation.viewmodel.location.LocationDetailsViewModel
import com.google.android.material.snackbar.Snackbar
import com.tuwien.geolocation_android.R
import com.tuwien.geolocation_android.databinding.FragmentLocationDetailsBinding


class LocationDetails : Fragment() {
    private lateinit var viewDataBinding: FragmentLocationDetailsBinding

    //todo navargs
    val args: LocationDetailsArgs by navArgs()


    private val viewModel by viewModels<LocationDetailsViewModel> { getViewModelFactory() }

    companion object {
        fun newInstance() =
            LocationDetails()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        setupNavigation()
    }

    private fun setupNavigation() {
        viewModel.deleteLocationEvent.observe(this, EventObserver {
            findNavController().navigate(R.id.action_locationDetails_pop)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_location_details, container, false)
        val viewDataBinding = FragmentLocationDetailsBinding.bind(view).apply {
            vm = viewModel
        }
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner

        //TODO: choose argument
        viewModel.init(1)

        return view
    }


}

package at.tuwien.android_geolocation.view.ui.location

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import at.tuwien.android_geolocation.util.EventObserver
import at.tuwien.android_geolocation.util.getViewModelFactory
import at.tuwien.android_geolocation.util.setupSnackbar
import at.tuwien.android_geolocation.view.adapter.LocationListAdapter
import at.tuwien.android_geolocation.viewmodel.location.LocationListViewModel
import com.google.android.material.snackbar.Snackbar
import com.tuwien.geolocation_android.R
import com.tuwien.geolocation_android.databinding.FragmentLocationListBinding

class LocationList : Fragment() {

    private val viewModel by viewModels<LocationListViewModel> { getViewModelFactory() }

    private lateinit var viewDataBinding: FragmentLocationListBinding

    private lateinit var listAdapter: LocationListAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentLocationListBinding.inflate(inflater, container, false).apply {
            vm = viewModel
        }

        // Apparently this cannot be done with data-binding, because data-binding cannot give "Editable" which is needed by Saferoom
        val pwd = viewDataBinding.root.findViewById<EditText>(R.id.txt_password)
        val btn = viewDataBinding.root.findViewById<Button>(R.id.btn_security)

        btn.setOnClickListener { viewModel.secure(pwd.text) }

        setHasOptionsMenu(true)

        return viewDataBinding.root
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.menu_enable_security -> {
                viewModel.secureMenuAction()
                true
            }
            else -> false
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        setUpListAdapter()
        setUpNavigation()

        viewModel.loadLocations()
    }


    private fun setUpNavigation() {
        viewModel.openLocationEvent.observe(this, EventObserver {
            val action = LocationListDirections.actionLocationListToLocationDetails(it)
            findNavController().navigate(action)
        })
    }

    private fun setUpListAdapter() {
        val viewModel = viewDataBinding.vm
        if (viewModel != null) {
            listAdapter = LocationListAdapter(viewModel)
            viewDataBinding.itemList.adapter = listAdapter
        } else {
            Log.println(
                Log.WARN,
                "location_list",
                "viewModel not initialized when setting up adapter"
            )
        }
    }

}

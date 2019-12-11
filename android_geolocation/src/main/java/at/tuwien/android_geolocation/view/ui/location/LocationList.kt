package at.tuwien.android_geolocation.view.ui.location

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import at.tuwien.android_geolocation.service.AntennaService
import at.tuwien.android_geolocation.util.EventObserver
import at.tuwien.android_geolocation.util.setupSnackbar
import at.tuwien.android_geolocation.view.adapter.LocationListAdapter
import at.tuwien.android_geolocation.viewmodel.location.LocationListViewModel
import com.google.android.material.snackbar.Snackbar
import com.tuwien.geolocation_android.R
import com.tuwien.geolocation_android.databinding.FragmentLocationListBinding
import org.koin.androidx.viewmodel.ext.android.viewModel


class LocationList : Fragment() {

    private val viewModel by viewModel<LocationListViewModel>()

    private lateinit var viewDataBinding: FragmentLocationListBinding
    private lateinit var listAdapter: LocationListAdapter

    private lateinit var antennaService: AntennaService
    private var serviceIsBound: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startAntennaService()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentLocationListBinding.inflate(inflater, container, false).apply {
            vm = viewModel
        }

        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        setUpListAdapter()
        setUpNavigation()

        val toolbar: androidx.appcompat.widget.Toolbar = viewDataBinding.root.findViewById(R.id.list_toolbar)
        toolbar.inflateMenu(R.menu.list_menu)
        toolbar.setOnMenuItemClickListener { item -> onOptionsItemSelected((item))}

        viewModel.loadLocations()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_enable_security -> {
                if (viewModel.isSecurityEnabled()) {
                    viewModel.showSnackbarMessage(R.string.snackbar_security_already_enabled)
                } else {
                    if (!viewModel.progressBar.value!!) {
                        findNavController().navigate(R.id.action_locationList_to_enableSecurity)
                    } else {
                        viewModel.showSnackbarMessage(R.string.snackbar_loading_location)
                    }
                }
                true
            }
            else -> false
        }
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

    override fun onResume() {
        super.onResume()
        startAntennaService()
    }

    override fun onStop() {
        super.onStop()
        if (serviceIsBound) {
            this.context?.unbindService(serviceConnection)
            serviceIsBound = false
        }
    }

    private fun startAntennaService() {
        val serviceIntent = Intent(this.context, AntennaService::class.java)
        this.context?.startService(serviceIntent)

        val serviceBindIntent = Intent(this.context, AntennaService::class.java)
        this.context?.bindService(serviceBindIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: AntennaService.AntennaBinder =
                service as AntennaService.AntennaBinder
            antennaService = binder.getService()
            serviceIsBound = true

            viewModel.setAntennaService(antennaService)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            viewModel.setAntennaService(null)

            serviceIsBound = false
        }
    }

}

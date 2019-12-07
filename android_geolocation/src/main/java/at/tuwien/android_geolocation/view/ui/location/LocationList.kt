package at.tuwien.android_geolocation.view.ui.location

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import at.tuwien.android_geolocation.service.DummyContent
import at.tuwien.android_geolocation.service.MozillaLocationService
import at.tuwien.android_geolocation.util.EventObserver
import at.tuwien.android_geolocation.util.getViewModelFactory
import at.tuwien.android_geolocation.util.setupSnackbar
import at.tuwien.android_geolocation.view.adapter.LocationListAdapter
import at.tuwien.android_geolocation.viewmodel.location.LocationListViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.tuwien.geolocation_android.R
import com.tuwien.geolocation_android.databinding.FragmentLocationListBinding
import kotlinx.android.synthetic.main.item_list_content.view.*
import java.text.SimpleDateFormat
import java.util.*

class LocationList : Fragment() {

    private val viewModel by viewModels<LocationListViewModel> { getViewModelFactory() }

    private lateinit var viewDataBinding: FragmentLocationListBinding
    private lateinit var listAdapter: LocationListAdapter

    private lateinit var mozillaLocationService: MozillaLocationService
    private var serviceIsBound: Boolean = false

    @Volatile
    private var numChecked: Int = 0
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startMozillaLocationService()
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

    override fun onResume() {
        super.onResume()
        startMozillaLocationService()
    }

    override fun onStop() {
        super.onStop()
        if (serviceIsBound) {
            this.context?.unbindService(serviceConnection)
            serviceIsBound = false
        }
    }

    private fun startMozillaLocationService() {
        val serviceIntent = Intent(this.context, MozillaLocationService::class.java)
        this.context?.startService(serviceIntent)

        val serviceBindIntent = Intent(this.context, MozillaLocationService::class.java)
        this.context?.bindService(serviceBindIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: MozillaLocationService.MozillaLocationBinder = service as MozillaLocationService.MozillaLocationBinder
            mozillaLocationService = binder.getService()
            serviceIsBound = true

            viewModel.setMozillaLocationService(mozillaLocationService)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            viewModel.setMozillaLocationService(null)

            serviceIsBound = false
        }
    }

    inner class SimpleItemRecyclerViewAdapter(
        private val fragmentView: View,
        private val values: List<DummyContent.DummyItem>
    ) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
            if (numChecked > 0) {
                toggleCard(v as MaterialCardView)
            } else {
                //TODO: check selected
                val action = LocationListDirections.actionLocationListToLocationDetails(2)
                fragmentView.findNavController()
                    .navigate(action)
            }
        }

        private val onLongClickListener: View.OnLongClickListener = View.OnLongClickListener { v ->
            toggleCard(v as MaterialCardView)
            true
        }

        private fun toggleCard(v: MaterialCardView) {
            v.toggle()
            if (v.isChecked) {
                numChecked++
            } else {
                numChecked--
            }

            if (numChecked > 0) {
                fab.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_delete_forever_white,
                        context?.theme
                    )
                )
            } else {
                fab.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_add_white,
                        context?.theme
                    )
                )
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = item.id
            holder.contentView.text = item.content

            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMAN)
            holder.contentViewHeader.text = formatter.format(Calendar.getInstance().time)

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
                setOnLongClickListener(onLongClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
            val contentViewHeader: TextView = view.content_header
        }
    }
}

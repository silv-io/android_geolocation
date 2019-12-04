package at.tuwien.android_geolocation.view.ui.location

import android.os.Bundle
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

    @Volatile
    private var numChecked: Int = 0
    private lateinit var fab: FloatingActionButton

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
                        R.mipmap.baseline_delete_forever_white_24,
                        context?.theme
                    )
                )
            } else {
                fab.setImageDrawable(
                    resources.getDrawable(
                        R.mipmap.baseline_add_white_24,
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

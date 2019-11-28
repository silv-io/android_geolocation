package at.tuwien.geolocation_android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import at.tuwien.geolocation_android.dummy.DummyContent
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.tuwien.geolocation_android.R
import kotlinx.android.synthetic.main.item_list_content.view.*
import java.text.SimpleDateFormat
import java.util.*

class LocationList : Fragment() {

    private lateinit var viewModel: LocationListViewModel

    @Volatile private var numChecked: Int = 0
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v:View = inflater.inflate(R.layout.fragment_location_list, container, false)
        fab = v.findViewById(R.id.fab)
        val itemList: RecyclerView = v.findViewById(R.id.item_list)

        fab.setOnClickListener { view ->
            if (numChecked == 0) {
                view.findNavController().navigate(R.id.action_locationList_to_locationDetails)
            } else {
                Snackbar.make(v, "Delete action", Snackbar.LENGTH_LONG).show()
            }
        }

        setupRecyclerView(itemList, v)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LocationListViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun setupRecyclerView(recyclerView: RecyclerView, fragmentView: View) {
        recyclerView.adapter =
            SimpleItemRecyclerViewAdapter(
                fragmentView,
                DummyContent.ITEMS
            )
    }

    inner class SimpleItemRecyclerViewAdapter(
        private val fragmentView: View,
        private val values: List<DummyContent.DummyItem>
    ) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener = View.OnClickListener {v ->
            if (numChecked > 0) {
                toggleCard(v as MaterialCardView)
            } else {
                fragmentView.findNavController()
                    .navigate(R.id.action_locationList_to_locationDetails)
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
                fab.setImageDrawable(resources.getDrawable(R.mipmap.baseline_delete_forever_white_24, context?.theme))
            } else {
                fab.setImageDrawable(resources.getDrawable(R.mipmap.baseline_add_white_24, context?.theme))
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

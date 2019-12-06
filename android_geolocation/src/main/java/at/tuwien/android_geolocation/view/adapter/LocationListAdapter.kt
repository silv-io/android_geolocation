package at.tuwien.android_geolocation.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.tuwien.android_geolocation.service.model.Location
import at.tuwien.android_geolocation.viewmodel.location.LocationListViewModel
import com.tuwien.geolocation_android.databinding.ItemListContentBinding


class LocationListAdapter(private val viewModel: LocationListViewModel) :
    ListAdapter<Location, LocationListAdapter.ViewHolder>(LocationDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(viewModel, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemListContentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            viewModel: LocationListViewModel,
            item: Location
        ) {

            binding.vm = viewModel
            binding.location = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemListContentBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}


/**
 * Callback for calculating the diff between two non-null items in a list.
 *
 * Used by ListAdapter to calculate the minimum number of changes between and old list and a new
 * list that's been passed to `submitList`.
 */
class LocationDiffCallback : DiffUtil.ItemCallback<Location>() {
    override fun areItemsTheSame(oldItem: Location, newItem: Location): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Location, newItem: Location): Boolean {
        return oldItem == newItem
    }
}


@BindingAdapter("android:src")
fun setItems(listView: RecyclerView, items: List<Location>) {
    (listView.adapter as LocationListAdapter).submitList(items)
}
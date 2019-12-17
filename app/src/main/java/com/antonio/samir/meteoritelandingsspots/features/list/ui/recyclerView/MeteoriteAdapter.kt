package com.antonio.samir.meteoritelandingsspots.features.list.ui.recyclerView

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import com.antonio.samir.meteoritelandingsspots.R
import com.antonio.samir.meteoritelandingsspots.features.getDistanceFrom
import com.antonio.samir.meteoritelandingsspots.features.list.ui.recyclerView.selector.MeteoriteSelector
import com.antonio.samir.meteoritelandingsspots.features.list.viewmodel.MeteoriteListViewModel
import com.antonio.samir.meteoritelandingsspots.features.yearString
import com.antonio.samir.meteoritelandingsspots.service.business.model.Meteorite
import org.apache.commons.lang3.StringUtils
import java.util.*

/**
 * Custom RecyclerView.Adapter to deal with meteorites cursor
 */
class MeteoriteAdapter(
        private val context: Context,
        private val meteoriteSelector: MeteoriteSelector,
        private val viewModel: MeteoriteListViewModel,
        differ: MeteoriteDiffCallback
) : PagedListAdapter<Meteorite, ViewHolderMeteorite>(differ) {

    private var selectedMeteorite: Meteorite? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderMeteorite {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_meteorite, parent, false)
        val vh = ViewHolderMeteorite(view)

        //On view click use MeteoriteSelector to do execute the proper according the current layout
        view.setOnClickListener {
            vh.meteorite?.let { meteoriteSelector.selectItem(it) }
        }
        return vh
    }

    fun updateListUI(meteorite: Meteorite) {
        if (!Objects.equals(meteorite, selectedMeteorite)) {

            val previousMet = selectedMeteorite
            selectedMeteorite = meteorite

//            notifyItemChanged(meteorites.indexOf(previousMet))
//            notifyItemChanged(meteorites.indexOf(meteorite))
        }
    }

    fun setData(meteorites: PagedList<Meteorite>) {
        if (itemCount != meteorites.size && currentList != meteorites) {
            submitList(null)
            notifyDataSetChanged()
        }
        submitList(meteorites)
    }

    override fun onBindViewHolder(viewHolder: ViewHolderMeteorite, position: Int) {

        getItem(position)?.let { meteorite ->

            val meteoriteName = meteorite.name
            val year = meteorite.yearString

            viewHolder.name.text = context.getString(R.string.title, meteoriteName, year)

            viewHolder.name.contentDescription = meteoriteName

            setLocationText(meteorite, viewHolder)

            viewHolder.meteorite = meteorite


            var color = R.color.unselected_item_color
            var title_color = R.color.title_color
            var elevation = R.dimen.unselected_item_elevation

            if (Objects.equals(meteorite, selectedMeteorite)) {
                color = R.color.selected_item_color
                title_color = R.color.selected_title_color
                elevation = R.dimen.selected_item_elevation
            }

            viewHolder.mCardview.setCardBackgroundColor(context.resources.getColor(color))
            viewHolder.mCardview.cardElevation = context.resources.getDimensionPixelSize(elevation).toFloat()
            viewHolder.name.setTextColor(context.resources.getColor(title_color))
        }
    }


    private fun setLocationText(meteorite: Meteorite, viewHolder: ViewHolderMeteorite) {
        val address = meteorite.address

        //Always remove the previous observer
        if (viewHolder.liveMet != null && viewHolder.addressObserver != null) {
            viewHolder.liveMet?.removeObserver(viewHolder.addressObserver!!)
            viewHolder.liveMet = null
            viewHolder.addressObserver = null
        }

        if (StringUtils.isNotEmpty(address)) {
            showAddress(viewHolder, address, meteorite)
        } else {
            //If address is still empty then observe this entity to be aware of any change
            viewHolder.liveMet = viewModel.getMeteorite(meteorite)

            val addressObserver = Observer<Meteorite> { meteorite1 ->
                val newAddress = meteorite1?.address
                if (StringUtils.isNotEmpty(newAddress)) {
                    showAddress(viewHolder, newAddress!!, meteorite)
                    viewHolder.location.startAnimation(AnimationUtils.loadAnimation(context, R.anim.view_show))
                    viewHolder.liveMet?.removeObserver(viewHolder.addressObserver!!)
                }
            }

            viewHolder.addressObserver = addressObserver
            viewHolder.liveMet?.observeForever(addressObserver)
            viewHolder.location.visibility = View.INVISIBLE
        }
    }

    private fun showAddress(viewHolder: ViewHolderMeteorite, address: String?, meteorite: Meteorite) {
        val location = viewModel.getLocation()
        val finalAddress = address + if (location != null) {
            " - ${meteorite.getDistanceFrom(location.latitude, location.longitude)}"
        } else {
            ""
        }
        viewHolder.location.text = finalAddress
        viewHolder.location.visibility = View.VISIBLE
    }

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        return if (item != null) {
            (item.id.hashCode() + item.name.hashCode()).toLong()
        } else {
            super.getItemId(position)
        }
    }

}
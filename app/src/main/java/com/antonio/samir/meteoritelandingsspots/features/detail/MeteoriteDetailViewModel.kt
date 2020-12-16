package com.antonio.samir.meteoritelandingsspots.features.detail

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.antonio.samir.meteoritelandingsspots.R
import com.antonio.samir.meteoritelandingsspots.data.Result
import com.antonio.samir.meteoritelandingsspots.data.repository.MeteoriteRepository
import com.antonio.samir.meteoritelandingsspots.data.repository.model.Meteorite
import com.antonio.samir.meteoritelandingsspots.features.getLocationText
import com.antonio.samir.meteoritelandingsspots.features.yearString
import com.antonio.samir.meteoritelandingsspots.ui.extension.convertToNumberFormat
import com.antonio.samir.meteoritelandingsspots.util.GPSTrackerInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

@FlowPreview
@ExperimentalCoroutinesApi
class MeteoriteDetailViewModel(
        private val context: Context,
        private val meteoriteRepository: MeteoriteRepository,
        gpsTracker: GPSTrackerInterface
) : ViewModel() {

    private var currentMeteorite = ConflatedBroadcastChannel<String>()

    val location = gpsTracker.location

    val meteorite: LiveData<Result<MeteoriteView>> = currentMeteorite.asFlow()
            .flatMapLatest(meteoriteRepository::getMeteoriteById)
            .combine(location) { meteorite, location -> //Add location
                when (meteorite) {
                    is Result.Success -> Result.Success(getMeteoriteView(meteorite.data, location))
                    is Result.Error -> Result.Error(meteorite.exception)
                    is Result.InProgress -> Result.InProgress()
                }
            }
            .asLiveData()

    private fun getMeteoriteView(meteorite: Meteorite, location: Location?) = MeteoriteView(
            id = meteorite.id.toString(),
            name = meteorite.name,
            yearString = meteorite.yearString,
            address = meteorite.getLocationText(
                    context = context,
                    location = location
            ),
            recclass = meteorite.recclass,
            mass = meteorite.mass.convertToNumberFormat(context.getString(R.string.unkown)),
            reclat = meteorite.reclat?.toDouble() ?: 0.0,
            reclong = meteorite.reclong?.toDouble() ?: 0.0,
            hasAddress = !meteorite.address.isNullOrBlank()
    )

    fun loadMeteorite(meteoriteId: String) {
        currentMeteorite.offer(meteoriteId)
    }

    fun requestAddressUpdate(meteorite: MeteoriteView) {

    }

}

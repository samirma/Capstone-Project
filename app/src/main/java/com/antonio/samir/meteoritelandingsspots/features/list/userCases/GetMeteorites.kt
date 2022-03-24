package com.antonio.samir.meteoritelandingsspots.features.list.userCases

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.antonio.samir.meteoritelandingsspots.common.userCase.UserCaseBase
import com.antonio.samir.meteoritelandingsspots.data.local.MeteoriteLocalRepository
import com.antonio.samir.meteoritelandingsspots.features.list.MeteoriteItemView
import com.antonio.samir.meteoritelandingsspots.features.list.mapper.MeteoriteViewMapper
import com.antonio.samir.meteoritelandingsspots.util.GPSTrackerInterface
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
class GetMeteorites(
    private val meteoriteLocalRepository: MeteoriteLocalRepository,
    private val mapper: MeteoriteViewMapper,
    private val gpsTracker: GPSTrackerInterface,
) : UserCaseBase<String?, PagingData<MeteoriteItemView>>() {

    override fun action(input: String?) = flow {
        Log.i(TAG, "Searching $input")

        gpsTracker.reqeustLocation()

        gpsTracker.location.collect { location ->
            Log.i(TAG, "location $location")

            val pagingData: Flow<PagingData<MeteoriteItemView>> = Pager(
                PagingConfig(pageSize = PAGE_SIZE)
            ) {
                meteoriteLocalRepository.meteoriteOrdered(
                    filter = input,
                    longitude = location?.longitude,
                    latitude = location?.latitude,
                    limit = LIMIT
                )
            }.flow.map { pagingData ->
                pagingData.map {
                    mapper.map(
                        MeteoriteViewMapper.Input(
                            meteorite = it,
                            location = location
                        )
                    )
                }
            }

            emitAll(pagingData)

        }

    }

    companion object {
        private val TAG = GetMeteorites::class.java.simpleName
        const val PAGE_SIZE = 20
        const val LIMIT = 1000L
    }

}
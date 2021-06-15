package com.antonio.samir.meteoritelandingsspots.features.list

import android.location.Location
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import androidx.paging.*
import com.antonio.samir.meteoritelandingsspots.common.ResultOf
import com.antonio.samir.meteoritelandingsspots.common.ResultOf.Success
import com.antonio.samir.meteoritelandingsspots.data.repository.MeteoriteRepository
import com.antonio.samir.meteoritelandingsspots.features.list.MeteoriteListViewModel.ContentStatus.*
import com.antonio.samir.meteoritelandingsspots.features.list.userCases.GetMeteorites
import com.antonio.samir.meteoritelandingsspots.service.AddressServiceInterface
import com.antonio.samir.meteoritelandingsspots.util.DispatcherProvider
import com.antonio.samir.meteoritelandingsspots.util.GPSTrackerInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


/**
 * Layer responsible for manage the interactions between the activity and the services
 */
@FlowPreview
@ExperimentalCoroutinesApi
class MeteoriteListViewModel(
    private val stateHandle: SavedStateHandle,
    private val meteoriteRepository: MeteoriteRepository,
    private val gpsTracker: GPSTrackerInterface,
    private val addressService: AddressServiceInterface,
    private val dispatchers: DispatcherProvider,
    private val getMeteorites: GetMeteorites,
) : ViewModel() {

    var filter = ""

    private val currentFilter = ConflatedBroadcastChannel<String?>()

    private val meteorite = ConflatedBroadcastChannel<MeteoriteItemView?>(stateHandle[METEORITE])

    val selectedMeteorite = meteorite.asFlow().asLiveData()

    private val contentStatus = MutableLiveData<ContentStatus>(Loading)

    fun getNetworkLoadingStatus() = meteoriteRepository.loadDatabase().asLiveData()

    @VisibleForTesting
    val addressServiceControl = MutableLiveData(false)

    fun getContentStatus(): LiveData<ContentStatus> {
        return contentStatus.distinctUntilChanged().map { status ->
            val shouldResumeAddressService = when (status) {
                Loading -> false
                else -> true
            }
            addressServiceControl.postValue(shouldResumeAddressService)
            return@map status
        }
    }

    fun getRecoverAddressStatus() = Transformations.switchMap(addressServiceControl) {
        if (it) {
            addressService.recoveryAddress().asLiveData()
        } else {
            liveData<ResultOf<Float>> {
                emit(Success(COMPLETED))
            }
        }
    }.distinctUntilChanged()


    fun loadMeteorites(location: String? = null) {

        contentStatus.postValue(Loading)

        location?.let { this.filter = it }

        currentFilter.offer(location)

    }

    fun selectMeteorite(meteorite: MeteoriteItemView?) {
        stateHandle[METEORITE] = meteorite
        this.meteorite.offer(meteorite)
    }

    fun updateLocation() {
        viewModelScope.launch(dispatchers.default()) {
            gpsTracker.startLocationService()
        }
    }

    fun isAuthorizationRequested(): LiveData<Boolean> {
        return gpsTracker.needAuthorization.asLiveData()
    }

    fun getLocation(): LiveData<Location?> {
        return gpsTracker.location.asLiveData()
    }


    fun getMeteorites(): LiveData<PagingData<MeteoriteItemView>> {
        return currentFilter.asFlow()
            .flowOn(dispatchers.default())
            .combine<String?, Location?, Pair<String?, Location?>>(gpsTracker.location) { _, location ->
                Pair(this.filter, location)
            }
            .map {
                getMeteorites.execute(it)
            }.flatMapConcat {
                it
            }
            .cachedIn(viewModelScope)
            .asLiveData()
    }

    fun clearSelectedMeteorite() {
        selectMeteorite(null)
    }

    sealed class ContentStatus {
        object ShowContent : ContentStatus()
        object NoContent : ContentStatus()
        object Loading : ContentStatus()
    }

    companion object {
        const val COMPLETED = 100f
        const val METEORITE = "METEORITE"
    }

}

package com.antonio.samir.meteoritelandingsspots.features.list

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.antonio.samir.meteoritelandingsspots.R
import com.antonio.samir.meteoritelandingsspots.common.ResultOf
import com.antonio.samir.meteoritelandingsspots.common.userCase.IsDarkTheme
import com.antonio.samir.meteoritelandingsspots.features.list.userCases.*
import com.antonio.samir.meteoritelandingsspots.util.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


/**
 * Layer responsible for manage the interactions between the activity and the services
 */
@FlowPreview
@ExperimentalCoroutinesApi
class MeteoriteListViewModel(
    private val stateHandle: SavedStateHandle,
    private val startAddressRecover: StartAddressRecover,
    private val statusAddressRecover: StatusAddressRecover,
    private val fetchMeteoriteList: FetchMeteoriteList,
    private val dispatchers: DispatcherProvider,
    private val getMeteorites: GetMeteorites,
    private val setDarkMode: SetUITheme,
    isDarkTheme: IsDarkTheme,
) : ViewModel() {

    private val meteorite = MutableStateFlow<MeteoriteItemView?>(stateHandle[METEORITE])

    private var isDarkMode = true

    val selectedMeteorite = meteorite.asLiveData()

    private val debounceState = MutableStateFlow<HeaderState?>(null)

    private val _meteorites =
        MutableStateFlow<PagingData<MeteoriteItemView>>(PagingData.empty())
    val meteorites = _meteorites

    private val viewModelState = MutableStateFlow(
        UiState(
            isLoading = true,
            addressStatus = ResultOf.InProgress(0f),
            meteorites = meteorites,
            onDarkModeToggleClick = {
                onDarkModeToggleClick()
            },
            isDark = isDarkTheme(Unit).stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = false
            )
        )
    )


    // UI state exposed to the UI
    val uiState: StateFlow<UiState> = viewModelState

    private fun onDarkModeToggleClick() {
        isDarkMode = !isDarkMode
//        viewModelState.update { it.copy(isDark = isDarkMode) }
        viewModelScope.launch {
            setDarkMode(SetUITheme.Input(isDarkMode)).collect()
        }
    }

    init {
        fetchMeteoriteList()

        viewModelScope.launch(dispatchers.default()) {
            debounceState.debounce(DEBOUNCE.toLong()).collect { headerState ->
                if (headerState != null) {
                    viewModelState.update {
                        it.copy(
                            headerState = headerState
                        )
                    }
                }
            }
        }

    }

    private fun fetchMeteoriteList() {

        viewModelScope.launch {
            fetchMeteoriteList(Unit).collect { resultOf ->
                viewModelState.update {
                    when (resultOf) {
                        is ResultOf.Error -> it.copy(
                            isLoading = false,
                            message = R.string.general_error
                        )
                        is ResultOf.InProgress -> it.copy(
                            isLoading = false,
                            message = R.string.general_error
                        )
                        is ResultOf.Success -> it.copy(isLoading = false, message = null)
                    }
                }
            }
        }

        viewModelScope.launch {
            recoverAddressStatus().collect { resultOf ->
                viewModelState.update {
                    it.copy(addressStatus = resultOf)
                }
            }
        }
    }

    private fun recoverAddressStatus(): Flow<ResultOf<Float>> =
        startAddressRecover(Unit).flatMapConcat { statusAddressRecover(it) }

    private val _searchQuery: MutableState<String?> = mutableStateOf(null)
    val searchQuery: State<String?> = _searchQuery

    fun searchLocation(query: String?, activity: AppCompatActivity) {
        _searchQuery.value = query
        viewModelScope.launch {
            getMeteorites(GetMeteorites.Input(query = query, activity = activity))
                .cachedIn(viewModelScope)
                .onEach { _meteorites.value = it }
                .collect {}
        }
    }

    fun selectMeteorite(meteorite: MeteoriteItemView?) {
        stateHandle[METEORITE] = meteorite
        this.meteorite.value = meteorite
    }

    fun clearSelectedMeteorite() {
        selectMeteorite(null)
    }

    fun onTopList(offset: Float) {
        if (debounceState.value != HeaderState.Search) {
            debounceState.value = if (offset > 0) {
                HeaderState.Expanded
            } else {
                HeaderState.Collapsed
            }
        }
    }

    fun setHeaderState(headerState: HeaderState) {
        debounceState.value = headerState
    }

    companion object {
        private val TAG = MeteoriteListViewModel::class.java.simpleName
        const val METEORITE = "METEORITE"
        const val DEBOUNCE = 200
    }

}

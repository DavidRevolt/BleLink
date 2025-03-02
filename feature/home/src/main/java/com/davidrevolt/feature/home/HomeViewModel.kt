package com.davidrevolt.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidrevolt.core.ble.BluetoothLeService
import com.davidrevolt.core.ble.model.CustomScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// todo: snackbars
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bluetoothLeService: BluetoothLeService,
) : ViewModel() {

    // Used to send msg to snackbar
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val homeUiState = combine(
        bluetoothLeService.isScanning(),
        bluetoothLeService.getScanResults()
    ) { isScanning, scanResults ->
        HomeUiState.Data(isScanning = isScanning, scanResults = scanResults)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )

    fun startBluetoothLeScan() {
        viewModelScope.launch {
            try {
                bluetoothLeService.startBluetoothLeScan()
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("${e.message}"))
            }
        }
    }

    fun stopBluetoothLeScan() {
        viewModelScope.launch {
            try {
                bluetoothLeService.stopBluetoothLeScan()
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("${e.message}"))
            }
        }
    }
}

sealed interface HomeUiState {
    data class Data(val isScanning: Boolean, val scanResults: List<CustomScanResult>) :
        HomeUiState

    data object Loading : HomeUiState
}

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}
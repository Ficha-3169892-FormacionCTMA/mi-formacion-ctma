package com.example.miformacionctma.ui.state

import com.example.miformacionctma.data.util.DataError

sealed interface RefreshUiState {
    object Idle : RefreshUiState
    object Running : RefreshUiState
    object Success : RefreshUiState
    data class Failed(val error: DataError.Network) : RefreshUiState
}

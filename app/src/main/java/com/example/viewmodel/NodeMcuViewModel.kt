package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ConnectionStatus
import com.example.model.DeviceStatusResponse
import com.example.model.HttpLogItem
import com.example.model.LedState
import com.example.network.HttpException
import com.example.network.NetworkException
import com.example.network.NodeMcuApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

data class NodeMcuUiState(
    val baseUrl: String = "http://nodemcu-led.local",
    val ledState: LedState = LedState.UNKNOWN,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val isPending: Boolean = false,
    val pendingActionText: String? = null,
    val lastPingMs: Long? = null,
    val lastSuccessfulUpdate: Long? = null,
    val lastRawResponse: String? = null,
    val lastErrorMessage: String? = null,
    val lastErrorDetail: String? = null,
    val uptimeSeconds: Long? = null,
    val rssi: Int? = null,
    val deviceIp: String? = null,
    val freeHeapBytes: Long? = null,
    val autoPoll: Boolean = false,
    val selectedTab: Int = 0,
    val recentUrls: List<String> = listOf(
        "http://nodemcu-led.local",
        "http://192.168.1.150",
        "http://192.168.4.1"
    ),
    val logs: List<HttpLogItem> = emptyList()
)

class NodeMcuViewModel(
    private val apiClient: NodeMcuApiClient = NodeMcuApiClient()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NodeMcuUiState())
    val uiState: StateFlow<NodeMcuUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

    init {
        // Query status on initial launch
        refreshStatus(silently = false)
    }

    fun setBaseUrl(newUrl: String) {
        val trimmed = newUrl.trim()
        _uiState.update { it.copy(baseUrl = trimmed) }
    }

    fun selectPresetUrl(url: String) {
        setBaseUrl(url)
        addRecentUrl(url)
        refreshStatus(silently = false)
    }

    fun setSelectedTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(lastErrorMessage = null, lastErrorDetail = null) }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    fun toggleAutoPoll() {
        val next = !_uiState.value.autoPoll
        _uiState.update { it.copy(autoPoll = next) }
        if (next) {
            startPolling()
        } else {
            stopPolling()
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                if (!_uiState.value.isPending) {
                    refreshStatus(silently = true)
                }
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    fun refreshStatus(silently: Boolean = false) {
        if (_uiState.value.isPending && !silently) return

        val targetUrl = _uiState.value.baseUrl
        viewModelScope.launch {
            if (!silently) {
                _uiState.update {
                    it.copy(
                        isPending = true,
                        pendingActionText = "Checking /api/status...",
                        connectionStatus = ConnectionStatus.CONNECTING
                    )
                }
            }

            val result = apiClient.getStatus(targetUrl)
            handleResult(
                result = result,
                method = "GET",
                endpoint = "/api/status",
                actionDescription = "Query Status"
            )
        }
    }

    fun turnLedOn() {
        if (_uiState.value.isPending) return
        val targetUrl = _uiState.value.baseUrl
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPending = true,
                    pendingActionText = "Sending POST /api/led/on...",
                    connectionStatus = ConnectionStatus.CONNECTING
                )
            }

            val result = apiClient.setLedOn(targetUrl)
            handleResult(
                result = result,
                method = "POST",
                endpoint = "/api/led/on",
                actionDescription = "Turn LED ON"
            )
        }
    }

    fun turnLedOff() {
        if (_uiState.value.isPending) return
        val targetUrl = _uiState.value.baseUrl
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPending = true,
                    pendingActionText = "Sending POST /api/led/off...",
                    connectionStatus = ConnectionStatus.CONNECTING
                )
            }

            val result = apiClient.setLedOff(targetUrl)
            handleResult(
                result = result,
                method = "POST",
                endpoint = "/api/led/off",
                actionDescription = "Turn LED OFF"
            )
        }
    }

    private fun handleResult(
        result: Result<Pair<DeviceStatusResponse, Long>>,
        method: String,
        endpoint: String,
        actionDescription: String
    ) {
        result.fold(
            onSuccess = { (response, durationMs) ->
                val newLedState = response.ledState
                _uiState.update { state ->
                    val updatedLogs = (listOf(
                        HttpLogItem(
                            id = UUID.randomUUID().toString(),
                            method = method,
                            endpoint = endpoint,
                            statusCode = 200,
                            durationMs = durationMs,
                            isSuccess = true,
                            responseBody = response.rawJson
                        )
                    ) + state.logs).take(50)

                    state.copy(
                        isPending = false,
                        pendingActionText = null,
                        ledState = if (newLedState != LedState.UNKNOWN) newLedState else state.ledState,
                        connectionStatus = ConnectionStatus.CONNECTED,
                        lastPingMs = durationMs,
                        lastSuccessfulUpdate = System.currentTimeMillis(),
                        lastRawResponse = response.rawJson,
                        lastErrorMessage = null,
                        lastErrorDetail = null,
                        uptimeSeconds = response.uptime ?: state.uptimeSeconds,
                        rssi = response.rssi ?: state.rssi,
                        deviceIp = response.ip ?: state.deviceIp,
                        freeHeapBytes = response.heap ?: state.freeHeapBytes,
                        logs = updatedLogs
                    )
                }
                addRecentUrl(_uiState.value.baseUrl)
            },
            onFailure = { error ->
                val (statusCode, errorMsg, detail, duration) = when (error) {
                    is HttpException -> Quad(error.statusCode, error.message, error.responseBody, error.durationMs)
                    is NetworkException -> Quad(null, error.message, error.cause?.toString(), error.durationMs)
                    else -> Quad(null, error.localizedMessage ?: "Unknown network failure", null, null)
                }

                _uiState.update { state ->
                    val updatedLogs = (listOf(
                        HttpLogItem(
                            id = UUID.randomUUID().toString(),
                            method = method,
                            endpoint = endpoint,
                            statusCode = statusCode,
                            durationMs = duration,
                            isSuccess = false,
                            errorMessage = errorMsg,
                            responseBody = detail
                        )
                    ) + state.logs).take(50)

                    state.copy(
                        isPending = false,
                        pendingActionText = null,
                        connectionStatus = ConnectionStatus.ERROR,
                        lastErrorMessage = "$actionDescription Failed: $errorMsg",
                        lastErrorDetail = detail,
                        logs = updatedLogs
                    )
                }
            }
        )
    }

    private fun addRecentUrl(url: String) {
        val trimmed = apiClient.normalizeUrl(url)
        _uiState.update { state ->
            val set = (listOf(trimmed) + state.recentUrls).distinct().take(5)
            state.copy(recentUrls = set)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

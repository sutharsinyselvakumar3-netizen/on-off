package com.example.model

enum class LedState {
    ON,
    OFF,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): LedState {
            return when (value?.trim()?.uppercase()) {
                "ON", "1", "TRUE" -> ON
                "OFF", "0", "FALSE" -> OFF
                else -> UNKNOWN
            }
        }
    }
}

enum class ConnectionStatus {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    ERROR
}

data class DeviceStatusResponse(
    val status: String? = null,
    val led: String? = null,
    val gpio: Int? = 2,
    val uptime: Long? = null,
    val rssi: Int? = null,
    val ip: String? = null,
    val heap: Long? = null,
    val message: String? = null,
    val rawJson: String? = null
) {
    val ledState: LedState
        get() = LedState.fromString(led)
}

data class HttpLogItem(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val method: String,
    val endpoint: String,
    val statusCode: Int? = null,
    val durationMs: Long? = null,
    val isSuccess: Boolean,
    val responseBody: String? = null,
    val errorMessage: String? = null
)

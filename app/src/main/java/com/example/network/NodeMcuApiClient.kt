package com.example.network

import com.example.model.DeviceStatusResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class NodeMcuApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .writeTimeout(4, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
) {

    fun normalizeUrl(input: String): String {
        var trimmed = input.trim()
        if (trimmed.isEmpty()) {
            trimmed = "http://nodemcu-led.local"
        }
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "http://$trimmed"
        }
        return trimmed.trimEnd('/')
    }

    suspend fun getStatus(baseUrl: String): Result<Pair<DeviceStatusResponse, Long>> =
        withContext(Dispatchers.IO) {
            val normalized = normalizeUrl(baseUrl)
            val url = "$normalized/api/status"
            executeRequest(
                Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Accept", "application/json")
                    .build(),
                url
            )
        }

    suspend fun setLedOn(baseUrl: String): Result<Pair<DeviceStatusResponse, Long>> =
        withContext(Dispatchers.IO) {
            val normalized = normalizeUrl(baseUrl)
            val url = "$normalized/api/led/on"
            val emptyBody = "".toRequestBody("application/json".toMediaType())
            executeRequest(
                Request.Builder()
                    .url(url)
                    .post(emptyBody)
                    .addHeader("Accept", "application/json")
                    .build(),
                url
            )
        }

    suspend fun setLedOff(baseUrl: String): Result<Pair<DeviceStatusResponse, Long>> =
        withContext(Dispatchers.IO) {
            val normalized = normalizeUrl(baseUrl)
            val url = "$normalized/api/led/off"
            val emptyBody = "".toRequestBody("application/json".toMediaType())
            executeRequest(
                Request.Builder()
                    .url(url)
                    .post(emptyBody)
                    .addHeader("Accept", "application/json")
                    .build(),
                url
            )
        }

    private fun executeRequest(
        request: Request,
        fullUrl: String
    ): Result<Pair<DeviceStatusResponse, Long>> {
        val startTime = System.currentTimeMillis()
        return try {
            client.newCall(request).execute().use { response ->
                val duration = System.currentTimeMillis() - startTime
                val bodyString = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val code = response.code
                    val errorMsg = when (code) {
                        404 -> "Endpoint not found (HTTP 404). Check that ESP8266 sketch defines ${request.url.encodedPath}"
                        405 -> "Method Not Allowed (HTTP 405). Expected ${request.method}"
                        500 -> "NodeMCU Internal Server Error (HTTP 500)"
                        else -> "HTTP $code: ${response.message.ifBlank { bodyString.take(100) }}"
                    }
                    return Result.failure(HttpException(code, errorMsg, bodyString, duration))
                }

                val parsed = parseJsonResponse(bodyString)
                Result.success(Pair(parsed, duration))
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            val friendlyMsg = mapExceptionToMessage(e, fullUrl)
            Result.failure(NetworkException(friendlyMsg, duration, e))
        }
    }

    private fun parseJsonResponse(rawJson: String): DeviceStatusResponse {
        return try {
            val trimmed = rawJson.trim()
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                val json = JSONObject(trimmed)
                DeviceStatusResponse(
                    status = json.optString("status", "ok"),
                    led = json.optString("led", "").ifEmpty {
                        if (json.has("state")) json.optString("state", "") else ""
                    },
                    gpio = if (json.has("gpio")) json.optInt("gpio", 2) else 2,
                    uptime = if (json.has("uptime")) json.optLong("uptime", 0L) else null,
                    rssi = if (json.has("rssi")) json.optInt("rssi", 0) else null,
                    ip = json.optString("ip", "").ifEmpty { null },
                    heap = if (json.has("heap")) json.optLong("heap", 0L) else null,
                    message = json.optString("message", "").ifEmpty { null },
                    rawJson = rawJson
                )
            } else {
                // In case firmware returned plain text like "ON" or "OFF"
                val inferredLed = when (trimmed.uppercase()) {
                    "ON", "LED IS ON" -> "ON"
                    "OFF", "LED IS OFF" -> "OFF"
                    else -> "UNKNOWN"
                }
                DeviceStatusResponse(
                    status = "ok",
                    led = inferredLed,
                    message = trimmed,
                    rawJson = rawJson
                )
            }
        } catch (e: Exception) {
            DeviceStatusResponse(
                status = "unknown",
                led = "UNKNOWN",
                message = "Failed to parse JSON: ${e.message}",
                rawJson = rawJson
            )
        }
    }

    private fun mapExceptionToMessage(e: Exception, fullUrl: String): String {
        return when (e) {
            is UnknownHostException ->
                "Could not resolve host in '$fullUrl'. Check that NodeMCU is powered on and connected to the same Wi-Fi. If mDNS (.local) fails on your network, use the NodeMCU's numeric IP address (e.g. 192.168.1.150)."

            is SocketTimeoutException ->
                "Connection timed out (4s). The NodeMCU at '$fullUrl' did not respond. Check power, Wi-Fi connection, or local IP address."

            is ConnectException ->
                "Connection refused. No active web server responded at '$fullUrl' on port 80. Ensure the Arduino sketch is running."

            is IOException ->
                "Network error: ${e.message ?: "Failed to connect to device"}"

            else ->
                e.localizedMessage ?: "Unexpected error connecting to NodeMCU"
        }
    }
}

class HttpException(
    val statusCode: Int,
    override val message: String,
    val responseBody: String,
    val durationMs: Long
) : Exception(message)

class NetworkException(
    override val message: String,
    val durationMs: Long,
    override val cause: Throwable?
) : Exception(message, cause)

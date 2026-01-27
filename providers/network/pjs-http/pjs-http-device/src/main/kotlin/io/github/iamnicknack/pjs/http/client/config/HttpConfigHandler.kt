package io.github.iamnicknack.pjs.http.client.config

import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.model.device.Device
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * A [io.github.iamnicknack.pjs.http.config.ConfigHandler] implementation that uses HTTP to communicate with the device provider.
 * @param httpClient the HTTP client to use for communication
 * @param deviceTypeUrlComponent the URL component to use for device types
 */
class HttpConfigHandler<T : Device<T>>(
    private val httpClient: HttpClient,
    private val deviceTypeUrlComponent: String,
    private val configAsBody: suspend HttpRequestBuilder.(ConfigHandler.DeviceConfigPayload<T>) -> Unit,
    private val bodyAsConfig: suspend HttpResponse.() -> ConfigHandler.DeviceConfigPayload<T>,
) : ConfigHandler<T> {

    override suspend fun createDevice(
        deviceId: String,
        config: ConfigHandler.DeviceConfigPayload<T>
    ): DeviceConfig<T> {
        return httpClient
            .post("/api/v1/$deviceTypeUrlComponent/$deviceId") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                configAsBody(config)
            }
            .bodyAsConfig()
            .asDeviceConfig(deviceId)
    }

    override suspend fun removeDevice(deviceId: String) {
        httpClient
            .delete("/api/v1/$deviceTypeUrlComponent/$deviceId")    }

    override suspend fun getDevice(deviceId: String): DeviceConfig<T> {
        return httpClient
            .get("/api/v1/$deviceTypeUrlComponent/$deviceId") {
                accept(ContentType.Application.Json)
            }
            .bodyAsConfig()
            .asDeviceConfig(deviceId)
    }
}
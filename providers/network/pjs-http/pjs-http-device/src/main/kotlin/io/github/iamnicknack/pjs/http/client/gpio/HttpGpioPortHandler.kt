package io.github.iamnicknack.pjs.http.client.gpio

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.http.client.config.HttpConfigHandler
import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.http.gpio.GpioPortHandler
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.github.iamnicknack.pjs.model.event.GpioChangeEvent
import io.github.iamnicknack.pjs.model.event.GpioEventListener
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HttpGpioPortHandler(
    private val httpClient: HttpClient,
    private val deviceRegistry: DeviceRegistry,
    private val configHandler: ConfigHandler<GpioPort> = HttpConfigHandler(
        httpClient, "gpio",
        { setBody(it) },
        { body<GpioPortHandler.GpioPortConfigPayload>() }
    )
) : GpioPortClientHandler, ConfigHandler<GpioPort> by configHandler {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val jobsMap: MutableMap<String, Job> = mutableMapOf()

    override suspend fun readDevice(deviceId: String): Int {
        return httpClient
            .get("/api/v1/gpio/$deviceId/value") {
                accept(ContentType.Application.Json)
            }
            .body<Int>()
    }

    override suspend fun writeDevice(deviceId: String, value: Int) {
        httpClient.put("/api/v1/gpio/$deviceId/value/$value")
    }

    override suspend fun listen(deviceId: String, listener: GpioEventListener<GpioPort>) {
        if (jobsMap[deviceId]?.isActive == true) return

        // Don't return until a connected event is received
        val completer = CompletableDeferred<Unit>()

        jobsMap[deviceId] = CoroutineScope(Dispatchers.IO)
            .launch {
                httpClient
                    .sse("/api/v1/gpio/$deviceId/events") {
                        val port = deviceRegistry.device(deviceId, GpioPort::class.java)
                            ?: return@sse

                        incoming
                            .collect { sseEvent ->
                                val sseEventType = sseEvent.event
                                    ?.let { GpioPortHandler.SseEventType.valueOf(it) }
                                    ?: GpioPortHandler.SseEventType.NONE

                                if (sseEventType == GpioPortHandler.SseEventType.CONNECTED) {
                                    logger.debug("Received GPIO event for port {}: {}", port.config.id, sseEventType)
                                    completer.complete(Unit)
                                    return@collect
                                }

                                listener.onEvent(GpioChangeEvent(port, sseEventType.gpioEvent))
                                    .also { logger.debug("Forwarded GPIO event for port {}: {}", port.config.id, sseEventType) }
                            }
                    }
            }

        withTimeoutOrNull(1000) { completer.await() }
    }

    override suspend fun unlisten(deviceId: String) {
        if (jobsMap[deviceId]?.isActive == true) {
            jobsMap[deviceId]?.cancel()
            httpClient.delete("/api/v1/gpio/$deviceId/events")
        }
    }
}
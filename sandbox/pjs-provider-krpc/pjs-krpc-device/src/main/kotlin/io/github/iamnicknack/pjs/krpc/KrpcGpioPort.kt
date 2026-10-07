package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.grpc.gen.v1.port.EventMode
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortConfigService
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortService
import io.github.iamnicknack.pjs.grpc.gen.v1.port.StateChangeEvent
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.invoke
import io.github.iamnicknack.pjs.krpc.proto.asIntegerRequest
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.github.iamnicknack.pjs.model.event.GpioChangeEvent
import io.github.iamnicknack.pjs.model.event.GpioChangeEventType
import io.github.iamnicknack.pjs.model.event.GpioEventListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.milliseconds

class KrpcGpioPort(
    private val config: GpioPortConfig,
    private val portService: PortService,
    private val portConfigService: PortConfigService
) : GpioPort {
    private val logger: Logger = LoggerFactory.getLogger(KrpcGpioPort::class.java)

    private var listenerJob: Job? = null
    private val listeners: MutableList<GpioEventListener<GpioPort>> = mutableListOf()

    override fun read(): Int = runBlocking {
        portService.read(config.asDeviceRequest()).value
    }

    override fun write(value: Int): Unit = runBlocking {
        portService.write(config.asIntegerRequest(value))
    }

    override fun addListener(listener: GpioEventListener<GpioPort>): Unit = runBlocking {
        listeners.add(listener)
        if (listenerJob == null) {
            val completer = CompletableDeferred<Unit>()
            listenerJob = CoroutineScope(Dispatchers.IO)
                .launch {
                    portService.addListener(config.asDeviceRequest())
                        .collect { event ->
                            event.eventMode
                            listeners.forEach { it.onEvent(event.asGpioEvent()) }
                        }
                }
            // allow one second for the server to respond
            withTimeoutOrNull(1000.milliseconds) { completer.await() }
        }
    }

    override fun removeListener(listener: GpioEventListener<GpioPort>) {
        listeners.remove(listener)
        if (listeners.isEmpty() && listenerJob != null) {
            listenerJob?.cancel()
            listenerJob = null
        }
    }

    override fun getConfig(): DeviceConfig<GpioPort> = config

    override fun close(): Unit = runBlocking {
        portConfigService.remove(config.asDeviceRequest())
        listenerJob?.cancel()
        listenerJob = null
    }

    fun GpioPortConfig.asDeviceRequest(): DeviceRequest = DeviceRequest {
        deviceId = id
    }

    fun StateChangeEvent.asGpioEvent(): GpioChangeEvent<GpioPort> = GpioChangeEvent(
        this@KrpcGpioPort,
        when(this.eventMode) {
            EventMode.FALLING -> GpioChangeEventType.FALLING
            EventMode.RISING -> GpioChangeEventType.RISING
            else -> GpioChangeEventType.NONE
        }
    )
}
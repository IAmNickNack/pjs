package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.krpc.gen.v1.port.EventMode
import io.github.iamnicknack.pjs.krpc.gen.v1.port.PortConfigService
import io.github.iamnicknack.pjs.krpc.gen.v1.port.PortService
import io.github.iamnicknack.pjs.krpc.gen.v1.port.RemoveListenerRequest
import io.github.iamnicknack.pjs.krpc.gen.v1.port.StateChangeEvent
import io.github.iamnicknack.pjs.krpc.gen.v1.port.invoke
import io.github.iamnicknack.pjs.krpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.krpc.gen.v1.types.invoke
import io.github.iamnicknack.pjs.krpc.proto.asIntegerRequest
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.github.iamnicknack.pjs.model.event.GpioChangeEvent
import io.github.iamnicknack.pjs.model.event.GpioChangeEventType
import io.github.iamnicknack.pjs.model.event.GpioEventEmitterDelegate
import io.github.iamnicknack.pjs.model.event.GpioEventListener
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.milliseconds

class KrpcGpioPort(
    private val config: GpioPortConfig,
    private val portService: PortService,
    private val portConfigService: PortConfigService,
    private val listenerDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GpioPort {
    private val logger: Logger = LoggerFactory.getLogger(KrpcGpioPort::class.java)

    private val eventsDelegate: GpioEventEmitterDelegate<GpioPort> = GpioEventEmitterDelegate()
    private val stateChangeCollector = StateChangeCollector()

    override fun read(): Int = runBlocking {
        portService.read(config.asDeviceRequest()).value
    }

    override fun write(value: Int): Unit = runBlocking {
        portService.write(config.asIntegerRequest(value))
    }

    override fun addListener(listener: GpioEventListener<GpioPort>): Unit = runBlocking {
        logger.info("Adding listener to device ${config.id}")
        eventsDelegate.addListener(listener)
        if (!stateChangeCollector.isConnected) {
            stateChangeCollector.connect()
        }
    }

    override fun removeListener(listener: GpioEventListener<GpioPort>) {
        logger.info("Removing listener from port ${config.id}")
        eventsDelegate.removeListener(listener)
        if (eventsDelegate.listenerCount == 0) {
            stateChangeCollector.close()
        }
    }

    override fun getConfig(): DeviceConfig<GpioPort> = config

    override fun close(): Unit = runBlocking {
        portConfigService.remove(config.asDeviceRequest())
        stateChangeCollector.close()
    }

    fun GpioPortConfig.asDeviceRequest(): DeviceRequest = DeviceRequest {
        deviceId = id
    }


    private inner class StateChangeCollector : AutoCloseable {
        private val logger: Logger = LoggerFactory.getLogger(StateChangeCollector::class.java)

        @Volatile
        private var listenerId: String? = null

        private var listenerJob: Job? = null

        val isConnected: Boolean
            get() = listenerJob != null

        fun connect() {
            if (listenerJob != null) return

            val connectionRendezvous: Channel<Unit> = Channel(Channel.RENDEZVOUS)

            listenerJob = CoroutineScope(listenerDispatcher)
                .launch {
                    portService.addListener(config.asDeviceRequest())
                        .catch { exception ->
                            when (exception) {
                                is StatusException -> exception.status.description?.let { logger.error(it) }
                                is StatusRuntimeException -> exception.status.description?.let { logger.error(it) }
                                else -> logger.warn("Non-status exception: ${exception.message}")
                            }
                        }
                        .collect { event ->
                            if (event.eventMode == EventMode.NONE) {
                                listenerId = event.listenerId
                                connectionRendezvous.send(Unit)
                            } else {
                                eventsDelegate.onEvent(event.asGpioEvent())
                            }
                        }
                }

            runBlocking {
                withTimeoutOrNull(500.milliseconds) { connectionRendezvous.receive() }
                    ?: logger.warn("Timeout waiting for connection event on device: {}", config.id)
            }
        }

        override fun close() {
            this.listenerId?.also {
                runBlocking {
                    val request = RemoveListenerRequest.invoke {
                        this.listenerId = it
                    }
                    portService.removeListener(request)
                }
            }
            listenerJob?.cancel()
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
}
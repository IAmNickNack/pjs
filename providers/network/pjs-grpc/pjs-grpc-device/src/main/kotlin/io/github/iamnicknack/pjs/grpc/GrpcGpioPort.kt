package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.grpc.gen.v1.port.EventType
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortConfigServiceGrpc
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortServiceGrpc
import io.github.iamnicknack.pjs.grpc.gen.v1.port.RemoveListenerRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.port.StateChangeEvent
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.github.iamnicknack.pjs.model.event.GpioChangeEvent
import io.github.iamnicknack.pjs.model.event.GpioChangeEventType
import io.github.iamnicknack.pjs.model.event.GpioEventEmitterDelegate
import io.github.iamnicknack.pjs.model.event.GpioEventListener
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GrpcGpioPort(
    private val config: GpioPortConfig,
    private val stub : PortServiceGrpc.PortServiceBlockingStub,
    private val configStub : PortConfigServiceGrpc.PortConfigServiceBlockingStub
) : GpioPort {

    private val logger: Logger = LoggerFactory.getLogger(GrpcGpioPort::class.java)

    /**
     * Delegate used to manage listeners
     */
    private val eventsDelegate: GpioEventEmitterDelegate<GpioPort> = GpioEventEmitterDelegate()
    /**
     * Container used to listen for and forward events from the remote device
     */
    private val observer: StateChangeObserver = StateChangeObserver(this, eventsDelegate)

    override fun read(): Int {
        return stub.read(this.config.asDeviceRequest()).value
    }

    override fun write(value: Int) {
        stub.write(this.config.asIntegerRequest(value))
    }

    override fun getConfig(): DeviceConfig<GpioPort> {
        return this.config
    }

    override fun addListener(listener: GpioEventListener<GpioPort>) {
        logger.info("Add listener for device: {}", config.id)
        if (eventsDelegate.listenerCount == 0) {
            eventsDelegate.addListener(listener)
            runBlocking { observer.connect() }
        } else {
            eventsDelegate.addListener(listener)
        }
    }

    override fun removeListener(listener: GpioEventListener<GpioPort>) {
        logger.info("Remove listener for device: {}", config.id)
        eventsDelegate.removeListener(listener)

        if (eventsDelegate.listenerCount == 0 && observer.listenerId != null) {
            logger.info("Unsubscribing from remote device: {}", config.id)
            stub.removeListener(
                RemoveListenerRequest.newBuilder()
                    .setListenerId(observer.listenerId)
                    .build()
            )
            observer.listenerId = null
        }
    }

    override fun close() {
        if (observer.listenerId != null) {
            stub.removeListener(
                RemoveListenerRequest.newBuilder()
                    .setListenerId(observer.listenerId)
                    .build()
            )
            observer.listenerId = null
        }
        eventsDelegate.close()
        configStub.remove(config.asDeviceRequest())
    }

    /**
     * [StreamObserver] implementation used to receive events from the remote device.
     */
    private inner class StateChangeObserver(
        val device: GpioPort,
        val eventListener: GpioEventListener<GpioPort>,
        @Volatile
        var listenerId: String? = null,
    ) : StreamObserver<StateChangeEvent> {

        private val logger: Logger = LoggerFactory.getLogger(StateChangeObserver::class.java)

        /**
         * Callback stub used to handle events
         */
        private val listenerStub: PortServiceGrpc.PortServiceStub = PortServiceGrpc.newStub(stub.channel)

        /**
         * Latch to indicate connected status (completed or not completed)
         */
        private val connectionRendezvous: Channel<Unit> = Channel(Channel.RENDEZVOUS)

        /**
         * Connects the observer to the remote device and waits for the first event.
         */
        suspend fun connect() {
            listenerStub.addListener(
                config.asDeviceRequest(),
                this
            )
            withTimeoutOrNull(500) { connectionRendezvous.receive()}
                ?: logger.warn("Timeout waiting for connection event on device: {}", device.config.id)
        }

        override fun onNext(value: StateChangeEvent) {
            if (logger.isDebugEnabled) {
                logger.debug(
                    "Received event: {}, {}, {}, {}",
                    value.deviceId,
                    value.listenerId,
                    value.eventType,
                    value.value
                )
            }
            // Using NONE to indicate connection status.
            // This should maybe have a specific event type
            if (value.eventType == EventType.NONE) {
                listenerId = value.listenerId
                connectionRendezvous.trySend(Unit)
            } else {
                val event = GpioChangeEvent(device, value.eventType.asGpioChangeEventType())
                eventListener.onEvent(event)
            }
        }

        override fun onError(t: Throwable) {
            when (t) {
                is StatusException -> t.status.description?.let { logger.error(it) }
                is StatusRuntimeException -> t.status.description?.let { logger.warn(it) }
                else -> logger.warn("Non-status exception on device: {}, {}", t.message, device.config.id)
            }
        }

        override fun onCompleted() {
            logger.info("Event stream completed on device: {}", device.config.id)
        }

        private fun EventType.asGpioChangeEventType(): GpioChangeEventType {
            return when(this) {
                EventType.RISING -> GpioChangeEventType.RISING
                EventType.FALLING -> GpioChangeEventType.FALLING
                else -> GpioChangeEventType.NONE
            }
        }
    }
}
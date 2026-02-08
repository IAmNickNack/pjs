package io.github.iamnicknack.pjs.grpc.service

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode
import io.github.iamnicknack.pjs.grpc.deviceOrThrow
import io.github.iamnicknack.pjs.grpc.gen.v1.port.Empty
import io.github.iamnicknack.pjs.grpc.gen.v1.port.EventMode
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortServiceGrpcKt
import io.github.iamnicknack.pjs.grpc.gen.v1.port.RemoveListenerRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.port.StateChangeEvent
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.IntegerRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.IntegerResponse
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.github.iamnicknack.pjs.model.event.GpioChangeEventType
import io.github.iamnicknack.pjs.model.event.GpioEventListener
import io.grpc.Status
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class GrpcPortService(
    private val deviceRegistry: DeviceRegistry
) : PortServiceGrpcKt.PortServiceCoroutineImplBase() {

    private val logger: Logger = LoggerFactory.getLogger(GrpcPortService::class.java)
    private val listeners: ConcurrentMap<String, DeviceListener> = ConcurrentHashMap()

    override suspend fun read(request: DeviceRequest): IntegerResponse {
        val value = deviceRegistry.deviceOrThrow<GpioPort>(request.deviceId)
            .read()

        return IntegerResponse.newBuilder()
            .setValue(value)
            .build()
    }

    override suspend fun write(request: IntegerRequest): Empty {
        deviceRegistry.deviceOrThrow<GpioPort>(request.deviceId)
            .write(request.value)

        return Empty.getDefaultInstance()
    }

    override fun addListener(request: DeviceRequest): Flow<StateChangeEvent> {
        // should only do this if port is input
        val device = deviceRegistry.deviceOrThrow<GpioPort>(request.deviceId)
            .takeIf { (it.config as GpioPortConfig).portMode.isSet(GpioPortMode.INPUT) }
            ?: throw Status.INVALID_ARGUMENT
                .withDescription("Can only listen to input ports")
                .asRuntimeException()

        return callbackFlow {
            val listenerId = UUID.randomUUID().toString()
            val listener = GpioEventListener<GpioPort> { event ->
                if (logger.isDebugEnabled) {
                    logger.debug("Digital input state changed: {} -> {}", event.port.config.id, event.eventType)
                }

                val changeEvent = StateChangeEvent.newBuilder()
                    .setDeviceId(request.deviceId)
                    .setListenerId(listenerId)
                    .setEventMode(event.eventType.asEventType())
                    .setValue(event.port.read())
                    .build()

                trySend(changeEvent)
            }
            listeners[listenerId] = DeviceListener(device, listener, this)
            device.addListener(listener)

            logger.info("Added listener for device: {} -> {}", request.deviceId, listenerId)

            val registerEvent = StateChangeEvent.newBuilder()
                .setDeviceId(request.deviceId)
                .setListenerId(listenerId)
                .setEventMode(GpioChangeEventType.NONE.asEventType())
                .setValue(0)
                .build()

            // send the initial state change event
            trySend(registerEvent)

            // should add hook to remove the listener when the device is closed
            awaitClose { device.removeListener(listener) }
        }
    }

    override suspend fun removeListener(request: RemoveListenerRequest): Empty {
        listeners[request.listenerId]
            ?.also {
                it.eventProducer.close()
                it.device.removeListener(it.listener)
                listeners.remove(request.listenerId)

                logger.info("Removed listener for device: {} -> {}", it.device.config.id, request.listenerId)
            }
        return Empty.getDefaultInstance()
    }

    /**
     * Map the API event type to gRPC event type.
     */
    private fun GpioChangeEventType.asEventType(): EventMode {
        return when(this) {
            GpioChangeEventType.RISING -> EventMode.RISING
            GpioChangeEventType.FALLING -> EventMode.FALLING
            else -> EventMode.NONE
        }
    }

    /**
     * Associated pair of device and event-listener.
     * @param device the device
     * @param listener the event-listener
     */
    class DeviceListener(
        val device: GpioPort,
        val listener: GpioEventListener<GpioPort>,
        val eventProducer: ProducerScope<StateChangeEvent>
    )
}
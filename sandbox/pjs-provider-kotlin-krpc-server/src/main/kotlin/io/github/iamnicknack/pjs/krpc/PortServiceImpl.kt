package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode
import io.github.iamnicknack.pjs.grpc.gen.v1.port.EventMode
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortModePayload
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortService
import io.github.iamnicknack.pjs.grpc.gen.v1.port.RemoveListenerRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.port.StateChangeEvent
import io.github.iamnicknack.pjs.grpc.gen.v1.port.invoke
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.Empty
import io.github.iamnicknack.pjs.grpc.gen.v1.types.IntegerRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.IntegerResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.types.invoke
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

class PortServiceImpl(
    private val deviceRegistry: DeviceRegistry
) : PortService {

    private val logger: Logger = LoggerFactory.getLogger(PortServiceImpl::class.java)
    private val listeners: ConcurrentMap<String, DeviceListener> = ConcurrentHashMap()

    override suspend fun read(message: DeviceRequest): IntegerResponse {
        return deviceRegistry.withDevice<GpioPort, IntegerResponse>(message.deviceId) {
            IntegerResponse {
                value = read()
            }
        }
    }

    override suspend fun write(message: IntegerRequest): Empty {
        return deviceRegistry.withDevice<GpioPort, Empty>(message.deviceId) {
            write(message.value)
            Empty {  }
        }
    }

    override suspend fun setPortMode(message: PortModePayload): Empty {
        return deviceRegistry.withDevice<GpioPort, Empty>(message.deviceId) {
            setDirection(message.portMode.asGpioPortMode())
            Empty {}
        }
    }

    override fun addListener(message: DeviceRequest): Flow<StateChangeEvent> {
        // should only do this if port is input
        val device = deviceRegistry.deviceOrThrow<GpioPort>(message.deviceId)
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

                val changeEvent = StateChangeEvent {
                    this.deviceId = message.deviceId
                    this.listenerId = listenerId
                    this.eventMode = when(event.eventType) {
                        GpioChangeEventType.RISING -> EventMode.RISING
                        GpioChangeEventType.FALLING -> EventMode.FALLING
                        else -> EventMode.NONE
                    }
                    this.value = event.port.read()
                }

                trySend(changeEvent)
            }

            listeners[listenerId] = DeviceListener(device, listener, this)
            device.addListener(listener)

            logger.info("Added listener for device: {} -> {}", message.deviceId, listenerId)

            val registerEvent = StateChangeEvent {
                this.deviceId = message.deviceId
                this.listenerId = listenerId
                this.eventMode = EventMode.NONE
                this.value = 0
            }

            // send the initial state change event
            trySend(registerEvent)

            // should add hook to remove the listener when the device is closed
            awaitClose { device.removeListener(listener) }
        }
    }

    override suspend fun removeListener(message: RemoveListenerRequest): Empty {
        listeners[message.listenerId]
            ?.also {
                it.eventProducer.close()
                it.device.removeListener(it.listener)
                listeners.remove(message.listenerId)

                logger.info("Removed listener for device: {} -> {}", it.device.config.id, message.listenerId)
            }
        return Empty {}
    }

    /**
     * Associated pair of device and event-listener.
     * @param device the device
     * @param listener the event listener
     * @param eventProducer the event producer
     */
    private class DeviceListener(
        val device: GpioPort,
        val listener: GpioEventListener<GpioPort>,
        val eventProducer: ProducerScope<StateChangeEvent>
    )
}
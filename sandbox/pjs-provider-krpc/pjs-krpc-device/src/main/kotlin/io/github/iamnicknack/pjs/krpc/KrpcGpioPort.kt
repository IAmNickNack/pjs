package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortConfigService
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortService
import io.github.iamnicknack.pjs.krpc.proto.asDeviceRequest
import io.github.iamnicknack.pjs.krpc.proto.asIntegerRequest
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.github.iamnicknack.pjs.model.event.GpioEventListener
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KrpcGpioPort(
    private val config: GpioPortConfig,
    private val portService: PortService,
    private val portConfigService: PortConfigService
) : GpioPort {
    private val logger: Logger = LoggerFactory.getLogger(KrpcGpioPort::class.java)

    override fun read(): Int = runBlocking {
        portService.read(config.asDeviceRequest()).value
    }

    override fun write(value: Int): Unit = runBlocking {
        portService.write(config.asIntegerRequest(value))
    }

    override fun addListener(listener: GpioEventListener<GpioPort>) {
        TODO("Not yet implemented")
    }

    override fun removeListener(listener: GpioEventListener<GpioPort>) {
        TODO("Not yet implemented")
    }

    override fun getConfig(): DeviceConfig<GpioPort> = config

    override fun close(): Unit = runBlocking {
        portConfigService.remove(config.asDeviceRequest())
    }
}
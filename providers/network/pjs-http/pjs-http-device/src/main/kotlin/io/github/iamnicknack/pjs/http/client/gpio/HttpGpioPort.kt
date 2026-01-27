package io.github.iamnicknack.pjs.http.client.gpio

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.github.iamnicknack.pjs.model.event.GpioEventListener
import kotlinx.coroutines.runBlocking

class HttpGpioPort(
    private val handler: GpioPortClientHandler,
    private val config: GpioPortConfig
) : GpioPort {

    override fun read(): Int = runBlocking {
        handler.readDevice(config.id)
    }

    override fun write(value: Int) = runBlocking {
        handler.writeDevice(config.id, value)
    }

    override fun addListener(listener: GpioEventListener<GpioPort>) = runBlocking {
        handler.listen(config.id, listener)
    }

    override fun removeListener(listener: GpioEventListener<GpioPort>) = runBlocking {
        handler.unlisten(config.id)
    }

    override fun getConfig(): DeviceConfig<GpioPort> = this.config

    override fun close() = runBlocking {
        handler.unlisten(config.id)
        handler.removeDevice(config.id)
    }

}
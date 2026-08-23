package io.github.iamnicknack.pjs.http.client.gpio

import io.github.iamnicknack.pjs.device.gpio.GpioPinMask
import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.gpio.GpioPortFactory
import io.github.iamnicknack.pjs.http.gpio.GpioPortHandler
import kotlinx.coroutines.runBlocking

class HttpGpioPortFactory(
    private val portHandler: GpioPortClientHandler
) : GpioPortFactory {

    override fun create(config: GpioPortConfig): GpioPort {
        val config = runBlocking { portHandler.createDevice(config.id, config.asGpioPortConfigPayload()) }
        return HttpGpioPort.Default(portHandler, config as GpioPortConfig)
    }

    fun GpioPortConfig.asGpioPortConfigPayload() = GpioPortHandler.GpioPortConfigPayload(
        GpioPinMask.offsets(this.mask),
        this.portMode,
        this.eventMode,
        this.defaultValue,
        this.debounceDelay
    )
}
package io.github.iamnicknack.pjs.http.client.gpio

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.gpio.GpioPortProvider
import io.github.iamnicknack.pjs.http.gpio.GpioPortHandler
import kotlinx.coroutines.runBlocking

class HttpGpioPortProvider(
    private val portHandler: GpioPortClientHandler
) : GpioPortProvider {

    override fun create(config: GpioPortConfig): GpioPort {
        val config = runBlocking { portHandler.createDevice(config.id, config.asGpioPortConfigPayload()) }
        return HttpGpioPort(portHandler, config as GpioPortConfig)
    }

    fun GpioPortConfig.asGpioPortConfigPayload() = GpioPortHandler.GpioPortConfigPayload(
        this.pinNumber,
        this.portMode,
        this.eventMode,
        this.defaultValue,
        this.debounceDelay
    )
}
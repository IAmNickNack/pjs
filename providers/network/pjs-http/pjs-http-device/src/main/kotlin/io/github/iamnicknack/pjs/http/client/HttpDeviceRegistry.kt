package io.github.iamnicknack.pjs.http.client

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.http.client.gpio.GpioPortClientHandler
import io.github.iamnicknack.pjs.http.client.gpio.HttpGpioPortHandler
import io.github.iamnicknack.pjs.http.client.gpio.HttpGpioPortProvider
import io.github.iamnicknack.pjs.http.client.i2c.HttpI2CHandler
import io.github.iamnicknack.pjs.http.client.i2c.HttpI2CProvider
import io.github.iamnicknack.pjs.http.client.pwm.HttpPwmHandler
import io.github.iamnicknack.pjs.http.client.pwm.HttpPwmProvider
import io.github.iamnicknack.pjs.http.client.spi.HttpSpiHandler
import io.github.iamnicknack.pjs.http.client.spi.HttpSpiProvider
import io.github.iamnicknack.pjs.http.client.spi.HttpSpiTransferHandler
import io.github.iamnicknack.pjs.http.i2c.I2CHandler
import io.github.iamnicknack.pjs.http.pwm.PwmHandler
import io.github.iamnicknack.pjs.http.spi.SpiHandler
import io.github.iamnicknack.pjs.http.spi.SpiTransferHandler
import io.github.iamnicknack.pjs.impl.DefaultDeviceRegistry
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.serialization.jackson.*

class HttpDeviceRegistry(
    private val client: HttpClient,
    portHandler: GpioPortClientHandler? = null,
    i2cHandler: I2CHandler = HttpI2CHandler(client),
    spiHandler: SpiHandler = HttpSpiHandler(client),
    spiTransferHandler: SpiTransferHandler = HttpSpiTransferHandler(client),
    pwmHandler: PwmHandler = HttpPwmHandler(client)
) : DefaultDeviceRegistry() {

    constructor(baseUrl: String) : this(
        HttpClient(CIO) {
            install(DefaultRequest) {
                url(baseUrl)
            }
            install(ContentNegotiation) {
                jackson()
            }
            install(SSE)
        }
    )

    init {
        registerProvider(
            HttpGpioPortProvider(
                portHandler
                    ?: HttpGpioPortHandler(client, this)
            ),
            GpioPortConfig::class.java
        )
        registerProvider(HttpI2CProvider(i2cHandler), I2CConfig::class.java)
        registerProvider(HttpSpiProvider(spiHandler, spiTransferHandler), SpiConfig::class.java)
        registerProvider(HttpPwmProvider(pwmHandler), PwmConfig::class.java)
    }

}
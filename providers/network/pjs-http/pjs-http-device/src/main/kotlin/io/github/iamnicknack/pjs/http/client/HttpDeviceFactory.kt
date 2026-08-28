package io.github.iamnicknack.pjs.http.client

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.http.client.gpio.GpioPortClientHandler
import io.github.iamnicknack.pjs.http.client.gpio.HttpGpioPort
import io.github.iamnicknack.pjs.http.client.gpio.HttpGpioPortFactory
import io.github.iamnicknack.pjs.http.client.gpio.HttpGpioPortHandler
import io.github.iamnicknack.pjs.http.client.i2c.HttpI2C
import io.github.iamnicknack.pjs.http.client.i2c.HttpI2CFactory
import io.github.iamnicknack.pjs.http.client.i2c.HttpI2CHandler
import io.github.iamnicknack.pjs.http.client.pwm.HttpPwm
import io.github.iamnicknack.pjs.http.client.pwm.HttpPwmFactory
import io.github.iamnicknack.pjs.http.client.pwm.HttpPwmHandler
import io.github.iamnicknack.pjs.http.client.spi.HttpSpi
import io.github.iamnicknack.pjs.http.client.spi.HttpSpiFactory
import io.github.iamnicknack.pjs.http.client.spi.HttpSpiHandler
import io.github.iamnicknack.pjs.http.client.spi.HttpSpiTransferHandler
import io.github.iamnicknack.pjs.http.i2c.I2CHandler
import io.github.iamnicknack.pjs.http.pwm.PwmHandler
import io.github.iamnicknack.pjs.http.spi.SpiHandler
import io.github.iamnicknack.pjs.http.spi.SpiTransferHandler
import io.github.iamnicknack.pjs.model.device.Device
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.serialization.jackson3.*
import kotlinx.coroutines.runBlocking

sealed class HttpDeviceFactory(client: HttpClient) : GenericDeviceFactory {

    protected val portHandler: GpioPortClientHandler = HttpGpioPortHandler(client, this.asDeviceRegistry())
    protected val i2cHandler: I2CHandler = HttpI2CHandler(client)
    protected val spiHandler: SpiHandler = HttpSpiHandler(client)
    protected val spiTransferHandler: SpiTransferHandler = HttpSpiTransferHandler(client)
    protected val pwmHandler: PwmHandler = HttpPwmHandler(client)

    /**
     * Factory with the default behaviour of delegating construction of a new device instance to the server
     */
    class Default(httpClient: HttpClient) : HttpDeviceFactory(httpClient) {

        constructor(host: String, port: Int) : this(httpClient(host, port))

        private val delegate = GenericDeviceFactory.builder()
            .factory(HttpGpioPortFactory(this.portHandler), GpioPortConfig::class.java)
            .factory(HttpI2CFactory(this.i2cHandler), I2CConfig::class.java)
            .factory(HttpSpiFactory(this.spiHandler, this.spiTransferHandler), SpiConfig::class.java)
            .factory(HttpPwmFactory(this.pwmHandler), PwmConfig::class.java)
            .build()

        override fun <T : Device<T>, V : DeviceConfig<T>> create(config: V): T = delegate.create(config)
    }

    /**
     * Factory which provides access to existing devices on the server
     */
    class Proxy(httpClient: HttpClient) : HttpDeviceFactory(httpClient) {

        constructor(host: String, port: Int) : this(httpClient(host, port))

        private val delegate = GenericDeviceFactory.builder()
            .factory(this::createHttpProxy, GpioPortConfig::class.java)
            .factory(this::createI2CProxy, I2CConfig::class.java)
            .factory(this::createSpiProxy, SpiConfig::class.java)
            .factory(this::createPwmProxy, PwmConfig::class.java)
            .build()

        override fun <T : Device<T>, V : DeviceConfig<T>> create(config: V): T = delegate.create(config)

        private fun createHttpProxy(config: GpioPortConfig): GpioPort = runBlocking {
            val remoteConfig = portHandler.getDevice(config.id()) as? GpioPortConfig
                ?: error("Device not found: ${config.id}")
            HttpGpioPort.Proxy(portHandler, remoteConfig)
        }

        private fun createI2CProxy(config: I2CConfig): I2C = runBlocking {
            val remoteConfig = i2cHandler.getDevice(config.id) as? I2CConfig
                ?: error("Device not found: ${config.id}")
            HttpI2C.Proxy(i2cHandler, remoteConfig)
        }

        private fun createSpiProxy(config: SpiConfig): Spi = runBlocking {
            val remoteConfig = spiHandler.getDevice(config.id) as? SpiConfig
                ?: error("Device not found: ${config.id}")
            HttpSpi.Proxy(spiHandler, spiTransferHandler, remoteConfig)
        }

        private fun createPwmProxy(config: PwmConfig): Pwm = runBlocking {
            val remoteConfig = pwmHandler.getDevice(config.id) as? PwmConfig
                ?: error("Device not found: ${config.id}")
            HttpPwm.Proxy(pwmHandler, remoteConfig)
        }
    }

    companion object {
        private fun httpClient(baseUrl: String) = HttpClient(CIO) {
            install(DefaultRequest) {
                url(baseUrl)
            }
            install(ContentNegotiation) {
                jackson()
            }
            install(SSE)
        }

        private fun httpClient(host: String, port: Int) = httpClient("http://$host:$port")
    }
}

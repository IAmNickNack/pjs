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
import io.github.iamnicknack.pjs.http.client.gpio.HttpGpioPortHandler
import io.github.iamnicknack.pjs.http.client.gpio.HttpGpioPortProvider
import io.github.iamnicknack.pjs.http.client.i2c.HttpI2C
import io.github.iamnicknack.pjs.http.client.i2c.HttpI2CHandler
import io.github.iamnicknack.pjs.http.client.i2c.HttpI2CProvider
import io.github.iamnicknack.pjs.http.client.pwm.HttpPwm
import io.github.iamnicknack.pjs.http.client.pwm.HttpPwmHandler
import io.github.iamnicknack.pjs.http.client.pwm.HttpPwmProvider
import io.github.iamnicknack.pjs.http.client.spi.HttpSpi
import io.github.iamnicknack.pjs.http.client.spi.HttpSpiHandler
import io.github.iamnicknack.pjs.http.client.spi.HttpSpiProvider
import io.github.iamnicknack.pjs.http.client.spi.HttpSpiTransferHandler
import io.github.iamnicknack.pjs.http.i2c.I2CHandler
import io.github.iamnicknack.pjs.http.pwm.PwmHandler
import io.github.iamnicknack.pjs.http.spi.SpiHandler
import io.github.iamnicknack.pjs.http.spi.SpiTransferHandler
import io.github.iamnicknack.pjs.impl.DefaultDeviceRegistry
import io.github.iamnicknack.pjs.model.device.Device
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking

sealed class HttpDeviceRegistry(client: HttpClient) : DefaultDeviceRegistry() {

    private val portHandler: GpioPortClientHandler = HttpGpioPortHandler(client, this)
    private val i2cHandler: I2CHandler = HttpI2CHandler(client)
    private val spiHandler: SpiHandler = HttpSpiHandler(client)
    private val spiTransferHandler: SpiTransferHandler = HttpSpiTransferHandler(client)
    private val pwmHandler: PwmHandler = HttpPwmHandler(client)

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

    constructor(host: String, port: Int) : this("http://$host:$port")

    init {
        registerProvider(HttpGpioPortProvider(this.portHandler), GpioPortConfig::class.java)
        registerProvider(HttpI2CProvider(i2cHandler), I2CConfig::class.java)
        registerProvider(HttpSpiProvider(spiHandler, spiTransferHandler), SpiConfig::class.java)
        registerProvider(HttpPwmProvider(pwmHandler), PwmConfig::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Device<T>> device(id: String, deviceType: Class<T>): T? {
        return super.device(id, deviceType) ?: when (deviceType) {
            GpioPort::class.java -> runBlocking {
                val deviceConfig = portHandler.getDevice(id) as GpioPortConfig
                HttpGpioPort.Proxy(portHandler, deviceConfig) as T
            }
            I2CConfig::class.java -> runBlocking {
                val deviceConfig = i2cHandler.getDevice(id) as I2CConfig
                HttpI2C.Proxy(i2cHandler, deviceConfig) as T
            }
            SpiConfig::class.java -> runBlocking {
                val deviceConfig = spiHandler.getDevice(id) as SpiConfig
                HttpSpi.Proxy(spiHandler, deviceConfig) as T
            }
            PwmConfig::class.java -> runBlocking {
                val deviceConfig = pwmHandler.getDevice(id) as PwmConfig
                HttpPwm.Proxy(pwmHandler, deviceConfig) as T
            }
            else -> null
        }?.also { appendDevice(it) }
    }

    /**
     * Proxy implementation used when the device is not managed by the local registry
     */
    @Suppress("UNCHECKED_CAST")
    class Proxy(httpClient: HttpClient) : HttpDeviceRegistry(httpClient) {

        constructor(baseUrl: String) : this(httpClient(baseUrl))
        constructor(host: String, port: Int) : this(httpClient(host, port))

        override fun <T : Device<T>, V : DeviceConfig<T>> create(config: V): T {
            return when (config) {
                is GpioPortConfig -> device(config.id, GpioPort::class.java) as T
                is I2CConfig -> device(config.id, I2C::class.java) as T
                is SpiConfig -> device(config.id, Spi::class.java) as T
                is PwmConfig -> device(config.id, Pwm::class.java) as T
                else -> error("Unsupported device type ${config.javaClass.simpleName}")
            }
        }
    }

    /**
     * Default implementation used when the device is managed by the local registry
     */
    class Default(httpClient: HttpClient) : HttpDeviceRegistry(httpClient) {
        constructor(baseUrl: String) : this(httpClient(baseUrl))
        constructor(host: String, port: Int) : this(httpClient(host, port))
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
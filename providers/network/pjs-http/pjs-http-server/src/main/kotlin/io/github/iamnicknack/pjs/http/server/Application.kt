package io.github.iamnicknack.pjs.http.server

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.http.i2c.I2CHandler
import io.github.iamnicknack.pjs.http.pwm.PwmHandler
import io.github.iamnicknack.pjs.http.server.config.ConfigHandlerImpl
import io.github.iamnicknack.pjs.http.server.gpio.GpioPortHandlerImpl
import io.github.iamnicknack.pjs.http.server.gpio.GpioPortServerHandler
import io.github.iamnicknack.pjs.http.server.gpio.gpioPortRoutes
import io.github.iamnicknack.pjs.http.server.i2c.I2CHandlerImpl
import io.github.iamnicknack.pjs.http.server.i2c.i2cRoutes
import io.github.iamnicknack.pjs.http.server.pwm.PwmHandlerImpl
import io.github.iamnicknack.pjs.http.server.pwm.pwmRoutes
import io.github.iamnicknack.pjs.http.server.spi.SpiHandlerImpl
import io.github.iamnicknack.pjs.http.server.spi.SpiTransferHandlerImpl
import io.github.iamnicknack.pjs.http.server.spi.spiRoutes
import io.github.iamnicknack.pjs.http.spi.SpiHandler
import io.github.iamnicknack.pjs.http.spi.SpiTransferHandler
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.github.iamnicknack.pjs.server.ConfigurableDeviceRegistryProvider
import io.github.iamnicknack.pjs.server.DeviceRegistryProvider
import io.github.iamnicknack.pjs.server.ServerConfiguration
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {

    val config = ServerConfiguration.createFromCommandLine(args)
    if (config.help) {
        ServerConfiguration.parser.help(System.out)
    } else {

        val registryProvider: DeviceRegistryProvider = ConfigurableDeviceRegistryProvider(config)

        embeddedServer(
            factory = Netty,
            port = config.port ?: 8080,
            module = { koinModule(registryProvider); module() },
        ).start(wait = true)
    }
}

fun Application.module() {
    install(ContentNegotiation) {
        jackson()
    }
    install(SSE)
    installStatusPages()
    routing {
        swaggerUI(path = "/swagger", swaggerFile = "pjs-openapi.yaml")
        gpioPortRoutes(get())
        spiRoutes(get(), get())
        pwmRoutes(get())
        i2cRoutes(get())
    }
}

/**
 * Install Koin modules for dependency injection
 * @param registryProvider Device registry provider for dependency injection
 */
fun Application.koinModule(registryProvider: DeviceRegistryProvider) {
    install(Koin) {
        modules(
            module { single { registryProvider.createDeviceRegistry() } bind DeviceRegistry::class },
            handlerModule
        )
    }
}

val handlerModule = module {
    single {
        val configHandler = ConfigHandlerImpl(
            get(),
            GpioPort::class.java,
        )
        GpioPortHandlerImpl(get(), configHandler)
    } bind GpioPortServerHandler::class

    single {
        val configHandler = ConfigHandlerImpl(
            get(),
            Spi::class.java,
        )
        SpiHandlerImpl(get(), configHandler)
    } bind SpiHandler::class

    single {
        SpiTransferHandlerImpl(get())
    } bind SpiTransferHandler::class

    single {
        val configHandler = ConfigHandlerImpl(
            get(),
            Pwm::class.java
        )
        PwmHandlerImpl(get(), configHandler)
    } bind PwmHandler::class

    single {
        val configHandler = ConfigHandlerImpl(
            get(),
            I2C::class.java,
        )
        I2CHandlerImpl(get(), configHandler)
    } bind I2CHandler::class
}




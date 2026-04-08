package io.github.iamnicknack.pjs.http.server

import io.github.iamnicknack.pjs.http.server.gpio.gpioPortRoutes
import io.github.iamnicknack.pjs.http.server.i2c.i2cRoutes
import io.github.iamnicknack.pjs.http.server.pwm.pwmRoutes
import io.github.iamnicknack.pjs.http.server.spi.spiRoutes
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.github.iamnicknack.pjs.server.ConfigurableDeviceRegistryProvider
import io.github.iamnicknack.pjs.server.DeviceRegistryProvider
import io.github.iamnicknack.pjs.server.ServerConfiguration
import io.github.iamnicknack.pjs.util.LoggingUtils
import io.github.iamnicknack.pjs.util.StartupUtils
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import org.apache.commons.cli.help.HelpFormatter
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    StartupUtils.loadApplicationProperties()
    LoggingUtils.setLogbackLevelsFromProperties(System.getProperties())
    val config = ServerConfiguration.createFromCommandLine(args)
    if (config.help) {
        HelpFormatter.builder().setShowSince(false).get()
            .also { it.printOptions(ServerConfiguration.options) }
    } else {
        embeddedServer(
            factory = Netty,
            port = config.port ?: 8080,
            module = { koinModule(config); ktorModule() },
        ).start(wait = true)
    }
}

fun Application.ktorModule() {
    install(ContentNegotiation) {
        jackson()
    }
    installStatusPages()
    install(SSE)
    routing {
        swaggerUI(path = "/swagger", swaggerFile = "pjs-openapi.yaml")
        gpioPortRoutes()
        spiRoutes()
        pwmRoutes()
        i2cRoutes()
    }
}

/**
 * Install Koin modules for dependency injection
 * @param config The server configuration
 */
fun Application.koinModule(config: ServerConfiguration) {
    install(Koin) {
        modules(
            module {
                single<DeviceRegistry>(createdAtStart = true) {
                    val registryProvider: DeviceRegistryProvider = ConfigurableDeviceRegistryProvider(config)
                    registryProvider.createDeviceRegistry()
                }
            },
            handlerModule
        )
    }
}

package io.github.iamnicknack.pjs.http

import io.github.iamnicknack.pjs.http.client.HttpDeviceFactory
import io.github.iamnicknack.pjs.http.server.handlerModule
import io.github.iamnicknack.pjs.http.server.ktorModule
import io.github.iamnicknack.pjs.mock.MockDeviceFactory
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.serialization.jackson3.*
import io.ktor.server.testing.*
import org.koin.dsl.bind
import org.koin.ktor.plugin.Koin

/**
 * Local and remote device registries for use in HTTP device testing
 * @param mockDeviceRegistry remote device registry
 * @param httpDeviceRegistry local device registry
 */
class PjsHttpTestCase(
    val mockDeviceRegistry: DeviceRegistry,
    val httpDeviceRegistry: DeviceRegistry,
    val httpProxyDeviceRegistry: DeviceRegistry
)

fun pjsHttpTestCase(block: suspend PjsHttpTestCase.() -> Unit) = testApplication {

    val mockDeviceRegistry = MockDeviceFactory().asDeviceRegistry()

    install(Koin) {
        modules(
            org.koin.dsl.module {
                single { mockDeviceRegistry } bind DeviceRegistry::class
            },
            handlerModule
        )
    }

    application {
        ktorModule()
    }

    val client = createClient {
        install(ContentNegotiation) {
            jackson()
        }
        install(SSE)
    }

    val httpDeviceRegistry = HttpDeviceFactory.Default(client).asDeviceRegistry()
    val httpProxyDeviceRegistry = HttpDeviceFactory.Proxy(client).asDeviceRegistry()

    block(PjsHttpTestCase(mockDeviceRegistry, httpDeviceRegistry, httpProxyDeviceRegistry))

}
package io.github.iamnicknack.pjs.http

import io.github.iamnicknack.pjs.http.client.HttpDeviceRegistry
import io.github.iamnicknack.pjs.http.server.handlerModule
import io.github.iamnicknack.pjs.http.server.module
import io.github.iamnicknack.pjs.mock.MockDeviceRegistry
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import org.koin.dsl.bind
import org.koin.ktor.plugin.Koin

/**
 * Local and remote device registries for use in HTTP device testing
 * @param mockDeviceRegistry remote device registry
 * @param httpDeviceRegistry local device registry
 */
class PjsHttpTestCase(
    val mockDeviceRegistry: MockDeviceRegistry,
    val httpDeviceRegistry: HttpDeviceRegistry
)

fun pjsHttpTestCase(block: suspend PjsHttpTestCase.() -> Unit) = testApplication {

    val mockDeviceRegistry = MockDeviceRegistry()

    install(Koin) {
        modules(
            org.koin.dsl.module {
                single { mockDeviceRegistry } bind DeviceRegistry::class
            },
            handlerModule
        )
    }

    application {
        module()
    }

    val client = createClient {
        install(ContentNegotiation) {
            jackson()
        }
        install(SSE)
    }

    val httpDeviceRegistry = HttpDeviceRegistry(client)

    block(PjsHttpTestCase(mockDeviceRegistry, httpDeviceRegistry))

}
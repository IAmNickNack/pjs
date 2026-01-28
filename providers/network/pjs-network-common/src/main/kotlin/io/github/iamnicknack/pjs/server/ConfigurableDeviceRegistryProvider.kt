package io.github.iamnicknack.pjs.server

import io.github.iamnicknack.pjs.logging.LoggingDeviceRegistry
import io.github.iamnicknack.pjs.mock.MockDeviceRegistry
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader
import org.slf4j.LoggerFactory
import java.util.*

class ConfigurableDeviceRegistryProvider(
    val preferredMode: String = System.getProperty("pjs.mode", "mock"),
    val proxyHost: String? = System.getProperty("pjs.proxy.host"),
    val proxyPort: Int? = System.getProperty("pjs.proxy.port")?.toInt(),
    val logging: Boolean = System.getProperty("pjs.logging", "false").toBoolean(),
) : DeviceRegistryProvider {

    constructor(config: ServerConfiguration) : this(
        preferredMode = config.preferredMode ?: "mock",
        proxyHost = config.proxyHost,
        proxyPort = config.proxyPort,
        logging = config.logging
    )

    private val logger = LoggerFactory.getLogger(ConfigurableDeviceRegistryProvider::class.java)

    private val propertyMap: Map<String, Any?> = mapOf(
        "pjs.mode" to preferredMode,
        "pjs.proxy.host" to proxyHost,
        "pjs.proxy.port" to proxyPort,
        "pi4j.grpc.host" to proxyHost,
        "pi4j.grpc.port" to proxyPort,
    )

    /**
     * The device registry used to manage the devices.
     * Registries are loaded using the [io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader] service loader.
     */
    override fun createDeviceRegistry(): DeviceRegistry {
        val registry = ServiceLoader.load(DeviceRegistryLoader::class.java, DeviceRegistryLoader::class.java.classLoader)
            .firstOrNull { loader ->
                loader.isLoadable(propertyMap)
                    .also { logger.debug("Loader {}: isLoadable={}", loader.javaClass.simpleName, it) }
            }
            ?.load(propertyMap)
            ?: MockDeviceRegistry()

        logger.info("Using {} devices", registry.javaClass.simpleName)

        if (logging) {
            logger.info("Using logging devices")
            return LoggingDeviceRegistry(registry)
        } else {
            return registry
        }
    }

    override fun toString(): String {
        return "ConfigurableDeviceRegistry(logging=$logging, proxyPort=$proxyPort, proxyHost=$proxyHost, preferredMode='$preferredMode')"
    }
}
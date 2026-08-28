package io.github.iamnicknack.pjs.server

import io.github.iamnicknack.pjs.logging.LoggingDeviceFactory
import io.github.iamnicknack.pjs.mock.MockDeviceFactory
import io.github.iamnicknack.pjs.model.device.DeviceFactoryLoader
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import org.slf4j.LoggerFactory
import java.util.*

class ConfigurableDeviceRegistryFactory(
    val preferredMode: String = System.getProperty("pjs.mode", "mock"),
    val proxyHost: String? = System.getProperty("pjs.proxy.host"),
    val proxyPort: Int? = System.getProperty("pjs.proxy.port")?.toInt(),
    val logging: Boolean = System.getProperty("pjs.logging", "false").toBoolean(),
) : DeviceRegistryFactory {

    constructor(config: ServerConfiguration) : this(
        preferredMode = config.preferredMode ?: "mock",
        proxyHost = config.proxyHost,
        proxyPort = config.proxyPort,
        logging = config.logging
    )

    private val logger = LoggerFactory.getLogger(ConfigurableDeviceRegistryFactory::class.java)

    private val propertyMap: Map<String, Any> = mapOf(
        "pjs.mode" to preferredMode,
        "pjs.proxy.host" to proxyHost,
        "pjs.proxy.port" to proxyPort,
        "pi4j.grpc.host" to proxyHost,
        "pi4j.grpc.port" to proxyPort,
    )
        .filter { it.value != null } // assert the map does not contain null values
        .map { it.key to it.value!! }
        .toMap()

    /**
     * The device registry used to manage the devices.
     * Registries are loaded using the [DeviceFactoryLoader] service loader.
     */
    override fun createDeviceRegistry(): DeviceRegistry {
        var factory = ServiceLoader
            .load(DeviceFactoryLoader::class.java, DeviceFactoryLoader::class.java.classLoader)
            .firstOrNull { loader ->
                loader.isLoadable(propertyMap)
                    .also { logger.debug("Loader {}: isLoadable={}", loader.javaClass.simpleName, it) }
            }
            ?.load(propertyMap)
            ?: MockDeviceFactory()

        logger.info("Using {} devices", factory.javaClass.simpleName)

        factory = if (logging) {
            logger.info("Using logging devices")
            LoggingDeviceFactory(factory)
        } else {
            factory
        }

        return factory.asDeviceRegistry()
    }

    override fun toString(): String {
        return "ConfigurableDeviceRegistry(" +
                "logging=$logging, " +
                "proxyPort=$proxyPort, " +
                "proxyHost=$proxyHost, " +
                "preferredMode='$preferredMode'" +
                ")"
    }
}

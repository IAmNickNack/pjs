package io.github.iamnicknack.pjs.http.client

import io.github.iamnicknack.pjs.model.device.DeviceFactoryLoader

/**
 * [io.github.iamnicknack.pjs.model.device.DeviceFactoryLoader] for [HttpDeviceFactory]
 */
class HttpDeviceFactoryLoader : DeviceFactoryLoader<HttpDeviceFactoryConfig> {

    override fun isLoadable(properties: Map<String, Any>) = config(properties)
        ?.let { properties["pjs.mode"]?.toString() == "http" }
        ?: false

    override fun load(properties: Map<String, Any>) = config(properties)
        ?.let(this::load)

    override fun load(config: HttpDeviceFactoryConfig) = if (config.mode == HttpDeviceFactoryConfig.Mode.PROXY) {
        HttpDeviceFactory.Proxy(config.proxyHost, config.proxyPort)
    } else {
        HttpDeviceFactory.Default(config.proxyHost, config.proxyPort)
    }

    /**
     * Load configuration from the provided properties
     * @param properties the properties to load from
     * @return the configuration, or null if the properties are not valid
     */
    private fun config(properties: Map<String, Any>): HttpDeviceFactoryConfig? {
        return if (properties["pjs.proxy.port"] != null && properties["pjs.proxy.host"] != null) {
            HttpDeviceFactoryConfig(
                properties["pjs.proxy.host"] as String,
                properties["pjs.proxy.port"].toString().toIntOrNull() ?: 8080,
                properties["pjs.http.mode"]?.toString()
                    ?.let { HttpDeviceFactoryConfig.Mode.valueOf(it.uppercase()) }
                    ?: HttpDeviceFactoryConfig.Mode.DEFAULT
            )
        } else {
            null
        }
    }
}

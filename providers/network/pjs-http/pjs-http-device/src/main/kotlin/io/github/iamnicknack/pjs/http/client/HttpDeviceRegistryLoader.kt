package io.github.iamnicknack.pjs.http.client

import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader

/**
 * [DeviceRegistryLoader] for [HttpDeviceRegistry]
 */
class HttpDeviceRegistryLoader : DeviceRegistryLoader {

    override fun isLoadable(properties: Map<String, Any>) = config(properties)
        ?.let { properties["pjs.mode"]?.toString() == "http" }
        ?: false

    override fun load(properties: Map<String, Any>) = config(properties)
        ?.let(this::load)

    fun load(config: HttpDeviceRegistryConfig) = if (config.mode == HttpDeviceRegistryConfig.Mode.PROXY) {
        HttpDeviceRegistry.Proxy(config.proxyHost, config.proxyPort)
    } else {
        HttpDeviceRegistry.Default(config.proxyHost, config.proxyPort)
    }

    private fun config(properties: Map<String, Any>): HttpDeviceRegistryConfig? {
        return if (properties["pjs.proxy.port"] != null && properties["pjs.proxy.host"] != null) {
            HttpDeviceRegistryConfig(
                properties["pjs.proxy.host"] as String,
                properties["pjs.proxy.port"].toString().toIntOrNull() ?: 8080,
                properties["pjs.http.mode"]?.toString()
                    ?.let { HttpDeviceRegistryConfig.Mode.valueOf(it.uppercase()) }
                    ?: HttpDeviceRegistryConfig.Mode.DEFAULT
            )
        } else {
            null
        }
    }
}

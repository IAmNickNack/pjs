package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader
import io.grpc.ManagedChannelBuilder

class GrpcDeviceRegistryLoader : DeviceRegistryLoader<GrpcDeviceRegistryLoader.Config> {

    override fun isLoadable(properties: Map<String, Any>): Boolean {
        return properties["pjs.mode"] == "grpc" && load(properties) != null
    }

    override fun load(properties: Map<String, Any>) = loadConfig(properties)?.let(this::load)

    override fun load(registryConfig: Config): DeviceRegistry {
        val channel = ManagedChannelBuilder.forAddress(registryConfig.proxyHost, registryConfig.proxyPort)
            .usePlaintext()
            .build()

        return GrpcDeviceRegistry(channel)
    }

    private fun loadConfig(properties: Map<String, Any>): Config? {
        return if (properties.containsKey("pjs.proxy.host")) {
            Config(
                properties["pjs.proxy.host"] as String,
                properties["pjs.proxy.port"].toString().toIntOrNull() ?: 9090,
            )
        } else {
            null
        }
    }

    class Config(
        val proxyHost: String,
        val proxyPort: Int,
    )
}
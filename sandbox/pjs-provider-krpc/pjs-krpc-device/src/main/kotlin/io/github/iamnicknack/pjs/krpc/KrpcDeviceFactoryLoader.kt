package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.model.device.DeviceFactoryLoader
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory
import kotlinx.rpc.grpc.client.GrpcClient

class KrpcDeviceFactoryLoader : DeviceFactoryLoader<KrpcDeviceFactoryLoader.Config> {

    override fun isLoadable(properties: Map<String, Any>): Boolean {
        return properties["pjs.mode"] == "krpc"
    }

    override fun load(registryConfig: Config): GenericDeviceFactory {
        val client = GrpcClient(registryConfig.proxyHost, registryConfig.proxyPort) {
            credentials = plaintext()
        }

        return KrpcDeviceFactory(client)
    }

    override fun load(properties: Map<String, Any>): GenericDeviceFactory? {
        return loadConfig(properties)?.let(this::load)
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

    data class Config(
        val proxyHost: String,
        val proxyPort: Int,
    )
}
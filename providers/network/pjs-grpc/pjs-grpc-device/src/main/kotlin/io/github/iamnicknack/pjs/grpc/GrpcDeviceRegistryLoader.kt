package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader
import io.grpc.ManagedChannelBuilder

class GrpcDeviceRegistryLoader : DeviceRegistryLoader {

    override fun isLoadable(properties: Map<String, Any>) = Config(properties)
        .let { it.isRequested && it.isUsable() }

    override fun load(properties: Map<String, Any>) = Config(properties)
        .takeIf(Config::isUsable)
        ?.let {
            val channel = ManagedChannelBuilder.forAddress(it.proxyHost!!, it.proxyPort!!)
                .usePlaintext()
                .build()

            GrpcDeviceRegistry(channel)
        }

    private class Config(
        private val properties: Map<String, Any>
    ) {
        val proxyPort: Int? by lazy {
            properties["pjs.proxy.port"].toString().toIntOrNull()
        }

        val proxyHost: String? by lazy {
            properties["pjs.proxy.host"] as? String
        }

        val isRequested: Boolean by lazy {
            properties["pjs.mode"] as? String == "grpc"
        }

        fun isUsable() = proxyPort != null && proxyHost != null
    }
}
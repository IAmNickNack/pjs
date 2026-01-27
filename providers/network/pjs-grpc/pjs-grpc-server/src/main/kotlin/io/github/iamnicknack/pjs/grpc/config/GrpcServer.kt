package io.github.iamnicknack.pjs.grpc.config

import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.grpc.Server
import java.util.concurrent.TimeUnit

/**
 * Interface which specifies the minimum requirements for a gRPC server.
 * @param deviceRegistry the device registry to use when creating devices via the server
 * @param server the gRPC server instance
 */
interface GrpcServer : AutoCloseable {
    val deviceRegistry: DeviceRegistry
    val server: Server

    override fun close() {
        deviceRegistry.close()
        server.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}
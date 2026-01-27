package io.github.iamnicknack.pjs.grpc.config

import io.github.iamnicknack.pjs.grpc.service.*
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.github.iamnicknack.pjs.server.DeviceRegistryProvider
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionServiceV1
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * Default implementation of [GrpcServer].
 */
class DefaultGrpcServer(
    deviceRegistryProvider: DeviceRegistryProvider,
    serverBuilderProvider: ServerBuilderProvider
) : GrpcServer {

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            close()
        })
    }

    private val logger = LoggerFactory.getLogger(DefaultGrpcServer::class.java)

    override val deviceRegistry: DeviceRegistry by lazy {
        deviceRegistryProvider.createDeviceRegistry()
    }

    override val server: Server by lazy {
        serverBuilderProvider.createServerBuilder()
            .addService(GrpcPortService(deviceRegistry))
            .addService(GrpcPortConfigService(deviceRegistry))
            .addService(GrpcI2CBusService(deviceRegistry))
            .addService(GrpcI2CBusConfigService(deviceRegistry))
            .addService(GrpcPwmService(deviceRegistry))
            .addService(GrpcPwmConfigService(deviceRegistry))
            .addService(GrpcSpiService(deviceRegistry))
            .addService(GrpcSpiConfigService(deviceRegistry))
            .addService(GrpcSpiTransferService(deviceRegistry))
            .addService(GrpcDeviceConfigService(deviceRegistry))
            .addService(ProtoReflectionServiceV1.newInstance())
            .intercept(ExceptionLoggingInterceptor())
            .build()
    }

    override fun close() {
        logger.info("*** shutting down gRPC server")
        deviceRegistry.close()
        server.shutdown().awaitTermination(5, TimeUnit.SECONDS)
        logger.info("*** server shut down")
    }

    /**
     * Lazy provider for the gRPC server builder.
     * Allows customising the server builder before applying the default configuration of services.
     */
    fun interface ServerBuilderProvider {
        fun createServerBuilder(): ServerBuilder<*>
    }
}
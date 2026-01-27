package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.grpc.service.*
import io.github.iamnicknack.pjs.mock.MockDeviceRegistry
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.grpc.Channel
import io.grpc.Server
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

class PjsExtension : BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private lateinit var channel: Channel
    private lateinit var localRegistry: DeviceRegistry
    private lateinit var remoteRegistry: DeviceRegistry
    private lateinit var server: Server

    override fun beforeEach(context: ExtensionContext) {
        channel = InProcessChannelBuilder.forName("test").build()
        remoteRegistry = MockDeviceRegistry()
        localRegistry = GrpcDeviceRegistry(channel)
        server = InProcessServerBuilder.forName("test").directExecutor()
            .addService(GrpcPortService(remoteRegistry))
            .addService(GrpcPortConfigService(remoteRegistry))
            .addService(GrpcI2CBusService(remoteRegistry))
            .addService(GrpcI2CBusConfigService(remoteRegistry))
            .addService(GrpcPwmService(remoteRegistry))
            .addService(GrpcPwmConfigService(remoteRegistry))
            .addService(GrpcSpiService(remoteRegistry))
            .addService(GrpcSpiConfigService(remoteRegistry))
            .addService(GrpcSpiTransferService(remoteRegistry))
            .addService(GrpcDeviceConfigService(remoteRegistry))
            .intercept(ExceptionLoggingInterceptor())
            .build()
            .also { it.start() }
    }

    override fun afterEach(context: ExtensionContext) {
        server.shutdownNow()
    }

    override fun supportsParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ): Boolean {
        return parameterContext.parameter.type == DeviceRegistry::class.java &&
                (parameterContext.isAnnotated(Local::class.java)
                        || parameterContext.isAnnotated(Remote::class.java))
    }

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ): Any? {
        return if (parameterContext.isAnnotated(Local::class.java)) {
            localRegistry
        } else if (parameterContext.isAnnotated(Remote::class.java)) {
            remoteRegistry
        } else {
            null
        }
    }

    @Retention(AnnotationRetention.RUNTIME)
    annotation class Local
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Remote
}
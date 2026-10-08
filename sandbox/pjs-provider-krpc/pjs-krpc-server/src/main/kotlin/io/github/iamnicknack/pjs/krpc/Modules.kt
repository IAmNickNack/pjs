package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.ffm.NativeDeviceFactoryLoader
import io.github.iamnicknack.pjs.krpc.gen.v1.config.DeviceConfigService
import io.github.iamnicknack.pjs.krpc.gen.v1.i2c.bus.I2CBusConfigService
import io.github.iamnicknack.pjs.krpc.gen.v1.i2c.bus.I2CBusService
import io.github.iamnicknack.pjs.krpc.gen.v1.port.PortConfigService
import io.github.iamnicknack.pjs.krpc.gen.v1.port.PortService
import io.github.iamnicknack.pjs.krpc.gen.v1.pwm.PwmConfigService
import io.github.iamnicknack.pjs.krpc.gen.v1.pwm.PwmService
import io.github.iamnicknack.pjs.krpc.gen.v1.spi.SpiConfigService
import io.github.iamnicknack.pjs.krpc.gen.v1.spi.SpiService
import io.github.iamnicknack.pjs.krpc.gen.v1.spi.SpiTransferService
import io.github.iamnicknack.pjs.http.client.HttpDeviceFactoryConfig
import io.github.iamnicknack.pjs.http.client.HttpDeviceFactoryLoader
import io.github.iamnicknack.pjs.krpc.ErrorHandlingProxy.withErrorHandling
import io.github.iamnicknack.pjs.krpc.service.*
import io.github.iamnicknack.pjs.logging.LoggingDeviceFactory
import io.github.iamnicknack.pjs.mock.MockDeviceFactory
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory
import io.github.iamnicknack.pjs.pi4j.Pi4JDeviceFactoryLoader
import io.github.iamnicknack.pjs.server.ServerConfiguration
import kotlinx.rpc.grpc.server.GrpcServer
import kotlinx.rpc.registerService
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Supplier


val registryModule = module {
    val logger: Logger = LoggerFactory.getLogger("io.github.iamnicknack.pjs.krpc.registry")

    single<DeviceRegistry> {
        val serverConfiguration: ServerConfiguration by inject()

        val builder = GenericDeviceFactory.builder()
            .factory(get())

        if (serverConfiguration.logging) {
            builder.decorator {
                logger.info("Logging enabled")
                LoggingDeviceFactory(it)
            }
        }

        builder
            .build()
            .asDeviceRegistry()
    }

    single<Supplier<GenericDeviceFactory>> {
        val serverConfiguration: ServerConfiguration by inject()

        when (ServerConfiguration.Mode.fromString(serverConfiguration.preferredMode ?: "mock")) {
            ServerConfiguration.Mode.MOCK -> Supplier {
                logger.info("Using mock device factory")
                MockDeviceFactory()
            }
            ServerConfiguration.Mode.FFM -> Supplier {
                logger.info("Using native device factory")
                NativeDeviceFactoryLoader().load()
            }
            ServerConfiguration.Mode.PI4J -> Supplier {
                logger.info("Using pi4j device factory")
                Pi4JDeviceFactoryLoader().load(
                    Pi4JDeviceFactoryLoader.Config(
                        serverConfiguration.proxyHost,
                        serverConfiguration.proxyPort ?: 9090,
                        System.getProperties().entries
                            .associate { it.key.toString() to it.value.toString() }
                    )
                )
            }
            ServerConfiguration.Mode.GRPC -> Supplier {
                logger.info("Using grpc device factory")
                KrpcDeviceFactoryLoader().load(
                    KrpcDeviceFactoryLoader.Config(
                        proxyHost = serverConfiguration.proxyHost ?: "localhost",
                        proxyPort = serverConfiguration.proxyPort ?: 9090
                    )
                )
            }
            ServerConfiguration.Mode.HTTP -> Supplier {
                logger.info("Using http device factory")
                HttpDeviceFactoryLoader().load(
                    HttpDeviceFactoryConfig(
                        proxyHost = serverConfiguration.proxyHost ?: "localhost",
                        proxyPort = serverConfiguration.proxyPort ?: 9090,
                        mode = HttpDeviceFactoryConfig.Mode.DEFAULT
                    )
                )
            }
        }
    }
}

val servicesModule = module {
    includes(registryModule)
    single<DeviceConfigService> { DeviceConfigServiceImpl(get()).withErrorHandling() }
    single<PortConfigService> { PortConfigServiceImpl(get()).withErrorHandling() }
    single<PortService> { PortServiceImpl(get()).withErrorHandling() }
    single<I2CBusConfigService> { I2CBusConfigServiceImpl(get()).withErrorHandling() }
    single<I2CBusService> { I2CBusServiceImpl(get()).withErrorHandling() }
    single<PwmConfigService> { PwmConfigServiceImpl(get()).withErrorHandling() }
    single<PwmService> { PwmServiceImpl(get()).withErrorHandling() }
    single<SpiConfigService> { SpiConfigServiceImpl(get()).withErrorHandling() }
    single<SpiService> { SpiServiceImpl(get()).withErrorHandling() }
    single<SpiTransferService> { SpiTransferServiceImpl(get()).withErrorHandling() }
}

val serverModule = module {
    includes(servicesModule)
    single<GrpcServer> {
        val serverConfiguration: ServerConfiguration by inject()

        GrpcServer(serverConfiguration.port ?: 9090) {
            services {
                registerService<DeviceConfigService> { get() }
                registerService<PortConfigService> { get() }
                registerService<PortService> { get() }
                registerService<I2CBusConfigService> { get() }
                registerService<I2CBusService> { get() }
                registerService<PwmConfigService> { get() }
                registerService<PwmService> { get() }
                registerService<SpiConfigService> { get() }
                registerService<SpiService> { get() }
                registerService<SpiTransferService> { get() }
            }
        }
    }
}

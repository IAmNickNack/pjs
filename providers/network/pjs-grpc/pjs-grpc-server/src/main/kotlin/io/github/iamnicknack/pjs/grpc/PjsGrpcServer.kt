package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.grpc.config.DefaultGrpcServer
import io.github.iamnicknack.pjs.server.ConfigurableDeviceRegistryProvider
import io.github.iamnicknack.pjs.server.ServerConfiguration
import io.github.iamnicknack.pjs.util.LoggingUtils
import io.grpc.ServerBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object PjsGrpcServer {
    val logger: Logger = LoggerFactory.getLogger(PjsGrpcServer::class.java)


    fun execute(args: Array<String>): Int {
        val config = ServerConfiguration.createFromCommandLine(args)

        if (config.help) {
            ServerConfiguration.parser.help(System.out)
        } else {
            val port = config.port ?: 9090
            val serverProvider = DefaultGrpcServer.ServerBuilderProvider  { ServerBuilder.forPort(port) }
            val registry = ConfigurableDeviceRegistryProvider(
                 preferredMode = config.preferredMode ?: "native",
                 proxyHost = config.proxyHost,
                 proxyPort = config.proxyPort,
                 logging = config.logging
             )

            DefaultGrpcServer(registry, serverProvider)
                .use {
                    logger.info("Config: {}", registry)
                    it.server.start()
                    logger.info("gRPC server listening on $port")
                    it.server.awaitTermination()
                }
        }
        return 0
    }
}

fun main(args: Array<String>) {
    LoggingUtils.setLogbackLevelsFromProperties(System.getProperties())
    PjsGrpcServer.execute(args)
}

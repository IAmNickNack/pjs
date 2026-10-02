package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.grpc.gen.v1.config.DeviceConfigService
import kotlinx.coroutines.runBlocking
import kotlinx.rpc.grpc.server.GrpcServer
import kotlinx.rpc.registerService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object KrpcServer {
    val logger: Logger = LoggerFactory.getLogger(KrpcServer::class.java)
}

fun main(args: Array<String>): Unit = runBlocking {
    val server = GrpcServer(9090) {
        services {
            registerService<DeviceConfigService> { DeviceConfigServiceImpl() }
        }
    }

    server.start()
    KrpcServer.logger.info("KRPC server started on port 9090")
    server.awaitTermination()
}


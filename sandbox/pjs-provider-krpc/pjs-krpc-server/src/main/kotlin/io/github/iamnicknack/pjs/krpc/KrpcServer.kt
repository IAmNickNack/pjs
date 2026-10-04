package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.server.ServerConfiguration
import kotlinx.coroutines.runBlocking
import kotlinx.rpc.grpc.server.GrpcServer
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object KrpcServer {
    val logger: Logger = LoggerFactory.getLogger(KrpcServer::class.java)
}

fun main(args: Array<String>): Unit = runBlocking {
    val serverConfiguration: ServerConfiguration = ServerConfiguration.createFromCommandLine(args)
    if (serverConfiguration.help) {
        ServerConfiguration.printHelp()
        return@runBlocking
    }

    startKoin {
        modules(
            module { single<ServerConfiguration> { serverConfiguration } },
            serverModule
        )
    }
    with (KoinPlatform.getKoin().get<GrpcServer>()) {
        start()
        KrpcServer.logger.info("KRPC server started on port $port")
        awaitTermination()
    }
    stopKoin()
}

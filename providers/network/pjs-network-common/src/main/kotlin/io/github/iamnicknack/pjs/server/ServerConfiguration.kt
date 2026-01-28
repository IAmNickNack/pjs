package io.github.iamnicknack.pjs.server

import io.github.iamnicknack.pjs.util.args.CommandLineArgument
import io.github.iamnicknack.pjs.util.args.CommandLineParser

data class ServerConfiguration(
    val port: Int?,
    val preferredMode: String?,
    val proxyHost: String?,
    val proxyPort: Int?,
    val logging: Boolean,
    val help: Boolean
) {

    companion object {

        private val modeArg = CommandLineArgument(
            "mode",
            "mock",
            property = "pjs.mode",
            description = "Preferred mode for running the device service. " +
                    "Valid values are 'native', 'grpc', 'http' and 'mock'. " +
                    "Defaults to 'native' if the native library is available, `mock` if not."
        )

        private val portArg = CommandLineArgument(
            "port",
            property = "pjs.port",
            description = "Port to listen on."
        )

        private val proxyHostArg = CommandLineArgument(
            "proxy-host",
            property = "pjs.proxy.host",
            description = "Host name of the gRPC backend to use when running in proxy mode."
        )

        private val proxyPortArg = CommandLineArgument(
            "proxy-port",
            property = "pjs.proxy.port",
            description = "Port of the gRPC server to use when running in proxy mode."
        )

        private val loggingArg = CommandLineArgument(
            "logging",
            "false",
            isFlag = true,
            property = "pjs.logging",
            description = "Enables logging. Defaults to false."
        )

        private val helpArg = CommandLineArgument(
            "help",
            defaultValue = "false",
            isFlag = true,
            description = "Prints this help message."
        )

        val parser: CommandLineParser = CommandLineParser.Builder()
            .arg(modeArg)
            .arg(portArg)
            .arg(proxyHostArg)
            .arg(proxyPortArg)
            .arg(loggingArg)
            .arg(helpArg)
            .build()

        fun createFromCommandLine(args: Array<String>): ServerConfiguration {

            val parseResult = parser.parse(args)

            return ServerConfiguration(
                port = parseResult.valueOrNull<Int>(portArg),
                preferredMode = parseResult.valueOrNull<String>(modeArg),
                proxyHost = parseResult.valueOrNull<String>(proxyHostArg),
                proxyPort = parseResult.valueOrNull<Int>(proxyPortArg),
                logging = parseResult.value<Boolean>(loggingArg),
                help = parseResult.value<Boolean>(helpArg)
            )
        }
    }

}

package io.github.iamnicknack.pjs.server

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options

data class ServerConfiguration(
    val port: Int?,
    val preferredMode: String?,
    val proxyHost: String?,
    val proxyPort: Int?,
    val logging: Boolean,
    val help: Boolean
) {

    enum class Mode {
        FFM,
        GRPC,
        HTTP,
        MOCK;

        companion object {
            fun fromString(value: String): Mode {
                return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                    ?: throw IllegalArgumentException("Invalid mode: $value")
            }
        }
    }

    companion object {

        val options: Options = Options()
            .addOption(Option.builder()
                .longOpt("mode")
                .type(Mode::class.java)
                .converter(Mode::fromString)
                .hasArg()
                .desc("Preferred mode for running the device service. " +
                        "Valid values are 'native', 'grpc', 'http' and 'mock'. " +
                        "Defaults to 'native' if the native library is available, `mock` if not.")
                .get()
            )
            .addOption(Option.builder()
                .longOpt("port")
                .type(Int::class.java)
                .hasArg()
                .desc("Port to listen on.")
                .get()
            )
            .addOption(Option.builder()
                .longOpt("proxy-host")
                .type(String::class.java)
                .get()
            )
            .addOption(Option.builder()
                .longOpt("proxy-port")
                .desc("Port of the server to use when running in proxy mode.")
                .get()
            )
            .addOption(Option.builder()
                .longOpt("logging")
                .type(Boolean::class.java)
                .desc("Enables logging. Defaults to false.")
                .get()
            )
            .addOption(Option.builder()
                .longOpt("help")
                .type(Boolean::class.java)
                .desc("Prints this help message.")
                .get()
            )

        fun createFromCommandLine(args: Array<String>): ServerConfiguration {
            val commandLine = DefaultParser().parse(options, args)

            return ServerConfiguration(
                port = commandLine.getParsedOptionValue<Int>("port"),
                preferredMode = commandLine.getParsedOptionValue<Mode>("mode")?.name?.lowercase(),
                proxyHost = commandLine.getParsedOptionValue<String>("proxy-host"),
                proxyPort = commandLine.getParsedOptionValue<Int>("proxy-port"),
                logging = commandLine.getParsedOptionValue("logging", false),
                help = commandLine.hasOption("help")
            )
        }
    }

}

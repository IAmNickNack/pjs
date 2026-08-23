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
    val hardwareAllocationConfig: String?,
    val help: Boolean
) {

    enum class Mode {
        FFM,
        GRPC,
        HTTP,
        MOCK,
        PI4J;

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
                .converter { it.toInt() }
                .hasArg()
                .desc("Port to listen on.")
                .get()
            )
            .addOption(Option.builder()
                .longOpt("proxy-host")
                .hasArg()
                .desc("Hostname of the server to use when running in proxy mode.")
                .type(String::class.java)
                .get()
            )
            .addOption(Option.builder()
                .longOpt("proxy-port")
                .converter { it.toInt() }
                .hasArg()
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
                .longOpt("hardware-allocation-config")
                .hasArg()
                .type(String::class.java)
                .desc("Sets the hardware allocation for the server.")
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
                logging = commandLine.hasOption("logging"),
                hardwareAllocationConfig = commandLine.getParsedOptionValue<String>("hardware-allocation-config"),
                help = commandLine.hasOption("help")
            )
        }
    }
}

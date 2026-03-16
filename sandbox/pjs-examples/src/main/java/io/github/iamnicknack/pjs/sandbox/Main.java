package io.github.iamnicknack.pjs.sandbox;

import com.pi4j.extension.Plugin;
import com.pi4j.plugin.ffm.FFMPlugin;
import com.pi4j.plugin.mock.MockPlugin;
import io.github.iamnicknack.pi4j.grpc.client.GrpcPlugin;
import io.github.iamnicknack.pjs.ffm.NativeDeviceRegistryLoader;
import io.github.iamnicknack.pjs.grpc.GrpcDeviceRegistry;
import io.github.iamnicknack.pjs.http.client.HttpDeviceRegistry;
import io.github.iamnicknack.pjs.logging.LoggingDeviceRegistry;
import io.github.iamnicknack.pjs.mock.MockDeviceRegistry;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.pi4j.Pi4jDeviceRegistryLoader;
import io.github.iamnicknack.pjs.sandbox.example.*;
import io.github.iamnicknack.pjs.util.LoggingUtils;
import io.github.iamnicknack.pjs.util.StartupUtils;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static final Logger logger = LoggerFactory.getLogger(Main.class);

    enum Mode  {
        MOCK, GRPC, FFM, PI4J;

        public static Mode fromString(String value) {
            return switch (value.toLowerCase()) {
                case "mock" -> MOCK;
                case "grpc" -> GRPC;
                case "ffm" -> FFM;
                case "pi4j" -> PI4J;
                default -> throw new IllegalArgumentException("Unknown mode: " + value);
            };
        }
    }

    static final Options options = new Options()
            .addOption(Option.builder()
                    .option("p")
                    .longOpt("plugin")
                    .desc("Which plugin to load. (`mock`, `grpc`, `ffm`, `pi4j`)")
                    .hasArg()
                    .type(Mode.class)
                    .converter(Mode::fromString)
                    .get()
            )
            .addOption("G", "grpc-host", true, "The gRPC host to connect to.")
            .addOption(Option.builder()
                    .option("P")
                    .longOpt("grpc-port")
                    .desc("The gRPC port to connect to.")
                    .hasArg()
                    .type(Integer.class)
                    .get()
            )
            .addOption(Option.builder()
                    .option("m")
                    .longOpt("mode")
                    .desc("The mode to run in. (`mock`, `grpc`, `ffm`, `pi4j`)")
                    .hasArg()
                    .type(Mode.class)
                    .converter(Mode::fromString)
                    .get()
            )
            .addOption("l", "logging", false, "Enable debug logging for IO operations")
            .addOptionGroup(
                    new OptionGroup()
                            .addOption(new Option(null, "gpio", false, "Run the GPIO example"))
                            .addOption(new Option(null, "i2c", false, "Run the I2C example"))
                            .addOption(new Option(null, "seven-segment", false, "Run the seven-segment example"))
                            .addOption(new Option(null, "spi", false, "Run the SPI example"))
                            .addOption(new Option(null, "eeprom", false, "Run the EEPROM example"))
                            .addOption(new Option(null, "328", false, "Run the three-to-eight decoder example"))
                            .addOption(new Option(null, "pwm", false, "Run the PWM example"))
                            .addOption(new Option(null, "mcp", false, "Run the MCP23017 example"))
                            .addOption(new Option(null, "oled", false, "Run the OLED example"))
                            .addOption(new Option(null, "debounce", false, "Run the debounce example"))
                            .addOption(new Option(null, "rotary", false, "Run the rotary encoder example. Doesn't currently work well with grpc"))
            )
            .addOption("h", "help", false, "Display help information");

    static Map<String, Object> optionsAsSystemProperties(CommandLine commandLine) {
        var map = new HashMap<String, Object>();
        if (commandLine.hasOption("grpc-host")) {
            map.put("pjs.grpc.host", commandLine.getOptionValue("grpc-host"));
        }
        if (commandLine.hasOption("grpc-port")) {
            map.put("pjs.grpc.port", commandLine.getOptionValue("grpc-port"));
        }
        if (commandLine.hasOption("mode")) {
            map.put("pjs.mode", commandLine.getOptionValue("mode"));
        }
        return map;
    }

    static void main(String[] args) throws ParseException, IOException {
        StartupUtils.loadApplicationProperties();
        LoggingUtils.setLogbackLevelsFromProperties(System.getProperties());

        var commandLineArgs = new DefaultParser()
                .parse(options, args);

        if (commandLineArgs.hasOption("help")) {
            var helpFormatter = HelpFormatter.builder()
                    .setShowSince(false)
                    .get();
            helpFormatter.printOptions(options);
            return;
        }

        try(DeviceRegistry registry = switch (commandLineArgs.getOptionValue("plugin")) {
            case "grpc" -> {
                var channel = Grpc.newChannelBuilderForAddress(
                        commandLineArgs.getOptionValue("grpc-host", "localhost"),
                        commandLineArgs.getParsedOptionValue("grpc-port", 9090),
                        InsecureChannelCredentials.create()
                ).build();

                yield new GrpcDeviceRegistry(channel);
            }
            case "http" -> new HttpDeviceRegistry(
                    commandLineArgs.getOptionValue("grpc-host"),
                    commandLineArgs.getParsedOptionValue("grpc-port", 9090)
            );
            case "ffm" -> new NativeDeviceRegistryLoader().load();
            case "pi4j" -> {
                Class<? extends Plugin> pluginClass = switch (commandLineArgs.getOptionValue("mode")) {
                    case "mock" -> MockPlugin.class;
                    case "ffm" -> FFMPlugin.class;
                    case "grpc" -> GrpcPlugin.class;
                    case null, default -> null;
                };
                var loader = new Pi4jDeviceRegistryLoader(pluginClass);
                yield loader.load(optionsAsSystemProperties(commandLineArgs));
            }
            default -> new MockDeviceRegistry();
        }) {
            var registryDelegate = (commandLineArgs.hasOption("logging"))
                    ? new LoggingDeviceRegistry(registry)
                    : registry;

            Runnable example;
            if (commandLineArgs.hasOption("gpio")) {
                example = new GpioExample(registryDelegate);
            } else if (commandLineArgs.hasOption("i2c")) {
                example = new I2CExample(registryDelegate);
            } else if (commandLineArgs.hasOption("seven-segment")) {
                example = new SevenSegmentExample(registryDelegate);
            } else if (commandLineArgs.hasOption("spi")) {
                example = new SpiExample(registryDelegate);
            } else if (commandLineArgs.hasOption("eeprom")) {
                example = new EepromExample(registryDelegate);
            } else if (commandLineArgs.hasOption("328")) {
                example = new ThreeToEightExample(registryDelegate);
            } else if (commandLineArgs.hasOption("pwm")) {
                example = new PwmExample(registryDelegate);
            } else if (commandLineArgs.hasOption("mcp")) {
                example = new McpInterruptExample(registryDelegate);
            } else if (commandLineArgs.hasOption("oled")) {
                example = new OledExample(registryDelegate);
            } else if (commandLineArgs.hasOption("debounce")) {
                example = new DebounceTester(registryDelegate);
            } else if (commandLineArgs.hasOption("rotary")) {
                example = new RotaryEncoderExample(registryDelegate);
            } else {
                example = () -> logger.info("No example selected");
            }

            example.run();
        }

    }
}
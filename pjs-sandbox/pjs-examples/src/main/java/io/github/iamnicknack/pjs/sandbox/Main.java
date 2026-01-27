package io.github.iamnicknack.pjs.sandbox;

import com.pi4j.Pi4J;
import io.github.iamnicknack.pjs.sandbox.example.EepromExample;
import io.github.iamnicknack.pjs.sandbox.example.GpioExample;
import io.github.iamnicknack.pjs.sandbox.example.I2CExample;
import io.github.iamnicknack.pjs.sandbox.example.McpInterruptExample;
import io.github.iamnicknack.pjs.sandbox.example.OledExample;
import io.github.iamnicknack.pjs.sandbox.example.PwmExample;
import io.github.iamnicknack.pjs.sandbox.example.SevenSegmentExample;
import io.github.iamnicknack.pjs.sandbox.example.SpiExample;
import io.github.iamnicknack.pjs.sandbox.example.ThreeToEightExample;
import io.github.iamnicknack.pjs.ffm.NativeDeviceRegistry;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.grpc.GrpcDeviceRegistry;
import io.github.iamnicknack.pjs.http.client.HttpDeviceRegistry;
import io.github.iamnicknack.pjs.logging.LoggingDeviceRegistry;
import io.github.iamnicknack.pjs.mock.MockDeviceRegistry;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.pi4j.Pi4jDeviceRegistry;
import io.github.iamnicknack.pjs.util.args.CommandLineParser;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;

import static io.github.iamnicknack.pjs.sandbox.CommandLineOptions.*;

public class Main {

    static final Logger logger = LoggerFactory.getLogger(Main.class);

    static final CommandLineParser commandLineParser = new CommandLineParser.Builder()
            .arg(PLUGIN)
            .arg(GRPC_HOST)
            .arg(GRPC_PORT)
            .arg(GPIO_EXAMPLE)
            .arg(I2C_EXAMPLE)
            .arg(SEVEN_SEGMENT_EXAMPLE)
            .arg(SPI_EXAMPLE)
            .arg(EEPROM_EXAMPLE)
            .arg(THREE2EIGHT_EXAMPLE)
            .arg(PWM_EXAMPLE)
            .arg(MCP_EXAMPLE)
            .arg(OLED_EXAMPLE)
            .arg(LOGGING)
            .arg(HELP)
            .build();

    public static void main(String[] args) {

        var commandLineArgs = commandLineParser.parse(args);

        if (commandLineArgs.flag(HELP)) {
            commandLineParser.help(System.out);
            return;
        }

        try(DeviceRegistry registry = switch (commandLineArgs.value(PLUGIN)) {
            case "grpc" -> {
                var channel = Grpc.newChannelBuilderForAddress(
                        commandLineArgs.value(GRPC_HOST),
                        Integer.parseInt(commandLineArgs.value(GRPC_PORT)),
                        InsecureChannelCredentials.create()
                ).build();

                yield new GrpcDeviceRegistry(channel);
            }
            case "http" -> new HttpDeviceRegistry("http://" + commandLineArgs.value(GRPC_HOST) + ":" + commandLineArgs.value(GRPC_PORT) + "/");
            case "ffm" -> new NativeDeviceRegistry(new NativeContext(Arena.ofAuto()));
            case "pi4j" -> new Pi4jDeviceRegistry(Pi4J.newAutoContext());
            default -> new MockDeviceRegistry();
        }) {

            var registryDelegate = (commandLineArgs.flag(LOGGING))
                    ? new LoggingDeviceRegistry(registry)
                    : registry;

            Runnable example;
            if (commandLineArgs.flag(GPIO_EXAMPLE.getName())) {
                example = new GpioExample(registryDelegate);
            } else if (commandLineArgs.flag(I2C_EXAMPLE.getName())) {
                example = new I2CExample(registryDelegate);
            } else if (commandLineArgs.flag(SEVEN_SEGMENT_EXAMPLE.getName())) {
                example = new SevenSegmentExample(registryDelegate);
            } else if (commandLineArgs.flag(SPI_EXAMPLE.getName())) {
                example = new SpiExample(registryDelegate);
            } else if (commandLineArgs.flag(EEPROM_EXAMPLE.getName())) {
                example = new EepromExample(registryDelegate);
            } else if (commandLineArgs.flag(THREE2EIGHT_EXAMPLE.getName())) {
                example = new ThreeToEightExample(registryDelegate);
            } else if (commandLineArgs.flag(PWM_EXAMPLE.getName())) {
                example = new PwmExample(registryDelegate);
            } else if (commandLineArgs.flag(MCP_EXAMPLE.getName())) {
                example = new McpInterruptExample(registryDelegate);
            } else if (commandLineArgs.flag(OLED_EXAMPLE.getName())) {
                example = new OledExample(registryDelegate);
            } else {
                example = () -> logger.info("No example selected");
            }

            example.run();
        }

    }
}
package io.github.iamnicknack.pjs.sandbox;

import io.github.iamnicknack.pjs.util.args.CommandLineArgument;

public class CommandLineOptions {
    private CommandLineOptions() {}

    public static final CommandLineArgument PLUGIN = new CommandLineArgument.Builder()
            .name("plugin")
            .defaultValue("mock")
            .description("Which plugin to load. (`mock`, `grpc`, `ffm`, `pi4j`)")
            .property("pjs.plugin")
            .build();

    public static final CommandLineArgument PI4J_MODE = new CommandLineArgument.Builder()
            .name("pi4j-mode")
            .defaultValue("auto")
            .description("The Pi4J mode to use. (`mock`, `grpc`, `ffm`, `auto`)")
            .property("pjs.pi4j.mode")
            .build();

    public static final CommandLineArgument GRPC_HOST = new CommandLineArgument.Builder()
            .name("grpc-host")
            .defaultValue("localhost")
            .description("The gRPC host to connect to.")
            .property("pjs.grpc.host")
            .build();

    public static final CommandLineArgument GRPC_PORT = new CommandLineArgument.Builder()
            .name("grpc-port")
            .defaultValue("9090")
            .description("The gRPC port to connect to.")
            .property("pjs.grpc.port")
            .build();

    public static final CommandLineArgument GPIO_EXAMPLE = new CommandLineArgument.Builder()
            .name("gpio")
            .isFlag(true)
            .description("Run the GPIO example")
            .property("pjs.example.gpio")
            .build();

    public static final CommandLineArgument I2C_EXAMPLE = new CommandLineArgument.Builder()
            .name("i2c")
            .isFlag(true)
            .description("Run the i2c example")
            .property("pjs.example.i2c")
            .build();

    public static final CommandLineArgument SEVEN_SEGMENT_EXAMPLE = new CommandLineArgument.Builder()
            .name("seven-segment")
            .isFlag(true)
            .description("Run the seven-segment example")
            .property("pjs.example.seven-segment")
            .build();

    public static final CommandLineArgument SPI_EXAMPLE = new CommandLineArgument.Builder()
            .name("spi")
            .isFlag(true)
            .description("Run the spi example")
            .property("pjs.example.spi")
            .build();

    public static final CommandLineArgument EEPROM_EXAMPLE = new CommandLineArgument.Builder()
            .name("eeprom")
            .isFlag(true)
            .description("Run the eeprom example")
            .property("pjs.example.eeprom")
            .build();

    public static final CommandLineArgument THREE2EIGHT_EXAMPLE = new CommandLineArgument.Builder()
            .name("328")
            .isFlag(true)
            .description("Run the 328 decoder example")
            .property("pjs.example.328")
            .build();

    public static final CommandLineArgument PWM_EXAMPLE = new CommandLineArgument.Builder()
            .name("pwm")
            .isFlag(true)
            .description("Run the PWM example")
            .property("pjs.example.pwm")
            .build();

    public static final CommandLineArgument MCP_EXAMPLE = new CommandLineArgument.Builder()
            .name("mcp")
            .isFlag(true)
            .description("Run the MCP example")
            .property("pjs.example.mcp")
            .build();

    public static final CommandLineArgument OLED_EXAMPLE = new CommandLineArgument.Builder()
            .name("oled")
            .isFlag(true)
            .description("Run the OLED example")
            .property("pjs.example.oled")
            .build();

    public static final CommandLineArgument LOGGING = new CommandLineArgument.Builder()
            .name("logging")
            .isFlag(true)
            .description("Debug logging for IO operations")
            .build();

    public static final CommandLineArgument HELP = new CommandLineArgument.Builder()
            .name("help")
            .isFlag(true)
            .description("Show this help message")
            .build();
}

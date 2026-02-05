package io.github.iamnicknack.pjs.util.args

/**
 * Helper class for accessing parsed command line arguments.
 */
class CommandLineArgs(
    val values: Map<String, CommandLineArgument>,
) {
    /**
     * @param arg the argument name
     * @param T the type of the argument value
     * @return the value of the argument as [T]
     */
    inline fun <reified T> value(arg: String): T {
        return values[arg]?.typed<T>()
            ?: throw IllegalArgumentException("Missing value for argument: $arg")
    }

    /**
     * @param arg the argument
     * @param T the type of the argument value
     * @return the value of the argument as [T]
     */
    inline fun <reified T> value(arg: CommandLineArgument): T = value<T>(arg.name)

    /**
     * @param arg the argument name
     * @param T the type of the argument value
     * @return the value of the argument as [T] or null if the argument is not present
     */
    inline fun <reified T> valueOrNull(arg: String): T? {
        return values[arg]?.typed<T>()
    }

    /**
     * @param arg the argument
     * @param T the type of the argument value
     * @return the value of the argument as [T] or null if the argument is not present
     */
    inline fun <reified T> valueOrNull(arg: CommandLineArgument): T? = valueOrNull<T>(arg.name)

    /**
     * @param arg the argument name
     * @return the value of the argument
     * @Throws IllegalArgumentException if the argument is not present
     */
    fun value(arg: String): String {
        return values[arg]?.stringValue()
            ?: throw IllegalArgumentException("Missing value for argument: $arg")
    }

    /**
     * @param arg the argument
     * @return the value of the argument
     * @Throws IllegalArgumentException if the argument is not present
     */
    fun value(arg: CommandLineArgument): String = value(arg.name)

    /**
     * @param arg the argument name
     * @return the value of the argument or null if the argument is not present
     */
    fun valueOrNull(arg: String): String? {
        return values[arg]?.stringValue()
    }

    /**
     * @param arg the argument
     * @return the value of the argument or null if the argument is not present
     */
    fun valueOrNull(arg: CommandLineArgument): String? = valueOrNull(arg.name)

    /**
     * @param arg the flag name
     * @return true if the flag is present, false otherwise
     */
    fun flag(arg: String): Boolean {
        return values[arg]?.typed<Boolean>() ?: false
    }

    /**
     * @param arg the flag argument
     * @return true if the flag is present, false otherwise
     */
    fun flag(arg: CommandLineArgument): Boolean = flag(arg.name)

    /**
     * Check if the argument is present in the parsed arguments.
     * @param arg the argument name
     * @return true if the argument is present, false otherwise
     */
    fun contains(arg: String): Boolean = values.containsKey(arg)

    /**
     * Check if the argument is present in the parsed arguments.
     * @param arg the argument
     * @return true if the argument is present, false otherwise
     */
    fun contains(arg: CommandLineArgument): Boolean = values.containsKey(arg.name)

    /**
     * Return the parsed command line arguments as a map of system properties
     * @return System properties map of parsed arguments.
     */
    fun asMap(): Map<String, Any> {
        return values.values
            .mapNotNull { arg ->
                if (arg.property != null && arg.value != null) {
                    arg.property to arg.value
                } else {
                    null
                }
            }
            .toMap()
    }

    /**
     * Add the parsed command line arguments to existing system properties
     * @return System properties appended with parsed arguments.
     */
    fun asSystemProperties() = System.getProperties().apply { putAll(asMap()) }
}
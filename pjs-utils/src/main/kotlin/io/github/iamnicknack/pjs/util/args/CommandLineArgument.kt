package io.github.iamnicknack.pjs.util.args

/**
 * Container for details of a command line argument.
 * @param name the name of the argument
 * @param defaultValue the default value of the argument
 * @param description the description of the argument
 * @param property the system property name to use for the argument
 * @param isFlag whether the argument is a flag (i.e. no value associated with it)
 * @param value the value of the argument (or null if arguments are not yet parsed)
 */
data class CommandLineArgument(
    val name: String,
    val defaultValue: String? = null,
    val description: String? = null,
    val property: String? = null,
    val isFlag: Boolean = false,
    val value: String? = defaultValue,
) {
    inline fun <reified T> typed(): T? {
        return when (T::class) {
            String::class -> (value ?: defaultValue) as T?
            Int::class -> (value ?: defaultValue)?.toInt() as T?
            Boolean::class -> (value ?: defaultValue)?.toBoolean() as T?
            Any::class -> value as T?
            else -> null
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> typed(type: Class<T>): T? = when (type) {
        String::class.java -> (value ?: defaultValue) as T?
        Int::class.java -> ((value ?: defaultValue)?.toInt()) as T?
        Boolean::class.java -> ((value ?: defaultValue)?.toBoolean()) as T?
        else -> null
    }

    fun stringValue(): String? = value ?: defaultValue

    class Builder() {
        private var name: String? = null
        private var defaultValue: String? = null
        private var description: String? = null
        private var property: String? = null
        private var isFlag: Boolean = false
        private var value: String? = null

        fun name(name: String) = apply { this.name = name }

        fun description(description: String) = apply { this.description = description }

        fun property(systemProperty: String) = apply { this.property = systemProperty }

        fun isFlag(isFlag: Boolean) = apply { this.isFlag = isFlag }

        fun defaultValue(value: String) = apply { this.defaultValue = value }

        fun defaultValue(value: Int) = apply { this.defaultValue = value.toString() }

        fun defaultValue(value: Boolean) = apply {
            this.defaultValue = value.toString()
            this.isFlag = true
        }

        fun build(): CommandLineArgument = CommandLineArgument(
            name = name ?: throw IllegalArgumentException("name is required"),
            defaultValue = defaultValue,
            description = description,
            property = property,
            isFlag = isFlag,
            value = value
        )

    }
}
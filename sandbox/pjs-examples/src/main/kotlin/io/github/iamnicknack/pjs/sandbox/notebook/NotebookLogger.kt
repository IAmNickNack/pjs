package io.github.iamnicknack.pjs.sandbox.notebook

import org.slf4j.LoggerFactory
import org.slf4j.spi.SLF4JServiceProvider

object NotebookLogger {

    fun reconfigureSlf4jSimple(
        loader: ClassLoader = this.javaClass.classLoader,
        providerClass: String = "org.slf4j.simple.SimpleServiceProvider",
    ) {
        reconfigureSlf4j(
            loader = loader,
            providerClass = providerClass
        ) {
            System.setProperty("org.slf4j.simpleLogger.logFile", "System.out")
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug")
            System.setProperty("org.slf4j.simpleLogger.showDateTime", "false")
            System.setProperty("org.slf4j.simpleLogger.showThreadName", "false")
            System.setProperty("org.slf4j.simpleLogger.showShortLogName", "false")
            System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true")

            System.setProperty("org.slf4j.simpleLogger.log.io.github.iamnicknack", "debug")
            System.setProperty("org.slf4j.simpleLogger.log.mock-setup", "debug")
            System.setProperty("org.slf4j.simpleLogger.log.notebook", "debug")
        }
    }

    /**
     * Used by Kotlin notebooks to configure SLF4J for logging from notebook code.
     *
     * This allows application log messages to be written to the notebook console.
     */
    fun reconfigureSlf4j(
        loader: ClassLoader = this.javaClass.classLoader,
        providerClass: String = "ch.qos.logback.classic.spi.LogbackServiceProvider",
        preconfigure: () -> Unit = {
            System.setProperty("logback.configurationFile", "logback-console.xml")
        }
    ) {
        preconfigure()
        try {
            // Find the provider class
            val providerClass = Class.forName(providerClass, true, loader)
//            val providerClass = Class.forName("org.slf4j.simple.SimpleServiceProvider", true, loader)
//            val providerClass = Class.forName("org.slf4j.simple.SimpleServiceProvider", true, loader)
            val provider = providerClass.getDeclaredConstructor().newInstance() as SLF4JServiceProvider

            // Initialise it manually
            provider.initialize()

            // Inject it into LoggerFactory using reflection
            val factoryClass = LoggerFactory::class.java

            // For SLF4J 2.x:
            val providerField = factoryClass.getDeclaredField("PROVIDER")
            providerField.isAccessible = true
            providerField.set(null, provider)

            // Reset state to "INITIALIZED" (3 or 4 depending on version, usually 3=SUCCESSFUL_INITIALIZATION)
            val stateField = factoryClass.getDeclaredField("INITIALIZATION_STATE")
            stateField.isAccessible = true
            stateField.setInt(null, 3) // 3 = SUCCESSFUL_INITIALIZATION

            println("FORCE-LOADED SLF4J Provider: " + provider.requestedApiVersion)
        } catch (e: Exception) {
            println("Could not force-load provider: $e")
            e.printStackTrace(System.err)
        }
    }
}
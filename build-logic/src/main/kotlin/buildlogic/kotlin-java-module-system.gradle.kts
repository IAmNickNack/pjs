package buildlogic

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("buildlogic.kotlin-core")
}

/**
 * Extension for configuring the Java Module System.
 * @property moduleName the module name to patch
 */
open class JavaModuleSystemExtension {
    var moduleName: String? = null
}

/**
 * Local reference to the [JavaModuleSystemExtension]
 */
val javaModuleExt = extensions.create("javaModuleSystem", JavaModuleSystemExtension::class.java)

/**
 * Apply the Java Module System patch to the java compiler
 */
tasks.named<JavaCompile>("compileJava") {
    options.compilerArgumentProviders += object : CommandLineArgumentProvider {

        @InputFiles
        @PathSensitive(PathSensitivity.RELATIVE)
        val kotlinClasses = tasks.named<KotlinCompile>("compileKotlin")
            .flatMap(KotlinCompile::destinationDirectory)

        override fun asArguments(): Iterable<String> {
            val module = javaModuleExt.moduleName
                ?: throw IllegalStateException("`javaModuleSystem.moduleName` must be set in the module build script")

            return listOf(
                "--patch-module",
                "$module=${kotlinClasses.get().asFile.absolutePath}",
            )
        }
    }
}
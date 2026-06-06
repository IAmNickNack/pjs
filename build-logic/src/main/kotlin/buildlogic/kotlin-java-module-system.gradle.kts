package buildlogic

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("buildlogic.kotlin-core")
}

/**
 * Extension for configuring the Java Module System.
 * @property moduleName the module name to patch
 */
abstract class JavaModuleSystemExtension @Inject constructor() : CommandLineArgumentProvider {
    @get:Input
    abstract val moduleName: Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val kotlinClassesDir: DirectoryProperty

    override fun asArguments(): Iterable<String> =
        listOf("--patch-module", "${moduleName.get()}=${kotlinClassesDir.get().asFile.absolutePath}")
}

/**
 * Local reference to the [JavaModuleSystemExtension]
 */
val javaModuleExt = extensions.create("javaModuleSystem", JavaModuleSystemExtension::class.java)

/**
 * Apply the Java Module System patch to the java compiler
 */
tasks.named<JavaCompile>("compileJava") {
    val compileKotlin = tasks.named<KotlinCompile>("compileKotlin")

    val provider = objects.newInstance(JavaModuleSystemExtension::class.java).apply {
        moduleName.set(javaModuleExt.moduleName)
        kotlinClassesDir.set(compileKotlin.flatMap { it.destinationDirectory })
    }

    options.compilerArgumentProviders.add(provider)
}
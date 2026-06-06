package buildlogic

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project

fun Project.withVersionCatalog(block: WithVersionCatalog.() -> Unit) {
    RealWithVersionCatalog(this).block()
}

val Project.buildVersion: String
    get() = BuildVersion(this).version

val Project.librariesForLibs: LibrariesForLibs
    get() {
        return (this as org.gradle.api.plugins.ExtensionAware)
            .extensions.getByName("libs") as? LibrariesForLibs
            ?: error("Version catalog 'libs' is not available in this project.")
    }

interface WithVersionCatalog {
    val project: Project
    val Project.libs: LibrariesForLibs
        get() = librariesForLibs
}

private class RealWithVersionCatalog(
    override val project: Project
) : WithVersionCatalog

private class BuildVersion(
    val project: Project
) {
    val environmentVersion: String? by lazy {
        project.providers.environmentVariable("VERSION").orNull
            ?.takeIf { it.isNotBlank() }
    }

    val propertyVersion: String? by lazy {
        project.providers.gradleProperty("version").orNull
            ?.takeIf { it.isNotBlank() }
    }

    val version: String by lazy {
        environmentVersion
            ?: propertyVersion
            ?: project.librariesForLibs.versions.buildVersion.get()
    }
}
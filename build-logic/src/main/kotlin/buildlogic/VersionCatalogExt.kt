package buildlogic

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project

fun Project.withVersionCatalog(block: WithVersionCatalog.() -> Unit) {
    RealWithVersionCatalog(this).block()
}

val Project.buildVersion: String
    get() = providers.gradleProperty("version")
        .getOrElse(librariesForLibs.versions.buildVersion.get())
        .takeIf { it.isNotBlank() }
        ?: librariesForLibs.versions.buildVersion.get()

val Project.librariesForLibs: LibrariesForLibs get() {
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

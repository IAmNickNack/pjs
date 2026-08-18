package buildlogic

import java.nio.file.Files

plugins {
    java
    checkstyle
    pmd
}

dependencies {
    withVersionCatalog {
        implementation(libs.jspecify)
        implementation(libs.slf4j.api)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

pmd {
    isConsoleOutput = true
    toolVersion = "7.26.0"
    isIgnoreFailures = true
    rulesMinimumPriority = 2
}

checkstyle {
    toolVersion = "13.10.0"
    configFile = findUpwards("config/checkstyle/checkstyle.xml")?.toFile() ?: return@checkstyle
}

tasks.withType<Checkstyle>().configureEach {
    exclude("**/module-info.java")
    exclude("**/generated/**")
}

fun findUpwards(relativePath: String, startDir: java.nio.file.Path = project.projectDir.toPath()): java.nio.file.Path? {
    val candidate = startDir.resolve(relativePath).normalize()
    if (Files.exists(candidate)) return candidate

    val parent = startDir.parent ?: return null
    return findUpwards(relativePath, parent)
}

import buildlogic.buildVersion

plugins {
    base
    id("buildlogic.gradle-versions")
}

subprojects {
    group = "io.github.iamnicknack"
    version = rootProject.version
}

tasks.register("printVersion") {
    description = "Output the effective version"

    val v = rootProject.version.toString()
        .takeIf { it.isNotBlank() && it != "unspecified" }
        ?: buildVersion

    doLast {
        println("version: $v")
    }
}
import buildlogic.buildVersion

plugins {
    base
    id("buildlogic.gradle-versions")
}

version = buildVersion

subprojects {
    group = "io.github.iamnicknack"
    if (this.projectDir.path.contains("pjs/sandbox")) {
        version = rootProject.version.toString() + "-SNAPSHOT"
    } else {
        version = rootProject.version.toString()
    }
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

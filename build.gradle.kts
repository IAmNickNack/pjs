import buildlogic.withVersionCatalog

plugins {
    base
    id("buildlogic.gradle-versions")
}

subprojects {
    group = "io.github.iamnicknack"
    version = rootProject.version
}

tasks.register("printVersion") {
    doLast {
        println("version: ${libs.versions.pjs.get()}")
    }
}
plugins {
    base
    id("buildlogic.gradle-versions")
}

subprojects {
    group = "io.github.iamnicknack"
    version = rootProject.version
}

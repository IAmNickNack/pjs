plugins {
    base
}

tasks.named("build") {
    dependsOn(subprojects.map { it.tasks.named("build") })
}

tasks.named("clean") {
    dependsOn(subprojects.map { it.tasks.named("clean") })
}

subprojects {
    group = "io.github.iamnicknack"
    version = properties["version"].toString().let {
        if (it.endsWith("-SNAPSHOT")) it else "$it-SNAPSHOT"
    }
}

plugins {
    base
    id("buildlogic.included-build")
    id("buildlogic.gradle-versions")
}

subprojects {
    group = "io.github.iamnicknack"
//    version = properties["version"].toString().let {
//        if (it.endsWith("-SNAPSHOT")) it else "$it-SNAPSHOT"
//    }
}

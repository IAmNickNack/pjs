plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

dependencies {
    api(project(":pjs-core"))
    compileOnly(libs.pi4j.core) // allow the pi4j version to be specified by the user
    testImplementation(libs.pi4j.plugin.mock)
}

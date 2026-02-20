plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

dependencies {
    api(project(":pjs-core"))
    api(libs.pi4j.core)
    implementation(libs.bundles.pi4j.plugins)
    testImplementation(libs.pi4j.plugin.mock)
}

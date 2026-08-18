plugins {
    id("buildlogic.repositories")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.logging")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

dependencies {
    implementation(libs.logback.classic)
    implementation(libs.jackson.databind)
    implementation(libs.kotlin.coroutines.core)
    implementation(project(":pjs-core"))
    implementation(project(":pjs-utils"))
}

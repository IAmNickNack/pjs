plugins {
    id("buildlogic.repositories")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.kotlin-core")
    id("buildlogic.logging")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

dependencies {
    api(project(":pjs-core"))
    implementation(project(":providers:pjs-mock-device"))
}
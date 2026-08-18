plugins {
    id("buildlogic.repositories")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.kotlin-core")
    id("buildlogic.logging")
    id("buildlogic.java-library")
}

dependencies {
    api(project(":pjs-core"))
    implementation(project(":pjs-mock-device"))
}
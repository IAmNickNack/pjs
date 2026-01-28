plugins {
    id("buildlogic.repositories")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

dependencies {
    implementation(project(":pjs-utils"))
    implementation(project(":pjs-mock-device"))
    implementation(libs.slf4j.api)
}


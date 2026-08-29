plugins {
    id("buildlogic.repositories")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

dependencies {
    implementation(project(":pjs-utils"))
    implementation(project(":providers:pjs-mock-device"))
    implementation(libs.slf4j.api)
    implementation(libs.apache.cli)
    implementation(project(":sandbox:pjs-device-factory-validation"))
}


plugins {
    id("buildlogic.repositories")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

dependencies {
    implementation("io.github.iamnicknack:pjs-utils")
    implementation(project(":pjs-mock-device"))
    implementation(libs.slf4j.api)
    implementation(libs.apache.cli)
}


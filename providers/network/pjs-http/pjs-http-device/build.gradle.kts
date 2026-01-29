plugins {
    id("buildlogic.repositories")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.logging")
    id("buildlogic.ktor.ktor-client")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

dependencies {
    implementation(libs.logback.classic)
    implementation(project(":pjs-http-common"))
    implementation(project(":pjs-core"))
    implementation(project(":pjs-utils"))

    testImplementation(project(":pjs-mock-device"))
    testImplementation(project(":pjs-http-server"))
    testImplementation(libs.koin.ktor)
}


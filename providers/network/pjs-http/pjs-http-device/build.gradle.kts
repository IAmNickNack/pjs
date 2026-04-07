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
    implementation("io.github.iamnicknack:pjs-core")
    implementation("io.github.iamnicknack:pjs-utils")

    testImplementation(project(":pjs-mock-device"))
    testImplementation(project(":pjs-http-server"))
    testImplementation(libs.koin.ktor)
}

tasks.withType<Test>().configureEach {
    retry {
        maxRetries = 3
    }
}

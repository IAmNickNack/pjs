plugins {
    id("buildlogic.repositories")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.kotlin-core")
    id("buildlogic.logging")
    id("buildlogic.java-library")
}

dependencies {
    api("io.github.iamnicknack:pjs-core")
    implementation("io.github.iamnicknack:pjs-mock-device")
}
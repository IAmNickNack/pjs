plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    `java-library`
}

dependencies {
    api("io.github.iamnicknack:pjs-core:0.0.0")
    testImplementation("io.github.iamnicknack:pjs-mock-device:0.0.0")
}
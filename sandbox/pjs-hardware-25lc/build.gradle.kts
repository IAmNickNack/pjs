plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    `java-library`
}

dependencies {
    api("io.github.iamnicknack:pjs-core:${project.version}")
    testImplementation("io.github.iamnicknack:pjs-mock-device:${project.version}")
}
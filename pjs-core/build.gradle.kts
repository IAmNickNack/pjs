plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    id("buildlogic.logging")
    id("buildlogic.java-library")
}

dependencies {
    api(project(":pjs-utils"))
    testImplementation(project(":pjs-mock-device"))
}
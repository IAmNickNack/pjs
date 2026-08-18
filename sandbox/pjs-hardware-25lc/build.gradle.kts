plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    `java-library`
}

dependencies {
    api(project(":pjs-core"))
    testImplementation(project(":providers:pjs-mock-device"))
}

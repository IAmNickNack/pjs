plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    id("buildlogic.logging")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

dependencies {
    api(project(":pjs-utils"))
    testImplementation(project(":providers:pjs-mock-device"))
}

subprojects.forEach {
    println("Adding ${it.name} to ${project.name} dependencies")
}
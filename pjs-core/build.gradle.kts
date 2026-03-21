import buildlogic.withVersionCatalog
import org.gradle.api.internal.artifacts.dependencies.DefaultMutableVersionConstraint.withVersion

plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    id("buildlogic.logging")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

dependencies {
    api("io.github.iamnicknack:pjs-utils")
    testImplementation("io.github.iamnicknack:pjs-mock-device")
}
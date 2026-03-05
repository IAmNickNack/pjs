plugins {
    id("buildlogic.repositories")
    id("buildlogic.test.test-java")
    id("buildlogic.logging")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

dependencies {
    compileOnly(libs.logback.classic)
}
plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-java")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

dependencies {
    api(project(":pjs-core"))
    api(libs.slf4j.api)
}
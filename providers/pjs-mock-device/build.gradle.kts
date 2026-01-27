plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-java")
    id("buildlogic.java-library")
}

dependencies {
    api(project(":pjs-core"))
    api(libs.slf4j.api)
}
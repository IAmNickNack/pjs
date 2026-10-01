plugins {
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    `java-library`
}

dependencies {
    api(project(":pjs-core"))
}

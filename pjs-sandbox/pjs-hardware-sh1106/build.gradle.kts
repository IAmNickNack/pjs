plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    `java-library`
}

dependencies {
    api("io.github.iamnicknack:pjs-core:${properties["pjs.version"]}")
}
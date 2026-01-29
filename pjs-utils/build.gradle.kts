plugins {
    id("buildlogic.repositories")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.test.test-java")
    id("buildlogic.logging")
    id("buildlogic.kotlin-java-module-system")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

javaModuleSystem.moduleName = "pjs.util"
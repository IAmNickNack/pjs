package buildlogic

plugins {
    java
}

dependencies {
    withVersionCatalog {
        implementation(libs.slf4j.api)
        testRuntimeOnly(libs.logback.classic)
    }
}
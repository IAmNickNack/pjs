package buildlogic.test

import buildlogic.withVersionCatalog

plugins {
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-core")
    id("org.gradle.test-retry")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    withVersionCatalog {
        testImplementation(libs.kotlin.coroutines.test)
        testImplementation(libs.assertk)
    }
}

tasks.withType<Test>().configureEach {
    retry {
        maxRetries = 3
    }
}

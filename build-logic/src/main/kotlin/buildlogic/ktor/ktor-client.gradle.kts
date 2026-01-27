package buildlogic.ktor

import buildlogic.withVersionCatalog

plugins {
    id("buildlogic.kotlin-core")
}

dependencies {
    withVersionCatalog {
        implementation(platform(libs.ktor.bom))
    }
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-jackson")
    implementation("io.ktor:ktor-client-logging")

    testImplementation("io.ktor:ktor-server-test-host")
}

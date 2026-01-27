package buildlogic.ktor

import buildlogic.withVersionCatalog

plugins {
    id("buildlogic.kotlin-core")
}

dependencies {
    withVersionCatalog {
        implementation(platform(libs.ktor.bom))
    }
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-jackson")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-sse")
    implementation("io.ktor:ktor-server-swagger")

    testImplementation("io.ktor:ktor-server-test-host")
}

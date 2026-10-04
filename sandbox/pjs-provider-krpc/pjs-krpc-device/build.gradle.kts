plugins {
    id("buildlogic.kotlin-core")
    id("buildlogic.grpc.kotlin-krpc")
    id("buildlogic.logging")
    id("buildlogic.java-library")
}

dependencies {
    implementation(project(":pjs-core"))
    implementation(project(":sandbox:pjs-provider-krpc:pjs-krpc-proto"))
    implementation("org.jetbrains.kotlinx:kotlinx-rpc-grpc-client")
    implementation(libs.koin.core)
    implementation(libs.logback.classic)
}
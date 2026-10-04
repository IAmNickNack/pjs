plugins {
    id("buildlogic.kotlin-core")
    id("buildlogic.grpc.kotlin-krpc")
    id("buildlogic.logging")
    id("buildlogic.java-library")
}

dependencies {
    implementation(project(":pjs-core"))
    implementation(project(":sandbox:pjs-provider-krpc:pjs-krpc-proto"))
    implementation(libs.krpc.grpc.client)
    implementation(libs.koin.core)
    implementation(libs.logback.classic)
}
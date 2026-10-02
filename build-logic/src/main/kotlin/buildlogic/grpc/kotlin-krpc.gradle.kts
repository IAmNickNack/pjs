package buildlogic.grpc

import buildlogic.withVersionCatalog

plugins {
    id("buildlogic.kotlin-core")
    id("org.jetbrains.kotlinx.rpc.plugin")
}

dependencies {
    implementation(platform("org.jetbrains.kotlinx:kotlinx-rpc-bom:0.11.0-grpc-190"))
    implementation("org.jetbrains.kotlinx:kotlinx-rpc-grpc-core")
    implementation("org.jetbrains.kotlinx:kotlinx-rpc-protobuf")
    implementation("org.jetbrains.kotlinx:kotlinx-rpc-grpc-client")
    implementation("org.jetbrains.kotlinx:kotlinx-rpc-grpc-server")

    implementation("org.jetbrains.kotlinx:kotlinx-rpc-protobuf-jvm")

    withVersionCatalog {
        implementation(libs.grpc.netty)
    }
}

rpc {
    protoc()
}
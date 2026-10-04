package buildlogic.grpc

import buildlogic.withVersionCatalog

plugins {
    id("buildlogic.kotlin-core")
    id("org.jetbrains.kotlinx.rpc.plugin")
}

dependencies {
    withVersionCatalog {
        api(libs.krpc.grpc.core)
        api(libs.krpc.grpc.protobuf)
        implementation(libs.krpc.grpc.protobuf.jvm)
        implementation(libs.krpc.grpc.client)
        implementation(libs.krpc.grpc.server)
        implementation(libs.grpc.netty)
    }
}

rpc {
    protoc()
}
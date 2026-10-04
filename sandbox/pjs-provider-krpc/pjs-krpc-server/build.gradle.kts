plugins {
    id("buildlogic.kotlin-core")
    id("buildlogic.logging")
    application
}

dependencies {
    implementation(project(":sandbox:pjs-provider-krpc:pjs-krpc-proto"))
    implementation(project(":sandbox:pjs-provider-krpc:pjs-krpc-device"))
    implementation("org.jetbrains.kotlinx:kotlinx-rpc-grpc-server")
    implementation(project(":pjs-core"))
    implementation(project(":providers:pjs-mock-device"))
    implementation(project(":providers:pjs-pi4j-device"))
    implementation(project(":providers:pjs-native:pjs-native-device"))
    implementation(project(":providers:network:pjs-http:pjs-http-device"))
    implementation(project(":providers:network:pjs-network-common"))
    implementation(libs.koin.core)
    implementation(libs.logback.classic)
}

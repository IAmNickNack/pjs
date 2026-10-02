plugins {
    id("buildlogic.kotlin-core")
    id("buildlogic.logging")
    id("buildlogic.grpc.kotlin-krpc")
    application
}

dependencies {
    implementation(project(":pjs-core"))
    implementation(libs.logback.classic)
}

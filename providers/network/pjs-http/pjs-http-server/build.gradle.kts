plugins {
    id("buildlogic.repositories")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.logging")
    id("buildlogic.ktor.ktor-server")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
    application
}

dependencies {
    implementation(libs.logback.classic)
    implementation(libs.koin.ktor)
    implementation(libs.apache.cli)
    runtimeOnly(libs.bundles.pi4j.plugins)

    api(project(":pjs-core"))
    implementation(project(":providers:network:pjs-network-common"))
    implementation(project(":providers:network:pjs-http:pjs-http-common"))
    runtimeOnly(project(":providers:network:pjs-grpc:pjs-grpc-device"))
    runtimeOnly(project(":providers:pjs-native:pjs-native-device"))
    runtimeOnly(project(":providers:pjs-pi4j-device"))
    runtimeOnly(project(":providers:pjs-mock-device"))

}

application {
    mainClass.set("io.github.iamnicknack.pjs.http.server.ApplicationKt")
    applicationDefaultJvmArgs = listOf(
        "--sun-misc-unsafe-memory-access=allow",
        "--enable-native-access=ALL-UNNAMED"
    )
}
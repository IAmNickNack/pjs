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

version = libs.versions.pjs.get()

dependencies {
    implementation(libs.logback.classic)
    implementation(libs.koin.ktor)
    implementation(libs.apache.cli)
    runtimeOnly(libs.bundles.pi4j.plugins)

    api("io.github.iamnicknack:pjs-core")
    implementation(project(":pjs-network-common"))
    implementation(project(":pjs-http-common"))
    runtimeOnly(project(":pjs-native-device"))
    runtimeOnly(project(":pjs-grpc-device"))
    runtimeOnly(project(":pjs-pi4j-device"))
    runtimeOnly(project(":pjs-mock-device"))

}

application {
    mainClass.set("io.github.iamnicknack.pjs.http.server.ApplicationKt")
    applicationDefaultJvmArgs = listOf(
        "--sun-misc-unsafe-memory-access=allow",
        "--enable-native-access=ALL-UNNAMED"
    )
}
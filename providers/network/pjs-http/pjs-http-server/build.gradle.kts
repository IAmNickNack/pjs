plugins {
    id("buildlogic.repositories")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.logging")
    id("buildlogic.ktor.ktor-server")
    application
}

dependencies {
    implementation(libs.logback.classic)
    implementation(libs.koin.ktor)

    implementation(project(":network-common"))
    implementation(project(":pjs-http-common"))
    implementation(project(":pjs-native-device"))
    implementation(project(":pjs-grpc-device"))
    implementation(project(":pjs-pi4j-device"))
    implementation(project(":pjs-mock-device"))
    implementation(project(":pjs-utils"))
}

application {
    mainClass.set("io.github.iamnicknack.pjs.http.server.ApplicationKt")
    applicationDefaultJvmArgs = listOf(
        "--sun-misc-unsafe-memory-access=allow",
        "--enable-native-access=ALL-UNNAMED"
    )
}
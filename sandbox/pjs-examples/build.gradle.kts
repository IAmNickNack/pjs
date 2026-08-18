plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-java")
    id("buildlogic.grpc.grpc-kotlin")
    application
}

dependencies {
    api(project(":pjs-core"))
    implementation(project(":pjs-mock-device"))
    implementation(project(":pjs-native-device"))
    implementation(project(":pjs-grpc-device"))
    implementation(project(":pjs-http-device"))
    api(project(":pjs-pi4j-device"))

    implementation(project(":pjs-hardware-25lc"))
    implementation(project(":pjs-hardware-mcp23x"))
    implementation(project(":pjs-hardware-sh1106"))

    implementation(libs.bundles.logging)
    implementation(libs.bundles.pi4j.plugins)
    implementation(libs.apache.cli)
}

application {
    mainClass.set("io.github.iamnicknack.pjs.sandbox.Main")
    applicationDefaultJvmArgs = listOf(
        // currently required for grpc-netty until https://github.com/netty/netty/issues/14942
        // is incorporated into the grpc-netty artefact
        "--sun-misc-unsafe-memory-access=allow",
        // required for Pi4J
        "--enable-native-access=ALL-UNNAMED"
    )
}
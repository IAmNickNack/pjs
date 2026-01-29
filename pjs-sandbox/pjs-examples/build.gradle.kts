plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-java")
    id("buildlogic.grpc.grpc-kotlin")
    application
}

dependencies {
    api("io.github.iamnicknack.pjs:pjs-core:${properties["pjs.version"]}")
    implementation("io.github.iamnicknack.pjs:pjs-mock-device:${properties["pjs.version"]}")
    implementation("io.github.iamnicknack.pjs:pjs-native-device:${properties["pjs.version"]}")
    implementation("io.github.iamnicknack.pjs:pjs-grpc-device:${properties["pjs.version"]}")
    implementation("io.github.iamnicknack.pjs:pjs-http-device:${properties["pjs.version"]}")
    implementation("io.github.iamnicknack.pjs:pjs-pi4j-device:${properties["pjs.version"]}")

    implementation(project(":pjs-hardware-25lc"))
    implementation(project(":pjs-hardware-mcp23x"))
    implementation(project(":pjs-hardware-sh1106"))

    implementation(libs.pi4j.core)
    implementation(libs.pi4j.plugin.ffm)
    implementation(libs.bundles.logging)
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
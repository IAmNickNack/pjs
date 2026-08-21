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
    implementation(project(":providers:pjs-mock-device"))
    implementation(project(":providers:pjs-native:pjs-native-device"))
    implementation(project(":providers:network:pjs-grpc::pjs-grpc-device"))
    implementation(project(":providers:network:pjs-http:pjs-http-device"))
    api(project(":providers:pjs-pi4j-device"))

    implementation(project(":sandbox:pjs-hardware-25lc"))
    implementation(project(":sandbox:pjs-hardware-mcp23x"))
    implementation(project(":sandbox:pjs-hardware-sh1106"))

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

//detekt {
//    config.setFrom(files("$rootDir/config/detekt/disabled.yml"))
//    buildUponDefaultConfig = false
//}
//
//tasks.withType<Detekt>().configureEach {
//    reports {
//        checkstyle.required = false
//        html.required = false
//        markdown.required = true
//        sarif.required = true
//    }
//}


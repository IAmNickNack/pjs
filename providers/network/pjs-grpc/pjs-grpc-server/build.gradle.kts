plugins {
    id("buildlogic.repositories")
    id("buildlogic.grpc.grpc-kotlin")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.build-version")
    application
}

dependencies {
    implementation(project(":providers:network:pjs-network-common"))
    implementation(project(":providers:pjs-native:pjs-native-device"))
    implementation(project(":providers:network:pjs-grpc:pjs-grpc-device"))
    implementation(project(":providers:pjs-pi4j-device"))
    implementation(project(":providers:pjs-mock-device"))
    implementation(project(":pjs-utils"))
    implementation(libs.apache.cli)
    implementation(libs.bundles.logging)
    implementation(libs.grpc.inprocess)
    runtimeOnly(libs.bundles.pi4j.plugins)
}

tasks.named<JavaExec>("run") {
    mainClass = "io.github.iamnicknack.pjs.grpc.PjsGrpcServerKt"
    jvmArgs(
        "--sun-misc-unsafe-memory-access=allow",
        "--enable-native-access=ALL-UNNAMED"
    )
}

application {
    mainClass.set("io.github.iamnicknack.pjs.grpc.PjsGrpcServerKt")
    applicationDefaultJvmArgs = listOf(
        // currently required for grpc-netty until https://github.com/netty/netty/issues/14942
        // is incorporated into the grpc-netty artefact
        "--sun-misc-unsafe-memory-access=allow",
        // required for Pi4J
        "--enable-native-access=ALL-UNNAMED"
    )
}


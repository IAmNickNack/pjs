plugins {
    id("buildlogic.repositories")
    id("buildlogic.grpc.grpc-kotlin")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-kotlin")
    application
}

dependencies {
    implementation(project(":network-common"))
    implementation(project(":pjs-native-device"))
    implementation(project(":pjs-grpc-device"))
    implementation(project(":pjs-pi4j-device"))
    implementation(project(":pjs-mock-device"))
    implementation(project(":pjs-utils"))
    implementation(libs.bundles.logging)
    implementation(libs.bundles.pi4j)
    implementation(libs.grpc.inprocess)
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


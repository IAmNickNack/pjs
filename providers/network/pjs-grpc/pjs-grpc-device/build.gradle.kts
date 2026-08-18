plugins {
    id("buildlogic.repositories")
    id("buildlogic.grpc.grpc-kotlin")
    id("buildlogic.kotlin-core")
    id("buildlogic.logging")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.kotlin-java-module-system")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

javaModuleSystem.moduleName = "pjs.grpc"

dependencies {
    api(project(":pjs-core"))
    api(project(":providers:network:pjs-grpc:pjs-grpc-proto"))

    testImplementation(project(":providers:pjs-mock-device"))
    testImplementation(project(":providers:network:pjs-grpc:pjs-grpc-server"))
}

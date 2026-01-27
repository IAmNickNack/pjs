plugins {
    id("buildlogic.repositories")
    id("buildlogic.grpc.grpc-kotlin")
    id("buildlogic.kotlin-core")
    id("buildlogic.logging")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.kotlin-java-module-system")
    id("buildlogic.java-library")
}

javaModuleSystem.moduleName = "pjs.grpc"

dependencies {
    api(project(":pjs-core"))
    api(project(":pjs-grpc-proto"))

    testImplementation(project(":pjs-mock-device"))
    testImplementation(project(":pjs-grpc-server"))
}

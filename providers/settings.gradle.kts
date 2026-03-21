rootProject.name = "pjs-providers"

pluginManagement {
    includeBuild("../build-logic")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

includeBuild("..") {
    dependencySubstitution {
        alias("pjs-core")
        alias("pjs-utils")
    }
}

include(":pjs-mock-device")
include(":pjs-native-context")
include(":pjs-native-device")
include(":pjs-pi4j-device")
include(":pjs-grpc-server")
include(":pjs-grpc-device")
include(":pjs-grpc-proto")
include(":pjs-http-common")
include(":pjs-http-server")
include(":pjs-http-device")
include(":pjs-network-common")

project(":pjs-native-context").projectDir = file("pjs-native/pjs-native-context")
project(":pjs-native-device").projectDir = file("pjs-native/pjs-native-device")
project(":pjs-grpc-server").projectDir = file("network/pjs-grpc/pjs-grpc-server")
project(":pjs-grpc-device").projectDir = file("network/pjs-grpc/pjs-grpc-device")
project(":pjs-grpc-proto").projectDir = file("network/pjs-grpc/pjs-grpc-proto")
project(":pjs-http-common").projectDir = file("network/pjs-http/pjs-http-common")
project(":pjs-http-server").projectDir = file("network/pjs-http/pjs-http-server")
project(":pjs-http-device").projectDir = file("network/pjs-http/pjs-http-device")
project(":pjs-network-common").projectDir = file("network/pjs-network-common")

fun DependencySubstitutions.alias(moduleName: String) =
    substitute(module("io.github.iamnicknack:${moduleName}:${providers.gradleProperty("version").get()}"))
        .using(project(":$moduleName"))

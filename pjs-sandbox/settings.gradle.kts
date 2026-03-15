rootProject.name = "pjs-sandbox"

pluginManagement {
    includeBuild("../build-logic") // if you have convention plugins
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
        alias(":pjs-core")
        alias(":pjs-utils")
        alias(":pjs-mock-device")
        alias(":pjs-native-device")
        alias(":pjs-grpc-device")
        alias(":pjs-http-device")
        alias(":pjs-pi4j-device")
    }
}

include(
    "pjs-hardware-sh1106",
    "pjs-hardware-mcp23x",
    "pjs-hardware-25lc",
    "pjs-examples"
)

fun DependencySubstitutions.alias(moduleName: String) =
    substitute(module("io.github.iamnicknack${moduleName}:0.0.0"))
        .using(project(moduleName))

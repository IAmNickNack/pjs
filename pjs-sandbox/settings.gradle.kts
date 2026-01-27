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

val pjsVersion: String by settings

includeBuild("..") {
    dependencySubstitution {
        substitute(module("io.github.iamnicknack.pjs:pjs-core:$pjsVersion"))
            .using(project(":pjs-core"))
        substitute(module("io.github.iamnicknack.pjs:pjs-utils:$pjsVersion"))
            .using(project(":pjs-utils"))
        substitute(module("io.github.iamnicknack.pjs:pjs-mock-device:$pjsVersion"))
            .using(project(":pjs-mock-device"))

        substitute(module("io.github.iamnicknack.pjs:pjs-grpc-device:$pjsVersion"))
            .using(project(":pjs-grpc-device"))
        substitute(module("io.github.iamnicknack.pjs:pjs-http-device:$pjsVersion"))
            .using(project(":pjs-http-device"))
        substitute(module("io.github.iamnicknack.pjs:pjs-pi4j-device:$pjsVersion"))
            .using(project(":pjs-pi4j-device"))
        substitute(module("io.github.iamnicknack.pjs:pjs-native-device:$pjsVersion"))
            .using(project(":pjs-native-device"))
        substitute(module("io.github.iamnicknack.pjs:pjs-mock-device:$pjsVersion"))
            .using(project(":pjs-mock-device"))
    }
}

include(
    "pjs-device-sh1106",
    "pjs-device-mcp23x",
    "pjs-device-eeprom",
    "pjs-examples"
)


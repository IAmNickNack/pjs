rootProject.name = "pjs"

includeBuild("build-logic")
includeBuild("sandbox")
includeBuild("providers") {
    dependencySubstitution {
        substitute(module("io.github.iamnicknack:pjs-mock-device:"))
            .using(project(":pjs-mock-device"))
    }
}

include("pjs-core")
include("pjs-utils")

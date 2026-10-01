@file:Suppress("UnstableApiUsage")
package buildlogic

fun RepositoryHandler.configureRepositories() {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()

    exclusiveContent {
        filter {
            includeVersionByRegex("^com\\.pi4j$", "^pi4j-.*$", "^.*-SNAPSHOT$")
            includeVersionByRegex("^io\\.github\\.iamnicknack$", "^pi4j-plugin-grpc$", "^.*-SNAPSHOT$")
        }
        forRepository {
            maven {
                url = uri("https://central.sonatype.com/repository/maven-snapshots/")
                name = "SonatypeSnapshots"
            }
        }
    }

    exclusiveContent {
        filter {
            includeModuleByRegex("^org\\.jetbrains\\.kotlinx$", "^kotlinx-rpc-.*")
            includeModuleByRegex("^org\\.jetbrains\\.kotlinx$", "^protoc-gen-.*")
        }
        forRepository {
            maven {
                url = uri("https://redirector.kotlinlang.org/maven/kxrpc-grpc/")
                name = "kxrpc-grpc"
            }
        }
    }
}

settings.pluginManagement.repositories.configureRepositories()
settings.dependencyResolutionManagement.repositories.configureRepositories()

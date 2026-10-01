
pluginManagement {
    apply(from = "src/main/kotlin/buildlogic/repositories.settings.gradle.kts")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

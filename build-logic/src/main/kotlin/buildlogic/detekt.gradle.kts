package buildlogic

import dev.detekt.gradle.Detekt

plugins {
    kotlin("jvm")
    id("dev.detekt")
}

detekt {
    toolVersion = "2.0.0-alpha.6"
    baseline = file("detekt-baseline.xml") // $rootDir/config/detekt/
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    ignoreFailures = true
}

tasks.withType<Detekt>().configureEach {
    // Only run detekt when explicitly requested
//    onlyIf { gradle.startParameter.taskNames.any { it.contains("detekt")} }

    reports {
        checkstyle.required = false
        html.required = false
        markdown.required = true
        sarif.required = true
    }

    if (project.hasProperty("precommit")) {
        val rootDir = project.rootDir
        val projectDir = projectDir

        val fileCollection = files()

        setSource(
            getGitStagedFiles(rootDir)
                .map { stagedFiles ->
                    val stagedFilesFromThisProject = stagedFiles
                        .filter { it.startsWith(projectDir) }

                    @Suppress("SpreadOperator")
                    fileCollection.setFrom(*stagedFilesFromThisProject.toTypedArray())

                    fileCollection.asFileTree
                }
        )
    }
}

afterEvaluate {
    tasks.withType(Detekt::class.java).configureEach {
        val typeResolutionEnabled = !classpath.isEmpty
        if (typeResolutionEnabled && project.hasProperty("precommit")) {
            // We must exclude kts files from pre-commit hook to prevent detekt from crashing
            // This is a workaround for the https://github.com/detekt/detekt/issues/5501
            exclude("*.gradle.kts")
        }
    }
}

fun Project.getGitStagedFiles(rootDir: File): Provider<List<File>> {
    return providers.exec {
        commandLine("git", "--no-pager", "diff", "--name-only", "--cached")
    }.standardOutput.asText
        .map { outputText ->
            outputText.trim()
                .split("\n")
                .filter { it.isNotBlank() }
                .map { File(rootDir, it) }
        }
}
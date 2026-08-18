import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.name

rootProject.name = "pjs"

includeBuild("build-logic")

val projectPath = file(rootProject.projectDir.absolutePath).toPath()

val projects = Files.find(projectPath, 5, { path, _ -> path.endsWith("build.gradle.kts") })
    .filter { !Paths.get(it.parent.toString(), "settings.gradle.kts").exists() }
    .map { it.parent.name to projectPath.relativize(it.parent).toString() }
    .toList()

projects.forEach { (name, path) ->
    include(":$name")
    project(":$name").projectDir = file(path)
}

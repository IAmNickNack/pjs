package buildlogic

/**
 * Applied to an `includedBuild` to expose build and publish tasks to the root project.
 */
plugins {
    base
}

delegateNamed("assemble")
delegateNamed("build")
delegateNamed("clean")

delegateRegister("publishToMavenCentral", "publishing")
delegateRegister("publishToMavenLocal", "publishing")

fun delegateNamed(name: String) {
    tasks.named(name) {
        dependsOn(subprojects.map { it.tasks.named(name) })
    }
}

fun delegateRegister(name: String, groupName: String = "other") {
    tasks.register(name) {
        group = groupName
        dependsOn(
            subprojects
                .mapNotNull { it.tasks.findByName(name) }
        )
    }
}

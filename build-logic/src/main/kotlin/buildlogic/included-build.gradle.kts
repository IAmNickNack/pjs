package buildlogic

plugins {
    base
}

delegateNamed("build")
delegateNamed("clean")

delegateRegister("publish")
delegateRegister("publishToMavenLocal")

fun delegateNamed(name: String) =
    tasks.named(name) {
        dependsOn(subprojects.map { it.tasks.named(name) })
    }

fun delegateRegister(name: String) =
    tasks.register(name) {
        dependsOn(
            subprojects
                .filter { it.tasks.findByName(name) != null }
                .map { it.tasks.named(name) }
        )
    }


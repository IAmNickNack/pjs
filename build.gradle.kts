plugins {
    base
}

val pjsVersion: String by project

subprojects {
    group = "io.github.iamnicknack.pjs"
    version = pjsVersion
}

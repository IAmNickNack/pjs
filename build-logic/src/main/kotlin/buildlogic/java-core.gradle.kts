package buildlogic

plugins {
    java
    checkstyle
    pmd
}

dependencies {
    withVersionCatalog {
        implementation(libs.jspecify)
        implementation(libs.slf4j.api)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

pmd {
    isConsoleOutput = true
    toolVersion = "7.26.0"
    isIgnoreFailures = true
    rulesMinimumPriority = 2
    ruleSets = listOf(
        "${rootDir.path}/config/pmd/ruleset.xml"
    )
}

checkstyle {
    toolVersion = "13.10.0"
    configFile = file("${rootDir.path}/config/checkstyle/checkstyle.xml")
}

tasks.withType<Checkstyle>().configureEach {
    exclude("**/module-info.java")
    exclude("**/generated/**")
}

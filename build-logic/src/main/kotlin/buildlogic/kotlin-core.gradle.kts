package buildlogic

import dev.detekt.gradle.Detekt

plugins {
    kotlin("jvm")
    id("dev.detekt")
    id("buildlogic.java-core")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }

    jvmToolchain(25)
}

dependencies {
    withVersionCatalog {
        implementation(platform(libs.kotlin.bom))
        implementation("org.jetbrains.kotlin:kotlin-stdlib") {
            version {
                // force stdlib to avoid confusion with transitive dependencies
                strictly(libs.versions.kotlin.asProvider().get())
            }
        }
    }
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

detekt {
    toolVersion = "2.0.0-alpha.6"
    baseline = file("detekt-baseline.xml") // $rootDir/config/detekt/
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    ignoreFailures = true
}

tasks.withType<Detekt>().configureEach {
    reports {
        checkstyle.required = false
        html.required = false
        markdown.required = true
        sarif.required = true
    }
}

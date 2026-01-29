import kotlin.io.path.toPath

plugins {
    `kotlin-dsl`
    `version-catalog`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    api(files(libs.javaClass.protectionDomain.codeSource.location.toURI().toPath()))
    implementation(libs.protobuf.plugin)
    implementation(libs.gradle.test.retry)
    implementation(libs.publish.vanniktech)
    implementation(libs.dokka)
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib") {
        version {
            // force stdlib to avoid confusion with transitive dependencies
            strictly(libs.versions.kotlin.asProvider().get())
        }
    }
}

kotlin {
    jvmToolchain(25)
}

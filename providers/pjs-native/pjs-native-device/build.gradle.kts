plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
}

dependencies {
    api(project(":pjs-core"))
    api(project(":pjs-native-context"))
    api(libs.slf4j.api)
    testRuntimeOnly(libs.logback.classic)
    testImplementation(libs.jimfs)
}

tasks.withType<Test> {
    configurations {
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
}

plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    id("buildlogic.java-library")
}

dependencies {
    api(project(":pjs-core"))
    api(project(":pjs-native-context"))
    api(libs.slf4j.api)
    testRuntimeOnly(libs.logback.classic)
}

tasks.withType<Test> {
    configurations {
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
}

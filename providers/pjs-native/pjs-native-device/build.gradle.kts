plugins {
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    id("buildlogic.java-library")
    id("buildlogic.maven-publish")
    `java-test-fixtures`
}

dependencies {
    api(project(":pjs-core"))
    api(project(":providers:pjs-native:pjs-native-context"))
    api(libs.slf4j.api)
    testRuntimeOnly(libs.logback.classic)

    testFixturesImplementation(libs.jimfs)
}

tasks.withType<Test> {
    configurations {
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
}

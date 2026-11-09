package buildlogic.test

import buildlogic.withVersionCatalog

plugins {
    id("buildlogic.java-core")
    jacoco
    `java-test-fixtures`
}

dependencies {
    withVersionCatalog {
        testImplementation(platform(libs.junit.bom))
        testImplementation("org.junit.jupiter:junit-jupiter-engine")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")

        testFixturesImplementation(platform(libs.junit.bom))
        testFixturesImplementation("org.junit.jupiter:junit-jupiter-engine")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}
package buildlogic

plugins {
    java
}

dependencies {
    withVersionCatalog {
        implementation(libs.jspecify)
        implementation(libs.slf4j.api)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

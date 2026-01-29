package buildlogic

plugins {
    id("buildlogic.java-core")
    `java-library`
}

dependencies {
    withVersionCatalog {
        api(libs.jspecify)
    }
}

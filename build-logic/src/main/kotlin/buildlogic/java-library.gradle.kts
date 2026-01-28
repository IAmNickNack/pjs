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
//
//tasks.withType<Javadoc>().configureEach {
////    val sourceSetDirectories = sourceSets
////        .main
////        .get()
////        .java
////        .sourceDirectories
////        .joinToString(":")
////    (options as CoreJavadocOptions).addStringOption("-source-path", sourceSetDirectories)
//
//    doFirst {
//        val modulePath = configurations.runtimeClasspath.get().asPath
//        (options as CoreJavadocOptions).addStringOption("-module-path", modulePath)
//    }
//}
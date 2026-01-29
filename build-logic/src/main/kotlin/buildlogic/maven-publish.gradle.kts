package buildlogic

plugins {
    id("org.jetbrains.dokka-javadoc")
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    coordinates(
        project.group as String,
        project.name,
        project.version as String
    )

    pom {
        name = "PJs Raspberry Pi Abstractions"
        description = "Hardware and network abstraction layers for the Raspberry Pi"
        url = "https://github.com/IAmNickNack/pjs"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "iamnicknack"
                name = "I Am Nicknack"
                email = "62-opinion.leopard@icloud.com"
            }
        }
        scm {
            connection = "scm:git:git@github.com:IAmNickNack/pjs.git"
            developerConnection = "scm:git:git@github.com:IAmNickNack/pjs.git"
            url = "https://github.com/IAmNickNack/pjs"
        }
    }

    signAllPublications()
    publishToMavenCentral()
}

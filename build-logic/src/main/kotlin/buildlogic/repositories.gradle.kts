package buildlogic

repositories {
    mavenLocal()
    mavenCentral()

    exclusiveContent {
        filter {
            includeGroup("com.pi4j")
        }
        forRepository {
            maven {
                url = uri("https://central.sonatype.com/repository/maven-snapshots/")
                name = "SonatypeSnapshots"
            }
        }
    }
}

package buildlogic

repositories {
    mavenLocal()
    mavenCentral()

    exclusiveContent {
        filter {
            includeVersionByRegex("^com\\.pi4j$", "^pi4j-.*$", "^.*-SNAPSHOT$")
            includeVersionByRegex("^io\\.github\\.iamnicknack$", "^pi4j-plugin-grpc$", "^.*-SNAPSHOT$")        }
        forRepository {
            maven {
                url = uri("https://central.sonatype.com/repository/maven-snapshots/")
                name = "SonatypeSnapshots"
            }
        }
    }
}

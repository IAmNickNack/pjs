plugins {
    id("buildlogic.kotlin-core")
    id("buildlogic.grpc.kotlin-krpc")
    id("buildlogic.java-library")
}

dependencies {
    api(project(":pjs-core"))
}
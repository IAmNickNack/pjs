rootProject.name = "pjs"

includeBuild("build-logic")
includeBuild("pjs-sandbox")

include("pjs-core")
include("pjs-utils")
include(":pjs-mock-device")
include(":pjs-native-context")
include(":pjs-native-device")
include(":pjs-grpc-server")
include(":pjs-grpc-device")
include(":pjs-grpc-proto")
include(":pjs-pi4j-device")
include(":pjs-http-common")
include(":pjs-http-server")
include(":pjs-http-device")
include(":network-common")

project(":pjs-mock-device").projectDir = file("providers/pjs-mock-device")

project(":pjs-native-context").projectDir = file("providers/pjs-native/pjs-native-context")
project(":pjs-native-device").projectDir = file("providers/pjs-native/pjs-native-device")

project(":pjs-pi4j-device").projectDir = file("providers/pjs-pi4j-device")

project(":pjs-grpc-server").projectDir = file("providers/network/pjs-grpc/pjs-grpc-server")
project(":pjs-grpc-device").projectDir = file("providers/network/pjs-grpc/pjs-grpc-device")
project(":pjs-grpc-proto").projectDir = file("providers/network/pjs-grpc/pjs-grpc-proto")

project(":pjs-http-common").projectDir = file("providers/network/pjs-http/pjs-http-common")
project(":pjs-http-server").projectDir = file("providers/network/pjs-http/pjs-http-server")
project(":pjs-http-device").projectDir = file("providers/network/pjs-http/pjs-http-device")

project(":network-common").projectDir = file("providers/network/network-common")

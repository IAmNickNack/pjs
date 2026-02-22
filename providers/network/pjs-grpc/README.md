# pjs-grpc

## pjs-grpc-device

A PJs device driver which uses gRPC to communicate with a remote Pi

## pjs-grpc-proto

Protobuf schemas for the gRPC service

## pjs-grpc-server

A server application which can run on the Pi and communicate with hardware

Download the latest release from [Github](https://github.com/iamnicknack/pjs/releases/latest)

```shell
./gradlew :pjs-grpc-server:installDist
./providers/network/pjs-grpc/pjs-grpc-server/build/install/pjs-grpc-server/bin/pjs-grpc-server --help
```

### Dependency coordinates for Maven

```xml
<dependency>
    <groupId>io.github.iamnicknack</groupId>
    <artifactId>pjs-grpc-device</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Dependency coordinates for Gradle

```kotlin
implementation("io.github.iamnicknack:pjs-grpc-device:0.1.0")
```

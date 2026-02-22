# pjs-http

## pjs-http-device

A PJs device driver which uses REST to communicate with a remote Pi

## pjs-http-server

A server application which can run on the Pi and communicate with hardware

```shell
./gradlew :pjs-http-server:installDist 
./providers/network/pjs-http/pjs-http-server/build/install/pjs-http-server/bin/pjs-http-server --help
```

Download the latest release from [Github](https://github.com/iamnicknack/pjs/releases/latest)

### Dependency coordinates for Maven

```xml
<dependency>
    <groupId>io.github.iamnicknack</groupId>
    <artifactId>pjs-http-device</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Dependency coordinates for Gradle

```kotlin
implementation("io.github.iamnicknack:pjs-http-device:0.1.0")
```

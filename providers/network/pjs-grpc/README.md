# pjs-grpc

## pjs-grpc-device

A PJs device driver which uses gRPC to communicate with a remote Pi

## pjs-grpc-proto

Protobuf schemas for the gRPC service

## pjs-grpc-server

A server application which can run on the Pi and communicate with hardware

```shell
./gradlew :pjs-grpc-server:installDist
./providers/network/pjs-grpc/pjs-grpc-server/build/install/pjs-grpc-server/bin/pjs-grpc-server --help
```
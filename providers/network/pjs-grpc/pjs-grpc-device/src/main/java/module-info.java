import io.github.iamnicknack.pjs.grpc.GrpcDeviceRegistryLoader;
import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader;

module pjs.grpc {
    requires pjs.core;
    requires io.grpc;
    requires io.grpc.protobuf;
    requires io.grpc.stub;
    requires org.jspecify;

    exports io.github.iamnicknack.pjs.grpc;

    provides DeviceRegistryLoader with GrpcDeviceRegistryLoader;
}
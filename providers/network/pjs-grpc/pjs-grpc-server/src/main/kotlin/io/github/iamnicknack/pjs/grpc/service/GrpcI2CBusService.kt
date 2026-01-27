package io.github.iamnicknack.pjs.grpc.service

import com.google.protobuf.ByteString
import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.grpc.deviceOrThrow
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusServiceGrpcKt
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.MessageType
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.TransferRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.TransferResponse
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class GrpcI2CBusService(
    private val deviceRegistry: DeviceRegistry,
) : I2CBusServiceGrpcKt.I2CBusServiceCoroutineImplBase() {

    override suspend fun transfer(request: TransferRequest): TransferResponse {
        val device = deviceRegistry.deviceOrThrow<I2C>(request.deviceId)

        val transferMessages = request.messageList
            .map {
                val data = if (it.type == MessageType.READ) {
                    ByteArray(it.data.size())
                } else {
                    it.data.toByteArray()
                }

                val type = if (it.type == MessageType.READ) {
                    I2C.Message.Type.READ
                } else {
                    I2C.Message.Type.WRITE
                }

                I2C.Message(it.address, data, 0, data.size, type)
            }
            .toTypedArray()

        device.transfer(*transferMessages)

        return request.messageList
            .mapIndexed { index, message ->
                if (message.type == MessageType.READ) {
                    message.toBuilder().setData(ByteString.copyFrom(transferMessages[index].data)).build()
                } else {
                    message
                }
            }
            .fold(TransferResponse.newBuilder()) { builder, message ->
                builder.addMessage(message)
            }
            .build()
    }
}

package io.github.iamnicknack.pjs.grpc

import com.google.protobuf.ByteString
import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusConfigServiceGrpc
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusMessage
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusServiceGrpc
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.MessageType
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.TransferRequest
import io.github.iamnicknack.pjs.model.device.DeviceConfig

class GrpcI2C(
    private val config: I2CConfig,
    private val stub : I2CBusServiceGrpc.I2CBusServiceBlockingStub,
    private val configStub: I2CBusConfigServiceGrpc.I2CBusConfigServiceBlockingStub,
) : I2C {

    override fun getConfig(): DeviceConfig<I2C> = this.config

    override fun transfer(messages: Array<out I2C.Message>) {
        val response = stub.transfer(this.config.asTransferRequest(messages))
        response.messageList
            .mapIndexed { index, i2CBusMessage ->
                if (messages[index].type == I2C.Message.Type.READ) {
                    i2CBusMessage.data.copyTo(messages[index].data, 0)
                }
            }
    }

    override fun close() {
        configStub.remove(config.asDeviceRequest())
    }

    private fun DeviceConfig<*>.asTransferRequest(messages: Array<out I2C.Message>): TransferRequest {
        return TransferRequest.newBuilder()
            .setDeviceId(this.id)
            .addAllMessage(
                messages.map {
                    I2CBusMessage.newBuilder()
                        .setAddress(it.address)
                        .setType(it.type.asMessageType())
                        .setData(ByteString.copyFrom(it.data))
                        .build()
                }
            )
            .build()
    }

    private fun I2C.Message.Type.asMessageType(): MessageType {
        return when (this) {
            I2C.Message.Type.READ -> MessageType.READ
            I2C.Message.Type.WRITE -> MessageType.WRITE
        }
    }
}
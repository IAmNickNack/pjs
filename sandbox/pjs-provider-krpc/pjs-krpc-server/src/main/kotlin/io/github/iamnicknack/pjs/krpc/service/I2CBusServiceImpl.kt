package io.github.iamnicknack.pjs.krpc.service

import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.krpc.gen.v1.i2c.bus.I2CBusMessage
import io.github.iamnicknack.pjs.krpc.gen.v1.i2c.bus.I2CBusService
import io.github.iamnicknack.pjs.krpc.gen.v1.i2c.bus.MessageType
import io.github.iamnicknack.pjs.krpc.gen.v1.i2c.bus.TransferRequest
import io.github.iamnicknack.pjs.krpc.gen.v1.i2c.bus.TransferResponse
import io.github.iamnicknack.pjs.krpc.gen.v1.i2c.bus.invoke
import io.github.iamnicknack.pjs.krpc.proto.deviceOrThrow
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import kotlinx.io.bytestring.ByteString

class I2CBusServiceImpl(
    private val deviceRegistry: DeviceRegistry,
) : I2CBusService {

    override suspend fun transfer(message: TransferRequest): TransferResponse {
        val device = deviceRegistry.deviceOrThrow<I2C>(message.deviceId)

        val transferMessages = message.message
            .map {
                val data = if (it.type == MessageType.READ) {
                    ByteArray(it.data.size)
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

        return message.message
            .mapIndexed { index, message ->
                if (message.type == MessageType.READ) {
                    I2CBusMessage.invoke {
                        address = message.address
                        type = if (message.type == MessageType.READ) {
                            MessageType.READ
                        } else {
                            MessageType.WRITE
                        }
                        data = ByteString(transferMessages[index].data)
                    }
                } else {
                    message
                }
            }
            .toList()
            .let { TransferResponse { this.message = it } }
    }
}

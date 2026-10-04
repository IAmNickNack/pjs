package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusConfigService
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusMessage
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusService
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.MessageType
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.TransferRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.invoke
import io.github.iamnicknack.pjs.krpc.proto.asDeviceRequest
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import kotlinx.coroutines.runBlocking
import kotlinx.io.bytestring.ByteString

class KrpcI2C(
    private val config: I2CConfig,
    private val i2cService: I2CBusService,
    private val i2cConfigService: I2CBusConfigService
) : I2C {

    override fun transfer(vararg messages: I2C.Message): Unit = runBlocking {
        val response = i2cService.transfer(config.asTransferRequest(messages.toList()))
        response.message
            .forEachIndexed { index, message ->
                message.data.copyInto(messages[index].data)
            }
    }

    override fun getConfig(): DeviceConfig<I2C> = config

    override fun close(): Unit = runBlocking {
        i2cConfigService.remove(config.asDeviceRequest())
    }

    private fun I2CConfig.asTransferRequest(messages: List<I2C.Message>) = TransferRequest.invoke {
        deviceId = this@asTransferRequest.id
        message = messages.map {
            I2CBusMessage.invoke {
                address = it.address
                data = ByteString(it.data)
                type = if (it.type == I2C.Message.Type.WRITE) MessageType.WRITE else MessageType.READ
            }
        }
    }
}
package io.github.iamnicknack.pjs.krpc.service

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiTransfer
import io.github.iamnicknack.pjs.krpc.gen.v1.spi.SpiTransferMessage
import io.github.iamnicknack.pjs.krpc.gen.v1.spi.SpiTransferMessageList
import io.github.iamnicknack.pjs.krpc.gen.v1.spi.SpiTransferService
import io.github.iamnicknack.pjs.krpc.gen.v1.spi.invoke
import io.github.iamnicknack.pjs.krpc.proto.deviceOrThrow
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import kotlinx.io.bytestring.ByteString

class SpiTransferServiceImpl(
    private val deviceRegistry: DeviceRegistry
) : SpiTransferService {

    override suspend fun transfer(message: SpiTransferMessageList): SpiTransferMessageList {
        val transfer = deviceRegistry.deviceOrThrow<Spi>(message.deviceId).createTransfer()

        val messagesToSend = message.message
            .map {
                SpiTransfer.DefaultMessage(
                    it.payload.toByteArray(),
                    0,
                    ByteArray(it.length),
                    0,
                    it.length,
                    it.delayUs.toInt(),
                    it.csChange
                )
            }
            .toTypedArray()

        transfer.transfer(*messagesToSend)

        val messagesToReturn = messagesToSend
            .map {
                SpiTransferMessage {
                    payload = ByteString(it.read())
                    length = it.length()
                    delayUs = it.delayUs.toUInt()
                    csChange = it.csChange
                }
            }
            .toList()

        return SpiTransferMessageList {
            this.deviceId = message.deviceId
            this.message = messagesToReturn
        }
    }
}
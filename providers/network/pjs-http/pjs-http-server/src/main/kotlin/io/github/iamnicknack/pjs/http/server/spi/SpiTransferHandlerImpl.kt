package io.github.iamnicknack.pjs.http.server.spi

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiTransfer
import io.github.iamnicknack.pjs.http.server.deviceOrThrow
import io.github.iamnicknack.pjs.http.spi.SpiTransferHandler
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class SpiTransferHandlerImpl(
    private val deviceRegistry: DeviceRegistry,
) : SpiTransferHandler {

    override suspend fun transfer(
        deviceId: String,
        messageList: SpiTransferHandler.TransferMessageList
    ): SpiTransferHandler.TransferMessageList {
        val transfer = deviceRegistry.deviceOrThrow<Spi>(deviceId).createTransfer()

        val messagesToSend = messageList.messages
            .map {
                SpiTransfer.DefaultMessage(
                    it.payload,
                    0,
                    ByteArray(it.payload.size),
                    0,
                    it.payload.size,
                    it.delayUs,
                    it.csChange
                )
            }
            .toTypedArray()

        transfer.transfer(*messagesToSend)

        val messagesToReturn = messagesToSend
            .map {
                SpiTransferHandler.TransferMessage(
                    it.read,
                    it.delayUs,
                    it.csChange
                )
            }

        return SpiTransferHandler.TransferMessageList(messagesToReturn)
    }
}
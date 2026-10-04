package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.device.spi.SpiTransfer
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiTransferMessage
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiTransferMessageList
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiTransferService
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.invoke
import kotlinx.coroutines.runBlocking
import kotlinx.io.bytestring.ByteString

class KrpcSpiTransfer(
    private val config: SpiConfig,
    private val spiTransferService: SpiTransferService
) : SpiTransfer {

    override fun transfer(vararg messages: SpiTransfer.Message): Int {
        val outgoingMessages = messages
            .map {
                SpiTransferMessage.invoke {
                    payload = ByteString(it.write())
                    length = it.length()
                    delayUs = it.delayUs().toUInt()
                    csChange = it.csChange()
                }
            }
            .let { SpiTransferMessageList.invoke { this.message = it } }

        val result = runBlocking { spiTransferService.transfer(outgoingMessages) }

        result.message
            .forEachIndexed { index, message ->
                message.payload.copyInto(
                    messages[index].read(),
                    messages[index].readOffset(),
                    message.length
                )
            }

        return result.message.fold(0) { acc, message -> acc + message.length }
    }
}
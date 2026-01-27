package io.github.iamnicknack.pjs.http.client.spi

import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.device.spi.SpiTransfer
import io.github.iamnicknack.pjs.http.spi.SpiTransferHandler
import kotlinx.coroutines.runBlocking

class HttpSpiTransfer(
    private val spiTransferHandler: SpiTransferHandler,
    private val config: SpiConfig
) : SpiTransfer {

    override fun transfer(vararg messages: SpiTransfer.Message): Int {
        val messagesToSend = messages
            .map {
                SpiTransferHandler.TransferMessage(
                    it.write(),
                    it.delayUs(),
                    it.csChange()
                )
            }

        val messagesToReturn = runBlocking {
            spiTransferHandler.transfer(
                config.id,
                SpiTransferHandler.TransferMessageList(messagesToSend)
            )
        }

        messagesToReturn.messages
            .forEachIndexed { index, message ->
                System.arraycopy(message.payload, 0, messages[index].read(), 0, message.payload.size)
            }

        return messages.sumOf { it.length() }
    }
}
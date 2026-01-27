package io.github.iamnicknack.pjs.http.spi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

interface SpiTransferHandler {

    suspend fun transfer(deviceId: String, messageList: TransferMessageList): TransferMessageList

    @JsonIgnoreProperties(ignoreUnknown = true)
    class TransferMessage(
        val payload: ByteArray,
        val delayUs: Int = 0,
        val csChange: Boolean = false
    )

    class TransferMessageList(
        val messages: List<TransferMessage>
    )
}
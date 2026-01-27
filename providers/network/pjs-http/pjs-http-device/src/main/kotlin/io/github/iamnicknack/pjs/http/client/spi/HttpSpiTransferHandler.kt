package io.github.iamnicknack.pjs.http.client.spi

import io.github.iamnicknack.pjs.http.spi.SpiTransferHandler
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class HttpSpiTransferHandler(
    private val httpClient: HttpClient,
) : SpiTransferHandler {

    override suspend fun transfer(
        deviceId: String,
        messageList: SpiTransferHandler.TransferMessageList
    ): SpiTransferHandler.TransferMessageList {
        return httpClient
            .post("/api/v1/spi/$deviceId/spi-transfer") {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(messageList)
            }
            .body<SpiTransferHandler.TransferMessageList>()
    }
}
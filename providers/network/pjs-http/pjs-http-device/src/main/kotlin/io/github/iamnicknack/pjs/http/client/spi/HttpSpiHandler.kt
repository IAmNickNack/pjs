package io.github.iamnicknack.pjs.http.client.spi

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.http.client.config.HttpConfigHandler
import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.http.spi.SpiHandler
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class HttpSpiHandler(
    private val httpClient: HttpClient,
    private val configHandler: ConfigHandler<Spi> = HttpConfigHandler(
        httpClient,
        "spi",
        { setBody(it) },
        { body<SpiHandler.SpiConfigPayload>() }
    )
) : SpiHandler, ConfigHandler<Spi> by configHandler {

    override suspend fun transfer(
        deviceId: String,
        payload: SpiHandler.TransferPayload
    ): SpiHandler.TransferPayload {
        return httpClient
            .post("/api/v1/spi/$deviceId/transfer") {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            .body<SpiHandler.TransferPayload>()
    }
}
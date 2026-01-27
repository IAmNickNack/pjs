package io.github.iamnicknack.pjs.http.client.i2c

import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.http.client.config.HttpConfigHandler
import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.http.i2c.I2CHandler
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class HttpI2CHandler(
    private val httpClient: HttpClient,
    private val configHandler: ConfigHandler<I2C> = HttpConfigHandler(
        httpClient,
        "i2c",
        { setBody(it) },
        { body<I2CHandler.I2CConfigPayload>() }
    )
) : I2CHandler, ConfigHandler<I2C> by configHandler {

    override suspend fun transfer(
        deviceId: String,
        payload: I2CHandler.I2CTransferPayload
    ): I2CHandler.I2CTransferPayload {
        return httpClient
            .post("/api/v1/i2c/$deviceId/transfer") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(payload)
            }
            .body<I2CHandler.I2CTransferPayload>()
    }
}
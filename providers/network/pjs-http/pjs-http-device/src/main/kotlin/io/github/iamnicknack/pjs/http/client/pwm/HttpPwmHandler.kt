package io.github.iamnicknack.pjs.http.client.pwm

import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.http.client.config.HttpConfigHandler
import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.http.pwm.PwmHandler
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class HttpPwmHandler(
    private val httpClient: HttpClient,
    private val configHandler: ConfigHandler<Pwm> = HttpConfigHandler(
        httpClient,
        "pwm",
        { setBody(it) },
        { body<PwmHandler.PwmConfigPayload>() }
    )
) : PwmHandler, ConfigHandler<Pwm> by configHandler  {

    override suspend fun on(deviceId: String) {
        httpClient.put("/api/v1/pwm/$deviceId/on") {}
    }

    override suspend fun off(deviceId: String) {
        httpClient.put("/api/v1/pwm/$deviceId/off") {}
    }

    override suspend fun setDutyCycle(deviceId: String, dutyCycle: Long) {
        httpClient.put("/api/v1/pwm/$deviceId/duty-cycle/$dutyCycle")
    }

    override suspend fun getDutyCycle(deviceId: String): Long =
        httpClient
            .get("/api/v1/pwm/$deviceId/duty-cycle")
            .body<Long>()

    override suspend fun setPolarity(deviceId: String, polarity: Pwm.Polarity) {
        httpClient.put("/api/v1/pwm/$deviceId/polarity/${polarity.name.lowercase()}")
    }

    override suspend fun getPolarity(deviceId: String): Pwm.Polarity =
        httpClient
            .get("/api/v1/pwm/$deviceId/polarity")
            .body<String>()
            .let { Pwm.Polarity.valueOf(it.uppercase()) }

//    override suspend fun setFrequency(deviceId: String, frequency: Int) {
//        httpClient.put("/api/v1/pwm/$deviceId/frequency/$frequency")
//    }
//
//    override suspend fun getFrequency(deviceId: String): Int =
//        httpClient
//            .get("/api/v1/pwm/$deviceId/frequency")
//            .body<Int>()
//
    override suspend fun setPeriod(deviceId: String, period: Long) {
        httpClient.put("/api/v1/pwm/$deviceId/period/$period")
    }

    override suspend fun getPeriod(deviceId: String): Long =
        httpClient
            .get("/api/v1/pwm/$deviceId/period")
            .body<Long>()

}
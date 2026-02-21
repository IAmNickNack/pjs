package io.github.iamnicknack.pjs.http.server.pwm

import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.http.pwm.PwmHandler
import io.github.iamnicknack.pjs.http.server.config.configRoutes
import io.github.iamnicknack.pjs.http.server.deviceId
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.pwmRoutes(handler: PwmHandler) {
    route("/api/v1/pwm/{deviceId}") {
        /**
         * Turn the PWM pin on
         */
        put("/on") {
            handler.on(call.deviceId)
            call.respond(HttpStatusCode.OK)
        }

        /**
         * Turn the PWM pin off
         */
        put("/off") {
            handler.off(call.deviceId)
            call.respond(HttpStatusCode.OK)
        }

        /**
         * Set the frequency of the PWM pin
         */
        put("/period/{period}") {
            handler.setPeriod(call.deviceId, call.parameters["period"]!!.toLong())
            call.respond(HttpStatusCode.OK)
        }

        /**
         * Read the current frequency of the PWM pin
         */
        get("/period") {
            call.respond(handler.getPeriod(call.deviceId))
        }

        /**
         * Set the duty cycle of the PWM pin
         */
        put("/duty-cycle/{dutyCycle}") {
            handler.setDutyCycle(call.deviceId, call.parameters["dutyCycle"]!!.toLong())
            call.respond(HttpStatusCode.OK)
        }

        /**
         * Read the current duty cycle of the PWM pin
         */
        get("/duty-cycle") {
            call.respond(handler.getDutyCycle(call.deviceId))
        }

        /**
         * Set the polarity of the PWM pin
         */
        put("/polarity/{polarity}") {
            handler.setPolarity(call.deviceId, Pwm.Polarity.valueOf(call.parameters["polarity"]!!))
            call.respond(HttpStatusCode.OK)
        }

        /**
         * Read the current polarity of the PWM pin
         */
        get("/polarity") {
            call.respond(handler.getPolarity(call.deviceId).toString())
        }

        configRoutes(
            handler,
            { call.receive(PwmHandler.PwmConfigPayload::class) },
            { call.respond(it) }
        )
    }
}
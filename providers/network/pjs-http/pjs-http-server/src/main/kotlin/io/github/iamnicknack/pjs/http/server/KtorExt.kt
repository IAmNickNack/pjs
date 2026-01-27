package io.github.iamnicknack.pjs.http.server

import io.ktor.server.routing.*

val RoutingCall.deviceId: String
    get() = parameters["deviceId"] ?: throw IllegalArgumentException("Invalid deviceId")
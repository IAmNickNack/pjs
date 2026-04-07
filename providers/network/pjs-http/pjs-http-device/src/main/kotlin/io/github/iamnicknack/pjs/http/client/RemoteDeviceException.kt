package io.github.iamnicknack.pjs.http.client

import io.ktor.client.statement.*
import io.ktor.http.*

sealed class RemoteDeviceException(
    message: String,
    val status: HttpStatusCode,
    val deviceId: String,
) : RuntimeException(message) {

    class RemoteDeviceNotFoundException(
        message: String,
        status: HttpStatusCode,
        deviceId: String,
    ) : RemoteDeviceException(message, status, deviceId)

    class RemoteDeviceConflictException(
        message: String,
        status: HttpStatusCode,
        deviceId: String,
    ) : RemoteDeviceException(message, status, deviceId)

    class RemoteDeviceBadRequestException(
        message: String,
        status: HttpStatusCode,
        deviceId: String,
    ) : RemoteDeviceException(message, status, deviceId)

    class GeneralRemoteDeviceException(
        message: String,
        status: HttpStatusCode,
        deviceId: String,
    ) : RemoteDeviceException(message, status, deviceId)
}

suspend fun HttpResponse.ensureSuccess(deviceId: String): HttpResponse {

    if (status.value in 200..299) return this

    val message = bodyAsText().ifBlank {
        "HTTP ${status.value} for $deviceId"
    }

    throw when (status) {
        HttpStatusCode.NotFound -> RemoteDeviceException.RemoteDeviceNotFoundException(
            message = message,
            status = status,
            deviceId = deviceId,
        )
        HttpStatusCode.Conflict -> RemoteDeviceException.RemoteDeviceConflictException(
            message = message,
            status = status,
            deviceId = deviceId,
        )
        HttpStatusCode.BadRequest -> RemoteDeviceException.RemoteDeviceBadRequestException(
            message = message,
            status = status,
            deviceId = deviceId,
        )
        else -> RemoteDeviceException.GeneralRemoteDeviceException(
            message = message,
            status = status,
            deviceId = deviceId,
        )
    }
}
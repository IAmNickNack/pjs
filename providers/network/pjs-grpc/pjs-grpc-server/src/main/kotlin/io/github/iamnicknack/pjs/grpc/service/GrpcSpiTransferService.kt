package io.github.iamnicknack.pjs.grpc.service

import com.google.protobuf.ByteString
import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.device.spi.SpiProvider
import io.github.iamnicknack.pjs.device.spi.SpiTransfer
import io.github.iamnicknack.pjs.grpc.deviceOrThrow
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiTransferMessage
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiTransferMessageList
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiTransferServiceGrpcKt
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class GrpcSpiTransferService(
    private val deviceRegistry: DeviceRegistry
) : SpiTransferServiceGrpcKt.SpiTransferServiceCoroutineImplBase() {

    private val provider: SpiProvider = deviceRegistry.getProvider(SpiConfig::class.java) as SpiProvider

    override suspend fun transfer(request: SpiTransferMessageList): SpiTransferMessageList {
        val transfer = provider.createTransfer(deviceRegistry.deviceOrThrow<Spi>(request.deviceId))

        val messagesToSend = request.messageList
            .map {
                SpiTransfer.DefaultMessage(
                    it.payload.toByteArray(),
                    0,
                    ByteArray(it.length),
                    0,
                    it.length,
                    it.delayUs,
                    it.csChange
                )
            }
            .toTypedArray()

        transfer.transfer(*messagesToSend)

        val messagesToReturn = messagesToSend
            .map {
                SpiTransferMessage.newBuilder()
                    .setPayload(ByteString.copyFrom(it.read()))
                    .setLength(it.length())
                    .setDelayUs(it.delayUs())
                    .setCsChange(it.csChange())
                    .build()
            }

        return SpiTransferMessageList.newBuilder()
            .setDeviceId(request.deviceId)
            .addAllMessage(messagesToReturn)
            .build()
    }
}
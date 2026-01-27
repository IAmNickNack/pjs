package io.github.iamnicknack.pjs.grpc

import com.google.protobuf.ByteString
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.device.spi.SpiTransfer
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiTransferMessage
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiTransferMessageList
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiTransferServiceGrpc

class GrpcSpiTransfer(
    private val config: SpiConfig,
    private val stub: SpiTransferServiceGrpc.SpiTransferServiceBlockingStub
) : SpiTransfer {

    override fun transfer(vararg messages: SpiTransfer.Message): Int {
        val grpcMessages = messages
            .map {
                SpiTransferMessage.newBuilder()
                    .setPayload(ByteString.copyFrom(it.write()))
                    .setLength(it.length())
                    .setDelayUs(it.delayUs())
                    .setCsChange(it.csChange())
                    .build()
            }

        val result = stub.transfer(
            SpiTransferMessageList.newBuilder()
                .setDeviceId(config.id)
                .addAllMessage(grpcMessages)
                .build()
        )

        result.messageList
            .forEachIndexed { index, message ->
                System.arraycopy(
                    message.payload.toByteArray(),
                    0,
                    messages[index].read(),
                    messages[index].readOffset(),
                    message.length
                )
            }

        return result.messageList.fold(0) { acc, message -> acc + message.length }
    }
}
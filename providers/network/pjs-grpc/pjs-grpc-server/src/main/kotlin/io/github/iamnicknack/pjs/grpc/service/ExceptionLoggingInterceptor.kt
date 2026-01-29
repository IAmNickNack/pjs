package io.github.iamnicknack.pjs.grpc.service

import io.grpc.ForwardingServerCall
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import org.slf4j.LoggerFactory


class ExceptionLoggingInterceptor : ServerInterceptor {

    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val logger = LoggerFactory.getLogger(call.methodDescriptor.fullMethodName)

        val safeCall = object : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            override fun sendMessage(message: RespT) {
                try {
                    super.sendMessage(message)
                } catch (t: Throwable) {
                    logger.error("Unhandled exception making gRPC call", t)
                    val cause = t.cause?.cause ?: t.cause ?: t
                    call.close(
                        Status.INTERNAL.withDescription(cause.message).withCause(cause),
                        Metadata()
                    )
                }
            }

            override fun close(status: Status, trailers: Metadata) {
                return when {
                    status.isOk -> super.close(status, trailers)
                    else -> {
                        val cause = status.cause?.cause ?: status.cause
                        logger.error("Unhandled exception closing gRPC call", status.cause)
                        call.close(
                            status.withDescription(cause?.message).withCause(cause),
                            trailers
                        )
                    }
                }
            }
        }

        return next.startCall(safeCall, headers)
    }
}

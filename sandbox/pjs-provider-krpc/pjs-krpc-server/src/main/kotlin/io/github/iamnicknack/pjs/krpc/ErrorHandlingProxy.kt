package io.github.iamnicknack.pjs.krpc

import io.grpc.Status
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object ErrorHandlingProxy {

    private val logger: Logger = LoggerFactory.getLogger(ErrorHandlingProxy::class.java)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> T.withErrorHandling(): T {
        return around(this)
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T : Any> around(delegate: T): T {
        return Proxy.newProxyInstance(
            Thread.currentThread().contextClassLoader,
            delegate.javaClass.interfaces,
            ErrorHandlingInvocationHandler(delegate)
        ) as T
    }

    private class ErrorHandlingInvocationHandler(
        private val delegate: Any
    ) : InvocationHandler {
        override fun invoke(
            proxy: Any,
            method: Method,
            args: Array<out Any>
        ): Any {
            return runCatching { method.invoke(delegate, *args) }
                .fold(
                    { it },
                    {
                        val ex = when (it) {
                            is InvocationTargetException -> it.cause ?: it
                            else -> it
                        }
                        logger.error("Error occurred while invoking method ${method.name}", ex)
                        throw Status.INTERNAL
                            .withDescription(ex.message)
                            .withCause(it)
                            .asRuntimeException()
                    }
                )
        }
    }
}

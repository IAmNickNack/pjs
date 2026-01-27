package io.github.iamnicknack.pjs.http.event

import io.github.iamnicknack.pjs.model.event.GpioChangeEvent
import io.github.iamnicknack.pjs.model.event.GpioEventEmitter
import io.github.iamnicknack.pjs.model.event.GpioEventListener
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.lang.AutoCloseable

/**
 * [kotlinx.coroutines.flow.Flow] adapter for [io.github.iamnicknack.pjs.model.event.GpioEventEmitter]/[io.github.iamnicknack.pjs.model.event.GpioEventListener]
 */
interface EventBroadcaster<T : GpioEventEmitter<T>> : GpioEventListener<T>, AutoCloseable {
    /**
     * [kotlinx.coroutines.flow.Flow] of [io.github.iamnicknack.pjs.model.event.GpioChangeEvent]s
     */
    val events: Flow<GpioChangeEvent<T>>

    /**
     * Emit [event] via [events]
     */
    suspend fun broadcast(event: GpioChangeEvent<T>)

    override fun close() {
        // do nothing
    }

    /**
     * Default implementation of [EventBroadcaster]
     */
    class ServerChannel<T : GpioEventEmitter<T>> : EventBroadcaster<T> {
        private val _events = Channel<GpioChangeEvent<T>>(
            capacity = 100,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        override val events: Flow<GpioChangeEvent<T>> = _events.receiveAsFlow()

        override suspend fun broadcast(event: GpioChangeEvent<T>) {
            _events.send(event)
        }

        override fun onEvent(event: GpioChangeEvent<T>) {
            _events.trySend(event)
        }

        override fun close() {
            _events.close()
        }
    }
}
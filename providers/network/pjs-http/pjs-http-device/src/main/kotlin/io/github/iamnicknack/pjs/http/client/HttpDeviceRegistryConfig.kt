package io.github.iamnicknack.pjs.http.client

/**
 * Configuration required by an [HttpDeviceRegistry]
 * @param proxyHost the remote host
 * @param proxyPort the remote port
 * @param mode whether to manage the device lifecycle with [Mode.DEFAULT] or delegate to another instance
 *  with [Mode.PROXY]
 */
data class HttpDeviceRegistryConfig(
    val proxyHost: String,
    val proxyPort: Int,
    val mode: Mode,
) {
    /**
     * The mode to run the registry with
     */
    enum class Mode {
        /**
         * Delegate the device lifecycle to another registry, only providing access to those devices
         */
        PROXY,

        /**
         * Manage the whole device lifecycle locally in this application instance
         */
        DEFAULT
    }
}

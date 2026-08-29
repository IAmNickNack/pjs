package io.github.iamnicknack.pjs.sandbox.registry.line

import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

class PinctrlLineSupplier : LineSupplier {

    init {
        require(isSupported()) {
            "pinctrl is not installed"
        }
    }

    override fun lines(): Set<HardwareAllocationIndex.Line> {
        val str = runCatching { readBashOutput("pinctrl | head -n 27") }

        val lines = str.getOrNull()
            ?.let { PinctrlParser.readLines(it.reader()) }
            ?: throw str.exceptionOrNull() ?: RuntimeException("Cannot read pinctrl")

        return lines
    }

    companion object {
        /**
         * Check if pinctrl is installed
         */
        @JvmStatic
        fun isSupported(): Boolean {
            val path = runCatching { readBashOutput("which pinctrl") }

            return path
                .map { Files.exists(Paths.get(it)) }
                .fold(
                    onSuccess = { it },
                    onFailure = { false }
                )
        }

        /**
         * Create a [LineSupplier] from `pinctrl` output stored in the specified file
         * @return a [LineSupplier] from the contents of the file at [path]
         */
        @JvmStatic
        fun from(path: String): LineSupplier {
            val stream = if (Files.exists(Paths.get(path))) {
                Files.newInputStream(Paths.get(path))
            } else {
                javaClass.getResourceAsStream(path)
            }

            val lines = stream?.bufferedReader()
                ?.let { PinctrlParser.readLines(it) }
                ?: throw FileNotFoundException(path)

            return { lines }
        }

        /**
         * Read the standard output of a bash command
         * @param command the bash command to execute
         * @return the standard output of the command
         * @throws IOException if the command fails
         */
        private fun readBashOutput(command: String): String {
            val proc = ProcessBuilder("bash", "-c", command)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(5, TimeUnit.SECONDS)

            if (proc.exitValue() != 0) {
                throw IOException("bash command failed: $command")
            }

            return proc.inputStream.bufferedReader().readText().trim()
        }
    }
}

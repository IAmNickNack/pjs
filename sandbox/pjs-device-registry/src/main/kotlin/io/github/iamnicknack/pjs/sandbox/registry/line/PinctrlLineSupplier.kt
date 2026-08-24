package io.github.iamnicknack.pjs.sandbox.registry.line

import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex
import java.io.FileNotFoundException
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
        val str = runCatching {
            val proc = ProcessBuilder("bash", "-c", "pinctrl | head -n 27")
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(5, TimeUnit.SECONDS)
            proc.inputStream.bufferedReader().readText()
        }

        val lines = str.getOrNull()
            ?.let { PinctrlParser.readLines(it.reader()) }
            ?: throw str.exceptionOrNull() ?: RuntimeException("Cannot read pinctrl")

        return lines
    }

    companion object {

        @JvmStatic
        fun isSupported(): Boolean {
            val path = runCatching {
                val proc = ProcessBuilder("bash", "-c", "which pinctrl")
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start()

                proc.waitFor(5, TimeUnit.SECONDS)

                if (proc.exitValue() != 0) {
                    throw FileNotFoundException("pinctrl not found")
                }

                proc.inputStream.bufferedReader().readText().trim()
            }

            return path
                .map { Files.exists(Paths.get(it)) }
                .fold(
                    onSuccess = { it },
                    onFailure = { false }
                )
        }

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
    }
}

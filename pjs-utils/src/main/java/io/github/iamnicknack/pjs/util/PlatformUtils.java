package io.github.iamnicknack.pjs.util;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class PlatformUtils {

    private PlatformUtils() {}

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().startsWith("linux");
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public static boolean isRaspberryPi() {
        if (!isLinux()) {
            return false;
        }

        var cpuInfo = new File("/proc/cpuinfo");
        if (!cpuInfo.exists()) {
            return false;
        }

        try (var reader = new BufferedReader(Files.newBufferedReader(cpuInfo.toPath(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                var lower = line.toLowerCase();
                if (lower.startsWith("model") && lower.contains("raspberry pi")) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }
}

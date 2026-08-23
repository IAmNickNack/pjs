package io.github.iamnicknack.pjs.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

public class PlatformUtils {

    private PlatformUtils() {}

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().startsWith("linux");
    }

    public static boolean isRaspberryPi() {
        if (!isLinux()) {
            return false;
        }

        var cpuInfo = new File("/proc/cpuinfo");
        if (!cpuInfo.exists()) {
            return false;
        }

        try (var reader = new BufferedReader(new FileReader(cpuInfo, StandardCharsets.UTF_8))) {
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

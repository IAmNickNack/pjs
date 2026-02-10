package io.github.iamnicknack.pjs.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

public class LoggingUtils {

    private static final String LOGBACK_PROPERTY_PREFIX = "logback.";

    public static void setLogbackLogLevel(String loggerName, String level) {
        var logger = LoggerFactory.getLogger(loggerName);
        if (logger instanceof Logger logbackLogger) {
            logbackLogger.setLevel(Level.toLevel(level));
        }
    }

    public static void setLogbackLevelsFromProperties(Properties properties) {
        properties.entrySet().stream()
                .filter(e -> e.getKey().toString().startsWith(LOGBACK_PROPERTY_PREFIX))
                .map(e -> Map.entry(e.getKey().toString().substring(LOGBACK_PROPERTY_PREFIX.length()), e.getValue().toString()))
                .forEach(e -> setLogbackLogLevel(e.getKey(), e.getValue()));
    }
}

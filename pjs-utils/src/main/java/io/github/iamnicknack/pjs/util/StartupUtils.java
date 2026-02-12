package io.github.iamnicknack.pjs.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class StartupUtils {

    private static final Logger logger = LoggerFactory.getLogger(StartupUtils.class);
    private static final Path CONFIG_PROPERTIES_PATH = Path.of("pjs.properties");

    public static void loadApplicationProperties() {
        if (Files.exists(CONFIG_PROPERTIES_PATH)) {
            try (var reader = Files.newBufferedReader(CONFIG_PROPERTIES_PATH.toAbsolutePath())) {
                 var properties = new Properties();
                 properties.load(reader);
                 properties.forEach((k, v) -> System.setProperty((String) k, (String) v));
            } catch (Exception e) {
                logger.error("Failed to load application properties", e);
            }
        }
    }

}

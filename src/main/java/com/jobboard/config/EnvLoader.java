package com.jobboard.config;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.Map;

public class EnvLoader {
    public static void load() {
        File file = new File(".env");
        if (!file.exists()) {
            System.out.println(".env not found, skipping");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                int idx = line.indexOf('=');
                if (idx < 0)
                    continue;

                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();

                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                // Set both system property and environment variable
                System.setProperty(key, value);
                setEnv(key, value);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load .env file", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void setEnv(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        } catch (Exception e) {
            // Fallback: just use system properties
            System.out.println("Could not set environment variable, using system property only");
        }
    }
}

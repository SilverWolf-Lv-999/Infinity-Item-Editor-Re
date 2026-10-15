package io.github.seraphina.infinity_item_editor_re.config;

import io.github.seraphina.infinity_item_editor_re.ModSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class ClientConfig {
    private static final String CONFIG_FILE_NAME = ModSource.MODID + "-client.properties";
    private static final List<Integer> customColors = new ArrayList<>();
    private static Path configPath;

    private ClientConfig() {
    }

    public static synchronized void load(File minecraftDirectory) {
        configPath = minecraftDirectory.toPath().resolve("config").resolve(CONFIG_FILE_NAME);
        customColors.clear();
        if (!Files.isRegularFile(configPath)) {
            save();
            return;
        }

        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException exception) {
            ModSource.LOGGER.error("Failed to load client config {}", configPath, exception);
            return;
        }

        String value = properties.getProperty("customColors", "");
        for (String entry : value.split(",")) {
            if (entry.isBlank()) {
                continue;
            }
            try {
                int color = Integer.parseInt(entry.trim(), 16) & 0xFFFFFF;
                if (!customColors.contains(color)) {
                    customColors.add(color);
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public static synchronized List<Integer> getCustomColors() {
        return new ArrayList<>(customColors);
    }

    public static synchronized boolean addCustomColor(int color) {
        color &= 0xFFFFFF;
        if (customColors.contains(color)) {
            return false;
        }
        customColors.add(color);
        save();
        return true;
    }

    public static synchronized boolean removeCustomColor(int color) {
        boolean removed = customColors.remove(Integer.valueOf(color & 0xFFFFFF));
        if (removed) {
            save();
        }
        return removed;
    }

    private static synchronized void save() {
        if (configPath == null) {
            return;
        }

        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException exception) {
            ModSource.LOGGER.error("Failed to create client config directory {}", configPath.getParent(), exception);
            return;
        }

        Properties properties = new Properties();
        StringBuilder colors = new StringBuilder();
        for (int i = 0; i < customColors.size(); i++) {
            if (i > 0) {
                colors.append(',');
            }
            colors.append(String.format("%06X", customColors.get(i)));
        }
        properties.setProperty("customColors", colors.toString());

        Path temporaryPath = configPath.resolveSibling(configPath.getFileName() + ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(temporaryPath, StandardCharsets.UTF_8)) {
            properties.store(writer, null);
            Files.move(temporaryPath, configPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveException) {
            try {
                Files.move(temporaryPath, configPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                ModSource.LOGGER.error("Failed to save client config {}", configPath, exception);
            }
        }
    }
}

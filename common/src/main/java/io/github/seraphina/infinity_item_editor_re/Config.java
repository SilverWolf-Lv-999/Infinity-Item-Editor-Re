package io.github.seraphina.infinity_item_editor_re;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public final class Config {
    private static final String CONFIG_FILE_NAME = ModSource.MODID + ".properties";
    private static final List<BooleanEntry> BOOLEAN_ENTRIES_BUILDER = new ArrayList<>();

    public static final BooleanEntry VOID_TAB = defineBoolean("voidTab", true, "void_tab");
    public static final BooleanEntry VOID_ADD_NOTIFICATION = defineBoolean("voidAddNotification", false, "void_add_notification");
    public static final BooleanEntry VOID_TAB_HIDE_HEADS = defineBoolean("voidTabHideHeads", false, "void_tab_hide_heads");
    public static final BooleanEntry UNAVAILABLE_TAB = defineBoolean("unavailableTab", true, "unavailable_tab");
    public static final BooleanEntry BANNER_TAB = defineBoolean("bannerTab", true, "banner_tab");
    public static final BooleanEntry HEAD_TAB = defineBoolean("headTab", true, "head_tab");
    public static final BooleanEntry THIEF_TAB = defineBoolean("thiefTab", true, "thief_tab");
    public static final BooleanEntry FIREWORK_TAB = defineBoolean("fireworkTab", true, "firework_tab");

    private static final List<BooleanEntry> BOOLEAN_ENTRIES = Collections.unmodifiableList(BOOLEAN_ENTRIES_BUILDER);
    private static Path configPath;

    public static ItemEditorUiMode itemGuiMode = ItemEditorUiMode.LEGACY;
    public static boolean voidTab = true;
    public static boolean voidAddNotification = false;
    public static boolean voidTabHideHeads = false;
    public static boolean unavailableTab = true;
    public static boolean bannerTab = true;
    public static boolean headTab = true;
    public static boolean thiefTab = true;
    public static boolean fireworkTab = true;

    public static final int MAIN_COLOR = colorFromRgba(255, 150, 0, 200);
    public static final int ALT_COLOR = colorFromRgba(255, 50, 20, 75);
    public static final int CONTRAST_COLOR = colorFromRgba(255, 0, 100, 255);

    private Config() {
    }

    public enum ItemEditorUiMode {
        LEGACY,
        SIDEBAR
    }

    public static synchronized void load(File minecraftDirectory) {
        Path configDirectory = minecraftDirectory.toPath().resolve("config");
        configPath = configDirectory.resolve(CONFIG_FILE_NAME);

        try {
            Files.createDirectories(configDirectory);
        } catch (IOException exception) {
            ModSource.LOGGER.error("Failed to create config directory {}", configDirectory, exception);
            return;
        }

        Properties properties = new Properties();
        if (Files.isRegularFile(configPath)) {
            try (BufferedReader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException exception) {
                ModSource.LOGGER.error("Failed to load config {}", configPath, exception);
            }
        }

        itemGuiMode = parseItemGuiMode(properties.getProperty("itemGuiMode"));
        for (BooleanEntry entry : BOOLEAN_ENTRIES) {
            entry.load(properties.getProperty(entry.path()));
        }
        syncPublicFields();
        save();
    }

    public static List<BooleanEntry> booleanEntries() {
        return BOOLEAN_ENTRIES;
    }

    public static synchronized void save() {
        if (configPath == null) {
            return;
        }

        Properties properties = new Properties();
        properties.setProperty("itemGuiMode", itemGuiMode.name());
        for (BooleanEntry entry : BOOLEAN_ENTRIES) {
            properties.setProperty(entry.path(), Boolean.toString(entry.get()));
        }

        Path temporaryPath = configPath.resolveSibling(configPath.getFileName() + ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(temporaryPath, StandardCharsets.UTF_8)) {
            properties.store(writer, "Infinity Item Editor Re client configuration");
        } catch (IOException exception) {
            ModSource.LOGGER.error("Failed to write config {}", temporaryPath, exception);
            return;
        }

        try {
            Files.move(temporaryPath, configPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveException) {
            try {
                Files.move(temporaryPath, configPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                ModSource.LOGGER.error("Failed to save config {}", configPath, exception);
            }
        }
    }

    public static boolean getItemSidebar() {
        return itemGuiMode == ItemEditorUiMode.SIDEBAR;
    }

    public static ItemEditorUiMode getItemGuiMode() {
        return itemGuiMode;
    }

    public static ItemEditorUiMode toggleItemGuiMode() {
        ItemEditorUiMode mode = itemGuiMode == ItemEditorUiMode.LEGACY ? ItemEditorUiMode.SIDEBAR : ItemEditorUiMode.LEGACY;
        setItemGuiMode(mode);
        return mode;
    }

    public static void setItemGuiMode(ItemEditorUiMode mode) {
        itemGuiMode = mode == null ? ItemEditorUiMode.LEGACY : mode;
        save();
    }

    public static boolean getIsVoidEnabled() {
        return voidTab;
    }

    public static boolean getIsUnavailableTabEnabled() {
        return unavailableTab;
    }

    public static boolean getIsBannerTabEnabled() {
        return bannerTab;
    }

    public static boolean getIsHeadTabEnabled() {
        return headTab;
    }

    public static boolean getIsThiefTabEnabled() {
        return thiefTab;
    }

    public static boolean getIsFireworkTabEnabled() {
        return fireworkTab;
    }

    public static void syncPublicFields() {
        voidTab = VOID_TAB.get();
        voidAddNotification = VOID_ADD_NOTIFICATION.get();
        voidTabHideHeads = VOID_TAB_HIDE_HEADS.get();
        unavailableTab = UNAVAILABLE_TAB.get();
        bannerTab = BANNER_TAB.get();
        headTab = HEAD_TAB.get();
        thiefTab = THIEF_TAB.get();
        fireworkTab = FIREWORK_TAB.get();
    }

    private static BooleanEntry defineBoolean(String path, boolean defaultValue, String keySuffix) {
        BooleanEntry entry = new BooleanEntry(
                path,
                defaultValue,
                translationKey(keySuffix, "title"),
                translationKey(keySuffix, "description")
        );
        BOOLEAN_ENTRIES_BUILDER.add(entry);
        return entry;
    }

    private static ItemEditorUiMode parseItemGuiMode(String value) {
        if (value == null || value.isBlank()) {
            return ItemEditorUiMode.LEGACY;
        }

        try {
            return ItemEditorUiMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            ModSource.LOGGER.warn("Ignoring invalid itemGuiMode value '{}'", value);
            return ItemEditorUiMode.LEGACY;
        }
    }

    private static int colorFromRgba(int alpha, int red, int green, int blue) {
        return ((alpha & 255) << 24) | ((red & 255) << 16) | ((green & 255) << 8) | (blue & 255);
    }

    private static String translationKey(String keySuffix, String part) {
        return "config." + ModSource.MODID + "." + keySuffix + "." + part;
    }

    public static final class BooleanEntry {
        private final String path;
        private final boolean defaultValue;
        private final String titleKey;
        private final String descriptionKey;
        private boolean value;

        private BooleanEntry(String path, boolean defaultValue, String titleKey, String descriptionKey) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.titleKey = titleKey;
            this.descriptionKey = descriptionKey;
            this.value = defaultValue;
        }

        public String path() {
            return this.path;
        }

        public boolean defaultValue() {
            return this.defaultValue;
        }

        public String titleKey() {
            return this.titleKey;
        }

        public String descriptionKey() {
            return this.descriptionKey;
        }

        public boolean get() {
            return this.value;
        }

        public void set(boolean value) {
            this.value = value;
        }

        public void reset() {
            set(this.defaultValue);
        }

        private void load(String serializedValue) {
            this.value = serializedValue == null ? this.defaultValue : Boolean.parseBoolean(serializedValue);
        }
    }
}

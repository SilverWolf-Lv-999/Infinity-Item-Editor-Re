package io.github.seraphina.infinity_item_editor_re;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<BooleanEntry> BOOLEAN_ENTRIES_BUILDER = new ArrayList<>();

    public static final BooleanEntry VOID_TAB = defineBoolean(
            "voidTab",
            true,
            "Whether the Infinity Void creative tab is shown.",
            "void_tab"
    );
    public static final BooleanEntry VOID_ADD_NOTIFICATION = defineBoolean(
            "voidAddNotification",
            false,
            "Whether newly discovered Infinity Void entries should notify the player.",
            "void_add_notification"
    );
    public static final BooleanEntry VOID_TAB_HIDE_HEADS = defineBoolean(
            "voidTabHideHeads",
            false,
            "Whether player heads should be hidden from the Infinity Void tab.",
            "void_tab_hide_heads"
    );
    public static final BooleanEntry UNAVAILABLE_TAB = defineBoolean(
            "unavailableTab",
            true,
            "Whether the unavailable-items creative tab is shown.",
            "unavailable_tab"
    );
    public static final BooleanEntry BANNER_TAB = defineBoolean(
            "bannerTab",
            true,
            "Whether the banner helper creative tab is shown.",
            "banner_tab"
    );
    public static final BooleanEntry HEAD_TAB = defineBoolean(
            "headTab",
            true,
            "Whether the head helper creative tab is shown.",
            "head_tab"
    );
    public static final BooleanEntry THIEF_TAB = defineBoolean(
            "thiefTab",
            true,
            "Whether the thief creative tab is shown.",
            "thief_tab"
    );
    public static final BooleanEntry FIREWORK_TAB = defineBoolean(
            "fireworkTab",
            true,
            "Whether the firework helper creative tab is shown.",
            "firework_tab"
    );

    private static final List<BooleanEntry> BOOLEAN_ENTRIES = Collections.unmodifiableList(BOOLEAN_ENTRIES_BUILDER);
    private static File configFile;

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

    public static List<BooleanEntry> booleanEntries() {
        return BOOLEAN_ENTRIES;
    }

    public static synchronized void load(File minecraftDirectory) {
        File configDirectory = new File(minecraftDirectory, "config");
        if (!configDirectory.exists() && !configDirectory.mkdirs()) {
            ModSource.LOGGER.warn("Failed to create config directory {}", configDirectory.getAbsolutePath());
        }

        configFile = new File(configDirectory, ModSource.MODID + ".json");
        if (configFile.exists()) {
            try (Reader reader = Files.newBufferedReader(configFile.toPath(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                itemGuiMode = readItemGuiMode(root);
                for (BooleanEntry entry : BOOLEAN_ENTRIES) {
                    entry.read(root);
                }
            } catch (Exception exception) {
                ModSource.LOGGER.error("Failed to load config {}", configFile.getAbsolutePath(), exception);
            }
        }

        syncPublicFields();
        save();
    }

    public static synchronized void save() {
        if (configFile == null) {
            return;
        }

        File parent = configFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            ModSource.LOGGER.warn("Failed to create config directory {}", parent.getAbsolutePath());
        }

        JsonObject root = new JsonObject();
        root.addProperty("itemGuiMode", itemGuiMode.name());
        for (BooleanEntry entry : BOOLEAN_ENTRIES) {
            root.addProperty(entry.path(), entry.get());
        }

        try (Writer writer = Files.newBufferedWriter(configFile.toPath(), StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        } catch (IOException exception) {
            ModSource.LOGGER.error("Failed to save config {}", configFile.getAbsolutePath(), exception);
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

    public static void syncFromSpec() {
        syncPublicFields();
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

    private static ItemEditorUiMode readItemGuiMode(JsonObject root) {
        if (root == null || !root.has("itemGuiMode")) {
            return ItemEditorUiMode.LEGACY;
        }

        try {
            return ItemEditorUiMode.valueOf(root.get("itemGuiMode").getAsString().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return ItemEditorUiMode.LEGACY;
        }
    }

    private static BooleanEntry defineBoolean(String path, boolean defaultValue, String comment, String keySuffix) {
        String titleKey = translationKey(keySuffix, "title");
        String descriptionKey = translationKey(keySuffix, "description");
        BooleanEntry entry = new BooleanEntry(path, defaultValue, titleKey, descriptionKey);
        BOOLEAN_ENTRIES_BUILDER.add(entry);
        return entry;
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
        private boolean cachedValue;

        private BooleanEntry(String path, boolean defaultValue, String titleKey, String descriptionKey) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.titleKey = titleKey;
            this.descriptionKey = descriptionKey;
            this.cachedValue = defaultValue;
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
            return this.cachedValue;
        }

        public void set(boolean value) {
            this.cachedValue = value;
        }

        public void reset() {
            set(this.defaultValue);
        }

        private void read(JsonObject root) {
            if (root != null && root.has(this.path) && root.get(this.path).isJsonPrimitive()) {
                this.cachedValue = root.get(this.path).getAsBoolean();
            }
        }
    }
}

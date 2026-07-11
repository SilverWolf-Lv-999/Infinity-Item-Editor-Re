package io.github.seraphina.infinity_item_editor_re;

import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EventBusSubscriber(modid = ModSource.MODID)
public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final List<BooleanEntry> BOOLEAN_ENTRIES_BUILDER = new ArrayList<>();

    private static final ModConfigSpec.EnumValue<ItemEditorUiMode> ITEM_GUI_MODE = BUILDER
            .comment("Item editor UI mode. LEGACY keeps the original editor layout. SIDEBAR enables the redesigned sidebar layout.")
            .translation(translationKey("item_gui_mode", "title"))
            .defineEnum("itemGuiMode", ItemEditorUiMode.LEGACY);
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

    public static final ModConfigSpec SPEC = BUILDER.build();
    private static final List<BooleanEntry> BOOLEAN_ENTRIES = Collections.unmodifiableList(BOOLEAN_ENTRIES_BUILDER);

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

    public static void save() {
        SPEC.save();
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
        itemGuiMode = mode;
        ITEM_GUI_MODE.set(mode);
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

    @SubscribeEvent
    public static void onLoad(ModConfigEvent event) {
        syncFromSpec();
    }

    public static void syncFromSpec() {
        for (BooleanEntry entry : BOOLEAN_ENTRIES) {
            entry.syncFromSpec();
        }
        syncPublicFields();
    }

    public static void syncPublicFields() {
        itemGuiMode = ITEM_GUI_MODE.get();
        voidTab = VOID_TAB.get();
        voidAddNotification = VOID_ADD_NOTIFICATION.get();
        voidTabHideHeads = VOID_TAB_HIDE_HEADS.get();
        unavailableTab = UNAVAILABLE_TAB.get();
        bannerTab = BANNER_TAB.get();
        headTab = HEAD_TAB.get();
        thiefTab = THIEF_TAB.get();
        fireworkTab = FIREWORK_TAB.get();
    }

    private static BooleanEntry defineBoolean(String path, boolean defaultValue, String comment, String keySuffix) {
        String titleKey = translationKey(keySuffix, "title");
        String descriptionKey = translationKey(keySuffix, "description");
        ModConfigSpec.BooleanValue value = BUILDER
                .comment(comment)
                .translation(titleKey)
                .define(path, defaultValue);
        BooleanEntry entry = new BooleanEntry(path, defaultValue, titleKey, descriptionKey, value);
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
        private final ModConfigSpec.BooleanValue value;
        private boolean cachedValue;

        private BooleanEntry(String path, boolean defaultValue, String titleKey, String descriptionKey, ModConfigSpec.BooleanValue value) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.titleKey = titleKey;
            this.descriptionKey = descriptionKey;
            this.value = value;
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
            this.value.set(value);
            this.cachedValue = value;
        }

        public void reset() {
            set(this.defaultValue);
        }

        private void syncFromSpec() {
            this.cachedValue = this.value.get();
        }
    }
}

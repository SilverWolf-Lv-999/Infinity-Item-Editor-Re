package io.github.seraphina.infinity_item_editor_re.client;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import java.lang.reflect.Field;

public final class ModernFixCompat {
    private static Field CACHE_FIELD;
    private static boolean LOADED = false;

    static {
        try {
            CACHE_FIELD = CreativeModeTab.class.getDeclaredField("mfix$oldParameters");
            CACHE_FIELD.setAccessible(true);
            LOADED = true;
        } catch (NoSuchFieldException ignored) {}
    }

    public static void clearAllCaches() {
        if (!LOADED) return;
        try {
            for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
                CACHE_FIELD.set(tab, null);
            }
        } catch (Exception ignored) {}
    }

    public static void clearCache(CreativeModeTab tab) {
        if (LOADED) {
            try {
                CACHE_FIELD.set(tab, null);
            } catch (IllegalAccessException ignored) {}
        }
    }
}

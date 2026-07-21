package io.github.seraphina.infinity_item_editor_re.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

public final class CreativeTabRefresher {
    private CreativeTabRefresher() {
    }

    public static void refreshRealm(Minecraft minecraft) {
        refreshOpenScreen(minecraft);
    }

    public static void refreshThief(Minecraft minecraft) {
        refreshOpenScreen(minecraft);
    }

    public static void rebuildAllTabs(Minecraft minecraft) {
        refreshOpenScreen(minecraft);
    }

    private static void refreshOpenScreen(Minecraft minecraft) {
        if (minecraft == null || !(minecraft.screen instanceof CreativeModeInventoryScreen creativeScreen)) {
            return;
        }
        creativeScreen.resize(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    }
}

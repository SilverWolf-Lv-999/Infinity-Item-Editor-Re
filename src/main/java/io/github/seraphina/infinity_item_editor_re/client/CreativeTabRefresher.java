package io.github.seraphina.infinity_item_editor_re.client;

import io.github.seraphina.infinity_item_editor_re.init.CreativeTabRegistry;
import io.github.seraphina.infinity_item_editor_re.mixin.CreativeModeInventoryScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;

public final class CreativeTabRefresher {
    private CreativeTabRefresher() {
    }

    public static void refreshRealm(Minecraft minecraft) {
        refreshOpenScreen(minecraft, CreativeTabRegistry.REALM);
    }

    public static void refreshThief(Minecraft minecraft) {
        refreshOpenScreen(minecraft, CreativeTabRegistry.THIEF);
    }

    public static void refreshVoid(Minecraft minecraft) {
        refreshOpenScreen(minecraft, CreativeTabRegistry.VOID);
    }

    public static void rebuildAllTabs(Minecraft minecraft) {
        if (minecraft == null || !(minecraft.screen instanceof CreativeModeInventoryScreen creativeScreen)) {
            return;
        }

        int selectedTab = creativeScreen.getSelectedTab();
        if (selectedTab >= 0 && selectedTab < CreativeModeTab.TABS.length) {
            refreshOpenScreen(minecraft, CreativeModeTab.TABS[selectedTab]);
        }
    }

    private static void refreshOpenScreen(Minecraft minecraft, CreativeModeTab tab) {
        if (minecraft == null || tab == null
                || !(minecraft.screen instanceof CreativeModeInventoryScreen creativeScreen)
                || creativeScreen.getSelectedTab() != tab.getId()) {
            return;
        }

        ((CreativeModeInventoryScreenAccessor) creativeScreen).infinityItemEditorRe$selectTab(tab);
    }
}

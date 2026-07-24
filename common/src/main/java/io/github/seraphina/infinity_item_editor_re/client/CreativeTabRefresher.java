package io.github.seraphina.infinity_item_editor_re.client;

import io.github.seraphina.infinity_item_editor_re.init.CreativeTabRegistry;
import io.github.seraphina.infinity_item_editor_re.mixin.CreativeModeInventoryScreenAccessor;
import io.github.seraphina.infinity_item_editor_re.util.MinecraftCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

public final class CreativeTabRefresher {
    private CreativeTabRefresher() {}

    public static void refreshRealm(Minecraft minecraft) {
        if (CreativeTabRegistry.REALM.isBound()) {
            refresh(minecraft, CreativeTabRegistry.REALM.get());
        }
    }

    public static void refreshThief(Minecraft minecraft) {
        if (CreativeTabRegistry.THIEF.isBound()) {
            refresh(minecraft, CreativeTabRegistry.THIEF.get());
        }
    }

    public static void rebuildAllTabs(Minecraft minecraft) {
        if (minecraft == null || minecraft.player == null) return;

        ModernFixCompat.clearAllCaches();

        CreativeModeTab.ItemDisplayParameters params = createParameters(minecraft);
        CreativeModeTabs.allTabs().forEach(tab -> tab.buildContents(params));
        refreshOpenScreen(minecraft);
    }

    private static void refresh(Minecraft minecraft, CreativeModeTab tab) {
        if (minecraft == null || minecraft.player == null) return;

        ModernFixCompat.clearCache(tab);
        tab.buildContents(createParameters(minecraft));
        refreshOpenScreen(minecraft, tab);
    }

    private static CreativeModeTab.ItemDisplayParameters createParameters(Minecraft minecraft) {
        boolean hasPermissions = minecraft.player.canUseGameMasterBlocks() && minecraft.options.operatorItemsTab().get();
        return new CreativeModeTab.ItemDisplayParameters(
                minecraft.player.connection.enabledFeatures(),
                hasPermissions,
                minecraft.player.level().registryAccess()
        );
    }

    private static void refreshOpenScreen(Minecraft minecraft) {
        if (!(MinecraftCompat.screen(minecraft) instanceof CreativeModeInventoryScreen creativeScreen)) return;

        CreativeModeTab selectedTab = CreativeModeInventoryScreenAccessor.infinityItemEditorRe$getSelectedTab();
        CreativeModeInventoryScreenAccessor accessor = (CreativeModeInventoryScreenAccessor) creativeScreen;

        if (!selectedTab.shouldDisplay()) {
            accessor.infinityItemEditorRe$selectTab(CreativeModeTabs.getDefaultTab());
            return;
        }
        accessor.infinityItemEditorRe$refreshCurrentTabContents(selectedTab.getDisplayItems());
    }

    private static void refreshOpenScreen(Minecraft minecraft, CreativeModeTab tab) {
        var currentScreen = MinecraftCompat.screen(minecraft);
        if (currentScreen instanceof CreativeModeInventoryScreen
                && CreativeModeInventoryScreenAccessor.infinityItemEditorRe$getSelectedTab() == tab) {
            ((CreativeModeInventoryScreenAccessor) currentScreen)
                    .infinityItemEditorRe$refreshCurrentTabContents(tab.getDisplayItems());
        }
    }
}
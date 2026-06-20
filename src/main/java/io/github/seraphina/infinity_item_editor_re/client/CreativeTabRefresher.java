package io.github.seraphina.infinity_item_editor_re.client;

import io.github.seraphina.infinity_item_editor_re.init.CreativeTabRegistry;
import io.github.seraphina.infinity_item_editor_re.mixin.CreativeModeInventoryScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

public final class CreativeTabRefresher {
    private CreativeTabRefresher() {
    }

    public static void refreshRealm(Minecraft minecraft) {
        if (!CreativeTabRegistry.REALM.isPresent()) {
            return;
        }

        refresh(minecraft, CreativeTabRegistry.REALM.get());
    }

    public static void refreshThief(Minecraft minecraft) {
        if (!CreativeTabRegistry.THIEF.isPresent()) {
            return;
        }

        refresh(minecraft, CreativeTabRegistry.THIEF.get());
    }

    public static void rebuildAllTabs(Minecraft minecraft) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        CreativeModeTab.ItemDisplayParameters parameters = createParameters(minecraft);
        CreativeModeTabs.allTabs().stream()
                .filter(tab -> tab.getType() == CreativeModeTab.Type.CATEGORY)
                .forEach(tab -> tab.buildContents(parameters));
        CreativeModeTabs.allTabs().stream()
                .filter(tab -> tab.getType() != CreativeModeTab.Type.CATEGORY)
                .forEach(tab -> tab.buildContents(parameters));
        refreshOpenScreen(minecraft);
    }

    private static void refresh(Minecraft minecraft, CreativeModeTab tab) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

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
        if (!(minecraft.screen instanceof CreativeModeInventoryScreen creativeScreen)) {
            return;
        }

        CreativeModeTab selectedTab = CreativeModeInventoryScreenAccessor.infinityItemEditorRe$getSelectedTab();
        CreativeModeInventoryScreenAccessor accessor = (CreativeModeInventoryScreenAccessor) creativeScreen;
        if (!selectedTab.shouldDisplay()) {
            accessor.infinityItemEditorRe$selectTab(CreativeModeTabs.getDefaultTab());
            return;
        }

        accessor.infinityItemEditorRe$refreshCurrentTabContents(selectedTab.getDisplayItems());
    }

    private static void refreshOpenScreen(Minecraft minecraft, CreativeModeTab tab) {
        if (minecraft.screen instanceof CreativeModeInventoryScreen
                && CreativeModeInventoryScreenAccessor.infinityItemEditorRe$getSelectedTab() == tab) {
            ((CreativeModeInventoryScreenAccessor) minecraft.screen).infinityItemEditorRe$refreshCurrentTabContents(tab.getDisplayItems());
        }
    }
}

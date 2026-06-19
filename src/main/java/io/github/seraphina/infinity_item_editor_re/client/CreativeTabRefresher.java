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
        if (minecraft == null || minecraft.player == null || !CreativeTabRegistry.REALM.isPresent()) {
            return;
        }

        CreativeModeTab realmTab = CreativeTabRegistry.REALM.get();
        boolean hasPermissions = minecraft.player.canUseGameMasterBlocks() && minecraft.options.operatorItemsTab().get();
        realmTab.buildContents(new CreativeModeTab.ItemDisplayParameters(
                minecraft.player.connection.enabledFeatures(),
                hasPermissions,
                minecraft.player.level().registryAccess()
        ));

        if (minecraft.screen instanceof CreativeModeInventoryScreen
                && CreativeModeInventoryScreenAccessor.infinityItemEditorRe$getSelectedTab() == realmTab) {
            ((CreativeModeInventoryScreenAccessor) minecraft.screen).infinityItemEditorRe$refreshCurrentTabContents(realmTab.getDisplayItems());
        }
    }
}

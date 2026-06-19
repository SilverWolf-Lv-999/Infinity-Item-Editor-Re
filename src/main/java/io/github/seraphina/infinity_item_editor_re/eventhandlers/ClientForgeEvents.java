package io.github.seraphina.infinity_item_editor_re.eventhandlers;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.data.realms.RealmController;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModSource.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientForgeEvents {
    private ClientForgeEvents() {
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        while (ClientKeyMappings.SAVE_REALM.consumeClick()) {
            RealmController realmController = ModSource.getRealmController();
            if (realmController == null) {
                ModSource.initClientStorage(minecraft.gameDirectory);
                realmController = ModSource.getRealmController();
            }

            if (realmController != null) {
                ItemStack heldStack = minecraft.player.getMainHandItem();
                realmController.addItemStack(minecraft.player, heldStack.copy());
            }
        }

        while (ClientKeyMappings.OPEN_EDITOR.consumeClick()) {
            minecraft.player.displayClientMessage(Component.translatable("message." + ModSource.MODID + ".editor_not_ported"), true);
        }

        while (ClientKeyMappings.COPY_TARGET.consumeClick()) {
            minecraft.player.displayClientMessage(Component.translatable("message." + ModSource.MODID + ".copy_not_ported"), true);
        }
    }
}

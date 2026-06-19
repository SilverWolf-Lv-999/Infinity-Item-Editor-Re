package io.github.seraphina.infinity_item_editor_re.eventhandlers;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ModSource.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ModSource.initClientStorage(Minecraft.getInstance().gameDirectory));
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientKeyMappings.OPEN_EDITOR);
        event.register(ClientKeyMappings.COPY_TARGET);
        event.register(ClientKeyMappings.SAVE_REALM);
    }
}

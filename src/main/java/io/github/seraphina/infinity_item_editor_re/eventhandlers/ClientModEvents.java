package io.github.seraphina.infinity_item_editor_re.eventhandlers;

import net.neoforged.fml.common.EventBusSubscriber;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.VoidConsumer;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = ModSource.MODID, value = Dist.CLIENT)
public final class ClientModEvents {
    private static boolean voidConsumerStarted;

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ModSource.initClientStorage(Minecraft.getInstance().gameDirectory);
            startVoidConsumer();
        });
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientKeyMappings.OPEN_EDITOR);
        event.register(ClientKeyMappings.COPY_TARGET);
        event.register(ClientKeyMappings.SAVE_REALM);
    }

    private static synchronized void startVoidConsumer() {
        if (voidConsumerStarted) {
            return;
        }

        Thread voidThread = new Thread(new VoidConsumer(ModSource.voidBuffer), "Infinity Item Editor Void Consumer");
        voidThread.setDaemon(true);
        voidThread.start();
        voidConsumerStarted = true;
    }
}

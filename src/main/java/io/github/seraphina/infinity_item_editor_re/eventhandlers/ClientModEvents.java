package io.github.seraphina.infinity_item_editor_re.eventhandlers;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.VoidConsumer;
import io.github.seraphina.infinity_item_editor_re.init.CreativeTabRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ModSource.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientModEvents {
    private static boolean voidConsumerStarted;

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CreativeTabRegistry.bootstrap();
            ClientRegistry.registerKeyBinding(ClientKeyMappings.OPEN_EDITOR);
            ClientRegistry.registerKeyBinding(ClientKeyMappings.COPY_TARGET);
            ClientRegistry.registerKeyBinding(ClientKeyMappings.SAVE_REALM);
            ModSource.initClientStorage(Minecraft.getInstance().gameDirectory);
            startVoidConsumer();
        });
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

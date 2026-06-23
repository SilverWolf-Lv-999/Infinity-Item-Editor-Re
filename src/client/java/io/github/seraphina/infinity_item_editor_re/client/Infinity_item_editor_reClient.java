package io.github.seraphina.infinity_item_editor_re.client;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.eventhandlers.ClientEvents;
import io.github.seraphina.infinity_item_editor_re.eventhandlers.ClientKeyMappings;
import io.github.seraphina.infinity_item_editor_re.init.CreativeTabRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;

public class Infinity_item_editor_reClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Minecraft minecraft = Minecraft.getInstance();
        ModSource.initClientStorage(minecraft.gameDirectory);
        CreativeTabRegistry.register();
        ClientKeyMappings.register();
        ClientEvents.register();
        ModSource.startVoidConsumer();
    }
}

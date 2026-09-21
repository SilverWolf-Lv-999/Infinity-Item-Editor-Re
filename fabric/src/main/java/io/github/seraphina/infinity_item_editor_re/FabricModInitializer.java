package io.github.seraphina.infinity_item_editor_re;

import io.github.seraphina.infinity_item_editor_re.client.ClientBootstrap;
import io.github.seraphina.infinity_item_editor_re.eventhandlers.ClientEvents;
import io.github.seraphina.infinity_item_editor_re.eventhandlers.ClientKeyMappings;
import io.github.seraphina.infinity_item_editor_re.init.CreativeTabRegistry;
import io.github.seraphina.infinity_item_editor_re.mixin.ConnectionAccessor;
import io.github.seraphina.infinity_item_editor_re.platform.PlatformServices;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class FabricModInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        PlatformServices.setModDisplayNameResolver(namespace -> FabricLoader.getInstance()
                .getModContainer(namespace)
                .map(container -> container.getMetadata().getName()));

        CreativeTabRegistry.register(
                FabricModInitializer::registerCreativeTab,
                FabricModInitializer::buildCreativeTab
        );
        ClientBootstrap.initialize(FabricLoader.getInstance().getGameDir().toFile());
        registerKeyMappings();
        registerEvents();
    }

    private static Supplier<CreativeModeTab> registerCreativeTab(
            String name,
            Supplier<CreativeModeTab> factory
    ) {
        CreativeModeTab tab = Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(ModSource.MODID, name),
                factory.get()
        );
        return () -> tab;
    }

    private static CreativeModeTab buildCreativeTab(
            String name,
            CreativeModeTab.Row row,
            int column,
            Supplier<ItemStack> icon,
            CreativeTabRegistry.TabContentsGenerator generator
    ) {
        return CreativeModeTab.builder(row, column)
                .title(Component.translatable("itemGroup." + ModSource.MODID + "." + name))
                .icon(icon)
                .displayItems((parameters, output) -> generator.accept(parameters, output::accept))
                .build();
    }

    private static void registerKeyMappings() {
        KeyMappingHelper.registerKeyMapping(ClientKeyMappings.OPEN_EDITOR);
        KeyMappingHelper.registerKeyMapping(ClientKeyMappings.COPY_TARGET);
        KeyMappingHelper.registerKeyMapping(ClientKeyMappings.SAVE_REALM);
    }

    private static void registerEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onClientTick);
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.allowKeyPress(screen).register((currentScreen, event) ->
                    !ClientEvents.handleScreenKeyPressed(currentScreen, event.key(), event.scancode()));
            ScreenMouseEvents.allowMouseClick(screen).register((currentScreen, event) ->
                    !ClientEvents.handleScreenMousePressed(currentScreen, event.button()));
        });
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->
                ClientEvents.onChatReceived(message));
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> ClientEvents.onChatReceived(message));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                ClientEvents.onServerConnection(((ConnectionAccessor) handler.getConnection())
                        .infinityItemEditorRe$getChannel()));
    }
}

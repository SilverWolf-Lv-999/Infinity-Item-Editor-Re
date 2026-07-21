package io.github.seraphina.infinity_item_editor_re.client;

import io.github.seraphina.infinity_item_editor_re.client.screen.InfinityConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;

@OnlyIn(Dist.CLIENT)
public final class ConfigScreenRegistration {
    private ConfigScreenRegistration() {
    }

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, parent) -> new InfinityConfigScreen(parent))
        );
    }
}

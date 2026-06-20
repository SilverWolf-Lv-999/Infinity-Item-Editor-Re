package io.github.seraphina.infinity_item_editor_re.client;

import io.github.seraphina.infinity_item_editor_re.client.screen.InfinityConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@OnlyIn(Dist.CLIENT)
public final class ConfigScreenRegistration {
    private ConfigScreenRegistration() {
    }

    public static void register(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new InfinityConfigScreen(parent))
        );
    }
}

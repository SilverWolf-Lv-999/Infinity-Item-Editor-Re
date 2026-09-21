package io.github.seraphina.infinity_item_editor_re;

import io.github.seraphina.infinity_item_editor_re.client.ConfigScreenRegistration;
import io.github.seraphina.infinity_item_editor_re.init.NeoForgeCreativeTabs;
import io.github.seraphina.infinity_item_editor_re.platform.PlatformServices;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

@Mod(value = ModSource.MODID, dist = Dist.CLIENT)
public final class NeoForgeMod {
    public NeoForgeMod(IEventBus modEventBus, ModContainer modContainer) {
        PlatformServices.setModDisplayNameResolver(namespace -> ModList.get()
                .getModContainerById(namespace)
                .map(container -> container.getModInfo().getDisplayName()));
        NeoForgeCreativeTabs.register(modEventBus);
        ConfigScreenRegistration.register(modContainer);
    }
}

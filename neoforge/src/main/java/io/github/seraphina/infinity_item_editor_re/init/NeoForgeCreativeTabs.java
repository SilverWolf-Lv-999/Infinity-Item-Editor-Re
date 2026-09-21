package io.github.seraphina.infinity_item_editor_re.init;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class NeoForgeCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModSource.MODID);

    private NeoForgeCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CreativeTabRegistry.register(
                CREATIVE_TABS::register,
                NeoForgeCreativeTabs::buildSearchTab
        );
        CREATIVE_TABS.register(modEventBus);
    }

    private static CreativeModeTab buildSearchTab(
            String name,
            CreativeModeTab.Row row,
            int column,
            Supplier<ItemStack> icon,
            CreativeTabRegistry.TabContentsGenerator generator
    ) {
        return CreativeModeTab.builder(row, column)
                .title(Component.translatable("itemGroup." + ModSource.MODID + "." + name))
                .icon(icon)
                .withSearchBar()
                .displayItems((parameters, output) -> generator.accept(parameters, output::accept))
                .build();
    }
}

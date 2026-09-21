package io.github.seraphina.infinity_item_editor_re.mixin;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;

@Mixin(CreativeModeInventoryScreen.class)
public interface CreativeModeInventoryScreenAccessor {
    @Accessor("selectedTab")
    static CreativeModeTab infinityItemEditorRe$getSelectedTab() {
        throw new AssertionError();
    }

    @Invoker("refreshCurrentTabContents")
    void infinityItemEditorRe$refreshCurrentTabContents(Collection<ItemStack> stacks);

    @Invoker("selectTab")
    void infinityItemEditorRe$selectTab(CreativeModeTab tab);
}

package io.github.seraphina.infinity_item_editor_re.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccessor {
    @Accessor("slot")
    int infinityItemEditorRe$getContainerSlot();
}

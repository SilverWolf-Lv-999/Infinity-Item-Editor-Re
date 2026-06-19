package io.github.seraphina.infinity_item_editor_re.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class GiveHelper {
    private GiveHelper() {
    }

    public static String getStringFromItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }

        ResourceLocation itemName = ForgeRegistries.ITEMS.getKey(stack.getItem());
        StringBuilder command = new StringBuilder("/give @p ");
        command.append(itemName == null ? "minecraft:air" : itemName);

        CompoundTag tag = stack.getTag();
        if (tag != null && !tag.isEmpty()) {
            command.append(tag);
        }

        if (stack.getCount() != 1) {
            command.append(' ').append(stack.getCount());
        }

        return command.toString();
    }
}

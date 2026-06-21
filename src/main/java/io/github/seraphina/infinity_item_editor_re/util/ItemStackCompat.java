package io.github.seraphina.infinity_item_editor_re.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class ItemStackCompat {
    private ItemStackCompat() {
    }

    public static boolean isSameItemSameTags(ItemStack first, ItemStack second) {
        return ItemStack.isSameItemSameComponents(first, second);
    }

    public static boolean hasCustomHoverName(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_NAME);
    }

    public static void setHoverName(ItemStack stack, Component name) {
        stack.set(DataComponents.CUSTOM_NAME, name);
    }

    public static void resetHoverName(ItemStack stack) {
        stack.remove(DataComponents.CUSTOM_NAME);
    }

    public static List<Component> getTooltipLines(ItemStack stack, Player player, TooltipFlag flag) {
        Item.TooltipContext context = player == null || player.level() == null
                ? Item.TooltipContext.of(ItemStackNbt.provider())
                : Item.TooltipContext.of(player.level());
        return stack.getTooltipLines(context, player, flag);
    }
}

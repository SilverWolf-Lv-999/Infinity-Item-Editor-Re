package io.github.seraphina.infinity_item_editor_re.client.screen;

import net.minecraft.world.item.ItemStack;

enum HideFlag {
    ENCHANTMENTS(ItemStack.TooltipPart.ENCHANTMENTS, "flag.enchantment"),
    ATTRIBUTE_MODIFIERS(ItemStack.TooltipPart.MODIFIERS, "flag.attributemod"),
    UNBREAKABLE(ItemStack.TooltipPart.UNBREAKABLE, "flag.unbreakable"),
    CAN_DESTROY(ItemStack.TooltipPart.CAN_DESTROY, "flag.candestroy"),
    CAN_PLACE_ON(ItemStack.TooltipPart.CAN_PLACE, "flag.canplaceon"),
    ITEM_INFO(ItemStack.TooltipPart.ADDITIONAL, "flag.iteminfo"),
    DYE(ItemStack.TooltipPart.DYE, "flag.dye"),
    UPGRADES(ItemStack.TooltipPart.UPGRADES, "flag.upgrades");

    private final ItemStack.TooltipPart tooltipPart;
    private final String translationKey;

    HideFlag(ItemStack.TooltipPart tooltipPart, String translationKey) {
        this.tooltipPart = tooltipPart;
        this.translationKey = translationKey;
    }

    int mask() {
        return this.tooltipPart.getMask();
    }

    String translationKey() {
        return this.translationKey;
    }
}

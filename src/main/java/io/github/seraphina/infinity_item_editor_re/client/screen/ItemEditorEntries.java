package io.github.seraphina.infinity_item_editor_re.client.screen;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

record EnchantmentEntry(Identifier id, Enchantment enchantment, int level) {
}

record AttributeEntry(int tagIndex, String attributeName, Attribute attribute, double amount, int operation, String slotName) {
}

record NbtRow(String path, String displayText, boolean isExpandable, int depth) {
}

record BannerPatternEntry(String name, String hash) {
}

record PotterySherdEntry(String name, Item item) {
}

record SpawnEggEntityEntry(Identifier id, EntityType<?> type) {
}

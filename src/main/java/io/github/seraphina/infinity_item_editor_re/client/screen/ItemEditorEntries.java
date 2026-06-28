package io.github.seraphina.infinity_item_editor_re.client.screen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

record EnchantmentEntry(ResourceLocation id, Enchantment enchantment, int level) {
}

record EnchantmentGroupEntry(String namespace, List<Enchantment> enchantments) {
}

record AttributeEntry(int tagIndex, String attributeName, Attribute attribute, double amount, int operation, String slotName) {
}

record AttributeGroupEntry(String namespace, List<Attribute> attributes) {
}

record NbtRow(String path, String displayText, boolean isExpandable, int depth) {
}

record BannerPatternEntry(String name, String hash) {
}

record SpawnEggEntityEntry(ResourceLocation id, EntityType<?> type) {
}

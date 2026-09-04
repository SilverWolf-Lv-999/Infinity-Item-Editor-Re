package io.github.seraphina.infinity_item_editor_re.client.screen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;

import java.util.List;

record EnchantmentEntry(ResourceLocation id, Enchantment enchantment, int level) {
}

record EnchantmentGroupEntry(String namespace, List<Enchantment> enchantments) {
}

record PotionGroupEntry(String namespace, List<MobEffect> effects) {
}

record AttributeEntry(int tagIndex, String attributeName, Attribute attribute, double amount, int operation, String slotName) {
}

record AttributeGroupEntry(String namespace, List<Attribute> attributes) {
}

record NbtRow(String path, String displayText, boolean isExpandable, int depth) {
}

record BannerPatternEntry(String name, String hash) {
}

record PotterySherdEntry(String name, Item item) {
}

record SpawnEggEntityEntry(ResourceLocation id, EntityType<?> type) {
}

record ArmorTrimMaterialEntry(ResourceLocation id, Holder<TrimMaterial> material) {
}

record ArmorTrimPatternEntry(ResourceLocation id, Holder<TrimPattern> pattern) {
}

record ArmorTrimEntry(ResourceLocation materialId, ResourceLocation patternId) {
}

package io.github.seraphina.infinity_item_editor_re.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

import java.util.Collection;
import java.util.List;

public final class CompatRegistries {
    public static final RegistryAdapter<Item> ITEMS = new RegistryAdapter<>(BuiltInRegistries.ITEM);
    public static final RegistryAdapter<Block> BLOCKS = new RegistryAdapter<>(BuiltInRegistries.BLOCK);
    public static final RegistryAdapter<EntityType<?>> ENTITY_TYPES = new RegistryAdapter<>(BuiltInRegistries.ENTITY_TYPE);
    public static final RegistryAdapter<MobEffect> MOB_EFFECTS = new RegistryAdapter<>(BuiltInRegistries.MOB_EFFECT);
    public static final RegistryAdapter<Attribute> ATTRIBUTES = new RegistryAdapter<>(BuiltInRegistries.ATTRIBUTE);
    public static final DynamicRegistryAdapter<Enchantment> ENCHANTMENTS = new DynamicRegistryAdapter<>(Registries.ENCHANTMENT);

    private CompatRegistries() {
    }

    public static class RegistryAdapter<T> {
        private final Registry<T> registry;

        private RegistryAdapter(Registry<T> registry) {
            this.registry = registry;
        }

        public Identifier getKey(T value) {
            return this.registry.getKey(value);
        }

        public T getValue(Identifier id) {
            return this.registry.getValue(id);
        }

        public Holder<T> getHolder(T value) {
            return value == null ? null : this.registry.wrapAsHolder(value);
        }

        public Holder<T> getHolder(Identifier id) {
            return this.registry.get(id).orElse(null);
        }

        public Collection<Holder.Reference<T>> getHolders() {
            return this.registry.listElements().toList();
        }

        public Collection<T> getValues() {
            return this.registry.stream().toList();
        }
    }

    public static final class DynamicRegistryAdapter<T> {
        private final ResourceKey<Registry<T>> key;

        private DynamicRegistryAdapter(ResourceKey<Registry<T>> key) {
            this.key = key;
        }

        public Identifier getKey(T value) {
            Registry<T> registry = registry();
            return registry == null ? null : registry.getKey(value);
        }

        public T getValue(Identifier id) {
            Registry<T> registry = registry();
            return registry == null ? null : registry.getValue(id);
        }

        public Holder<T> getHolder(T value) {
            Registry<T> registry = registry();
            return registry == null || value == null ? null : registry.wrapAsHolder(value);
        }

        public Holder<T> getHolder(Identifier id) {
            Registry<T> registry = registry();
            return registry == null ? null : registry.get(id).orElse(null);
        }

        public Collection<Holder.Reference<T>> getHolders() {
            Registry<T> registry = registry();
            return registry == null ? List.of() : registry.listElements().toList();
        }

        public Collection<T> getValues() {
            Registry<T> registry = registry();
            return registry == null ? List.of() : registry.stream().toList();
        }

        private Registry<T> registry() {
            RegistryAccess access = ItemStackNbt.registryAccess();
            return access.lookup(this.key).orElse(null);
        }
    }
}

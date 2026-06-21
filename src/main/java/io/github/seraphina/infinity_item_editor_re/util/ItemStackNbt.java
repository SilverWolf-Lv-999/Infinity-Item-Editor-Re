package io.github.seraphina.infinity_item_editor_re.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public final class ItemStackNbt {
    private ItemStackNbt() {
    }

    public static CompoundTag get(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null || data.isEmpty() ? null : data.getUnsafe();
    }

    public static CompoundTag getOrCreate(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            CompoundTag tag = new CompoundTag();
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            return tag;
        }
        return data.getUnsafe();
    }

    public static CompoundTag getElement(ItemStack stack, String key) {
        CompoundTag tag = get(stack);
        if (tag == null || !tag.contains(key, Tag.TAG_COMPOUND)) {
            return null;
        }
        return tag.getCompound(key);
    }

    public static CompoundTag getOrCreateElement(ItemStack stack, String key) {
        CompoundTag tag = getOrCreate(stack);
        if (!tag.contains(key, Tag.TAG_COMPOUND)) {
            tag.put(key, new CompoundTag());
        }
        return tag.getCompound(key);
    }

    public static void set(ItemStack stack, CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
            return;
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static CompoundTag save(ItemStack stack) {
        Tag saved = stack.saveOptional(provider());
        return saved instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
    }

    public static ItemStack parse(CompoundTag tag) {
        ItemStack parsed = ItemStack.parseOptional(provider(), tag);
        if (!parsed.isEmpty() || !isLegacyStackTag(tag)) {
            return parsed;
        }

        ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
        Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        int count = tag.contains("Count") ? tag.getByte("Count") : 1;
        ItemStack stack = new ItemStack(item, Math.max(1, count));
        if (tag.contains("tag", Tag.TAG_COMPOUND)) {
            set(stack, tag.getCompound("tag").copy());
        }
        return stack;
    }

    public static HolderLookup.Provider provider() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                return minecraft.level.registryAccess();
            }
        } catch (RuntimeException | LinkageError ignored) {
        }

        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    public static RegistryAccess registryAccess() {
        HolderLookup.Provider provider = provider();
        return provider instanceof RegistryAccess registryAccess ? registryAccess : RegistryAccess.EMPTY;
    }

    private static boolean isLegacyStackTag(CompoundTag tag) {
        return tag.contains("id") && (tag.contains("Count") || tag.contains("tag"));
    }
}

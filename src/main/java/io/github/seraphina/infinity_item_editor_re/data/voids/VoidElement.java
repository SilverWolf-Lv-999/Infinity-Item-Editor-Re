package io.github.seraphina.infinity_item_editor_re.data.voids;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class VoidElement {
    private final ItemStack stack;
    private ListTag uuids;

    public VoidElement(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }

    public static VoidElement readFromTag(CompoundTag tag) {
        ItemStack readStack = ItemStackNbt.parse(tag);
        ListTag ids = tag.getList("uuids", Tag.TAG_STRING);

        if (readStack.isEmpty() && tag.contains("stack", Tag.TAG_COMPOUND)) {
            readStack = ItemStackNbt.parse(tag.getCompound("stack"));
        }

        VoidElement element = new VoidElement(readStack);
        if (!ids.isEmpty()) {
            element.uuids = ids;
        }
        return element;
    }

    public CompoundTag writeToTag(CompoundTag tag) {
        tag.put("stack", ItemStackNbt.save(stack));

        if (uuids != null && !uuids.isEmpty()) {
            tag.put("uuids", uuids.copy());
        }

        return tag;
    }

    public boolean hasUuid(String id) {
        if (uuids == null) {
            return false;
        }

        for (Tag tag : uuids) {
            if (id.equals(tag.getAsString())) {
                return true;
            }
        }

        return false;
    }

    public boolean addUuid(String id, boolean check) {
        if (id == null || id.isEmpty() || (check && hasUuid(id))) {
            return false;
        }

        if (uuids == null) {
            uuids = new ListTag();
        }

        uuids.add(StringTag.valueOf(id));
        return true;
    }
}

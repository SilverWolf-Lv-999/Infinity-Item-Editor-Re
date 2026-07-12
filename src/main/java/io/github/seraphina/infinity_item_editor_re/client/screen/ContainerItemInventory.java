package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;
import io.github.seraphina.infinity_item_editor_re.util.NbtCompat;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class ContainerItemInventory implements Container {
    private static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    private static final String CONTAINER_ITEMS_TAG = "Items";
    private static final int SIZE = 27;

    private final ItemStack containerStack;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    ContainerItemInventory(ItemStack containerStack) {
        this.containerStack = containerStack;
        loadFromStack();
    }

    Component getDisplayName() {
        return this.containerStack.getHoverName();
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return isValidSlot(slot) ? this.items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(this.items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (!isValidSlot(slot)) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = ContainerHelper.takeItem(this.items, slot);
        if (!removed.isEmpty()) {
            saveToStack();
        }
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!isValidSlot(slot)) {
            return;
        }
        this.items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public void setChanged() {
        saveToStack();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < this.items.size(); slot++) {
            this.items.set(slot, ItemStack.EMPTY);
        }
        setChanged();
    }

    private void saveToStack() {
        CompoundTag blockEntity = ItemStackNbt.getOrCreateElement(this.containerStack, BLOCK_ENTITY_TAG);
        ListTag savedItems = new ListTag();
        for (int slot = 0; slot < this.items.size(); slot++) {
            ItemStack stack = this.items.get(slot);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = ItemStackNbt.save(stack);
                itemTag.putByte("Slot", (byte) slot);
                savedItems.add(itemTag);
            }
        }
        if (savedItems.isEmpty()) {
            blockEntity.remove(CONTAINER_ITEMS_TAG);
        } else {
            blockEntity.put(CONTAINER_ITEMS_TAG, savedItems);
        }
        cleanupBlockEntityTag(blockEntity);
    }

    private void loadFromStack() {
        CompoundTag blockEntity = ItemStackNbt.getElement(this.containerStack, BLOCK_ENTITY_TAG);
        if (blockEntity == null) {
            return;
        }
        ListTag savedItems = NbtCompat.getList(blockEntity, CONTAINER_ITEMS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < savedItems.size(); i++) {
            CompoundTag itemTag = NbtCompat.getCompound(savedItems, i).copy();
            int slot = NbtCompat.getByte(itemTag, "Slot") & 255;
            if (isValidSlot(slot)) {
                itemTag.remove("Slot");
                this.items.set(slot, ItemStackNbt.parse(itemTag));
            }
        }
    }

    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot < this.items.size();
    }

    private void cleanupBlockEntityTag(CompoundTag blockEntity) {
        CompoundTag tag = ItemStackNbt.get(this.containerStack);
        if (tag == null) {
            return;
        }

        if (blockEntity.isEmpty()) {
            tag.remove(BLOCK_ENTITY_TAG);
        } else {
            tag.put(BLOCK_ENTITY_TAG, blockEntity);
        }

        if (tag.isEmpty()) {
            ItemStackNbt.set(this.containerStack, null);
        }
    }
}

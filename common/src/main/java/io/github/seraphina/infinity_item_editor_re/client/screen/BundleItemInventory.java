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

final class BundleItemInventory implements Container {
    static final int ROWS = 6;
    static final int COLUMNS = 9;
    static final int PAGE_SIZE = ROWS * COLUMNS;

    private static final String ITEMS_TAG = "Items";

    private final ItemStack bundleStack;
    private final NonNullList<ItemStack> items = NonNullList.create();
    private int pageStart;

    BundleItemInventory(ItemStack bundleStack) {
        this.bundleStack = bundleStack;
        loadFromStack();
    }

    Component getDisplayName() {
        return this.bundleStack.getHoverName();
    }

    int getPage() {
        return this.pageStart / PAGE_SIZE;
    }

    int getPageCount() {
        return Math.max(1, trimmedSize() / PAGE_SIZE + 1);
    }

    boolean canPreviousPage() {
        return this.pageStart > 0;
    }

    boolean canNextPage() {
        return getPage() + 1 < getPageCount();
    }

    void changePage(int direction) {
        int page = Math.max(0, Math.min(getPage() + direction, getPageCount() - 1));
        this.pageStart = page * PAGE_SIZE;
    }

    boolean addItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        ItemStack moved = stack.copy();
        int emptySlot = firstEmptySlotInCurrentPage();
        if (emptySlot >= 0) {
            ensureBackingSize(emptySlot);
            this.items.set(emptySlot, moved);
        } else {
            int slot = trimmedSize();
            ensureBackingSize(slot);
            this.items.set(slot, moved);
            this.pageStart = slot / PAGE_SIZE * PAGE_SIZE;
        }
        setChanged();
        return true;
    }

    @Override
    public int getContainerSize() {
        return PAGE_SIZE;
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
        int backingSlot = backingSlot(slot);
        return backingSlot >= 0 && backingSlot < this.items.size() ? this.items.get(backingSlot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        int backingSlot = backingSlot(slot);
        if (backingSlot < 0 || backingSlot >= this.items.size()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = ContainerHelper.removeItem(this.items, backingSlot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        int backingSlot = backingSlot(slot);
        if (backingSlot < 0 || backingSlot >= this.items.size()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = ContainerHelper.takeItem(this.items, backingSlot);
        if (!removed.isEmpty()) {
            saveToStack();
        }
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        int backingSlot = backingSlot(slot);
        if (backingSlot < 0) {
            return;
        }

        ensureBackingSize(backingSlot);
        this.items.set(backingSlot, stack == null ? ItemStack.EMPTY : stack);
        setChanged();
    }

    @Override
    public void setChanged() {
        trimTrailingEmptyItems();
        clampPage();
        saveToStack();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.pageStart = 0;
        setChanged();
    }

    private void loadFromStack() {
        CompoundTag tag = ItemStackNbt.get(this.bundleStack);
        if (tag == null || !NbtCompat.contains(tag, ITEMS_TAG, Tag.TAG_LIST)) {
            return;
        }

        ListTag savedItems = NbtCompat.getList(tag, ITEMS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < savedItems.size(); i++) {
            CompoundTag itemTag = NbtCompat.getCompound(savedItems, i);
            ItemStack stack = ItemStackNbt.parse(itemTag);
            if (!stack.isEmpty()) {
                this.items.add(stack);
            }
        }
    }

    private void saveToStack() {
        CompoundTag tag = ItemStackNbt.getOrCreate(this.bundleStack);
        ListTag savedItems = new ListTag();
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                savedItems.add(ItemStackNbt.save(stack));
            }
        }

        if (savedItems.isEmpty()) {
            tag.remove(ITEMS_TAG);
        } else {
            tag.put(ITEMS_TAG, savedItems);
        }

        if (tag.isEmpty()) {
            ItemStackNbt.set(this.bundleStack, null);
        }
    }

    private int backingSlot(int visibleSlot) {
        return visibleSlot < 0 || visibleSlot >= PAGE_SIZE ? -1 : this.pageStart + visibleSlot;
    }

    private int firstEmptySlotInCurrentPage() {
        for (int slot = this.pageStart; slot < this.pageStart + PAGE_SIZE; slot++) {
            if (slot >= this.items.size() || this.items.get(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private void ensureBackingSize(int slot) {
        while (this.items.size() <= slot) {
            this.items.add(ItemStack.EMPTY);
        }
    }

    private int trimmedSize() {
        int size = this.items.size();
        while (size > 0 && this.items.get(size - 1).isEmpty()) {
            size--;
        }
        return size;
    }

    private void trimTrailingEmptyItems() {
        int size = trimmedSize();
        while (this.items.size() > size) {
            this.items.remove(this.items.size() - 1);
        }
    }

    private void clampPage() {
        int page = Math.min(getPage(), getPageCount() - 1);
        this.pageStart = Math.max(0, page) * PAGE_SIZE;
    }
}

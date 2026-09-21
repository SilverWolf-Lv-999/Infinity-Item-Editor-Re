package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.data.realms.RealmController;
import io.github.seraphina.infinity_item_editor_re.data.voids.VoidController;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class BundleSourceInventory extends Inventory {
    static final int VISIBLE_SIZE = 36;
    static final int ROW_SIZE = 9;

    private final List<ItemStack> sourceStacks = new ArrayList<>();
    private Source source = Source.INVENTORY;
    private int scrollOffset;

    BundleSourceInventory(Player player) {
        super(player, new EntityEquipment());
        reloadSource();
    }

    Component getSourceName() {
        return this.source.getDisplayName();
    }

    Component getSourceButtonText() {
        return Component.translatable(key("bundle.source"), getSourceName());
    }

    void cycleSource() {
        this.source = this.source.next();
        this.scrollOffset = 0;
        reloadSource();
    }

    boolean canScrollUp() {
        return this.scrollOffset > 0;
    }

    boolean canScrollDown() {
        return this.scrollOffset < maxScrollOffset();
    }

    void scrollRows(int rows) {
        setScrollOffset(this.scrollOffset + rows * ROW_SIZE);
    }

    void setScrollFromRatio(double ratio) {
        int max = maxScrollOffset();
        if (max <= 0) {
            setScrollOffset(0);
            return;
        }
        setScrollOffset((int) Math.round(Mth.clamp(ratio, 0.0D, 1.0D) * max));
    }

    int getScrollOffset() {
        return this.scrollOffset;
    }

    int maxScrollOffset() {
        return Math.max(0, this.sourceStacks.size() - VISIBLE_SIZE);
    }

    int getSourceSize() {
        return this.sourceStacks.size();
    }

    ItemStack copyFromContainerSlot(int containerSlot) {
        int displayIndex = displayIndexFromContainerSlot(containerSlot);
        if (displayIndex < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = getItem(containerSlot);
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    @Override
    public Component getName() {
        return getSourceName();
    }

    private void reloadSource() {
        this.sourceStacks.clear();
        this.source.load(this.sourceStacks);
        setScrollOffset(this.scrollOffset);
    }

    private void setScrollOffset(int offset) {
        this.scrollOffset = Mth.clamp(offset, 0, maxScrollOffset());
        applyVisibleItems();
    }

    private void applyVisibleItems() {
        for (int displayIndex = 0; displayIndex < VISIBLE_SIZE; displayIndex++) {
            int sourceIndex = this.scrollOffset + displayIndex;
            ItemStack stack = sourceIndex < this.sourceStacks.size()
                    ? this.sourceStacks.get(sourceIndex).copy()
                    : ItemStack.EMPTY;
            super.setItem(containerSlotFromDisplayIndex(displayIndex), stack);
        }
        setChanged();
    }

    private static int containerSlotFromDisplayIndex(int displayIndex) {
        return displayIndex < 27 ? displayIndex + ROW_SIZE : displayIndex - 27;
    }

    private static int displayIndexFromContainerSlot(int containerSlot) {
        if (containerSlot >= ROW_SIZE && containerSlot < VISIBLE_SIZE) {
            return containerSlot - ROW_SIZE;
        }
        if (containerSlot >= 0 && containerSlot < ROW_SIZE) {
            return containerSlot + 27;
        }
        return -1;
    }

    private static String key(String suffix) {
        return "screen." + ModSource.MODID + "." + suffix;
    }

    private enum Source {
        INVENTORY {
            @Override
            void load(List<ItemStack> stacks) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player == null) {
                    return;
                }
                Inventory inventory = minecraft.player.getInventory();
                for (int slot = ROW_SIZE; slot < VISIBLE_SIZE; slot++) {
                    stacks.add(inventory.getItem(slot).copy());
                }
                for (int slot = 0; slot < ROW_SIZE; slot++) {
                    stacks.add(inventory.getItem(slot).copy());
                }
            }
        },
        VOID {
            @Override
            void load(List<ItemStack> stacks) {
                NonNullList<ItemStack> loaded = NonNullList.create();
                VoidController.loadVoidToList(loaded);
                copyNonEmpty(loaded, stacks);
            }
        },
        REALM {
            @Override
            void load(List<ItemStack> stacks) {
                Minecraft minecraft = Minecraft.getInstance();
                RealmController controller = ModSource.getOrCreateRealmController(minecraft.gameDirectory);
                if (controller != null) {
                    copyNonEmpty(controller.getStackList(), stacks);
                }
            }
        };

        abstract void load(List<ItemStack> stacks);

        Component getDisplayName() {
            return Component.translatable(key("bundle.source." + name().toLowerCase(Locale.ROOT)));
        }

        Source next() {
            Source[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        static void copyNonEmpty(Iterable<ItemStack> source, List<ItemStack> target) {
            for (ItemStack stack : source) {
                if (!stack.isEmpty()) {
                    target.add(stack.copy());
                }
            }
        }
    }
}

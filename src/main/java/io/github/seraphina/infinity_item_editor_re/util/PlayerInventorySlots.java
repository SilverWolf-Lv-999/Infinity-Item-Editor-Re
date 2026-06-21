package io.github.seraphina.infinity_item_editor_re.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class PlayerInventorySlots {
    public static final int HEAD_CONTAINER_SLOT = 5;
    public static final int CHEST_CONTAINER_SLOT = 6;
    public static final int LEGS_CONTAINER_SLOT = 7;
    public static final int FEET_CONTAINER_SLOT = 8;
    public static final int HOTBAR_CONTAINER_SLOT_START = 36;
    public static final int HOTBAR_CONTAINER_SLOT_END = 44;
    public static final int OFFHAND_CONTAINER_SLOT = 45;

    private PlayerInventorySlots() {
    }

    public static boolean isPlayerInventorySlot(Player player, Slot slot) {
        return player != null && slot != null && slot.container == player.getInventory();
    }

    public static int toContainerSlot(Slot slot) {
        return slot == null ? -1 : toContainerSlot(slot.getSlotIndex());
    }

    public static int toContainerSlot(int inventorySlot) {
        if (inventorySlot >= 0 && inventorySlot <= 8) {
            return HOTBAR_CONTAINER_SLOT_START + inventorySlot;
        }
        if (inventorySlot >= 9 && inventorySlot <= 35) {
            return inventorySlot;
        }
        if (inventorySlot >= 36 && inventorySlot <= 39) {
            return 8 - inventorySlot % 4;
        }
        if (inventorySlot == 40) {
            return OFFHAND_CONTAINER_SLOT;
        }
        return -1;
    }

    public static boolean setStack(Player player, int containerSlot, ItemStack stack) {
        if (player == null || containerSlot < 0) {
            return false;
        }

        Inventory inventory = player.getInventory();
        ItemStack localStack = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        if (containerSlot >= HOTBAR_CONTAINER_SLOT_START && containerSlot <= HOTBAR_CONTAINER_SLOT_END) {
            inventory.items.set(containerSlot - HOTBAR_CONTAINER_SLOT_START, localStack);
            return true;
        }
        if (containerSlot >= 9 && containerSlot <= 35) {
            inventory.items.set(containerSlot, localStack);
            return true;
        }

        switch (containerSlot) {
            case HEAD_CONTAINER_SLOT -> inventory.armor.set(3, localStack);
            case CHEST_CONTAINER_SLOT -> inventory.armor.set(2, localStack);
            case LEGS_CONTAINER_SLOT -> inventory.armor.set(1, localStack);
            case FEET_CONTAINER_SLOT -> inventory.armor.set(0, localStack);
            case OFFHAND_CONTAINER_SLOT -> inventory.offhand.set(0, localStack);
            default -> {
                return false;
            }
        }
        return true;
    }
}

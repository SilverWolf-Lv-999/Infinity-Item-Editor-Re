package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
final class ContainerItemScreen extends ContainerScreen {
    private static final int INVENTORY_CLONE_COLOR = 0x7C2C87;

    private final Screen lastScreen;
    private final Inventory copiedInventory;

    static ContainerItemScreen create(ItemEditorScreen lastScreen, Player player, ItemStack containerStack) {
        Inventory copiedInventory = copyInventory(player.getInventory());
        ContainerItemInventory containerInventory = new ContainerItemInventory(containerStack);
        ChestMenu menu = ChestMenu.threeRows(0, copiedInventory, containerInventory);
        return new ContainerItemScreen(lastScreen, menu, copiedInventory, containerInventory.getDisplayName());
    }

    private ContainerItemScreen(Screen lastScreen, ChestMenu menu, Inventory copiedInventory, Component title) {
        super(menu, copiedInventory, title);
        this.lastScreen = lastScreen;
        this.copiedInventory = copiedInventory;
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType clickType) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        if (slot != null) {
            slotId = slot.index;
        }
        if (clickType == ClickType.SWAP) {
            handleLocalSwap(slot, mouseButton);
            return;
        }

        this.menu.clicked(slotId, mouseButton, clickType, this.minecraft.player);
        this.menu.broadcastChanges();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || isInventoryKey(keyCode, scanCode)) {
            returnToLastScreen();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        Component cloneLabel = Component.translatable("screen." + ModSource.MODID + ".container.inventory_clone");
        guiGraphics.drawString(this.font, cloneLabel, (this.imageWidth - this.font.width(cloneLabel)) / 2, -10,
                INVENTORY_CLONE_COLOR, false);
    }

    @Override
    public void onClose() {
        returnToLastScreen();
    }

    private boolean isInventoryKey(int keyCode, int scanCode) {
        return this.minecraft != null
                && this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode));
    }

    private void returnToLastScreen() {
        if (this.lastScreen instanceof ItemEditorScreen editorScreen) {
            editorScreen.refreshAfterContainerEdit();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen);
        }
    }

    private void handleLocalSwap(Slot slot, int inventorySlot) {
        if (slot == null || inventorySlot < 0 || inventorySlot >= this.copiedInventory.getContainerSize()
                || this.minecraft == null || this.minecraft.player == null || !this.menu.getCarried().isEmpty()) {
            return;
        }

        Player player = this.minecraft.player;
        ItemStack inventoryStack = this.copiedInventory.getItem(inventorySlot);
        ItemStack slotStack = slot.getItem();
        if (inventoryStack.isEmpty() && slotStack.isEmpty()) {
            return;
        }

        if (inventoryStack.isEmpty()) {
            if (slot.mayPickup(player)) {
                this.copiedInventory.setItem(inventorySlot, slotStack.copy());
                slot.setByPlayer(ItemStack.EMPTY);
                slot.onTake(player, slotStack);
            }
        } else if (slotStack.isEmpty()) {
            if (slot.mayPlace(inventoryStack)) {
                ItemStack moved = splitForSlot(slot, inventoryStack);
                slot.setByPlayer(moved);
                this.copiedInventory.setItem(inventorySlot, inventoryStack.isEmpty() ? ItemStack.EMPTY : inventoryStack);
            }
        } else if (slot.mayPickup(player) && slot.mayPlace(inventoryStack)) {
            ItemStack moved = splitForSlot(slot, inventoryStack);
            slot.setByPlayer(moved);
            if (inventoryStack.isEmpty()) {
                this.copiedInventory.setItem(inventorySlot, slotStack.copy());
            } else if (!this.copiedInventory.add(slotStack.copy())) {
                this.menu.setCarried(slotStack.copy());
            }
            slot.onTake(player, slotStack);
        }

        this.menu.broadcastChanges();
    }

    private static ItemStack splitForSlot(Slot slot, ItemStack stack) {
        int max = slot.getMaxStackSize(stack);
        if (stack.getCount() > max) {
            return stack.split(max);
        }

        ItemStack moved = stack.copy();
        stack.setCount(0);
        return moved;
    }

    private static Inventory copyInventory(Inventory source) {
        Inventory copied = new Inventory(source.player);
        for (int slot = 0; slot < source.getContainerSize(); slot++) {
            copied.setItem(slot, source.getItem(slot).copy());
        }
        copied.selected = source.selected;
        return copied;
    }
}

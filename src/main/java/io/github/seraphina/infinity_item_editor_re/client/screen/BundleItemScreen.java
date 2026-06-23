package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class BundleItemScreen extends ContainerScreen {
    private static final int BUNDLE_SLOT_COUNT = BundleItemInventory.PAGE_SIZE;
    private static final int CONTROL_WIDTH = 92;
    private static final int CONTROL_HEIGHT = 20;
    private static final int SCROLL_BAR_WIDTH = 6;
    private static final int SCROLL_BAR_COLOR = 0x80222222;
    private static final int SCROLL_THUMB_COLOR = 0xC89600C8;
    private static final int PAGE_LABEL_COLOR = 0xFF9600C8;

    private final Screen lastScreen;
    private final BundleSourceInventory sourceInventory;
    private final BundleItemInventory bundleInventory;
    private InfinityEditorButton sourceButton;
    private InfinityEditorButton sourceUpButton;
    private InfinityEditorButton sourceDownButton;
    private InfinityEditorButton bundlePrevButton;
    private InfinityEditorButton bundleNextButton;
    private boolean draggingSourceScroll;

    static BundleItemScreen create(ItemEditorScreen lastScreen, Player player, ItemStack bundleStack) {
        BundleSourceInventory sourceInventory = new BundleSourceInventory(player);
        BundleItemInventory bundleInventory = new BundleItemInventory(bundleStack);
        ChestMenu menu = ChestMenu.sixRows(0, sourceInventory, bundleInventory);
        return new BundleItemScreen(lastScreen, menu, sourceInventory, bundleInventory);
    }

    private BundleItemScreen(Screen lastScreen, ChestMenu menu, BundleSourceInventory sourceInventory,
                             BundleItemInventory bundleInventory) {
        super(menu, sourceInventory, bundleInventory.getDisplayName());
        this.lastScreen = lastScreen;
        this.sourceInventory = sourceInventory;
        this.bundleInventory = bundleInventory;
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos + this.imageWidth + 4;
        int y = this.topPos;
        this.sourceButton = addRenderableWidget(new InfinityEditorButton(x, y, CONTROL_WIDTH, CONTROL_HEIGHT,
                this.sourceInventory.getSourceButtonText(), button -> {
            this.sourceInventory.cycleSource();
            this.menu.broadcastChanges();
            updateControlState();
        }));
        this.sourceUpButton = addRenderableWidget(new InfinityEditorButton(x, y + 24, 44, CONTROL_HEIGHT,
                Component.literal("^"), button -> scrollSource(-1)));
        this.sourceDownButton = addRenderableWidget(new InfinityEditorButton(x + 48, y + 24, 44, CONTROL_HEIGHT,
                Component.literal("v"), button -> scrollSource(1)));
        this.bundlePrevButton = addRenderableWidget(new InfinityEditorButton(x, y + 58, 44, CONTROL_HEIGHT,
                Component.literal("<"), button -> changeBundlePage(-1)));
        this.bundleNextButton = addRenderableWidget(new InfinityEditorButton(x + 48, y + 58, 44, CONTROL_HEIGHT,
                Component.literal(">"), button -> changeBundlePage(1)));
        updateControlState();
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType clickType) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        if (slot != null) {
            slotId = slot.index;
        }

        if (slot != null && isSourceSlot(slot)) {
            handleSourceSlotClick(slot, mouseButton, clickType);
            return;
        }

        if (clickType == ClickType.SWAP && slot != null) {
            handleBundleHotbarSwap(slot, mouseButton);
            return;
        }

        this.menu.clicked(slotId, mouseButton, clickType, this.minecraft.player);
        this.menu.broadcastChanges();
        updateControlState();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        EditorBackgrounds.render(guiGraphics, this.width, this.height);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderSourceScrollBar(guiGraphics);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false);
        guiGraphics.drawString(this.font, this.sourceInventory.getSourceName(), this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
        Component page = Component.translatable(key("bundle.page"),
                this.bundleInventory.getPage() + 1, this.bundleInventory.getPageCount());
        guiGraphics.drawString(this.font, page, this.imageWidth + 4,
                84, PAGE_LABEL_COLOR, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int direction = -(int) Math.signum(scrollY);
        if (direction == 0) {
            return true;
        }
        if (isMouseOverSourceInventory(mouseX, mouseY)) {
            scrollSource(direction);
            return true;
        }
        if (isMouseOverBundleInventory(mouseX, mouseY)) {
            changeBundlePage(direction);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && isMouseOverSourceScrollBar(event.x(), event.y()) && this.sourceInventory.maxScrollOffset() > 0) {
            this.draggingSourceScroll = true;
            updateSourceScrollFromMouse(event.y());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.draggingSourceScroll) {
            updateSourceScrollFromMouse(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.draggingSourceScroll = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return keyPressed(event.key(), event.scancode(), event.modifiers());
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || isInventoryKey(keyCode, scanCode)) {
            returnToLastScreen();
            return true;
        }
        return super.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
    }

    @Override
    public void onClose() {
        returnToLastScreen();
    }

    private void handleSourceSlotClick(Slot slot, int mouseButton, ClickType clickType) {
        ItemStack sourceStack = this.sourceInventory.copyFromContainerSlot(slot.getContainerSlot());
        if (sourceStack.isEmpty()) {
            return;
        }
        if (clickType == ClickType.QUICK_MOVE) {
            this.bundleInventory.addItem(sourceStack);
            this.menu.broadcastChanges();
            updateControlState();
            return;
        }
        if (clickType == ClickType.CLONE || clickType == ClickType.PICKUP) {
            ItemStack carried = sourceStack.copy();
            if (mouseButton == 1 && clickType == ClickType.PICKUP) {
                carried.setCount(1);
            }
            this.menu.setCarried(carried);
            this.menu.broadcastChanges();
            return;
        }
    }

    private void handleBundleHotbarSwap(Slot slot, int inventorySlot) {
        if (inventorySlot < 0 || inventorySlot >= BundleSourceInventory.VISIBLE_SIZE || !this.menu.getCarried().isEmpty()) {
            return;
        }
        ItemStack sourceStack = this.sourceInventory.copyFromContainerSlot(inventorySlot);
        if (sourceStack.isEmpty()) {
            return;
        }
        if (slot.index < BUNDLE_SLOT_COUNT) {
            slot.setByPlayer(sourceStack);
            this.menu.broadcastChanges();
            updateControlState();
        }
    }

    private void scrollSource(int direction) {
        this.sourceInventory.scrollRows(direction);
        this.menu.broadcastChanges();
        updateControlState();
    }

    private void changeBundlePage(int direction) {
        this.bundleInventory.changePage(direction);
        this.menu.broadcastChanges();
        updateControlState();
    }

    private void updateControlState() {
        if (this.sourceButton != null) {
            this.sourceButton.setMessage(this.sourceInventory.getSourceButtonText());
        }
        if (this.sourceUpButton != null) {
            this.sourceUpButton.active = this.sourceInventory.canScrollUp();
        }
        if (this.sourceDownButton != null) {
            this.sourceDownButton.active = this.sourceInventory.canScrollDown();
        }
        if (this.bundlePrevButton != null) {
            this.bundlePrevButton.active = this.bundleInventory.canPreviousPage();
        }
        if (this.bundleNextButton != null) {
            this.bundleNextButton.active = this.bundleInventory.canNextPage();
        }
    }

    private void renderSourceScrollBar(GuiGraphics guiGraphics) {
        if (this.sourceInventory.maxScrollOffset() <= 0) {
            return;
        }
        int x = sourceScrollBarX();
        int top = sourceScrollTop();
        int height = sourceScrollHeight();
        guiGraphics.fill(x, top, x + SCROLL_BAR_WIDTH, top + height, SCROLL_BAR_COLOR);

        double covered = Math.min(1.0D, BundleSourceInventory.VISIBLE_SIZE / (double) this.sourceInventory.getSourceSize());
        int thumbHeight = Math.max(10, (int) Math.round(height * covered));
        double progress = this.sourceInventory.getScrollOffset() / (double) this.sourceInventory.maxScrollOffset();
        int thumbY = top + (int) Math.round((height - thumbHeight) * progress);
        guiGraphics.fill(x + 1, thumbY, x + SCROLL_BAR_WIDTH - 1, thumbY + thumbHeight, SCROLL_THUMB_COLOR);
    }

    private boolean isMouseOverSourceInventory(double mouseX, double mouseY) {
        return isMouseInsideImage(mouseX, mouseY, 7, this.inventoryLabelY + 10, 162, 76);
    }

    private boolean isMouseOverBundleInventory(double mouseX, double mouseY) {
        return isMouseInsideImage(mouseX, mouseY, 7, 17, 162, BundleItemInventory.ROWS * 18);
    }

    private boolean isMouseOverSourceScrollBar(double mouseX, double mouseY) {
        int x = sourceScrollBarX();
        int y = sourceScrollTop();
        return mouseX >= x - 2 && mouseX < x + SCROLL_BAR_WIDTH + 2
                && mouseY >= y && mouseY < y + sourceScrollHeight();
    }

    private boolean isMouseInsideImage(double mouseX, double mouseY, int x, int y, int width, int height) {
        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;
        return localX >= x && localX < x + width && localY >= y && localY < y + height;
    }

    private void updateSourceScrollFromMouse(double mouseY) {
        double ratio = (mouseY - sourceScrollTop()) / sourceScrollHeight();
        this.sourceInventory.setScrollFromRatio(ratio);
        this.menu.broadcastChanges();
        updateControlState();
    }

    private int sourceScrollBarX() {
        return this.leftPos + this.imageWidth - 10;
    }

    private int sourceScrollTop() {
        return this.topPos + this.inventoryLabelY + 10;
    }

    private int sourceScrollHeight() {
        return 76;
    }

    private boolean isSourceSlot(Slot slot) {
        return slot.index >= BUNDLE_SLOT_COUNT;
    }

    private boolean isInventoryKey(int keyCode, int scanCode) {
        return this.minecraft != null
                && this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(new net.minecraft.client.input.KeyEvent(keyCode, scanCode, 0)));
    }

    private void returnToLastScreen() {
        if (this.lastScreen instanceof ItemEditorScreen editorScreen) {
            editorScreen.refreshAfterContainerEdit();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen);
        }
    }

    private static String key(String suffix) {
        return "screen." + ModSource.MODID + "." + suffix;
    }
}

package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
public class ItemEditorScreen extends ItemEditorScreenRendering {
    public ItemEditorScreen(ItemStack stack) {
        this(stack, -1);
    }

    public ItemEditorScreen(ItemStack stack, int targetContainerSlot) {
        this(stack, targetContainerSlot, null, -1, -1);
    }

    ItemEditorScreen(ItemStack stack, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        this(stack, -1, parentTradeScreen, parentTradeIndex, parentTradeSlot);
    }

    private ItemEditorScreen(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(stack, targetContainerSlot, parentTradeScreen, parentTradeIndex, parentTradeSlot);
        this.expandedNbtPaths.add("tag");
        this.attributeSlot = getDefaultAttributeSlot(this.previewStack);
        readMainFieldsFromStack(this.previewStack);
        syncNbtEditorValuesFromStack();
        this.colorHexValue = formatColorHex(getEditorColor());
        ensureLorePainterRows();
    }

    @Override
    protected void init() {
        captureFieldValues();
        this.midX = isSidebarUi() ? safeLeft() + contentWidth() / 2 : this.width / 2;
        this.midY = this.height / 2;

        this.tickingBoxes.clear();
        this.mainTextBoxes.clear();
        this.loreBoxes.clear();
        this.signBoxes.clear();
        this.componentEditorBoxes.clear();
        this.componentEditorLabels.clear();
        this.loreActionButtons.clear();
        this.itemIdBox = null;
        this.countBox = null;
        this.damageBox = null;
        this.nameBox = null;
        this.rawNbtBox = null;
        this.componentFilterBox = null;
        this.componentValueSearchBox = null;
        this.componentNumberBox = null;
        this.componentNbtBox = null;
        this.enchantFilterBox = null;
        this.enchantLevelBox = null;
        this.potionFilterBox = null;
        this.potionLevelBox = null;
        this.potionTimeBox = null;
        this.attributeAmountBox = null;
        this.attributeDecimalBox = null;
        this.colorHexBox = null;
        this.signCommandBox = null;
        this.bookTitleBox = null;
        this.bookAuthorBox = null;
        this.headOwnerBox = null;
        this.headUuidBox = null;
        this.headTextureBox = null;
        this.headTextureSignatureBox = null;
        this.containerSlotNbtBox = null;
        this.bannerPatternFilterBox = null;
        this.potterySherdFilterBox = null;
        this.spawnEggEntityFilterBox = null;
        this.spawnEggCustomNameBox = null;
        this.spawnEggOwnerBox = null;
        this.tradeItemNbtBox = null;
        this.tradeUsesBox = null;
        this.tradeMaxUsesBox = null;
        this.tradeXpBox = null;
        this.tradePriceMultiplierBox = null;
        this.tradeSpecialPriceBox = null;
        this.tradeDemandBox = null;
        this.attributeInfinityButton = null;
        this.attributeOperationButton = null;
        this.attributeSlotButton = null;
        this.lorePainterScaleButton = null;
        this.lorePainterAddRowButton = null;
        this.lorePainterRemoveRowButton = null;
        this.lorePainterAddColumnButton = null;
        this.lorePainterRemoveColumnButton = null;
        this.lorePainterPreviewButton = null;
        this.redSlider = null;
        this.greenSlider = null;
        this.blueSlider = null;
        this.componentRedSlider = null;
        this.componentGreenSlider = null;
        this.componentBlueSlider = null;
        this.copyLoreButton = null;

        switch (this.activePanel) {
            case ITEM -> addItemPanel();
            case NBT -> addNbtPanel();
            case COMPONENTS -> addComponentsPanel();
            case NBT_ADVANCED -> addNbtAdvancedPanel();
            case HIDE_FLAGS -> addHideFlagsPanel();
            case ENCHANTMENTS -> addEnchantmentsPanel();
            case POTION -> addPotionPanel();
            case ATTRIBUTES -> addAttributesPanel();
            case COLOR -> addColorPanel();
            case SIGN -> addSignPanel();
            case HEAD -> addHeadPanel();
            case ARMOR_STAND -> addArmorStandPanel();
            case FIREWORK -> addFireworkPanel();
            case CONTAINER -> addContainerPanel();
            case BANNER -> addBannerPanel();
            case DECORATED_POT -> addDecoratedPotPanel();
            case SPAWN_EGG -> addSpawnEggPanel();
            case TRADES -> addTradesPanel();
            case TRADE -> addTradePanel();
            case BOOK -> addBookPanel();
            case LORE -> addLorePanel();
            case LORE_PAINTER -> addLorePainterPanel();
        }

        addBottomButtons();
    }

    @Override
    public void tick() {
        for (EditBox box : this.tickingBoxes) {
        }

        if ((this.activePanel == Panel.ENCHANTMENTS || this.activePanel == Panel.POTION || this.activePanel == Panel.ATTRIBUTES)
                && Math.abs(this.mouseDist - getRingRadius()) >= RING_HOVER_WIDTH) {
            this.rotOff++;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.activePanel == Panel.NBT_ADVANCED) {
            renderEditorBackground(guiGraphics, mouseX, mouseY, partialTick);
            renderNbtAdvancedPanel(guiGraphics, mouseX, mouseY);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }

        renderEditorBackground(guiGraphics, mouseX, mouseY, partialTick);

        switch (this.activePanel) {
            case ITEM -> renderItemPanel(guiGraphics, mouseX, mouseY);
            case NBT -> renderNbtPanel(guiGraphics, mouseX, mouseY);
            case COMPONENTS -> renderComponentsPanel(guiGraphics, mouseX, mouseY);
            case HIDE_FLAGS -> renderHideFlagsPanel(guiGraphics);
            case ENCHANTMENTS -> renderEnchantmentsPanel(guiGraphics, mouseX, mouseY, partialTick);
            case POTION -> renderPotionPanel(guiGraphics, mouseX, mouseY, partialTick);
            case ATTRIBUTES -> renderAttributesPanel(guiGraphics, mouseX, mouseY, partialTick);
            case COLOR -> renderColorPanel(guiGraphics);
            case SIGN -> renderSignPanel(guiGraphics);
            case HEAD -> renderHeadPanel(guiGraphics);
            case ARMOR_STAND -> renderArmorStandPanel(guiGraphics);
            case FIREWORK -> renderFireworkPanel(guiGraphics);
            case CONTAINER -> renderContainerPanel(guiGraphics);
            case BANNER -> renderBannerPanel(guiGraphics);
            case DECORATED_POT -> renderDecoratedPotPanel(guiGraphics);
            case SPAWN_EGG -> renderSpawnEggPanel(guiGraphics);
            case TRADES -> renderTradesPanel(guiGraphics, mouseX, mouseY);
            case TRADE -> renderTradePanel(guiGraphics);
            case BOOK -> renderBookPanel(guiGraphics);
            case LORE -> renderLorePanel(guiGraphics, mouseX, mouseY);
            case LORE_PAINTER -> renderLorePainterPanel(guiGraphics, mouseX, mouseY);
            case NBT_ADVANCED -> {
            }
        }

        if (!this.status.getString().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.status, this.midX, this.height - 58, 0xFFFFD966);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderPanelTooltips(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            goBack();
            return true;
        }

        if (isInventoryKey(keyCode, scanCode)) {
            return true;
        }

        if (keyCode == 257 || keyCode == 335) {
            if (this.activePanel == Panel.ITEM) {
                if (isTradeSlotEditor()) {
                    applyTradeSlotEditorAndReturn();
                    return true;
                }
                applyToSelectedSlot();
                return true;
            }
            if (this.activePanel == Panel.NBT) {
                updateRawNbt();
                return true;
            }
            if (this.activePanel == Panel.COLOR) {
                applyColorFromHex(true);
                return true;
            }
            if (this.activePanel == Panel.CONTAINER) {
                updateContainerSlotFromNbt();
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean isInventoryKey(int keyCode, int scanCode) {
        return this.minecraft != null
                && this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(new net.minecraft.client.input.KeyEvent(keyCode, scanCode, 0)));
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.activePanel == Panel.ENCHANTMENTS && this.enchantFilterBox != null && this.enchantFilterBox.isFocused()) {
            return this.enchantFilterBox.charTyped(new CharacterEvent(Character.toLowerCase(codePoint), modifiers));
        }
        if (this.activePanel == Panel.POTION && this.potionFilterBox != null && this.potionFilterBox.isFocused()) {
            return this.potionFilterBox.charTyped(new CharacterEvent(Character.toLowerCase(codePoint), modifiers));
        }
        if (this.activePanel == Panel.BANNER && this.bannerPatternFilterBox != null && this.bannerPatternFilterBox.isFocused()) {
            return this.bannerPatternFilterBox.charTyped(new CharacterEvent(Character.toLowerCase(codePoint), modifiers));
        }
        if (this.activePanel == Panel.DECORATED_POT && this.potterySherdFilterBox != null && this.potterySherdFilterBox.isFocused()) {
            return this.potterySherdFilterBox.charTyped(new CharacterEvent(Character.toLowerCase(codePoint), modifiers));
        }
        if (this.activePanel == Panel.SPAWN_EGG && this.spawnEggEntityFilterBox != null && this.spawnEggEntityFilterBox.isFocused()) {
            return this.spawnEggEntityFilterBox.charTyped(new CharacterEvent(Character.toLowerCase(codePoint), modifiers));
        }
        if (this.activePanel == Panel.COMPONENTS && this.componentFilterBox != null && this.componentFilterBox.isFocused()) {
            return this.componentFilterBox.charTyped(new CharacterEvent(Character.toLowerCase(codePoint), modifiers));
        }
        if (this.activePanel == Panel.COMPONENTS && this.componentValueSearchBox != null && this.componentValueSearchBox.isFocused()) {
            return this.componentValueSearchBox.charTyped(new CharacterEvent(Character.toLowerCase(codePoint), modifiers));
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (handled) {
            return handled;
        }

        updateMouseDistance((int) mouseX, (int) mouseY);
        if (this.activePanel == Panel.TRADES && (button == 0 || button == 1)) {
            return handleTradesClick(mouseX, mouseY, button);
        }
        if (this.activePanel == Panel.TRADE && (button == 0 || button == 1)) {
            return handleTradeClick(mouseX, mouseY);
        }
        if (button != 0) {
            return false;
        }
        return switch (this.activePanel) {
            case ENCHANTMENTS -> handleEnchantingClick(mouseX, mouseY);
            case POTION -> handlePotionClick(mouseX, mouseY);
            case ATTRIBUTES -> handleAttributesClick(mouseX, mouseY);
            case COMPONENTS -> handleComponentListClick(mouseX, mouseY);
            case COLOR -> handleColorClick(mouseX, mouseY);
            case CONTAINER -> handleContainerClick(mouseX, mouseY);
            case BANNER -> handleBannerClick(mouseX, mouseY);
            case DECORATED_POT -> handleDecoratedPotClick(mouseX, mouseY);
            case SPAWN_EGG -> handleSpawnEggClick(mouseX, mouseY);
            case NBT_ADVANCED -> handleNbtAdvancedClick(mouseX, mouseY);
            case LORE -> handleLoreClick(mouseX, mouseY);
            case LORE_PAINTER -> handleLorePainterClick(mouseX, mouseY);
            default -> false;
        };
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.activePanel == Panel.NBT_ADVANCED) {
            int rows = buildNbtRows().size();
            int visible = getNbtAdvancedVisibleRows();
            this.advancedScroll = Mth.clamp(this.advancedScroll - (int) Math.signum(scrollY), 0, Math.max(0, rows - visible));
            return true;
        }

        if (this.activePanel == Panel.LORE) {
            setLoreScroll(this.loreScroll - (int) Math.signum(scrollY));
            return true;
        }

        if (this.activePanel == Panel.CONTAINER && isBundleEditableItem(this.previewStack)) {
            cycleContainerSlot(-(int) Math.signum(scrollY));
            return true;
        }

        if (this.activePanel == Panel.COMPONENTS) {
            return scrollComponentList(scrollY);
        }

        if (this.activePanel == Panel.BANNER) {
            setBannerPatternScroll(this.bannerPatternScroll - (int) Math.signum(scrollY));
            return true;
        }

        if (this.activePanel == Panel.DECORATED_POT) {
            setPotterySherdScroll(this.potterySherdScroll - (int) Math.signum(scrollY));
            return true;
        }

        if (this.activePanel == Panel.SPAWN_EGG) {
            if (isMouseIn(mouseX, mouseY, spawnEggEntityListX(), getSpawnEggEntityRowY(0) - 1,
                    spawnEggEntityListWidth(), SPAWN_EGG_ENTITY_ROWS * 10 + 2)) {
                setSpawnEggEntityScroll(this.spawnEggEntityScroll - (int) Math.signum(scrollY));
                rebuildWidgets();
            } else {
                setSpawnEggTagScroll(this.spawnEggTagScroll - (int) Math.signum(scrollY));
                rebuildWidgets();
            }
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.activePanel == Panel.COMPONENTS && this.draggingComponentListScroll) {
            return dragComponentListScrollbar(mouseY);
        }
        if (this.activePanel == Panel.LORE && this.draggingLoreScroll) {
            updateLoreScrollFromMouse(mouseY);
            return true;
        }
        if (this.activePanel == Panel.LORE_PAINTER && this.lorePainterDragging) {
            paintLorePainterAt(mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingComponentListScroll = false;
        this.draggingLoreScroll = false;
        this.lorePainterDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

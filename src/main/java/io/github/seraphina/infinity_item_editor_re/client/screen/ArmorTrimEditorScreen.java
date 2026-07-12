package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.util.MinecraftCompat;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.util.CompatRegistries;
import io.github.seraphina.infinity_item_editor_re.util.NbtCompat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class ArmorTrimEditorScreen extends CompatScreen {
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 6;
    private static final int SCREEN_MARGIN = 10;
    private static final int PANEL_TOP = 34;
    private static final int PANEL_PADDING = 8;
    private static final int DROPDOWN_HEIGHT = 20;
    private static final int DROPDOWN_ROW_HEIGHT = 18;
    private static final int DROPDOWN_MAX_ROWS = 7;
    private static final int TRIM_ROW_HEIGHT = 12;
    private static final int PANEL_FILL = 0xDE323232;
    private static final int PANEL_SHADOW = 0xAA000000;
    private static final int FIELD_FILL = 0xFF1F1F1F;
    private static final int FIELD_DISABLED = 0xFF262626;
    private static final int ROW_HOVER = 0x171FFFFF;
    private static final int ROW_SELECTED = 0x332880FF;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_MUTED = 0xFFAAAAAA;
    private static final int STATUS_GOOD = 0xFF32CC64;
    private static final int STATUS_BAD = 0xFFF44262;
    private static final int STATUS_NEUTRAL = 0xFFFFD966;
    private static final int PREVIEW_ARMOR_STAND = 0;
    private static final int PREVIEW_PLAYER = 1;
    private static final int PREVIEW_ZOMBIE = 2;
    private static final String ARMOR_TRIMS_CUSTOM_DATA_TAG = "InfinityItemEditorArmorTrims";
    private static final String ARMOR_TRIM_MATERIAL_TAG = "material";
    private static final String ARMOR_TRIM_PATTERN_TAG = "pattern";

    private final ItemEditorScreen lastScreen;
    private ItemStack armorStack;
    private Component status = Component.empty();
    private int statusColor = STATUS_NEUTRAL;
    private int selectedMaterialIndex;
    private int selectedPatternIndex;
    private int previewEntity = PREVIEW_ARMOR_STAND;
    private int materialDropdownScroll;
    private int patternDropdownScroll;
    private int trimListScroll;
    private int lastMouseX;
    private int lastMouseY;
    private boolean materialDropdownOpen;
    private boolean patternDropdownOpen;
    private ArmorStand armorStandPreview;
    private Zombie zombiePreview;

    ArmorTrimEditorScreen(ItemEditorScreen lastScreen, ItemStack stack) {
        super(Component.translatable(key("armortrim")));
        this.lastScreen = lastScreen;
        this.armorStack = stack.copy();
        syncSelectionFromStack();
    }

    @Override
    protected void init() {
        addControls();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            returnToLastScreen();
            return true;
        }
        if (isInventoryKey(keyCode, scanCode)) {
            returnToLastScreen();
            return true;
        }
        if (CompatScreen.hasControlDown() && keyCode == 83) {
            applySelectedTrim();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        TrimEditorLayout layout = layout();

        EditorBackgrounds.render(guiGraphics, this.width, this.height);
        guiGraphics.centeredText(this.font, this.title, this.width / 2, 10, InfinityEditorButton.MAIN_COLOR);

        renderTrimList(guiGraphics, layout, mouseX, mouseY);
        renderPreview(guiGraphics, layout);
        renderControlPanel(guiGraphics, layout, mouseX, mouseY);

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        renderOpenDropdown(guiGraphics, layout, mouseX, mouseY);

        if (!this.status.getString().isEmpty()) {
            drawCenteredClipped(guiGraphics, this.status, layout.previewCenterX(),
                    layout.bottom() - 14, layout.previewWidth() - 12, this.statusColor);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && handleDropdownClick(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int direction = -(int) Math.signum(scrollY);
        if (direction == 0) {
            return true;
        }

        TrimEditorLayout layout = layout();
        if (this.materialDropdownOpen && isMouseOverDropdown(mouseX, mouseY, layout, true, getMaterials().size())) {
            scrollMaterialDropdown(direction);
            return true;
        }
        if (this.patternDropdownOpen && isMouseOverDropdown(mouseX, mouseY, layout, false, getPatterns().size())) {
            scrollPatternDropdown(direction);
            return true;
        }
        if (isMouseIn(mouseX, mouseY, layout.leftX(), layout.leftY(), layout.leftWidth(), layout.height())) {
            scrollTrimList(direction);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        returnToLastScreen();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void addControls() {
        List<ArmorTrimMaterialEntry> materials = getMaterials();
        List<ArmorTrimPatternEntry> patterns = getPatterns();
        clampSelection(materials, patterns);
        TrimEditorLayout layout = layout();

        int x = controlX(layout);
        int buttonWidth = twoColumnButtonWidth(layout);
        int fullWidth = controlWidth(layout);
        int previewButtonY = previewButtonY(layout);
        int actionTop = actionButtonsY(layout);

        addRenderableWidget(new InfinityEditorButton(x, previewButtonY, fullWidth, BUTTON_HEIGHT,
                Component.translatable(key("armortrim.preview"), getPreviewEntityName()),
                button -> cyclePreviewEntity()));

        InfinityEditorButton apply = addRenderableWidget(new InfinityEditorButton(x, actionTop, buttonWidth, BUTTON_HEIGHT,
                Component.translatable(key("armortrim.apply")), button -> applySelectedTrim()));
        apply.active = !materials.isEmpty() && !patterns.isEmpty();

        InfinityEditorButton add = addRenderableWidget(new InfinityEditorButton(x + buttonWidth + BUTTON_GAP, actionTop,
                buttonWidth, BUTTON_HEIGHT, Component.translatable(key("armortrim.add")), button -> addSelectedTrim()));
        add.active = !materials.isEmpty() && !patterns.isEmpty();

        int secondRowY = actionTop + BUTTON_HEIGHT + BUTTON_GAP;
        InfinityEditorButton remove = addRenderableWidget(new InfinityEditorButton(x, secondRowY,
                buttonWidth, BUTTON_HEIGHT, Component.translatable(key("armortrim.remove_last")), button -> removeLastTrim()));
        remove.active = getTrimCount() > 0;

        InfinityEditorButton clear = addRenderableWidget(new InfinityEditorButton(x + buttonWidth + BUTTON_GAP, secondRowY,
                buttonWidth, BUTTON_HEIGHT, Component.translatable(key("armortrim.clear")), button -> clearTrims()));
        clear.active = getTrimCount() > 0 || this.armorStack.has(DataComponents.TRIM);

        addRenderableWidget(new InfinityEditorButton(x, secondRowY + BUTTON_HEIGHT + BUTTON_GAP,
                fullWidth, BUTTON_HEIGHT, Component.translatable(key("back")), button -> returnToLastScreen()));
    }

    private TrimEditorLayout layout() {
        int top = PANEL_TOP;
        int bottom = Math.max(top + 130, this.height - SCREEN_MARGIN);
        int usableWidth = Math.max(1, this.width - SCREEN_MARGIN * 2);
        int sideMax = Math.max(80, usableWidth / 3);
        int leftWidth = Mth.clamp(this.width / 4, Math.min(118, sideMax), Math.min(184, sideMax));
        int rightWidth = Mth.clamp(this.width / 4, Math.min(138, sideMax), Math.min(212, sideMax));
        int previewWidth = usableWidth - leftWidth - rightWidth - BUTTON_GAP * 2;
        if (previewWidth < 96) {
            int needed = 96 - previewWidth;
            int leftReduction = Math.min(needed / 2 + needed % 2, Math.max(0, leftWidth - 86));
            leftWidth -= leftReduction;
            needed -= leftReduction;
            rightWidth -= Math.min(needed, Math.max(0, rightWidth - 96));
        }

        int leftX = SCREEN_MARGIN;
        int rightX = Math.max(leftX + leftWidth + BUTTON_GAP + 1, this.width - SCREEN_MARGIN - rightWidth);
        int previewLeft = leftX + leftWidth + BUTTON_GAP;
        int previewRight = Math.max(previewLeft + 1, rightX - BUTTON_GAP);
        return new TrimEditorLayout(leftX, top, leftWidth, rightX, top, rightWidth, previewLeft, top,
                previewRight, bottom, bottom);
    }

    private int controlX(TrimEditorLayout layout) {
        return layout.rightX() + PANEL_PADDING;
    }

    private int controlWidth(TrimEditorLayout layout) {
        return Math.max(1, layout.rightWidth() - PANEL_PADDING * 2);
    }

    private int twoColumnButtonWidth(TrimEditorLayout layout) {
        return Math.max(1, (controlWidth(layout) - BUTTON_GAP) / 2);
    }

    private int materialDropdownY(TrimEditorLayout layout) {
        return layout.rightY() + 42;
    }

    private int patternDropdownY(TrimEditorLayout layout) {
        return materialDropdownY(layout) + DROPDOWN_HEIGHT + 28;
    }

    private int previewButtonY(TrimEditorLayout layout) {
        int preferredY = layout.bottom() - BUTTON_HEIGHT * 4 - BUTTON_GAP * 3 - PANEL_PADDING;
        int minY = patternDropdownY(layout) + DROPDOWN_HEIGHT + 14;
        return Math.max(minY, preferredY);
    }

    private int actionButtonsY(TrimEditorLayout layout) {
        return previewButtonY(layout) + BUTTON_HEIGHT + BUTTON_GAP;
    }

    private void renderTrimList(GuiGraphicsExtractor guiGraphics, TrimEditorLayout layout, int mouseX, int mouseY) {
        drawLegacyPanel(guiGraphics, layout.leftX(), layout.leftY(), layout.leftRight(), layout.bottom());
        int textX = layout.leftX() + PANEL_PADDING;
        int y = layout.leftY() + PANEL_PADDING;
        drawClippedString(guiGraphics, Component.translatable(key("armortrim.trims")).getString(),
                textX, y, layout.leftWidth() - PANEL_PADDING * 2, TEXT_PRIMARY);

        List<String> rows = getTrimDisplayRows();
        int listTop = y + 18;
        int listBottom = layout.bottom() - PANEL_PADDING;
        int visibleRows = Math.max(1, (listBottom - listTop) / TRIM_ROW_HEIGHT);
        this.trimListScroll = Mth.clamp(this.trimListScroll, 0, Math.max(0, rows.size() - visibleRows));

        if (rows.isEmpty()) {
            drawClippedString(guiGraphics, Component.translatable(key("armortrim.no_trims")).getString(),
                    textX, listTop, layout.leftWidth() - PANEL_PADDING * 2, TEXT_MUTED);
            return;
        }

        guiGraphics.enableScissor(layout.leftX() + 2, listTop - 2, layout.leftRight() - 2, listBottom + 2);
        int end = Math.min(rows.size(), this.trimListScroll + visibleRows);
        for (int i = this.trimListScroll; i < end; i++) {
            int rowY = listTop + (i - this.trimListScroll) * TRIM_ROW_HEIGHT;
            boolean hovered = isMouseIn(mouseX, mouseY, textX - 4, rowY - 2,
                    layout.leftWidth() - PANEL_PADDING * 2 + 8, TRIM_ROW_HEIGHT);
            if (hovered || (i == 0 && this.armorStack.has(DataComponents.TRIM))) {
                drawLegacySelection(guiGraphics, textX - 4, rowY - 2, layout.leftRight() - PANEL_PADDING + 4, rowY + 10, hovered);
            }
            int color = i == 0 && this.armorStack.has(DataComponents.TRIM) ? InfinityEditorButton.CONTRAST_COLOR : TEXT_PRIMARY;
            drawClippedString(guiGraphics, rows.get(i), textX, rowY, layout.leftWidth() - PANEL_PADDING * 2, color);
        }
        guiGraphics.disableScissor();
    }

    private void renderPreview(GuiGraphicsExtractor guiGraphics, TrimEditorLayout layout) {
        int previewTop = layout.previewTop() + 10;
        int previewBottom = Math.max(previewTop + 64, layout.previewBottom() - 48);
        int centerX = layout.previewCenterX();
        int previewWidth = layout.previewWidth();
        renderArmorTrimEntityPreview(guiGraphics, layout.previewLeft(), previewTop, layout.previewRight(), previewBottom);

        ArmorTrimMaterialEntry material = getSelectedMaterialEntry();
        ArmorTrimPatternEntry pattern = getSelectedPatternEntry();
        int infoY = Math.min(layout.bottom() - 28, previewBottom + 8);
        if (material == null || pattern == null) {
            guiGraphics.centeredText(this.font, Component.translatable(key(material == null
                    ? "armortrim.no_materials"
                    : "armortrim.no_patterns")), centerX, infoY, STATUS_BAD);
            return;
        }

        drawCenteredClipped(guiGraphics, Component.translatable(key("armortrim.selected"),
                        getPatternName(pattern, material), getMaterialName(material)),
                centerX, infoY, previewWidth - 12, InfinityEditorButton.MAIN_COLOR);
        guiGraphics.centeredText(this.font, Component.translatable(key("armortrim.count"), getTrimCount()),
                centerX, infoY + 12, InfinityEditorButton.ALT_COLOR);
    }

    private void renderControlPanel(GuiGraphicsExtractor guiGraphics, TrimEditorLayout layout, int mouseX, int mouseY) {
        drawLegacyPanel(guiGraphics, layout.rightX(), layout.rightY(), layout.rightRight(), layout.bottom());
        int x = controlX(layout);
        int width = controlWidth(layout);
        int titleY = layout.rightY() + PANEL_PADDING;
        guiGraphics.item(this.armorStack, x, titleY);
        guiGraphics.itemDecorations(this.font, this.armorStack, x, titleY);
        drawClippedString(guiGraphics, Component.translatable(key("armortrim.options")).getString(),
                x + 22, titleY + 4, Math.max(1, width - 22), TEXT_PRIMARY);

        renderDropdownField(guiGraphics, layout, true, mouseX, mouseY);
        renderDropdownField(guiGraphics, layout, false, mouseX, mouseY);
    }

    private void renderDropdownField(GuiGraphicsExtractor guiGraphics, TrimEditorLayout layout, boolean material, int mouseX, int mouseY) {
        int x = controlX(layout);
        int y = material ? materialDropdownY(layout) : patternDropdownY(layout);
        int width = controlWidth(layout);
        boolean open = material ? this.materialDropdownOpen : this.patternDropdownOpen;
        boolean active = material ? !getMaterials().isEmpty() : !getPatterns().isEmpty();
        String label = Component.translatable(key(material ? "armortrim.material_select" : "armortrim.pattern_select")).getString();
        drawClippedString(guiGraphics, label, x, y - 11, width, TEXT_MUTED);

        drawLegacyField(guiGraphics, x, y, x + width, y + DROPDOWN_HEIGHT, open, active);
        String value = material ? getSelectedMaterialDisplayText() : getSelectedPatternDisplayText();
        int textColor = active ? TEXT_PRIMARY : 0xFF6D7875;
        drawClippedString(guiGraphics, value, x + 6, y + 6, Math.max(1, width - 20), textColor);
        guiGraphics.text(this.font, open ? "^" : "v", x + width - 11, y + 6, textColor, false);

        if (isMouseIn(mouseX, mouseY, x, y, width, DROPDOWN_HEIGHT) && this.font.width(value) > width - 20) {
            guiGraphics.setTooltipForNextFrame(this.font, Component.literal(value), mouseX, mouseY);
        }
    }

    private void renderOpenDropdown(GuiGraphicsExtractor guiGraphics, TrimEditorLayout layout, int mouseX, int mouseY) {
        if (this.materialDropdownOpen) {
            renderMaterialDropdown(guiGraphics, layout, mouseX, mouseY);
        }
        if (this.patternDropdownOpen) {
            renderPatternDropdown(guiGraphics, layout, mouseX, mouseY);
        }
    }

    private void renderMaterialDropdown(GuiGraphicsExtractor guiGraphics, TrimEditorLayout layout, int mouseX, int mouseY) {
        List<ArmorTrimMaterialEntry> materials = getMaterials();
        this.materialDropdownScroll = clampDropdownScroll(this.materialDropdownScroll, materials.size());
        int listTop = dropdownListTop(layout, true, materials.size());
        renderDropdownRows(guiGraphics, mouseX, mouseY, layout, true, materials.size(), this.materialDropdownScroll,
                index -> getMaterialDisplayText(materials.get(index)), this.selectedMaterialIndex);
        renderDropdownScrollbar(guiGraphics, layout, true, materials.size(), this.materialDropdownScroll, listTop);
    }

    private void renderPatternDropdown(GuiGraphicsExtractor guiGraphics, TrimEditorLayout layout, int mouseX, int mouseY) {
        List<ArmorTrimPatternEntry> patterns = getPatterns();
        this.patternDropdownScroll = clampDropdownScroll(this.patternDropdownScroll, patterns.size());
        int listTop = dropdownListTop(layout, false, patterns.size());
        renderDropdownRows(guiGraphics, mouseX, mouseY, layout, false, patterns.size(), this.patternDropdownScroll,
                index -> getPatternDisplayText(patterns.get(index)), this.selectedPatternIndex);
        renderDropdownScrollbar(guiGraphics, layout, false, patterns.size(), this.patternDropdownScroll, listTop);
    }

    private void renderDropdownRows(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, TrimEditorLayout layout,
                                    boolean material, int size, int scroll, DropdownTextProvider textProvider, int selectedIndex) {
        if (size <= 0) {
            return;
        }

        int x = controlX(layout);
        int width = controlWidth(layout);
        int visibleRows = dropdownVisibleRows(size);
        int listTop = dropdownListTop(layout, material, size);
        int listHeight = dropdownListHeight(size);
        drawLegacyPanel(guiGraphics, x, listTop, x + width, listTop + listHeight);

        for (int row = 0; row < visibleRows; row++) {
            int index = scroll + row;
            if (index >= size) {
                break;
            }
            int rowY = listTop + 1 + row * DROPDOWN_ROW_HEIGHT;
            boolean hovered = isMouseIn(mouseX, mouseY, x + 1, rowY, width - 2, DROPDOWN_ROW_HEIGHT);
            boolean selected = index == selectedIndex;
            if (hovered || selected) {
                drawLegacySelection(guiGraphics, x + 2, rowY + 1, x + width - 2, rowY + DROPDOWN_ROW_HEIGHT - 1, selected);
            }
            String text = textProvider.get(index);
            drawClippedString(guiGraphics, text, x + 7, rowY + 5, Math.max(1, width - 16),
                    selected ? InfinityEditorButton.CONTRAST_COLOR : TEXT_PRIMARY);
            if (hovered && this.font.width(text) > width - 16) {
                guiGraphics.setTooltipForNextFrame(this.font, Component.literal(text), mouseX, mouseY);
            }
        }
    }

    private void renderDropdownScrollbar(GuiGraphicsExtractor guiGraphics, TrimEditorLayout layout, boolean material,
                                         int size, int scroll, int listTop) {
        int visibleRows = dropdownVisibleRows(size);
        if (size <= visibleRows || visibleRows <= 0) {
            return;
        }

        int x = controlX(layout) + controlWidth(layout) - 5;
        int height = dropdownListHeight(size) - 4;
        int trackTop = listTop + 2;
        int thumbHeight = Math.max(8, height * visibleRows / size);
        int maxScroll = Math.max(1, size - visibleRows);
        int thumbY = trackTop + (height - thumbHeight) * scroll / maxScroll;
        guiGraphics.fill(x, trackTop, x + 2, trackTop + height, InfinityEditorButton.ALT_COLOR);
        guiGraphics.fill(x - 1, thumbY, x + 3, thumbY + thumbHeight, InfinityEditorButton.CONTRAST_COLOR);
    }

    private boolean handleDropdownClick(double mouseX, double mouseY) {
        TrimEditorLayout layout = layout();
        if (handleDropdownFieldClick(mouseX, mouseY, layout, true) || handleDropdownFieldClick(mouseX, mouseY, layout, false)) {
            return true;
        }

        if (this.materialDropdownOpen && handleDropdownRowClick(mouseX, mouseY, layout, true)) {
            return true;
        }
        if (this.patternDropdownOpen && handleDropdownRowClick(mouseX, mouseY, layout, false)) {
            return true;
        }

        if (this.materialDropdownOpen || this.patternDropdownOpen) {
            this.materialDropdownOpen = false;
            this.patternDropdownOpen = false;
            return true;
        }
        return false;
    }

    private boolean handleDropdownFieldClick(double mouseX, double mouseY, TrimEditorLayout layout, boolean material) {
        int x = controlX(layout);
        int y = material ? materialDropdownY(layout) : patternDropdownY(layout);
        if (!isMouseIn(mouseX, mouseY, x, y, controlWidth(layout), DROPDOWN_HEIGHT)) {
            return false;
        }

        if (material) {
            if (getMaterials().isEmpty()) {
                return true;
            }
            this.materialDropdownOpen = !this.materialDropdownOpen;
            this.patternDropdownOpen = false;
            scrollMaterialDropdown(0);
        } else {
            if (getPatterns().isEmpty()) {
                return true;
            }
            this.patternDropdownOpen = !this.patternDropdownOpen;
            this.materialDropdownOpen = false;
            scrollPatternDropdown(0);
        }
        return true;
    }

    private boolean handleDropdownRowClick(double mouseX, double mouseY, TrimEditorLayout layout, boolean material) {
        int size = material ? getMaterials().size() : getPatterns().size();
        if (!isMouseOverDropdown(mouseX, mouseY, layout, material, size)) {
            return false;
        }

        int listTop = dropdownListTop(layout, material, size);
        int row = ((int) mouseY - listTop - 1) / DROPDOWN_ROW_HEIGHT;
        int index = (material ? this.materialDropdownScroll : this.patternDropdownScroll) + row;
        if (row < 0 || row >= dropdownVisibleRows(size) || index < 0 || index >= size) {
            return true;
        }

        if (material) {
            this.selectedMaterialIndex = index;
            this.materialDropdownOpen = false;
        } else {
            this.selectedPatternIndex = index;
            this.patternDropdownOpen = false;
        }
        return true;
    }

    private boolean isMouseOverDropdown(double mouseX, double mouseY, TrimEditorLayout layout, boolean material, int size) {
        if (size <= 0) {
            return false;
        }
        int x = controlX(layout);
        int listTop = dropdownListTop(layout, material, size);
        return isMouseIn(mouseX, mouseY, x, listTop, controlWidth(layout), dropdownListHeight(size));
    }

    private int dropdownVisibleRows(int size) {
        return Math.min(DROPDOWN_MAX_ROWS, Math.max(0, size));
    }

    private int dropdownListHeight(int size) {
        return dropdownVisibleRows(size) * DROPDOWN_ROW_HEIGHT + 2;
    }

    private int dropdownListTop(TrimEditorLayout layout, boolean material, int size) {
        int fieldY = material ? materialDropdownY(layout) : patternDropdownY(layout);
        int listHeight = dropdownListHeight(size);
        int below = fieldY + DROPDOWN_HEIGHT + 2;
        if (below + listHeight > layout.bottom() - PANEL_PADDING && fieldY - listHeight - 2 >= layout.rightY() + PANEL_PADDING) {
            return fieldY - listHeight - 2;
        }
        return below;
    }

    private int clampDropdownScroll(int scroll, int size) {
        int visibleRows = dropdownVisibleRows(size);
        return Mth.clamp(scroll, 0, Math.max(0, size - visibleRows));
    }

    private void scrollMaterialDropdown(int direction) {
        List<ArmorTrimMaterialEntry> materials = getMaterials();
        int visibleRows = dropdownVisibleRows(materials.size());
        int target = direction == 0 ? this.selectedMaterialIndex - visibleRows / 2 : this.materialDropdownScroll + direction;
        this.materialDropdownScroll = Mth.clamp(target, 0, Math.max(0, materials.size() - visibleRows));
    }

    private void scrollPatternDropdown(int direction) {
        List<ArmorTrimPatternEntry> patterns = getPatterns();
        int visibleRows = dropdownVisibleRows(patterns.size());
        int target = direction == 0 ? this.selectedPatternIndex - visibleRows / 2 : this.patternDropdownScroll + direction;
        this.patternDropdownScroll = Mth.clamp(target, 0, Math.max(0, patterns.size() - visibleRows));
    }

    private void scrollTrimList(int direction) {
        TrimEditorLayout layout = layout();
        List<String> rows = getTrimDisplayRows();
        int listTop = layout.leftY() + PANEL_PADDING + 18;
        int listBottom = layout.bottom() - PANEL_PADDING;
        int visibleRows = Math.max(1, (listBottom - listTop) / TRIM_ROW_HEIGHT);
        this.trimListScroll = Mth.clamp(this.trimListScroll + direction, 0, Math.max(0, rows.size() - visibleRows));
    }

    private boolean isInventoryKey(int keyCode, int scanCode) {
        return this.minecraft != null
                && this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(new net.minecraft.client.input.KeyEvent(keyCode, scanCode, 0)));
    }

    private void returnToLastScreen() {
        if (this.minecraft != null) {
            MinecraftCompat.setScreen(this.minecraft, this.lastScreen);
        }
    }

    private void applySelectedTrim() {
        if (!isArmorTrimApplicable(this.armorStack)) {
            return;
        }
        if (!setSelectedTrimComponent()) {
            this.status = Component.translatable(key("armortrim.no_patterns"));
            this.statusColor = STATUS_BAD;
            return;
        }

        commitStack(Component.translatable(messageKey("editor_armor_trim_updated"),
                getSelectedPatternName(), getSelectedMaterialName()), STATUS_GOOD);
    }

    private void addSelectedTrim() {
        if (!isArmorTrimApplicable(this.armorStack)) {
            return;
        }

        ArmorTrimMaterialEntry material = getSelectedMaterialEntry();
        ArmorTrimPatternEntry pattern = getSelectedPatternEntry();
        if (material == null || pattern == null) {
            this.status = Component.translatable(key(material == null ? "armortrim.no_materials" : "armortrim.no_patterns"));
            this.statusColor = STATUS_BAD;
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, this.armorStack, tag -> {
            ListTag trims = NbtCompat.contains(tag, ARMOR_TRIMS_CUSTOM_DATA_TAG, Tag.TAG_LIST)
                    ? NbtCompat.getList(tag, ARMOR_TRIMS_CUSTOM_DATA_TAG, Tag.TAG_COMPOUND).copy()
                    : new ListTag();
            CompoundTag entry = new CompoundTag();
            entry.putString(ARMOR_TRIM_MATERIAL_TAG, material.id().toString());
            entry.putString(ARMOR_TRIM_PATTERN_TAG, pattern.id().toString());
            trims.add(entry);
            tag.put(ARMOR_TRIMS_CUSTOM_DATA_TAG, trims);
        });
        setSelectedTrimComponent();
        commitStack(Component.translatable(messageKey("editor_armor_trim_added"),
                getPatternName(pattern, material), getMaterialName(material)), STATUS_GOOD);
    }

    private void removeLastTrim() {
        if (getTrimCount() <= 0) {
            return;
        }

        final ArmorTrimEntry[] activeEntry = new ArmorTrimEntry[1];
        CustomData.update(DataComponents.CUSTOM_DATA, this.armorStack, tag -> {
            if (!NbtCompat.contains(tag, ARMOR_TRIMS_CUSTOM_DATA_TAG, Tag.TAG_LIST)) {
                return;
            }

            ListTag trims = NbtCompat.getList(tag, ARMOR_TRIMS_CUSTOM_DATA_TAG, Tag.TAG_COMPOUND).copy();
            if (trims.isEmpty()) {
                tag.remove(ARMOR_TRIMS_CUSTOM_DATA_TAG);
                return;
            }

            trims.remove(trims.size() - 1);
            if (trims.isEmpty()) {
                tag.remove(ARMOR_TRIMS_CUSTOM_DATA_TAG);
                return;
            }

            tag.put(ARMOR_TRIMS_CUSTOM_DATA_TAG, trims);
            CompoundTag last = NbtCompat.getCompound(trims, trims.size() - 1);
            Identifier materialId = Identifier.tryParse(NbtCompat.getString(last, ARMOR_TRIM_MATERIAL_TAG));
            Identifier patternId = Identifier.tryParse(NbtCompat.getString(last, ARMOR_TRIM_PATTERN_TAG));
            if (materialId != null && patternId != null) {
                activeEntry[0] = new ArmorTrimEntry(materialId, patternId);
            }
        });

        if (activeEntry[0] == null) {
            this.armorStack.remove(DataComponents.TRIM);
        } else {
            applyTrimEntry(activeEntry[0]);
        }
        syncSelectionFromStack();
        commitStack(Component.translatable(messageKey("editor_armor_trim_removed")), STATUS_GOOD);
    }

    private void clearTrims() {
        CustomData.update(DataComponents.CUSTOM_DATA, this.armorStack, tag -> tag.remove(ARMOR_TRIMS_CUSTOM_DATA_TAG));
        this.armorStack.remove(DataComponents.TRIM);
        syncSelectionFromStack();
        commitStack(Component.translatable(messageKey("editor_armor_trim_cleared")), STATUS_GOOD);
    }

    private void cycleMaterial(int direction) {
        List<ArmorTrimMaterialEntry> materials = getMaterials();
        if (materials.isEmpty()) {
            return;
        }

        this.selectedMaterialIndex = Mth.positiveModulo(this.selectedMaterialIndex + direction, materials.size());
        rebuildWidgets();
    }

    private void cyclePattern(int direction) {
        List<ArmorTrimPatternEntry> patterns = getPatterns();
        if (patterns.isEmpty()) {
            return;
        }

        this.selectedPatternIndex = Mth.positiveModulo(this.selectedPatternIndex + direction, patterns.size());
        rebuildWidgets();
    }

    private void cyclePreviewEntity() {
        this.previewEntity = Mth.positiveModulo(this.previewEntity + 1, 3);
        this.status = Component.translatable(messageKey("editor_armor_trim_preview_updated"), getPreviewEntityName());
        this.statusColor = STATUS_NEUTRAL;
        rebuildWidgets();
    }

    private void commitStack(Component status, int statusColor) {
        this.lastScreen.applyArmorTrimEditedStack(this.armorStack);
        this.armorStack = this.lastScreen.previewStack.copy();
        this.status = status;
        this.statusColor = statusColor;
        syncSelectionFromStack();
        rebuildWidgets();
    }

    private void syncSelectionFromStack() {
        List<ArmorTrimMaterialEntry> materials = getMaterials();
        List<ArmorTrimPatternEntry> patterns = getPatterns();
        if (materials.isEmpty() || patterns.isEmpty()) {
            this.selectedMaterialIndex = 0;
            this.selectedPatternIndex = 0;
            return;
        }

        Identifier materialId = null;
        Identifier patternId = null;
        ArmorTrim trim = this.armorStack.get(DataComponents.TRIM);
        if (trim != null) {
            materialId = CompatRegistries.TRIM_MATERIALS.getKey(trim.material().value());
            patternId = CompatRegistries.TRIM_PATTERNS.getKey(trim.pattern().value());
        }
        if (materialId == null || patternId == null) {
            ArmorTrimEntry entry = getLastTrimEntry();
            if (entry != null) {
                materialId = entry.materialId();
                patternId = entry.patternId();
            }
        }

        this.selectedMaterialIndex = findMaterialIndex(materials, materialId);
        this.selectedPatternIndex = findPatternIndex(patterns, patternId);
        clampSelection(materials, patterns);
    }

    private List<ArmorTrimMaterialEntry> getMaterials() {
        List<ArmorTrimMaterialEntry> entries = new ArrayList<>();
        for (var holder : CompatRegistries.TRIM_MATERIALS.getHolders()) {
            Identifier id = CompatRegistries.TRIM_MATERIALS.getKey(holder.value());
            if (id != null) {
                entries.add(new ArmorTrimMaterialEntry(id, holder));
            }
        }
        entries.sort(Comparator.comparing(entry -> entry.id().toString()));
        return entries;
    }

    private List<ArmorTrimPatternEntry> getPatterns() {
        List<ArmorTrimPatternEntry> entries = new ArrayList<>();
        for (var holder : CompatRegistries.TRIM_PATTERNS.getHolders()) {
            Identifier id = CompatRegistries.TRIM_PATTERNS.getKey(holder.value());
            if (id != null) {
                entries.add(new ArmorTrimPatternEntry(id, holder));
            }
        }
        entries.sort(Comparator.comparing(entry -> entry.id().toString()));
        return entries;
    }

    private ArmorTrimMaterialEntry getSelectedMaterialEntry() {
        List<ArmorTrimMaterialEntry> materials = getMaterials();
        clampSelection(materials, getPatterns());
        return materials.isEmpty() ? null : materials.get(this.selectedMaterialIndex);
    }

    private ArmorTrimPatternEntry getSelectedPatternEntry() {
        List<ArmorTrimPatternEntry> patterns = getPatterns();
        clampSelection(getMaterials(), patterns);
        return patterns.isEmpty() ? null : patterns.get(this.selectedPatternIndex);
    }

    private Holder<TrimMaterial> getSelectedMaterialHolder() {
        ArmorTrimMaterialEntry entry = getSelectedMaterialEntry();
        return entry == null ? null : entry.material();
    }

    private Holder<TrimPattern> getSelectedPatternHolder() {
        ArmorTrimPatternEntry entry = getSelectedPatternEntry();
        return entry == null ? null : entry.pattern();
    }

    private void clampSelection(List<ArmorTrimMaterialEntry> materials, List<ArmorTrimPatternEntry> patterns) {
        this.selectedMaterialIndex = materials.isEmpty()
                ? 0
                : Mth.clamp(this.selectedMaterialIndex, 0, materials.size() - 1);
        this.selectedPatternIndex = patterns.isEmpty()
                ? 0
                : Mth.clamp(this.selectedPatternIndex, 0, patterns.size() - 1);
    }

    private int findMaterialIndex(List<ArmorTrimMaterialEntry> materials, Identifier id) {
        if (id == null) {
            return 0;
        }
        for (int i = 0; i < materials.size(); i++) {
            if (id.equals(materials.get(i).id())) {
                return i;
            }
        }
        return 0;
    }

    private int findPatternIndex(List<ArmorTrimPatternEntry> patterns, Identifier id) {
        if (id == null) {
            return 0;
        }
        for (int i = 0; i < patterns.size(); i++) {
            if (id.equals(patterns.get(i).id())) {
                return i;
            }
        }
        return 0;
    }

    private Component getMaterialName(ArmorTrimMaterialEntry entry) {
        return entry == null ? Component.translatable(key("armortrim.no_materials")) : entry.material().value().description();
    }

    private Component getPatternName(ArmorTrimPatternEntry entry, ArmorTrimMaterialEntry material) {
        if (entry == null) {
            return Component.translatable(key("armortrim.no_patterns"));
        }
        if (material != null) {
            return entry.pattern().value().copyWithStyle(material.material());
        }
        return entry.pattern().value().description();
    }

    private Component getSelectedMaterialName() {
        return getMaterialName(getSelectedMaterialEntry());
    }

    private Component getSelectedPatternName() {
        return getPatternName(getSelectedPatternEntry(), getSelectedMaterialEntry());
    }

    private Component getPreviewEntityName() {
        return switch (this.previewEntity) {
            case PREVIEW_PLAYER -> Component.translatable(key("armortrim.preview.player"));
            case PREVIEW_ZOMBIE -> Component.translatable(key("armortrim.preview.zombie"));
            default -> Component.translatable(key("armortrim.preview.armor_stand"));
        };
    }

    private List<ArmorTrimEntry> getTrimEntries() {
        CustomData data = this.armorStack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return List.of();
        }

        CompoundTag tag = data.copyTag();
        if (!NbtCompat.contains(tag, ARMOR_TRIMS_CUSTOM_DATA_TAG, Tag.TAG_LIST)) {
            return List.of();
        }

        List<ArmorTrimEntry> entries = new ArrayList<>();
        ListTag list = NbtCompat.getList(tag, ARMOR_TRIMS_CUSTOM_DATA_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = NbtCompat.getCompound(list, i);
            Identifier materialId = Identifier.tryParse(NbtCompat.getString(entryTag, ARMOR_TRIM_MATERIAL_TAG));
            Identifier patternId = Identifier.tryParse(NbtCompat.getString(entryTag, ARMOR_TRIM_PATTERN_TAG));
            if (materialId != null && patternId != null) {
                entries.add(new ArmorTrimEntry(materialId, patternId));
            }
        }
        return entries;
    }

    private ArmorTrimEntry getLastTrimEntry() {
        List<ArmorTrimEntry> entries = getTrimEntries();
        return entries.isEmpty() ? null : entries.get(entries.size() - 1);
    }

    private int getTrimCount() {
        return getTrimEntries().size();
    }

    private boolean setSelectedTrimComponent() {
        Holder<TrimMaterial> material = getSelectedMaterialHolder();
        Holder<TrimPattern> pattern = getSelectedPatternHolder();
        if (material == null || pattern == null) {
            return false;
        }

        this.armorStack.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
        return true;
    }

    private boolean applyTrimEntry(ArmorTrimEntry entry) {
        Holder<TrimMaterial> material = CompatRegistries.TRIM_MATERIALS.getHolder(entry.materialId());
        Holder<TrimPattern> pattern = CompatRegistries.TRIM_PATTERNS.getHolder(entry.patternId());
        if (material == null || pattern == null) {
            return false;
        }

        this.armorStack.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
        return true;
    }

    private void renderArmorTrimEntityPreview(GuiGraphicsExtractor guiGraphics, int left, int top, int right, int bottom) {
        EquipmentSlot slot = getArmorTrimEquipmentSlot(this.armorStack);
        if (slot == null || this.minecraft == null || this.minecraft.level == null) {
            return;
        }

        ItemStack trimmed = this.armorStack.copyWithCount(1);
        Holder<TrimMaterial> material = getSelectedMaterialHolder();
        Holder<TrimPattern> pattern = getSelectedPatternHolder();
        if (material != null && pattern != null) {
            trimmed.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
        }

        if (this.previewEntity == PREVIEW_PLAYER) {
            renderPlayerPreview(guiGraphics, left, top, right, bottom, slot, trimmed);
            return;
        }

        LivingEntity entity = getPreviewEntity();
        if (entity == null) {
            return;
        }

        setPreviewEquipment(entity, slot, trimmed);
        renderLivingEntity(guiGraphics, left, top, right, bottom, entity);
    }

    private void renderPlayerPreview(GuiGraphicsExtractor guiGraphics, int left, int top, int right, int bottom,
                                     EquipmentSlot slot, ItemStack trimmed) {
        if (this.minecraft.player == null) {
            return;
        }

        List<ItemStack> previousEquipment = new ArrayList<>();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            previousEquipment.add(this.minecraft.player.getItemBySlot(equipmentSlot));
        }

        try {
            setPreviewEquipment(this.minecraft.player, slot, trimmed);
            renderLivingEntity(guiGraphics, left, top, right, bottom, this.minecraft.player);
        } finally {
            for (int i = 0; i < EquipmentSlot.VALUES.size(); i++) {
                this.minecraft.player.setItemSlot(EquipmentSlot.VALUES.get(i), previousEquipment.get(i));
            }
        }
    }

    private LivingEntity getPreviewEntity() {
        if (this.minecraft == null || this.minecraft.level == null) {
            return null;
        }

        if (this.previewEntity == PREVIEW_ZOMBIE) {
            if (this.zombiePreview == null || this.zombiePreview.level() != this.minecraft.level) {
                this.zombiePreview = EntityTypes.ZOMBIE.create(this.minecraft.level, EntitySpawnReason.LOAD);
            }
            return this.zombiePreview;
        }

        if (this.armorStandPreview == null || this.armorStandPreview.level() != this.minecraft.level) {
            ArmorStand armorStand = new ArmorStand(this.minecraft.level, 0.0D, 0.0D, 0.0D);
            armorStand.setShowArms(true);
            armorStand.setNoBasePlate(true);
            armorStand.setInvisible(false);
            this.armorStandPreview = armorStand;
        }
        return this.armorStandPreview;
    }

    private void setPreviewEquipment(LivingEntity entity, EquipmentSlot slot, ItemStack trimmed) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            entity.setItemSlot(equipmentSlot, ItemStack.EMPTY);
        }
        entity.setItemSlot(slot, trimmed);
    }

    private void renderLivingEntity(GuiGraphicsExtractor guiGraphics, int left, int top, int right, int bottom, LivingEntity entity) {
        int height = Math.max(1, bottom - top);
        int scale = this.previewEntity == PREVIEW_ARMOR_STAND
                ? Mth.clamp(height / 3, 24, 38)
                : Mth.clamp(height / 2, 34, 48);
        float yOffset = this.previewEntity == PREVIEW_ARMOR_STAND ? 0.05F : 0.0F;
        InventoryScreen.extractEntityInInventoryFollowsMouse(guiGraphics, left, top, right, bottom,
                scale, yOffset, this.lastMouseX, this.lastMouseY, entity);
    }

    private static boolean isArmorTrimApplicable(ItemStack stack) {
        return getArmorTrimEquipmentSlot(stack) != null;
    }

    private static EquipmentSlot getArmorTrimEquipmentSlot(ItemStack stack) {
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || !isArmorTrimSlot(equippable.slot())) {
            return null;
        }
        return equippable.slot();
    }

    private static boolean isArmorTrimSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD
                || slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.LEGS
                || slot == EquipmentSlot.FEET;
    }

    private void drawLegacyPanel(GuiGraphicsExtractor guiGraphics, int left, int top, int right, int bottom) {
        guiGraphics.fill(left + 2, top + 2, right + 2, bottom + 2, PANEL_SHADOW);
        guiGraphics.fill(left, top, right, bottom, PANEL_FILL);
        guiGraphics.fill(left, top, right, top + 1, InfinityEditorButton.MAIN_COLOR);
        guiGraphics.fill(left, top, left + 1, bottom, InfinityEditorButton.MAIN_COLOR);
        guiGraphics.fill(right - 1, top, right, bottom, InfinityEditorButton.ALT_COLOR);
        guiGraphics.fill(left, bottom - 1, right, bottom, InfinityEditorButton.ALT_COLOR);
    }

    private void drawLegacyField(GuiGraphicsExtractor guiGraphics, int left, int top, int right, int bottom,
                                 boolean focused, boolean active) {
        int border = focused && active ? InfinityEditorButton.CONTRAST_COLOR : InfinityEditorButton.MAIN_COLOR;
        guiGraphics.fill(left, top, right, bottom, border);
        guiGraphics.fill(left + 1, top + 1, right - 1, bottom - 1, active ? FIELD_FILL : FIELD_DISABLED);
    }

    private void drawLegacySelection(GuiGraphicsExtractor guiGraphics, int left, int top, int right, int bottom, boolean selected) {
        guiGraphics.fill(left, top, right, bottom, selected ? ROW_SELECTED : ROW_HOVER);
    }
    private boolean isMouseIn(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void drawClippedString(GuiGraphicsExtractor guiGraphics, String text, int x, int y, int maxWidth, int color) {
        String value = text;
        if (this.font.width(value) > maxWidth) {
            String ellipsis = "...";
            value = this.font.plainSubstrByWidth(value, Math.max(0, maxWidth - this.font.width(ellipsis))) + ellipsis;
        }
        guiGraphics.text(this.font, value, x, y, color, false);
    }

    private List<String> getTrimDisplayRows() {
        List<String> rows = new ArrayList<>();
        ArmorTrim current = this.armorStack.get(DataComponents.TRIM);
        if (current != null) {
            Identifier materialId = CompatRegistries.TRIM_MATERIALS.getKey(current.material().value());
            Identifier patternId = CompatRegistries.TRIM_PATTERNS.getKey(current.pattern().value());
            rows.add(Component.translatable(key("armortrim.current"),
                    getTrimPatternDisplayText(patternId, materialId), getTrimMaterialDisplayText(materialId)).getString());
        }

        List<ArmorTrimEntry> entries = getTrimEntries();
        for (int i = 0; i < entries.size(); i++) {
            ArmorTrimEntry entry = entries.get(i);
            rows.add(Component.translatable(key("armortrim.appended"), i + 1,
                    getTrimPatternDisplayText(entry.patternId(), entry.materialId()), getTrimMaterialDisplayText(entry.materialId())).getString());
        }
        return rows;
    }

    private String getSelectedMaterialDisplayText() {
        ArmorTrimMaterialEntry entry = getSelectedMaterialEntry();
        return entry == null
                ? Component.translatable(key("armortrim.no_materials")).getString()
                : getMaterialDisplayText(entry);
    }

    private String getSelectedPatternDisplayText() {
        ArmorTrimPatternEntry entry = getSelectedPatternEntry();
        return entry == null
                ? Component.translatable(key("armortrim.no_patterns")).getString()
                : getPatternDisplayText(entry);
    }

    private String getMaterialDisplayText(ArmorTrimMaterialEntry entry) {
        return getMaterialName(entry).getString() + " (" + entry.id() + ")";
    }

    private String getPatternDisplayText(ArmorTrimPatternEntry entry) {
        return getPatternName(entry, getSelectedMaterialEntry()).getString() + " (" + entry.id() + ")";
    }


    private String getTrimMaterialDisplayText(Identifier id) {
        ArmorTrimMaterialEntry entry = findMaterialEntry(id);
        return entry == null ? String.valueOf(id) : getMaterialName(entry).getString();
    }

    private String getTrimPatternDisplayText(Identifier patternId, Identifier materialId) {
        ArmorTrimPatternEntry entry = findPatternEntry(patternId);
        ArmorTrimMaterialEntry material = findMaterialEntry(materialId);
        return entry == null ? String.valueOf(patternId) : getPatternName(entry, material).getString();
    }

    private ArmorTrimMaterialEntry findMaterialEntry(Identifier id) {
        if (id == null) {
            return null;
        }
        for (ArmorTrimMaterialEntry entry : getMaterials()) {
            if (id.equals(entry.id())) {
                return entry;
            }
        }
        return null;
    }

    private ArmorTrimPatternEntry findPatternEntry(Identifier id) {
        if (id == null) {
            return null;
        }
        for (ArmorTrimPatternEntry entry : getPatterns()) {
            if (id.equals(entry.id())) {
                return entry;
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface DropdownTextProvider {
        String get(int index);
    }

    private record TrimEditorLayout(int leftX, int leftY, int leftWidth, int rightX, int rightY, int rightWidth,
                                    int previewLeft, int previewTop, int previewRight, int previewBottom, int bottom) {
        int leftRight() {
            return this.leftX + this.leftWidth;
        }

        int rightRight() {
            return this.rightX + this.rightWidth;
        }

        int height() {
            return this.bottom - this.leftY;
        }

        int previewWidth() {
            return this.previewRight - this.previewLeft;
        }

        int previewCenterX() {
            return this.previewLeft + previewWidth() / 2;
        }
    }

    private void drawCenteredClipped(GuiGraphicsExtractor guiGraphics, Component text, int centerX, int y, int maxWidth, int color) {
        String value = text.getString();
        if (this.font.width(value) > maxWidth) {
            String ellipsis = "...";
            value = this.font.plainSubstrByWidth(value, Math.max(0, maxWidth - this.font.width(ellipsis))) + ellipsis;
        }
        guiGraphics.centeredText(this.font, value, centerX, y, color);
    }

    private static String key(String suffix) {
        return "screen." + ModSource.MODID + "." + suffix;
    }

    private static String messageKey(String suffix) {
        return "message." + ModSource.MODID + "." + suffix;
    }
}

package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.seraphina.infinity_item_editor_re.client.screen.modern.ModernUi;
import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

abstract class ItemEditorScreenComponents extends ItemEditorScreenActions {
    private static final int COMPONENT_ROW_HEIGHT = 12;
    private static final int COMPONENT_PANEL_GAP = 10;

    protected ItemEditorScreenComponents(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(stack, targetContainerSlot, parentTradeScreen, parentTradeIndex, parentTradeSlot);
    }

    protected void addComponentEditorPanel() {
        int left = componentPanelLeft();
        int width = componentPanelWidth();
        int listWidth = componentListWidth(width);
        int valueLeft = left + listWidth + COMPONENT_PANEL_GAP;
        int valueWidth = Math.max(80, width - listWidth - COMPONENT_PANEL_GAP);
        List<String> componentKeys = getAvailableComponentKeys();
        ensureSelectedComponent(componentKeys);

        this.componentFilterBox = addTrackedBox(legacyTextBox(left, componentFilterY(), listWidth, FIELD_HEIGHT,
                Component.translatable(key("components.search"))));
        this.componentFilterBox.setMaxLength(128);
        this.componentFilterBox.setTextColor(componentInputTextColor());
        this.componentFilterBox.setValue(this.componentFilterValue == null ? "" : this.componentFilterValue);
        this.componentFilterBox.setResponder(value -> {
            this.componentFilterValue = value == null ? "" : value.toLowerCase(Locale.ROOT);
            clampComponentListScroll();
        });

        this.componentNbtBox = addTrackedBox(legacyTextBox(valueLeft, componentValueBoxY(), valueWidth, FIELD_HEIGHT,
                Component.translatable(key("components.value"))));
        this.componentNbtBox.setMaxLength(30000);
        this.componentNbtBox.setTextColor(componentInputTextColor());
        this.componentNbtBox.active = this.selectedComponentKey != null && !this.selectedComponentKey.isBlank();
        this.syncingComponentValue = true;
        this.componentNbtBox.setValue(selectedComponentValue());
        this.syncingComponentValue = false;
        this.componentNbtBox.setResponder(value -> {
            this.componentNbtValue = value;
            applySelectedComponentValue(value);
        });
    }

    protected void renderComponentEditorPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int left = componentPanelLeft();
        int top = componentPanelTop();
        int width = componentPanelWidth();
        int bottom = componentPanelBottom();
        int listWidth = componentListWidth(width);
        int valueLeft = left + listWidth + COMPONENT_PANEL_GAP;
        int valueWidth = Math.max(80, width - listWidth - COMPONENT_PANEL_GAP);
        int valueRight = valueLeft + valueWidth;

        if (isSidebarUi()) {
            ModernUi.fillToolDrawer(guiGraphics, left - 8, top, left + width + 8, bottom, false);
            ModernUi.fillToolDrawer(guiGraphics, valueLeft - 6, top + 18, valueRight + 6, componentValueBoxY() + FIELD_HEIGHT + 34, false);
        } else {
            guiGraphics.fill(left - 8, top, left + width + 8, bottom, 0xB8202020);
            guiGraphics.fill(left - 8, top, left + width + 8, top + 1, MAIN_COLOR);
            guiGraphics.fill(left - 8, bottom - 1, left + width + 8, bottom, MAIN_COLOR);
            guiGraphics.fill(left - 8, top, left - 7, bottom, MAIN_COLOR);
            guiGraphics.fill(left + width + 7, top, left + width + 8, bottom, MAIN_COLOR);
        }

        int labelColor = componentLabelColor();
        guiGraphics.drawString(this.font, Component.translatable(key("components.search")), left, componentFilterY() - 10, labelColor, false);
        guiGraphics.drawString(this.font, Component.translatable(key("components.available")), left, componentListY() - 10, labelColor, false);
        guiGraphics.drawString(this.font, Component.translatable(key("components.value")), valueLeft, componentValueBoxY() - 10, labelColor, false);

        renderComponentList(guiGraphics, mouseX, mouseY, left, listWidth);
        renderSelectedComponentSummary(guiGraphics, valueLeft, valueRight, componentValueBoxY() + FIELD_HEIGHT + 10);

        if (!this.nbtFeedback.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.nbtFeedback, this.midX, this.height - 44,
                    this.nbtFeedbackGood ? GOOD_GREEN : BAD_RED);
        }
    }

    protected boolean handleComponentListClick(double mouseX, double mouseY) {
        int left = componentPanelLeft();
        int width = componentListWidth(componentPanelWidth());
        int top = componentListY();
        int height = componentListHeight();
        if (!isMouseIn(mouseX, mouseY, left, top, width, height)) {
            return false;
        }

        List<String> componentKeys = getAvailableComponentKeys();
        int row = ((int) mouseY - top) / COMPONENT_ROW_HEIGHT;
        int index = this.componentListScroll + row;
        if (row < 0 || row >= componentVisibleRows() || index < 0 || index >= componentKeys.size()) {
            return false;
        }

        this.selectedComponentKey = componentKeys.get(index);
        this.componentNbtValue = selectedComponentValue();
        if (this.componentNbtBox != null) {
            this.syncingComponentValue = true;
            this.componentNbtBox.active = true;
            this.componentNbtBox.setValue(this.componentNbtValue);
            this.componentNbtBox.setCursorPosition(0);
            this.syncingComponentValue = false;
        }
        return true;
    }

    protected boolean scrollComponentList(double scrollY) {
        int previous = this.componentListScroll;
        this.componentListScroll = Mth.clamp(this.componentListScroll - (int) Math.signum(scrollY), 0, maxComponentListScroll());
        return previous != this.componentListScroll || maxComponentListScroll() > 0;
    }

    private void renderComponentList(GuiGraphics guiGraphics, int mouseX, int mouseY, int left, int width) {
        List<String> componentKeys = getAvailableComponentKeys();
        clampComponentListScroll();

        int top = componentListY();
        int height = componentListHeight();
        int bottom = top + height;
        int fill = isSidebarUi() ? ModernUi.SURFACE_SOFT : 0x80323232;
        int border = isSidebarUi() ? ModernUi.BORDER : MAIN_COLOR;
        if (isSidebarUi()) {
            ModernUi.fillInset(guiGraphics, left, top, left + width, bottom, 5, false, true);
        } else {
            guiGraphics.fill(left, top, left + width, bottom, fill);
            guiGraphics.fill(left, top, left + width, top + 1, border);
            guiGraphics.fill(left, bottom - 1, left + width, bottom, border);
            guiGraphics.fill(left, top, left + 1, bottom, border);
            guiGraphics.fill(left + width - 1, top, left + width, bottom, border);
        }

        if (componentKeys.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable(key("components.no_match")),
                    left + 6, top + 6, componentSecondaryTextColor(), false);
            return;
        }

        CompoundTag components = currentComponents();
        int visibleRows = componentVisibleRows();
        int end = Math.min(componentKeys.size(), this.componentListScroll + visibleRows);
        for (int i = this.componentListScroll; i < end; i++) {
            String componentKey = componentKeys.get(i);
            int y = top + (i - this.componentListScroll) * COMPONENT_ROW_HEIGHT;
            boolean selected = componentKey.equals(this.selectedComponentKey);
            boolean hovered = isMouseIn(mouseX, mouseY, left, y, width, COMPONENT_ROW_HEIGHT);
            if (isSidebarUi()) {
                if (selected || hovered) {
                    ModernUi.fillSelection(guiGraphics, left + 2, y + 1, left + width - 2, y + COMPONENT_ROW_HEIGHT - 1, 4, selected);
                }
            } else if (selected || hovered) {
                guiGraphics.fill(left + 1, y, left + width - 1, y + COMPONENT_ROW_HEIGHT, selected ? 0x8032CC64 : 0x55323232);
            }

            int color = selected ? componentSelectedTextColor() : componentPrimaryTextColor();
            String marker = components.contains(componentKey) ? "* " : "  ";
            String display = marker + this.font.plainSubstrByWidth(componentKey, Math.max(20, width - 16));
            guiGraphics.drawString(this.font, display, left + 5, y + 2, color, false);
        }
    }

    private void renderSelectedComponentSummary(GuiGraphics guiGraphics, int left, int right, int y) {
        if (this.selectedComponentKey == null || this.selectedComponentKey.isBlank()) {
            return;
        }

        CompoundTag components = currentComponents();
        boolean present = components.contains(this.selectedComponentKey);
        Component status = Component.translatable(key(present ? "components.present" : "components.not_present"));
        int keyWidth = Math.max(20, right - left);
        String selected = this.font.plainSubstrByWidth(this.selectedComponentKey, keyWidth);
        guiGraphics.drawString(this.font, selected, left, y, componentPrimaryTextColor(), false);
        guiGraphics.drawString(this.font, status, left, y + 12, present ? GOOD_GREEN : componentSecondaryTextColor(), false);
    }

    private List<String> getAvailableComponentKeys() {
        String filter = this.componentFilterValue == null ? "" : this.componentFilterValue.trim().toLowerCase(Locale.ROOT);
        return BuiltInRegistries.DATA_COMPONENT_TYPE.keySet().stream()
                .map(ResourceLocation::toString)
                .filter(key -> filter.isEmpty() || key.toLowerCase(Locale.ROOT).contains(filter))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> getAllComponentKeys() {
        return BuiltInRegistries.DATA_COMPONENT_TYPE.keySet().stream()
                .map(ResourceLocation::toString)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private void ensureSelectedComponent(List<String> filteredKeys) {
        List<String> allKeys = getAllComponentKeys();
        if (this.selectedComponentKey != null && allKeys.contains(this.selectedComponentKey)) {
            return;
        }
        this.selectedComponentKey = filteredKeys.isEmpty()
                ? (allKeys.isEmpty() ? "" : allKeys.get(0))
                : filteredKeys.get(0);
        this.componentNbtValue = selectedComponentValue();
    }

    private void applySelectedComponentValue(String raw) {
        if (this.syncingComponentValue || this.selectedComponentKey == null || this.selectedComponentKey.isBlank()) {
            return;
        }

        try {
            CompoundTag components = currentComponents();
            String value = raw == null ? "" : raw.trim();
            if (value.isEmpty()) {
                components.remove(this.selectedComponentKey);
                this.previewStack = parseStackWithComponents(this.previewStack, components);
                this.nbtFeedback = Component.translatable(messageKey("editor_component_removed")).getString();
            } else {
                CompoundTag parsed = parseNbt("{value:" + value + "}");
                Tag tag = parsed == null ? null : parsed.get("value");
                if (tag == null) {
                    throw new IllegalArgumentException(Component.translatable(messageKey("editor_component_invalid")).getString());
                }
                components.put(this.selectedComponentKey, tag.copy());
                this.previewStack = parseStackWithComponents(this.previewStack, components);
                this.nbtFeedback = Component.translatable(messageKey("editor_component_updated")).getString();
            }
            readMainFieldsFromStack(this.previewStack);
            syncNbtEditorValuesFromStack();
            this.componentNbtValue = selectedComponentValue();
            this.nbtFeedbackGood = true;
        } catch (CommandSyntaxException | RuntimeException exception) {
            this.nbtFeedbackGood = false;
            this.nbtFeedback = exception.getMessage() == null
                    ? Component.translatable(messageKey("editor_component_invalid")).getString()
                    : exception.getMessage();
        }
    }

    private String selectedComponentValue() {
        if (this.selectedComponentKey == null || this.selectedComponentKey.isBlank()) {
            return "";
        }
        Tag value = currentComponents().get(this.selectedComponentKey);
        return value == null ? "" : value.toString();
    }

    private CompoundTag currentComponents() {
        return ItemStackNbt.save(this.previewStack).getCompound("components").copy();
    }

    private void clampComponentListScroll() {
        this.componentListScroll = Mth.clamp(this.componentListScroll, 0, maxComponentListScroll());
    }

    private int maxComponentListScroll() {
        return Math.max(0, getAvailableComponentKeys().size() - componentVisibleRows());
    }

    private int componentPanelLeft() {
        return centeredContentX(componentPanelWidth());
    }

    private int componentPanelTop() {
        return 58;
    }

    private int componentPanelBottom() {
        return isSidebarUi() ? sidebarBottomButtonY() - 10 : this.height - 54;
    }

    private int componentPanelWidth() {
        if (isSidebarUi()) {
            return Math.max(220, contentWidth() - 24);
        }
        return contentLimitedWidth(620, 360, 24);
    }

    private int componentListWidth(int panelWidth) {
        return Mth.clamp(panelWidth / 3, 118, Math.max(118, panelWidth - 110));
    }

    private int componentFilterY() {
        return componentPanelTop() + 18;
    }

    private int componentListY() {
        return componentFilterY() + FIELD_HEIGHT + 18;
    }

    private int componentListHeight() {
        return Math.max(COMPONENT_ROW_HEIGHT, componentPanelBottom() - componentListY() - 6);
    }

    private int componentVisibleRows() {
        return Math.max(1, componentListHeight() / COMPONENT_ROW_HEIGHT);
    }

    private int componentValueBoxY() {
        return componentFilterY();
    }

    private int componentInputTextColor() {
        return isSidebarUi() ? ModernUi.TEXT_PRIMARY : 0xFFFFFFFF;
    }

    private int componentLabelColor() {
        return isSidebarUi() ? ModernUi.TEXT_MUTED : MAIN_COLOR;
    }

    private int componentPrimaryTextColor() {
        return isSidebarUi() ? ModernUi.TEXT_PRIMARY : 0xFFFFFFFF;
    }

    private int componentSelectedTextColor() {
        return isSidebarUi() ? ModernUi.TEXT_PRIMARY : GOOD_GREEN;
    }

    private int componentSecondaryTextColor() {
        return isSidebarUi() ? ModernUi.TEXT_SECONDARY : ALT_COLOR;
    }
}

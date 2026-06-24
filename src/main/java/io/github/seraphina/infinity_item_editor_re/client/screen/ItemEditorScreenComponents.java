package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.seraphina.infinity_item_editor_re.client.screen.modern.ModernUi;
import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;
import io.github.seraphina.infinity_item_editor_re.util.NbtCompat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

abstract class ItemEditorScreenComponents extends ItemEditorScreenActions {
    private static final int COMPONENT_ROW_HEIGHT = 14;
    private static final int COMPONENT_PANEL_GAP = 12;
    private static final int COMPONENT_DOUBLE_CLICK_MS = 350;
    private static final String VANILLA_NAMESPACE = "minecraft";
    private static final String MOD_GROUP_PREFIX = "mod:";

    private static final List<VanillaComponentCategory> VANILLA_CATEGORIES = List.of(
            new VanillaComponentCategory("display", Set.of("custom_name", "item_name", "item_model", "lore", "rarity",
                    "tooltip_display", "tooltip_style", "enchantment_glint_override", "dyed_color", "custom_model_data")),
            new VanillaComponentCategory("stack_durability", Set.of("max_stack_size", "max_damage", "damage", "unbreakable",
                    "repair_cost", "repairable", "damage_resistant", "break_sound")),
            new VanillaComponentCategory("enchantments_combat", Set.of("enchantments", "stored_enchantments", "enchantable",
                    "attribute_modifiers", "weapon", "blocks_attacks", "death_protection")),
            new VanillaComponentCategory("tools_blocks", Set.of("tool", "can_place_on", "can_break", "block_state",
                    "block_entity_data", "lock", "creative_slot_lock", "debug_stick_state")),
            new VanillaComponentCategory("food_use", Set.of("food", "consumable", "use_remainder", "use_cooldown")),
            new VanillaComponentCategory("equipment", Set.of("equippable", "glider", "trim")),
            new VanillaComponentCategory("containers", Set.of("container", "bundle_contents", "container_loot", "charged_projectiles")),
            new VanillaComponentCategory("potions_effects", Set.of("potion_contents", "potion_duration_scale",
                    "suspicious_stew_effects", "ominous_bottle_amplifier")),
            new VanillaComponentCategory("books_maps_music", Set.of("writable_book_content", "written_book_content",
                    "map_color", "map_id", "map_decorations", "map_post_processing", "recipes", "lodestone_tracker",
                    "jukebox_playable", "note_block_sound", "instrument")),
            new VanillaComponentCategory("blocks_decor", Set.of("firework_explosion", "fireworks", "banner_patterns",
                    "base_color", "pot_decorations", "provides_banner_patterns", "provides_trim_material", "bees")),
            new VanillaComponentCategory("entities", Set.of("entity_data", "bucket_entity_data", "profile",
                    "villager/variant", "wolf/variant", "wolf/sound_variant", "wolf/collar", "fox/variant",
                    "salmon/size", "parrot/variant", "tropical_fish/pattern", "tropical_fish/base_color",
                    "tropical_fish/pattern_color", "mooshroom/variant", "rabbit/variant", "pig/variant",
                    "cow/variant", "chicken/variant", "frog/variant", "horse/variant", "painting/variant",
                    "llama/variant", "axolotl/variant", "cat/variant", "cat/collar", "sheep/color", "shulker/color")),
            new VanillaComponentCategory("advanced", Set.of("custom_data", "intangible_projectile"))
    );
    private static final Map<String, String> VANILLA_CATEGORY_BY_PATH = createVanillaCategoryMap();
    private static final Set<String> MARKER_COMPONENTS = Set.of(
            "minecraft:unbreakable",
            "minecraft:creative_slot_lock",
            "minecraft:intangible_projectile",
            "minecraft:glider"
    );
    private static final Set<String> BOOLEAN_COMPONENTS = Set.of(
            "minecraft:enchantment_glint_override"
    );
    private static final Set<String> NUMBER_COMPONENTS = Set.of(
            "minecraft:max_stack_size",
            "minecraft:max_damage",
            "minecraft:damage",
            "minecraft:repair_cost",
            "minecraft:potion_duration_scale",
            "minecraft:ominous_bottle_amplifier",
            "minecraft:map_id"
    );
    private static final Set<String> STRING_COMPONENTS = Set.of(
            "minecraft:custom_name",
            "minecraft:item_name",
            "minecraft:item_model",
            "minecraft:rarity",
            "minecraft:tooltip_style",
            "minecraft:map_post_processing",
            "minecraft:instrument",
            "minecraft:provides_banner_patterns",
            "minecraft:note_block_sound",
            "minecraft:base_color",
            "minecraft:break_sound",
            "minecraft:villager/variant",
            "minecraft:wolf/variant",
            "minecraft:wolf/sound_variant",
            "minecraft:wolf/collar",
            "minecraft:fox/variant",
            "minecraft:salmon/size",
            "minecraft:parrot/variant",
            "minecraft:tropical_fish/pattern",
            "minecraft:tropical_fish/base_color",
            "minecraft:tropical_fish/pattern_color",
            "minecraft:mooshroom/variant",
            "minecraft:rabbit/variant",
            "minecraft:pig/variant",
            "minecraft:cow/variant",
            "minecraft:chicken/variant",
            "minecraft:frog/variant",
            "minecraft:horse/variant",
            "minecraft:painting/variant",
            "minecraft:llama/variant",
            "minecraft:axolotl/variant",
            "minecraft:cat/variant",
            "minecraft:cat/collar",
            "minecraft:sheep/color",
            "minecraft:shulker/color"
    );
    private static final Set<String> LIST_COMPONENTS = Set.of(
            "minecraft:charged_projectiles",
            "minecraft:bundle_contents",
            "minecraft:suspicious_stew_effects",
            "minecraft:recipes",
            "minecraft:banner_patterns",
            "minecraft:pot_decorations",
            "minecraft:container",
            "minecraft:bees"
    );
    private static final Map<String, String> DEFAULT_COMPONENT_VALUES = createDefaultComponentValues();

    protected ItemEditorScreenComponents(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(stack, targetContainerSlot, parentTradeScreen, parentTradeIndex, parentTradeSlot);
    }

    protected void addComponentEditorPanel() {
        int left = componentPanelLeft();
        int width = componentPanelWidth();
        int listWidth = componentListWidth(width);
        int valueLeft = left + listWidth + COMPONENT_PANEL_GAP;
        int valueWidth = Math.max(120, width - listWidth - COMPONENT_PANEL_GAP);
        ensureSelectedComponent(getAllComponentKeys());

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
                Component.translatable(key("components.raw_value"))));
        this.componentNbtBox.setMaxLength(30000);
        this.componentNbtBox.setTextColor(componentInputTextColor());
        this.componentNbtBox.active = hasSelectedComponent();
        setComponentBoxValue(selectedComponentValue());
        this.componentNbtBox.setResponder(value -> {
            this.componentNbtValue = value;
            applySelectedComponentValue(value);
        });

        addDirectComponentButtons(valueLeft, componentActionButtonY(), valueWidth);
    }

    protected void renderComponentEditorPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int left = componentPanelLeft();
        int top = componentPanelTop();
        int width = componentPanelWidth();
        int bottom = componentPanelBottom();
        int listWidth = componentListWidth(width);
        int valueLeft = left + listWidth + COMPONENT_PANEL_GAP;
        int valueRight = left + width;

        if (isSidebarUi()) {
            ModernUi.fillToolDrawer(guiGraphics, left - 8, top, left + width + 8, bottom, false);
            ModernUi.fillToolDrawer(guiGraphics, valueLeft - 6, top + 18, valueRight + 6, bottom - 6, false);
        } else {
            guiGraphics.fill(left - 8, top, left + width + 8, bottom, 0xB8202020);
            guiGraphics.fill(left - 8, top, left + width + 8, top + 1, MAIN_COLOR);
            guiGraphics.fill(left - 8, bottom - 1, left + width + 8, bottom, MAIN_COLOR);
            guiGraphics.fill(left - 8, top, left - 7, bottom, MAIN_COLOR);
            guiGraphics.fill(left + width + 7, top, left + width + 8, bottom, MAIN_COLOR);
            guiGraphics.fill(valueLeft - 6, top + 18, valueRight + 6, bottom - 6, 0x55323232);
        }

        int labelColor = componentLabelColor();
        guiGraphics.drawString(this.font, Component.translatable(key("components.search")), left, componentFilterY() - 10, labelColor, false);
        guiGraphics.drawString(this.font, Component.translatable(key("components.available")), left, componentListY() - 10, labelColor, false);
        guiGraphics.drawString(this.font, Component.translatable(key("components.raw_value")), valueLeft, componentValueBoxY() - 10, labelColor, false);

        renderComponentList(guiGraphics, mouseX, mouseY, left, listWidth);
        renderSelectedComponentSummary(guiGraphics, valueLeft, valueRight, componentActionButtonY() + FIELD_HEIGHT + 10);

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

        List<ComponentRow> rows = getComponentRows();
        int rowIndex = ((int) mouseY - top) / COMPONENT_ROW_HEIGHT;
        int index = this.componentListScroll + rowIndex;
        if (rowIndex < 0 || rowIndex >= componentVisibleRows() || index < 0 || index >= rows.size()) {
            return false;
        }

        ComponentRow row = rows.get(index);
        if (row.type() == ComponentRowType.MOD) {
            handleComponentGroupClick(row.id());
            return true;
        }
        if (row.type() != ComponentRowType.COMPONENT || row.componentKey() == null) {
            return true;
        }

        selectComponent(row.componentKey());
        return true;
    }

    protected boolean scrollComponentList(double scrollY) {
        int previous = this.componentListScroll;
        this.componentListScroll = Mth.clamp(this.componentListScroll - (int) Math.signum(scrollY), 0, maxComponentListScroll());
        return previous != this.componentListScroll || maxComponentListScroll() > 0;
    }

    private void addDirectComponentButtons(int left, int y, int width) {
        if (!hasSelectedComponent()) {
            return;
        }

        ComponentValueKind kind = selectedComponentValueKind();
        int gap = 4;
        int columns = width >= 360 ? 4 : 2;
        int buttonWidth = Math.max(44, (width - gap * (columns - 1)) / columns);
        int buttonHeight = isSidebarUi() ? SIDEBAR_BUTTON_HEIGHT : FIELD_HEIGHT;
        addComponentActionButton(left, y, buttonWidth, buttonHeight, 0, columns, gap,
                Component.translatable(key(kind == ComponentValueKind.MARKER ? "components.action.enable" : "components.action.default")),
                button -> applyDefaultComponentValue());
        addComponentActionButton(left, y, buttonWidth, buttonHeight, 1, columns, gap,
                Component.translatable(key("components.action.remove")), button -> removeSelectedComponent());

        if (kind == ComponentValueKind.BOOLEAN) {
            addComponentActionButton(left, y, buttonWidth, buttonHeight, 2, columns, gap,
                    Component.translatable(key("components.action.true")), button -> setSelectedComponentRaw("true", true));
            addComponentActionButton(left, y, buttonWidth, buttonHeight, 3, columns, gap,
                    Component.translatable(key("components.action.false")), button -> setSelectedComponentRaw("false", true));
        } else if (kind == ComponentValueKind.NUMBER) {
            addComponentActionButton(left, y, buttonWidth, buttonHeight, 2, columns, gap,
                    Component.literal("-1"), button -> stepSelectedNumericComponent(-1));
            addComponentActionButton(left, y, buttonWidth, buttonHeight, 3, columns, gap,
                    Component.literal("+1"), button -> stepSelectedNumericComponent(1));
        }
    }

    private void addComponentActionButton(int left, int y, int width, int height, int index, int columns, int gap,
                                          Component text, InfinityEditorButton.PressAction action) {
        int row = index / columns;
        int column = index % columns;
        addRenderableWidget(new InfinityEditorButton(left + column * (width + gap), y + row * (height + gap),
                width, height, text, action));
    }

    private void renderComponentList(GuiGraphics guiGraphics, int mouseX, int mouseY, int left, int width) {
        List<ComponentRow> rows = getComponentRows();
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

        if (rows.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable(key("components.no_match")),
                    left + 6, top + 6, componentSecondaryTextColor(), false);
            return;
        }

        CompoundTag components = currentComponents();
        int visibleRows = componentVisibleRows();
        int end = Math.min(rows.size(), this.componentListScroll + visibleRows);
        for (int i = this.componentListScroll; i < end; i++) {
            ComponentRow row = rows.get(i);
            int y = top + (i - this.componentListScroll) * COMPONENT_ROW_HEIGHT;
            boolean selected = row.type() == ComponentRowType.COMPONENT && row.componentKey().equals(this.selectedComponentKey);
            boolean hovered = isMouseIn(mouseX, mouseY, left, y, width, COMPONENT_ROW_HEIGHT);
            if (isSidebarUi()) {
                if (selected || hovered) {
                    ModernUi.fillSelection(guiGraphics, left + 2, y + 1, left + width - 2, y + COMPONENT_ROW_HEIGHT - 1, 4, selected);
                }
            } else if (selected || hovered) {
                guiGraphics.fill(left + 1, y, left + width - 1, y + COMPONENT_ROW_HEIGHT, selected ? 0x8032CC64 : 0x55323232);
            }

            int textX = left + 5 + row.depth() * 10;
            int textColor = rowColor(row, selected);
            String marker = rowMarker(row, components);
            String label = marker + rowLabel(row);
            String clipped = this.font.plainSubstrByWidth(label, Math.max(20, width - (textX - left) - 8));
            guiGraphics.drawString(this.font, clipped, textX, y + 3, textColor, false);
        }
    }

    private void renderSelectedComponentSummary(GuiGraphics guiGraphics, int left, int right, int y) {
        if (!hasSelectedComponent()) {
            return;
        }

        CompoundTag components = currentComponents();
        boolean present = components.contains(this.selectedComponentKey);
        ComponentValueKind kind = selectedComponentValueKind();
        String path = componentPath(this.selectedComponentKey);
        String defaultValue = defaultComponentValue(this.selectedComponentKey, kind);
        int textWidth = Math.max(20, right - left - 8);
        int color = componentPrimaryTextColor();
        int muted = componentSecondaryTextColor();

        guiGraphics.drawString(this.font, componentDisplayName(this.selectedComponentKey), left, y, color, false);
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(this.selectedComponentKey, textWidth), left, y + 14, muted, false);
        guiGraphics.drawString(this.font, Component.translatable(key("components.status"),
                Component.translatable(key(present ? "components.present" : "components.not_present"))), left, y + 30,
                present ? GOOD_GREEN : muted, false);
        guiGraphics.drawString(this.font, Component.translatable(key("components.value_type"),
                Component.translatable(key("components.type." + kind.translationKey()))), left, y + 44, muted, false);
        guiGraphics.drawString(this.font, Component.translatable(key("components.category_label"),
                componentCategoryName(this.selectedComponentKey)), left, y + 58, muted, false);

        int directY = y + 78;
        guiGraphics.drawString(this.font, Component.translatable(key("components.direct")), left, directY, componentLabelColor(), false);
        String hint = Component.translatable(key("components.direct_hint")).getString();
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(hint, textWidth), left, directY + 14, muted, false);

        if (!defaultValue.isBlank()) {
            int defaultY = directY + 34;
            guiGraphics.drawString(this.font, Component.translatable(key("components.default_value")), left, defaultY, componentLabelColor(), false);
            guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(defaultValue, textWidth), left, defaultY + 14, muted, false);
        }

        if (isVanillaComponent(this.selectedComponentKey) && !VANILLA_CATEGORY_BY_PATH.containsKey(path)) {
            guiGraphics.drawString(this.font, Component.translatable(key("components.category.advanced")), left, y + 72, muted, false);
        }
    }

    private List<ComponentRow> getComponentRows() {
        List<String> keys = getAllComponentKeys();
        String filter = normalizedComponentFilter();
        List<ComponentRow> rows = new ArrayList<>();
        Set<String> addedVanilla = new java.util.HashSet<>();

        for (VanillaComponentCategory category : VANILLA_CATEGORIES) {
            List<String> categoryKeys = keys.stream()
                    .filter(this::isVanillaComponent)
                    .filter(componentKey -> category.paths().contains(componentPath(componentKey)))
                    .filter(componentKey -> componentMatchesFilter(componentKey, filter))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            if (categoryKeys.isEmpty()) {
                continue;
            }
            rows.add(new ComponentRow(ComponentRowType.CATEGORY, category.key(), null, null, ComponentValueKind.OTHER, 0));
            for (String componentKey : categoryKeys) {
                addedVanilla.add(componentKey);
                rows.add(new ComponentRow(ComponentRowType.COMPONENT, category.key(), componentKey, componentKey,
                        componentValueKind(componentKey), 1));
            }
        }

        List<String> ungroupedVanilla = keys.stream()
                .filter(this::isVanillaComponent)
                .filter(componentKey -> !addedVanilla.contains(componentKey))
                .filter(componentKey -> componentMatchesFilter(componentKey, filter))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        if (!ungroupedVanilla.isEmpty()) {
            rows.add(new ComponentRow(ComponentRowType.CATEGORY, "advanced", null, null, ComponentValueKind.OTHER, 0));
            for (String componentKey : ungroupedVanilla) {
                rows.add(new ComponentRow(ComponentRowType.COMPONENT, "advanced", componentKey, componentKey,
                        componentValueKind(componentKey), 1));
            }
        }

        Map<String, List<String>> modded = new HashMap<>();
        for (String key : keys) {
            if (isVanillaComponent(key) || !componentMatchesFilter(key, filter)) {
                continue;
            }
            modded.computeIfAbsent(componentNamespace(key), ignored -> new ArrayList<>()).add(key);
        }
        if (!modded.isEmpty()) {
            rows.add(new ComponentRow(ComponentRowType.CATEGORY, "modded", null, null, ComponentValueKind.OTHER, 0));
            modded.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach(namespace -> addModdedRows(rows, namespace, modded.get(namespace), filter));
        }

        return rows;
    }

    private void addModdedRows(List<ComponentRow> rows, String namespace, List<String> keys, String filter) {
        String groupId = MOD_GROUP_PREFIX + namespace;
        rows.add(new ComponentRow(ComponentRowType.MOD, groupId, null, null, ComponentValueKind.OTHER, 1));
        if (!this.expandedComponentGroups.contains(groupId) && filter.isBlank()) {
            return;
        }

        List<String> sortedKeys = keys.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
        for (ComponentValueKind kind : ComponentValueKind.values()) {
            List<String> typed = sortedKeys.stream()
                    .filter(componentKey -> componentValueKind(componentKey) == kind)
                    .toList();
            if (typed.isEmpty()) {
                continue;
            }
            rows.add(new ComponentRow(ComponentRowType.TYPE, kind.translationKey(), null, null, kind, 2));
            for (String componentKey : typed) {
                rows.add(new ComponentRow(ComponentRowType.COMPONENT, groupId, componentKey, componentKey, kind, 3));
            }
        }
    }

    private boolean componentMatchesFilter(String componentKey, String filter) {
        if (filter.isBlank()) {
            return true;
        }
        return componentKey.toLowerCase(Locale.ROOT).contains(filter)
                || readableComponentPath(componentPath(componentKey)).toLowerCase(Locale.ROOT).contains(filter)
                || modDisplayName(componentNamespace(componentKey)).toLowerCase(Locale.ROOT).contains(filter);
    }

    private void handleComponentGroupClick(String groupId) {
        long now = System.currentTimeMillis();
        if (groupId.equals(this.lastComponentGroupClick) && now - this.lastComponentGroupClickMs <= COMPONENT_DOUBLE_CLICK_MS) {
            if (this.expandedComponentGroups.contains(groupId)) {
                this.expandedComponentGroups.remove(groupId);
            } else {
                this.expandedComponentGroups.add(groupId);
            }
            this.lastComponentGroupClick = "";
            this.lastComponentGroupClickMs = 0L;
            clampComponentListScroll();
            return;
        }
        this.lastComponentGroupClick = groupId;
        this.lastComponentGroupClickMs = now;
        this.nbtFeedbackGood = true;
        this.nbtFeedback = Component.translatable(messageKey("editor_component_group_double_click")).getString();
    }

    private void selectComponent(String componentKey) {
        this.selectedComponentKey = componentKey;
        setComponentBoxValue(selectedComponentValue());
        if (this.componentNbtBox != null) {
            this.componentNbtBox.active = true;
            this.componentNbtBox.setCursorPosition(0);
        }
    }

    private List<String> getAllComponentKeys() {
        return BuiltInRegistries.DATA_COMPONENT_TYPE.keySet().stream()
                .map(Identifier::toString)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private void ensureSelectedComponent(List<String> allKeys) {
        if (this.selectedComponentKey != null && allKeys.contains(this.selectedComponentKey)) {
            return;
        }
        this.selectedComponentKey = allKeys.isEmpty() ? "" : allKeys.get(0);
        this.componentNbtValue = selectedComponentValue();
    }

    private void applySelectedComponentValue(String raw) {
        if (this.syncingComponentValue || !hasSelectedComponent()) {
            return;
        }
        applyComponentValue(this.selectedComponentKey, raw);
    }

    private void applyDefaultComponentValue() {
        if (!hasSelectedComponent()) {
            return;
        }
        setSelectedComponentRaw(defaultComponentValue(this.selectedComponentKey, selectedComponentValueKind()), true);
    }

    private void removeSelectedComponent() {
        setSelectedComponentRaw("", true);
    }

    private void stepSelectedNumericComponent(int delta) {
        if (!hasSelectedComponent()) {
            return;
        }
        String raw = selectedComponentValue();
        if (raw.isBlank()) {
            raw = defaultComponentValue(this.selectedComponentKey, ComponentValueKind.NUMBER);
        }
        double value = parseLooseNumber(raw);
        String next = raw.contains(".") || raw.endsWith("f") || raw.endsWith("d")
                ? String.format(Locale.ROOT, "%.1ff", value + delta)
                : Integer.toString((int) Math.max(0, Math.round(value + delta)));
        setSelectedComponentRaw(next, true);
    }

    private void setSelectedComponentRaw(String raw, boolean apply) {
        this.componentNbtValue = raw == null ? "" : raw;
        setComponentBoxValue(this.componentNbtValue);
        if (apply) {
            applySelectedComponentValue(this.componentNbtValue);
        }
    }

    private void setComponentBoxValue(String raw) {
        this.syncingComponentValue = true;
        this.componentNbtValue = raw == null ? "" : raw;
        if (this.componentNbtBox != null) {
            this.componentNbtBox.setValue(this.componentNbtValue);
        }
        this.syncingComponentValue = false;
    }

    private void applyComponentValue(String componentKey, String raw) {
        try {
            CompoundTag components = currentComponents();
            String value = raw == null ? "" : raw.trim();
            if (value.isEmpty()) {
                components.remove(componentKey);
                this.previewStack = parseStackWithComponents(this.previewStack, components);
                this.nbtFeedback = Component.translatable(messageKey("editor_component_removed")).getString();
            } else {
                CompoundTag parsed = parseNbt("{value:" + value + "}");
                Tag tag = parsed == null ? null : parsed.get("value");
                if (tag == null) {
                    throw new IllegalArgumentException(Component.translatable(messageKey("editor_component_invalid")).getString());
                }
                components.put(componentKey, tag.copy());
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
        if (!hasSelectedComponent()) {
            return "";
        }
        Tag value = currentComponents().get(this.selectedComponentKey);
        return value == null ? "" : value.toString();
    }

    private ComponentValueKind selectedComponentValueKind() {
        return hasSelectedComponent() ? componentValueKind(this.selectedComponentKey) : ComponentValueKind.OTHER;
    }

    private ComponentValueKind componentValueKind(String componentKey) {
        Tag tag = currentComponents().get(componentKey);
        if (tag != null) {
            if (MARKER_COMPONENTS.contains(componentKey) && tag instanceof CompoundTag compound && compound.isEmpty()) {
                return ComponentValueKind.MARKER;
            }
            if (BOOLEAN_COMPONENTS.contains(componentKey)) {
                return ComponentValueKind.BOOLEAN;
            }
            return switch (tag.getId()) {
                case Tag.TAG_COMPOUND -> ((CompoundTag) tag).isEmpty() ? ComponentValueKind.EMPTY : ComponentValueKind.COMPOUND;
                case Tag.TAG_LIST -> ComponentValueKind.LIST;
                case Tag.TAG_STRING -> ComponentValueKind.STRING;
                case Tag.TAG_BYTE, Tag.TAG_SHORT, Tag.TAG_INT, Tag.TAG_LONG, Tag.TAG_FLOAT, Tag.TAG_DOUBLE -> ComponentValueKind.NUMBER;
                default -> ComponentValueKind.OTHER;
            };
        }
        if (MARKER_COMPONENTS.contains(componentKey)) {
            return ComponentValueKind.MARKER;
        }
        if (BOOLEAN_COMPONENTS.contains(componentKey)) {
            return ComponentValueKind.BOOLEAN;
        }
        if (NUMBER_COMPONENTS.contains(componentKey)) {
            return ComponentValueKind.NUMBER;
        }
        if (STRING_COMPONENTS.contains(componentKey)) {
            return ComponentValueKind.STRING;
        }
        if (LIST_COMPONENTS.contains(componentKey)) {
            return ComponentValueKind.LIST;
        }
        return DEFAULT_COMPONENT_VALUES.getOrDefault(componentKey, "{}").startsWith("[")
                ? ComponentValueKind.LIST
                : ComponentValueKind.COMPOUND;
    }

    private String defaultComponentValue(String componentKey, ComponentValueKind kind) {
        String value = DEFAULT_COMPONENT_VALUES.get(componentKey);
        if (value != null) {
            return value;
        }
        return switch (kind) {
            case MARKER, COMPOUND, EMPTY -> "{}";
            case LIST -> "[]";
            case BOOLEAN -> "true";
            case NUMBER -> "0";
            case STRING -> "\"minecraft:stone\"";
            case OTHER -> "{}";
        };
    }

    private CompoundTag currentComponents() {
        CompoundTag saved = ItemStackNbt.save(this.previewStack);
        return NbtCompat.getCompound(saved, "components").copy();
    }

    private boolean hasSelectedComponent() {
        return this.selectedComponentKey != null && !this.selectedComponentKey.isBlank();
    }

    private String normalizedComponentFilter() {
        return this.componentFilterValue == null ? "" : this.componentFilterValue.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isVanillaComponent(String componentKey) {
        return VANILLA_NAMESPACE.equals(componentNamespace(componentKey));
    }

    private String componentNamespace(String componentKey) {
        int separator = componentKey.indexOf(':');
        return separator <= 0 ? VANILLA_NAMESPACE : componentKey.substring(0, separator);
    }

    private String componentPath(String componentKey) {
        int separator = componentKey.indexOf(':');
        return separator < 0 ? componentKey : componentKey.substring(separator + 1);
    }

    private String rowLabel(ComponentRow row) {
        return switch (row.type()) {
            case CATEGORY -> Component.translatable(key("components.category." + row.id())).getString();
            case MOD -> (this.expandedComponentGroups.contains(row.id()) ? "[-] " : "[+] ")
                    + modDisplayName(row.id().substring(MOD_GROUP_PREFIX.length()));
            case TYPE -> Component.translatable(key("components.type." + row.valueKind().translationKey())).getString();
            case COMPONENT -> componentDisplayName(row.componentKey()).getString();
        };
    }

    private String rowMarker(ComponentRow row, CompoundTag components) {
        if (row.type() == ComponentRowType.COMPONENT) {
            return components.contains(row.componentKey()) ? "* " : "  ";
        }
        if (row.type() == ComponentRowType.TYPE) {
            return "- ";
        }
        return "";
    }

    private int rowColor(ComponentRow row, boolean selected) {
        if (selected) {
            return componentSelectedTextColor();
        }
        return switch (row.type()) {
            case CATEGORY -> componentLabelColor();
            case MOD -> GOOD_GREEN;
            case TYPE -> componentSecondaryTextColor();
            case COMPONENT -> componentPrimaryTextColor();
        };
    }

    private Component componentDisplayName(String componentKey) {
        if (isVanillaComponent(componentKey)) {
            String path = componentPath(componentKey);
            return Component.translatableWithFallback(key("component.minecraft." + sanitizeComponentPath(path)), readableComponentPath(path));
        }
        return Component.literal(readableComponentPath(componentPath(componentKey)));
    }

    private Component componentCategoryName(String componentKey) {
        if (!isVanillaComponent(componentKey)) {
            return Component.literal(modDisplayName(componentNamespace(componentKey)));
        }
        String category = VANILLA_CATEGORY_BY_PATH.getOrDefault(componentPath(componentKey), "advanced");
        return Component.translatable(key("components.category." + category));
    }

    private String modDisplayName(String namespace) {
        if (namespace == null || namespace.isBlank()) {
            return Component.translatable(key("components.modded")).getString();
        }
        return ModList.get().getModContainerById(namespace)
                .map(container -> container.getModInfo().getDisplayName())
                .orElseGet(() -> titleCase(namespace.replace('_', ' ').replace('-', ' ')));
    }

    private String readableComponentPath(String path) {
        return titleCase(path.replace('/', ' ').replace('_', ' ').replace('-', ' '));
    }

    private String titleCase(String value) {
        StringBuilder builder = new StringBuilder();
        boolean upper = true;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isWhitespace(c)) {
                builder.append(c);
                upper = true;
            } else if (upper) {
                builder.append(Character.toUpperCase(c));
                upper = false;
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private String sanitizeComponentPath(String path) {
        return path.replace('/', '.').replace('-', '_');
    }

    private double parseLooseNumber(String raw) {
        String normalized = raw == null ? "0" : raw.trim().toLowerCase(Locale.ROOT)
                .replace("b", "")
                .replace("s", "")
                .replace("l", "")
                .replace("f", "")
                .replace("d", "");
        if (normalized.isBlank()) {
            return 0.0D;
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException exception) {
            return 0.0D;
        }
    }

    private void clampComponentListScroll() {
        this.componentListScroll = Mth.clamp(this.componentListScroll, 0, maxComponentListScroll());
    }

    private int maxComponentListScroll() {
        return Math.max(0, getComponentRows().size() - componentVisibleRows());
    }

    private int componentPanelLeft() {
        return safeLeft() + 8;
    }

    private int componentPanelTop() {
        return isSidebarUi() ? safeTop() + 44 : 50;
    }

    private int componentPanelBottom() {
        return isSidebarUi() ? sidebarBottomButtonY() - 10 : this.height - 54;
    }

    private int componentPanelWidth() {
        return Math.max(220, contentWidth() - 16);
    }

    private int componentListWidth(int panelWidth) {
        int max = Math.max(130, panelWidth - 150);
        int min = Math.min(180, max);
        return Mth.clamp(panelWidth / 3, min, max);
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

    private int componentActionButtonY() {
        return componentValueBoxY() + FIELD_HEIGHT + 6;
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

    private static Map<String, String> createVanillaCategoryMap() {
        Map<String, String> categories = new HashMap<>();
        for (VanillaComponentCategory category : VANILLA_CATEGORIES) {
            for (String path : category.paths()) {
                categories.put(path, category.key());
            }
        }
        return categories;
    }

    private static Map<String, String> createDefaultComponentValues() {
        Map<String, String> values = new HashMap<>();
        values.put("minecraft:custom_data", "{}");
        values.put("minecraft:max_stack_size", "64");
        values.put("minecraft:max_damage", "100");
        values.put("minecraft:damage", "0");
        values.put("minecraft:unbreakable", "{}");
        values.put("minecraft:custom_name", "'{\"text\":\"Custom Name\"}'");
        values.put("minecraft:item_name", "'{\"text\":\"Item Name\"}'");
        values.put("minecraft:item_model", "\"minecraft:stick\"");
        values.put("minecraft:lore", "['{\"text\":\"Lore line\"}']");
        values.put("minecraft:rarity", "\"common\"");
        values.put("minecraft:enchantments", "{levels:{\"minecraft:sharpness\":1}}");
        values.put("minecraft:can_place_on", "{predicates:[{blocks:\"minecraft:stone\"}]}");
        values.put("minecraft:can_break", "{predicates:[{blocks:\"minecraft:stone\"}]}");
        values.put("minecraft:attribute_modifiers", "{modifiers:[]}");
        values.put("minecraft:custom_model_data", "{floats:[1.0f]}");
        values.put("minecraft:tooltip_display", "{}");
        values.put("minecraft:repair_cost", "0");
        values.put("minecraft:creative_slot_lock", "{}");
        values.put("minecraft:enchantment_glint_override", "true");
        values.put("minecraft:intangible_projectile", "{}");
        values.put("minecraft:food", "{nutrition:4,saturation:1.2f}");
        values.put("minecraft:consumable", "{consume_seconds:1.6f,animation:\"eat\",sound:\"minecraft:entity.generic.eat\"}");
        values.put("minecraft:use_remainder", "{id:\"minecraft:bowl\",count:1}");
        values.put("minecraft:use_cooldown", "{seconds:1.0f}");
        values.put("minecraft:damage_resistant", "{types:\"#minecraft:is_fire\"}");
        values.put("minecraft:tool", "{rules:[{blocks:\"#minecraft:mineable/pickaxe\",speed:6.0f,correct_for_drops:true}],default_mining_speed:1.0f,damage_per_block:1}");
        values.put("minecraft:weapon", "{item_damage_per_attack:1}");
        values.put("minecraft:enchantable", "{value:10}");
        values.put("minecraft:equippable", "{slot:\"head\",equip_sound:\"minecraft:item.armor.equip_generic\"}");
        values.put("minecraft:repairable", "{items:\"minecraft:iron_ingot\"}");
        values.put("minecraft:glider", "{}");
        values.put("minecraft:tooltip_style", "\"minecraft:default\"");
        values.put("minecraft:death_protection", "{}");
        values.put("minecraft:blocks_attacks", "{}");
        values.put("minecraft:stored_enchantments", "{levels:{\"minecraft:sharpness\":1}}");
        values.put("minecraft:dyed_color", "{rgb:16777215}");
        values.put("minecraft:map_color", "{rgb:16777215}");
        values.put("minecraft:map_id", "0");
        values.put("minecraft:map_decorations", "{}");
        values.put("minecraft:map_post_processing", "\"lock\"");
        values.put("minecraft:charged_projectiles", "[]");
        values.put("minecraft:bundle_contents", "[]");
        values.put("minecraft:potion_contents", "{potion:\"minecraft:water\"}");
        values.put("minecraft:potion_duration_scale", "1.0f");
        values.put("minecraft:suspicious_stew_effects", "[]");
        values.put("minecraft:writable_book_content", "{pages:[]}");
        values.put("minecraft:written_book_content", "{title:\"Title\",author:\"Player\",pages:[]}");
        values.put("minecraft:trim", "{material:\"minecraft:iron\",pattern:\"minecraft:sentry\"}");
        values.put("minecraft:debug_stick_state", "{}");
        values.put("minecraft:entity_data", "{id:\"minecraft:pig\"}");
        values.put("minecraft:bucket_entity_data", "{}");
        values.put("minecraft:block_entity_data", "{}");
        values.put("minecraft:instrument", "\"minecraft:ponder_goat_horn\"");
        values.put("minecraft:provides_trim_material", "{asset_name:\"iron\",ingredient:\"minecraft:iron_ingot\",item_model_index:0.0f}");
        values.put("minecraft:ominous_bottle_amplifier", "1");
        values.put("minecraft:jukebox_playable", "{song:\"minecraft:music_disc.13\"}");
        values.put("minecraft:provides_banner_patterns", "\"minecraft:pattern_item/flower\"");
        values.put("minecraft:recipes", "[\"minecraft:crafting_table\"]");
        values.put("minecraft:lodestone_tracker", "{tracked:false}");
        values.put("minecraft:firework_explosion", "{shape:\"small_ball\",colors:[I;16711680]}");
        values.put("minecraft:fireworks", "{flight_duration:1,explosions:[]}");
        values.put("minecraft:profile", "{name:\"Steve\"}");
        values.put("minecraft:note_block_sound", "\"minecraft:block.note_block.harp\"");
        values.put("minecraft:banner_patterns", "[]");
        values.put("minecraft:base_color", "\"white\"");
        values.put("minecraft:pot_decorations", "[\"minecraft:brick\",\"minecraft:brick\",\"minecraft:brick\",\"minecraft:brick\"]");
        values.put("minecraft:container", "[]");
        values.put("minecraft:block_state", "{}");
        values.put("minecraft:bees", "[]");
        values.put("minecraft:lock", "\"\"");
        values.put("minecraft:container_loot", "{loot_table:\"minecraft:chests/simple_dungeon\"}");
        values.put("minecraft:break_sound", "\"minecraft:block.stone.break\"");
        values.put("minecraft:villager/variant", "\"minecraft:plains\"");
        values.put("minecraft:wolf/variant", "\"minecraft:ashen\"");
        values.put("minecraft:wolf/sound_variant", "\"minecraft:classic\"");
        values.put("minecraft:wolf/collar", "\"red\"");
        values.put("minecraft:fox/variant", "\"red\"");
        values.put("minecraft:salmon/size", "\"medium\"");
        values.put("minecraft:parrot/variant", "\"red_blue\"");
        values.put("minecraft:tropical_fish/pattern", "\"kob\"");
        values.put("minecraft:tropical_fish/base_color", "\"white\"");
        values.put("minecraft:tropical_fish/pattern_color", "\"orange\"");
        values.put("minecraft:mooshroom/variant", "\"red\"");
        values.put("minecraft:rabbit/variant", "\"brown\"");
        values.put("minecraft:pig/variant", "\"minecraft:temperate\"");
        values.put("minecraft:cow/variant", "\"minecraft:temperate\"");
        values.put("minecraft:chicken/variant", "\"minecraft:temperate\"");
        values.put("minecraft:frog/variant", "\"minecraft:temperate\"");
        values.put("minecraft:horse/variant", "\"white\"");
        values.put("minecraft:painting/variant", "\"minecraft:kebab\"");
        values.put("minecraft:llama/variant", "\"creamy\"");
        values.put("minecraft:axolotl/variant", "\"lucy\"");
        values.put("minecraft:cat/variant", "\"minecraft:tabby\"");
        values.put("minecraft:cat/collar", "\"red\"");
        values.put("minecraft:sheep/color", "\"white\"");
        values.put("minecraft:shulker/color", "\"purple\"");
        return values;
    }

    private enum ComponentRowType {
        CATEGORY,
        MOD,
        TYPE,
        COMPONENT
    }

    private enum ComponentValueKind {
        COMPOUND("compound"),
        LIST("list"),
        STRING("string"),
        NUMBER("number"),
        BOOLEAN("boolean"),
        MARKER("marker"),
        EMPTY("empty"),
        OTHER("other");

        private final String translationKey;

        ComponentValueKind(String translationKey) {
            this.translationKey = translationKey;
        }

        private String translationKey() {
            return this.translationKey;
        }
    }

    private record VanillaComponentCategory(String key, Set<String> paths) {
    }

    private record ComponentRow(ComponentRowType type, String id, String componentKey, String selectedKey,
                                ComponentValueKind valueKind, int depth) {
    }
}

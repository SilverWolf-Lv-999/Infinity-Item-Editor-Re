package io.github.seraphina.infinity_item_editor_re.client.screen.component;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.screen.CompatScreen;
import io.github.seraphina.infinity_item_editor_re.client.screen.FilteredEditBox;
import io.github.seraphina.infinity_item_editor_re.client.screen.InfinityEditorButton;
import io.github.seraphina.infinity_item_editor_re.client.screen.ItemEditorScreen;
import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;
import io.github.seraphina.infinity_item_editor_re.util.NbtCompat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SpecialComponentEditorScreen extends CompatScreen {
    private static final int BUTTON_HEIGHT = 20;
    private static final int FIELD_START_Y = 52;
    private static final int FIELD_ROW_HEIGHT = 27;
    private static final int STATUS_GOOD = 0xFF32CC64;
    private static final int STATUS_BAD = 0xFFF44262;
    private static final Pattern TEXT_COMPONENT_PATTERN = Pattern.compile("\"text\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    private final ItemEditorScreen thisLastScreen;
    private final SpecialComponentSelectionScreen thisSelectionScreen;
    private final ComponentEditorDefinition thisDefinition;
    private ItemStack thisEditingStack;
    private final Map<String, EditBox> thisTextBoxes = new HashMap<>();
    private final Map<String, String> thisTextValues = new HashMap<>();
    private final Map<String, Boolean> thisBooleanValues = new HashMap<>();
    private final Map<String, String> thisEnumValues = new HashMap<>();
    private Component thisStatus = Component.empty();
    private int thisStatusColor = STATUS_GOOD;
    private int thisFieldScroll;
    private boolean thisFormInitialized;

    SpecialComponentEditorScreen(ItemEditorScreen lastScreen, SpecialComponentSelectionScreen selectionScreen,
                                 ItemStack stack, ComponentEditorDefinition definition) {
        super(definition.displayName());
        this.thisLastScreen = lastScreen;
        this.thisSelectionScreen = selectionScreen;
        this.thisEditingStack = stack.copy();
        this.thisDefinition = definition;
    }

    @Override
    protected void init() {
        captureVisibleFieldValues();
        this.thisTextBoxes.clear();
        if (!this.thisFormInitialized) {
            loadValues(currentComponentValue());
            this.thisFormInitialized = true;
        }

        int formWidth = Math.min(520, Math.max(220, this.width - 32));
        int formX = (this.width - formWidth) / 2;
        int labelWidth = Math.min(180, Math.max(92, formWidth / 3));
        int fieldX = formX + labelWidth + 8;
        int fieldWidth = Math.max(100, formWidth - labelWidth - 8);
        int visibleRows = visibleFieldRows();
        int maxScroll = Math.max(0, this.thisDefinition.fields().size() - visibleRows);
        this.thisFieldScroll = Math.max(0, Math.min(this.thisFieldScroll, maxScroll));

        int first = this.thisFieldScroll;
        int end = Math.min(this.thisDefinition.fields().size(), first + visibleRows);
        for (int index = first; index < end; index++) {
            ComponentEditorField field = this.thisDefinition.fields().get(index);
            int y = FIELD_START_Y + (index - first) * FIELD_ROW_HEIGHT;
            addFieldWidget(field, fieldX, y, fieldWidth);
        }

        int buttonY = this.height - 28;
        int totalWidth = 72 * 4 + 12;
        int startX = (this.width - totalWidth) / 2;
        addRenderableWidget(new InfinityEditorButton(startX, buttonY, 72, BUTTON_HEIGHT,
                Component.translatable(key("back")), button -> returnToSelection()));
        addRenderableWidget(new InfinityEditorButton(startX + 76, buttonY, 72, BUTTON_HEIGHT,
                Component.translatable(key("special_components.default")), button -> resetToDefault()));
        addRenderableWidget(new InfinityEditorButton(startX + 152, buttonY, 72, BUTTON_HEIGHT,
                Component.translatable(key("special_components.remove")), button -> removeComponent()));
        addRenderableWidget(new InfinityEditorButton(startX + 228, buttonY, 72, BUTTON_HEIGHT,
                Component.translatable(key("special_components.apply")), button -> applyComponent()));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            returnToSelection();
            return true;
        }
        if (CompatScreen.hasControlDown() && keyCode == 83) {
            applyComponent();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0 || this.thisDefinition.fields().size() <= visibleFieldRows()) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        int maxScroll = Math.max(0, this.thisDefinition.fields().size() - visibleFieldRows());
        int nextScroll = Math.max(0, Math.min(maxScroll, this.thisFieldScroll - (int) Math.signum(scrollY)));
        if (nextScroll != this.thisFieldScroll) {
            this.thisFieldScroll = nextScroll;
            rebuildWidgets();
        }
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xF0101113);
        guiGraphics.fill(0, 0, this.width, 1, InfinityEditorButton.MAIN_COLOR);
        guiGraphics.centeredText(this.font,
                Component.translatable(key("special_components.editor"), this.thisDefinition.displayName()),
                this.width / 2, 12, InfinityEditorButton.MAIN_COLOR);
        guiGraphics.centeredText(this.font, Component.literal(this.thisDefinition.id()), this.width / 2, 28, 0xFFBFC9C4);

        int formWidth = Math.min(520, Math.max(220, this.width - 32));
        int formX = (this.width - formWidth) / 2;
        int first = this.thisFieldScroll;
        int end = Math.min(this.thisDefinition.fields().size(), first + visibleFieldRows());
        for (int index = first; index < end; index++) {
            ComponentEditorField field = this.thisDefinition.fields().get(index);
            int y = FIELD_START_Y + (index - first) * FIELD_ROW_HEIGHT;
            guiGraphics.text(this.font, Component.translatable(field.label()), formX, y + 6, 0xFFE5EDE8, false);
        }
        if (this.thisDefinition.fields().isEmpty()) {
            guiGraphics.centeredText(this.font, Component.translatable(key("special_components.marker_hint")),
                    this.width / 2, 72, 0xFFBFC9C4);
        } else if (this.thisDefinition.fields().size() > visibleFieldRows()) {
            String progress = (this.thisFieldScroll + 1) + "/" + Math.max(1, this.thisDefinition.fields().size() - visibleFieldRows() + 1);
            guiGraphics.text(this.font, Component.literal(progress), this.width - this.font.width(progress) - 8, 34, 0xFFBFC9C4, false);
        }

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        if (!this.thisStatus.getString().isEmpty()) {
            guiGraphics.centeredText(this.font, clippedStatus(), this.width / 2, this.height - 42, this.thisStatusColor);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void onClose() {
        returnToSelection();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void addFieldWidget(ComponentEditorField field, int fieldX, int y, int fieldWidth) {
        switch (field.kind()) {
            case BOOLEAN -> {
                boolean enabled = this.thisBooleanValues.getOrDefault(field.key(), Boolean.parseBoolean(field.defaultValue()));
                addRenderableWidget(new InfinityEditorButton(fieldX, y, fieldWidth, BUTTON_HEIGHT,
                        booleanMessage(field, enabled), button -> {
                            boolean next = !this.thisBooleanValues.getOrDefault(field.key(), false);
                            this.thisBooleanValues.put(field.key(), next);
                            button.setMessage(booleanMessage(field, next));
                        }));
            }
            case ENUM -> {
                String selected = this.thisEnumValues.getOrDefault(field.key(), field.defaultValue());
                addRenderableWidget(new InfinityEditorButton(fieldX, y, fieldWidth, BUTTON_HEIGHT,
                        enumMessage(field, selected), button -> {
                            String next = nextEnumValue(field, this.thisEnumValues.getOrDefault(field.key(), field.defaultValue()));
                            this.thisEnumValues.put(field.key(), next);
                            button.setMessage(enumMessage(field, next));
                        }));
            }
            default -> {
                FilteredEditBox box = addRenderableWidget(new FilteredEditBox(this.font, fieldX, y, fieldWidth,
                        BUTTON_HEIGHT, Component.translatable(field.label())));
                box.setMaxLength(4096);
                box.setValue(this.thisTextValues.getOrDefault(field.key(), field.defaultValue()));
                box.setResponder(value -> this.thisTextValues.put(field.key(), value == null ? "" : value));
                this.thisTextBoxes.put(field.key(), box);
            }
        }
    }

    private void applyComponent() {
        try {
            captureVisibleFieldValues();
            ItemStack updatedStack = replaceComponent(createComponentValue(currentComponentValue()));
            this.thisEditingStack = updatedStack;
            this.thisSelectionScreen.replaceEditingStack(updatedStack);
            this.thisLastScreen.applySpecialComponentEditedStack(updatedStack);
            this.thisStatus = Component.translatable(key("special_components.updated"), this.thisDefinition.displayName());
            this.thisStatusColor = STATUS_GOOD;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            this.thisStatus = Component.literal(exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage());
            this.thisStatusColor = STATUS_BAD;
        }
    }

    private void removeComponent() {
        try {
            ItemStack updatedStack = replaceComponent(null);
            this.thisEditingStack = updatedStack;
            this.thisSelectionScreen.replaceEditingStack(updatedStack);
            this.thisLastScreen.applySpecialComponentEditedStack(updatedStack);
            this.thisStatus = Component.translatable(key("special_components.removed"), this.thisDefinition.displayName());
            this.thisStatusColor = STATUS_GOOD;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            this.thisStatus = Component.literal(exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage());
            this.thisStatusColor = STATUS_BAD;
        }
    }

    private void resetToDefault() {
        this.thisTextBoxes.clear();
        loadValues(defaultComponentValue());
        this.thisFieldScroll = 0;
        rebuildWidgets();
    }

    private void returnToSelection() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.thisSelectionScreen);
        }
    }

    private void captureVisibleFieldValues() {
        for (Map.Entry<String, EditBox> entry : this.thisTextBoxes.entrySet()) {
            this.thisTextValues.put(entry.getKey(), entry.getValue().getValue());
        }
    }

    private void loadValues(Tag componentValue) {
        this.thisTextValues.clear();
        this.thisBooleanValues.clear();
        this.thisEnumValues.clear();
        for (ComponentEditorField field : this.thisDefinition.fields()) {
            String value = readFieldValue(componentValue, field);
            switch (field.kind()) {
                case BOOLEAN -> this.thisBooleanValues.put(field.key(), Boolean.parseBoolean(value));
                case ENUM -> this.thisEnumValues.put(field.key(), value.isBlank() ? field.defaultValue() : value);
                default -> this.thisTextValues.put(field.key(), value);
            }
        }
    }

    private int visibleFieldRows() {
        return Math.max(1, (this.height - 114 - FIELD_START_Y) / FIELD_ROW_HEIGHT);
    }

    private Tag currentComponentValue() {
        CompoundTag saved = ItemStackNbt.save(this.thisEditingStack);
        CompoundTag components = NbtCompat.getCompound(saved, "components");
        Tag value = components.get(this.thisDefinition.id());
        return value == null ? null : value.copy();
    }

    private ItemStack replaceComponent(Tag value) {
        CompoundTag saved = ItemStackNbt.save(this.thisEditingStack);
        CompoundTag components = NbtCompat.getCompound(saved, "components").copy();
        if (value == null) {
            components.remove(this.thisDefinition.id());
        } else {
            components.put(this.thisDefinition.id(), value);
        }
        saved.put("components", components);
        return ItemStackNbt.parseStrict(saved);
    }

    private Tag defaultComponentValue() {
        return switch (this.thisDefinition.shape()) {
            case MARKER -> new CompoundTag();
            case ROOT_COMPONENT_TEXT -> StringTag.valueOf(componentTextJson(this.thisDefinition.fields().get(0).defaultValue()));
            case ROOT_STRING -> StringTag.valueOf(this.thisDefinition.fields().get(0).defaultValue());
            case ROOT_INTEGER -> IntTag.valueOf(parseInteger(this.thisDefinition.fields().get(0).defaultValue(), fieldLabelText(this.thisDefinition.fields().get(0))));
            case ROOT_DECIMAL -> FloatTag.valueOf(parseDecimal(this.thisDefinition.fields().get(0).defaultValue(), fieldLabelText(this.thisDefinition.fields().get(0))));
            case ROOT_BOOLEAN -> ByteTag.valueOf((byte) (Boolean.parseBoolean(this.thisDefinition.fields().get(0).defaultValue()) ? 1 : 0));
            case ROOT_ITEM -> itemTag(this.thisDefinition.fields().get(0).defaultValue());
            case ROOT_ITEM_LIST -> itemListTag(this.thisDefinition.fields().get(0).defaultValue());
            case ROOT_STRING_LIST -> stringListTag(this.thisDefinition.fields().get(0).defaultValue(), false);
            case ROOT_TEXT_LIST -> stringListTag(this.thisDefinition.fields().get(0).defaultValue(), true);
            case ROOT_EFFECT_LIST -> effectListTag(this.thisDefinition.fields().get(0).defaultValue());
            case ROOT_PATTERN_LIST -> patternListTag(this.thisDefinition.fields().get(0).defaultValue());
            case ROOT_BEE_LIST -> beeListTag(this.thisDefinition.fields().get(0).defaultValue());
            case ROOT_STRING_MAP -> stringMapTag(this.thisDefinition.fields().get(0).defaultValue());
            case ROOT_SNBT_COMPOUND -> parseSnbtCompound(this.thisDefinition.fields().get(0).defaultValue(),
                    fieldLabelText(this.thisDefinition.fields().get(0)));
            case COMPOUND -> {
                CompoundTag value = new CompoundTag();
                for (ComponentEditorField field : this.thisDefinition.fields()) {
                    if (!field.optional() || !field.defaultValue().isBlank()) {
                        value.put(field.key(), createFieldTag(field, field.defaultValue()));
                    }
                }
                yield value;
            }
        };
    }

    private Tag createComponentValue(Tag existingValue) {
        if (this.thisDefinition.shape() == ComponentValueShape.MARKER) {
            return new CompoundTag();
        }

        if (this.thisDefinition.shape() == ComponentValueShape.COMPOUND) {
            CompoundTag value = existingValue instanceof CompoundTag compoundTag ? compoundTag.copy() : new CompoundTag();
            for (ComponentEditorField field : this.thisDefinition.fields()) {
                String fieldValue = editorValue(field);
                if (field.optional() && fieldValue.isBlank()) {
                    value.remove(field.key());
                } else {
                    value.put(field.key(), createFieldTag(field, fieldValue));
                }
            }
            return value;
        }

        ComponentEditorField field = this.thisDefinition.fields().get(0);
        String value = editorValue(field);
        return switch (this.thisDefinition.shape()) {
            case ROOT_COMPONENT_TEXT -> StringTag.valueOf(componentTextJson(value));
            case ROOT_STRING -> StringTag.valueOf(value);
            case ROOT_INTEGER -> IntTag.valueOf(parseInteger(value, fieldLabelText(field)));
            case ROOT_DECIMAL -> FloatTag.valueOf(parseDecimal(value, fieldLabelText(field)));
            case ROOT_BOOLEAN -> ByteTag.valueOf((byte) (Boolean.parseBoolean(value) ? 1 : 0));
            case ROOT_ITEM, ROOT_ITEM_LIST, ROOT_STRING_LIST, ROOT_TEXT_LIST, ROOT_EFFECT_LIST, ROOT_PATTERN_LIST,
                    ROOT_BEE_LIST, ROOT_STRING_MAP -> createFieldTag(field, value);
            case ROOT_SNBT_COMPOUND -> parseSnbtCompound(value, fieldLabelText(field));
            default -> throw new IllegalStateException("Unsupported component value shape");
        };
    }

    private Tag createFieldTag(ComponentEditorField field, String rawValue) {
        return switch (field.kind()) {
            case TEXT, IDENTIFIER, ENUM -> StringTag.valueOf(rawValue.trim());
            case TEXT_COMPONENT -> StringTag.valueOf(componentTextJson(rawValue));
            case INTEGER -> IntTag.valueOf(parseInteger(rawValue, fieldLabelText(field)));
            case LONG -> LongTag.valueOf(parseLong(rawValue, fieldLabelText(field)));
            case DECIMAL -> FloatTag.valueOf(parseDecimal(rawValue, fieldLabelText(field)));
            case BOOLEAN -> ByteTag.valueOf((byte) (Boolean.parseBoolean(rawValue) ? 1 : 0));
            case STRING_LIST -> stringListTag(rawValue, false);
            case TEXT_LIST -> stringListTag(rawValue, true);
            case INTEGER_LIST -> integerListTag(rawValue);
            case DECIMAL_LIST -> decimalListTag(rawValue);
            case BOOLEAN_LIST -> booleanListTag(rawValue);
            case ITEM -> itemTag(rawValue);
            case ITEM_LIST -> itemListTag(rawValue);
            case INTEGER_MAP -> integerMapTag(rawValue);
            case STRING_MAP -> stringMapTag(rawValue);
            case BLOCK_PREDICATE_LIST -> blockPredicateListTag(rawValue);
            case TOOL_RULE_LIST -> toolRuleListTag(rawValue);
            case EFFECT_LIST -> effectListTag(rawValue);
            case ATTRIBUTE_LIST -> attributeListTag(rawValue);
            case FIREWORK_LIST -> fireworkListTag(rawValue);
            case PATTERN_LIST -> patternListTag(rawValue);
            case BEE_LIST -> beeListTag(rawValue);
            case UUID -> uuidTag(rawValue);
            case SNBT_TAG -> parseSnbtTag(rawValue, fieldLabelText(field));
            case SNBT_COMPOUND -> parseSnbtCompound(rawValue, fieldLabelText(field));
            case SNBT_LIST -> parseSnbtList(rawValue, fieldLabelText(field));
        };
    }

    private String readFieldValue(Tag componentValue, ComponentEditorField field) {
        if (componentValue == null) {
            return field.defaultValue();
        }
        if (field.kind() == ComponentFieldKind.UUID && componentValue instanceof CompoundTag compoundTag
                && NbtCompat.hasUUID(compoundTag, field.key())) {
            return NbtCompat.getUUID(compoundTag, field.key()).toString();
        }

        Tag value = this.thisDefinition.shape() == ComponentValueShape.COMPOUND && componentValue instanceof CompoundTag compoundTag
                ? compoundTag.get(field.key()) : componentValue;
        if (value == null) {
            return field.defaultValue();
        }
        return switch (field.kind()) {
            case TEXT, IDENTIFIER, ENUM -> value instanceof StringTag stringTag ? stringTag.value() : NbtCompat.asString(value);
            case TEXT_COMPONENT -> componentText(value instanceof StringTag stringTag ? stringTag.value() : NbtCompat.asString(value));
            case INTEGER -> Integer.toString(value instanceof NumericTag numericTag ? numericTag.intValue() : 0);
            case LONG -> Long.toString(value instanceof NumericTag numericTag ? numericTag.longValue() : 0L);
            case DECIMAL -> Float.toString(value instanceof NumericTag numericTag ? numericTag.floatValue() : 0);
            case BOOLEAN -> Boolean.toString(value instanceof NumericTag numericTag && numericTag.byteValue() != 0);
            case STRING_LIST -> readStringList(value, false);
            case TEXT_LIST -> readStringList(value, true);
            case INTEGER_LIST -> readIntegerList(value);
            case DECIMAL_LIST -> readDecimalList(value);
            case BOOLEAN_LIST -> readBooleanList(value);
            case ITEM -> readItem(value);
            case ITEM_LIST -> readItemList(value);
            case INTEGER_MAP -> readIntegerMap(value);
            case STRING_MAP -> readStringMap(value);
            case BLOCK_PREDICATE_LIST -> readBlockPredicateList(value);
            case TOOL_RULE_LIST -> readToolRuleList(value);
            case EFFECT_LIST -> readEffectList(value);
            case ATTRIBUTE_LIST -> readAttributeList(value);
            case FIREWORK_LIST -> readFireworkList(value);
            case PATTERN_LIST -> readPatternList(value);
            case BEE_LIST -> readBeeList(value);
            case UUID -> "";
            case SNBT_TAG, SNBT_COMPOUND, SNBT_LIST -> snbt(value);
        };
    }

    private String editorValue(ComponentEditorField field) {
        return switch (field.kind()) {
            case BOOLEAN -> Boolean.toString(this.thisBooleanValues.getOrDefault(field.key(), Boolean.parseBoolean(field.defaultValue())));
            case ENUM -> this.thisEnumValues.getOrDefault(field.key(), field.defaultValue());
            default -> this.thisTextValues.getOrDefault(field.key(), field.defaultValue());
        };
    }

    private Component booleanMessage(ComponentEditorField field, boolean value) {
        return Component.translatable(key("special_components.field_value"), Component.translatable(field.label()), Component.translatable(key(value ? "special_components.on" : "special_components.off")));
    }

    private Component enumMessage(ComponentEditorField field, String value) {
        return Component.translatable(key("special_components.field_value"), Component.translatable(field.label()), value);
    }

    private String nextEnumValue(ComponentEditorField field, String current) {
        if (field.options().isEmpty()) {
            return current;
        }
        int index = field.options().indexOf(current);
        return field.options().get((index + 1 + field.options().size()) % field.options().size());
    }

    private Component clippedStatus() {
        int maxWidth = Math.max(40, this.width - 20);
        String text = this.thisStatus.getString();
        return this.font.width(text) <= maxWidth ? this.thisStatus
                : Component.literal(this.font.plainSubstrByWidth(text, Math.max(1, maxWidth - this.font.width("..."))) + "...");
    }

    private static int parseInteger(String value, String label) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw error("integer", label);
        }
    }

    private static long parseLong(String value, String label) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException exception) {
            throw error("long", label);
        }
    }

    private static float parseDecimal(String value, String label) {
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException exception) {
            throw error("decimal", label);
        }
    }

    private static Tag parseSnbtTag(String value, String label) {
        try {
            return NbtCompat.parseAnyTag(value.trim());
        } catch (CommandSyntaxException exception) {
            throw error("snbt", label);
        }
    }

    private static CompoundTag parseSnbtCompound(String value, String label) {
        Tag tag = parseSnbtTag(value, label);
        if (tag instanceof CompoundTag compoundTag) {
            return compoundTag;
        }
        throw error("snbt_compound", label);
    }

    private static ListTag parseSnbtList(String value, String label) {
        Tag tag = parseSnbtTag(value, label);
        if (tag instanceof ListTag listTag) {
            return listTag;
        }
        throw error("snbt_list", label);
    }

    private static String snbt(Tag tag) {
        return new SnbtPrinterTagVisitor("", 0, new ArrayList<>()).visit(tag);
    }

    private static List<String> splitEntries(String value, String delimiter) {
        List<String> entries = new ArrayList<>();
        if (value == null || value.isBlank()) {
            return entries;
        }
        for (String part : value.split(delimiter)) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                entries.add(trimmed);
            }
        }
        return entries;
    }

    private static ListTag stringListTag(String value, boolean textComponents) {
        ListTag list = new ListTag();
        for (String entry : splitEntries(value, textComponents ? "\\|" : ",")) {
            list.add(StringTag.valueOf(textComponents ? componentTextJson(entry) : entry));
        }
        return list;
    }

    private static ListTag integerListTag(String value) {
        ListTag list = new ListTag();
        for (String entry : splitEntries(value, ",")) {
            list.add(IntTag.valueOf(parseInteger(entry, labelText("list_value"))));
        }
        return list;
    }

    private static ListTag decimalListTag(String value) {
        ListTag list = new ListTag();
        for (String entry : splitEntries(value, ",")) {
            list.add(FloatTag.valueOf(parseDecimal(entry, labelText("list_value"))));
        }
        return list;
    }

    private static ListTag booleanListTag(String value) {
        ListTag list = new ListTag();
        for (String entry : splitEntries(value, ",")) {
            list.add(ByteTag.valueOf((byte) (Boolean.parseBoolean(entry) ? 1 : 0)));
        }
        return list;
    }

    private static CompoundTag itemTag(String value) {
        String[] parts = value.trim().split("\\*", 2);
        String id = parts.length == 0 || parts[0].isBlank() ? "minecraft:air" : parts[0].trim();
        int count = parts.length < 2 || parts[1].isBlank() ? 1 : parseInteger(parts[1], labelText("item_count"));
        CompoundTag item = new CompoundTag();
        item.putString("id", id);
        item.putInt("count", Math.max(1, count));
        return item;
    }

    private static ListTag itemListTag(String value) {
        ListTag list = new ListTag();
        for (String entry : splitEntries(value, ",")) {
            list.add(itemTag(entry));
        }
        return list;
    }

    private static CompoundTag integerMapTag(String value) {
        CompoundTag map = new CompoundTag();
        for (String entry : splitEntries(value, ",")) {
            String[] parts = entry.split(":", 3);
            if (parts.length < 2) {
                throw error("map_id_format");
            }
            String id = parts.length == 2 ? parts[0] : parts[0] + ":" + parts[1];
            String number = parts.length == 2 ? parts[1] : parts[2];
            map.putInt(id, parseInteger(number, labelText("map_value")));
        }
        return map;
    }

    private static CompoundTag stringMapTag(String value) {
        CompoundTag map = new CompoundTag();
        for (String entry : splitEntries(value, ",")) {
            String[] parts = entry.split("=", 2);
            if (parts.length != 2 || parts[0].isBlank()) {
                throw error("map_key_format");
            }
            map.putString(parts[0].trim(), parts[1].trim());
        }
        return map;
    }

    private static ListTag blockPredicateListTag(String value) {
        ListTag list = new ListTag();
        for (String entry : splitEntries(value, ",")) {
            CompoundTag predicate = new CompoundTag();
            predicate.putString("blocks", entry);
            list.add(predicate);
        }
        return list;
    }

    private static ListTag toolRuleListTag(String value) {
        ListTag list = new ListTag();
        for (String entry : splitEntries(value, ",")) {
            String[] parts = entry.split("\\|", -1);
            CompoundTag rule = new CompoundTag();
            rule.putString("blocks", parts[0].trim());
            rule.putFloat("speed", parts.length > 1 && !parts[1].isBlank() ? parseDecimal(parts[1], labelText("rule_speed")) : 1);
            rule.putBoolean("correct_for_drops", parts.length <= 2 || Boolean.parseBoolean(parts[2].trim()));
            list.add(rule);
        }
        return list;
    }

    private static ListTag effectListTag(String value) {
        ListTag list = new ListTag();
        for (String entry : splitEntries(value, ",")) {
            String[] parts = entry.split("\\|", -1);
            CompoundTag effect = new CompoundTag();
            effect.putString("id", parts[0].trim());
            effect.putInt("duration", parts.length > 1 && !parts[1].isBlank() ? parseInteger(parts[1], labelText("effect_duration")) : 200);
            effect.putInt("amplifier", parts.length > 2 && !parts[2].isBlank() ? parseInteger(parts[2], labelText("effect_amplifier")) : 0);
            effect.putFloat("probability", parts.length > 3 && !parts[3].isBlank() ? parseDecimal(parts[3], labelText("effect_probability")) : 1);
            list.add(effect);
        }
        return list;
    }

    private static ListTag attributeListTag(String value) {
        ListTag list = new ListTag();
        for (String entry : splitEntries(value, ",")) {
            String[] parts = entry.split("\\|", -1);
            if (parts.length < 2) {
                throw error("attribute_format");
            }
            CompoundTag modifier = new CompoundTag();
            modifier.putString("type", parts[0].trim());
            modifier.putDouble("amount", parseDecimal(parts[1], labelText("attribute_amount")));
            modifier.putString("operation", parts.length > 2 && !parts[2].isBlank() ? parts[2].trim() : "add_value");
            if (parts.length > 3 && !parts[3].isBlank()) {
                modifier.putString("slot", parts[3].trim());
            }
            list.add(modifier);
        }
        return list;
    }

    private static ListTag fireworkListTag(String value) {
        ListTag list = new ListTag();
        for (String entry : splitEntries(value, ",")) {
            String[] parts = entry.split("\\|", -1);
            CompoundTag explosion = new CompoundTag();
            explosion.putString("shape", parts[0].trim());
            explosion.put("colors", integerListTag(parts.length > 1 ? parts[1] : ""));
            explosion.put("fade_colors", integerListTag(parts.length > 2 ? parts[2] : ""));
            explosion.putBoolean("has_trail", parts.length > 3 && Boolean.parseBoolean(parts[3].trim()));
            explosion.putBoolean("has_twinkle", parts.length > 4 && Boolean.parseBoolean(parts[4].trim()));
            list.add(explosion);
        }
        return list;
    }

    private static ListTag patternListTag(String value) {
        ListTag list = new ListTag();
        for (String entry : splitEntries(value, ",")) {
            String[] parts = entry.split(":", 3);
            if (parts.length < 2) {
                throw error("pattern_format");
            }
            String pattern = parts.length == 2 ? parts[0] : parts[0] + ":" + parts[1];
            String color = parts.length == 2 ? parts[1] : parts[2];
            CompoundTag layer = new CompoundTag();
            layer.putString("pattern", pattern);
            layer.putString("color", color);
            list.add(layer);
        }
        return list;
    }

    private static ListTag beeListTag(String value) {
        ListTag list = new ListTag();
        for (String entry : splitEntries(value, ",")) {
            String[] parts = entry.split("\\|", -1);
            CompoundTag bee = new CompoundTag();
            CompoundTag entityData = new CompoundTag();
            entityData.putString("id", parts[0].trim());
            bee.put("entity_data", entityData);
            bee.putInt("ticks_in_hive", parts.length > 1 && !parts[1].isBlank() ? parseInteger(parts[1], labelText("bee_time")) : 0);
            bee.putInt("min_ticks_in_hive", parts.length > 2 && !parts[2].isBlank() ? parseInteger(parts[2], labelText("bee_min_time")) : 600);
            list.add(bee);
        }
        return list;
    }

    private static Tag uuidTag(String value) {
        UUID uuid;
        try {
            uuid = UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw error("uuid_format");
        }
        CompoundTag holder = new CompoundTag();
        NbtCompat.putUUID(holder, "value", uuid);
        return holder.get("value");
    }

    private static String readStringList(Tag value, boolean textComponents) {
        if (!(value instanceof ListTag list)) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            String entry = NbtCompat.getString(list, index);
            entries.add(textComponents ? componentText(entry) : entry);
        }
        return String.join(textComponents ? "|" : ",", entries);
    }

    private static String readIntegerList(Tag value) {
        if (!(value instanceof ListTag list)) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            entries.add(Integer.toString(NbtCompat.getInt(list, index)));
        }
        return String.join(",", entries);
    }

    private static String readDecimalList(Tag value) {
        if (!(value instanceof ListTag list)) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            entries.add(Float.toString(NbtCompat.getFloat(list, index)));
        }
        return String.join(",", entries);
    }

    private static String readBooleanList(Tag value) {
        if (!(value instanceof ListTag list)) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            Tag entry = list.get(index);
            entries.add(Boolean.toString(entry instanceof NumericTag numericTag && numericTag.byteValue() != 0));
        }
        return String.join(",", entries);
    }

    private static String readItem(Tag value) {
        if (!(value instanceof CompoundTag item)) {
            return "";
        }
        String id = NbtCompat.getString(item, "id");
        int count = NbtCompat.getInt(item, "count");
        return id.isBlank() ? "" : id + "*" + Math.max(1, count);
    }

    private static String readItemList(Tag value) {
        if (!(value instanceof ListTag list)) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            entries.add(readItem(NbtCompat.getCompound(list, index)));
        }
        return String.join(",", entries);
    }

    private static String readIntegerMap(Tag value) {
        if (!(value instanceof CompoundTag map)) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (String key : map.keySet()) {
            entries.add(key + ":" + NbtCompat.getInt(map, key));
        }
        return String.join(",", entries);
    }

    private static String readStringMap(Tag value) {
        if (!(value instanceof CompoundTag map)) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (String key : map.keySet()) {
            Tag entry = map.get(key);
            entries.add(key + "=" + (entry == null ? "" : NbtCompat.asString(entry)));
        }
        return String.join(",", entries);
    }

    private static String readBlockPredicateList(Tag value) {
        if (!(value instanceof ListTag list)) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            entries.add(NbtCompat.getString(NbtCompat.getCompound(list, index), "blocks"));
        }
        return String.join(",", entries);
    }

    private static String readToolRuleList(Tag value) {
        if (!(value instanceof ListTag list)) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            CompoundTag rule = NbtCompat.getCompound(list, index);
            entries.add(NbtCompat.getString(rule, "blocks") + "|" + NbtCompat.getFloat(rule, "speed") + "|"
                    + NbtCompat.getBoolean(rule, "correct_for_drops"));
        }
        return String.join(",", entries);
    }

    private static String readEffectList(Tag value) {
        if (!(value instanceof ListTag list)) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            CompoundTag effect = NbtCompat.getCompound(list, index);
            entries.add(NbtCompat.getString(effect, "id") + "|" + NbtCompat.getInt(effect, "duration") + "|"
                    + NbtCompat.getInt(effect, "amplifier") + "|" + NbtCompat.getFloat(effect, "probability"));
        }
        return String.join(",", entries);
    }

    private static String readAttributeList(Tag value) {
        if (!(value instanceof ListTag list)) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            CompoundTag modifier = NbtCompat.getCompound(list, index);
            entries.add(NbtCompat.getString(modifier, "type") + "|" + NbtCompat.getDouble(modifier, "amount") + "|"
                    + NbtCompat.getString(modifier, "operation") + "|" + NbtCompat.getString(modifier, "slot"));
        }
        return String.join(",", entries);
    }

    private static String readFireworkList(Tag value) {
        if (!(value instanceof ListTag list)) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            CompoundTag explosion = NbtCompat.getCompound(list, index);
            entries.add(NbtCompat.getString(explosion, "shape") + "|" + readIntegerList(explosion.get("colors")) + "|"
                    + readIntegerList(explosion.get("fade_colors")) + "|" + NbtCompat.getBoolean(explosion, "has_trail") + "|"
                    + NbtCompat.getBoolean(explosion, "has_twinkle"));
        }
        return String.join(",", entries);
    }

    private static String readPatternList(Tag value) {
        if (!(value instanceof ListTag list)) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            CompoundTag pattern = NbtCompat.getCompound(list, index);
            entries.add(NbtCompat.getString(pattern, "pattern") + ":" + NbtCompat.getString(pattern, "color"));
        }
        return String.join(",", entries);
    }

    private static String readBeeList(Tag value) {
        if (!(value instanceof ListTag list)) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            CompoundTag bee = NbtCompat.getCompound(list, index);
            CompoundTag entityData = NbtCompat.getCompound(bee, "entity_data");
            entries.add(NbtCompat.getString(entityData, "id") + "|" + NbtCompat.getInt(bee, "ticks_in_hive") + "|"
                    + NbtCompat.getInt(bee, "min_ticks_in_hive"));
        }
        return String.join(",", entries);
    }

    private static String componentText(String value) {
        Matcher matcher = TEXT_COMPONENT_PATTERN.matcher(value == null ? "" : value);
        if (!matcher.find()) {
            return value == null ? "" : value;
        }
        return matcher.group(1).replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\");
    }

    private static String componentTextJson(String value) {
        String escaped = (value == null ? "" : value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
        return "{\"text\":\"" + escaped + "\"}";
    }

    private Component fieldLabel(ComponentEditorField field) {
        return Component.translatable(field.label());
    }

    private String fieldLabelText(ComponentEditorField field) {
        return fieldLabel(field).getString();
    }

    private static String labelText(String suffix) {
        return Component.translatable(key("special_components.label." + suffix)).getString();
    }

    private static IllegalArgumentException error(String suffix, Object... args) {
        return new IllegalArgumentException(Component.translatable(key("special_components.error." + suffix), args).getString());
    }
    private static String key(String suffix) {
        return "screen." + ModSource.MODID + "." + suffix;
    }
}

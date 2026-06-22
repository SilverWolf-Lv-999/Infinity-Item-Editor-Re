package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import io.github.seraphina.infinity_item_editor_re.util.CompatRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
final class ItemJsonEditorScreen extends CompatScreen {
    private static final int BUTTON_WIDTH = 78;
    private static final int BUTTON_HEIGHT = 20;
    private static final int EDITOR_MARGIN = 18;
    private static final int EDITOR_TOP = 34;
    private static final int EDITOR_BOTTOM_MARGIN = 44;
    private static final int STATUS_GOOD = 0xFF32CC64;
    private static final int STATUS_BAD = 0xFFF44262;
    private static final int STATUS_NEUTRAL = 0xFFFFD966;

    private final ItemEditorScreen lastScreen;
    private String jsonText;
    private String appliedJsonText;
    private JsonCodeEditBox jsonBox;
    private Component status = Component.empty();
    private int statusColor = STATUS_NEUTRAL;
    private boolean discardArmed;

    ItemJsonEditorScreen(ItemEditorScreen lastScreen, ItemStack stack) {
        super(Component.translatable(key("json")));
        this.lastScreen = lastScreen;
        this.jsonText = ItemJsonConverter.toJson(stack);
        this.appliedJsonText = this.jsonText;
    }

    @Override
    protected void init() {
        int editorWidth = Math.max(180, this.width - EDITOR_MARGIN * 2);
        int editorHeight = Math.max(80, this.height - EDITOR_TOP - EDITOR_BOTTOM_MARGIN);
        this.jsonBox = addRenderableWidget(new JsonCodeEditBox(this.font, EDITOR_MARGIN, EDITOR_TOP,
                editorWidth, editorHeight, Component.translatable(key("json.placeholder"))));
        this.jsonBox.setValue(this.jsonText);
        this.jsonBox.setValueListener(value -> {
            this.jsonText = value;
            this.discardArmed = false;
        });
        setFocused(this.jsonBox);
        this.jsonBox.setFocused(true);

        int buttonY = this.height - 28;
        int totalWidth = BUTTON_WIDTH * 4 + 12;
        int x = (this.width - totalWidth) / 2;
        addRenderableWidget(new InfinityEditorButton(x, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable(key("back")), button -> returnToLastScreen()));
        addRenderableWidget(new InfinityEditorButton(x + BUTTON_WIDTH + 4, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable(key("json.format")), button -> formatJson()));
        addRenderableWidget(new InfinityEditorButton(x + (BUTTON_WIDTH + 4) * 2, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable(key("reset")), button -> resetJson()));
        addRenderableWidget(new InfinityEditorButton(x + (BUTTON_WIDTH + 4) * 3, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable(key("json.apply")), button -> applyJson(false)));
    }

    @Override
    public void tick() {
        if (this.jsonBox != null) {
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && this.jsonBox != null && this.jsonBox.closeCompletions()) {
            return true;
        }
        if (keyCode == 256) {
            returnToLastScreen();
            return true;
        }
        if (isInventoryKey(keyCode, scanCode) && !isJsonBoxFocused()) {
            returnToLastScreen();
            return true;
        }
        if (CompatScreen.hasControlDown() && CompatScreen.hasAltDown() && keyCode == 76) {
            formatJson();
            return true;
        }
        if (CompatScreen.hasControlDown() && keyCode == 83) {
            applyJson(false);
            return true;
        }
        if (CompatScreen.hasControlDown() && keyCode == 70) {
            formatJson();
            return true;
        }
        if ((keyCode == 257 || keyCode == 335) && CompatScreen.hasControlDown()) {
            applyJson(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        EditorBackgrounds.render(guiGraphics, this.width, this.height);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, InfinityEditorButton.MAIN_COLOR);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (!this.status.getString().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, clippedStatus(), this.width / 2, this.height - 40, this.statusColor);
        }
        if (this.jsonBox != null) {
            this.jsonBox.renderCompletions(guiGraphics);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void onClose() {
        returnToLastScreen();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isInventoryKey(int keyCode, int scanCode) {
        return this.minecraft != null
                && this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(new net.minecraft.client.input.KeyEvent(keyCode, scanCode, 0)));
    }

    private boolean isJsonBoxFocused() {
        return this.jsonBox != null && this.jsonBox.isFocused();
    }

    private void applyJson(boolean returnAfterApply) {
        try {
            ItemStack stack = ItemJsonConverter.fromJson(this.jsonBox == null ? this.jsonText : this.jsonBox.getValue());
            this.lastScreen.applyJsonEditedStack(stack);
            this.status = Component.translatable(messageKey("editor_json_applied"), stack.getHoverName());
            this.statusColor = STATUS_GOOD;
            this.jsonText = ItemJsonConverter.toJson(stack);
            this.appliedJsonText = this.jsonText;
            this.discardArmed = false;
            if (this.jsonBox != null) {
                this.jsonBox.setValue(this.jsonText);
                this.jsonBox.clearError();
            }
            if (returnAfterApply) {
                returnToLastScreen();
            }
        } catch (JsonParseException | IllegalStateException | NumberFormatException exception) {
            showInvalidJson(exception);
        }
    }

    private void formatJson() {
        try {
            this.jsonText = ItemJsonConverter.format(this.jsonBox == null ? this.jsonText : this.jsonBox.getValue());
            if (this.jsonBox != null) {
                this.jsonBox.setValue(this.jsonText);
                this.jsonBox.clearError();
            }
            this.status = Component.translatable(messageKey("editor_json_formatted"));
            this.statusColor = STATUS_GOOD;
        } catch (JsonParseException exception) {
            showInvalidJson(exception);
        }
    }

    private void resetJson() {
        this.jsonText = ItemJsonConverter.toJson(this.lastScreen.previewStack);
        this.appliedJsonText = this.jsonText;
        this.discardArmed = false;
        if (this.jsonBox != null) {
            this.jsonBox.setValue(this.jsonText);
            this.jsonBox.clearError();
        }
        this.status = Component.translatable(messageKey("editor_json_reset"));
        this.statusColor = STATUS_NEUTRAL;
    }

    private void returnToLastScreen() {
        if (isDirty() && !this.discardArmed) {
            this.discardArmed = true;
            this.status = Component.translatable(messageKey("editor_json_unsaved"));
            this.statusColor = STATUS_NEUTRAL;
            if (this.jsonBox != null) {
                setFocused(this.jsonBox);
                this.jsonBox.setFocused(true);
            }
            return;
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen);
        }
    }

    private boolean isDirty() {
        String current = this.jsonBox == null ? this.jsonText : this.jsonBox.getValue();
        return !current.equals(this.appliedJsonText);
    }

    private Component clippedStatus() {
        int maxWidth = Math.max(40, this.width - 20);
        String text = this.status.getString();
        if (this.font.width(text) <= maxWidth) {
            return this.status;
        }
        return Component.literal(this.font.plainSubstrByWidth(text, maxWidth - this.font.width("...")) + "...");
    }

    private void showInvalidJson(Exception exception) {
        JsonErrorLocation location = JsonErrorLocation.from(exception.getMessage());
        if (location == null) {
            this.status = Component.translatable(messageKey("editor_invalid_json"), exception.getMessage());
        } else {
            this.status = Component.translatable(messageKey("editor_invalid_json_at"),
                    location.line(), location.column(), exception.getMessage());
            if (this.jsonBox != null) {
                this.jsonBox.moveCursorToLineColumn(location.line(), location.column());
                this.jsonBox.setErrorLine(location.line() - 1);
            }
        }
        this.statusColor = STATUS_BAD;
    }

    private static String key(String suffix) {
        return "screen." + ModSource.MODID + "." + suffix;
    }

    private static String messageKey(String suffix) {
        return "message." + ModSource.MODID + "." + suffix;
    }

    private record JsonErrorLocation(int line, int column) {
        private static final Pattern LINE_COLUMN = Pattern.compile("line (\\d+) column (\\d+)", Pattern.CASE_INSENSITIVE);

        private static JsonErrorLocation from(String message) {
            if (message == null) {
                return null;
            }
            Matcher matcher = LINE_COLUMN.matcher(message);
            if (!matcher.find()) {
                return null;
            }
            return new JsonErrorLocation(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        }
    }

    static final class JsonCodeEditBox extends AbstractTextAreaWidget {
        private static final int LINE_HEIGHT = 10;
        private static final int LINE_NUMBER_WIDTH = 34;
        private static final int COMPLETION_WIDTH = 176;
        private static final int COMPLETION_ROW_HEIGHT = 12;
        private static final int MAX_COMPLETIONS = 10;
        private static final String INDENT = "  ";
        private static final int COLOR_BACKGROUND = 0xF0101117;
        private static final int COLOR_LINE_NUMBER = 0xFF697184;
        private static final int COLOR_TEXT = 0xFFE7EAF0;
        private static final int COLOR_KEY = 0xFF6CB6FF;
        private static final int COLOR_STRING = 0xFF93D977;
        private static final int COLOR_NUMBER = 0xFFFFB86C;
        private static final int COLOR_LITERAL = 0xFFC792EA;
        private static final int COLOR_PUNCTUATION = 0xFFB6BDC9;
        private static final int COLOR_SELECTION = 0x663F6BC6;
        private static final int COLOR_CURSOR = 0xFFE7EAF0;
        private static final int COLOR_COMPLETION_BACKGROUND = 0xF01A1E28;
        private static final int COLOR_COMPLETION_SELECTED = 0xFF243A55;
        private static final int COLOR_ERROR_LINE = 0x44F44262;

        private static final List<String> KEY_COMPLETIONS = List.of(
                "id", "Count", "tag", "Damage", "display", "Name", "Lore", "HideFlags", "Unbreakable",
                "Enchantments", "StoredEnchantments", "lvl", "AttributeModifiers", "AttributeName", "Amount", "Operation",
                "UUID", "Slot", "RepairCost", "CanDestroy", "CanPlaceOn", "CustomPotionEffects", "CustomPotionColor",
                "Potion", "Ambient", "Amplifier", "Duration", "ShowIcon", "ShowParticles", "BlockEntityTag",
                "BlockStateTag", "EntityTag", "SkullOwner", "Properties", "textures", "Value", "Signature",
                "Fireworks", "Flight", "Explosions", "Explosion", "Type", "Colors", "FadeColors", "Flicker",
                "Trail", "Items", "Slot", "tag", "Offers", "Recipes", "buy", "buyB", "sell", "uses", "maxUses",
                "rewardExp", "xp", "priceMultiplier", "specialPrice", "demand", "Base", "Patterns", "Pattern", "Color"
        );
        private static final List<String> ROOT_KEY_COMPLETIONS = List.of("id", "Count", "tag");
        private static final List<String> DISPLAY_KEY_COMPLETIONS = List.of("Name", "Lore", "color", "italic", "bold",
                "underlined", "strikethrough", "obfuscated");
        private static final List<String> ENCHANTMENT_KEY_COMPLETIONS = List.of("id", "lvl");
        private static final List<String> ATTRIBUTE_KEY_COMPLETIONS = List.of("AttributeName", "Name", "Amount",
                "Operation", "UUID", "Slot");
        private static final List<String> POTION_EFFECT_KEY_COMPLETIONS = List.of("Id", "Amplifier", "Duration",
                "Ambient", "ShowParticles", "ShowIcon");
        private static final List<String> FIREWORK_KEY_COMPLETIONS = List.of("Flight", "Explosions", "Type", "Colors",
                "FadeColors", "Flicker", "Trail");
        private static final List<String> TRADE_KEY_COMPLETIONS = List.of("buy", "buyB", "sell", "uses", "maxUses",
                "rewardExp", "xp", "priceMultiplier", "specialPrice", "demand");
        private static final List<String> VALUE_LITERALS = List.of("true", "false", "0", "1", "{}", "[]");
        private static final List<String> SLOT_VALUES = List.of("mainhand", "offhand", "head", "chest", "legs", "feet");
        private static final List<String> COMMAND_NAMES = List.of(
                "advancement", "attribute", "ban", "ban-ip", "banlist", "bossbar", "clear", "clone", "damage",
                "data", "datapack", "debug", "defaultgamemode", "deop", "difficulty", "effect", "enchant",
                "execute", "experience", "fill", "forceload", "function", "gamemode", "gamerule", "give",
                "help", "item", "jfr", "kick", "kill", "list", "locate", "loot", "me", "msg", "op", "pardon",
                "pardon-ip", "particle", "perf", "place", "playsound", "publish", "recipe", "reload", "return",
                "ride", "save-all", "save-off", "save-on", "say", "schedule", "scoreboard", "seed", "setblock",
                "setidletimeout", "setworldspawn", "spawnpoint", "spectate", "spreadplayers", "stop", "stopsound",
                "summon", "tag", "team", "teammsg", "teleport", "tell", "tellraw", "time", "title", "tm", "tp",
                "trigger", "weather", "whitelist", "worldborder", "xp"
        );
        private static final List<String> TARGET_SELECTORS = List.of("@p", "@a", "@r", "@s", "@e");
        private static final List<String> EXECUTE_SUBCOMMANDS = List.of("as", "at", "positioned", "rotated",
                "facing", "align", "anchored", "in", "if", "unless", "store", "run");
        private static final List<String> EXECUTE_CONDITIONS = List.of("block", "blocks", "data", "entity",
                "predicate", "score");
        private static final List<String> EXECUTE_STORE_TARGETS = List.of("result", "success");
        private static final List<String> EXECUTE_STORE_DESTINATIONS = List.of("block", "bossbar", "entity", "score",
                "storage");
        private static final List<String> DATA_ACTIONS = List.of("get", "merge", "modify", "remove");
        private static final List<String> DATA_TARGETS = List.of("block", "entity", "storage");
        private static final List<String> DATA_MODIFY_ACTIONS = List.of("append", "insert", "merge", "prepend", "set");
        private static final List<String> DATA_SOURCES = List.of("from", "string", "value");
        private static final List<String> DATA_PATHS = List.of("Command", "SuccessCount", "LastOutput", "Items",
                "Inventory", "SelectedItem", "ArmorItems", "HandItems", "Pos", "Rotation", "Motion", "Health",
                "CustomName", "Offers.Recipes", "BlockEntityTag", "EntityTag");
        private static final List<String> COORDINATE_VALUES = List.of("~ ~ ~", "~ ~1 ~", "^ ^ ^", "~", "0 0 0");
        private static final List<String> COORDINATE_COMPONENT_VALUES = List.of("~", "~1", "~-1", "^", "^1", "0");
        private static final List<String> ROTATION_VALUES = List.of("~ ~", "0 0", "~", "0");
        private static final List<String> DIMENSION_VALUES = List.of("minecraft:overworld", "minecraft:the_nether",
                "minecraft:the_end");
        private static final List<String> NBT_VALUES = List.of("{}", "[]", "0", "1", "true", "false", "\"text\"",
                "{CustomName:'{\"text\":\"Name\"}'}", "{display:{Name:'{\"text\":\"Name\"}'}}");
        private static final List<String> EFFECT_DURATIONS = List.of("1", "10", "30", "60", "999999");
        private static final List<String> LEVEL_VALUES = List.of("0", "1", "2", "3", "4", "5", "10", "255");
        private static final List<String> COUNT_VALUES = List.of("1", "16", "32", "64");
        private static final List<String> BOOLEAN_VALUES = List.of("true", "false");
        private static final List<String> BLOCK_SET_MODES = List.of("destroy", "keep", "replace");
        private static final List<String> FILL_MODES = List.of("destroy", "hollow", "keep", "outline", "replace");
        private static final List<String> BLOCKS_SCAN_MODES = List.of("all", "masked");
        private static final List<String> DATA_STORAGE_VALUES = List.of("minecraft:storage", "infinity:storage");
        private static final List<String> SCORE_OPERATIONS = List.of("matches", "=", "<", "<=", ">", ">=", "><");
        private static final List<String> STORE_TYPES = List.of("byte", "short", "int", "long", "float", "double");
        private static final List<String> BOSSBAR_VALUES = List.of("value", "max");
        private static final List<String> COMMAND_SNIPPETS = List.of(
                "say hello",
                "give @p minecraft:diamond 64",
                "tp @p ~ ~1 ~",
                "effect give @p minecraft:speed 30 1 true",
                "summon minecraft:pig ~ ~1 ~",
                "setblock ~ ~-1 ~ minecraft:gold_block",
                "fill ~-1 ~ ~-1 ~1 ~ ~1 minecraft:glass",
                "data get entity @s SelectedItem",
                "data modify block ~ ~ ~ Command set value \"say hello\"",
                "execute as @e[type=minecraft:zombie,limit=1] run say found"
        );
        private static final List<String> ITEM_IDS = registryKeys(CompatRegistries.ITEMS.getValues());
        private static final List<String> BLOCK_IDS = registryKeys(CompatRegistries.BLOCKS.getValues());
        private static final List<String> ENCHANTMENT_IDS = registryKeys(CompatRegistries.ENCHANTMENTS.getValues());
        private static final List<String> ATTRIBUTE_IDS = registryKeys(CompatRegistries.ATTRIBUTES.getValues());
        private static final List<String> ENTITY_IDS = registryKeys(CompatRegistries.ENTITY_TYPES.getValues());
        private static final List<String> EFFECT_IDS = registryKeys(CompatRegistries.MOB_EFFECTS.getValues());

        private final Font font;
        private final Component hint;
        private final List<Completion> completions = new ArrayList<>();
        private List<String> rootKeyCompletions = ROOT_KEY_COMPLETIONS;
        private boolean commandMode;
        private String text = "";
        private Consumer<String> valueListener = value -> {
        };
        private int cursor;
        private int selectionAnchor = -1;
        private int errorLine = -1;
        private int ticks;
        private int selectedCompletion;
        private boolean completionSelectionArmed;
        private boolean draggingSelection;

        JsonCodeEditBox(Font font, int x, int y, int width, int height, Component hint) {
            super(x, y, width, height, hint);
            this.font = font;
            this.hint = hint;
        }

        void useCommandCompletions() {
            this.commandMode = true;
            rebuildCompletions();
        }

        void setValueListener(Consumer<String> valueListener) {
            this.valueListener = valueListener == null ? value -> {
            } : valueListener;
        }

        void setValue(String value) {
            this.text = value == null ? "" : value;
            this.cursor = 0;
            clearSelection();
            notifyValueChanged();
            this.selectedCompletion = 0;
            rebuildCompletions();
        }

        String getValue() {
            return this.text;
        }

        boolean closeCompletions() {
            if (this.completions.isEmpty()) {
                return false;
            }
            this.completions.clear();
            this.completionSelectionArmed = false;
            return true;
        }

        void clearError() {
            this.errorLine = -1;
        }

        void setErrorLine(int line) {
            this.errorLine = Math.max(-1, line);
        }

        void moveCursorToLineColumn(int line, int column) {
            List<TextLine> lines = lineViews();
            if (lines.isEmpty()) {
                moveCursorTo(0, false);
                return;
            }
            TextLine view = lines.get(Mth.clamp(line - 1, 0, lines.size() - 1));
            moveCursorTo(view.beginIndex() + Mth.clamp(column - 1, 0, view.endIndex() - view.beginIndex()), false);
        }

        void tick() {
            this.ticks++;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            return mouseClicked(event.x(), event.y(), event.button());
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.visible || !this.active) {
                return false;
            }
            if (button == 0 && clickCompletion(mouseX, mouseY)) {
                return true;
            }
            boolean handledByScroll = super.mouseClicked(mouseEvent(mouseX, mouseY, button), false);
            if (withinContentAreaPoint(mouseX, mouseY) && button == 0) {
                setFocused(true);
                seekCursor(mouseX, mouseY, CompatScreen.hasShiftDown());
                this.draggingSelection = true;
                rebuildCompletions();
                return true;
            }
            setFocused(false);
            return handledByScroll;
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
            return mouseDragged(event.x(), event.y(), event.button(), dragX, dragY);
        }

        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (super.mouseDragged(mouseEvent(mouseX, mouseY, button), dragX, dragY)) {
                return true;
            }
            if (this.draggingSelection && button == 0 && isFocused()) {
                seekCursor(mouseX, mouseY, true);
                rebuildCompletions();
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            return mouseReleased(event.x(), event.y(), event.button());
        }

        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            this.draggingSelection = false;
            return super.mouseReleased(mouseEvent(mouseX, mouseY, button));
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            return keyPressed(event.key(), event.scancode(), event.modifiers());
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!isFocused()) {
                return false;
            }
            if (keyCode == 256 && closeCompletions()) {
                return true;
            }
            if (keyCode == 258) {
                if (!CompatScreen.hasShiftDown() && this.completionSelectionArmed && applySelectedCompletion()) {
                    return true;
                }
                if (CompatScreen.hasShiftDown()) {
                    outdentSelectedLines();
                } else {
                    indentSelectedLines();
                }
                return true;
            }
            if (CompatScreen.hasControlDown() && keyCode == 32) {
                rebuildCompletions(true);
                this.completionSelectionArmed = !this.completions.isEmpty();
                return true;
            }
            if (!this.completions.isEmpty() && keyCode == 264) {
                this.selectedCompletion = (this.selectedCompletion + 1) % this.completions.size();
                this.completionSelectionArmed = true;
                return true;
            }
            if (!this.completions.isEmpty() && keyCode == 265) {
                this.selectedCompletion = (this.selectedCompletion + this.completions.size() - 1) % this.completions.size();
                this.completionSelectionArmed = true;
                return true;
            }
            if (!this.completions.isEmpty() && this.completionSelectionArmed && (keyCode == 257 || keyCode == 335)) {
                return applySelectedCompletion();
            }
            return handleEditKey(keyCode);
        }

        @Override
        public boolean charTyped(CharacterEvent event) {
            return charTyped((char) event.codepoint(), event.modifiers());
        }

        public boolean charTyped(char codePoint, int modifiers) {
            if (!isFocused() || !StringUtil.isAllowedChatCharacter(codePoint)) {
                return false;
            }
            normalizeCursorAndSelection();
            if (codePoint == '"') {
                if (this.cursor < this.text.length() && this.text.charAt(this.cursor) == '"' && !hasSelection()) {
                    moveCursorTo(this.cursor + 1, false);
                    return true;
                }
                if (isEscapedQuoteAtCursor() || isInsideString(this.cursor)) {
                    insertText("\"");
                    return true;
                }
                insertPairedText("\"", "\"");
                return true;
            }
            if (codePoint == '{') {
                insertPairedText("{", "}");
                return true;
            }
            if (codePoint == '[') {
                insertPairedText("[", "]");
                return true;
            }
            if (codePoint == '}' || codePoint == ']') {
                if (this.cursor < this.text.length() && this.text.charAt(this.cursor) == codePoint && !hasSelection()) {
                    moveCursorTo(this.cursor + 1, false);
                    return true;
                }
                insertClosingBracket(codePoint);
                return true;
            }
            insertText(Character.toString(codePoint));
            return true;
        }

        private static MouseButtonEvent mouseEvent(double mouseX, double mouseY, int button) {
            return new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0));
        }

        @Override
        protected int getInnerHeight() {
            return Math.max(LINE_HEIGHT, lineViews().size() * LINE_HEIGHT);
        }

        @Override
        protected double scrollRate() {
            return LINE_HEIGHT;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            defaultButtonNarrationText(narrationElementOutput);
        }

        @Override
        protected void renderBackground(GuiGraphics guiGraphics) {
            guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), InfinityEditorButton.MAIN_COLOR);
            guiGraphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, COLOR_BACKGROUND);
        }

        @Override
        protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (this.text.isEmpty() && !isFocused()) {
                guiGraphics.drawString(this.font, this.hint, getTextX(), getY() + innerPadding(), 0xFF697184, false);
                return;
            }

            int line = 0;
            TextLine selected = selectedLine();
            for (TextLine view : lineViews()) {
                int y = getY() + innerPadding() + line * LINE_HEIGHT;
                if (lineVisible(y)) {
                    renderLine(guiGraphics, this.text, view, selected, line, y);
                }
                line++;
            }

            renderCursor(guiGraphics, this.text);
        }

        void renderCompletions(GuiGraphics guiGraphics) {
            if (!isFocused() || this.completions.isEmpty()) {
                return;
            }
            int x = Mth.clamp(getCursorScreenX(), getX(), getX() + getWidth() - COMPLETION_WIDTH - 8);
            int y = getCursorScreenY() + LINE_HEIGHT + 2;
            int height = this.completions.size() * COMPLETION_ROW_HEIGHT + 2;
            if (y + height > getY() + getHeight()) {
                y = getCursorScreenY() - height - 2;
            }
            y = Math.max(getY() + 2, y);

            guiGraphics.fill(x, y, x + COMPLETION_WIDTH, y + height, COLOR_COMPLETION_BACKGROUND);
            guiGraphics.fill(x, y, x + COMPLETION_WIDTH, y + 1, 0x66FFFFFF);
            guiGraphics.fill(x, y + height - 1, x + COMPLETION_WIDTH, y + height, 0x99000000);
            for (int i = 0; i < this.completions.size(); i++) {
                Completion completion = this.completions.get(i);
                int rowY = y + 1 + i * COMPLETION_ROW_HEIGHT;
                if (this.completionSelectionArmed && i == this.selectedCompletion) {
                    guiGraphics.fill(x + 1, rowY, x + COMPLETION_WIDTH - 1, rowY + COMPLETION_ROW_HEIGHT, COLOR_COMPLETION_SELECTED);
                }
                int color = completion.keyCompletion() ? COLOR_KEY : COLOR_STRING;
                guiGraphics.drawString(this.font, completion.display(), x + 5, rowY + 2, color, false);
            }
        }

        private void renderLine(GuiGraphics guiGraphics, String value, TextLine view, TextLine selected, int line, int y) {
            int lineNumberColor = line == cursorLineIndex() ? InfinityEditorButton.CONTRAST_COLOR : COLOR_LINE_NUMBER;
            if (line == this.errorLine) {
                guiGraphics.fill(getX() + LINE_NUMBER_WIDTH + 1, y, getX() + getWidth() - innerPadding(), y + LINE_HEIGHT, COLOR_ERROR_LINE);
                lineNumberColor = STATUS_BAD;
            } else if (line == cursorLineIndex()) {
                guiGraphics.fill(getX() + LINE_NUMBER_WIDTH + 1, y, getX() + getWidth() - innerPadding(), y + LINE_HEIGHT, 0x221B3142);
            }
            String lineNumber = Integer.toString(line + 1);
            guiGraphics.drawString(this.font, lineNumber, getX() + LINE_NUMBER_WIDTH - this.font.width(lineNumber) - 6,
                    y + 1, lineNumberColor, false);
            guiGraphics.fill(getX() + LINE_NUMBER_WIDTH, y, getX() + LINE_NUMBER_WIDTH + 1, y + LINE_HEIGHT, 0x66344154);

            renderSelection(guiGraphics, value, view, selected, y);
            String text = value.substring(view.beginIndex(), view.endIndex());
            drawHighlighted(guiGraphics, value, text, view.beginIndex(), getTextX(), y + 1);
        }

        private void renderSelection(GuiGraphics guiGraphics, String value, TextLine view, TextLine selected, int y) {
            if (!hasSelection()) {
                return;
            }
            int start = Math.max(view.beginIndex(), selected.beginIndex());
            int end = Math.min(view.endIndex(), selected.endIndex());
            if (start >= end) {
                return;
            }
            int lineStart = view.beginIndex();
            int x1 = getTextX() + this.font.width(value.substring(lineStart, start));
            int x2 = getTextX() + this.font.width(value.substring(lineStart, end));
            guiGraphics.fill(x1, y, Math.max(x1 + 1, x2), y + LINE_HEIGHT, COLOR_SELECTION);
        }

        private void renderCursor(GuiGraphics guiGraphics, String value) {
            if (!isFocused() || this.ticks / 6 % 2 != 0) {
                return;
            }
            int cursor = this.cursor;
            int line = cursorLineIndex();
            TextLine view = lineView(line);
            int y = getY() + innerPadding() + line * LINE_HEIGHT;
            if (!lineVisible(y)) {
                return;
            }
            int x = getTextX() + this.font.width(value.substring(view.beginIndex(), Math.min(cursor, view.endIndex())));
            guiGraphics.fill(x, y, x + 1, y + LINE_HEIGHT, COLOR_CURSOR);
        }

        private void drawHighlighted(GuiGraphics guiGraphics, String fullValue, String text, int globalStart, int x, int y) {
            if (this.commandMode) {
                drawCommandHighlighted(guiGraphics, text, globalStart, x, y);
                return;
            }

            int index = 0;
            while (index < text.length()) {
                char c = text.charAt(index);
                int start = index;
                int color = COLOR_TEXT;
                if (c == '"') {
                    index = readString(text, index + 1);
                    color = isKeyString(fullValue, globalStart + index) ? COLOR_KEY : COLOR_STRING;
                } else if (isNumberStart(c)) {
                    index++;
                    while (index < text.length() && isNumberPart(text.charAt(index))) {
                        index++;
                    }
                    color = COLOR_NUMBER;
                } else if (isIdentifierStart(c)) {
                    index++;
                    while (index < text.length() && isIdentifierPart(text.charAt(index))) {
                        index++;
                    }
                    String token = text.substring(start, index);
                    color = isLiteral(token) ? COLOR_LITERAL : COLOR_TEXT;
                } else {
                    index++;
                    if ("{}[]:,".indexOf(c) >= 0) {
                        color = COLOR_PUNCTUATION;
                    }
                }
                String part = text.substring(start, index);
                guiGraphics.drawString(this.font, part, x, y, color, false);
                x += this.font.width(part);
            }
        }

        private void drawCommandHighlighted(GuiGraphics guiGraphics, String text, int globalStart, int x, int y) {
            int index = 0;
            boolean commandToken = isCommandTokenPosition(globalStart);
            while (index < text.length()) {
                char c = text.charAt(index);
                int start = index;
                int color = COLOR_TEXT;
                if (c == '"' || c == '\'') {
                    index = readQuotedString(text, index + 1, c);
                    color = COLOR_STRING;
                } else if (c == '@') {
                    index++;
                    while (index < text.length() && isSelectorPart(text.charAt(index))) {
                        index++;
                    }
                    color = COLOR_KEY;
                } else if (isCoordinateStart(c)) {
                    index++;
                    while (index < text.length() && isCoordinatePart(text.charAt(index))) {
                        index++;
                    }
                    color = COLOR_NUMBER;
                } else if (isCommandTokenChar(c)) {
                    index++;
                    while (index < text.length() && isCommandTokenChar(text.charAt(index))) {
                        index++;
                    }
                    String token = text.substring(start, index);
                    if (commandToken) {
                        color = COLOR_KEY;
                        commandToken = false;
                    } else if (isCommandLiteral(token)) {
                        color = COLOR_LITERAL;
                    } else if (token.indexOf(':') >= 0) {
                        color = COLOR_STRING;
                    }
                } else {
                    index++;
                    if ("[]{}=,:".indexOf(c) >= 0) {
                        color = COLOR_PUNCTUATION;
                    }
                }
                String part = text.substring(start, index);
                guiGraphics.drawString(this.font, part, x, y, color, false);
                x += this.font.width(part);
            }
        }

        private int readString(String text, int index) {
            boolean escaped = false;
            while (index < text.length()) {
                char c = text.charAt(index++);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    break;
                }
            }
            return index;
        }

        private int readQuotedString(String text, int index, char quote) {
            boolean escaped = false;
            while (index < text.length()) {
                char c = text.charAt(index++);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == quote) {
                    break;
                }
            }
            return index;
        }

        private boolean isKeyString(String value, int afterString) {
            int index = afterString;
            while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
                index++;
            }
            return index < value.length() && value.charAt(index) == ':';
        }

        private boolean clickCompletion(double mouseX, double mouseY) {
            if (this.completions.isEmpty()) {
                return false;
            }
            int x = Mth.clamp(getCursorScreenX(), getX(), getX() + getWidth() - COMPLETION_WIDTH - 8);
            int y = getCursorScreenY() + LINE_HEIGHT + 2;
            int height = this.completions.size() * COMPLETION_ROW_HEIGHT + 2;
            if (y + height > getY() + getHeight()) {
                y = getCursorScreenY() - height - 2;
            }
            y = Math.max(getY() + 2, y);
            if (mouseX < x || mouseX > x + COMPLETION_WIDTH || mouseY < y || mouseY > y + height) {
                return false;
            }
            int index = Mth.clamp(((int) mouseY - y - 1) / COMPLETION_ROW_HEIGHT, 0, this.completions.size() - 1);
            this.selectedCompletion = index;
            this.completionSelectionArmed = true;
            return applySelectedCompletion();
        }

        private boolean applySelectedCompletion() {
            if (this.completions.isEmpty()) {
                return false;
            }
            Completion completion = this.completions.get(Mth.clamp(this.selectedCompletion, 0, this.completions.size() - 1));
            int replaceStart = Mth.clamp(completion.replaceStart(), 0, this.text.length());
            int replaceEnd = Mth.clamp(completion.replaceEnd(), replaceStart, this.text.length());
            this.text = this.text.substring(0, replaceStart) + completion.insert() + this.text.substring(replaceEnd);
            this.cursor = replaceStart + Mth.clamp(completion.cursorOffset(), 0, completion.insert().length());
            if (completion.selectionLength() > 0) {
                this.selectionAnchor = Mth.clamp(this.cursor + completion.selectionLength(), 0, this.text.length());
            } else {
                clearSelection();
            }
            notifyValueChanged();
            ensureCursorVisible();
            rebuildCompletions(false);
            return true;
        }

        private void rebuildCompletions() {
            rebuildCompletions(false);
        }

        private void rebuildCompletions(boolean explicit) {
            this.completions.clear();
            this.completionSelectionArmed = false;
            CompletionRequest request = this.commandMode ? buildCommandCompletionRequest(explicit) : buildCompletionRequest(explicit);
            if (request == null) {
                return;
            }

            LinkedHashSet<Completion> results = new LinkedHashSet<>();
            if (this.commandMode) {
                addCommandCompletions(results, request);
            } else if (request.keyContext()) {
                addKeyCompletions(results, request);
            } else {
                addValueCompletions(results, request);
            }
            this.completions.addAll(results.stream().limit(MAX_COMPLETIONS).toList());
            this.selectedCompletion = Mth.clamp(this.selectedCompletion, 0, Math.max(0, this.completions.size() - 1));
        }

        private CompletionRequest buildCommandCompletionRequest(boolean explicit) {
            String value = this.text;
            int cursor = Mth.clamp(this.cursor, 0, value.length());
            if (isInsideCommandString(cursor)) {
                return null;
            }
            int start = cursor;
            while (start > 0 && isCommandCompletionChar(value.charAt(start - 1))) {
                start--;
            }
            String prefix = value.substring(start, cursor);
            if (prefix.isEmpty() && !explicit && !isCommandCompletionBoundary(cursor)) {
                return null;
            }
            return new CompletionRequest(prefix, start, cursor, false, "Command", false, false, List.of());
        }

        private CompletionRequest buildCompletionRequest(boolean explicit) {
            String value = this.text;
            int cursor = Mth.clamp(this.cursor, 0, value.length());
            int start = cursor;
            while (start > 0 && isCompletionChar(value.charAt(start - 1))) {
                start--;
            }
            boolean insideString = start > 0 && value.charAt(start - 1) == '"';
            String prefix = value.substring(start, cursor);
            String key = keyBeforeValue(value, start, insideString);
            boolean keyContext = key == null && isProbablyKeyContext(value, start, insideString);
            if (prefix.isEmpty() && !explicit) {
                return null;
            }
            if (!keyContext && key == null && prefix.length() < 2) {
                return null;
            }
            if (keyContext && prefix.length() > 32) {
                return null;
            }
            boolean existingKeySyntax = keyContext && insideString && hasExistingKeySyntax(value, cursor);
            int replaceEnd = keyContext && insideString && cursor < value.length() && value.charAt(cursor) == '"' && !existingKeySyntax
                    ? cursor + 1
                    : cursor;
            return new CompletionRequest(prefix, start, replaceEnd, keyContext, key, insideString,
                    existingKeySyntax, pathBeforeCursor(value, start));
        }

        private boolean isProbablyKeyContext(String value, int tokenStart, boolean insideString) {
            int index = insideString ? tokenStart - 2 : tokenStart - 1;
            while (index >= 0 && Character.isWhitespace(value.charAt(index))) {
                index--;
            }
            if (index < 0) {
                return true;
            }
            char previous = value.charAt(index);
            return previous == '{' || previous == ',';
        }

        private String keyBeforeValue(String value, int tokenStart, boolean insideString) {
            int index = tokenStart - 1;
            if (insideString && index >= 0 && value.charAt(index) == '"') {
                index--;
            }
            while (index >= 0 && Character.isWhitespace(value.charAt(index))) {
                index--;
            }
            while (index >= 0 && Character.isWhitespace(value.charAt(index))) {
                index--;
            }
            if (index < 0 || value.charAt(index) != ':') {
                return null;
            }
            index--;
            while (index >= 0 && Character.isWhitespace(value.charAt(index))) {
                index--;
            }
            if (index < 0 || value.charAt(index) != '"') {
                return null;
            }
            int end = index;
            index--;
            boolean escaped = false;
            while (index >= 0) {
                char c = value.charAt(index);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    return value.substring(index + 1, end);
                }
                index--;
            }
            return null;
        }

        private List<String> pathBeforeCursor(String value, int cursor) {
            List<String> path = new ArrayList<>();
            String pendingString = null;
            String currentKey = null;
            boolean inString = false;
            boolean escaped = false;
            int stringStart = -1;
            int end = Mth.clamp(cursor, 0, value.length());
            for (int i = 0; i < end; i++) {
                char c = value.charAt(i);
                if (inString) {
                    if (escaped) {
                        escaped = false;
                    } else if (c == '\\') {
                        escaped = true;
                    } else if (c == '"') {
                        pendingString = value.substring(stringStart, i);
                        inString = false;
                    }
                    continue;
                }

                if (Character.isWhitespace(c)) {
                    continue;
                }
                if (c == '"') {
                    inString = true;
                    escaped = false;
                    stringStart = i + 1;
                } else if (c == ':' && pendingString != null) {
                    currentKey = pendingString;
                    pendingString = null;
                } else if (c == '{' || c == '[') {
                    path.add(currentKey == null ? "" : currentKey);
                    currentKey = null;
                    pendingString = null;
                } else if (c == '}' || c == ']') {
                    if (!path.isEmpty()) {
                        path.remove(path.size() - 1);
                    }
                    currentKey = null;
                    pendingString = null;
                } else if (c == ',') {
                    currentKey = null;
                    pendingString = null;
                }
            }
            return path;
        }

        private void addCommandCompletions(Set<Completion> results, CompletionRequest request) {
            String prefix = request.prefix();
            String normalizedPrefix = prefix.startsWith("/") ? prefix.substring(1) : prefix;
            if (isCommandNamePosition(request.replaceStart())) {
                addCommandNameValues(results, request, normalizedPrefix, prefix.startsWith("/"));
                if (prefix.isEmpty()) {
                    addCommandSnippetValues(results, request);
                }
                return;
            }

            List<String> tokens = commandTokensBefore(request.replaceStart());
            int runIndex = !tokens.isEmpty() && "execute".equals(normalizedCommandToken(tokens.get(0)))
                    ? lastCommandTokenIndex(tokens, "run")
                    : -1;
            if (runIndex >= 0) {
                List<String> nested = tokens.subList(runIndex + 1, tokens.size());
                if (nested.isEmpty()) {
                    addCommandNameValues(results, request, normalizedPrefix, prefix.startsWith("/"));
                    addCommandSnippetValues(results, request);
                    return;
                }
                addCommandArgumentCompletions(results, request, nested);
            } else {
                addCommandArgumentCompletions(results, request, tokens);
            }

            addGeneralCommandCompletions(results, request);
            if (prefix.indexOf(':') >= 0 || prefix.length() >= 2) {
                addRawValues(results, request, ITEM_IDS);
                addRawValues(results, request, ENTITY_IDS);
                addRawValues(results, request, BLOCK_IDS);
            }
        }

        private void addCommandArgumentCompletions(Set<Completion> results, CompletionRequest request, List<String> tokens) {
            if (tokens.isEmpty()) {
                addCommandNameValues(results, request, request.prefix(), false);
                addCommandSnippetValues(results, request);
                return;
            }

            String command = normalizedCommandToken(tokens.get(0));
            int arg = Math.max(0, tokens.size() - 1);
            switch (command) {
                case "data" -> addDataCommandCompletions(results, request, tokens, arg);
                case "execute" -> addExecuteCommandCompletions(results, request, tokens, arg);
                case "give" -> addGiveCommandCompletions(results, request, arg);
                case "summon" -> addSummonCommandCompletions(results, request, arg);
                case "setblock" -> addSetBlockCommandCompletions(results, request, arg);
                case "fill" -> addFillCommandCompletions(results, request, arg);
                case "effect" -> addEffectCommandCompletions(results, request, tokens, arg);
                case "enchant" -> addEnchantCommandCompletions(results, request, arg);
                case "tp", "teleport" -> addTeleportCommandCompletions(results, request, arg);
                case "kill", "clear", "tag", "teammsg", "tell", "msg", "w" -> addRawValues(results, request, TARGET_SELECTORS);
                default -> {
                    if (request.prefix().startsWith("@")) {
                        addRawValues(results, request, TARGET_SELECTORS);
                    }
                }
            }
        }

        private void addGeneralCommandCompletions(Set<Completion> results, CompletionRequest request) {
            if (request.prefix().startsWith("@") || request.prefix().isEmpty()) {
                addRawValues(results, request, TARGET_SELECTORS);
            }
            addRawValues(results, request, List.of("true", "false"));
        }

        private void addCommandNameValues(Set<Completion> results, CompletionRequest request, String prefix, boolean leadingSlash) {
            List<String> matches = COMMAND_NAMES.stream()
                    .filter(value -> completionMatches(value, prefix))
                    .sorted(Comparator.comparingInt(value -> completionScore(value, prefix)))
                    .limit(MAX_COMPLETIONS)
                    .toList();
            for (String value : matches) {
                String insert = leadingSlash ? "/" + value : value;
                results.add(new Completion(insert, insert, request.replaceStart(), request.replaceEnd(), false));
            }
        }

        private void addCommandSnippetValues(Set<Completion> results, CompletionRequest request) {
            for (String snippet : COMMAND_SNIPPETS) {
                results.add(new Completion(snippet, snippet, request.replaceStart(), request.replaceEnd(), false));
                if (results.size() >= MAX_COMPLETIONS) {
                    return;
                }
            }
        }

        private void addDataCommandCompletions(Set<Completion> results, CompletionRequest request,
                                               List<String> tokens, int arg) {
            if (arg == 0) {
                addRawValues(results, request, DATA_ACTIONS);
                return;
            }
            if (arg == 1) {
                addRawValues(results, request, DATA_TARGETS);
                return;
            }

            String action = commandArgument(tokens, 0);
            String targetType = commandArgument(tokens, 1);
            int targetEnd = dataTargetEndArg(targetType, 2);
            if (targetEnd < 0) {
                return;
            }
            if (arg < targetEnd) {
                addDataTargetValueCompletions(results, request, targetType, arg - 2);
                return;
            }

            switch (action) {
                case "get" -> {
                    if (arg == targetEnd) {
                        addRawValues(results, request, DATA_PATHS);
                    } else if (arg == targetEnd + 1) {
                        addRawValues(results, request, LEVEL_VALUES);
                    }
                }
                case "merge" -> {
                    if (arg == targetEnd) {
                        addRawValues(results, request, NBT_VALUES);
                    }
                }
                case "remove" -> {
                    if (arg == targetEnd) {
                        addRawValues(results, request, DATA_PATHS);
                    }
                }
                case "modify" -> addDataModifyCompletions(results, request, tokens, arg, targetEnd);
                default -> {
                }
            }
        }

        private void addDataModifyCompletions(Set<Completion> results, CompletionRequest request,
                                              List<String> tokens, int arg, int targetEnd) {
            int pathArg = targetEnd;
            if (arg == pathArg) {
                addRawValues(results, request, DATA_PATHS);
                return;
            }

            int actionArg = pathArg + 1;
            if (arg == actionArg) {
                addRawValues(results, request, DATA_MODIFY_ACTIONS);
                return;
            }

            String modifyAction = commandArgument(tokens, actionArg);
            int sourceArg = actionArg + ("insert".equals(modifyAction) ? 2 : 1);
            if ("insert".equals(modifyAction) && arg == actionArg + 1) {
                addRawValues(results, request, LEVEL_VALUES);
                return;
            }
            if (arg == sourceArg) {
                addRawValues(results, request, DATA_SOURCES);
                return;
            }

            String sourceKind = commandArgument(tokens, sourceArg);
            if ("value".equals(sourceKind)) {
                if (arg == sourceArg + 1) {
                    addRawValues(results, request, NBT_VALUES);
                }
                return;
            }
            if (!"from".equals(sourceKind) && !"string".equals(sourceKind)) {
                return;
            }

            int sourceTargetTypeArg = sourceArg + 1;
            if (arg == sourceTargetTypeArg) {
                addRawValues(results, request, DATA_TARGETS);
                return;
            }
            String sourceTargetType = commandArgument(tokens, sourceTargetTypeArg);
            int sourceTargetStartArg = sourceTargetTypeArg + 1;
            int sourceTargetEndArg = dataTargetEndArg(sourceTargetType, sourceTargetStartArg);
            if (sourceTargetEndArg < 0) {
                return;
            }
            if (arg < sourceTargetEndArg) {
                addDataTargetValueCompletions(results, request, sourceTargetType, arg - sourceTargetStartArg);
            } else if (arg == sourceTargetEndArg) {
                addRawValues(results, request, DATA_PATHS);
            } else if ("string".equals(sourceKind) && (arg == sourceTargetEndArg + 1 || arg == sourceTargetEndArg + 2)) {
                addRawValues(results, request, LEVEL_VALUES);
            }
        }

        private void addExecuteCommandCompletions(Set<Completion> results, CompletionRequest request,
                                                  List<String> tokens, int arg) {
            if (arg == 0) {
                addRawValues(results, request, EXECUTE_SUBCOMMANDS);
                return;
            }
            if (addExecuteStoreCompletions(results, request, tokens)) {
                return;
            }
            if (addExecuteConditionCompletions(results, request, tokens)) {
                return;
            }
            if (addExecuteTransformCompletions(results, request, tokens)) {
                return;
            }
            addRawValues(results, request, EXECUTE_SUBCOMMANDS);
        }

        private boolean addExecuteTransformCompletions(Set<Completion> results, CompletionRequest request, List<String> tokens) {
            int subcommandIndex = lastExecuteSubcommandIndex(tokens);
            if (subcommandIndex < 1) {
                return false;
            }

            String subcommand = normalizedCommandToken(tokens.get(subcommandIndex));
            if ("if".equals(subcommand) || "unless".equals(subcommand) || "store".equals(subcommand) || "run".equals(subcommand)) {
                return false;
            }

            int offset = tokens.size() - subcommandIndex - 1;
            switch (subcommand) {
                case "as", "at" -> {
                    if (offset == 0) {
                        addRawValues(results, request, TARGET_SELECTORS);
                        return true;
                    }
                }
                case "positioned" -> {
                    if (offset == 0) {
                        addRawValues(results, request, COORDINATE_VALUES);
                        addRawValues(results, request, List.of("as"));
                        return true;
                    }
                    if ("as".equals(commandTokenAt(tokens, subcommandIndex + 1))) {
                        if (offset == 1) {
                            addRawValues(results, request, TARGET_SELECTORS);
                            return true;
                        }
                        return false;
                    }
                    if (offset < 3) {
                        addRawValues(results, request, COORDINATE_COMPONENT_VALUES);
                        return true;
                    }
                }
                case "rotated" -> {
                    if (offset == 0) {
                        addRawValues(results, request, ROTATION_VALUES);
                        addRawValues(results, request, List.of("as"));
                        return true;
                    }
                    if ("as".equals(commandTokenAt(tokens, subcommandIndex + 1))) {
                        if (offset == 1) {
                            addRawValues(results, request, TARGET_SELECTORS);
                            return true;
                        }
                        return false;
                    }
                    if (offset < 2) {
                        addRawValues(results, request, ROTATION_VALUES);
                        return true;
                    }
                }
                case "facing" -> {
                    if (offset == 0) {
                        addRawValues(results, request, COORDINATE_VALUES);
                        addRawValues(results, request, List.of("entity"));
                        return true;
                    }
                    if ("entity".equals(commandTokenAt(tokens, subcommandIndex + 1))) {
                        if (offset == 1) {
                            addRawValues(results, request, TARGET_SELECTORS);
                            return true;
                        }
                        if (offset == 2) {
                            addRawValues(results, request, List.of("eyes", "feet"));
                            return true;
                        }
                        return false;
                    }
                    if (offset < 3) {
                        addRawValues(results, request, COORDINATE_COMPONENT_VALUES);
                        return true;
                    }
                }
                case "align" -> {
                    if (offset == 0) {
                        addRawValues(results, request, List.of("x", "y", "z", "xy", "xz", "yz", "xyz"));
                        return true;
                    }
                }
                case "anchored" -> {
                    if (offset == 0) {
                        addRawValues(results, request, List.of("eyes", "feet"));
                        return true;
                    }
                }
                case "in" -> {
                    if (offset == 0) {
                        addRawValues(results, request, DIMENSION_VALUES);
                        return true;
                    }
                }
                default -> {
                }
            }
            return false;
        }

        private boolean addExecuteConditionCompletions(Set<Completion> results, CompletionRequest request, List<String> tokens) {
            int conditionIndex = Math.max(lastCommandTokenIndex(tokens, "if"), lastCommandTokenIndex(tokens, "unless"));
            if (conditionIndex < 1 || hasExecuteSubcommandAfter(tokens, conditionIndex)) {
                return false;
            }

            int offset = tokens.size() - conditionIndex - 1;
            if (offset == 0) {
                addRawValues(results, request, EXECUTE_CONDITIONS);
                return true;
            }

            String condition = commandTokenAt(tokens, conditionIndex + 1);
            switch (condition) {
                case "block" -> {
                    if (offset < 4) {
                        addRawValues(results, request, offset == 1 ? COORDINATE_VALUES : COORDINATE_COMPONENT_VALUES);
                        return true;
                    }
                    if (offset == 4) {
                        addRawValues(results, request, BLOCK_IDS);
                        return true;
                    }
                }
                case "blocks" -> {
                    if (offset < 10) {
                        addRawValues(results, request, offset == 1 || offset == 4 || offset == 7
                                ? COORDINATE_VALUES : COORDINATE_COMPONENT_VALUES);
                        return true;
                    }
                    if (offset == 10) {
                        addRawValues(results, request, BLOCKS_SCAN_MODES);
                        return true;
                    }
                }
                case "data" -> {
                    if (offset == 1) {
                        addRawValues(results, request, DATA_TARGETS);
                        return true;
                    }
                    String targetType = commandTokenAt(tokens, conditionIndex + 2);
                    int targetStartArg = conditionIndex + 2;
                    int targetEndIndex = dataTargetEndTokenIndex(targetType, targetStartArg + 1);
                    if (targetEndIndex < 0) {
                        return false;
                    }
                    int currentTokenIndex = tokens.size();
                    if (currentTokenIndex < targetEndIndex) {
                        addDataTargetValueCompletions(results, request, targetType, currentTokenIndex - targetStartArg - 1);
                        return true;
                    }
                    if (currentTokenIndex == targetEndIndex) {
                        addRawValues(results, request, DATA_PATHS);
                        return true;
                    }
                }
                case "entity" -> {
                    if (offset == 1) {
                        addRawValues(results, request, TARGET_SELECTORS);
                        return true;
                    }
                }
                case "predicate" -> {
                    if (offset == 1) {
                        addRawValues(results, request, DATA_STORAGE_VALUES);
                        return true;
                    }
                }
                case "score" -> {
                    if (offset == 1) {
                        addRawValues(results, request, TARGET_SELECTORS);
                        return true;
                    }
                    if (offset == 3) {
                        addRawValues(results, request, SCORE_OPERATIONS);
                        return true;
                    }
                    if (offset == 4) {
                        addRawValues(results, request, TARGET_SELECTORS);
                        addRawValues(results, request, LEVEL_VALUES);
                        return true;
                    }
                }
                default -> {
                }
            }
            return false;
        }

        private boolean addExecuteStoreCompletions(Set<Completion> results, CompletionRequest request, List<String> tokens) {
            int storeIndex = lastCommandTokenIndex(tokens, "store");
            if (storeIndex < 1 || hasExecuteSubcommandAfter(tokens, storeIndex)) {
                return false;
            }

            int offset = tokens.size() - storeIndex - 1;
            if (offset == 0) {
                addRawValues(results, request, EXECUTE_STORE_TARGETS);
                return true;
            }
            if (offset == 1) {
                addRawValues(results, request, EXECUTE_STORE_DESTINATIONS);
                return true;
            }

            String destination = commandTokenAt(tokens, storeIndex + 2);
            switch (destination) {
                case "block" -> {
                    if (offset < 5) {
                        addRawValues(results, request, offset == 2 ? COORDINATE_VALUES : COORDINATE_COMPONENT_VALUES);
                        return true;
                    }
                    if (offset == 5) {
                        addRawValues(results, request, DATA_PATHS);
                        return true;
                    }
                    if (offset == 6) {
                        addRawValues(results, request, STORE_TYPES);
                        return true;
                    }
                    if (offset == 7) {
                        addRawValues(results, request, LEVEL_VALUES);
                        return true;
                    }
                }
                case "entity" -> {
                    if (offset == 2) {
                        addRawValues(results, request, TARGET_SELECTORS);
                        return true;
                    }
                    if (offset == 3) {
                        addRawValues(results, request, DATA_PATHS);
                        return true;
                    }
                    if (offset == 4) {
                        addRawValues(results, request, STORE_TYPES);
                        return true;
                    }
                    if (offset == 5) {
                        addRawValues(results, request, LEVEL_VALUES);
                        return true;
                    }
                }
                case "storage" -> {
                    if (offset == 2) {
                        addRawValues(results, request, DATA_STORAGE_VALUES);
                        return true;
                    }
                    if (offset == 3) {
                        addRawValues(results, request, DATA_PATHS);
                        return true;
                    }
                    if (offset == 4) {
                        addRawValues(results, request, STORE_TYPES);
                        return true;
                    }
                    if (offset == 5) {
                        addRawValues(results, request, LEVEL_VALUES);
                        return true;
                    }
                }
                case "score" -> {
                    if (offset == 2) {
                        addRawValues(results, request, TARGET_SELECTORS);
                        return true;
                    }
                }
                case "bossbar" -> {
                    if (offset == 2) {
                        addRawValues(results, request, DATA_STORAGE_VALUES);
                        return true;
                    }
                    if (offset == 3) {
                        addRawValues(results, request, BOSSBAR_VALUES);
                        return true;
                    }
                }
                default -> {
                }
            }
            return false;
        }

        private void addGiveCommandCompletions(Set<Completion> results, CompletionRequest request, int arg) {
            if (arg == 0) {
                addRawValues(results, request, TARGET_SELECTORS);
            } else if (arg == 1) {
                addRawValues(results, request, ITEM_IDS);
            } else if (arg == 2) {
                addRawValues(results, request, COUNT_VALUES);
            }
        }

        private void addSummonCommandCompletions(Set<Completion> results, CompletionRequest request, int arg) {
            if (arg == 0) {
                addRawValues(results, request, ENTITY_IDS);
            } else if (arg >= 1 && arg <= 3) {
                addRawValues(results, request, arg == 1 ? COORDINATE_VALUES : COORDINATE_COMPONENT_VALUES);
            } else if (arg == 4) {
                addRawValues(results, request, NBT_VALUES);
            }
        }

        private void addSetBlockCommandCompletions(Set<Completion> results, CompletionRequest request, int arg) {
            if (arg >= 0 && arg <= 2) {
                addRawValues(results, request, arg == 0 ? COORDINATE_VALUES : COORDINATE_COMPONENT_VALUES);
            } else if (arg == 3) {
                addRawValues(results, request, BLOCK_IDS);
            } else if (arg == 4) {
                addRawValues(results, request, BLOCK_SET_MODES);
            }
        }

        private void addFillCommandCompletions(Set<Completion> results, CompletionRequest request, int arg) {
            if (arg >= 0 && arg <= 5) {
                addRawValues(results, request, arg == 0 || arg == 3 ? COORDINATE_VALUES : COORDINATE_COMPONENT_VALUES);
            } else if (arg == 6) {
                addRawValues(results, request, BLOCK_IDS);
            } else if (arg == 7) {
                addRawValues(results, request, FILL_MODES);
            } else if (arg == 8) {
                addRawValues(results, request, BLOCK_IDS);
            }
        }

        private void addEffectCommandCompletions(Set<Completion> results, CompletionRequest request,
                                                 List<String> tokens, int arg) {
            if (arg == 0) {
                addRawValues(results, request, List.of("give", "clear"));
                return;
            }
            String action = commandArgument(tokens, 0);
            if ("clear".equals(action)) {
                if (arg == 1) {
                    addRawValues(results, request, TARGET_SELECTORS);
                } else if (arg == 2) {
                    addRawValues(results, request, EFFECT_IDS);
                }
                return;
            }
            if (arg == 1) {
                addRawValues(results, request, TARGET_SELECTORS);
            } else if (arg == 2) {
                addRawValues(results, request, EFFECT_IDS);
            } else if (arg == 3) {
                addRawValues(results, request, EFFECT_DURATIONS);
            } else if (arg == 4) {
                addRawValues(results, request, LEVEL_VALUES);
            } else if (arg == 5) {
                addRawValues(results, request, BOOLEAN_VALUES);
            }
        }

        private void addEnchantCommandCompletions(Set<Completion> results, CompletionRequest request, int arg) {
            if (arg == 0) {
                addRawValues(results, request, TARGET_SELECTORS);
            } else if (arg == 1) {
                addRawValues(results, request, ENCHANTMENT_IDS);
            } else if (arg == 2) {
                addRawValues(results, request, LEVEL_VALUES);
            }
        }

        private void addTeleportCommandCompletions(Set<Completion> results, CompletionRequest request, int arg) {
            if (arg == 0) {
                addRawValues(results, request, TARGET_SELECTORS);
                addRawValues(results, request, COORDINATE_VALUES);
            } else if (arg >= 1 && arg <= 3) {
                addRawValues(results, request, TARGET_SELECTORS);
                addRawValues(results, request, COORDINATE_COMPONENT_VALUES);
            } else if (arg == 4 || arg == 5) {
                addRawValues(results, request, ROTATION_VALUES);
            }
        }

        private void addKeyCompletions(Set<Completion> results, CompletionRequest request) {
            for (String key : keyCompletionCandidates(request)) {
                if (!completionMatches(key, request.prefix())) {
                    continue;
                }
                String insert = request.insideString()
                        ? request.existingKeySyntax() ? key : key + "\": "
                        : "\"" + key + "\": ";
                results.add(new Completion(key, insert, request.replaceStart(), request.replaceEnd(), true));
            }
        }

        private List<String> keyCompletionCandidates(CompletionRequest request) {
            LinkedHashSet<String> keys = new LinkedHashSet<>();
            String context = lastPathKey(request.path()).toLowerCase(Locale.ROOT);
            if (context.isEmpty() && request.path().size() <= 1) {
                keys.addAll(this.rootKeyCompletions);
                return new ArrayList<>(keys);
            } else if (context.equals("display")) {
                keys.addAll(DISPLAY_KEY_COMPLETIONS);
            } else if (isEnchantmentContext(context)) {
                keys.addAll(ENCHANTMENT_KEY_COMPLETIONS);
            } else if (context.equals("attributemodifiers")) {
                keys.addAll(ATTRIBUTE_KEY_COMPLETIONS);
            } else if (context.equals("custompotioneffects")) {
                keys.addAll(POTION_EFFECT_KEY_COMPLETIONS);
            } else if (context.equals("fireworks") || context.equals("explosions")) {
                keys.addAll(FIREWORK_KEY_COMPLETIONS);
            } else if (context.equals("recipes") || context.equals("offers")) {
                keys.addAll(TRADE_KEY_COMPLETIONS);
            }
            keys.addAll(KEY_COMPLETIONS);
            return new ArrayList<>(keys);
        }

        private boolean isEnchantmentContext(String context) {
            return context.equals("enchantments") || context.equals("storedenchantments");
        }

        private void addValueCompletions(Set<Completion> results, CompletionRequest request) {
            String normalizedKey = request.key() == null ? "" : request.key().toLowerCase(Locale.ROOT);
            String context = lastPathKey(request.path()).toLowerCase(Locale.ROOT);
            if (normalizedKey.equals("id")) {
                if (isEnchantmentContext(context)) {
                    addStringValues(results, request, ENCHANTMENT_IDS);
                } else if (context.equals("entitytag")) {
                    addStringValues(results, request, ENTITY_IDS);
                } else {
                    addStringValues(results, request, ITEM_IDS);
                }
            } else if (normalizedKey.equals("attributename") || normalizedKey.equals("name")) {
                if (context.equals("attributemodifiers")) {
                    addStringValues(results, request, ATTRIBUTE_IDS);
                }
            } else if (normalizedKey.equals("slot")) {
                addStringValues(results, request, SLOT_VALUES);
            } else if (normalizedKey.equals("potion")) {
                addStringValues(results, request, POTION_IDS);
            }
            addSnippetValues(results, request, normalizedKey);
            addRawValues(results, request, VALUE_LITERALS);
        }

        private static final List<String> POTION_IDS = List.of(
                "minecraft:water", "minecraft:mundane", "minecraft:thick", "minecraft:awkward",
                "minecraft:night_vision", "minecraft:invisibility", "minecraft:leaping", "minecraft:fire_resistance",
                "minecraft:swiftness", "minecraft:slowness", "minecraft:turtle_master", "minecraft:water_breathing",
                "minecraft:healing", "minecraft:harming", "minecraft:poison", "minecraft:regeneration",
                "minecraft:strength", "minecraft:weakness", "minecraft:luck", "minecraft:slow_falling"
        );

        private void addSnippetValues(Set<Completion> results, CompletionRequest request, String normalizedKey) {
            if (!request.prefix().isEmpty()) {
                return;
            }
            if (request.insideString()) {
                return;
            }

            String valueIndent = indentationAt(request.replaceStart()) + INDENT;
            if (normalizedKey.equals("display")) {
                String snippet = "{\n" + valueIndent
                        + "\"Name\": \"{\\\"text\\\":\\\"Name\\\"}\",\n" + valueIndent
                        + "\"Lore\": []\n" + indentationAt(request.replaceStart()) + "}";
                String marker = "Name";
                results.add(new Completion("display object", snippet,
                        request.replaceStart(), request.replaceEnd(), false, templateCursor(snippet, marker, true),
                        marker.length()));
            } else if (normalizedKey.equals("enchantments") || normalizedKey.equals("storedenchantments")) {
                String snippet = "[\n" + valueIndent
                        + "{\"id\": \"minecraft:sharpness\", \"lvl\": 1}\n" + indentationAt(request.replaceStart()) + "]";
                String marker = "minecraft:sharpness";
                results.add(new Completion("enchantment list", snippet,
                        request.replaceStart(), request.replaceEnd(), false, templateCursor(snippet, marker, false),
                        marker.length()));
            } else if (normalizedKey.equals("attributemodifiers")) {
                String snippet = "[\n" + valueIndent
                        + "{\"AttributeName\": \"minecraft:generic.attack_damage\", \"Name\": \"generic.attack_damage\", "
                        + "\"Amount\": 1.0, \"Operation\": 0, \"UUID\": [0, 0, 0, 0], \"Slot\": \"mainhand\"}\n"
                        + indentationAt(request.replaceStart()) + "]";
                String marker = "minecraft:generic.attack_damage";
                results.add(new Completion("attribute modifier", snippet,
                        request.replaceStart(), request.replaceEnd(), false,
                        templateCursor(snippet, marker, false), marker.length()));
            } else if (normalizedKey.equals("canplaceon") || normalizedKey.equals("candestroy")) {
                String snippet = "[\"minecraft:stone\"]";
                String marker = "minecraft:stone";
                results.add(new Completion("block id list", snippet,
                        request.replaceStart(), request.replaceEnd(), false, templateCursor(snippet, marker, false),
                        marker.length()));
            }
        }

        private void addStringValues(Set<Completion> results, CompletionRequest request, List<String> values) {
            List<String> matches = values.stream()
                    .filter(value -> completionMatches(value, request.prefix()))
                    .sorted(Comparator.comparingInt(value -> completionScore(value, request.prefix())))
                    .limit(MAX_COMPLETIONS)
                    .toList();
            for (String value : matches) {
                String insert = request.insideString() ? value : "\"" + value + "\"";
                results.add(new Completion(value, insert, request.replaceStart(), request.replaceEnd(), false));
                if (results.size() >= MAX_COMPLETIONS) {
                    return;
                }
            }
        }

        private void addRawValues(Set<Completion> results, CompletionRequest request, List<String> values) {
            for (String value : values) {
                if (!completionMatches(value, request.prefix())) {
                    continue;
                }
                results.add(new Completion(value, value, request.replaceStart(), request.replaceEnd(), false));
            }
        }

        private int templateCursor(String snippet, String marker, boolean last) {
            int index = last ? snippet.lastIndexOf(marker) : snippet.indexOf(marker);
            return index >= 0 ? index : snippet.length();
        }

        private void ensureCursorVisible() {
            int line = cursorLineIndex();
            int y = line * LINE_HEIGHT;
            double scroll = scrollAmount();
            if (y < scroll) {
                setScrollAmount(y);
            } else if (y + LINE_HEIGHT > scroll + getHeight() - totalInnerPadding()) {
                setScrollAmount(y + LINE_HEIGHT - getHeight() + totalInnerPadding());
            }
        }

        private void seekCursor(double mouseX, double mouseY, boolean selecting) {
            int line = Mth.clamp((int) ((mouseY - getY() - innerPadding() + scrollAmount()) / LINE_HEIGHT),
                    0, lineViews().size() - 1);
            TextLine view = lineView(line);
            int x = Math.max(0, (int) mouseX - getTextX());
            int position = view.beginIndex() + charOffsetAtX(this.text.substring(view.beginIndex(), view.endIndex()), x);
            moveCursorTo(position, selecting);
            ensureCursorVisible();
        }

        private boolean lineVisible(int y) {
            double scroll = scrollAmount();
            return y + LINE_HEIGHT - scroll >= getY() && y - scroll <= getY() + getHeight();
        }

        private boolean withinContentAreaPoint(double mouseX, double mouseY) {
            return mouseX >= getX() + innerPadding()
                    && mouseX < getRight()
                    && mouseY >= getY() + innerPadding()
                    && mouseY < getBottom() - innerPadding();
        }

        private int getTextX() {
            return getX() + innerPadding() + LINE_NUMBER_WIDTH + 6;
        }

        private int getCursorScreenX() {
            int cursor = this.cursor;
            int line = cursorLineIndex();
            TextLine view = lineView(line);
            return getTextX() + this.font.width(this.text.substring(view.beginIndex(), Math.min(cursor, view.endIndex())));
        }

        private int getCursorScreenY() {
            return getY() + innerPadding() + cursorLineIndex() * LINE_HEIGHT - (int) scrollAmount();
        }

        private boolean handleEditKey(int keyCode) {
            normalizeCursorAndSelection();
            if (CompatScreen.isSelectAll(keyCode)) {
                this.selectionAnchor = 0;
                this.cursor = this.text.length();
                rebuildCompletions();
                return true;
            }
            if (CompatScreen.isCopy(keyCode)) {
                copySelection();
                return true;
            }
            if (CompatScreen.isCut(keyCode)) {
                copySelection();
                deleteSelection();
                return true;
            }
            if (CompatScreen.isPaste(keyCode)) {
                insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                return true;
            }

            boolean selecting = CompatScreen.hasShiftDown();
            return switch (keyCode) {
                case 257, 335 -> {
                    if (this.commandMode) {
                        insertCommandNewlineWithIndent();
                    } else {
                        insertNewlineWithIndent();
                    }
                    yield true;
                }
                case 259 -> {
                    deleteBackward();
                    yield true;
                }
                case 261 -> {
                    deleteForward();
                    yield true;
                }
                case 262 -> {
                    moveCursorTo(CompatScreen.hasControlDown() ? nextWord(this.cursor) : this.cursor + 1, selecting);
                    yield true;
                }
                case 263 -> {
                    moveCursorTo(CompatScreen.hasControlDown() ? previousWord(this.cursor) : this.cursor - 1, selecting);
                    yield true;
                }
                case 264 -> {
                    moveCursorVertical(1, selecting);
                    yield true;
                }
                case 265 -> {
                    moveCursorVertical(-1, selecting);
                    yield true;
                }
                case 266 -> {
                    moveCursorVertical(-10, selecting);
                    yield true;
                }
                case 267 -> {
                    moveCursorVertical(10, selecting);
                    yield true;
                }
                case 268 -> {
                    TextLine line = lineView(cursorLineIndex());
                    moveCursorTo(CompatScreen.hasControlDown() ? 0 : line.beginIndex(), selecting);
                    yield true;
                }
                case 269 -> {
                    TextLine line = lineView(cursorLineIndex());
                    moveCursorTo(CompatScreen.hasControlDown() ? this.text.length() : line.endIndex(), selecting);
                    yield true;
                }
                default -> false;
            };
        }

        private void insertText(String insert) {
            if (insert == null || insert.isEmpty()) {
                return;
            }
            replaceSelection(insert, insert.length());
        }

        private void insertPairedText(String open, String close) {
            if (!hasSelection() && this.cursor < this.text.length() && this.text.charAt(this.cursor) == close.charAt(0)) {
                moveCursorTo(this.cursor + close.length(), false);
                return;
            }
            if (hasSelection()) {
                int start = selectionStart();
                int end = selectionEnd();
                String selected = this.text.substring(start, end);
                replaceSelection(open + selected + close, open.length() + selected.length() + close.length());
                return;
            }
            replaceSelection(open + close, open.length());
        }

        private void insertNewlineWithIndent() {
            int start = selectionStart();
            int end = selectionEnd();
            String indent = indentationAt(start);
            String prefix = shouldInsertCommaBeforeNewline(start, end) ? "," : "";
            boolean splitPair = isOpeningBracket(previousNonWhitespaceBefore(start))
                    && isMatchingPair(previousNonWhitespaceBefore(start), nextNonWhitespaceAfter(end));
            if (splitPair) {
                String insert = prefix + "\n" + indent + INDENT + "\n" + indent;
                replaceSelection(insert, prefix.length() + 1 + indent.length() + INDENT.length());
                return;
            }

            String extra = isOpeningBracket(previousNonWhitespaceBefore(start)) ? INDENT : "";
            replaceSelection(prefix + "\n" + indent + extra,
                    prefix.length() + 1 + indent.length() + extra.length());
        }

        private void insertCommandNewlineWithIndent() {
            int start = selectionStart();
            String indent = indentationAt(start);
            String lineBeforeCursor = this.text.substring(lineStartAt(start), start).trim();
            String extra = shouldIndentCommandLine(lineBeforeCursor) ? INDENT : "";
            replaceSelection("\n" + indent + extra, 1 + indent.length() + extra.length());
        }

        private boolean shouldIndentCommandLine(String line) {
            if (line.isEmpty()) {
                return false;
            }
            String normalized = line.toLowerCase(Locale.ROOT);
            return normalized.endsWith(" run")
                    || normalized.equals("run")
                    || normalized.startsWith("execute ") && !normalized.contains(" run ")
                    || hasUnclosedCommandDelimiter(line);
        }

        private boolean hasUnclosedCommandDelimiter(String line) {
            int square = 0;
            int curly = 0;
            boolean inQuote = false;
            char quote = '\0';
            boolean escaped = false;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (inQuote) {
                    if (escaped) {
                        escaped = false;
                    } else if (c == '\\') {
                        escaped = true;
                    } else if (c == quote) {
                        inQuote = false;
                    }
                    continue;
                }
                if (c == '"' || c == '\'') {
                    inQuote = true;
                    quote = c;
                } else if (c == '[') {
                    square++;
                } else if (c == ']') {
                    square = Math.max(0, square - 1);
                } else if (c == '{') {
                    curly++;
                } else if (c == '}') {
                    curly = Math.max(0, curly - 1);
                }
            }
            return square > 0 || curly > 0;
        }

        private void insertClosingBracket(char close) {
            if (!hasSelection() && onlyWhitespaceBeforeCursorOnLine()) {
                outdentCurrentLine();
            }
            insertText(Character.toString(close));
        }

        private void indentSelectedLines() {
            changeSelectedLineIndent(true);
        }

        private void outdentSelectedLines() {
            changeSelectedLineIndent(false);
        }

        private void outdentCurrentLine() {
            int line = cursorLineIndex();
            TextLine view = lineView(line);
            int amount = removableIndentAt(view.beginIndex());
            if (amount <= 0) {
                return;
            }
            this.text = this.text.substring(0, view.beginIndex()) + this.text.substring(view.beginIndex() + amount);
            this.cursor = Math.max(view.beginIndex(), this.cursor - amount);
            if (this.selectionAnchor >= 0) {
                this.selectionAnchor = Math.max(view.beginIndex(), this.selectionAnchor - amount);
            }
            notifyValueChanged();
            ensureCursorVisible();
            rebuildCompletions();
        }

        private void changeSelectedLineIndent(boolean indent) {
            List<TextLine> lines = lineViews();
            int start = selectionStart();
            int end = selectionEnd();
            int firstLine = lineIndexAt(start);
            int lastLine = lineIndexAt(end);
            if (hasSelection() && lastLine > firstLine && end == lineView(lastLine).beginIndex()) {
                lastLine--;
            }

            String updated = this.text;
            int newCursor = this.cursor;
            int newAnchor = this.selectionAnchor;
            for (int line = lastLine; line >= firstLine; line--) {
                TextLine view = lines.get(Mth.clamp(line, 0, lines.size() - 1));
                if (indent) {
                    updated = updated.substring(0, view.beginIndex()) + INDENT + updated.substring(view.beginIndex());
                    newCursor = adjustPositionForInsert(newCursor, view.beginIndex(), INDENT.length());
                    newAnchor = adjustPositionForInsert(newAnchor, view.beginIndex(), INDENT.length());
                } else {
                    int amount = removableIndentAt(updated, view.beginIndex());
                    if (amount <= 0) {
                        continue;
                    }
                    updated = updated.substring(0, view.beginIndex()) + updated.substring(view.beginIndex() + amount);
                    newCursor = adjustPositionForRemoval(newCursor, view.beginIndex(), amount);
                    newAnchor = adjustPositionForRemoval(newAnchor, view.beginIndex(), amount);
                }
            }

            this.text = updated;
            this.cursor = Mth.clamp(newCursor, 0, this.text.length());
            this.selectionAnchor = newAnchor >= 0 ? Mth.clamp(newAnchor, 0, this.text.length()) : -1;
            notifyValueChanged();
            ensureCursorVisible();
            rebuildCompletions();
        }

        private int adjustPositionForInsert(int position, int insertAt, int amount) {
            if (position < 0) {
                return position;
            }
            return position >= insertAt ? position + amount : position;
        }

        private int adjustPositionForRemoval(int position, int removeAt, int amount) {
            if (position < 0 || position <= removeAt) {
                return position;
            }
            return position <= removeAt + amount ? removeAt : position - amount;
        }

        private void replaceSelection(String insert, int cursorOffset) {
            normalizeCursorAndSelection();
            int start = selectionStart();
            int end = selectionEnd();
            if (this.text.length() - (end - start) + insert.length() > 200000) {
                return;
            }
            this.text = this.text.substring(0, start) + insert + this.text.substring(end);
            this.cursor = Mth.clamp(start + cursorOffset, 0, this.text.length());
            clearSelection();
            notifyValueChanged();
            ensureCursorVisible();
            rebuildCompletions();
        }

        private void deleteBackward() {
            if (deleteSelection()) {
                return;
            }
            if (this.cursor <= 0) {
                return;
            }
            if (deleteIndentBeforeCursor()) {
                return;
            }
            if (this.cursor < this.text.length() && isMatchingPair(this.text.charAt(this.cursor - 1), this.text.charAt(this.cursor))) {
                this.text = this.text.substring(0, this.cursor - 1) + this.text.substring(this.cursor + 1);
                this.cursor--;
                notifyValueChanged();
                ensureCursorVisible();
                rebuildCompletions();
                return;
            }
            this.text = this.text.substring(0, this.cursor - 1) + this.text.substring(this.cursor);
            this.cursor--;
            notifyValueChanged();
            ensureCursorVisible();
            rebuildCompletions();
        }

        private void deleteForward() {
            if (deleteSelection()) {
                return;
            }
            if (this.cursor >= this.text.length()) {
                return;
            }
            this.text = this.text.substring(0, this.cursor) + this.text.substring(this.cursor + 1);
            notifyValueChanged();
            ensureCursorVisible();
            rebuildCompletions();
        }

        private boolean deleteSelection() {
            normalizeCursorAndSelection();
            if (!hasSelection()) {
                return false;
            }
            int start = selectionStart();
            int end = selectionEnd();
            if (start >= end) {
                clearSelection();
                return false;
            }
            this.text = this.text.substring(0, start) + this.text.substring(end);
            this.cursor = start;
            clearSelection();
            notifyValueChanged();
            ensureCursorVisible();
            rebuildCompletions();
            return true;
        }

        private boolean deleteIndentBeforeCursor() {
            if (!onlyWhitespaceBeforeCursorOnLine()) {
                return false;
            }
            int lineStart = lineStartAt(this.cursor);
            int spaces = this.cursor - lineStart;
            if (spaces <= 0) {
                return false;
            }
            int amount = spaces % INDENT.length() == 0 ? INDENT.length() : spaces % INDENT.length();
            amount = Math.min(amount, spaces);
            this.text = this.text.substring(0, this.cursor - amount) + this.text.substring(this.cursor);
            this.cursor -= amount;
            notifyValueChanged();
            ensureCursorVisible();
            rebuildCompletions();
            return true;
        }

        private void moveCursorVertical(int lines, boolean selecting) {
            normalizeCursorAndSelection();
            int currentLine = cursorLineIndex();
            TextLine current = lineView(currentLine);
            int column = this.cursor - current.beginIndex();
            int targetLine = Mth.clamp(currentLine + lines, 0, lineViews().size() - 1);
            TextLine target = lineView(targetLine);
            moveCursorTo(target.beginIndex() + Math.min(column, target.endIndex() - target.beginIndex()), selecting);
        }

        private void moveCursorTo(int position, boolean selecting) {
            int clamped = Mth.clamp(position, 0, this.text.length());
            int currentCursor = Mth.clamp(this.cursor, 0, this.text.length());
            if (selecting) {
                if (this.selectionAnchor < 0) {
                    this.selectionAnchor = currentCursor;
                } else {
                    this.selectionAnchor = Mth.clamp(this.selectionAnchor, 0, this.text.length());
                }
            } else {
                clearSelection();
            }
            this.cursor = clamped;
            if (this.selectionAnchor == this.cursor) {
                clearSelection();
            }
            ensureCursorVisible();
            rebuildCompletions();
        }

        private void copySelection() {
            normalizeCursorAndSelection();
            if (!hasSelection()) {
                return;
            }
            Minecraft.getInstance().keyboardHandler.setClipboard(this.text.substring(selectionStart(), selectionEnd()));
        }

        private int previousWord(int position) {
            int index = Mth.clamp(position, 0, this.text.length());
            while (index > 0 && Character.isWhitespace(this.text.charAt(index - 1))) {
                index--;
            }
            while (index > 0 && isCompletionChar(this.text.charAt(index - 1))) {
                index--;
            }
            return index;
        }

        private int nextWord(int position) {
            int index = Mth.clamp(position, 0, this.text.length());
            while (index < this.text.length() && isCompletionChar(this.text.charAt(index))) {
                index++;
            }
            while (index < this.text.length() && Character.isWhitespace(this.text.charAt(index))) {
                index++;
            }
            return index;
        }

        private int charOffsetAtX(String line, int x) {
            if (x <= 0) {
                return 0;
            }
            for (int i = 1; i <= line.length(); i++) {
                int previous = this.font.width(line.substring(0, i - 1));
                int current = this.font.width(line.substring(0, i));
                if (x < previous + (current - previous) / 2) {
                    return i - 1;
                }
            }
            return line.length();
        }

        private boolean hasSelection() {
            return this.selectionAnchor >= 0
                    && Mth.clamp(this.selectionAnchor, 0, this.text.length()) != Mth.clamp(this.cursor, 0, this.text.length());
        }

        private int selectionStart() {
            int cursor = Mth.clamp(this.cursor, 0, this.text.length());
            int anchor = Mth.clamp(this.selectionAnchor, 0, this.text.length());
            return hasSelection() ? Math.min(anchor, cursor) : cursor;
        }

        private int selectionEnd() {
            int cursor = Mth.clamp(this.cursor, 0, this.text.length());
            int anchor = Mth.clamp(this.selectionAnchor, 0, this.text.length());
            return hasSelection() ? Math.max(anchor, cursor) : cursor;
        }

        private void clearSelection() {
            this.selectionAnchor = -1;
        }

        private void normalizeCursorAndSelection() {
            this.cursor = Mth.clamp(this.cursor, 0, this.text.length());
            if (this.selectionAnchor >= 0) {
                this.selectionAnchor = Mth.clamp(this.selectionAnchor, 0, this.text.length());
                if (this.selectionAnchor == this.cursor) {
                    clearSelection();
                }
            }
        }

        private void notifyValueChanged() {
            clearError();
            this.valueListener.accept(this.text);
        }

        private String indentationAt(int position) {
            int lineStart = lineStartAt(position);
            int index = lineStart;
            while (index < this.text.length()) {
                char c = this.text.charAt(index);
                if (c != ' ' && c != '\t') {
                    break;
                }
                index++;
            }
            return this.text.substring(lineStart, index);
        }

        private boolean onlyWhitespaceBeforeCursorOnLine() {
            int lineStart = lineStartAt(this.cursor);
            for (int i = lineStart; i < this.cursor; i++) {
                char c = this.text.charAt(i);
                if (c != ' ' && c != '\t') {
                    return false;
                }
            }
            return true;
        }

        private int lineStartAt(int position) {
            int index = Mth.clamp(position, 0, this.text.length());
            while (index > 0 && this.text.charAt(index - 1) != '\n') {
                index--;
            }
            return index;
        }

        private int lineIndexAt(int position) {
            int line = 0;
            int clamped = Mth.clamp(position, 0, this.text.length());
            for (int i = 0; i < clamped; i++) {
                if (this.text.charAt(i) == '\n') {
                    line++;
                }
            }
            return line;
        }

        private int removableIndentAt(int lineStart) {
            return removableIndentAt(this.text, lineStart);
        }

        private int removableIndentAt(String value, int lineStart) {
            if (lineStart >= value.length()) {
                return 0;
            }
            char first = value.charAt(lineStart);
            if (first == '\t') {
                return 1;
            }
            int amount = 0;
            while (amount < INDENT.length() && lineStart + amount < value.length()
                    && value.charAt(lineStart + amount) == ' ') {
                amount++;
            }
            return amount;
        }

        private boolean isEscapedQuoteAtCursor() {
            int slashCount = 0;
            int index = this.cursor - 1;
            while (index >= 0 && this.text.charAt(index) == '\\') {
                slashCount++;
                index--;
            }
            return slashCount % 2 == 1;
        }

        private boolean isInsideString(int position) {
            boolean inside = false;
            boolean escaped = false;
            int clamped = Mth.clamp(position, 0, this.text.length());
            for (int i = 0; i < clamped; i++) {
                char c = this.text.charAt(i);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inside = !inside;
                }
            }
            return inside;
        }

        private boolean isInsideCommandString(int position) {
            boolean inQuote = false;
            char quote = '\0';
            boolean escaped = false;
            int clamped = Mth.clamp(position, 0, this.text.length());
            int lineStart = lineStartAt(clamped);
            for (int i = lineStart; i < clamped; i++) {
                char c = this.text.charAt(i);
                if (inQuote) {
                    if (escaped) {
                        escaped = false;
                    } else if (c == '\\') {
                        escaped = true;
                    } else if (c == quote) {
                        inQuote = false;
                    }
                } else if (c == '"' || c == '\'') {
                    inQuote = true;
                    quote = c;
                    escaped = false;
                }
            }
            return inQuote;
        }

        private char previousNonWhitespaceBefore(int position) {
            int index = Mth.clamp(position, 0, this.text.length()) - 1;
            while (index >= 0 && Character.isWhitespace(this.text.charAt(index))) {
                index--;
            }
            return index >= 0 ? this.text.charAt(index) : '\0';
        }

        private char nextNonWhitespaceAfter(int position) {
            int index = Mth.clamp(position, 0, this.text.length());
            while (index < this.text.length() && Character.isWhitespace(this.text.charAt(index))) {
                index++;
            }
            return index < this.text.length() ? this.text.charAt(index) : '\0';
        }

        private boolean shouldInsertCommaBeforeNewline(int start, int end) {
            if (isInsideString(start)) {
                return false;
            }
            char previous = previousNonWhitespaceBefore(start);
            char next = nextNonWhitespaceAfter(end);
            if (next != '}' && next != ']') {
                return false;
            }
            return previous != '\0' && previous != '{' && previous != '[' && previous != ',' && previous != ':';
        }

        private boolean isOpeningBracket(char c) {
            return c == '{' || c == '[';
        }

        private boolean isMatchingPair(char open, char close) {
            return open == '{' && close == '}' || open == '[' && close == ']' || open == '"' && close == '"';
        }

        private boolean hasExistingKeySyntax(String value, int cursor) {
            if (cursor >= value.length() || value.charAt(cursor) != '"') {
                return false;
            }
            int index = cursor + 1;
            while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
                index++;
            }
            return index < value.length() && value.charAt(index) == ':';
        }

        private String lastPathKey(List<String> path) {
            for (int i = path.size() - 1; i >= 0; i--) {
                String key = path.get(i);
                if (key != null && !key.isEmpty()) {
                    return key;
                }
            }
            return "";
        }

        private boolean isCommandTokenPosition(int globalStart) {
            int lineStart = lineStartAt(globalStart);
            int index = lineStart;
            while (index < this.text.length() && index < globalStart && Character.isWhitespace(this.text.charAt(index))) {
                index++;
            }
            return index >= globalStart;
        }

        private boolean isCommandNamePosition(int position) {
            int lineStart = lineStartAt(position);
            for (int i = lineStart; i < position; i++) {
                if (!Character.isWhitespace(this.text.charAt(i))) {
                    return false;
                }
            }
            return true;
        }

        private String commandNameBefore(int position) {
            int lineStart = lineStartAt(position);
            int index = lineStart;
            while (index < position && Character.isWhitespace(this.text.charAt(index))) {
                index++;
            }
            if (index < position && this.text.charAt(index) == '/') {
                index++;
            }
            int start = index;
            while (index < position && isCommandNameChar(this.text.charAt(index))) {
                index++;
            }
            return this.text.substring(start, index).toLowerCase(Locale.ROOT);
        }

        private boolean isCommandCompletionBoundary(int cursor) {
            int clamped = Mth.clamp(cursor, 0, this.text.length());
            if (isInsideString(clamped)) {
                return false;
            }
            if (clamped == 0) {
                return true;
            }
            char previous = this.text.charAt(clamped - 1);
            return Character.isWhitespace(previous) || previous == '[' || previous == '{'
                    || previous == '=' || previous == ',' || previous == ':';
        }

        private List<String> commandTokensBefore(int position) {
            int end = Mth.clamp(position, 0, this.text.length());
            int lineStart = lineStartAt(end);
            List<String> tokens = new ArrayList<>();
            StringBuilder token = new StringBuilder();
            boolean inQuote = false;
            char quote = '\0';
            boolean escaped = false;
            int squareDepth = 0;
            int curlyDepth = 0;
            int parenDepth = 0;

            for (int i = lineStart; i < end; i++) {
                char c = this.text.charAt(i);
                if (inQuote) {
                    token.append(c);
                    if (escaped) {
                        escaped = false;
                    } else if (c == '\\') {
                        escaped = true;
                    } else if (c == quote) {
                        inQuote = false;
                    }
                    continue;
                }

                if (c == '"' || c == '\'') {
                    token.append(c);
                    inQuote = true;
                    quote = c;
                    escaped = false;
                } else if (Character.isWhitespace(c) && squareDepth == 0 && curlyDepth == 0 && parenDepth == 0) {
                    flushCommandToken(tokens, token);
                } else {
                    token.append(c);
                    if (c == '[') {
                        squareDepth++;
                    } else if (c == ']') {
                        squareDepth = Math.max(0, squareDepth - 1);
                    } else if (c == '{') {
                        curlyDepth++;
                    } else if (c == '}') {
                        curlyDepth = Math.max(0, curlyDepth - 1);
                    } else if (c == '(') {
                        parenDepth++;
                    } else if (c == ')') {
                        parenDepth = Math.max(0, parenDepth - 1);
                    }
                }
            }

            if (end > lineStart && Character.isWhitespace(this.text.charAt(end - 1))) {
                flushCommandToken(tokens, token);
            }
            return tokens;
        }

        private void flushCommandToken(List<String> tokens, StringBuilder token) {
            if (!token.isEmpty()) {
                tokens.add(token.toString());
                token.setLength(0);
            }
        }

        private String commandArgument(List<String> tokens, int argIndex) {
            return commandTokenAt(tokens, argIndex + 1);
        }

        private String commandTokenAt(List<String> tokens, int index) {
            return index >= 0 && index < tokens.size() ? normalizedCommandToken(tokens.get(index)) : "";
        }

        private String normalizedCommandToken(String token) {
            if (token == null) {
                return "";
            }
            String normalized = token.trim().toLowerCase(Locale.ROOT);
            return normalized.startsWith("/") ? normalized.substring(1) : normalized;
        }

        private int lastCommandTokenIndex(List<String> tokens, String token) {
            for (int i = tokens.size() - 1; i >= 0; i--) {
                if (token.equals(commandTokenAt(tokens, i))) {
                    return i;
                }
            }
            return -1;
        }

        private int lastExecuteSubcommandIndex(List<String> tokens) {
            for (int i = tokens.size() - 1; i >= 1; i--) {
                if (EXECUTE_SUBCOMMANDS.contains(commandTokenAt(tokens, i))) {
                    return i;
                }
            }
            return -1;
        }

        private boolean hasExecuteSubcommandAfter(List<String> tokens, int index) {
            for (int i = index + 1; i < tokens.size(); i++) {
                if (EXECUTE_SUBCOMMANDS.contains(commandTokenAt(tokens, i))) {
                    return true;
                }
            }
            return false;
        }

        private int dataTargetEndArg(String targetType, int targetStartArg) {
            int count = dataTargetArgumentCount(targetType);
            return count < 0 ? -1 : targetStartArg + count;
        }

        private int dataTargetEndTokenIndex(String targetType, int targetStartIndex) {
            int count = dataTargetArgumentCount(targetType);
            return count < 0 ? -1 : targetStartIndex + count;
        }

        private int dataTargetArgumentCount(String targetType) {
            return switch (targetType) {
                case "block" -> 3;
                case "entity", "storage" -> 1;
                default -> -1;
            };
        }

        private void addDataTargetValueCompletions(Set<Completion> results, CompletionRequest request,
                                                   String targetType, int targetOffset) {
            switch (targetType) {
                case "block" -> addRawValues(results, request,
                        targetOffset <= 0 ? COORDINATE_VALUES : COORDINATE_COMPONENT_VALUES);
                case "entity" -> addRawValues(results, request, TARGET_SELECTORS);
                case "storage" -> addRawValues(results, request, DATA_STORAGE_VALUES);
                default -> {
                }
            }
        }

        private boolean completionMatches(String value, String prefix) {
            if (prefix == null || prefix.isEmpty()) {
                return true;
            }
            String normalizedValue = value.toLowerCase(Locale.ROOT);
            String normalizedPrefix = prefix.toLowerCase(Locale.ROOT);
            String shortName = shortRegistryName(normalizedValue);
            String humps = completionHumps(value);
            return normalizedValue.startsWith(normalizedPrefix)
                    || shortName.startsWith(normalizedPrefix)
                    || humps.startsWith(normalizedPrefix)
                    || normalizedValue.contains(normalizedPrefix)
                    || shortName.contains(normalizedPrefix);
        }

        private int completionScore(String value, String prefix) {
            if (prefix == null || prefix.isEmpty()) {
                return 0;
            }
            String normalizedValue = value.toLowerCase(Locale.ROOT);
            String normalizedPrefix = prefix.toLowerCase(Locale.ROOT);
            String shortName = shortRegistryName(normalizedValue);
            if (normalizedValue.startsWith(normalizedPrefix)) {
                return 0;
            }
            if (shortName.startsWith(normalizedPrefix)) {
                return 1;
            }
            if (completionHumps(value).startsWith(normalizedPrefix)) {
                return 2;
            }
            if (normalizedValue.contains(normalizedPrefix)) {
                return 3;
            }
            if (shortName.contains(normalizedPrefix)) {
                return 4;
            }
            return 5;
        }

        private String shortRegistryName(String value) {
            int colon = value.indexOf(':');
            return colon >= 0 && colon + 1 < value.length() ? value.substring(colon + 1) : value;
        }

        private String completionHumps(String value) {
            String name = shortRegistryName(value);
            StringBuilder builder = new StringBuilder();
            boolean nextSegment = true;
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                if (c == '_' || c == '-' || c == '.' || c == '/') {
                    nextSegment = true;
                    continue;
                }
                if (nextSegment || Character.isUpperCase(c)) {
                    builder.append(Character.toLowerCase(c));
                    nextSegment = false;
                }
            }
            return builder.toString();
        }

        private static boolean isCompletionChar(char c) {
            return Character.isLetterOrDigit(c) || c == '_' || c == ':' || c == '/' || c == '.' || c == '-';
        }

        private static boolean isCommandCompletionChar(char c) {
            return isCommandTokenChar(c) || c == '@' || c == '~' || c == '^' || c == '/' || c == '.';
        }

        private static boolean isCommandTokenChar(char c) {
            return Character.isLetterOrDigit(c) || c == '_' || c == ':' || c == '/' || c == '.' || c == '-';
        }

        private static boolean isCommandNameChar(char c) {
            return Character.isLetterOrDigit(c) || c == '_' || c == '-';
        }

        private static boolean isSelectorPart(char c) {
            return Character.isLetterOrDigit(c) || c == '_' || c == '[' || c == ']' || c == '='
                    || c == ',' || c == ':' || c == '.' || c == '-';
        }

        private static boolean isCoordinateStart(char c) {
            return c == '~' || c == '^' || c == '-' || c >= '0' && c <= '9';
        }

        private static boolean isCoordinatePart(char c) {
            return c == '~' || c == '^' || c == '-' || c == '+' || c == '.' || c >= '0' && c <= '9';
        }

        private static boolean isCommandLiteral(String token) {
            return "true".equals(token) || "false".equals(token) || "run".equals(token) || "as".equals(token)
                    || "at".equals(token) || "if".equals(token) || "unless".equals(token) || "in".equals(token)
                    || "positioned".equals(token) || "rotated".equals(token) || "anchored".equals(token)
                    || "store".equals(token) || "entity".equals(token) || "block".equals(token)
                    || "score".equals(token);
        }

        private static boolean isNumberStart(char c) {
            return c == '-' || c >= '0' && c <= '9';
        }

        private static boolean isNumberPart(char c) {
            return c >= '0' && c <= '9' || c == '.' || c == '-' || c == '+' || c == 'e' || c == 'E';
        }

        private static boolean isIdentifierStart(char c) {
            return Character.isLetter(c) || c == '_';
        }

        private static boolean isIdentifierPart(char c) {
            return Character.isLetterOrDigit(c) || c == '_';
        }

        private static boolean isLiteral(String token) {
            return "true".equals(token) || "false".equals(token) || "null".equals(token);
        }

        private static <T> List<String> registryKeys(Iterable<T> values) {
            List<String> keys = new ArrayList<>();
            for (T value : values) {
                ResourceLocation key = null;
                if (value instanceof Item item) {
                    key = CompatRegistries.ITEMS.getKey(item);
                } else if (value instanceof Block block) {
                    key = CompatRegistries.BLOCKS.getKey(block);
                } else if (value instanceof Enchantment enchantment) {
                    key = CompatRegistries.ENCHANTMENTS.getKey(enchantment);
                } else if (value instanceof Attribute attribute) {
                    key = CompatRegistries.ATTRIBUTES.getKey(attribute);
                } else if (value instanceof EntityType<?> entityType) {
                    key = CompatRegistries.ENTITY_TYPES.getKey(entityType);
                } else if (value instanceof MobEffect effect) {
                    key = CompatRegistries.MOB_EFFECTS.getKey(effect);
                }
                if (key != null) {
                    keys.add(key.toString());
                }
            }
            keys.sort(Comparator.naturalOrder());
            return keys;
        }

        private List<TextLine> lineViews() {
            List<TextLine> lines = new ArrayList<>();
            int start = 0;
            for (int i = 0; i < this.text.length(); i++) {
                if (this.text.charAt(i) == '\n') {
                    lines.add(new TextLine(start, i));
                    start = i + 1;
                }
            }
            lines.add(new TextLine(start, this.text.length()));
            return lines;
        }

        private TextLine lineView(int line) {
            List<TextLine> lines = lineViews();
            if (lines.isEmpty()) {
                return new TextLine(0, 0);
            }
            return lines.get(Mth.clamp(line, 0, lines.size() - 1));
        }

        private TextLine selectedLine() {
            return new TextLine(selectionStart(), selectionEnd());
        }

        private int cursorLineIndex() {
            List<TextLine> lines = lineViews();
            for (int i = 0; i < lines.size(); i++) {
                TextLine line = lines.get(i);
                if (this.cursor >= line.beginIndex() && this.cursor <= line.endIndex()) {
                    return i;
                }
            }
            return Math.max(0, lines.size() - 1);
        }

        private record CompletionRequest(String prefix, int replaceStart, int replaceEnd, boolean keyContext,
                                         String key, boolean insideString, boolean existingKeySyntax, List<String> path) {
        }

        private record Completion(String display, String insert, int replaceStart, int replaceEnd,
                                  boolean keyCompletion, int cursorOffset, int selectionLength) {
            private Completion(String display, String insert, int replaceStart, int replaceEnd, boolean keyCompletion) {
                this(display, insert, replaceStart, replaceEnd, keyCompletion, insert.length(), 0);
            }

            private Completion(String display, String insert, int replaceStart, int replaceEnd,
                               boolean keyCompletion, int cursorOffset) {
                this(display, insert, replaceStart, replaceEnd, keyCompletion, cursorOffset, 0);
            }
        }

        private record TextLine(int beginIndex, int endIndex) {
        }
    }
}

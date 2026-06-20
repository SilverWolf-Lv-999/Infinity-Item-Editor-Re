package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
final class ItemCommandBlockEditorScreen extends Screen {
    private static final int BUTTON_HEIGHT = 20;
    private static final int EDITOR_MARGIN = 18;
    private static final int EDITOR_TOP = 44;
    private static final int EDITOR_BOTTOM_MARGIN = 52;
    private static final int STATUS_GOOD = 0xFF32CC64;
    private static final int STATUS_BAD = 0xFFF44262;
    private static final int STATUS_NEUTRAL = 0xFFFFD966;
    private static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    private static final String ENTITY_TAG = "EntityTag";

    private final ItemEditorScreen lastScreen;
    private ItemStack commandStack;
    private String jsonText;
    private String appliedJsonText;
    private ItemJsonEditorScreen.JsonCodeEditBox jsonBox;
    private Component status = Component.empty();
    private int statusColor = STATUS_NEUTRAL;
    private boolean discardArmed;

    ItemCommandBlockEditorScreen(ItemEditorScreen lastScreen, ItemStack stack) {
        super(Component.translatable(key("commandblock")));
        this.lastScreen = lastScreen;
        this.commandStack = stack.copy();
        this.jsonText = initialJson(this.commandStack);
        this.appliedJsonText = this.jsonText;
    }

    @Override
    protected void init() {
        int editorWidth = Math.max(180, this.width - EDITOR_MARGIN * 2);
        int editorHeight = Math.max(80, this.height - EDITOR_TOP - EDITOR_BOTTOM_MARGIN);
        this.jsonBox = addRenderableWidget(new ItemJsonEditorScreen.JsonCodeEditBox(this.font, EDITOR_MARGIN, EDITOR_TOP,
                editorWidth, editorHeight, Component.translatable(key("commandblock.placeholder"))));
        this.jsonBox.useCommandBlockCompletions();
        this.jsonBox.setValue(this.jsonText);
        this.jsonBox.setValueListener(value -> {
            this.jsonText = value;
            this.discardArmed = false;
        });
        setFocused(this.jsonBox);
        this.jsonBox.setFocused(true);

        int buttons = 5;
        int buttonWidth = Mth.clamp((this.width - EDITOR_MARGIN * 2 - (buttons - 1) * 4) / buttons, 54, 78);
        int totalWidth = buttonWidth * buttons + (buttons - 1) * 4;
        int x = (this.width - totalWidth) / 2;
        int buttonY = this.height - 28;
        addRenderableWidget(new InfinityEditorButton(x, buttonY, buttonWidth, BUTTON_HEIGHT,
                Component.translatable(key("back")), button -> returnToLastScreen()));
        addRenderableWidget(new InfinityEditorButton(x + (buttonWidth + 4), buttonY, buttonWidth, BUTTON_HEIGHT,
                Component.translatable(key("json.format")), button -> formatJson()));
        addRenderableWidget(new InfinityEditorButton(x + (buttonWidth + 4) * 2, buttonY, buttonWidth, BUTTON_HEIGHT,
                Component.translatable(key("reset")), button -> resetJson()));
        addRenderableWidget(new InfinityEditorButton(x + (buttonWidth + 4) * 3, buttonY, buttonWidth, BUTTON_HEIGHT,
                Component.translatable(key("clear")), button -> clearJson()));
        addRenderableWidget(new InfinityEditorButton(x + (buttonWidth + 4) * 4, buttonY, buttonWidth, BUTTON_HEIGHT,
                Component.translatable(key("commandblock.apply")), button -> applyJson(false)));
    }

    @Override
    public void tick() {
        if (this.jsonBox != null) {
            this.jsonBox.tick();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && this.jsonBox != null && this.jsonBox.closeCompletions()) {
            return true;
        }
        if (keyCode == 256 || isInventoryKey(keyCode, scanCode)) {
            returnToLastScreen();
            return true;
        }
        if (Screen.hasControlDown() && Screen.hasAltDown() && keyCode == 76) {
            formatJson();
            return true;
        }
        if (Screen.hasControlDown() && keyCode == 83) {
            applyJson(false);
            return true;
        }
        if (Screen.hasControlDown() && keyCode == 70) {
            formatJson();
            return true;
        }
        if ((keyCode == 257 || keyCode == 335) && Screen.hasControlDown()) {
            applyJson(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, InfinityEditorButton.MAIN_COLOR);
        guiGraphics.renderItem(this.commandStack, 20, 12);
        guiGraphics.renderItemDecorations(this.font, this.commandStack, 20, 12);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key(isMinecart() ? "commandblock.target.minecart" : "commandblock.target.block")),
                this.width / 2, 26, InfinityEditorButton.ALT_COLOR);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (!this.status.getString().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, clippedStatus(), this.width / 2, this.height - 42, this.statusColor);
        }
        if (this.jsonBox != null) {
            this.jsonBox.renderCompletions(guiGraphics);
        }
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
                && this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode));
    }

    private void applyJson(boolean returnAfterApply) {
        try {
            CompoundTag commandData = ItemJsonConverter.compoundFromJson(currentText());
            ItemStack updated = this.commandStack.copy();
            writeCommandData(updated, commandData);
            this.commandStack = updated;
            this.lastScreen.applyCommandBlockEditedStack(updated);
            this.jsonText = actualJson(this.commandStack);
            this.appliedJsonText = this.jsonText;
            this.discardArmed = false;
            if (this.jsonBox != null) {
                this.jsonBox.setValue(this.jsonText);
                this.jsonBox.clearError();
            }
            this.status = Component.translatable(messageKey("editor_command_block_applied"));
            this.statusColor = STATUS_GOOD;
            if (returnAfterApply) {
                returnToLastScreen();
            }
        } catch (JsonParseException | IllegalStateException | NumberFormatException exception) {
            showInvalidJson(exception);
        }
    }

    private void formatJson() {
        try {
            this.jsonText = ItemJsonConverter.format(currentText());
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
        this.commandStack = this.lastScreen.previewStack.copy();
        this.jsonText = initialJson(this.commandStack);
        this.appliedJsonText = this.jsonText;
        this.discardArmed = false;
        if (this.jsonBox != null) {
            this.jsonBox.setValue(this.jsonText);
            this.jsonBox.clearError();
        }
        this.status = Component.translatable(messageKey("editor_command_block_reset"));
        this.statusColor = STATUS_NEUTRAL;
    }

    private void clearJson() {
        this.jsonText = "{}";
        this.discardArmed = false;
        if (this.jsonBox != null) {
            this.jsonBox.setValue(this.jsonText);
            this.jsonBox.clearError();
        }
        this.status = Component.translatable(messageKey("editor_command_block_cleared"));
        this.statusColor = STATUS_NEUTRAL;
    }

    private void returnToLastScreen() {
        if (isDirty() && !this.discardArmed) {
            this.discardArmed = true;
            this.status = Component.translatable(messageKey("editor_command_block_unsaved"));
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
        return !currentText().equals(this.appliedJsonText);
    }

    private String currentText() {
        return this.jsonBox == null ? this.jsonText : this.jsonBox.getValue();
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

    private String initialJson(ItemStack stack) {
        return ItemJsonConverter.compoundToJson(readCommandData(stack, !hasCommandData(stack)));
    }

    private String actualJson(ItemStack stack) {
        return ItemJsonConverter.compoundToJson(readCommandData(stack, false));
    }

    private CompoundTag readCommandData(ItemStack stack, boolean includeDefaults) {
        CompoundTag existing = stack.getTagElement(dataTagKey(stack));
        CompoundTag commandData = existing == null ? new CompoundTag() : existing.copy();
        if (includeDefaults) {
            putCommandDefaults(commandData, stack);
        }
        return commandData;
    }

    private void putCommandDefaults(CompoundTag commandData, ItemStack stack) {
        if (!commandData.contains("id", Tag.TAG_STRING)) {
            commandData.putString("id", stack.is(Items.COMMAND_BLOCK_MINECART)
                    ? "minecraft:command_block_minecart"
                    : "minecraft:command_block");
        }
        if (!commandData.contains("Command", Tag.TAG_STRING)) {
            commandData.putString("Command", "");
        }
        if (!commandData.contains("CustomName", Tag.TAG_STRING)) {
            commandData.putString("CustomName", "{\"text\":\"@\"}");
        }
        if (!commandData.contains("TrackOutput", Tag.TAG_BYTE)) {
            commandData.putBoolean("TrackOutput", true);
        }
        if (!stack.is(Items.COMMAND_BLOCK_MINECART)) {
            if (!commandData.contains("auto", Tag.TAG_BYTE)) {
                commandData.putBoolean("auto", false);
            }
            if (!commandData.contains("conditionMet", Tag.TAG_BYTE)) {
                commandData.putBoolean("conditionMet", false);
            }
            if (!commandData.contains("powered", Tag.TAG_BYTE)) {
                commandData.putBoolean("powered", false);
            }
            if (!commandData.contains("UpdateLastExecution", Tag.TAG_BYTE)) {
                commandData.putBoolean("UpdateLastExecution", true);
            }
        }
    }

    private void writeCommandData(ItemStack stack, CompoundTag commandData) {
        String tagKey = dataTagKey(stack);
        if (commandData == null || commandData.isEmpty()) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.remove(tagKey);
                if (tag.isEmpty()) {
                    stack.setTag(null);
                }
            }
            return;
        }

        stack.getOrCreateTag().put(tagKey, commandData.copy());
    }

    private boolean hasCommandData(ItemStack stack) {
        CompoundTag existing = stack.getTagElement(dataTagKey(stack));
        return existing != null && !existing.isEmpty();
    }

    private boolean isMinecart() {
        return this.commandStack.is(Items.COMMAND_BLOCK_MINECART);
    }

    private String dataTagKey(ItemStack stack) {
        return stack.is(Items.COMMAND_BLOCK_MINECART) ? ENTITY_TAG : BLOCK_ENTITY_TAG;
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
}

package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
final class ItemCommandBlockEditorScreen extends Screen {
    private static final int BUTTON_HEIGHT = 20;
    private static final int EDITOR_MARGIN = 18;
    private static final int EDITOR_TOP = 44;
    private static final int EDITOR_TOP_WITH_OPTIONS = 68;
    private static final int OPTIONS_Y = 40;
    private static final int EDITOR_BOTTOM_MARGIN = 52;
    private static final int STATUS_GOOD = 0xFF32CC64;
    private static final int STATUS_NEUTRAL = 0xFFFFD966;
    private static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    private static final String BLOCK_STATE_TAG = "BlockStateTag";
    private static final String ENTITY_TAG = "EntityTag";
    private static final String COMMAND_TAG = "Command";
    private static final String AUTO_TAG = "auto";
    private static final String CONDITIONAL_TAG = "conditional";

    private final ItemEditorScreen lastScreen;
    private final boolean supportsCommandBlockOptions;
    private ItemStack commandStack;
    private String commandText;
    private String appliedCommandText;
    private boolean unconditional;
    private boolean appliedUnconditional;
    private boolean alwaysActive;
    private boolean appliedAlwaysActive;
    private ItemJsonEditorScreen.JsonCodeEditBox commandBox;
    private InfinityEditorButton conditionButton;
    private InfinityEditorButton activationButton;
    private Component status = Component.empty();
    private int statusColor = STATUS_NEUTRAL;
    private boolean discardArmed;

    ItemCommandBlockEditorScreen(ItemEditorScreen lastScreen, ItemStack stack) {
        super(Component.translatable(key("commandblock")));
        this.lastScreen = lastScreen;
        this.commandStack = stack.copy();
        this.supportsCommandBlockOptions = supportsCommandBlockOptions(this.commandStack);
        this.commandText = readCommand(this.commandStack);
        this.appliedCommandText = this.commandText;
        this.unconditional = readUnconditional(this.commandStack);
        this.appliedUnconditional = this.unconditional;
        this.alwaysActive = readAlwaysActive(this.commandStack);
        this.appliedAlwaysActive = this.alwaysActive;
    }

    @Override
    protected void init() {
        if (this.supportsCommandBlockOptions) {
            int optionGap = 4;
            int optionWidth = Mth.clamp((this.width - EDITOR_MARGIN * 2 - optionGap) / 2, 104, 160);
            int totalWidth = optionWidth * 2 + optionGap;
            int optionX = (this.width - totalWidth) / 2;
            this.conditionButton = addRenderableWidget(new InfinityEditorButton(optionX, OPTIONS_Y, optionWidth, BUTTON_HEIGHT,
                    conditionModeText(), button -> toggleUnconditional()));
            this.activationButton = addRenderableWidget(new InfinityEditorButton(optionX + optionWidth + optionGap, OPTIONS_Y,
                    optionWidth, BUTTON_HEIGHT, activationModeText(), button -> toggleAlwaysActive()));
        }

        int editorWidth = Math.max(180, this.width - EDITOR_MARGIN * 2);
        int editorTop = this.supportsCommandBlockOptions ? EDITOR_TOP_WITH_OPTIONS : EDITOR_TOP;
        int editorHeight = Math.max(80, this.height - editorTop - EDITOR_BOTTOM_MARGIN);
        this.commandBox = addRenderableWidget(new ItemJsonEditorScreen.JsonCodeEditBox(this.font, EDITOR_MARGIN, editorTop,
                editorWidth, editorHeight, Component.translatable(key("commandblock.placeholder"))));
        this.commandBox.useCommandCompletions();
        this.commandBox.setValue(this.commandText);
        this.commandBox.setValueListener(value -> {
            this.commandText = value;
            this.discardArmed = false;
        });
        setFocused(this.commandBox);
        this.commandBox.setFocused(true);

        int buttons = 4;
        int buttonWidth = Mth.clamp((this.width - EDITOR_MARGIN * 2 - (buttons - 1) * 4) / buttons, 58, 86);
        int totalWidth = buttonWidth * buttons + (buttons - 1) * 4;
        int x = (this.width - totalWidth) / 2;
        int buttonY = this.height - 28;
        addRenderableWidget(new InfinityEditorButton(x, buttonY, buttonWidth, BUTTON_HEIGHT,
                Component.translatable(key("back")), button -> returnToLastScreen()));
        addRenderableWidget(new InfinityEditorButton(x + (buttonWidth + 4), buttonY, buttonWidth, BUTTON_HEIGHT,
                Component.translatable(key("reset")), button -> resetCommand()));
        addRenderableWidget(new InfinityEditorButton(x + (buttonWidth + 4) * 2, buttonY, buttonWidth, BUTTON_HEIGHT,
                Component.translatable(key("clear")), button -> clearCommand()));
        addRenderableWidget(new InfinityEditorButton(x + (buttonWidth + 4) * 3, buttonY, buttonWidth, BUTTON_HEIGHT,
                Component.translatable(key("commandblock.apply")), button -> applyCommand(false)));
    }

    @Override
    public void tick() {
        if (this.commandBox != null) {
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && this.commandBox != null && this.commandBox.closeCompletions()) {
            return true;
        }
        if (keyCode == 256) {
            returnToLastScreen();
            return true;
        }
        if (isInventoryKey(keyCode, scanCode) && !isCommandBoxFocused()) {
            returnToLastScreen();
            return true;
        }
        if (Screen.hasControlDown() && keyCode == 83) {
            applyCommand(false);
            return true;
        }
        if ((keyCode == 257 || keyCode == 335) && Screen.hasControlDown()) {
            applyCommand(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, InfinityEditorButton.MAIN_COLOR);
        guiGraphics.renderItem(this.commandStack, 20, 12);
        guiGraphics.renderItemDecorations(this.font, this.commandStack, 20, 12);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("commandblock.target")),
                this.width / 2, 26, InfinityEditorButton.ALT_COLOR);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (!this.status.getString().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, clippedStatus(), this.width / 2, this.height - 42, this.statusColor);
        }
        if (this.commandBox != null) {
            this.commandBox.renderCompletions(guiGraphics);
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

    private boolean isCommandBoxFocused() {
        return this.commandBox != null && this.commandBox.isFocused();
    }

    private void applyCommand(boolean returnAfterApply) {
        ItemStack updated = this.commandStack.copy();
        writeCommand(updated, normalizedCommand(currentText()));
        if (this.supportsCommandBlockOptions) {
            writeUnconditional(updated, this.unconditional);
            writeAlwaysActive(updated, this.alwaysActive);
        }
        this.commandStack = updated;
        this.lastScreen.applyCommandBlockEditedStack(updated);
        this.commandText = readCommand(this.commandStack);
        this.appliedCommandText = this.commandText;
        this.unconditional = readUnconditional(this.commandStack);
        this.appliedUnconditional = this.unconditional;
        this.alwaysActive = readAlwaysActive(this.commandStack);
        this.appliedAlwaysActive = this.alwaysActive;
        this.discardArmed = false;
        if (this.commandBox != null) {
            this.commandBox.setValue(this.commandText);
            this.commandBox.clearError();
        }
        syncOptionButtons();
        this.status = Component.translatable(messageKey("editor_command_block_applied"));
        this.statusColor = STATUS_GOOD;
        if (returnAfterApply) {
            returnToLastScreen();
        }
    }

    private void resetCommand() {
        this.commandStack = this.lastScreen.previewStack.copy();
        this.commandText = readCommand(this.commandStack);
        this.appliedCommandText = this.commandText;
        this.unconditional = readUnconditional(this.commandStack);
        this.appliedUnconditional = this.unconditional;
        this.alwaysActive = readAlwaysActive(this.commandStack);
        this.appliedAlwaysActive = this.alwaysActive;
        this.discardArmed = false;
        if (this.commandBox != null) {
            this.commandBox.setValue(this.commandText);
            this.commandBox.clearError();
        }
        syncOptionButtons();
        this.status = Component.translatable(messageKey("editor_command_block_reset"));
        this.statusColor = STATUS_NEUTRAL;
    }

    private void clearCommand() {
        this.commandText = "";
        this.discardArmed = false;
        if (this.commandBox != null) {
            this.commandBox.setValue("");
            this.commandBox.clearError();
        }
        this.status = Component.translatable(messageKey("editor_command_block_cleared"));
        this.statusColor = STATUS_NEUTRAL;
    }

    private void returnToLastScreen() {
        if (isDirty() && !this.discardArmed) {
            this.discardArmed = true;
            this.status = Component.translatable(messageKey("editor_command_block_unsaved"));
            this.statusColor = STATUS_NEUTRAL;
            if (this.commandBox != null) {
                setFocused(this.commandBox);
                this.commandBox.setFocused(true);
            }
            return;
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen);
        }
    }

    private boolean isDirty() {
        return !currentText().equals(this.appliedCommandText)
                || (this.supportsCommandBlockOptions
                        && (this.unconditional != this.appliedUnconditional || this.alwaysActive != this.appliedAlwaysActive));
    }

    private String currentText() {
        return this.commandBox == null ? this.commandText : this.commandBox.getValue();
    }

    private String normalizedCommand(String value) {
        return value == null ? "" : value.replace('\r', ' ').replace('\n', ' ').trim();
    }

    private Component clippedStatus() {
        int maxWidth = Math.max(40, this.width - 20);
        String text = this.status.getString();
        if (this.font.width(text) <= maxWidth) {
            return this.status;
        }
        return Component.literal(this.font.plainSubstrByWidth(text, maxWidth - this.font.width("...")) + "...");
    }

    private String readCommand(ItemStack stack) {
        CompoundTag commandData = ItemStackNbt.getElement(stack, dataTagKey(stack));
        return commandData == null ? "" : commandData.getString(COMMAND_TAG);
    }

    private boolean readUnconditional(ItemStack stack) {
        CompoundTag blockState = ItemStackNbt.getElement(stack, BLOCK_STATE_TAG);
        return blockState == null || !"true".equalsIgnoreCase(blockState.getString(CONDITIONAL_TAG));
    }

    private boolean readAlwaysActive(ItemStack stack) {
        CompoundTag commandData = ItemStackNbt.getElement(stack, BLOCK_ENTITY_TAG);
        return commandData != null && commandData.getBoolean(AUTO_TAG);
    }

    private void writeCommand(ItemStack stack, String command) {
        String tagKey = dataTagKey(stack);
        CompoundTag rootTag = ItemStackNbt.get(stack);
        CompoundTag commandData = rootTag == null ? new CompoundTag() : rootTag.getCompound(tagKey);
        if (command.isEmpty()) {
            commandData.remove(COMMAND_TAG);
        } else {
            commandData.putString(COMMAND_TAG, command);
        }

        putOrRemoveTag(stack, rootTag, tagKey, commandData);
    }

    private void writeUnconditional(ItemStack stack, boolean unconditional) {
        CompoundTag rootTag = ItemStackNbt.get(stack);
        CompoundTag blockState = rootTag == null ? new CompoundTag() : rootTag.getCompound(BLOCK_STATE_TAG);
        if (unconditional) {
            blockState.remove(CONDITIONAL_TAG);
        } else {
            blockState.putString(CONDITIONAL_TAG, "true");
        }
        putOrRemoveTag(stack, rootTag, BLOCK_STATE_TAG, blockState);
    }

    private void writeAlwaysActive(ItemStack stack, boolean alwaysActive) {
        CompoundTag rootTag = ItemStackNbt.get(stack);
        CompoundTag commandData = rootTag == null ? new CompoundTag() : rootTag.getCompound(BLOCK_ENTITY_TAG);
        if (alwaysActive) {
            commandData.putBoolean(AUTO_TAG, true);
        } else {
            commandData.remove(AUTO_TAG);
        }
        putOrRemoveTag(stack, rootTag, BLOCK_ENTITY_TAG, commandData);
    }

    private void putOrRemoveTag(ItemStack stack, CompoundTag rootTag, String tagKey, CompoundTag data) {
        if (data.isEmpty()) {
            if (rootTag != null) {
                rootTag.remove(tagKey);
                if (rootTag.isEmpty()) {
                    ItemStackNbt.set(stack, null);
                }
            }
            return;
        }

        ItemStackNbt.getOrCreate(stack).put(tagKey, data);
    }

    private void toggleUnconditional() {
        this.unconditional = !this.unconditional;
        this.discardArmed = false;
        syncOptionButtons();
    }

    private void toggleAlwaysActive() {
        this.alwaysActive = !this.alwaysActive;
        this.discardArmed = false;
        syncOptionButtons();
    }

    private void syncOptionButtons() {
        if (this.conditionButton != null) {
            this.conditionButton.setMessage(conditionModeText());
        }
        if (this.activationButton != null) {
            this.activationButton.setMessage(activationModeText());
        }
    }

    private Component conditionModeText() {
        return Component.translatable(key("commandblock.condition_mode"),
                Component.translatable(key(this.unconditional ? "commandblock.unconditional" : "commandblock.conditional")));
    }

    private Component activationModeText() {
        return Component.translatable(key("commandblock.activation_mode"),
                Component.translatable(key(this.alwaysActive ? "commandblock.always_active" : "commandblock.needs_redstone")));
    }

    private String dataTagKey(ItemStack stack) {
        return stack.is(Items.COMMAND_BLOCK_MINECART) ? ENTITY_TAG : BLOCK_ENTITY_TAG;
    }

    private boolean supportsCommandBlockOptions(ItemStack stack) {
        return stack.is(Items.COMMAND_BLOCK)
                || stack.is(Items.CHAIN_COMMAND_BLOCK)
                || stack.is(Items.REPEATING_COMMAND_BLOCK);
    }

    private static String key(String suffix) {
        return "screen." + ModSource.MODID + "." + suffix;
    }

    private static String messageKey(String suffix) {
        return "message." + ModSource.MODID + "." + suffix;
    }
}

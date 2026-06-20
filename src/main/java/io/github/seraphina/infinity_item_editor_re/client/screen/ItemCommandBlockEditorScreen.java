package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
final class ItemCommandBlockEditorScreen extends Screen {
    private static final int BUTTON_HEIGHT = 20;
    private static final int EDITOR_MARGIN = 18;
    private static final int EDITOR_TOP = 44;
    private static final int EDITOR_BOTTOM_MARGIN = 52;
    private static final int STATUS_GOOD = 0xFF32CC64;
    private static final int STATUS_NEUTRAL = 0xFFFFD966;
    private static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    private static final String ENTITY_TAG = "EntityTag";
    private static final String COMMAND_TAG = "Command";

    private final ItemEditorScreen lastScreen;
    private ItemStack commandStack;
    private String commandText;
    private String appliedCommandText;
    private ItemJsonEditorScreen.JsonCodeEditBox commandBox;
    private Component status = Component.empty();
    private int statusColor = STATUS_NEUTRAL;
    private boolean discardArmed;

    ItemCommandBlockEditorScreen(ItemEditorScreen lastScreen, ItemStack stack) {
        super(Component.translatable(key("commandblock")));
        this.lastScreen = lastScreen;
        this.commandStack = stack.copy();
        this.commandText = readCommand(this.commandStack);
        this.appliedCommandText = this.commandText;
    }

    @Override
    protected void init() {
        int editorWidth = Math.max(180, this.width - EDITOR_MARGIN * 2);
        int editorHeight = Math.max(80, this.height - EDITOR_TOP - EDITOR_BOTTOM_MARGIN);
        this.commandBox = addRenderableWidget(new ItemJsonEditorScreen.JsonCodeEditBox(this.font, EDITOR_MARGIN, EDITOR_TOP,
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
            this.commandBox.tick();
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
        renderBackground(guiGraphics);
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
        this.commandStack = updated;
        this.lastScreen.applyCommandBlockEditedStack(updated);
        this.commandText = readCommand(this.commandStack);
        this.appliedCommandText = this.commandText;
        this.discardArmed = false;
        if (this.commandBox != null) {
            this.commandBox.setValue(this.commandText);
            this.commandBox.clearError();
        }
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
        this.discardArmed = false;
        if (this.commandBox != null) {
            this.commandBox.setValue(this.commandText);
            this.commandBox.clearError();
        }
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
        return !currentText().equals(this.appliedCommandText);
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
        CompoundTag commandData = stack.getTagElement(dataTagKey(stack));
        return commandData == null ? "" : commandData.getString(COMMAND_TAG);
    }

    private void writeCommand(ItemStack stack, String command) {
        String tagKey = dataTagKey(stack);
        CompoundTag rootTag = stack.getTag();
        CompoundTag commandData = rootTag == null ? new CompoundTag() : rootTag.getCompound(tagKey);
        if (command.isEmpty()) {
            commandData.remove(COMMAND_TAG);
        } else {
            commandData.putString(COMMAND_TAG, command);
        }

        if (commandData.isEmpty()) {
            if (rootTag != null) {
                rootTag.remove(tagKey);
                if (rootTag.isEmpty()) {
                    stack.setTag(null);
                }
            }
            return;
        }

        stack.getOrCreateTag().put(tagKey, commandData);
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
}

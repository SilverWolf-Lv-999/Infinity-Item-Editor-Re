package io.github.seraphina.infinity_item_editor_re.client.screen.modern;

import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class ModernTextEditBox extends EditBox {
    private int modernMaxLength = 32;
    private float focusAmount;

    public ModernTextEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        setBordered(false);
        setTextColor(ModernUi.TEXT_PRIMARY);
        setTextColorUneditable(ModernUi.TEXT_MUTED);
    }

    @Override
    public void setMaxLength(int maxLength) {
        this.modernMaxLength = maxLength;
        super.setMaxLength(maxLength);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!this.active || !canConsumeInput()) {
            return false;
        }
        if (isModernAllowedCharacter(codePoint)) {
            insertText(Character.toString(codePoint));
            return true;
        }
        return false;
    }

    @Override
    public void insertText(String text) {
        if (!this.active) {
            return;
        }

        String filtered = filterModernText(text);
        String value = getValue();
        int[] range = getSelectionRange();
        int room = this.modernMaxLength - value.length() + (range[1] - range[0]);
        if (room < filtered.length()) {
            filtered = filtered.substring(0, Math.max(0, room));
        }

        String next = value.substring(0, range[0]) + filtered + value.substring(range[1]);
        super.setValue(next);
        int cursor = range[0] + filtered.length();
        setCursorPosition(cursor);
        setHighlightPos(cursor);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isVisible()) {
            return;
        }

        boolean highlighted = this.active && (isFocused() || isMouseOver(mouseX, mouseY));
        updateFocusAmount(partialTick, highlighted);
        ModernUi.fillToolInput(guiGraphics, getX() - 2, getY() + 1, getX() + getWidth() + 2,
                getY() + getHeight() - 1, highlighted, this.active);
        if (this.focusAmount > 0.0F && this.active) {
            int glow = ModernUi.alpha(0xFFB347, Math.round(32.0F * this.focusAmount));
            guiGraphics.fill(getX() + 2, getY() + getHeight() - 2, getX() + getWidth() - 2, getY() + getHeight() - 1, glow);
        }
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void updateFocusAmount(float partialTick, boolean highlighted) {
        float target = highlighted ? 1.0F : 0.0F;
        float speed = 0.18F + Math.min(partialTick, 1.0F) * 0.12F;
        this.focusAmount += (target - this.focusAmount) * speed;
        if (Math.abs(this.focusAmount - target) < 0.01F) {
            this.focusAmount = target;
        }
    }

    private int[] getSelectionRange() {
        String highlighted = getHighlighted();
        int cursor = getCursorPosition();
        if (highlighted.isEmpty()) {
            return new int[]{cursor, cursor};
        }

        String value = getValue();
        int length = highlighted.length();
        int start;
        int end;
        if (cursor >= length && value.substring(cursor - length, cursor).equals(highlighted)) {
            start = cursor - length;
            end = cursor;
        } else {
            start = cursor;
            end = Math.min(value.length(), cursor + length);
        }
        return new int[]{Math.min(start, end), Math.max(start, end)};
    }

    private static String filterModernText(String input) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char character = input.charAt(i);
            if (isModernAllowedCharacter(character)) {
                builder.append(character);
            }
        }
        return builder.toString();
    }

    private static boolean isModernAllowedCharacter(char character) {
        return SharedConstants.isAllowedChatCharacter(character) || character == ChatFormatting.PREFIX_CODE;
    }
}

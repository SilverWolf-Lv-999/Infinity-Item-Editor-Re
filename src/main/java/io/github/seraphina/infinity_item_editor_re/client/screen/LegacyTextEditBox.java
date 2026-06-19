package io.github.seraphina.infinity_item_editor_re.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

class LegacyTextEditBox extends EditBox {
    private int legacyMaxLength = 32;

    LegacyTextEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    @Override
    public void setMaxLength(int maxLength) {
        this.legacyMaxLength = maxLength;
        super.setMaxLength(maxLength);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!this.active || !canConsumeInput()) {
            return false;
        }
        if (isLegacyAllowedCharacter(codePoint)) {
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

        String filtered = filterLegacyText(text);
        String value = getValue();
        int[] range = getSelectionRange();
        int room = this.legacyMaxLength - value.length() + (range[1] - range[0]);
        if (room < filtered.length()) {
            filtered = filtered.substring(0, Math.max(0, room));
        }

        String next = value.substring(0, range[0]) + filtered + value.substring(range[1]);
        super.setValue(next);
        int cursor = range[0] + filtered.length();
        setCursorPosition(cursor);
        setHighlightPos(cursor);
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

    private static String filterLegacyText(String input) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char character = input.charAt(i);
            if (isLegacyAllowedCharacter(character)) {
                builder.append(character);
            }
        }
        return builder.toString();
    }

    private static boolean isLegacyAllowedCharacter(char character) {
        return SharedConstants.isAllowedChatCharacter(character) || character == ChatFormatting.PREFIX_CODE;
    }
}

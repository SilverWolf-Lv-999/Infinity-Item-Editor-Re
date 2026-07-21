package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.Config;
import io.github.seraphina.infinity_item_editor_re.client.screen.modern.ModernUi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

class FixedDigitEditBox extends EditBox {
    private static final int DISABLED_COLOR = 0xFF707070;

    private final Font font;
    private final int digits;
    private final int minValue;
    private final int maxValue;
    private int digitCursor;
    private int cursorFrame;

    FixedDigitEditBox(Font font, int x, int y, int width, int height, int digits, int minValue, int maxValue) {
        super(font, x, y, width, height, Component.empty());
        this.font = font;
        this.digits = Math.max(1, digits);
        this.minValue = minValue;
        this.maxValue = maxValue;
        super.setMaxLength(this.digits + (minValue < 0 ? 1 : 0));
        super.setTextColor(InfinityEditorButton.MAIN_COLOR);
        super.setTextColorUneditable(DISABLED_COLOR);
        setFixedValue(minValue);
    }

    @Override
    public void tick() {
        this.cursorFrame++;
    }

    @Override
    public void setValue(String value) {
        setFixedValue(parseFixedValue(value));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.active || !canConsumeInput()) {
            return false;
        }

        if (Screen.isCopy(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(getValue());
            return true;
        }
        if (Screen.isPaste(keyCode)) {
            return true;
        }
        if (Screen.isCut(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(getValue());
            setFixedValue(0);
            return true;
        }

        return switch (keyCode) {
            case 259, 261 -> {
                replaceDigit('0');
                yield true;
            }
            case 262 -> {
                moveDigitCursor(1);
                yield true;
            }
            case 263 -> {
                moveDigitCursor(-1);
                yield true;
            }
            case 268 -> {
                setDigitCursorPosition(0);
                yield true;
            }
            case 269 -> {
                setDigitCursorPosition(this.digits - 1);
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!this.active || !canConsumeInput()) {
            return false;
        }

        if (codePoint == '-' && this.minValue < 0) {
            setFixedValue(-Math.abs(parseFixedValue(getValue())));
            return true;
        }
        if (codePoint == '+' && this.maxValue >= 0) {
            setFixedValue(Math.abs(parseFixedValue(getValue())));
            return true;
        }
        if (codePoint >= '0' && codePoint <= '9') {
            replaceDigit(codePoint);
            moveDigitCursor(1);
            return true;
        }
        return true;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int localX = Mth.floor(mouseX) - getX() - 4;
        String value = getValue();
        int cursor = this.font.plainSubstrByWidth(value, Math.max(0, localX)).length();
        if (value.startsWith("-")) {
            cursor--;
        }
        setDigitCursorPosition(cursor);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isVisible()) {
            return;
        }

        boolean sidebarUi = Config.getItemGuiMode() == Config.ItemEditorUiMode.SIDEBAR;
        int color = this.active ? (sidebarUi ? ModernUi.TEXT_PRIMARY : InfinityEditorButton.MAIN_COLOR) : DISABLED_COLOR;
        if (sidebarUi) {
            ModernUi.fillInset(guiGraphics, getX() - 2, getY() + 1, getX() + getWidth() + 2, getY() + getHeight() - 1,
                    6, this.active && (isFocused() || isMouseOver(mouseX, mouseY)), this.active);
        } else {
            guiGraphics.fill(getX() - 1, getY() - 1, getX() + getWidth() + 1, getY() + getHeight() + 1, color);
            guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), InfinityEditorButton.ALT_COLOR);
        }

        String value = getValue();
        int textX = getX() + 4;
        int textY = getY() + (getHeight() - 8) / 2;
        int cursorStringPosition = getCursorStringPosition();
        int cursorX = textX;

        if (!value.isEmpty()) {
            String beforeCursor = value.substring(0, Math.min(cursorStringPosition, value.length()));
            if (!beforeCursor.isEmpty()) {
                cursorX = guiGraphics.drawString(this.font, beforeCursor, textX, textY, color, !sidebarUi) - 1;
            }

            if (cursorStringPosition < value.length()) {
                guiGraphics.drawString(this.font, value.substring(cursorStringPosition), cursorX, textY, color, !sidebarUi);
            }
        }

        if (isFocused() && this.cursorFrame / 6 % 2 == 0) {
            guiGraphics.drawString(this.font, "_", cursorX, textY,
                    sidebarUi ? ModernUi.ACCENT_HOVER : InfinityEditorButton.CONTRAST_COLOR, !sidebarUi);
        }
    }

    private void replaceDigit(char digit) {
        char[] characters = getDigitCharacters();
        characters[this.digitCursor] = digit;
        int value = parseFixedValue(new String(characters));
        if (getValue().startsWith("-")) {
            value = -value;
        }
        int cursor = this.digitCursor;
        setFixedValue(value);
        setDigitCursorPosition(cursor);
    }

    private void moveDigitCursor(int amount) {
        setDigitCursorPosition(this.digitCursor + amount);
    }

    private void setDigitCursorPosition(int position) {
        this.digitCursor = Mth.clamp(position, 0, this.digits - 1);
        syncSuperCursor();
    }

    private void setFixedValue(int value) {
        int clamped = Mth.clamp(value, this.minValue, this.maxValue);
        super.setValue(formatFixedValue(clamped));
        this.digitCursor = Mth.clamp(this.digitCursor, 0, this.digits - 1);
        syncSuperCursor();
    }

    private void syncSuperCursor() {
        int cursor = getCursorStringPosition();
        super.setCursorPosition(cursor);
        super.setHighlightPos(cursor);
    }

    private int getCursorStringPosition() {
        return getValue().startsWith("-") ? this.digitCursor + 1 : this.digitCursor;
    }

    private char[] getDigitCharacters() {
        String value = getValue();
        String rawDigits = value.startsWith("-") ? value.substring(1) : value;
        if (rawDigits.length() < this.digits) {
            rawDigits = "0".repeat(this.digits - rawDigits.length()) + rawDigits;
        } else if (rawDigits.length() > this.digits) {
            rawDigits = rawDigits.substring(rawDigits.length() - this.digits);
        }
        return rawDigits.toCharArray();
    }

    private String formatFixedValue(int value) {
        boolean negative = value < 0;
        String rawDigits = Integer.toString(Math.abs(value));
        if (rawDigits.length() < this.digits) {
            rawDigits = "0".repeat(this.digits - rawDigits.length()) + rawDigits;
        }
        return negative ? "-" + rawDigits : rawDigits;
    }

    private int parseFixedValue(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }

        boolean negative = value.startsWith("-");
        long parsed = 0L;
        for (int i = negative ? 1 : 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character >= '0' && character <= '9') {
                parsed = parsed * 10L + character - '0';
                if (parsed > Integer.MAX_VALUE) {
                    return negative ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                }
            }
        }
        return (int) (negative ? -parsed : parsed);
    }
}

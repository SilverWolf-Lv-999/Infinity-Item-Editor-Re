package io.github.seraphina.infinity_item_editor_re.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;

import java.util.Objects;
import java.util.function.Predicate;

public class FilteredEditBox extends EditBox {
    private Predicate<String> filter = value -> true;
    private int filteredMaxLength = 32;

    public FilteredEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    public static void setFilter(EditBox box, Predicate<String> filter) {
        if (!(box instanceof FilteredEditBox filteredBox)) {
            throw new IllegalArgumentException("Edit box does not support input filtering");
        }
        filteredBox.setFilter(filter);
    }

    public void setFilter(Predicate<String> filter) {
        this.filter = Objects.requireNonNull(filter);
    }

    @Override
    public void setMaxLength(int maxLength) {
        this.filteredMaxLength = maxLength;
        super.setMaxLength(maxLength);
    }

    @Override
    public void setValue(String value) {
        if (this.filter.test(value)) {
            super.setValue(value);
        }
    }

    @Override
    public void insertText(String input) {
        String value = getValue();
        int[] range = getSelectionRange();
        int room = this.filteredMaxLength - value.length() + range[1] - range[0];
        if (room <= 0) {
            return;
        }

        String filteredInput = StringUtil.filterText(input);
        if (filteredInput.length() > room) {
            int end = room;
            if (Character.isHighSurrogate(filteredInput.charAt(end - 1))) {
                end--;
            }
            filteredInput = filteredInput.substring(0, end);
        }

        String next = value.substring(0, range[0]) + filteredInput + value.substring(range[1]);
        if (!this.filter.test(next)) {
            return;
        }

        super.setValue(next);
        int cursor = range[0] + filteredInput.length();
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
}

package io.github.seraphina.infinity_item_editor_re.client.screen;

import net.minecraft.client.gui.GuiGraphics;

final class EditorBackgrounds {
    private static final int TOP = 0xF0101113;
    private static final int BOTTOM = 0xF0202224;

    private EditorBackgrounds() {
    }

    static void render(GuiGraphics guiGraphics, int width, int height) {
        guiGraphics.fillGradient(0, 0, width, height, TOP, BOTTOM);
    }
}

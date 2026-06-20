package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class InfinityEditorButton extends AbstractButton {
    public static final int MAIN_COLOR = 0xFF9600C8;
    public static final int ALT_COLOR = 0xFF32144B;
    public static final int CONTRAST_COLOR = 0xFF0064FF;
    private static final int DISABLED_COLOR = 0xFFF44262;
    private static final int LIGHT_SHADE = 0x1AFFFFFF;
    private static final int DARK_SHADE = 0x32000000;
    private static final int SIDEBAR_FILL = 0xB4211A35;
    private static final int SIDEBAR_HOVER_FILL = 0xE0322A58;
    private static final int SIDEBAR_ACCENT = 0xFF2EC8FF;
    private static final int SIDEBAR_BORDER = 0x887E5CC8;

    private final PressAction onPress;

    public InfinityEditorButton(int x, int y, int width, int height, Component message, PressAction onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (Config.getItemGuiMode() == Config.ItemEditorUiMode.SIDEBAR) {
            renderSidebarWidget(guiGraphics);
            return;
        }

        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        guiGraphics.fill(x, y, x + width, y + height, ALT_COLOR);
        guiGraphics.fill(x, y, x + width, y + 1, LIGHT_SHADE);
        guiGraphics.fill(x, y, x + 1, y + height, LIGHT_SHADE);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, DARK_SHADE);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, DARK_SHADE);

        int textColor = this.active ? (this.isHoveredOrFocused() ? CONTRAST_COLOR : MAIN_COLOR) : DISABLED_COLOR;
        renderScrollingString(guiGraphics, Minecraft.getInstance().font, 2, textColor);
    }

    private void renderSidebarWidget(GuiGraphics guiGraphics) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        int fillColor = this.isHoveredOrFocused() && this.active ? SIDEBAR_HOVER_FILL : SIDEBAR_FILL;

        guiGraphics.fill(x, y, x + width, y + height, fillColor);
        guiGraphics.fill(x, y, x + width, y + 1, SIDEBAR_BORDER);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, DARK_SHADE);
        guiGraphics.fill(x, y, x + 2, y + height, this.isHoveredOrFocused() ? SIDEBAR_ACCENT : SIDEBAR_BORDER);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, DARK_SHADE);
        if (this.isHoveredOrFocused() && this.active) {
            guiGraphics.fill(x + 2, y + 2, x + width - 2, y + 3, 0x552EC8FF);
            guiGraphics.fill(x + 2, y + height - 3, x + width - 2, y + height - 2, 0x332EC8FF);
        }

        var font = Minecraft.getInstance().font;
        int textColor = this.active ? (this.isHoveredOrFocused() ? SIDEBAR_ACCENT : MAIN_COLOR) : DISABLED_COLOR;
        int padding = width <= 20 ? 2 : 5;
        Component text = getMessage();
        int textWidth = font.width(text);
        int textX = x + Math.max(padding, (width - textWidth) / 2);
        int textY = y + (height - font.lineHeight) / 2;
        guiGraphics.drawString(font, text, textX, textY, textColor, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    @FunctionalInterface
    public interface PressAction {
        void onPress(InfinityEditorButton button);
    }
}

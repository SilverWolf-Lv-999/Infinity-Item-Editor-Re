package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.Config;
import io.github.seraphina.infinity_item_editor_re.client.screen.modern.ModernUi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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

    private final PressAction onPress;
    private float hoverAmount;

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
        updateHoverAmount(partialTick);
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
        boolean highlighted = this.isHoveredOrFocused() && this.active;
        if (width <= 16 || height <= 16) {
            ModernUi.fillToolPaletteCell(guiGraphics, x, y, x + width, y + height, highlighted, this.active);
        } else {
            ModernUi.fillToolButton(guiGraphics, x, y, x + width, y + height, highlighted, this.active, this.hoverAmount);
        }

        var font = Minecraft.getInstance().font;
        int textColor = this.active
                ? ModernUi.lerpColor(ModernUi.TEXT_PRIMARY, ModernUi.ACCENT_HOVER, this.hoverAmount)
                : 0xFF6D7875;
        int inset = width <= 20 ? 1 : 7;
        renderCenteredModernText(guiGraphics, font, x + inset, y, x + width - inset, y + height, textColor);
    }

    private void renderCenteredModernText(GuiGraphics guiGraphics, Font font, int left, int top, int right, int bottom, int color) {
        int maxWidth = Math.max(0, right - left);
        int centerX = (left + right) / 2;
        int textY = top + (bottom - top - 8) / 2;
        Component message = getMessage();
        if (font.width(message) <= maxWidth) {
            guiGraphics.drawCenteredString(font, message, centerX, textY, color);
            return;
        }

        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        String text = message.getString();
        String clipped = maxWidth <= ellipsisWidth
                ? font.plainSubstrByWidth(text, maxWidth)
                : font.plainSubstrByWidth(text, maxWidth - ellipsisWidth) + ellipsis;
        guiGraphics.drawCenteredString(font, clipped, centerX, textY, color);
    }

    private void updateHoverAmount(float partialTick) {
        float target = this.active && this.isHoveredOrFocused() ? 1.0F : 0.0F;
        float speed = 0.18F + Math.min(partialTick, 1.0F) * 0.12F;
        this.hoverAmount += (target - this.hoverAmount) * speed;
        if (Math.abs(this.hoverAmount - target) < 0.01F) {
            this.hoverAmount = target;
        }
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

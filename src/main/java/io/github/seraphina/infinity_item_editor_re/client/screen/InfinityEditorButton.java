package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.Config;
import io.github.seraphina.infinity_item_editor_re.client.screen.modern.ModernUi;
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
        int fillColor = this.active
                ? ModernUi.lerpColor(ModernUi.SURFACE, ModernUi.SURFACE_HOVER, this.hoverAmount)
                : ModernUi.SURFACE_DISABLED;
        int borderColor = highlighted ? ModernUi.BORDER_STRONG : ModernUi.BORDER;
        int radius = Math.min(8, Math.max(4, height / 2));

        if (highlighted) {
            ModernUi.fillRounded(guiGraphics, x - 1, y - 1, x + width + 1, y + height + 1, radius + 1,
                    ModernUi.alpha(0x62D6FF, Math.round(32.0F * this.hoverAmount)));
        }
        ModernUi.fillPill(guiGraphics, x, y, x + width, y + height, radius, fillColor, borderColor);
        if (highlighted) {
            int accentWidth = Math.min(3 + Math.round(this.hoverAmount * 3.0F), Math.max(2, width / 4));
            guiGraphics.fill(x + 2, y + 4, x + 2 + accentWidth, y + height - 4, ModernUi.ACCENT_HOVER);
            guiGraphics.fill(x + 6, y + 1, x + width - 6, y + 2, ModernUi.alpha(0xFFFFFF, 42));
        }

        var font = Minecraft.getInstance().font;
        int textColor = this.active
                ? ModernUi.lerpColor(ModernUi.TEXT_PRIMARY, ModernUi.ACCENT_HOVER, this.hoverAmount)
                : 0xFF6D7875;
        renderScrollingString(guiGraphics, font, width <= 20 ? 1 : 4, textColor);
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

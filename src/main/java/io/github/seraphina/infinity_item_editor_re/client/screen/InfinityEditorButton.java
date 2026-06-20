package io.github.seraphina.infinity_item_editor_re.client.screen;

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

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    @FunctionalInterface
    public interface PressAction {
        void onPress(InfinityEditorButton button);
    }
}

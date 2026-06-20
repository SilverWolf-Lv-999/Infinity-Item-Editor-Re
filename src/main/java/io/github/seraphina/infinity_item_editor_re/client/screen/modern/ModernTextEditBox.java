package io.github.seraphina.infinity_item_editor_re.client.screen.modern;

import io.github.seraphina.infinity_item_editor_re.client.screen.legacy.LegacyTextEditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ModernTextEditBox extends LegacyTextEditBox {
    private float focusAmount;

    public ModernTextEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        setBordered(false);
        setTextColor(ModernUi.TEXT_PRIMARY);
        setTextColorUneditable(ModernUi.TEXT_MUTED);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isVisible()) {
            return;
        }

        boolean highlighted = this.active && (isFocused() || isMouseOver(mouseX, mouseY));
        updateFocusAmount(partialTick, highlighted);
        ModernUi.fillInset(guiGraphics, getX() - 3, getY() - 2, getX() + getWidth() + 3, getY() + getHeight() + 2,
                6, highlighted, this.active);
        if (this.focusAmount > 0.0F && this.active) {
            int glow = ModernUi.alpha(0x62D6FF, Math.round(26.0F * this.focusAmount));
            guiGraphics.fill(getX() + 2, getY() + getHeight(), getX() + getWidth() - 2, getY() + getHeight() + 1, glow);
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
}

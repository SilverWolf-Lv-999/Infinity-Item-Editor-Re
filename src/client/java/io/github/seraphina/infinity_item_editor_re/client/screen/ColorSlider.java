package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.Config;
import io.github.seraphina.infinity_item_editor_re.client.screen.modern.ModernUi;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.IntConsumer;

class ColorSlider extends AbstractSliderButton {
    private final Component label;
    private final IntConsumer responder;
    private float hoverAmount;

    ColorSlider(int x, int y, int width, int height, Component label, int value, IntConsumer responder) {
        super(x, y, width, height, Component.empty(), Mth.clamp(value, 0, 255) / 255.0D);
        this.label = label;
        this.responder = responder;
        updateMessage();
    }

    int getIntValue() {
        return Mth.clamp((int) Math.round(this.value * 255.0D), 0, 255);
    }

    void setIntValue(int value) {
        this.value = Mth.clamp(value, 0, 255) / 255.0D;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(this.label.getString() + ": " + getIntValue()));
    }

    @Override
    protected void applyValue() {
        this.responder.accept(getIntValue());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (Config.getItemGuiMode() != Config.ItemEditorUiMode.SIDEBAR) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }

        updateHoverAmount(partialTick);

        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        boolean highlighted = this.isHoveredOrFocused() && this.active;
        ModernUi.fillToolButton(guiGraphics, x, y, x + width, y + height, highlighted, this.active, this.hoverAmount);

        int trackLeft = x + 7;
        int trackRight = x + width - 7;
        int trackY = y + height - 5;
        ModernUi.fillRounded(guiGraphics, trackLeft, trackY - 2, trackRight, trackY + 3, 2, ModernUi.alpha(0x000000, 92));
        guiGraphics.fill(trackLeft + 1, trackY - 1, trackRight - 1, trackY, ModernUi.alpha(0xFFFFFF, 24));
        int knobX = (int) (trackLeft + (trackRight - trackLeft) * this.value);
        ModernUi.fillRounded(guiGraphics, trackLeft + 1, trackY - 1, knobX, trackY + 2, 2,
                ModernUi.lerpColor(ModernUi.ACCENT, ModernUi.WARM, this.hoverAmount));
        ModernUi.fillRounded(guiGraphics, knobX - 5, trackY - 5, knobX + 6, trackY + 7, 3, ModernUi.SHADOW_SOFT);
        ModernUi.fillRounded(guiGraphics, knobX - 4, trackY - 5, knobX + 5, trackY + 6, 3,
                ModernUi.lerpColor(ModernUi.ACCENT, ModernUi.ACCENT_HOVER, this.hoverAmount));
        guiGraphics.fill(knobX - 2, trackY - 3, knobX + 3, trackY - 2, ModernUi.alpha(0xFFFFFF, 95));
        guiGraphics.fill(knobX - 2, trackY + 3, knobX + 3, trackY + 4, ModernUi.alpha(0x000000, 70));

        var font = Minecraft.getInstance().font;
        int textColor = ModernUi.lerpColor(ModernUi.TEXT_PRIMARY, ModernUi.ACCENT_HOVER, this.hoverAmount);
        guiGraphics.drawCenteredString(font, getMessage(), x + width / 2, y + 4, textColor);
    }

    private void updateHoverAmount(float partialTick) {
        float target = this.active && this.isHoveredOrFocused() ? 1.0F : 0.0F;
        float speed = 0.16F + Math.min(partialTick, 1.0F) * 0.10F;
        this.hoverAmount += (target - this.hoverAmount) * speed;
        if (Math.abs(this.hoverAmount - target) < 0.01F) {
            this.hoverAmount = target;
        }
    }
}

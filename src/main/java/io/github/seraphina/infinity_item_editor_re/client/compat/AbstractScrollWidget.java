package io.github.seraphina.infinity_item_editor_re.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Backport of the small portion of the modern scroll widget API used by the JSON editor.
 */
public abstract class AbstractScrollWidget extends AbstractWidget {
    private static final int INNER_PADDING = 4;
    private double scrollAmount;

    protected AbstractScrollWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }

    public void setFocus(boolean focused) {
        setFocused(focused);
    }

    protected int innerPadding() {
        return INNER_PADDING;
    }

    protected int totalInnerPadding() {
        return INNER_PADDING * 2;
    }

    protected double scrollAmount() {
        return this.scrollAmount;
    }

    protected void setScrollAmount(double amount) {
        this.scrollAmount = Mth.clamp(amount, 0.0D, getMaxScrollAmount());
    }

    protected boolean withinContentAreaPoint(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + this.width - 6
                && mouseY >= this.y && mouseY < this.y + this.height;
    }

    protected abstract int getInnerHeight();

    protected abstract double scrollRate();

    protected abstract void renderBackground(GuiGraphics guiGraphics);

    protected abstract void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    protected abstract void updateWidgetNarration(NarrationElementOutput narrationElementOutput);

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        GuiGraphics guiGraphics = GuiGraphics.wrap(poseStack);
        renderBackground(guiGraphics);
        guiGraphics.enableScissor(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1);
        guiGraphics.pushPose();
        guiGraphics.translate(0.0D, -this.scrollAmount, 0.0D);
        renderContents(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.popPose();
        guiGraphics.disableScissor();
        renderScrollbar(guiGraphics);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!this.visible || !this.active || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        setScrollAmount(this.scrollAmount - delta * scrollRate());
        return true;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        updateWidgetNarration(narrationElementOutput);
    }

    private int getMaxScrollAmount() {
        return Math.max(0, getInnerHeight() + totalInnerPadding() - this.height);
    }

    private void renderScrollbar(GuiGraphics guiGraphics) {
        int maxScroll = getMaxScrollAmount();
        if (maxScroll <= 0) {
            return;
        }

        int barLeft = this.x + this.width - 6;
        int barHeight = Math.max(16, this.height * this.height / (getInnerHeight() + totalInnerPadding()));
        int barTop = this.y + (int) (this.scrollAmount * (this.height - barHeight) / maxScroll);
        guiGraphics.fill(barLeft, this.y, this.x + this.width, this.y + this.height, 0x66000000);
        guiGraphics.fill(barLeft + 1, barTop, this.x + this.width - 1, barTop + barHeight, 0xFF808080);
    }
}

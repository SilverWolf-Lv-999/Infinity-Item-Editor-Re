package io.github.seraphina.infinity_item_editor_re.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Keeps tooltip bounds and scissor rectangles in sync with the scaled editor canvas. */
final class EditorGuiGraphics extends GuiGraphics {
    private final EditorViewport viewport;

    EditorGuiGraphics(Minecraft minecraft, GuiGraphics target, EditorViewport viewport) {
        super(minecraft, minecraft.renderBuffers().bufferSource());
        this.viewport = viewport;
        pose().last().pose().set(target.pose().last().pose());
        pose().last().normal().set(target.pose().last().normal());
        pose().scale((float) viewport.scale(), (float) viewport.scale(), 1.0F);
    }

    @Override
    public int guiWidth() {
        return this.viewport.width();
    }

    @Override
    public int guiHeight() {
        return this.viewport.height();
    }

}

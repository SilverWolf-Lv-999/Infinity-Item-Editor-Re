package io.github.seraphina.infinity_item_editor_re.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector3f;

/** Keeps tooltip bounds and scissor rectangles in sync with the scaled editor canvas. */
final class EditorGuiGraphics extends GuiGraphics {
    private final GuiGraphics target;
    private final EditorViewport viewport;

    EditorGuiGraphics(Minecraft minecraft, GuiGraphics target, EditorViewport viewport) {
        super(minecraft, target.bufferSource());
        this.target = target;
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

    @Override
    public void enableScissor(int left, int top, int right, int bottom) {
        flush();
        Vector3f start = pose().last().pose().transformPosition(new Vector3f(left, top, 0));
        Vector3f end = pose().last().pose().transformPosition(new Vector3f(right, bottom, 0));
        this.target.enableScissor((int) Math.floor(start.x()), (int) Math.floor(start.y()),
                (int) Math.ceil(end.x()), (int) Math.ceil(end.y()));
    }

    @Override
    public void disableScissor() {
        flush();
        this.target.disableScissor();
    }
}

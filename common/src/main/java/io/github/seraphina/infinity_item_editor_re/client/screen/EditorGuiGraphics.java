package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.seraphina.infinity_item_editor_re.mixin.EditorRenderStateAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Shares the frame's render state while keeping tooltip bounds in editor coordinates. */
final class EditorGuiGraphics extends GuiGraphicsExtractor {
    private final GuiGraphicsExtractor target;
    private final EditorViewport viewport;

    EditorGuiGraphics(Minecraft minecraft, GuiGraphicsExtractor target, EditorViewport viewport, int mouseX, int mouseY) {
        super(minecraft, ((EditorRenderStateAccessor) target).infinityItemEditorRe$getGuiRenderState(), mouseX, mouseY);
        this.target = target;
        this.viewport = viewport;
        pose().set(target.pose());
        pose().scale((float) viewport.scale(), (float) viewport.scale());
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
    public void requestCursor(CursorType cursor) {
        this.target.requestCursor(cursor);
    }

    @Override
    public boolean containsPointInScissor(int x, int y) {
        return super.containsPointInScissor((int) (x * this.viewport.scale()), (int) (y * this.viewport.scale()));
    }
}

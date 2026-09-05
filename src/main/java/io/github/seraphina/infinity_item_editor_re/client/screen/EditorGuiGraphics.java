package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;
import io.github.seraphina.infinity_item_editor_re.client.compat.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;

/** Bridges the editor pose to 1.18's item renderer, which uses the global model-view stack. */
final class EditorGuiGraphics extends GuiGraphics {
    EditorGuiGraphics(GuiGraphics target, EditorViewport viewport) {
        super(target);
        scale((float) viewport.scale(), (float) viewport.scale(), 1.0F);
    }

    @Override
    public void renderItem(ItemStack stack, int x, int y) {
        withItemPose(() -> super.renderItem(stack, x, y));
    }

    @Override
    public void renderItemDecorations(Font font, ItemStack stack, int x, int y) {
        withItemPose(() -> super.renderItemDecorations(font, stack, x, y));
    }

    @Override
    public void renderItemDecorations(Font font, ItemStack stack, int x, int y, String countText) {
        withItemPose(() -> super.renderItemDecorations(font, stack, x, y, countText));
    }

    private void withItemPose(Runnable render) {
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.mulPoseMatrix(last().pose());
        RenderSystem.applyModelViewMatrix();
        try {
            render.run();
        } finally {
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    @Override
    public void enableScissor(int left, int top, int right, int bottom) {
        Vector4f start = new Vector4f(left, top, 0, 1);
        Vector4f end = new Vector4f(right, bottom, 0, 1);
        start.transform(last().pose());
        end.transform(last().pose());
        super.enableScissor((int) Math.floor(start.x()), (int) Math.floor(start.y()),
                (int) Math.ceil(end.x()), (int) Math.ceil(end.y()));
    }
}

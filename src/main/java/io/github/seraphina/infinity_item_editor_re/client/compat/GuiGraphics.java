package io.github.seraphina.infinity_item_editor_re.client.compat;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/**
 * Small source-compatibility bridge for code written against the 1.20 GUI API.
 */
public class GuiGraphics extends PoseStack {
    public GuiGraphics() {
    }

    public GuiGraphics(PoseStack poseStack) {
        if (poseStack != null) {
            mulPoseMatrix(poseStack.last().pose());
        }
    }

    public static GuiGraphics wrap(PoseStack poseStack) {
        return poseStack instanceof GuiGraphics guiGraphics ? guiGraphics : new GuiGraphics(poseStack);
    }

    public PoseStack pose() {
        return this;
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        GuiComponent.fill(this, x1, y1, x2, y2, color);
    }

    public void fillGradient(int x1, int y1, int x2, int y2, int topColor, int bottomColor) {
        GuiComponent.fill(this, x1, y1, x2, y2, topColor);
    }

    public void hLine(int minX, int maxX, int y, int color) {
        GuiComponent.fill(this, Math.min(minX, maxX), y, Math.max(minX, maxX) + 1, y + 1, color);
    }

    public int drawString(Font font, String text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, String text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(this, text, x, y, color) : font.draw(this, text, x, y, color);
    }

    public int drawString(Font font, Component text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(this, text, x, y, color) : font.draw(this, text, x, y, color);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(this, text, x, y, color) : font.draw(this, text, x, y, color);
    }

    public void drawCenteredString(Font font, String text, int x, int y, int color) {
        GuiComponent.drawCenteredString(this, font, text, x, y, color);
    }

    public void drawCenteredString(Font font, Component text, int x, int y, int color) {
        GuiComponent.drawCenteredString(this, font, text, x, y, color);
    }

    public void drawCenteredString(Font font, FormattedCharSequence text, int x, int y, int color) {
        GuiComponent.drawCenteredString(this, font, text, x, y, color);
    }

    public void renderItem(ItemStack stack, int x, int y) {
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(stack, x, y);
    }

    public void renderItemDecorations(Font font, ItemStack stack, int x, int y) {
        Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(font, stack, x, y);
    }

    public void renderItemDecorations(Font font, ItemStack stack, int x, int y, String countText) {
        Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(font, stack, x, y, countText);
    }

    public void renderTooltip(Font font, Component text, int x, int y) {
        if (Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().screen.renderTooltip(this, text, x, y);
        }
    }

    public void renderTooltip(Font font, ItemStack stack, int x, int y) {
        if (Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().screen.renderComponentTooltip(
                    this,
                    Minecraft.getInstance().screen.getTooltipFromItem(stack),
                    x,
                    y,
                    stack
            );
        }
    }

    public void renderComponentTooltip(Font font, java.util.List<? extends Component> lines, int x, int y) {
        if (Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().screen.renderComponentTooltip(this, lines, x, y, font);
        }
    }

    public void blit(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(this, x, y, u, v, width, height, 256, 256);
    }

    public void enableScissor(int x1, int y1, int x2, int y2) {
        Window window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();
        int x = (int) Math.floor(x1 * scale);
        int y = (int) Math.floor(window.getHeight() - y2 * scale);
        int width = Math.max(0, (int) Math.ceil((x2 - x1) * scale));
        int height = Math.max(0, (int) Math.ceil((y2 - y1) * scale));
        RenderSystem.enableScissor(x, y, width, height);
    }

    public void disableScissor() {
        RenderSystem.disableScissor();
    }
}

package io.github.seraphina.infinity_item_editor_re.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphics.class)
public interface EditorRenderStateAccessor {
    @Accessor("guiRenderState")
    GuiRenderState infinityItemEditorRe$getGuiRenderState();
}

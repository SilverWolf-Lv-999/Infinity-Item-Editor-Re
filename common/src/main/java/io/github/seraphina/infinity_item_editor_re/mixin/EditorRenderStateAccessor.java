package io.github.seraphina.infinity_item_editor_re.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphicsExtractor.class)
public interface EditorRenderStateAccessor {
    @Accessor("guiRenderState")
    GuiRenderState infinityItemEditorRe$getGuiRenderState();
}

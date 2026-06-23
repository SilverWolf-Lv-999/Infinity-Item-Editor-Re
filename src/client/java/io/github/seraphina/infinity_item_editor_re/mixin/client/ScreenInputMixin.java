package io.github.seraphina.infinity_item_editor_re.mixin.client;

import io.github.seraphina.infinity_item_editor_re.eventhandlers.ClientEvents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class ScreenInputMixin {
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void infinityItemEditorRe$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (ClientEvents.handleScreenKeyPressed((Screen) (Object) this, keyCode, scanCode)) {
            callbackInfo.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"), cancellable = true)
    private void infinityItemEditorRe$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (ClientEvents.handleScreenMousePressed((Screen) (Object) this, button)) {
            callbackInfo.setReturnValue(true);
        }
    }
}

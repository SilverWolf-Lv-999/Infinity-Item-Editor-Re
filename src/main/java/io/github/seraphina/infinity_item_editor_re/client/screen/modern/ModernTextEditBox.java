package io.github.seraphina.infinity_item_editor_re.client.screen.modern;

import io.github.seraphina.infinity_item_editor_re.client.screen.legacy.LegacyTextEditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ModernTextEditBox extends LegacyTextEditBox {
    public ModernTextEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        setBordered(false);
        setTextColor(ModernUi.TEXT_PRIMARY);
        setTextColorUneditable(ModernUi.TEXT_MUTED);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isVisible()) {
            return;
        }

        ModernUi.fillInset(guiGraphics, getX() - 3, getY() - 2, getX() + getWidth() + 3, getY() + getHeight() + 2,
                5, isFocused(), this.active);
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }
}

package io.github.seraphina.infinity_item_editor_re.client.screen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.IntConsumer;

class ColorSlider extends AbstractSliderButton {
    private final Component label;
    private final IntConsumer responder;

    ColorSlider(int x, int y, int width, int height, Component label, int value, IntConsumer responder) {
        super(x, y, width, height, Component.empty(), Mth.clamp(value, 0, 255) / 255.0D);
        this.label = label;
        this.responder = responder;
        updateMessage();
    }

    private int getIntValue() {
        return Mth.clamp((int) Math.round(this.value * 255.0D), 0, 255);
    }

    void setIntValue(int value) {
        this.value = Mth.clamp(value, 0, 255) / 255.0D;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(this.label.getString() + ": " + getIntValue()));
    }

    @Override
    protected void applyValue() {
        this.responder.accept(getIntValue());
    }
}

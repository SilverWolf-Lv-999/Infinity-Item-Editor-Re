package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.config.ClientConfig;
import io.github.seraphina.infinity_item_editor_re.util.MinecraftCompat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class CustomTextColorScreen extends CompatScreen {
    private final Screen parent;
    private FilteredEditBox hexBox;
    private ColorSlider redSlider;
    private ColorSlider greenSlider;
    private ColorSlider blueSlider;
    private InfinityEditorButton addButton;
    private Component status = Component.empty();
    private int color = 0xFFFFFF;
    private boolean syncing;

    CustomTextColorScreen(Screen parent) {
        super(Component.translatable(key("text_color")));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int sliderWidth = Math.min(240, Math.max(140, this.width - 80));
        int sliderX = (this.width - sliderWidth) / 2;
        this.hexBox = new FilteredEditBox(this.font, (this.width - 90) / 2, 54, 90, 20,
                Component.translatable(key("color.hex")));
        this.hexBox.setMaxLength(7);
        FilteredEditBox.setFilter(this.hexBox, value -> value.matches("#?[0-9a-fA-F]{0,6}"));
        this.hexBox.setValue(formatHex(this.color));
        this.hexBox.setResponder(value -> {
            if (this.syncing) {
                return;
            }
            int parsed = parseHex(value);
            if (this.addButton != null) {
                this.addButton.active = parsed >= 0;
            }
            if (parsed >= 0) {
                this.color = parsed;
                syncSliders();
                this.status = Component.empty();
            }
        });
        addRenderableWidget(this.hexBox);

        this.redSlider = addRenderableWidget(new ColorSlider(sliderX, 92, sliderWidth, 22,
                Component.translatable(key("color.red")), getRed(this.color), value -> setComponent(16, value)));
        this.greenSlider = addRenderableWidget(new ColorSlider(sliderX, 126, sliderWidth, 22,
                Component.translatable(key("color.green")), getGreen(this.color), value -> setComponent(8, value)));
        this.blueSlider = addRenderableWidget(new ColorSlider(sliderX, 160, sliderWidth, 22,
                Component.translatable(key("color.blue")), getBlue(this.color), value -> setComponent(0, value)));

        int buttonWidth = 72;
        int gap = 8;
        int buttonY = this.height - 42;
        int totalWidth = buttonWidth * 2 + gap;
        int buttonX = (this.width - totalWidth) / 2;
        this.addButton = addRenderableWidget(new InfinityEditorButton(buttonX, buttonY, buttonWidth, 20,
                Component.translatable(key("text_color.add")), button -> addColor()));
        addRenderableWidget(new InfinityEditorButton(buttonX + buttonWidth + gap, buttonY, buttonWidth, 20,
                Component.translatable(key("back")), button -> returnToParent()));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        EditorBackgrounds.render(guiGraphics, this.width, this.height);
        guiGraphics.fill((this.width - 42) / 2, 20, (this.width + 42) / 2, 42, 0xFF000000 | this.color);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);
        if (!this.status.getString().isEmpty()) {
            guiGraphics.centeredText(this.font, this.status, this.width / 2, this.height - 58, 0xFFFFD966);
        }
    }

    @Override
    public void onClose() {
        returnToParent();
    }

    private void setComponent(int shift, int value) {
        this.color = (this.color & ~(0xFF << shift)) | (value << shift);
        syncControls();
        this.status = Component.empty();
    }

    private void syncControls() {
        this.syncing = true;
        this.hexBox.setValue(formatHex(this.color));
        this.redSlider.setIntValue(getRed(this.color));
        this.greenSlider.setIntValue(getGreen(this.color));
        this.blueSlider.setIntValue(getBlue(this.color));
        this.syncing = false;
        this.addButton.active = true;
    }

    private void syncSliders() {
        this.syncing = true;
        this.redSlider.setIntValue(getRed(this.color));
        this.greenSlider.setIntValue(getGreen(this.color));
        this.blueSlider.setIntValue(getBlue(this.color));
        this.syncing = false;
    }

    private void addColor() {
        int parsed = parseHex(this.hexBox.getValue());
        if (parsed < 0) {
            this.status = Component.translatable(key("text_color.invalid"));
            return;
        }
        ClientConfig.addCustomColor(parsed);
        returnToParent();
    }

    private void returnToParent() {
        MinecraftCompat.setScreen(this.minecraft, this.parent);
    }

    private static int parseHex(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (normalized.length() != 6 || !normalized.matches("[0-9a-fA-F]{6}")) {
            return -1;
        }
        try {
            return Integer.parseInt(normalized, 16);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private static String formatHex(int color) {
        return "#" + String.format("%06X", color & 0xFFFFFF);
    }

    private static int getRed(int color) {
        return color >> 16 & 0xFF;
    }

    private static int getGreen(int color) {
        return color >> 8 & 0xFF;
    }

    private static int getBlue(int color) {
        return color & 0xFF;
    }

    private static String key(String suffix) {
        return "screen." + ModSource.MODID + "." + suffix;
    }
}

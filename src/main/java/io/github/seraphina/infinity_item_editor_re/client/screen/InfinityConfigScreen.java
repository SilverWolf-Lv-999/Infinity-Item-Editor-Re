package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.Config;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class InfinityConfigScreen extends Screen {
    private static final int LIST_TOP = 32;
    private static final int LIST_BOTTOM_PADDING = 40;
    private static final int ROW_HEIGHT = 48;
    private static final int BUTTON_WIDTH = 74;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 6;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int MUTED_TEXT_COLOR = 0xFFB0B0B0;
    private static final int SAVED_TEXT_COLOR = 0xFF55FF55;

    private final Screen parent;
    private final Map<Config.BooleanEntry, Boolean> pendingValues = new LinkedHashMap<>();
    private Component status = Component.empty();
    private ConfigList configList;

    public InfinityConfigScreen(Screen parent) {
        super(Component.translatable(key("title")));
        this.parent = parent;
        for (Config.BooleanEntry entry : Config.booleanEntries()) {
            this.pendingValues.put(entry, entry.get());
        }
    }

    @Override
    protected void init() {
        int listBottom = this.height - LIST_BOTTOM_PADDING;
        this.configList = new ConfigList(this, this.minecraft, this.width, this.height, LIST_TOP, listBottom);
        this.addRenderableWidget(this.configList);

        int totalButtonWidth = BUTTON_WIDTH * 3 + BUTTON_GAP * 2;
        int x = (this.width - totalButtonWidth) / 2;
        int y = this.height - 27;
        this.addRenderableWidget(Button.builder(Component.translatable(key("save")), button -> saveChanges())
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable(key("reset")), button -> resetToDefaults())
                .bounds(x + BUTTON_WIDTH + BUTTON_GAP, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable(key("done")), button -> saveAndClose())
                .bounds(x + (BUTTON_WIDTH + BUTTON_GAP) * 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, TEXT_COLOR);
        if (!this.status.getString().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.status, this.width / 2, this.height - 38, SAVED_TEXT_COLOR);
        }
    }

    @Override
    public void onClose() {
        saveAndClose();
    }

    private boolean getPendingValue(Config.BooleanEntry entry) {
        return this.pendingValues.getOrDefault(entry, entry.get());
    }

    private void toggle(Config.BooleanEntry entry) {
        this.pendingValues.put(entry, !getPendingValue(entry));
        this.status = Component.empty();
    }

    private void resetToDefaults() {
        for (Config.BooleanEntry entry : Config.booleanEntries()) {
            this.pendingValues.put(entry, entry.defaultValue());
        }
        saveChanges();
    }

    private void saveChanges() {
        for (Map.Entry<Config.BooleanEntry, Boolean> entry : this.pendingValues.entrySet()) {
            entry.getKey().set(entry.getValue());
        }
        Config.syncPublicFields();
        Config.save();
        this.status = Component.translatable(key("saved"));
    }

    private void saveAndClose() {
        saveChanges();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private static String key(String suffix) {
        return "config." + ModSource.MODID + "." + suffix;
    }

    private static Component booleanText(boolean value) {
        return Component.translatable(key(value ? "on" : "off"));
    }

    @OnlyIn(Dist.CLIENT)
    private static final class ConfigList extends ObjectSelectionList<ConfigEntry> {
        private ConfigList(InfinityConfigScreen screen, Minecraft minecraft, int width, int height, int top, int bottom) {
            super(minecraft, width, height, top, bottom, ROW_HEIGHT);
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
            this.setRenderSelection(false);
            for (Config.BooleanEntry entry : Config.booleanEntries()) {
                this.addEntry(new ConfigEntry(screen, entry));
            }
        }

        @Override
        public int getRowWidth() {
            return Math.min(560, Math.max(260, this.width - 72));
        }

        @Override
        protected int getScrollbarPosition() {
            return Math.min(this.width - 8, this.getRowRight() + 6);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static final class ConfigEntry extends ObjectSelectionList.Entry<ConfigEntry> {
        private static final int TOGGLE_WIDTH = 58;
        private static final int TOGGLE_HEIGHT = 20;
        private static final int TOGGLE_TEXT_COLOR = 0xFFFFFFFF;
        private static final int ENABLED_FILL = 0xFF246B3A;
        private static final int DISABLED_FILL = 0xFF6B2836;
        private static final int BORDER_COLOR = 0xFF000000;
        private static final int HOVER_FILL = 0x33000000;

        private final InfinityConfigScreen screen;
        private final Config.BooleanEntry entry;

        private ConfigEntry(InfinityConfigScreen screen, Config.BooleanEntry entry) {
            this.screen = screen;
            this.entry = entry;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {
            if (hovered) {
                guiGraphics.fill(left - 4, top - 1, left + width + 4, top + height - 3, HOVER_FILL);
            }

            boolean value = this.screen.getPendingValue(this.entry);
            int toggleX = left + width - TOGGLE_WIDTH - 2;
            int toggleY = top + (height - TOGGLE_HEIGHT) / 2 - 2;
            int textWidth = Math.max(1, toggleX - left - 12);

            guiGraphics.drawString(this.screen.font, Component.translatable(this.entry.titleKey()), left, top + 4, TEXT_COLOR);
            List<FormattedCharSequence> description = this.screen.font.split(
                    Component.translatable(this.entry.descriptionKey()),
                    textWidth
            );
            for (int line = 0; line < Math.min(2, description.size()); line++) {
                guiGraphics.drawString(this.screen.font, description.get(line), left, top + 17 + line * 10, MUTED_TEXT_COLOR);
            }

            guiGraphics.fill(toggleX - 1, toggleY - 1, toggleX + TOGGLE_WIDTH + 1, toggleY + TOGGLE_HEIGHT + 1, BORDER_COLOR);
            guiGraphics.fill(toggleX, toggleY, toggleX + TOGGLE_WIDTH, toggleY + TOGGLE_HEIGHT,
                    value ? ENABLED_FILL : DISABLED_FILL);
            Component text = booleanText(value);
            int textX = toggleX + (TOGGLE_WIDTH - this.screen.font.width(text)) / 2;
            guiGraphics.drawString(this.screen.font, text, textX, toggleY + 6, TOGGLE_TEXT_COLOR);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) {
                return false;
            }
            this.screen.toggle(this.entry);
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.empty()
                    .append(Component.translatable(this.entry.titleKey()))
                    .append(" ")
                    .append(booleanText(this.screen.getPendingValue(this.entry)));
        }
    }
}

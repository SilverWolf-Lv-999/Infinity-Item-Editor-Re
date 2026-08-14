package io.github.seraphina.infinity_item_editor_re.client.screen.component;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.screen.CompatScreen;
import io.github.seraphina.infinity_item_editor_re.client.screen.FilteredEditBox;
import io.github.seraphina.infinity_item_editor_re.client.screen.InfinityEditorButton;
import io.github.seraphina.infinity_item_editor_re.client.screen.ItemEditorScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SpecialComponentSelectionScreen extends CompatScreen {
    private static final int BUTTON_HEIGHT = 20;
    private static final int GRID_COLUMNS = 3;
    private static final int GRID_ROWS = 5;
    private static final int PAGE_SIZE = GRID_COLUMNS * GRID_ROWS;

    private final ItemEditorScreen thisLastScreen;
    private ItemStack thisEditingStack;
    private String thisSearchText = "";
    private int thisPage;
    private FilteredEditBox thisSearchBox;

    public SpecialComponentSelectionScreen(ItemEditorScreen lastScreen, ItemStack stack) {
        super(Component.translatable(key("special_components.select")));
        this.thisLastScreen = lastScreen;
        this.thisEditingStack = stack.copy();
    }

    @Override
    protected void init() {
        int searchWidth = Math.min(300, Math.max(150, this.width - 154));
        int searchX = (this.width - searchWidth) / 2 - 32;
        this.thisSearchBox = addRenderableWidget(new FilteredEditBox(this.font, searchX, 34, searchWidth, BUTTON_HEIGHT,
                Component.translatable(key("special_components.search"))));
        this.thisSearchBox.setMaxLength(128);
        this.thisSearchBox.setValue(this.thisSearchText);
        this.thisSearchBox.setResponder(value -> this.thisSearchText = value == null ? "" : value);

        addRenderableWidget(new InfinityEditorButton(searchX + searchWidth + 4, 34, 60, BUTTON_HEIGHT,
                Component.translatable(key("special_components.search_action")), button -> {
                    this.thisPage = 0;
                    rebuildWidgets();
                }));

        List<ComponentEditorDefinition> entries = filteredDefinitions();
        clampPage(entries.size());
        int gridMargin = 18;
        int gap = 5;
        int gridWidth = Math.max(180, this.width - gridMargin * 2);
        int cellWidth = Math.max(56, (gridWidth - gap * (GRID_COLUMNS - 1)) / GRID_COLUMNS);
        int gridX = (this.width - (cellWidth * GRID_COLUMNS + gap * (GRID_COLUMNS - 1))) / 2;
        int gridY = 66;

        int first = this.thisPage * PAGE_SIZE;
        for (int index = 0; index < PAGE_SIZE && first + index < entries.size(); index++) {
            ComponentEditorDefinition definition = entries.get(first + index);
            int column = index % GRID_COLUMNS;
            int row = index / GRID_COLUMNS;
            int x = gridX + column * (cellWidth + gap);
            int y = gridY + row * (BUTTON_HEIGHT + gap);
            addRenderableWidget(new InfinityEditorButton(x, y, cellWidth, BUTTON_HEIGHT, definition.displayName(),
                    button -> openEditor(definition)));
        }

        int bottomY = this.height - 28;
        int totalWidth = 72 * 3 + 8;
        int startX = (this.width - totalWidth) / 2;
        addRenderableWidget(new InfinityEditorButton(startX, bottomY, 72, BUTTON_HEIGHT,
                Component.translatable(key("back")), button -> returnToLastScreen()));
        addRenderableWidget(new InfinityEditorButton(startX + 76, bottomY, 72, BUTTON_HEIGHT,
                Component.translatable(key("special_components.previous")), button -> {
                    if (this.thisPage > 0) {
                        this.thisPage--;
                        rebuildWidgets();
                    }
                }));
        addRenderableWidget(new InfinityEditorButton(startX + 152, bottomY, 72, BUTTON_HEIGHT,
                Component.translatable(key("special_components.next")), button -> {
                    if ((this.thisPage + 1) * PAGE_SIZE < entries.size()) {
                        this.thisPage++;
                        rebuildWidgets();
                    }
                }));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            returnToLastScreen();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xF0101113);
        guiGraphics.fill(0, 0, this.width, 1, InfinityEditorButton.MAIN_COLOR);
        guiGraphics.centeredText(this.font, this.title, this.width / 2, 12, InfinityEditorButton.MAIN_COLOR);
        guiGraphics.centeredText(this.font, Component.translatable(key("special_components.hint")),
                this.width / 2, 56, 0xFFBFC9C4);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        List<ComponentEditorDefinition> entries = filteredDefinitions();
        if (entries.isEmpty()) {
            guiGraphics.centeredText(this.font, Component.translatable(key("special_components.no_results")),
                    this.width / 2, 112, 0xFFF44262);
        } else {
            int pages = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
            guiGraphics.centeredText(this.font, Component.translatable(key("special_components.page"), this.thisPage + 1, pages),
                    this.width / 2, this.height - 42, 0xFFFFD966);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void onClose() {
        returnToLastScreen();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    void replaceEditingStack(ItemStack stack) {
        this.thisEditingStack = stack.copy();
    }

    private void openEditor(ComponentEditorDefinition definition) {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new SpecialComponentEditorScreen(this.thisLastScreen, this, this.thisEditingStack, definition));
        }
    }

    private void returnToLastScreen() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.thisLastScreen);
        }
    }

    private List<ComponentEditorDefinition> filteredDefinitions() {
        String filter = this.thisSearchText.trim().toLowerCase(Locale.ROOT);
        if (filter.isEmpty()) {
            return VanillaComponentEditorRegistry.definitions();
        }

        List<ComponentEditorDefinition> matches = new ArrayList<>();
        for (ComponentEditorDefinition definition : VanillaComponentEditorRegistry.definitions()) {
            String translated = definition.displayName().getString().toLowerCase(Locale.ROOT);
            if (definition.id().contains(filter) || definition.title().toLowerCase(Locale.ROOT).contains(filter)
                    || translated.contains(filter) || definition.category().contains(filter)) {
                matches.add(definition);
            }
        }
        return matches;
    }

    private void clampPage(int totalEntries) {
        int maxPage = Math.max(0, (totalEntries - 1) / PAGE_SIZE);
        this.thisPage = Math.max(0, Math.min(this.thisPage, maxPage));
    }

    private static String key(String suffix) {
        return "screen." + ModSource.MODID + "." + suffix;
    }
}

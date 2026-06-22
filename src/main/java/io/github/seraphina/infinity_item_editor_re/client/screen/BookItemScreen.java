package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.util.ComponentCompat;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.screen.legacy.LegacyTextEditBox;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
final class BookItemScreen extends Screen {
    private static final int TEXT_WIDTH = 114;
    private static final int TEXT_HEIGHT = 128;
    private static final int IMAGE_WIDTH = 192;
    private static final int PAGE_LEFT_OFFSET = 36;
    private static final int PAGE_TOP_OFFSET = 32;
    private static final int LINE_HEIGHT = 9;
    private static final int PAGE_LINES = TEXT_HEIGHT / LINE_HEIGHT;
    private static final int MAX_PAGE_LENGTH = WritableBookContent.PAGE_EDIT_LENGTH;
    private static final int MAX_PAGES = WritableBookContent.MAX_PAGES;
    private static final int FORMAT_BUTTON_WIDTH = 13;
    private static final int FORMAT_BUTTON_HEIGHT = 15;
    private static final int FORMAT_BUTTON_Y_OFFSET = 30;
    private static final int FORMAT_BUTTON_ROWS = 2;

    private final ItemEditorScreen lastScreen;
    private final ItemStack bookStack;
    private final List<String> pages = new ArrayList<>();
    private final List<EditBox> lineBoxes = new ArrayList<>();
    private int currentPage;
    private PageButton forwardButton;
    private PageButton backButton;
    private InfinityEditorButton deletePageButton;
    private EditBox lastFocusedLineBox;
    private boolean syncingLineBoxes;
    private boolean modified;

    BookItemScreen(ItemEditorScreen lastScreen, ItemStack bookStack) {
        super(Component.translatable(key("book.edit_pages")));
        this.lastScreen = lastScreen;
        this.bookStack = bookStack;
        readPagesFromStack();
    }

    @Override
    protected void init() {
        this.lineBoxes.clear();
        int left = getBookLeft();
        int pageX = left + PAGE_LEFT_OFFSET;
        for (int line = 0; line < PAGE_LINES; line++) {
            EditBox lineBox = new LegacyTextEditBox(this.font, pageX, PAGE_TOP_OFFSET + line * LINE_HEIGHT,
                    TEXT_WIDTH, LINE_HEIGHT, Component.translatable(key("book.line"), line + 1));
            int lineIndex = line;
            lineBox.setBordered(false);
            lineBox.setTextColor(0);
            lineBox.setTextColorUneditable(0xFF555555);
            lineBox.setMaxLength(MAX_PAGE_LENGTH);
            lineBox.setValue(getPageLine(currentPage, lineIndex));
            lineBox.setResponder(value -> updatePageLine(lineIndex, value));
            this.lineBoxes.add(addRenderableWidget(lineBox));
        }

        addRenderableWidget(new InfinityEditorButton(this.width / 2 + 2, 196, 98, 20,
                CommonComponents.GUI_DONE, button -> returnToLastScreen()));
        this.deletePageButton = addRenderableWidget(new InfinityEditorButton(this.width / 2 - 100, 196, 98, 20,
                Component.translatable(key("book.delete_page")), button -> deleteCurrentPage()));
        this.forwardButton = addRenderableWidget(new PageButton(left + 116, 159, true, button -> pageForward(), true));
        this.backButton = addRenderableWidget(new PageButton(left + 43, 159, false, button -> pageBack(), true));
        addFormatButtons();
        updateButtonVisibility();
        focusLine(Mth.clamp(getFocusedLineIndex(), 0, this.lineBoxes.size() - 1));
    }

    @Override
    public void tick() {
        for (EditBox lineBox : this.lineBoxes) {
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            returnToLastScreen();
            return true;
        }
        if (isInventoryKey(keyCode, scanCode) && !isLineBoxFocused()) {
            returnToLastScreen();
            return true;
        }

        if (keyCode == 257 || keyCode == 335) {
            int focusedLine = getFocusedLineIndex();
            if (focusedLine >= 0) {
                if (focusedLine < PAGE_LINES - 1) {
                    focusLine(focusedLine + 1);
                } else {
                    pageForward();
                    focusLine(0);
                }
                return true;
            }
        }

        if (keyCode == 264) {
            int focusedLine = getFocusedLineIndex();
            if (focusedLine >= 0 && focusedLine < PAGE_LINES - 1) {
                focusLine(focusedLine + 1);
                return true;
            }
        }

        if (keyCode == 265) {
            int focusedLine = getFocusedLineIndex();
            if (focusedLine > 0) {
                focusLine(focusedLine - 1);
                return true;
            }
        }

        if (keyCode == 266) {
            pageBack();
            return true;
        }

        if (keyCode == 267) {
            pageForward();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        updateLastFocusedLineBox();
        return handled;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        EditorBackgrounds.render(guiGraphics, this.width, this.height);
        int left = getBookLeft();
        guiGraphics.blit(RenderType::guiTextured, BookViewScreen.BOOK_LOCATION, left, 2,
                0.0F, 0.0F, IMAGE_WIDTH, IMAGE_WIDTH, 256, 256);
        Component pageMsg = Component.translatable("book.pageIndicator", this.currentPage + 1, Math.max(this.pages.size(), 1));
        guiGraphics.drawString(this.font, pageMsg, left - this.font.width(pageMsg) + IMAGE_WIDTH - 44, 18, 0, false);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void onClose() {
        returnToLastScreen();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isInventoryKey(int keyCode, int scanCode) {
        return this.minecraft != null
                && this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode));
    }

    private boolean isLineBoxFocused() {
        for (EditBox lineBox : this.lineBoxes) {
            if (lineBox.isFocused()) {
                return true;
            }
        }
        return false;
    }

    private int getBookLeft() {
        return (this.width - IMAGE_WIDTH) / 2;
    }

    private void pageBack() {
        if (this.currentPage <= 0) {
            return;
        }

        captureCurrentPageLines();
        this.currentPage--;
        syncLineBoxesFromPage();
        updateButtonVisibility();
    }

    private void pageForward() {
        captureCurrentPageLines();
        if (this.currentPage < this.pages.size() - 1) {
            this.currentPage++;
        } else if (this.pages.size() < MAX_PAGES) {
            this.pages.add("");
            this.currentPage = this.pages.size() - 1;
        } else {
            return;
        }

        syncLineBoxesFromPage();
        updateButtonVisibility();
    }

    private void deleteCurrentPage() {
        captureCurrentPageLines();
        if (this.pages.size() <= 1) {
            this.pages.set(0, "");
        } else {
            this.pages.remove(this.currentPage);
            this.currentPage = Mth.clamp(this.currentPage, 0, this.pages.size() - 1);
        }

        this.modified = true;
        writePagesToStack();
        syncLineBoxesFromPage();
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (this.backButton != null) {
            this.backButton.visible = this.currentPage > 0;
        }
        if (this.forwardButton != null) {
            this.forwardButton.visible = this.currentPage < this.pages.size() - 1 || this.pages.size() < MAX_PAGES;
        }
        if (this.deletePageButton != null) {
            this.deletePageButton.active = this.pages.size() > 1 || !getCurrentPageText().isEmpty();
        }
    }

    private void syncLineBoxesFromPage() {
        this.syncingLineBoxes = true;
        for (int line = 0; line < this.lineBoxes.size(); line++) {
            this.lineBoxes.get(line).setValue(getPageLine(this.currentPage, line));
        }
        this.syncingLineBoxes = false;
    }

    private void captureCurrentPageLines() {
        if (this.lineBoxes.isEmpty()) {
            return;
        }

        List<String> lines = new ArrayList<>();
        for (EditBox lineBox : this.lineBoxes) {
            lines.add(lineBox.getValue());
        }
        String text = joinPageLines(lines);
        if (!text.equals(getCurrentPageText())) {
            setPageText(this.currentPage, text);
            this.modified = true;
        }
    }

    private void updatePageLine(int line, String value) {
        if (this.syncingLineBoxes) {
            return;
        }

        List<String> lines = splitPageLines(getCurrentPageText());
        while (lines.size() <= line) {
            lines.add("");
        }
        lines.set(line, value);
        setPageText(this.currentPage, joinPageLines(lines));
        this.modified = true;
        writePagesToStack();
        updateButtonVisibility();
    }

    private void setPageText(int page, String text) {
        ensurePageExists(page);
        this.pages.set(page, text);
    }

    private String getCurrentPageText() {
        return getPageText(this.currentPage);
    }

    private String getPageText(int page) {
        if (page < 0 || page >= this.pages.size()) {
            return "";
        }
        return this.pages.get(page);
    }

    private String getPageLine(int page, int line) {
        List<String> lines = splitPageLines(getPageText(page));
        return line >= 0 && line < lines.size() ? lines.get(line) : "";
    }

    private void ensurePageExists(int page) {
        while (this.pages.size() <= page) {
            this.pages.add("");
        }
    }

    private void focusLine(int line) {
        if (line < 0 || line >= this.lineBoxes.size()) {
            return;
        }
        EditBox lineBox = this.lineBoxes.get(line);
        for (EditBox box : this.lineBoxes) {
            box.setFocused(box == lineBox);
        }
        setFocused(lineBox);
        this.lastFocusedLineBox = lineBox;
    }

    private int getFocusedLineIndex() {
        for (int line = 0; line < this.lineBoxes.size(); line++) {
            if (this.lineBoxes.get(line).isFocused()) {
                return line;
            }
        }
        return this.lastFocusedLineBox == null ? -1 : this.lineBoxes.indexOf(this.lastFocusedLineBox);
    }

    private void updateLastFocusedLineBox() {
        GuiEventListener focused = getFocused();
        if (focused instanceof EditBox editBox && this.lineBoxes.contains(editBox)) {
            this.lastFocusedLineBox = editBox;
        }
    }

    private void addFormatButtons() {
        ChatFormatting[] formats = ChatFormatting.values();
        int colorAmount = 2 + formats.length;
        int columns = colorAmount / FORMAT_BUTTON_ROWS;
        int startX = this.width - 1 - FORMAT_BUTTON_WIDTH * ((colorAmount + FORMAT_BUTTON_ROWS) / FORMAT_BUTTON_ROWS);
        int startY = this.height - FORMAT_BUTTON_Y_OFFSET;

        addRenderableWidget(new InfinityEditorButton(startX + FORMAT_BUTTON_WIDTH, startY,
                FORMAT_BUTTON_WIDTH, FORMAT_BUTTON_HEIGHT,
                Component.literal(String.valueOf(ChatFormatting.PREFIX_CODE)), button -> insertFocusedText(String.valueOf(ChatFormatting.PREFIX_CODE))));
        addRenderableWidget(new InfinityEditorButton(startX + FORMAT_BUTTON_WIDTH * 2, startY,
                FORMAT_BUTTON_WIDTH, FORMAT_BUTTON_HEIGHT,
                Component.literal(ChatFormatting.DARK_RED + "%"), button -> stripFocusedFormatting()));

        for (int i = 2; i < colorAmount; i++) {
            ChatFormatting format = formats[i - 2];
            int x = startX + FORMAT_BUTTON_WIDTH * ((i % columns) + 1);
            int y = startY + FORMAT_BUTTON_HEIGHT * (i / columns);
            addRenderableWidget(new InfinityEditorButton(x, y, FORMAT_BUTTON_WIDTH, FORMAT_BUTTON_HEIGHT,
                    Component.literal(format.toString() + format.getChar()), button -> insertFocusedText(format.toString())));
        }
    }

    private void insertFocusedText(String text) {
        EditBox focused = getTargetLineBox();
        if (focused == null) {
            return;
        }
        focused.insertText(text);
        this.lastFocusedLineBox = focused;
        focusLine(this.lineBoxes.indexOf(focused));
    }

    private void stripFocusedFormatting() {
        EditBox focused = getTargetLineBox();
        if (focused == null) {
            return;
        }
        focused.setValue(Objects.requireNonNullElse(ChatFormatting.stripFormatting(focused.getValue()), ""));
        this.lastFocusedLineBox = focused;
        focusLine(this.lineBoxes.indexOf(focused));
    }

    @Nullable
    private EditBox getTargetLineBox() {
        updateLastFocusedLineBox();
        if (this.lastFocusedLineBox != null && this.lineBoxes.contains(this.lastFocusedLineBox)) {
            return this.lastFocusedLineBox;
        }
        return this.lineBoxes.isEmpty() ? null : this.lineBoxes.get(0);
    }

    private void readPagesFromStack() {
        this.pages.clear();
        CompoundTag tag = ItemStackNbt.get(this.bookStack);
        if (tag != null && tag.contains(ItemEditorScreenState.BOOK_PAGES_TAG, Tag.TAG_LIST)) {
            ListTag pageTags = tag.getList(ItemEditorScreenState.BOOK_PAGES_TAG, Tag.TAG_STRING);
            for (int i = 0; i < pageTags.size(); i++) {
                this.pages.add(readPageForEditing(pageTags.getString(i)));
            }
        }

        if (this.pages.isEmpty()) {
            this.pages.add("");
        }
        this.currentPage = Mth.clamp(this.currentPage, 0, this.pages.size() - 1);
    }

    private String readPageForEditing(String raw) {
        if (!this.bookStack.is(Items.WRITTEN_BOOK)) {
            return raw;
        }

        Component parsed = this.lastScreen.readBookPageComponent(raw);
        return parsed.getString();
    }

    private void writePagesToStack() {
        List<String> savedPages = new ArrayList<>(this.pages);
        removeEmptyTrailingPages(savedPages);

        CompoundTag tag = ItemStackNbt.getOrCreate(this.bookStack);
        if (savedPages.isEmpty()) {
            tag.remove(ItemEditorScreenState.BOOK_PAGES_TAG);
        } else {
            ListTag pageTags = new ListTag();
            for (String page : savedPages) {
                pageTags.add(StringTag.valueOf(writePageForStack(page)));
            }
            tag.put(ItemEditorScreenState.BOOK_PAGES_TAG, pageTags);
        }
        tag.remove(ItemEditorScreenState.BOOK_FILTERED_PAGES_TAG);
        if (tag.isEmpty()) {
            ItemStackNbt.set(this.bookStack, null);
        }
    }

    private String writePageForStack(String page) {
        if (this.bookStack.is(Items.WRITTEN_BOOK)) {
            return ComponentCompat.toJson(this.lastScreen.readBookPageComponent(page));
        }
        return page;
    }

    private void returnToLastScreen() {
        captureCurrentPageLines();
        if (this.modified) {
            writePagesToStack();
        }
        this.lastScreen.refreshAfterBookEdit();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen);
        }
    }

    private static void removeEmptyTrailingPages(List<String> pages) {
        for (int i = pages.size() - 1; i >= 0 && pages.get(i).isEmpty(); i--) {
            pages.remove(i);
        }
    }

    private static List<String> splitPageLines(String page) {
        String[] split = page.split("\n", -1);
        List<String> lines = new ArrayList<>(Math.max(split.length, PAGE_LINES));
        for (String line : split) {
            lines.add(line);
        }
        return lines;
    }

    private static String joinPageLines(List<String> lines) {
        int end = Math.min(lines.size(), PAGE_LINES);
        while (end > 0 && lines.get(end - 1).isEmpty()) {
            end--;
        }
        return String.join("\n", lines.subList(0, end));
    }

    private static String key(String suffix) {
        return "screen." + ModSource.MODID + "." + suffix;
    }
}

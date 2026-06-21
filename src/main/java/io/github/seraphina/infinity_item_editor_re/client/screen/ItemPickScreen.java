package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.data.realms.RealmController;
import io.github.seraphina.infinity_item_editor_re.data.voids.VoidController;
import io.github.seraphina.infinity_item_editor_re.util.GiveHelper;
import net.minecraft.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
final class ItemPickScreen extends Screen {
    private static final int MAX_IN_ROW = 8;
    private static final int ROWS = 10;
    private static final int ITEM_SIZE = 16;
    private static final int TOP_BAR = 20;
    private static final int BLAND_COLOR = 0xFF96C8FF;
    private static final int HOVER_COLOR = 0x96969696;
    private static final PickList REALM_LIST = new PickList() {
        @Override
        List<ItemStack> getStackList() {
            Minecraft minecraft = Minecraft.getInstance();
            RealmController controller = ModSource.getOrCreateRealmController(minecraft.gameDirectory);
            NonNullList<ItemStack> stacks = NonNullList.create();
            if (controller != null) {
                for (ItemStack stack : controller.getStackList()) {
                    stacks.add(stack.copy());
                }
            }
            return stacks;
        }

        @Override
        Component getName() {
            return Component.translatable(itemGroupKey("realm"));
        }
    };
    private static final PickList VOID_LIST = new PickList() {
        @Override
        List<ItemStack> getStackList() {
            NonNullList<ItemStack> stacks = NonNullList.create();
            VoidController.loadVoidToList(stacks);
            return stacks;
        }

        @Override
        Component getName() {
            return Component.translatable(itemGroupKey("void"));
        }
    };
    private static final PickList INVENTORY_LIST = new PickList() {
        @Override
        List<ItemStack> getStackList() {
            Minecraft minecraft = Minecraft.getInstance();
            NonNullList<ItemStack> stacks = NonNullList.create();
            if (minecraft.player == null) {
                return stacks;
            }

            Inventory inventory = minecraft.player.getInventory();
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (!stack.isEmpty()) {
                    stacks.add(stack.copy());
                }
            }
            return stacks;
        }

        @Override
        Component getName() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return Component.translatable("container.inventory");
            }
            return minecraft.player.getInventory().getDisplayName();
        }
    };

    private final Screen lastScreen;
    private final Consumer<ItemStack> stackSetter;
    private final Supplier<ItemStack> stackGetter;
    private final List<ItemStack> filteredList = new ArrayList<>();
    private PickList pickList;
    private int currentElement;
    private String filteredString;
    private String searchString = "";
    private int midX;
    private int midY;

    ItemPickScreen(Screen lastScreen, Consumer<ItemStack> stackSetter, Supplier<ItemStack> stackGetter) {
        this(lastScreen, stackSetter, stackGetter, REALM_LIST);
    }

    private ItemPickScreen(Screen lastScreen, Consumer<ItemStack> stackSetter, Supplier<ItemStack> stackGetter, PickList pickList) {
        super(Component.translatable(key("pick")));
        this.lastScreen = lastScreen;
        this.stackSetter = stackSetter;
        this.stackGetter = stackGetter;
        this.pickList = pickList;
    }

    @Override
    protected void init() {
        this.midX = this.width / 2;
        this.midY = this.height / 2;

        addRenderableWidget(new InfinityEditorButton(this.midX - 90, this.height - 35, 60, 20,
                Component.translatable(key("back")), button -> returnToLastScreen()));
        InfinityEditorButton reset = addRenderableWidget(new InfinityEditorButton(this.midX - 30, this.height - 35, 60, 20,
                Component.translatable(key("reset")), button -> {
        }));
        reset.active = false;
        addRenderableWidget(new InfinityEditorButton(this.midX + 30, this.height - 35, 60, 20,
                Component.translatable(key("drop")), button -> dropSelectedStack()));

        applySearchIfNeeded();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            returnToLastScreen();
            return true;
        }
        if (keyCode == 259) {
            this.searchString = this.searchString.substring(0, Math.max(this.searchString.length() - 1, 0));
            if (this.searchString.isEmpty() && !this.searchString.equals(this.filteredString)) {
                applySearch();
            }
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            if (!this.searchString.equals(this.filteredString)) {
                applySearch();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (StringUtil.isAllowedChatCharacter(codePoint) && this.searchString.length() < 20) {
            this.searchString += codePoint;
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0) {
            return false;
        }

        int space = getClickSpace();
        int nextPageWidth = this.font.width("-->");
        int currentPage = getCurrentPage();
        int amountPages = getAmountPages();
        int searchWidth = this.font.width(this.searchString);
        if (!this.searchString.isEmpty() && !this.searchString.equals(this.filteredString)
                && isMouseIn(mouseX, mouseY, this.midX - searchWidth / 2, 56, searchWidth, 8)) {
            applySearch();
            return true;
        }

        if (currentPage + 1 < amountPages
                && isMouseIn(mouseX, mouseY, this.midX + 10, 70 + TOP_BAR + 168, nextPageWidth, 8)) {
            this.currentElement = Math.min(this.filteredList.size() - 1, (currentPage + 1) * getAmountInPage());
            return true;
        }
        if (currentPage > 0
                && isMouseIn(mouseX, mouseY, this.midX - 10 - nextPageWidth, 70 + TOP_BAR + 168, nextPageWidth, 8)) {
            this.currentElement = Math.max(0, (currentPage - 1) * getAmountInPage());
            return true;
        }
        if (mouseX < space) {
            if (trySwitchPickList(mouseX, mouseY, space, REALM_LIST, 100)) {
                return true;
            }
            if (trySwitchPickList(mouseX, mouseY, space, VOID_LIST, 120)) {
                return true;
            }
            return trySwitchPickList(mouseX, mouseY, space, INVENTORY_LIST, 140);
        }

        if (!this.filteredList.isEmpty()) {
            int start = Math.min(this.filteredList.size() - 1, currentPage * getAmountInPage());
            int end = Math.min(this.filteredList.size(), (currentPage + 1) * getAmountInPage());
            for (int i = start; i < end; i++) {
                int x = space + ITEM_SIZE * (i % MAX_IN_ROW);
                int y = 70 + TOP_BAR + ITEM_SIZE * ((i % getAmountInPage()) / MAX_IN_ROW);
                if (mouseX > x && mouseX < x + ITEM_SIZE && mouseY > y && mouseY < y + ITEM_SIZE) {
                    this.stackSetter.accept(this.filteredList.get(i).copy());
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        EditorBackgrounds.render(guiGraphics, this.width, this.height);
        renderSelectedStack(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.midX, 15, InfinityEditorButton.MAIN_COLOR);
        renderPickContents(guiGraphics, mouseX, mouseY);
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

    private void renderPickContents(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int space = getRenderSpace();
        int currentPage = getCurrentPage();
        int amountPages = getAmountPages();

        guiGraphics.drawCenteredString(this.font, this.pickList.getName(), this.midX, 60, InfinityEditorButton.CONTRAST_COLOR);
        Component searchText = this.searchString.isEmpty()
                ? Component.translatable(key("pick.type_search"))
                : Component.literal(this.searchString);
        guiGraphics.drawCenteredString(this.font, searchText, this.midX, 73, BLAND_COLOR);

        renderPickListName(guiGraphics, REALM_LIST, space, 100);
        renderPickListName(guiGraphics, VOID_LIST, space, 120);
        renderPickListName(guiGraphics, INVENTORY_LIST, space, 140);

        String nextPage = "-->";
        int nextPageWidth = this.font.width(nextPage);
        if (currentPage + 1 < amountPages) {
            boolean selected = isMouseIn(mouseX, mouseY, this.midX + 10, 70 + TOP_BAR + 168, nextPageWidth, 8);
            guiGraphics.drawString(this.font, nextPage, this.midX + 10, 70 + TOP_BAR + 168,
                    selected ? InfinityEditorButton.CONTRAST_COLOR : BLAND_COLOR);
        }

        guiGraphics.drawCenteredString(this.font, Integer.toString(currentPage + 1), this.midX, 70 + TOP_BAR + 168, BLAND_COLOR);

        if (currentPage > 0) {
            String previousPage = "<--";
            boolean selected = isMouseIn(mouseX, mouseY, this.midX - nextPageWidth - 10, 70 + TOP_BAR + 168, nextPageWidth, 8);
            guiGraphics.drawString(this.font, previousPage, this.midX - nextPageWidth - 10, 70 + TOP_BAR + 168,
                    selected ? InfinityEditorButton.CONTRAST_COLOR : BLAND_COLOR);
        }

        ItemStack hovered = renderPickItems(guiGraphics, mouseX, mouseY, space, currentPage);
        int searchWidth = this.font.width(this.searchString);
        if (!hovered.isEmpty()) {
            guiGraphics.renderTooltip(this.font, hovered, mouseX, mouseY);
        } else if (!this.searchString.equals(this.filteredString)
                && isMouseIn(mouseX, mouseY, this.midX - searchWidth / 2, 56, searchWidth, 8)) {
            guiGraphics.renderTooltip(this.font, Component.translatable(key("pick.click_search")), mouseX, mouseY);
        } else if (isMouseIn(mouseX, mouseY, this.midX - 8, 27, ITEM_SIZE, ITEM_SIZE)) {
            ItemStack stack = getSelectedStack();
            if (!stack.isEmpty()) {
                guiGraphics.renderTooltip(this.font, stack, mouseX, mouseY);
            }
        }
    }

    private ItemStack renderPickItems(GuiGraphics guiGraphics, int mouseX, int mouseY, int space, int currentPage) {
        if (this.filteredList.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack hovered = ItemStack.EMPTY;
        int start = Math.min(this.filteredList.size() - 1, currentPage * getAmountInPage());
        int end = Math.min(this.filteredList.size(), (currentPage + 1) * getAmountInPage());
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        for (int i = start; i < end; i++) {
            int x = space + ITEM_SIZE * (i % MAX_IN_ROW);
            int y = 70 + TOP_BAR + ITEM_SIZE * ((i % getAmountInPage()) / MAX_IN_ROW);
            ItemStack stack = this.filteredList.get(i);
            guiGraphics.renderItem(stack, x, y);
            guiGraphics.renderItemDecorations(this.font, stack, x, y);
            if (mouseX > x && mouseX < x + ITEM_SIZE && mouseY > y && mouseY < y + ITEM_SIZE) {
                guiGraphics.fill(x, y, x + ITEM_SIZE, y + ITEM_SIZE, HOVER_COLOR);
                hovered = stack;
            }
        }
        guiGraphics.pose().popPose();
        return hovered;
    }

    private void renderSelectedStack(GuiGraphics guiGraphics) {
        ItemStack stack = getSelectedStack();
        if (stack.isEmpty()) {
            return;
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        guiGraphics.renderItem(stack, this.midX - 8, 27);
        guiGraphics.renderItemDecorations(this.font, stack, this.midX - 8, 27);
        guiGraphics.pose().popPose();
    }

    private void renderPickListName(GuiGraphics guiGraphics, PickList list, int space, int y) {
        Component name = list.getName();
        int x = space - this.font.width(name) - 10;
        int color = this.pickList == list ? InfinityEditorButton.CONTRAST_COLOR : InfinityEditorButton.MAIN_COLOR;
        guiGraphics.drawString(this.font, name, x, y, color);
    }

    private boolean trySwitchPickList(double mouseX, double mouseY, int space, PickList list, int y) {
        Component name = list.getName();
        if (this.pickList != list && isMouseIn(mouseX, mouseY, space - this.font.width(name) - 10, y, this.font.width(name), 8)) {
            this.pickList = list;
            this.filteredString = null;
            applySearch();
            return true;
        }
        return false;
    }

    private void applySearchIfNeeded() {
        if (!this.searchString.equals(this.filteredString)) {
            applySearch();
        }
    }

    private void applySearch() {
        this.filteredList.clear();
        String loweredSearch = this.searchString.toLowerCase(Locale.ROOT);
        for (ItemStack stack : this.pickList.getStackList()) {
            if (stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(loweredSearch)) {
                this.filteredList.add(stack);
            }
        }
        this.currentElement = 0;
        this.filteredString = this.searchString;
    }

    private void returnToLastScreen() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen);
        }
    }

    private void dropSelectedStack() {
        ItemStack stack = getSelectedStack();
        if (stack.isEmpty() || this.minecraft == null) {
            return;
        }

        if (Screen.hasShiftDown()) {
            this.minecraft.keyboardHandler.setClipboard(GiveHelper.getStringFromItemStack(stack));
            return;
        }

        if (this.minecraft.gameMode != null && this.minecraft.player != null && this.minecraft.player.getAbilities().instabuild) {
            this.minecraft.gameMode.handleCreativeModeItemDrop(stack.copy());
        }
    }

    private ItemStack getSelectedStack() {
        ItemStack stack = this.stackGetter.get();
        return stack == null ? ItemStack.EMPTY : stack;
    }

    private int getAmountInPage() {
        return MAX_IN_ROW * ROWS;
    }

    private int getCurrentPage() {
        return this.currentElement / getAmountInPage();
    }

    private int getAmountPages() {
        return this.filteredList.size() / getAmountInPage() + 1;
    }

    private int getClickSpace() {
        return (this.width - MAX_IN_ROW * ITEM_SIZE) / 2;
    }

    private int getRenderSpace() {
        return (this.width - (MAX_IN_ROW * ITEM_SIZE + 3)) / 2;
    }

    private static boolean isMouseIn(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static String key(String suffix) {
        return "screen." + ModSource.MODID + "." + suffix;
    }

    private static String itemGroupKey(String suffix) {
        return "itemGroup." + ModSource.MODID + "." + suffix;
    }

    private abstract static class PickList {
        abstract List<ItemStack> getStackList();

        abstract Component getName();
    }
}

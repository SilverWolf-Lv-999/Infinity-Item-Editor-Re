package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Axis;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.data.realms.RealmController;
import io.github.seraphina.infinity_item_editor_re.util.GiveHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntConsumer;

@OnlyIn(Dist.CLIENT)
public class ItemEditorScreen extends Screen {
    private static final int MAX_COUNT = 64;
    private static final int MAX_ENCHANTMENT_LEVEL = 32767;
    private static final int MAX_POTION_LEVEL = 127;
    private static final int MAX_POTION_SECONDS = 99999;
    private static final int MAX_ATTRIBUTE_INTEGER = 99999999;
    private static final int FIELD_HEIGHT = 20;
    private static final int OLD_BUTTON_WIDTH = 60;
    private static final int OLD_BUTTON_HEIGHT = 20;
    private static final int ITEM_SIZE = 16;
    private static final int RING_ICON_HIT_RADIUS = 10;
    private static final int RING_HOVER_WIDTH = 16;
    private static final int CENTER_HIT_RADIUS = 15;
    private static final int MAIN_COLOR = InfinityEditorButton.MAIN_COLOR;
    private static final int ALT_COLOR = InfinityEditorButton.ALT_COLOR;
    private static final int CONTRAST_COLOR = InfinityEditorButton.CONTRAST_COLOR;
    private static final int BAD_RED = 0xFFF44262;
    private static final int GOOD_GREEN = 0xFF32CC64;
    private static final int CYAN = 0xFF00FFFF;
    private static final String ITEM_ENCHANTMENTS_TAG = "Enchantments";
    private static final String BOOK_ENCHANTMENTS_TAG = "StoredEnchantments";
    private static final String DISPLAY_TAG = "display";
    private static final String LORE_TAG = "Lore";
    private static final String HIDE_FLAGS_TAG = "HideFlags";
    private static final String CUSTOM_POTION_EFFECTS_TAG = "CustomPotionEffects";
    private static final String ATTRIBUTE_MODIFIERS_TAG = "AttributeModifiers";
    private static final String CUSTOM_POTION_COLOR_TAG = PotionUtils.TAG_CUSTOM_POTION_COLOR;
    private static final String MAP_COLOR_TAG = "MapColor";

    private final ItemStack originalStack;
    private ItemStack previewStack;
    private Panel activePanel = Panel.ITEM;
    private Component status = Component.empty();

    private String itemIdValue;
    private String countValue;
    private String damageValue;
    private String nameValue;
    private String rawNbtValue;
    private String enchantFilterValue = "";
    private String enchantLevelValue = "1";
    private String potionFilterValue = "";
    private String potionLevelValue = "1";
    private String potionTimeValue = "1";
    private String attributeAmountValue = "0";
    private String attributeDecimalValue = "0";
    private String colorHexValue;
    private String nbtFeedback = "";
    private boolean nbtFeedbackGood;
    private boolean showAllEnchantments;
    private boolean showPotionParticles = true;
    private boolean attributeInfinity;
    private boolean attributeNegative;
    private boolean syncingColorControls;
    private boolean lorePainterDragging;
    private boolean lorePainterPreview;
    private int rotOff;
    private int mouseDist;
    private int midX;
    private int midY;
    private int advancedScroll;
    private int loreScroll;
    private int attributeSlot = 1;
    private int attributeOperation;
    private int lorePainterWidth = 3;
    private int lorePainterHeight = 3;
    private boolean draggingLoreScroll;

    private final List<String> loreValues = new ArrayList<>();
    private final List<List<LorePixel>> lorePainterRows = new ArrayList<>();
    private final List<EditBox> tickingBoxes = new ArrayList<>();
    private final List<EditBox> mainTextBoxes = new ArrayList<>();
    private final List<EditBox> loreBoxes = new ArrayList<>();
    private final List<InfinityEditorButton> loreActionButtons = new ArrayList<>();
    private final Set<String> expandedNbtPaths = new HashSet<>();
    private final ItemStack enchantBook = new ItemStack(Items.ENCHANTED_BOOK);
    private final ItemStack potionIcon = new ItemStack(Items.POTION);
    private final ItemStack attributeIcon = new ItemStack(Items.PAPER);
    private final LorePixel currentLorePixel = new LorePixel();

    private EditBox itemIdBox;
    private EditBox countBox;
    private EditBox damageBox;
    private EditBox nameBox;
    private EditBox rawNbtBox;
    private EditBox enchantFilterBox;
    private EditBox enchantLevelBox;
    private EditBox potionFilterBox;
    private EditBox potionLevelBox;
    private EditBox potionTimeBox;
    private EditBox attributeAmountBox;
    private EditBox attributeDecimalBox;
    private EditBox colorHexBox;
    private InfinityEditorButton attributeInfinityButton;
    private InfinityEditorButton attributeOperationButton;
    private InfinityEditorButton attributeSlotButton;
    private InfinityEditorButton lorePainterScaleButton;
    private InfinityEditorButton lorePainterAddRowButton;
    private InfinityEditorButton lorePainterRemoveRowButton;
    private InfinityEditorButton lorePainterAddColumnButton;
    private InfinityEditorButton lorePainterRemoveColumnButton;
    private InfinityEditorButton lorePainterPreviewButton;
    private InfinityEditorButton copyLoreButton;
    private ColorSlider redSlider;
    private ColorSlider greenSlider;
    private ColorSlider blueSlider;

    public ItemEditorScreen(ItemStack stack) {
        super(Component.translatable(key("item")));
        this.originalStack = stack.copy();
        this.previewStack = stack.copy();
        this.expandedNbtPaths.add("tag");
        this.attributeSlot = getDefaultAttributeSlot(this.previewStack);
        readMainFieldsFromStack(this.previewStack);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.colorHexValue = formatColorHex(getEditorColor());
        ensureLorePainterRows();
    }

    @Override
    protected void init() {
        captureFieldValues();
        this.midX = this.width / 2;
        this.midY = this.height / 2;

        this.tickingBoxes.clear();
        this.mainTextBoxes.clear();
        this.loreBoxes.clear();
        this.loreActionButtons.clear();
        this.itemIdBox = null;
        this.countBox = null;
        this.damageBox = null;
        this.nameBox = null;
        this.rawNbtBox = null;
        this.enchantFilterBox = null;
        this.enchantLevelBox = null;
        this.potionFilterBox = null;
        this.potionLevelBox = null;
        this.potionTimeBox = null;
        this.attributeAmountBox = null;
        this.attributeDecimalBox = null;
        this.colorHexBox = null;
        this.attributeInfinityButton = null;
        this.attributeOperationButton = null;
        this.attributeSlotButton = null;
        this.lorePainterScaleButton = null;
        this.lorePainterAddRowButton = null;
        this.lorePainterRemoveRowButton = null;
        this.lorePainterAddColumnButton = null;
        this.lorePainterRemoveColumnButton = null;
        this.lorePainterPreviewButton = null;
        this.redSlider = null;
        this.greenSlider = null;
        this.blueSlider = null;
        this.copyLoreButton = null;

        switch (this.activePanel) {
            case ITEM -> addItemPanel();
            case NBT -> addNbtPanel();
            case NBT_ADVANCED -> addNbtAdvancedPanel();
            case HIDE_FLAGS -> addHideFlagsPanel();
            case ENCHANTMENTS -> addEnchantmentsPanel();
            case POTION -> addPotionPanel();
            case ATTRIBUTES -> addAttributesPanel();
            case COLOR -> addColorPanel();
            case LORE -> addLorePanel();
            case LORE_PAINTER -> addLorePainterPanel();
        }

        addBottomButtons();
    }

    @Override
    public void tick() {
        for (EditBox box : this.tickingBoxes) {
            box.tick();
        }

        if ((this.activePanel == Panel.ENCHANTMENTS || this.activePanel == Panel.POTION || this.activePanel == Panel.ATTRIBUTES)
                && Math.abs(this.mouseDist - getRingRadius()) >= RING_HOVER_WIDTH) {
            this.rotOff++;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.activePanel == Panel.NBT_ADVANCED) {
            renderNbtAdvancedPanel(guiGraphics, mouseX, mouseY);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }

        renderBackground(guiGraphics);

        switch (this.activePanel) {
            case ITEM -> renderItemPanel(guiGraphics, mouseX, mouseY);
            case NBT -> renderNbtPanel(guiGraphics, mouseX, mouseY);
            case HIDE_FLAGS -> renderHideFlagsPanel(guiGraphics);
            case ENCHANTMENTS -> renderEnchantmentsPanel(guiGraphics, mouseX, mouseY, partialTick);
            case POTION -> renderPotionPanel(guiGraphics, mouseX, mouseY, partialTick);
            case ATTRIBUTES -> renderAttributesPanel(guiGraphics, mouseX, mouseY, partialTick);
            case COLOR -> renderColorPanel(guiGraphics);
            case LORE -> renderLorePanel(guiGraphics, mouseX, mouseY);
            case LORE_PAINTER -> renderLorePainterPanel(guiGraphics, mouseX, mouseY);
            case NBT_ADVANCED -> {
            }
        }

        if (!this.status.getString().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.status, this.midX, this.height - 58, 0xFFFFD966);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderPanelTooltips(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            goBack();
            return true;
        }

        if (keyCode == 257 || keyCode == 335) {
            if (this.activePanel == Panel.ITEM) {
                applyToSelectedSlot();
                return true;
            }
            if (this.activePanel == Panel.NBT) {
                updateRawNbt();
                return true;
            }
            if (this.activePanel == Panel.COLOR) {
                applyColorFromHex(true);
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.activePanel == Panel.ENCHANTMENTS && this.enchantFilterBox != null && this.enchantFilterBox.isFocused()) {
            return this.enchantFilterBox.charTyped(Character.toLowerCase(codePoint), modifiers);
        }
        if (this.activePanel == Panel.POTION && this.potionFilterBox != null && this.potionFilterBox.isFocused()) {
            return this.potionFilterBox.charTyped(Character.toLowerCase(codePoint), modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (handled || button != 0) {
            return handled;
        }

        updateMouseDistance((int) mouseX, (int) mouseY);
        return switch (this.activePanel) {
            case ENCHANTMENTS -> handleEnchantingClick(mouseX, mouseY);
            case POTION -> handlePotionClick(mouseX, mouseY);
            case ATTRIBUTES -> handleAttributesClick(mouseX, mouseY);
            case COLOR -> handleColorClick(mouseX, mouseY);
            case NBT_ADVANCED -> handleNbtAdvancedClick(mouseX, mouseY);
            case LORE -> handleLoreClick(mouseX, mouseY);
            case LORE_PAINTER -> handleLorePainterClick(mouseX, mouseY);
            default -> false;
        };
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.activePanel == Panel.NBT_ADVANCED) {
            int rows = buildNbtRows().size();
            int visible = getNbtAdvancedVisibleRows();
            this.advancedScroll = Mth.clamp(this.advancedScroll - (int) Math.signum(delta), 0, Math.max(0, rows - visible));
            return true;
        }

        if (this.activePanel == Panel.LORE) {
            setLoreScroll(this.loreScroll - (int) Math.signum(delta));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.activePanel == Panel.LORE && this.draggingLoreScroll) {
            updateLoreScrollFromMouse(mouseY);
            return true;
        }
        if (this.activePanel == Panel.LORE_PAINTER && this.lorePainterDragging) {
            paintLorePainterAt(mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingLoreScroll = false;
        this.lorePainterDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void addItemPanel() {
        this.itemIdBox = addTrackedBox(new EditBox(this.font, this.midX, 55, 75, FIELD_HEIGHT,
                Component.translatable(key("item.id"))));
        this.itemIdBox.setMaxLength(100);
        this.itemIdBox.setTextColor(MAIN_COLOR);
        this.itemIdBox.setValue(this.itemIdValue);
        this.itemIdBox.setResponder(value -> {
            this.itemIdValue = value;
            if (tryApplyItemId(false)) {
                rebuildWidgets();
            }
        });
        this.mainTextBoxes.add(this.itemIdBox);

        this.countBox = addTrackedBox(numberBox(this.midX, 85, 20, FIELD_HEIGHT, 2, this.countValue, 1, MAX_COUNT));
        this.countBox.setResponder(value -> {
            this.countValue = value;
            tryApplyCount(false);
        });

        int maxDamage = getDamageMaxForField(this.previewStack);
        int damageDigits = Math.max(1, Integer.toString(maxDamage).length());
        this.damageBox = addTrackedBox(numberBox(this.midX, 115, Math.max(10 * damageDigits, 15), FIELD_HEIGHT,
                damageDigits, this.damageValue, 0, maxDamage));
        this.damageBox.setResponder(value -> {
            this.damageValue = value;
            tryApplyDamage(false);
        });

        addRenderableWidget(new InfinityEditorButton(this.midX - 82, 145, 80, FIELD_HEIGHT,
                Component.translatable(key("nbt")), button -> switchPanel(Panel.NBT)));
        addRenderableWidget(new InfinityEditorButton(this.midX + 2, 145, 80, FIELD_HEIGHT,
                Component.translatable(key("nbtadv")), button -> switchPanel(Panel.NBT_ADVANCED)));
        addRenderableWidget(new InfinityEditorButton(this.width - 75, 74, 70, FIELD_HEIGHT,
                Component.translatable(key("hideflags")), button -> switchPanel(Panel.HIDE_FLAGS)));

        addSpecialButtons();
        addNameAndLoreWidgets();
        addFormatButtons();
    }

    private void addNameAndLoreWidgets() {
        this.nameBox = addTrackedBox(legacyTextBox(this.width - 180, 50, 130, FIELD_HEIGHT,
                Component.translatable(key("item.name"))));
        this.nameBox.setMaxLength(100);
        this.nameBox.setTextColor(MAIN_COLOR);
        this.nameBox.setValue(this.nameValue);
        this.nameBox.setResponder(value -> {
            this.nameValue = value;
            applyNameToStack();
        });
        this.mainTextBoxes.add(this.nameBox);

        addRenderableWidget(new InfinityEditorButton(this.width - 45, 50, 40, FIELD_HEIGHT,
                Component.translatable(key("clear")), button -> clearCustomName()));

        int visibleLoreLines = Math.min(5, this.loreValues.size() + 1);
        for (int i = 0; i < visibleLoreLines; i++) {
            boolean realLine = i < this.loreValues.size();
            addLoreTextField(i, realLine);
        }

        addRenderableWidget(new InfinityEditorButton(this.width - 180, 100 + 30 * visibleLoreLines, 170, FIELD_HEIGHT,
                Component.translatable(key("lore")), button -> switchPanel(Panel.LORE)));
    }

    private void addLoreTextField(int line, boolean realLine) {
        EditBox loreBox = addTrackedBox(legacyTextBox(this.width - 180, 100 + 30 * line, 170, FIELD_HEIGHT,
                Component.literal("Lore " + (line + 1))));
        loreBox.setMaxLength(100);
        loreBox.setTextColor(MAIN_COLOR);
        String placeholder = "Lore " + (line + 1);
        loreBox.setValue(realLine ? this.loreValues.get(line) : placeholder);
        loreBox.setResponder(value -> {
            if (!realLine && placeholder.equals(value)) {
                return;
            }
            setLoreLine(line, value);
            applyLoreToStack();
            if (!realLine && line < 4) {
                rebuildWidgets();
            }
        });
        this.loreBoxes.add(loreBox);

        if (realLine) {
            addRenderableWidget(new InfinityEditorButton(this.width - 195, 100 + 30 * line, 14, FIELD_HEIGHT,
                    Component.literal(ChatFormatting.DARK_RED + "X"), button -> {
                removeLoreLine(line);
                rebuildWidgets();
            }));
        }
    }

    private void addSpecialButtons() {
        int y = 175;
        if (this.previewStack.isDamageableItem()) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    getUnbreakableText(), button -> toggleUnbreakable()));
            y += 30;
        }

        if (!this.previewStack.isEmpty()) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("attributes")), button -> switchPanel(Panel.ATTRIBUTES)));
            y += 30;
        }

        if (isColorApplicable(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("color")), button -> switchPanel(Panel.COLOR)));
            y += 30;
        }

        if (canShowEnchantingButton(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("enchanting")), button -> switchPanel(Panel.ENCHANTMENTS)));
            y += 30;
        }

        if (isPotionItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("potion")), button -> switchPanel(Panel.POTION)));
        }
    }

    private void addFormatButtons() {
        ChatFormatting[] formats = ChatFormatting.values();
        int colorAmount = 2 + formats.length;
        int columns = colorAmount / 2;
        int startX = this.width - 1 - 13 * ((colorAmount + 2) / 2);

        addRenderableWidget(new InfinityEditorButton(startX + 13, this.height - 30, 13, 15,
                Component.literal(String.valueOf(ChatFormatting.PREFIX_CODE)), button -> insertFormattingPrefix()));
        addRenderableWidget(new InfinityEditorButton(startX + 26, this.height - 30, 13, 15,
                Component.literal(ChatFormatting.DARK_RED + "%"), button -> stripFocusedFormatting()));

        for (int i = 2; i < colorAmount; i++) {
            ChatFormatting format = formats[i - 2];
            int x = startX + 13 * ((i % columns) + 1);
            int y = this.height - 30 + 15 * (i / columns);
            addRenderableWidget(new InfinityEditorButton(x, y, 13, 15,
                    Component.literal(format.toString() + format.getChar()), button -> insertFocusedText(format.toString())));
        }
    }

    private void addNbtPanel() {
        this.rawNbtBox = addTrackedBox(legacyTextBox(this.width / 4, 80, this.width / 2, 16,
                Component.translatable(key("nbt"))));
        this.rawNbtBox.setMaxLength(20000);
        this.rawNbtBox.setTextColor(MAIN_COLOR);
        this.rawNbtBox.setValue(this.rawNbtValue == null ? getInitialNbt(this.previewStack) : this.rawNbtValue);
        this.rawNbtBox.setResponder(value -> this.rawNbtValue = value);

        addRenderableWidget(new InfinityEditorButton(3 * this.width / 7, 100, this.width / 7, FIELD_HEIGHT,
                Component.translatable(key("nbt.update")), button -> updateRawNbt()));
        addFormatButtons();
    }

    private void addNbtAdvancedPanel() {
        this.advancedScroll = Mth.clamp(this.advancedScroll, 0, Math.max(0, buildNbtRows().size() - getNbtAdvancedVisibleRows()));
    }

    private void addHideFlagsPanel() {
        for (int i = 0; i < HideFlag.values().length; i++) {
            HideFlag flag = HideFlag.values()[i];
            addRenderableWidget(new InfinityEditorButton(this.midX - 60, 60 + 30 * i, 120, FIELD_HEIGHT,
                    getHideFlagText(flag), button -> {
                toggleHideFlag(flag);
                rebuildWidgets();
            }));
        }
    }

    private void addEnchantmentsPanel() {
        this.enchantFilterBox = addTrackedBox(new EditBox(this.font, this.width - 115, this.height - 33, 100, 18,
                Component.translatable(key("enchantment_filter"))));
        this.enchantFilterBox.setMaxLength(20);
        this.enchantFilterBox.setFilter(value -> value.matches("[a-z]*"));
        this.enchantFilterBox.setTextColor(MAIN_COLOR);
        this.enchantFilterBox.setValue(this.enchantFilterValue);
        this.enchantFilterBox.setResponder(value -> {
            this.enchantFilterValue = value.toLowerCase(Locale.ROOT);
        });

        this.enchantLevelBox = addTrackedBox(numberBox(15, this.height - 33, 40, 18, 5,
                this.enchantLevelValue, 1, MAX_ENCHANTMENT_LEVEL));
        this.enchantLevelBox.setResponder(value -> this.enchantLevelValue = value);

        addRenderableWidget(new InfinityEditorButton(15, this.height - 63, 90, OLD_BUTTON_HEIGHT,
                Component.translatable(key("enchanting.enchanttoggle." + (this.showAllEnchantments ? 0 : 1))),
                button -> toggleEnchantmentsScope()));
    }

    private void addPotionPanel() {
        this.potionFilterBox = addTrackedBox(new EditBox(this.font, this.width - 115, this.height - 33, 100, 18,
                Component.translatable(key("potion_filter"))));
        this.potionFilterBox.setMaxLength(20);
        this.potionFilterBox.setFilter(value -> value.matches("[a-z]*"));
        this.potionFilterBox.setTextColor(MAIN_COLOR);
        this.potionFilterBox.setValue(this.potionFilterValue);
        this.potionFilterBox.setResponder(value -> this.potionFilterValue = value.toLowerCase(Locale.ROOT));

        this.potionLevelBox = addTrackedBox(numberBox(15, this.height - 33, 40, 18, 3,
                this.potionLevelValue, 1, MAX_POTION_LEVEL));
        this.potionLevelBox.setResponder(value -> this.potionLevelValue = value);

        this.potionTimeBox = addTrackedBox(numberBox(15, this.height - 60, 40, 18, 5,
                this.potionTimeValue, 1, MAX_POTION_SECONDS));
        this.potionTimeBox.setResponder(value -> this.potionTimeValue = value);

        addRenderableWidget(new InfinityEditorButton(15, this.height - 120, 80, OLD_BUTTON_HEIGHT,
                Component.translatable(key("color")), button -> switchPanel(Panel.COLOR)));
        addRenderableWidget(new InfinityEditorButton(15, this.height - 90, 80, OLD_BUTTON_HEIGHT,
                Component.translatable(key("potion.showparticles." + (this.showPotionParticles ? 1 : 0))),
                button -> togglePotionParticles()));
    }

    private void addAttributesPanel() {
        this.attributeInfinityButton = addRenderableWidget(new InfinityEditorButton(15, this.height - 123, 80, OLD_BUTTON_HEIGHT,
                Component.translatable(key("attributes.infinity." + (this.attributeInfinity ? 1 : 0))),
                button -> toggleAttributeInfinity()));

        this.attributeOperationButton = addRenderableWidget(new InfinityEditorButton(15, this.height - 93, 80, OLD_BUTTON_HEIGHT,
                Component.translatable(key("attributes.operation." + this.attributeOperation)),
                button -> cycleAttributeOperation()));
        this.attributeOperationButton.active = !this.attributeInfinity;

        this.attributeSlotButton = addRenderableWidget(new InfinityEditorButton(15, this.height - 63, 80, OLD_BUTTON_HEIGHT,
                Component.translatable(key("attributes.slot." + this.attributeSlot)),
                button -> cycleAttributeSlot()));

        addRenderableWidget(new InfinityEditorButton(15, this.height - 33, 20, OLD_BUTTON_HEIGHT,
                Component.literal(this.attributeNegative ? "-" : "+"), button -> {
            this.attributeNegative = !this.attributeNegative;
            rebuildWidgets();
        }));

        this.attributeAmountBox = addTrackedBox(numberBox(38, this.height - 32, 55, 18, 8,
                this.attributeAmountValue, 0, MAX_ATTRIBUTE_INTEGER));
        this.attributeAmountBox.setResponder(value -> this.attributeAmountValue = value);
        this.attributeAmountBox.active = !this.attributeInfinity;

        this.attributeDecimalBox = addTrackedBox(numberBox(100, this.height - 32, 25, 18, 3,
                this.attributeDecimalValue, 0, 999));
        this.attributeDecimalBox.setResponder(value -> this.attributeDecimalValue = value);
        this.attributeDecimalBox.active = !this.attributeInfinity;
    }

    private void addColorPanel() {
        int color = getEditorColor();
        this.colorHexValue = formatColorHex(color);

        this.colorHexBox = addTrackedBox(new EditBox(this.font, this.midX - 25, this.midY - 85, 50, OLD_BUTTON_HEIGHT,
                Component.translatable(key("color.hex"))));
        this.colorHexBox.setMaxLength(7);
        this.colorHexBox.setFilter(value -> value.matches("#?[0-9a-fA-F]{0,6}"));
        this.colorHexBox.setTextColor(MAIN_COLOR);
        this.colorHexBox.setValue(this.colorHexValue);
        this.colorHexBox.setResponder(value -> {
            this.colorHexValue = value;
            if (!this.syncingColorControls) {
                applyColorFromHex(false);
            }
        });

        this.redSlider = addRenderableWidget(new ColorSlider(this.midX - 80, this.midY - 50, 160, OLD_BUTTON_HEIGHT,
                Component.translatable(key("color.red")), getRed(color), value -> setColorComponent(16, value)));
        this.greenSlider = addRenderableWidget(new ColorSlider(this.midX - 80, this.midY - 10, 160, OLD_BUTTON_HEIGHT,
                Component.translatable(key("color.green")), getGreen(color), value -> setColorComponent(8, value)));
        this.blueSlider = addRenderableWidget(new ColorSlider(this.midX - 80, this.midY + 30, 160, OLD_BUTTON_HEIGHT,
                Component.translatable(key("color.blue")), getBlue(color), value -> setColorComponent(0, value)));

        addRenderableWidget(new InfinityEditorButton((this.width - 60) / 2, this.midY + 65, 60, OLD_BUTTON_HEIGHT,
                Component.translatable(key("color.random")), button -> {
            setEditorColor(ThreadLocalRandom.current().nextInt(0x1000000));
            syncColorControlsFromStack();
            this.status = Component.translatable(messageKey("editor_color_updated"), this.colorHexValue);
        }));
    }

    private void addLorePainterPanel() {
        ensureLorePainterRows();

        Component insertText = Component.translatable(key("lorepainter.insert"));
        int insertWidth = this.font.width(insertText) + 5;
        addRenderableWidget(new InfinityEditorButton(this.midX - insertWidth / 2, this.height - 55, insertWidth, OLD_BUTTON_HEIGHT,
                insertText, button -> insertLorePainterRows()));

        Component scaleText = Component.translatable(key("lorepainter.scale"));
        int scaleWidth = this.font.width(scaleText) + 5;
        this.lorePainterScaleButton = addRenderableWidget(new InfinityEditorButton(this.width - scaleWidth, this.height - 20, scaleWidth, OLD_BUTTON_HEIGHT,
                scaleText, button -> cycleGuiScale()));

        this.lorePainterAddRowButton = addRenderableWidget(new InfinityEditorButton(this.midX - 20, this.height - 50, 20, OLD_BUTTON_HEIGHT,
                Component.literal("+"), button -> addLorePainterRow()));
        this.lorePainterRemoveRowButton = addRenderableWidget(new InfinityEditorButton(this.midX, this.height - 50, 20, OLD_BUTTON_HEIGHT,
                Component.literal("-"), button -> removeLorePainterRow()));
        this.lorePainterAddColumnButton = addRenderableWidget(new InfinityEditorButton(this.width - 50, this.midY - 20, 20, OLD_BUTTON_HEIGHT,
                Component.literal("+"), button -> addLorePainterColumn()));
        this.lorePainterRemoveColumnButton = addRenderableWidget(new InfinityEditorButton(this.width - 50, this.midY, 20, OLD_BUTTON_HEIGHT,
                Component.literal("-"), button -> removeLorePainterColumn()));

        Component previewText = Component.translatable(key("lorepainter.preview"));
        this.lorePainterPreviewButton = addRenderableWidget(new InfinityEditorButton(0, this.height - 20, this.font.width(previewText) + 5, OLD_BUTTON_HEIGHT,
                previewText, button -> this.lorePainterPreview = !this.lorePainterPreview));
    }

    private void addLorePanel() {
        this.loreScroll = Mth.clamp(this.loreScroll, 0, Math.max(0, this.loreValues.size() - loreLineSpaces()));
        int x = 100;
        int padding = 2;
        InfinityEditorButton button = addTopButton(x, "lore.addline", pressed -> {
            this.loreValues.add("");
            this.loreScroll = Math.max(0, this.loreValues.size() - loreLineSpaces());
            applyLoreToStack();
            rebuildWidgets();
        });
        x += button.getWidth() + padding;
        this.copyLoreButton = addTopButton(x, "lore.copylore", pressed -> copyLoreOnly());
        x += this.copyLoreButton.getWidth() + padding;
        button = addTopButton(x, "lore.copyall", pressed -> copyFullTooltip());
        x += button.getWidth() + padding;
        button = addTopButton(x, "lore.paste", pressed -> pasteLore());
        x += button.getWidth() + padding;
        button = addTopButton(x, "hideflags", pressed -> switchPanel(Panel.HIDE_FLAGS));
        x += button.getWidth() + padding;
        addTopButton(x, "lorepainter", pressed -> switchPanel(Panel.LORE_PAINTER));

        int spaces = loreLineSpaces();
        int end = Math.min(this.loreValues.size(), this.loreScroll + spaces);
        for (int i = this.loreScroll; i < end; i++) {
            int line = i;
            int row = i - this.loreScroll;
            int y = 55 + 30 * row;
            EditBox field = addTrackedBox(legacyTextBox(100, y, Math.max(80, this.width - 200), FIELD_HEIGHT,
                    Component.literal("Line " + (line + 1))));
            field.setMaxLength(500);
            field.setTextColor(MAIN_COLOR);
            field.setValue(this.loreValues.get(line));
            field.setResponder(value -> {
                setLoreLine(line, value);
                applyLoreToStack();
            });
            this.loreBoxes.add(field);

            InfinityEditorButton up = addRenderableWidget(new InfinityEditorButton(field.getX() + field.getWidth() + 3, y, 20, FIELD_HEIGHT,
                    Component.literal("\u2B06"), pressed -> moveLoreLine(line, -1)));
            up.active = line > 0;
            InfinityEditorButton down = addRenderableWidget(new InfinityEditorButton(field.getX() + field.getWidth() + 24, y, 20, FIELD_HEIGHT,
                    Component.literal("\u2B07"), pressed -> moveLoreLine(line, 1)));
            down.active = line < this.loreValues.size() - 1;
            addRenderableWidget(new InfinityEditorButton(field.getX() + field.getWidth() + 45, y, 20, FIELD_HEIGHT,
                    Component.literal("\u2715"), pressed -> {
                removeLoreLine(line);
                rebuildWidgets();
            }));
        }
        addFormatButtons();
    }

    private InfinityEditorButton addTopButton(int x, String keySuffix, InfinityEditorButton.PressAction action) {
        Component text = Component.translatable(key(keySuffix));
        int width = this.font.width(text) + 6;
        InfinityEditorButton button = addRenderableWidget(new InfinityEditorButton(x, 10, width, FIELD_HEIGHT, text, action));
        this.loreActionButtons.add(button);
        return button;
    }

    private void addBottomButtons() {
        switch (this.activePanel) {
            case ITEM -> {
                addRenderableWidget(new InfinityEditorButton(this.midX - 90, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("close")), button -> onClose()));
                addRenderableWidget(new InfinityEditorButton(this.midX - 30, this.height - 25, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("save")), button -> applyToSelectedSlot()));
                addRenderableWidget(new InfinityEditorButton(this.midX - 30, this.height - 45, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("reset")), button -> resetStack()));
                addRenderableWidget(new InfinityEditorButton(this.midX + 30, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("drop")), button -> dropEditedStack()));
            }
            case NBT -> {
                addRenderableWidget(new InfinityEditorButton(this.midX - 60, this.height - 25, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("back")), button -> goBack()));
                addRenderableWidget(new InfinityEditorButton(this.midX, this.height - 25, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("reset")), button -> resetStack()));
            }
            case NBT_ADVANCED -> addRenderableWidget(new InfinityEditorButton(this.midX - 60, this.height - 25, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                    Component.translatable(key("back")), button -> goBack()));
            default -> {
                addRenderableWidget(new InfinityEditorButton(this.midX - 90, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("back")), button -> goBack()));
                addRenderableWidget(new InfinityEditorButton(this.midX - 30, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("reset")), button -> resetStack()));
                addRenderableWidget(new InfinityEditorButton(this.midX + 30, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("drop")), button -> dropEditedStack()));
            }
        }
    }

    private EditBox addTrackedBox(EditBox box) {
        this.tickingBoxes.add(box);
        return addRenderableWidget(box);
    }

    private EditBox legacyTextBox(int x, int y, int width, int height, Component message) {
        EditBox box = new LegacyTextEditBox(this.font, x, y, width, height, message);
        box.setTextColor(MAIN_COLOR);
        return box;
    }

    private EditBox numberBox(int x, int y, int width, int height, int digits, String value, int minValue, int maxValue) {
        EditBox box = new FixedDigitEditBox(this.font, x, y, width, height, digits, minValue, maxValue);
        box.setValue(value);
        return box;
    }

    private void renderItemPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderSmallItem(guiGraphics, this.midX, 40);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("item")), this.midX, 15, MAIN_COLOR);

        drawRightLabel(guiGraphics, Component.translatable(key("item.id")), this.midX - 5, 61);
        drawRightLabel(guiGraphics, Component.translatable(key("item.count")), this.midX - 5, 91);
        drawRightLabel(guiGraphics, Component.translatable(key("item.meta")), this.midX - 5, 121);

        guiGraphics.drawString(this.font, Component.translatable(key("item.name")), this.width - 110, 35, MAIN_COLOR);
        guiGraphics.drawString(this.font, Component.translatable(key("item.lore")), this.width - 110, 80, MAIN_COLOR);
    }

    private void renderNbtPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 38);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("nbt")), this.midX, 15, MAIN_COLOR);
        if (!this.nbtFeedback.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.nbtFeedback, this.midX, 130, this.nbtFeedbackGood ? GOOD_GREEN : BAD_RED);
        }
    }

    private void renderNbtAdvancedPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int mainColor = 0xFF1EC8FF;
        guiGraphics.fillGradient(18, 18, this.width - 18, 38, 0xFF323232, mainColor);
        guiGraphics.fill(20, 40, this.width - 20, this.height - 20, 0xDE323232);
        guiGraphics.fill(18, 38, this.width - 18, 40, mainColor);
        guiGraphics.fill(18, 40, 20, this.height - 20, mainColor);
        guiGraphics.fill(this.width - 20, 40, this.width - 18, this.height - 20, mainColor);
        guiGraphics.fill(18, this.height - 20, this.width - 18, this.height - 18, mainColor);
        guiGraphics.drawString(this.font, ModSource.MODID + " - " + Component.translatable(key("nbtadv")).getString(), 25, 26, CYAN);

        List<NbtRow> rows = buildNbtRows();
        int visibleRows = getNbtAdvancedVisibleRows();
        this.advancedScroll = Mth.clamp(this.advancedScroll, 0, Math.max(0, rows.size() - visibleRows));
        int end = Math.min(rows.size(), this.advancedScroll + visibleRows);
        for (int i = this.advancedScroll; i < end; i++) {
            NbtRow row = rows.get(i);
            int y = 48 + (i - this.advancedScroll) * 12;
            int x = 25 + row.depth() * 12;
            int color = row.isExpandable() ? MAIN_COLOR : 0xFFFFFFFF;
            guiGraphics.drawString(this.font, row.displayText(), x, y, color);
        }

        String unfinished = Component.translatable(key("nbtadv.unfinished")).getString();
        guiGraphics.drawString(this.font, unfinished, this.width - this.font.width(unfinished) - 25, this.height - 30, 0xFFFFFF32);
    }

    private void renderHideFlagsPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderSimpleItemPanelTitle(guiGraphics, "hideflags", 40);
    }

    private void renderSimpleItemPanelTitle(GuiGraphics guiGraphics, String titleKey, int itemY) {
        renderSmallItem(guiGraphics, this.midX, itemY);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key(titleKey)), this.midX, 15, MAIN_COLOR);
    }

    private void renderEnchantmentsPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("enchanting")), this.midX, 15, MAIN_COLOR);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("enchanting.search")),
                this.enchantFilterBox.getX() + this.enchantFilterBox.getWidth() / 2,
                this.enchantFilterBox.getY() - 12, MAIN_COLOR);
        renderActiveEnchantments(guiGraphics);
        updateMouseDistance(mouseX, mouseY);
        renderLargePreviewItem(guiGraphics);

        List<Enchantment> filteredEnchantments = getFilteredEnchantments(this.previewStack);
        if (filteredEnchantments.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("no_enchantment_matches")),
                    this.midX, this.midY + 34, CONTRAST_COLOR);
            return;
        }

        int radius = getRingRadius();
        double angle = (2.0D * Math.PI) / filteredEnchantments.size();
        double rotation = (this.rotOff + (Math.abs(this.mouseDist - radius) >= RING_HOVER_WIDTH ? partialTick : 0.0D)) / 60.0D;
        for (int i = 0; i < filteredEnchantments.size(); i++) {
            Enchantment enchantment = filteredEnchantments.get(i);
            double enchantmentAngle = rotation + angle * i;
            int x = (int) (this.midX + radius * Math.cos(enchantmentAngle));
            int y = (int) (this.midY + radius * Math.sin(enchantmentAngle));
            guiGraphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(formatRingEnchantmentName(enchantment), 118), x, y - 17, MAIN_COLOR);
            guiGraphics.renderItem(this.enchantBook, x - 8, y - 8);
            guiGraphics.fill(x - 1, y - 1, x + 1, y + 1, 0xFFFFFFFF);
        }

        if (isMouseOverCenter(mouseX, mouseY)) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("enchanting.addall")),
                    this.midX, this.midY, CONTRAST_COLOR);
        }
    }

    private void renderPotionPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("potion")), this.midX, 15, MAIN_COLOR);
        List<MobEffectInstance> activeEffects = getCustomPotionEffects();
        int start = this.midY - 5 * activeEffects.size();
        for (int i = 0; i < activeEffects.size(); i++) {
            MobEffectInstance effect = activeEffects.get(i);
            int color = effect.getEffect().getCategory() == MobEffectCategory.HARMFUL ? BAD_RED : CONTRAST_COLOR;
            guiGraphics.drawString(this.font, formatPotionEffect(effect), 5, start + i * 10, color);
        }

        guiGraphics.drawCenteredString(this.font, Component.translatable(key("enchanting.search")),
                this.potionFilterBox.getX() + this.potionFilterBox.getWidth() / 2,
                this.potionFilterBox.getY() - 12, MAIN_COLOR);
        guiGraphics.drawString(this.font, Component.translatable(key("potion.time")), 62, this.height - 56, MAIN_COLOR);
        guiGraphics.drawString(this.font, Component.translatable(key("potion.level")), 62, this.height - 29, MAIN_COLOR);

        updateMouseDistance(mouseX, mouseY);
        renderLargePreviewItem(guiGraphics);

        List<MobEffect> filteredEffects = getFilteredPotionEffects();
        if (filteredEffects.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("no_potion_matches")),
                    this.midX, this.midY + 34, CONTRAST_COLOR);
            return;
        }

        int radius = getRingRadius();
        double angle = (2.0D * Math.PI) / filteredEffects.size();
        double rotation = (this.rotOff + (Math.abs(this.mouseDist - radius) >= RING_HOVER_WIDTH ? partialTick : 0.0D)) / 60.0D;
        for (int i = 0; i < filteredEffects.size(); i++) {
            MobEffect effect = filteredEffects.get(i);
            double effectAngle = rotation + angle * i;
            int x = (int) (this.midX + radius * Math.cos(effectAngle));
            int y = (int) (this.midY + radius * Math.sin(effectAngle));
            guiGraphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(formatPotionRingName(effect), 118), x, y - 17, MAIN_COLOR);
            guiGraphics.renderItem(this.potionIcon, x - 8, y - 8);
            guiGraphics.fill(x - 1, y - 1, x + 1, y + 1, 0xFFFFFFFF);
        }

        if (isMouseOverCenter(mouseX, mouseY)) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("enchanting.addall")),
                    this.midX, this.midY, CONTRAST_COLOR);
        }
    }

    private void renderAttributesPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("attributes")), this.midX, 15, MAIN_COLOR);
        List<AttributeEntry> entries = getAttributeModifierEntries();
        int start = this.midY - 5 * entries.size();
        for (int i = 0; i < entries.size(); i++) {
            guiGraphics.drawString(this.font, formatAttributeEntry(entries.get(i)), 5, start + i * 10, MAIN_COLOR);
        }

        guiGraphics.drawString(this.font, ".", 96, this.height - 26, MAIN_COLOR);
        updateMouseDistance(mouseX, mouseY);
        renderLargePreviewItem(guiGraphics);

        List<Attribute> attributes = getSharedAttributes();
        int radius = getRingRadius();
        double angle = (2.0D * Math.PI) / attributes.size();
        double rotation = (this.rotOff + (Math.abs(this.mouseDist - radius) >= RING_HOVER_WIDTH ? partialTick : 0.0D)) / 60.0D;
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            double attributeAngle = rotation + angle * i;
            int x = (int) (this.midX + radius * Math.cos(attributeAngle));
            int y = (int) (this.midY + radius * Math.sin(attributeAngle));
            guiGraphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(Component.translatable(attribute.getDescriptionId()).getString(), 118),
                    x, y - 17, MAIN_COLOR);
            guiGraphics.renderItem(this.attributeIcon, x - 8, y - 8);
            guiGraphics.fill(x - 1, y - 1, x + 1, y + 1, 0xFFFFFFFF);
        }
    }

    private void renderColorPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(4.0F, 4.0F, 1.0F);
        guiGraphics.renderItem(this.previewStack, this.width / 8 - 8, 5);
        guiGraphics.pose().popPose();

        guiGraphics.drawCenteredString(this.font, Component.translatable(key("color")), this.midX, 15, MAIN_COLOR);

        int color = getEditorColor();
        if (this.redSlider != null && this.greenSlider != null && this.blueSlider != null) {
            guiGraphics.fill(this.redSlider.getX() - 5, this.redSlider.getY() - 5,
                    this.blueSlider.getX() + this.blueSlider.getWidth() + 5,
                    this.blueSlider.getY() + this.blueSlider.getHeight() + 5,
                    argb(100, color));
            guiGraphics.fill(this.redSlider.getX() - 2, this.redSlider.getY() - 2,
                    this.redSlider.getX() + this.redSlider.getWidth() + 2,
                    this.redSlider.getY() + this.redSlider.getHeight() + 2,
                    argb(255, getRed(color) << 16));
            guiGraphics.fill(this.greenSlider.getX() - 2, this.greenSlider.getY() - 2,
                    this.greenSlider.getX() + this.greenSlider.getWidth() + 2,
                    this.greenSlider.getY() + this.greenSlider.getHeight() + 2,
                    argb(255, getGreen(color) << 8));
            guiGraphics.fill(this.blueSlider.getX() - 2, this.blueSlider.getY() - 2,
                    this.blueSlider.getX() + this.blueSlider.getWidth() + 2,
                    this.blueSlider.getY() + this.blueSlider.getHeight() + 2,
                    argb(255, getBlue(color)));
            renderDyeGrid(guiGraphics);
        }
    }

    private void renderLorePanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderSmallItem(guiGraphics, this.midX, 35);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("lore")), this.midX, 15, MAIN_COLOR);
        int spaces = loreLineSpaces();
        int size = this.loreValues.size();
        this.loreScroll = Mth.clamp(this.loreScroll, 0, Math.max(0, size - spaces));
        int y = 25;
        for (int i = this.loreScroll; i < this.loreScroll + spaces && i < size; i++) {
            String label = "Line " + (i + 1);
            y = 55 + 30 * (i - this.loreScroll);
            guiGraphics.drawString(this.font, label, 90 - this.font.width(label), y + 6, 0xFFFFFFFF);
        }

        int actionY = y + 30;
        for (InfinityEditorButton button : this.loreActionButtons) {
            button.setY(actionY);
        }
        if (this.copyLoreButton != null) {
            this.copyLoreButton.active = size > 0;
        }

        guiGraphics.hLine(this.width - 15, this.width - 5, 50, 0xFFAAAAAA);
        guiGraphics.hLine(this.width - 15, this.width - 5, this.height - 50, 0xFFAAAAAA);
        int scrollHeight = this.height - 103;
        float covered = size < spaces || size == 0 ? 1.0F : spaces / (float) size;
        int coveredHeight = (int) Math.max(1, scrollHeight * covered);
        float div = size - spaces;
        float perc = div <= 0.0F ? 0.0F : this.loreScroll / div;
        int scrollY = (int) (52 + (scrollHeight - coveredHeight) * perc);
        guiGraphics.fill(this.width - 14, scrollY, this.width - 5, scrollY + coveredHeight, 0xFF666666);
    }

    private void renderLorePainterPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ensureLorePainterRows();
        if (this.lorePainterDragging) {
            paintLorePainterAt(mouseX, mouseY);
        }

        guiGraphics.drawCenteredString(this.font, Component.translatable(key("lorepainter")), this.midX, 15, MAIN_COLOR);

        int gridX = getLorePainterGridX();
        int gridY = getLorePainterGridY();
        guiGraphics.drawCenteredString(this.font, this.lorePainterWidth + "x" + this.lorePainterHeight,
                this.midX, gridY - 15, MAIN_COLOR);

        int yOffset = 0;
        for (List<LorePixel> row : this.lorePainterRows) {
            guiGraphics.drawString(this.font, buildLorePainterRow(row), gridX, gridY + yOffset, 0xFFFFFFFF, false);
            yOffset += 9;
        }

        int rowButtonY = gridY + getLorePainterSizeY() + 9;
        if (this.lorePainterAddRowButton != null) {
            this.lorePainterAddRowButton.setY(rowButtonY);
        }
        if (this.lorePainterRemoveRowButton != null) {
            this.lorePainterRemoveRowButton.setY(rowButtonY);
            this.lorePainterRemoveRowButton.active = this.lorePainterHeight > 1;
        }

        int columnButtonX = gridX + getLorePainterSizeX() + 9;
        if (this.lorePainterAddColumnButton != null) {
            this.lorePainterAddColumnButton.setX(columnButtonX);
        }
        if (this.lorePainterRemoveColumnButton != null) {
            this.lorePainterRemoveColumnButton.setX(columnButtonX);
            this.lorePainterRemoveColumnButton.active = this.lorePainterWidth > 1;
        }

        String symbols = buildLorePainterSymbols();
        guiGraphics.drawString(this.font, symbols, 0, 0, 0xFFFFFFFF, false);
        guiGraphics.drawString(this.font, this.currentLorePixel.format() + Component.translatable(key("lorepainter.symbol." + this.currentLorePixel.symbol.nameKey())).getString(),
                2, 10, 0xFFFFFFFF, false);

        String colors = buildLorePainterColors();
        int colorX = getLorePainterColorX();
        guiGraphics.drawString(this.font, colors, colorX, 0, 0xFFFFFFFF, false);
        String colorName = this.currentLorePixel.format() + this.currentLorePixel.color.getName();
        guiGraphics.drawString(this.font, colorName, this.width - this.font.width(colorName) - 2, 10, 0xFFFFFFFF, false);
    }

    private void renderItemTooltipPreview(GuiGraphics guiGraphics) {
        if (this.previewStack.isEmpty() || this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.8F, 0.8F, 1.0F);
        guiGraphics.renderTooltip(this.font, this.previewStack, 0, 25);
        guiGraphics.pose().popPose();
    }

    private void renderPrettyNbt(GuiGraphics guiGraphics) {
        List<Component> lines = getPrettyNbtLines();
        if (!lines.isEmpty()) {
            guiGraphics.renderComponentTooltip(this.font, lines, 0, this.height);
        }
    }

    private void renderSmallItem(GuiGraphics guiGraphics, int centerX, int centerY) {
        if (this.previewStack.isEmpty()) {
            return;
        }
        int x = centerX - 8;
        int y = centerY - 8;
        guiGraphics.renderItem(this.previewStack, x, y);
        guiGraphics.renderItemDecorations(this.font, this.previewStack, x, y);
    }

    private void renderLargePreviewItem(GuiGraphics guiGraphics) {
        if (this.previewStack.isEmpty()) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.midX, this.midY, 100.0F);
        guiGraphics.pose().mulPose(Axis.ZN.rotationDegrees(this.rotOff * 3.0F));
        guiGraphics.pose().scale(5.0F, 5.0F, 1.0F);
        guiGraphics.renderItem(this.previewStack, -8, -8);
        guiGraphics.pose().popPose();
    }

    private void renderPanelTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.activePanel == Panel.ITEM) {
            if (isMouseIn(mouseX, mouseY, this.midX - 8, 32, ITEM_SIZE, ITEM_SIZE)) {
                guiGraphics.renderTooltip(this.font, this.previewStack, mouseX, mouseY);
            }
            if (this.itemIdBox != null && this.itemIdBox.getValue().length() > 9
                    && isMouseIn(mouseX, mouseY, this.itemIdBox.getX(), this.itemIdBox.getY(), this.itemIdBox.getWidth(), this.itemIdBox.getHeight())) {
                guiGraphics.renderTooltip(this.font, Component.literal(this.itemIdBox.getValue()), mouseX, mouseY);
            }
            if (isMouseIn(mouseX, mouseY, this.midX + 30, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT)) {
                guiGraphics.renderComponentTooltip(this.font, List.of(
                        Component.translatable(key("item.drop.tooltip.1")),
                        Component.translatable(key("item.drop.tooltip.2"))), mouseX, mouseY);
            }
        } else if (this.activePanel == Panel.LORE) {
            for (EditBox box : this.loreBoxes) {
                if (isMouseIn(mouseX, mouseY, box.getX(), box.getY(), box.getWidth(), box.getHeight())) {
                    String value = box.getValue().replace(ChatFormatting.PREFIX_CODE, '&');
                    if (!value.isEmpty()) {
                        guiGraphics.renderTooltip(this.font, Component.literal(value), mouseX, mouseY);
                    }
                    return;
                }
            }
            if (isMouseIn(mouseX, mouseY, this.midX - 9, 27, 18, 18)) {
                guiGraphics.renderTooltip(this.font, this.previewStack, mouseX, mouseY);
            }
        } else if (this.activePanel == Panel.LORE_PAINTER) {
            if (this.lorePainterScaleButton != null && isMouseIn(mouseX, mouseY,
                    this.lorePainterScaleButton.getX(), this.lorePainterScaleButton.getY(),
                    this.lorePainterScaleButton.getWidth(), this.lorePainterScaleButton.getHeight())
                    && this.minecraft != null) {
                guiGraphics.renderTooltip(this.font, Component.literal(String.valueOf(this.minecraft.options.guiScale().get())), mouseX, mouseY);
                return;
            }
            boolean hoveringPreview = this.lorePainterPreviewButton != null && isMouseIn(mouseX, mouseY,
                    this.lorePainterPreviewButton.getX(), this.lorePainterPreviewButton.getY(),
                    this.lorePainterPreviewButton.getWidth(), this.lorePainterPreviewButton.getHeight());
            if (this.lorePainterPreview || hoveringPreview) {
                List<Component> lines = new ArrayList<>();
                lines.add(Component.translatable(key("lorepainter")));
                for (List<LorePixel> row : this.lorePainterRows) {
                    lines.add(Component.literal(buildLorePainterRow(row)));
                }
                guiGraphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
            }
        }
    }

    private void drawRightLabel(GuiGraphics guiGraphics, Component text, int rightX, int y) {
        guiGraphics.drawString(this.font, text, rightX - this.font.width(text), y, MAIN_COLOR);
    }

    private boolean handleEnchantingClick(double mouseX, double mouseY) {
        if (tryRemoveActiveEnchantment(mouseX, mouseY)) {
            return true;
        }
        if (isMouseOverCenter(mouseX, mouseY)) {
            addMatchingEnchantments();
            return true;
        }
        return tryAddRingEnchantment(mouseX, mouseY);
    }

    private boolean handlePotionClick(double mouseX, double mouseY) {
        if (tryRemoveActivePotionEffect(mouseX, mouseY)) {
            return true;
        }
        if (isMouseOverCenter(mouseX, mouseY)) {
            addMatchingPotionEffects();
            return true;
        }
        return tryAddRingPotionEffect(mouseX, mouseY);
    }

    private boolean handleAttributesClick(double mouseX, double mouseY) {
        if (tryRemoveActiveAttributeModifier(mouseX, mouseY)) {
            return true;
        }
        return tryAddRingAttribute(mouseX, mouseY);
    }

    private boolean handleColorClick(double mouseX, double mouseY) {
        if (!shouldShowDyeGrid() || this.blueSlider == null) {
            return false;
        }

        int gridX = this.blueSlider.getX();
        int gridY = this.blueSlider.getY() + this.blueSlider.getHeight() + 10;
        if (!isMouseIn(mouseX, mouseY, gridX, gridY, 160, 40)) {
            return false;
        }

        int column = ((int) mouseX - gridX) / 20;
        int row = ((int) mouseY - gridY) / 20;
        int index = column + row * 8;
        DyeColor[] colors = DyeColor.values();
        if (index < 0 || index >= colors.length) {
            return false;
        }

        addDyeToColor(colors[index]);
        syncColorControlsFromStack();
        this.status = Component.translatable(messageKey("editor_color_updated"), this.colorHexValue);
        return true;
    }

    private boolean handleLoreClick(double mouseX, double mouseY) {
        if (isMouseIn(mouseX, mouseY, this.width - 15, 50, 11, this.height - 99)) {
            this.draggingLoreScroll = true;
            updateLoreScrollFromMouse(mouseY);
            return true;
        }
        return false;
    }

    private boolean handleLorePainterClick(double mouseX, double mouseY) {
        this.lorePainterDragging = true;
        paintLorePainterAt(mouseX, mouseY);
        return true;
    }

    private boolean handleNbtAdvancedClick(double mouseX, double mouseY) {
        if (mouseX < 20 || mouseX > this.width - 20 || mouseY < 40 || mouseY > this.height - 20) {
            return false;
        }

        int rowIndex = this.advancedScroll + ((int) mouseY - 48) / 12;
        List<NbtRow> rows = buildNbtRows();
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return false;
        }

        NbtRow row = rows.get(rowIndex);
        if (!row.isExpandable()) {
            return false;
        }

        if (this.expandedNbtPaths.contains(row.path())) {
            this.expandedNbtPaths.remove(row.path());
        } else {
            this.expandedNbtPaths.add(row.path());
        }
        return true;
    }

    private void switchPanel(Panel panel) {
        if (this.activePanel == panel) {
            return;
        }
        captureFieldValues();
        if (this.activePanel == Panel.ITEM) {
            applyMainFieldsToStack(false);
        }
        this.activePanel = panel;
        this.status = Component.empty();
        rebuildWidgets();
    }

    private void goBack() {
        if (this.activePanel == Panel.ITEM) {
            onClose();
            return;
        }

        this.activePanel = Panel.ITEM;
        readMainFieldsFromStack(this.previewStack);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        rebuildWidgets();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    private void applyToSelectedSlot() {
        if (!applyMainFieldsToStack(true) || this.previewStack.isEmpty() || this.minecraft == null || this.minecraft.player == null || this.minecraft.gameMode == null) {
            return;
        }

        if (!this.minecraft.player.getAbilities().instabuild) {
            this.status = Component.translatable(messageKey("editor_requires_creative"));
            return;
        }

        int selected = this.minecraft.player.getInventory().selected;
        ItemStack inventoryStack = this.previewStack.copy();
        this.minecraft.player.getInventory().items.set(selected, inventoryStack);
        this.minecraft.gameMode.handleCreativeModeItemAdd(inventoryStack.copy(), 36 + selected);
        this.status = Component.translatable(messageKey("editor_applied"), inventoryStack.getHoverName());
    }

    private void dropEditedStack() {
        if (Screen.hasShiftDown()) {
            copyGiveCommand();
            return;
        }

        if (!applyMainFieldsToStack(true) || this.previewStack.isEmpty() || this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }

        if (this.minecraft.player == null || !this.minecraft.player.getAbilities().instabuild) {
            this.status = Component.translatable(messageKey("editor_requires_creative"));
            return;
        }

        ItemStack dropped = this.previewStack.copy();
        this.minecraft.gameMode.handleCreativeModeItemDrop(dropped);
        this.status = Component.translatable(messageKey("editor_dropped"), dropped.getHoverName());
    }

    private void copyGiveCommand() {
        if (!applyMainFieldsToStack(true) || this.previewStack.isEmpty() || this.minecraft == null) {
            return;
        }
        this.minecraft.keyboardHandler.setClipboard(GiveHelper.getStringFromItemStack(this.previewStack));
        this.status = Component.translatable(messageKey("editor_copied"));
    }

    private void resetStack() {
        if (this.activePanel == Panel.NBT) {
            this.previewStack.setTag(new CompoundTag());
        } else {
            this.previewStack.setTag(null);
        }
        readMainFieldsFromStack(this.previewStack);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.nbtFeedback = "";
        rebuildWidgets();
    }

    private void saveRealm() {
        if (!applyMainFieldsToStack(true) || this.previewStack.isEmpty() || this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        RealmController realmController = ModSource.getOrCreateRealmController(this.minecraft.gameDirectory);
        if (realmController != null) {
            realmController.addItemStack(this.minecraft.player, this.previewStack.copy());
            this.status = Component.translatable(messageKey("editor_saved"), this.previewStack.getHoverName());
        }
    }

    private boolean applyMainFieldsToStack(boolean updateStatus) {
        try {
            tryApplyItemId(true);
            tryApplyCount(true);
            tryApplyDamage(true);
            applyNameToStack();
            applyLoreToStack();
            this.rawNbtValue = getInitialNbt(this.previewStack);
            return true;
        } catch (IllegalArgumentException exception) {
            if (updateStatus) {
                this.status = Component.literal(exception.getMessage());
            }
            return false;
        }
    }

    private boolean tryApplyItemId(boolean throwOnError) {
        String idText = normalizeItemId(this.itemIdBox == null ? this.itemIdValue : this.itemIdBox.getValue());
        ResourceLocation id = ResourceLocation.tryParse(idText);
        Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == Items.AIR && !"minecraft:air".equals(idText)) {
            if (throwOnError) {
                throw new IllegalArgumentException(Component.translatable(messageKey("editor_invalid_item"), idText).getString());
            }
            return false;
        }

        this.itemIdValue = stripMinecraftNamespace(id);
        if (this.previewStack.is(item)) {
            return false;
        }

        CompoundTag tag = this.previewStack.getTag() == null ? null : this.previewStack.getTag().copy();
        int count = this.previewStack.getCount() <= 0 ? 1 : this.previewStack.getCount();
        this.previewStack = new ItemStack(item, count);
        this.previewStack.setTag(tag);
        this.damageValue = Integer.toString(Math.min(getDamageMaxForField(this.previewStack), parsePositiveOrZero(this.damageValue)));
        tryApplyDamage(false);
        this.attributeSlot = getDefaultAttributeSlot(this.previewStack);
        this.colorHexValue = formatColorHex(getEditorColor());
        return true;
    }

    private void tryApplyCount(boolean throwOnError) {
        String value = this.countBox == null ? this.countValue : this.countBox.getValue();
        if (value == null || value.isBlank()) {
            if (throwOnError) {
                throw new IllegalArgumentException(Component.translatable(messageKey("editor_invalid_count"), MAX_COUNT).getString());
            }
            return;
        }

        int count = Integer.parseInt(value);
        if (count < 1 || count > MAX_COUNT) {
            if (throwOnError) {
                throw new IllegalArgumentException(Component.translatable(messageKey("editor_invalid_count"), MAX_COUNT).getString());
            }
            return;
        }

        this.countValue = Integer.toString(count);
        this.previewStack.setCount(count);
    }

    private void tryApplyDamage(boolean throwOnError) {
        String value = this.damageBox == null ? this.damageValue : this.damageBox.getValue();
        if (value == null || value.isBlank()) {
            if (throwOnError) {
                throw new IllegalArgumentException(Component.translatable(messageKey("editor_invalid_damage"), getDamageMaxForField(this.previewStack)).getString());
            }
            return;
        }

        int damage = Integer.parseInt(value);
        int maxDamage = getDamageMaxForField(this.previewStack);
        if (damage < 0 || damage > maxDamage) {
            if (throwOnError) {
                throw new IllegalArgumentException(Component.translatable(messageKey("editor_invalid_damage"), maxDamage).getString());
            }
            return;
        }

        this.damageValue = Integer.toString(damage);
        this.previewStack.setDamageValue(damage);
    }

    private void applyNameToStack() {
        String value = this.nameBox == null ? this.nameValue : this.nameBox.getValue();
        this.nameValue = value;
        if (value == null || value.isBlank()) {
            this.previewStack.resetHoverName();
            cleanupEmptyDisplayTag();
            return;
        }

        ItemStack withoutName = this.previewStack.copy();
        withoutName.resetHoverName();
        if (!this.previewStack.hasCustomHoverName() && value.equals(withoutName.getHoverName().getString())) {
            return;
        }

        this.previewStack.setHoverName(Component.literal(value));
    }

    private void clearCustomName() {
        this.previewStack.resetHoverName();
        this.nameValue = this.previewStack.getHoverName().getString();
        if (this.nameBox != null) {
            this.nameBox.setValue(this.nameValue);
        }
        cleanupEmptyDisplayTag();
    }

    private void applyLoreToStack() {
        CompoundTag tag = this.previewStack.getOrCreateTag();
        CompoundTag display = tag.getCompound(DISPLAY_TAG);
        if (this.loreValues.isEmpty()) {
            display.remove(LORE_TAG);
        } else {
            ListTag lore = new ListTag();
            for (String line : this.loreValues) {
                lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(line))));
            }
            display.put(LORE_TAG, lore);
        }

        if (display.isEmpty()) {
            tag.remove(DISPLAY_TAG);
        } else {
            tag.put(DISPLAY_TAG, display);
        }
        cleanupEmptyTag();
    }

    private void setLoreLine(int line, String value) {
        while (this.loreValues.size() <= line) {
            this.loreValues.add("");
        }
        this.loreValues.set(line, value);
    }

    private void removeLoreLine(int line) {
        if (line >= 0 && line < this.loreValues.size()) {
            this.loreValues.remove(line);
            this.loreScroll = Mth.clamp(this.loreScroll, 0, Math.max(0, this.loreValues.size() - loreLineSpaces()));
            applyLoreToStack();
        }
    }

    private void moveLoreLine(int line, int direction) {
        int other = line + direction;
        if (line < 0 || line >= this.loreValues.size() || other < 0 || other >= this.loreValues.size()) {
            return;
        }
        String value = this.loreValues.get(line);
        this.loreValues.set(line, this.loreValues.get(other));
        this.loreValues.set(other, value);
        applyLoreToStack();
        rebuildWidgets();
    }

    private void copyLoreOnly() {
        if (this.minecraft != null) {
            this.minecraft.keyboardHandler.setClipboard(String.join("\n", this.loreValues));
        }
    }

    private void copyFullTooltip() {
        if (this.minecraft != null && this.minecraft.player != null) {
            List<Component> tooltip = this.previewStack.getTooltipLines(this.minecraft.player, this.minecraft.options.advancedItemTooltips ? net.minecraft.world.item.TooltipFlag.Default.ADVANCED : net.minecraft.world.item.TooltipFlag.Default.NORMAL);
            List<String> lines = new ArrayList<>();
            for (Component component : tooltip) {
                lines.add(component.getString());
            }
            this.minecraft.keyboardHandler.setClipboard(String.join("\n", lines));
        }
    }

    private void pasteLore() {
        if (this.minecraft == null) {
            return;
        }
        String clipboard = this.minecraft.keyboardHandler.getClipboard();
        if (clipboard == null || clipboard.isBlank()) {
            return;
        }
        for (String line : clipboard.split("\\r?\\n")) {
            if (!line.startsWith(String.valueOf(ChatFormatting.PREFIX_CODE)) && line.length() > 1) {
                line = ChatFormatting.RESET.toString() + ChatFormatting.GRAY + line;
            }
            this.loreValues.add(line);
        }
        applyLoreToStack();
        rebuildWidgets();
    }

    private void updateRawNbt() {
        String raw = this.rawNbtBox == null ? this.rawNbtValue : this.rawNbtBox.getValue();
        try {
            CompoundTag tag = parseNbt(raw);
            this.previewStack.setTag(tag);
            readMainFieldsFromStack(this.previewStack);
            this.rawNbtValue = getInitialNbt(this.previewStack);
            this.nbtFeedbackGood = true;
            this.nbtFeedback = "Looks good";
        } catch (CommandSyntaxException exception) {
            this.nbtFeedbackGood = false;
            this.nbtFeedback = exception.getMessage();
        }
    }

    private void toggleUnbreakable() {
        CompoundTag tag = this.previewStack.getOrCreateTag();
        boolean current = tag.getBoolean("Unbreakable");
        if (current) {
            tag.remove("Unbreakable");
        } else {
            tag.putBoolean("Unbreakable", true);
        }
        cleanupEmptyTag();
        this.rawNbtValue = getInitialNbt(this.previewStack);
        rebuildWidgets();
    }

    private Component getUnbreakableText() {
        boolean unbreakable = this.previewStack.getTag() != null && this.previewStack.getTag().getBoolean("Unbreakable");
        return Component.translatable(key("tag.unbreakable." + (unbreakable ? 1 : 0)));
    }

    private void toggleHideFlag(HideFlag flag) {
        CompoundTag tag = this.previewStack.getOrCreateTag();
        int value = tag.getInt(HIDE_FLAGS_TAG);
        if ((value & flag.mask()) != 0) {
            value &= ~flag.mask();
        } else {
            value |= flag.mask();
        }
        if (value == 0) {
            tag.remove(HIDE_FLAGS_TAG);
        } else {
            tag.putInt(HIDE_FLAGS_TAG, value);
        }
        cleanupEmptyTag();
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    private Component getHideFlagText(HideFlag flag) {
        CompoundTag tag = this.previewStack.getTag();
        boolean hidden = tag != null && (tag.getInt(HIDE_FLAGS_TAG) & flag.mask()) != 0;
        return Component.translatable(key(flag.translationKey() + "." + (hidden ? 1 : 0)));
    }

    private void toggleEnchantmentsScope() {
        this.showAllEnchantments = !this.showAllEnchantments;
        rebuildWidgets();
    }

    private void renderActiveEnchantments(GuiGraphics guiGraphics) {
        List<EnchantmentEntry> activeEnchantments = getStoredEnchantments(this.previewStack);
        int startY = this.midY - activeEnchantments.size() * 5;
        for (int i = 0; i < activeEnchantments.size(); i++) {
            EnchantmentEntry entry = activeEnchantments.get(i);
            int color = entry.enchantment() == null ? BAD_RED : MAIN_COLOR;
            guiGraphics.drawString(this.font, formatStoredEnchantment(entry), 5, startY + i * 10, color);
        }
    }

    private boolean tryRemoveActiveEnchantment(double mouseX, double mouseY) {
        List<EnchantmentEntry> activeEnchantments = getStoredEnchantments(this.previewStack);
        if (activeEnchantments.isEmpty()) {
            return false;
        }

        int startY = this.midY - activeEnchantments.size() * 5;
        int listWidth = this.font.width("Unbreaking 32767");
        for (EnchantmentEntry entry : activeEnchantments) {
            listWidth = Math.max(listWidth, this.font.width(formatStoredEnchantment(entry)));
        }

        if (mouseX < 0 || mouseX > 5 + listWidth || mouseY < startY || mouseY >= startY + activeEnchantments.size() * 10) {
            return false;
        }

        int index = (int) ((mouseY - startY) / 10);
        EnchantmentEntry entry = activeEnchantments.get(index);
        if (removeEnchantmentAtIndex(index)) {
            Component name = entry.enchantment() == null
                    ? Component.literal(String.valueOf(entry.id()))
                    : Component.translatable(entry.enchantment().getDescriptionId());
            this.status = Component.translatable(messageKey("editor_enchantment_removed"), name);
        }
        return true;
    }

    private boolean tryAddRingEnchantment(double mouseX, double mouseY) {
        List<Enchantment> filteredEnchantments = getFilteredEnchantments(this.previewStack);
        if (filteredEnchantments.isEmpty() || Math.abs(this.mouseDist - getRingRadius()) >= RING_HOVER_WIDTH) {
            return false;
        }

        double angle = (2.0D * Math.PI) / filteredEnchantments.size();
        int lowDist = Integer.MAX_VALUE;
        Enchantment closestEnchantment = null;
        for (int i = 0; i < filteredEnchantments.size(); i++) {
            double enchantmentAngle = this.rotOff / 60.0D + angle * i;
            int x = (int) (this.midX + getRingRadius() * Math.cos(enchantmentAngle));
            int y = (int) (this.midY + getRingRadius() * Math.sin(enchantmentAngle));
            int distX = x - (int) mouseX;
            int distY = y - (int) mouseY;
            int dist = (int) Math.sqrt(distX * distX + distY * distY);
            if (dist < RING_ICON_HIT_RADIUS && dist < lowDist) {
                lowDist = dist;
                closestEnchantment = filteredEnchantments.get(i);
            }
        }

        if (closestEnchantment == null) {
            return false;
        }

        addEnchantment(closestEnchantment);
        return true;
    }

    private void addEnchantment(Enchantment enchantment) {
        int level = getLevelForEnchantment(enchantment);
        if (level < 1) {
            return;
        }
        putEnchantment(enchantment, level);
        this.status = Component.translatable(messageKey("editor_enchantment_added"),
                Component.translatable(enchantment.getDescriptionId()), level);
    }

    private void addMatchingEnchantments() {
        List<Enchantment> enchantments = getFilteredEnchantments(this.previewStack);
        if (enchantments.isEmpty()) {
            this.status = Component.translatable(messageKey("editor_no_enchantment_match"));
            return;
        }

        for (Enchantment enchantment : enchantments) {
            int level = getLevelForEnchantment(enchantment);
            if (level < 1) {
                return;
            }
            putEnchantment(enchantment, level);
        }
        this.status = Component.translatable(messageKey("editor_enchantments_added"), enchantments.size());
    }

    private int getLevelForEnchantment(Enchantment enchantment) {
        int level;
        try {
            level = Integer.parseInt(this.enchantLevelBox == null ? this.enchantLevelValue : this.enchantLevelBox.getValue());
        } catch (NumberFormatException exception) {
            this.status = Component.translatable(messageKey("editor_invalid_enchantment_level"), MAX_ENCHANTMENT_LEVEL);
            return -1;
        }

        if (level < 1 || level > MAX_ENCHANTMENT_LEVEL) {
            this.status = Component.translatable(messageKey("editor_invalid_enchantment_level"), MAX_ENCHANTMENT_LEVEL);
            return -1;
        }
        this.enchantLevelValue = Integer.toString(level);
        return enchantment.getMaxLevel() == 1 ? 1 : level;
    }

    private int getDisplayLevel(Enchantment enchantment) {
        if (enchantment.getMaxLevel() == 1) {
            return 1;
        }
        try {
            int level = Integer.parseInt(this.enchantLevelValue);
            return Math.max(1, Math.min(MAX_ENCHANTMENT_LEVEL, level));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private void putEnchantment(Enchantment enchantment, int level) {
        ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        if (id == null) {
            return;
        }
        ListTag enchantments = getOrCreateEnchantmentsTag();
        int index = findEnchantmentIndex(enchantments, id);
        CompoundTag enchantmentTag = EnchantmentHelper.storeEnchantment(id, level);
        if (index >= 0) {
            enchantments.set(index, enchantmentTag);
        } else {
            enchantments.add(enchantmentTag);
        }
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    private boolean removeEnchantmentAtIndex(int index) {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null) {
            return false;
        }

        String key = getEnchantmentTagKey(this.previewStack);
        ListTag enchantments = tag.getList(key, Tag.TAG_COMPOUND);
        if (index < 0 || index >= enchantments.size()) {
            return false;
        }

        enchantments.remove(index);
        if (enchantments.isEmpty()) {
            this.previewStack.removeTagKey(key);
        }
        this.rawNbtValue = getInitialNbt(this.previewStack);
        return true;
    }

    private ListTag getOrCreateEnchantmentsTag() {
        String key = getEnchantmentTagKey(this.previewStack);
        CompoundTag tag = this.previewStack.getOrCreateTag();
        if (!tag.contains(key, Tag.TAG_LIST)) {
            tag.put(key, new ListTag());
        }
        return tag.getList(key, Tag.TAG_COMPOUND);
    }

    private List<Enchantment> getFilteredEnchantments(ItemStack stack) {
        List<Enchantment> enchantments = new ArrayList<>();
        String filter = this.enchantFilterValue == null ? "" : this.enchantFilterValue.trim().toLowerCase(Locale.ROOT);
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS.getValues()) {
            if (!this.showAllEnchantments && !canApplyEnchantment(stack, enchantment)) {
                continue;
            }
            ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
            String name = Component.translatable(enchantment.getDescriptionId()).getString().toLowerCase(Locale.ROOT);
            String idString = id == null ? "" : id.toString().toLowerCase(Locale.ROOT);
            if (filter.isEmpty() || name.contains(filter) || idString.contains(filter)) {
                enchantments.add(enchantment);
            }
        }
        enchantments.sort(Comparator.comparing(enchantment -> Component.translatable(enchantment.getDescriptionId()).getString(),
                String.CASE_INSENSITIVE_ORDER));
        return enchantments;
    }

    private boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
        return enchantment.canEnchant(stack)
                || stack.is(Items.BOOK)
                || (stack.is(Items.ENCHANTED_BOOK) && enchantment.isAllowedOnBooks());
    }

    private List<EnchantmentEntry> getStoredEnchantments(ItemStack stack) {
        List<EnchantmentEntry> entries = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return entries;
        }

        ListTag enchantments = tag.getList(getEnchantmentTagKey(stack), Tag.TAG_COMPOUND);
        for (int i = 0; i < enchantments.size(); i++) {
            CompoundTag enchantmentTag = enchantments.getCompound(i);
            ResourceLocation id = ResourceLocation.tryParse(enchantmentTag.getString("id"));
            Enchantment enchantment = id == null ? null : ForgeRegistries.ENCHANTMENTS.getValue(id);
            entries.add(new EnchantmentEntry(id, enchantment, enchantmentTag.getInt("lvl")));
        }
        return entries;
    }

    private boolean tryRemoveActivePotionEffect(double mouseX, double mouseY) {
        List<MobEffectInstance> effects = getCustomPotionEffects();
        if (effects.isEmpty()) {
            return false;
        }

        int startY = this.midY - effects.size() * 5;
        int listWidth = this.font.width("Unbreaking 32767");
        for (MobEffectInstance effect : effects) {
            listWidth = Math.max(listWidth, this.font.width(formatPotionEffect(effect)));
        }

        if (mouseX < 0 || mouseX > 5 + listWidth || mouseY < startY || mouseY >= startY + effects.size() * 10) {
            return false;
        }

        int index = (int) ((mouseY - startY) / 10);
        removeCustomPotionEffectAt(index);
        return true;
    }

    private boolean tryAddRingPotionEffect(double mouseX, double mouseY) {
        List<MobEffect> filteredEffects = getFilteredPotionEffects();
        if (filteredEffects.isEmpty() || Math.abs(this.mouseDist - getRingRadius()) >= RING_HOVER_WIDTH) {
            return false;
        }

        double angle = (2.0D * Math.PI) / filteredEffects.size();
        int lowDist = Integer.MAX_VALUE;
        MobEffect closestEffect = null;
        for (int i = 0; i < filteredEffects.size(); i++) {
            double effectAngle = this.rotOff / 60.0D + angle * i;
            int x = (int) (this.midX + getRingRadius() * Math.cos(effectAngle));
            int y = (int) (this.midY + getRingRadius() * Math.sin(effectAngle));
            int distX = x - (int) mouseX;
            int distY = y - (int) mouseY;
            int dist = (int) Math.sqrt(distX * distX + distY * distY);
            if (dist < RING_ICON_HIT_RADIUS && dist < lowDist) {
                lowDist = dist;
                closestEffect = filteredEffects.get(i);
            }
        }

        if (closestEffect == null) {
            return false;
        }

        addPotionEffect(closestEffect);
        return true;
    }

    private void addMatchingPotionEffects() {
        List<MobEffect> effects = getFilteredPotionEffects();
        if (effects.isEmpty()) {
            this.status = Component.translatable(messageKey("editor_no_potion_match"));
            return;
        }
        for (MobEffect effect : effects) {
            if (!addPotionEffect(effect)) {
                return;
            }
        }
    }

    private boolean addPotionEffect(MobEffect effect) {
        int level = parsePotionLevel();
        int seconds = parsePotionSeconds();
        if (level < 1 || seconds < 1) {
            return false;
        }

        MobEffectInstance instance = new MobEffectInstance(effect, seconds * 20, level - 1, false, this.showPotionParticles);
        List<MobEffectInstance> effects = new ArrayList<>(getCustomPotionEffects());
        effects.removeIf(existing -> existing.getEffect() == effect);
        effects.add(instance);
        PotionUtils.setCustomEffects(this.previewStack, effects);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_potion_added"), effect.getDisplayName(), level);
        return true;
    }

    private void removeCustomPotionEffectAt(int index) {
        List<MobEffectInstance> effects = new ArrayList<>(getCustomPotionEffects());
        if (index < 0 || index >= effects.size()) {
            return;
        }
        MobEffectInstance removed = effects.remove(index);
        if (effects.isEmpty()) {
            CompoundTag tag = this.previewStack.getTag();
            if (tag != null) {
                tag.remove(CUSTOM_POTION_EFFECTS_TAG);
            }
            cleanupEmptyTag();
        } else {
            PotionUtils.setCustomEffects(this.previewStack, effects);
        }
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_potion_removed"), removed.getEffect().getDisplayName());
    }

    private List<MobEffectInstance> getCustomPotionEffects() {
        return new ArrayList<>(PotionUtils.getCustomEffects(this.previewStack));
    }

    private List<MobEffect> getFilteredPotionEffects() {
        List<MobEffect> effects = new ArrayList<>();
        String filter = this.potionFilterValue == null ? "" : this.potionFilterValue.trim().toLowerCase(Locale.ROOT);
        for (MobEffect effect : ForgeRegistries.MOB_EFFECTS.getValues()) {
            ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            String name = effect.getDisplayName().getString().toLowerCase(Locale.ROOT);
            String idString = id == null ? "" : id.toString().toLowerCase(Locale.ROOT);
            if (filter.isEmpty() || name.contains(filter) || idString.contains(filter)) {
                effects.add(effect);
            }
        }
        effects.sort(Comparator.comparing(effect -> effect.getDisplayName().getString(), String.CASE_INSENSITIVE_ORDER));
        return effects;
    }

    private int parsePotionLevel() {
        try {
            int level = Integer.parseInt(this.potionLevelBox == null ? this.potionLevelValue : this.potionLevelBox.getValue());
            if (level < 1 || level > MAX_POTION_LEVEL) {
                throw new NumberFormatException();
            }
            this.potionLevelValue = Integer.toString(level);
            return level;
        } catch (NumberFormatException exception) {
            this.status = Component.translatable(messageKey("editor_invalid_potion_level"), MAX_POTION_LEVEL);
            return -1;
        }
    }

    private int parsePotionSeconds() {
        try {
            int seconds = Integer.parseInt(this.potionTimeBox == null ? this.potionTimeValue : this.potionTimeBox.getValue());
            if (seconds < 1 || seconds > MAX_POTION_SECONDS) {
                throw new NumberFormatException();
            }
            this.potionTimeValue = Integer.toString(seconds);
            return seconds;
        } catch (NumberFormatException exception) {
            this.status = Component.translatable(messageKey("editor_invalid_potion_time"), MAX_POTION_SECONDS);
            return -1;
        }
    }

    private void togglePotionParticles() {
        this.showPotionParticles = !this.showPotionParticles;
        rebuildWidgets();
    }

    private String formatPotionEffect(MobEffectInstance effect) {
        int amplifier = effect.getAmplifier();
        String text = effect.getEffect().getDisplayName().getString() + " (" + (amplifier + 1) + ")";
        if (amplifier > 1) {
            text += " " + Component.translatable("potion.potency." + amplifier).getString().trim();
        }
        text += effect.isVisible() ? " P:S" : " P:H";
        return text;
    }

    private String formatPotionRingName(MobEffect effect) {
        int level = 1;
        try {
            level = Math.max(1, Integer.parseInt(this.potionLevelValue));
        } catch (NumberFormatException ignored) {
        }
        String text = effect.getDisplayName().getString();
        if (level > 1) {
            text += " " + Component.translatable("potion.potency." + (level - 1)).getString().trim();
        }
        return text;
    }

    private void toggleAttributeInfinity() {
        this.attributeInfinity = !this.attributeInfinity;
        rebuildWidgets();
    }

    private void cycleAttributeOperation() {
        this.attributeOperation = (this.attributeOperation + 1) % 3;
        rebuildWidgets();
    }

    private void cycleAttributeSlot() {
        this.attributeSlot = (this.attributeSlot + 1) % 7;
        rebuildWidgets();
    }

    private boolean tryRemoveActiveAttributeModifier(double mouseX, double mouseY) {
        List<AttributeEntry> entries = getAttributeModifierEntries();
        if (entries.isEmpty()) {
            return false;
        }

        int startY = this.midY - entries.size() * 5;
        int listWidth = this.font.width("Unbreaking 32767");
        for (AttributeEntry entry : entries) {
            listWidth = Math.max(listWidth, this.font.width(formatAttributeEntry(entry)));
        }

        if (mouseX < 0 || mouseX > 5 + listWidth || mouseY < startY || mouseY >= startY + entries.size() * 10) {
            return false;
        }

        int index = (int) ((mouseY - startY) / 10);
        AttributeEntry entry = entries.get(index);
        removeAttributeModifierAt(entry.tagIndex());
        this.status = Component.translatable(messageKey("editor_attribute_removed"), getAttributeDisplayName(entry));
        return true;
    }

    private boolean tryAddRingAttribute(double mouseX, double mouseY) {
        List<Attribute> attributes = getSharedAttributes();
        if (attributes.isEmpty() || Math.abs(this.mouseDist - getRingRadius()) >= RING_HOVER_WIDTH) {
            return false;
        }

        double angle = (2.0D * Math.PI) / attributes.size();
        int lowDist = Integer.MAX_VALUE;
        Attribute closestAttribute = null;
        for (int i = 0; i < attributes.size(); i++) {
            double attributeAngle = this.rotOff / 60.0D + angle * i;
            int x = (int) (this.midX + getRingRadius() * Math.cos(attributeAngle));
            int y = (int) (this.midY + getRingRadius() * Math.sin(attributeAngle));
            int distX = x - (int) mouseX;
            int distY = y - (int) mouseY;
            int dist = (int) Math.sqrt(distX * distX + distY * distY);
            if (dist < RING_ICON_HIT_RADIUS && dist < lowDist) {
                lowDist = dist;
                closestAttribute = attributes.get(i);
            }
        }

        if (closestAttribute == null) {
            return false;
        }

        addAttributeModifier(closestAttribute);
        return true;
    }

    private void addAttributeModifier(Attribute attribute) {
        Double amount = getAttributeAmount();
        if (amount == null) {
            return;
        }

        ResourceLocation id = ForgeRegistries.ATTRIBUTES.getKey(attribute);
        if (id == null) {
            return;
        }

        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), id.toString(), amount, getAttributeOperation());
        CompoundTag modifierTag = modifier.save();
        modifierTag.putString("AttributeName", id.toString());
        String slotName = getAttributeSlotName(this.attributeSlot);
        if (slotName != null) {
            modifierTag.putString("Slot", slotName);
        }

        getOrCreateAttributeModifiersTag().add(modifierTag);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_attribute_added"), Component.translatable(attribute.getDescriptionId()));
    }

    private Double getAttributeAmount() {
        if (this.attributeInfinity) {
            return this.attributeNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }

        try {
            int whole = parseBoundedAttributeNumber(this.attributeAmountBox == null ? this.attributeAmountValue : this.attributeAmountBox.getValue(), MAX_ATTRIBUTE_INTEGER);
            int decimal = parseBoundedAttributeNumber(this.attributeDecimalBox == null ? this.attributeDecimalValue : this.attributeDecimalBox.getValue(), 999);
            this.attributeAmountValue = Integer.toString(whole);
            this.attributeDecimalValue = Integer.toString(decimal);
            double amount = whole + decimal / 1000.0D;
            return this.attributeNegative ? -amount : amount;
        } catch (NumberFormatException exception) {
            this.status = Component.translatable(messageKey("editor_invalid_attribute_value"));
            return null;
        }
    }

    private int parseBoundedAttributeNumber(String value, int max) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        int parsed = Integer.parseInt(value);
        if (parsed < 0 || parsed > max) {
            throw new NumberFormatException();
        }
        return parsed;
    }

    private AttributeModifier.Operation getAttributeOperation() {
        if (this.attributeInfinity) {
            return AttributeModifier.Operation.ADDITION;
        }
        return switch (this.attributeOperation) {
            case 1 -> AttributeModifier.Operation.MULTIPLY_BASE;
            case 2 -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> AttributeModifier.Operation.ADDITION;
        };
    }

    private ListTag getOrCreateAttributeModifiersTag() {
        CompoundTag tag = this.previewStack.getOrCreateTag();
        if (!tag.contains(ATTRIBUTE_MODIFIERS_TAG, Tag.TAG_LIST)) {
            tag.put(ATTRIBUTE_MODIFIERS_TAG, new ListTag());
        }
        return tag.getList(ATTRIBUTE_MODIFIERS_TAG, Tag.TAG_COMPOUND);
    }

    private List<AttributeEntry> getAttributeModifierEntries() {
        List<AttributeEntry> entries = new ArrayList<>();
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null || !tag.contains(ATTRIBUTE_MODIFIERS_TAG, Tag.TAG_LIST)) {
            return entries;
        }

        ListTag modifiers = tag.getList(ATTRIBUTE_MODIFIERS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < modifiers.size(); i++) {
            CompoundTag modifierTag = modifiers.getCompound(i);
            String attributeName = modifierTag.getString("AttributeName");
            Attribute attribute = getAttributeByName(attributeName);
            double amount = modifierTag.contains("Amount", Tag.TAG_DOUBLE) ? modifierTag.getDouble("Amount") : 0.0D;
            int operation = Mth.positiveModulo(modifierTag.getInt("Operation"), 3);
            String slotName = modifierTag.contains("Slot", Tag.TAG_STRING) ? modifierTag.getString("Slot") : "any";
            entries.add(new AttributeEntry(i, attributeName, attribute, amount, operation, slotName));
        }
        return entries;
    }

    private void removeAttributeModifierAt(int tagIndex) {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null || !tag.contains(ATTRIBUTE_MODIFIERS_TAG, Tag.TAG_LIST)) {
            return;
        }

        ListTag modifiers = tag.getList(ATTRIBUTE_MODIFIERS_TAG, Tag.TAG_COMPOUND);
        if (tagIndex < 0 || tagIndex >= modifiers.size()) {
            return;
        }

        modifiers.remove(tagIndex);
        if (modifiers.isEmpty()) {
            tag.remove(ATTRIBUTE_MODIFIERS_TAG);
            cleanupEmptyTag();
        }
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    private String formatAttributeEntry(AttributeEntry entry) {
        String[] prefixes = {entry.amount() < 0.0D ? "" : "+", "*", "**"};
        return getAttributeDisplayName(entry).getString() + " " + prefixes[entry.operation()] + entry.amount() + " (" + entry.slotName() + ")";
    }

    private Component getAttributeDisplayName(AttributeEntry entry) {
        if (entry.attribute() != null) {
            return Component.translatable(entry.attribute().getDescriptionId());
        }
        if (!entry.attributeName().isBlank()) {
            return Component.literal(entry.attributeName());
        }
        return Component.literal("Unknown Attribute");
    }

    private Attribute getAttributeByName(String name) {
        ResourceLocation id = ResourceLocation.tryParse(name);
        return id == null ? null : ForgeRegistries.ATTRIBUTES.getValue(id);
    }

    private List<Attribute> getSharedAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(Attributes.MAX_HEALTH);
        attributes.add(Attributes.FOLLOW_RANGE);
        attributes.add(Attributes.KNOCKBACK_RESISTANCE);
        attributes.add(Attributes.MOVEMENT_SPEED);
        attributes.add(Attributes.ATTACK_DAMAGE);
        attributes.add(Attributes.ATTACK_SPEED);
        attributes.add(Attributes.ARMOR);
        attributes.add(Attributes.ARMOR_TOUGHNESS);
        attributes.add(Attributes.LUCK);
        attributes.add(ForgeMod.BLOCK_REACH.get());
        attributes.add(Attributes.FLYING_SPEED);
        return attributes;
    }

    private void applyColorFromHex(boolean updateStatus) {
        String text = this.colorHexBox == null ? this.colorHexValue : this.colorHexBox.getValue();
        String normalized = text == null ? "" : text.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }

        if (normalized.length() != 6) {
            if (updateStatus && !normalized.isBlank()) {
                this.status = Component.translatable(messageKey("editor_invalid_color"));
            }
            return;
        }

        try {
            setEditorColor(Integer.parseInt(normalized, 16));
            syncColorControlsFromStack();
            if (updateStatus) {
                this.status = Component.translatable(messageKey("editor_color_updated"), this.colorHexValue);
            }
        } catch (NumberFormatException exception) {
            this.status = Component.translatable(messageKey("editor_invalid_color"));
        }
    }

    private void setColorComponent(int shift, int value) {
        int color = getEditorColor();
        color &= ~(0xFF << shift);
        color |= (Mth.clamp(value, 0, 255) << shift);
        setEditorColor(color);
        syncColorControlsFromStack();
    }

    private void syncColorControlsFromStack() {
        int color = getEditorColor();
        this.colorHexValue = formatColorHex(color);
        this.syncingColorControls = true;
        if (this.colorHexBox != null) {
            this.colorHexBox.setValue(this.colorHexValue);
        }
        if (this.redSlider != null) {
            this.redSlider.setIntValue(getRed(color));
        }
        if (this.greenSlider != null) {
            this.greenSlider.setIntValue(getGreen(color));
        }
        if (this.blueSlider != null) {
            this.blueSlider.setIntValue(getBlue(color));
        }
        this.syncingColorControls = false;
    }

    private int getEditorColor() {
        if (isPotionItem(this.previewStack)) {
            return PotionUtils.getColor(this.previewStack) & 0xFFFFFF;
        }
        if (isMapItem(this.previewStack)) {
            CompoundTag display = this.previewStack.getTagElement(DISPLAY_TAG);
            return display == null ? 0 : display.getInt(MAP_COLOR_TAG) & 0xFFFFFF;
        }
        if (this.previewStack.getItem() instanceof DyeableLeatherItem dyeableLeatherItem) {
            return dyeableLeatherItem.getColor(this.previewStack) & 0xFFFFFF;
        }
        return 0;
    }

    private void setEditorColor(int color) {
        color &= 0xFFFFFF;
        if (isPotionItem(this.previewStack)) {
            this.previewStack.getOrCreateTag().putInt(CUSTOM_POTION_COLOR_TAG, color);
        } else if (isMapItem(this.previewStack)) {
            CompoundTag tag = this.previewStack.getOrCreateTag();
            CompoundTag display = tag.getCompound(DISPLAY_TAG);
            display.putInt(MAP_COLOR_TAG, color);
            tag.put(DISPLAY_TAG, display);
        } else if (this.previewStack.getItem() instanceof DyeableLeatherItem dyeableLeatherItem) {
            dyeableLeatherItem.setColor(this.previewStack, color);
        }
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    private void addDyeToColor(DyeColor dyeColor) {
        if (!(this.previewStack.getItem() instanceof DyeableLeatherItem dyeableLeatherItem)) {
            return;
        }

        int[] totals = new int[3];
        int totalBrightness = 0;
        int colors = 0;
        if (dyeableLeatherItem.hasCustomColor(this.previewStack)) {
            int current = dyeableLeatherItem.getColor(this.previewStack);
            int red = getRed(current);
            int green = getGreen(current);
            int blue = getBlue(current);
            totalBrightness += Math.max(red, Math.max(green, blue));
            totals[0] += red;
            totals[1] += green;
            totals[2] += blue;
            colors++;
        }

        float[] dyeRgb = dyeColor.getTextureDiffuseColors();
        int red = (int) (dyeRgb[0] * 255.0F);
        int green = (int) (dyeRgb[1] * 255.0F);
        int blue = (int) (dyeRgb[2] * 255.0F);
        totalBrightness += Math.max(red, Math.max(green, blue));
        totals[0] += red;
        totals[1] += green;
        totals[2] += blue;
        colors++;

        int mixedRed = totals[0] / colors;
        int mixedGreen = totals[1] / colors;
        int mixedBlue = totals[2] / colors;
        float averageBrightness = (float) totalBrightness / (float) colors;
        float maxMixed = Math.max(mixedRed, Math.max(mixedGreen, mixedBlue));
        if (maxMixed > 0.0F) {
            mixedRed = (int) (mixedRed * averageBrightness / maxMixed);
            mixedGreen = (int) (mixedGreen * averageBrightness / maxMixed);
            mixedBlue = (int) (mixedBlue * averageBrightness / maxMixed);
        }
        setEditorColor((mixedRed << 16) | (mixedGreen << 8) | mixedBlue);
    }

    private void renderDyeGrid(GuiGraphics guiGraphics) {
        if (!shouldShowDyeGrid() || this.blueSlider == null) {
            return;
        }

        int gridX = this.blueSlider.getX();
        int gridY = this.blueSlider.getY() + this.blueSlider.getHeight() + 10;
        int index = 0;
        for (DyeColor dyeColor : DyeColor.values()) {
            int x = gridX + 20 * (index % 8);
            int y = gridY + 20 * (index / 8);
            guiGraphics.fill(x, y, x + 20, y + 20, argb(159, dyeColor.getTextColor()));
            DyeItem dyeItem = DyeItem.byColor(dyeColor);
            if (dyeItem != null) {
                guiGraphics.renderItem(new ItemStack(dyeItem), x + 2, y + 2);
            }
            index++;
        }
    }

    private boolean shouldShowDyeGrid() {
        return this.previewStack.getItem() instanceof DyeableLeatherItem;
    }

    private void ensureLorePainterRows() {
        while (this.lorePainterRows.size() < this.lorePainterHeight) {
            List<LorePixel> row = new ArrayList<>();
            for (int i = 0; i < this.lorePainterWidth; i++) {
                row.add(this.currentLorePixel.copy());
            }
            this.lorePainterRows.add(row);
        }
        while (this.lorePainterRows.size() > this.lorePainterHeight) {
            this.lorePainterRows.remove(this.lorePainterRows.size() - 1);
        }
        for (List<LorePixel> row : this.lorePainterRows) {
            while (row.size() < this.lorePainterWidth) {
                row.add(this.currentLorePixel.copy());
            }
            while (row.size() > this.lorePainterWidth) {
                row.remove(row.size() - 1);
            }
        }
    }

    private void addLorePainterRow() {
        List<LorePixel> row = new ArrayList<>();
        for (int i = 0; i < this.lorePainterWidth; i++) {
            row.add(this.currentLorePixel.copy());
        }
        this.lorePainterRows.add(row);
        this.lorePainterHeight++;
    }

    private void removeLorePainterRow() {
        if (this.lorePainterHeight <= 1) {
            return;
        }
        this.lorePainterRows.remove(this.lorePainterRows.size() - 1);
        this.lorePainterHeight--;
    }

    private void addLorePainterColumn() {
        for (List<LorePixel> row : this.lorePainterRows) {
            row.add(this.currentLorePixel.copy());
        }
        this.lorePainterWidth++;
    }

    private void removeLorePainterColumn() {
        if (this.lorePainterWidth <= 1) {
            return;
        }
        for (List<LorePixel> row : this.lorePainterRows) {
            row.remove(row.size() - 1);
        }
        this.lorePainterWidth--;
    }

    private void insertLorePainterRows() {
        ensureLorePainterRows();
        for (List<LorePixel> row : this.lorePainterRows) {
            this.loreValues.add(buildLorePainterRow(row));
        }
        applyLoreToStack();
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_lore_painted"));
    }

    private void cycleGuiScale() {
        if (this.minecraft == null) {
            return;
        }
        int current = this.minecraft.options.guiScale().get();
        int next = current >= 4 ? 0 : current + 1;
        this.minecraft.options.guiScale().set(next);
        this.minecraft.resizeDisplay();
        rebuildWidgets();
    }

    private void paintLorePainterAt(double mouseX, double mouseY) {
        ensureLorePainterRows();
        int gridX = getLorePainterGridX();
        int gridY = getLorePainterGridY();
        if (isMouseIn(mouseX, mouseY, gridX, gridY, getLorePainterSizeX(), getLorePainterSizeY())) {
            int x = Mth.clamp(((int) mouseX - gridX) / 9, 0, this.lorePainterWidth - 1);
            int y = Mth.clamp(((int) mouseY - gridY) / 9, 0, this.lorePainterHeight - 1);
            this.lorePainterRows.get(y).set(x, this.currentLorePixel.copy());
            return;
        }

        if (isMouseIn(mouseX, mouseY, 0, 0, LoreSymbol.values().length * 9, 9)) {
            int index = Mth.clamp((int) mouseX / 9, 0, LoreSymbol.values().length - 1);
            this.currentLorePixel.symbol = LoreSymbol.values()[index];
            return;
        }

        int colorX = getLorePainterColorX();
        if (isMouseIn(mouseX, mouseY, colorX, 0, DyeColor.values().length * 9, 9)) {
            int index = Mth.clamp(((int) mouseX - colorX) / 9, 0, DyeColor.values().length - 1);
            this.currentLorePixel.color = DyeColor.values()[index];
        }
    }

    private String buildLorePainterRow(List<LorePixel> row) {
        StringBuilder builder = new StringBuilder();
        for (LorePixel pixel : row) {
            builder.append(pixel);
        }
        return builder.toString();
    }

    private String buildLorePainterSymbols() {
        StringBuilder builder = new StringBuilder();
        for (LoreSymbol symbol : LoreSymbol.values()) {
            if (symbol == LoreSymbol.FULL_SPACE) {
                builder.append(ChatFormatting.ITALIC).append(ChatFormatting.BOLD).append("E");
            } else {
                builder.append(new LorePixel(this.currentLorePixel.color, symbol));
            }
        }
        return builder.toString();
    }

    private String buildLorePainterColors() {
        StringBuilder builder = new StringBuilder();
        for (DyeColor color : DyeColor.values()) {
            builder.append(new LorePixel(color, this.currentLorePixel.symbol));
        }
        return builder.toString();
    }

    private int getLorePainterColorX() {
        return this.width - this.font.width(buildLorePainterColors());
    }

    private int getLorePainterGridX() {
        return this.midX - getLorePainterSizeX() / 2;
    }

    private int getLorePainterGridY() {
        return this.midY - getLorePainterSizeY() / 2;
    }

    private int getLorePainterSizeX() {
        return 9 * this.lorePainterWidth;
    }

    private int getLorePainterSizeY() {
        return 9 * this.lorePainterHeight;
    }

    private void updateMouseDistance(int mouseX, int mouseY) {
        int distX = this.midX - mouseX;
        int distY = this.midY - mouseY;
        this.mouseDist = (int) Math.sqrt(distX * distX + distY * distY);
    }

    private int getRingRadius() {
        return this.height / 3;
    }

    private boolean isMouseOverCenter(double mouseX, double mouseY) {
        return mouseX > this.midX - CENTER_HIT_RADIUS
                && mouseX < this.midX + CENTER_HIT_RADIUS
                && mouseY > this.midY - CENTER_HIT_RADIUS
                && mouseY < this.midY + CENTER_HIT_RADIUS;
    }

    private void insertFormattingPrefix() {
        insertFocusedText(String.valueOf(ChatFormatting.PREFIX_CODE));
    }

    private void stripFocusedFormatting() {
        EditBox focused = getFocusedTextBox();
        if (focused == null) {
            return;
        }
        focused.setValue(Objects.requireNonNullElse(ChatFormatting.stripFormatting(focused.getValue()), ""));
    }

    private void insertFocusedText(String text) {
        EditBox focused = getFocusedTextBox();
        if (focused != null) {
            focused.insertText(text);
        }
    }

    private EditBox getFocusedTextBox() {
        for (EditBox box : this.mainTextBoxes) {
            if (box.isFocused()) {
                return box;
            }
        }
        for (EditBox box : this.loreBoxes) {
            if (box.isFocused()) {
                return box;
            }
        }
        if (this.rawNbtBox != null && this.rawNbtBox.isFocused()) {
            return this.rawNbtBox;
        }
        return null;
    }

    private List<Component> getPrettyNbtLines() {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null || tag.isEmpty()) {
            return List.of(Component.literal(ChatFormatting.DARK_PURPLE + "{}"));
        }
        List<Component> lines = new ArrayList<>();
        appendPrettyTagLines(lines, tag, 0, "");
        return lines;
    }

    private void appendPrettyTagLines(List<Component> lines, Tag tag, int depth, String name) {
        String indent = "  ".repeat(depth);
        if (tag instanceof CompoundTag compoundTag) {
            if (!name.isEmpty()) {
                lines.add(Component.literal(ChatFormatting.DARK_PURPLE + indent + name + ": {"));
            } else {
                lines.add(Component.literal(ChatFormatting.DARK_PURPLE + indent + "{"));
            }
            for (String key : compoundTag.getAllKeys()) {
                appendPrettyTagLines(lines, compoundTag.get(key), depth + 1, key);
            }
            lines.add(Component.literal(ChatFormatting.DARK_PURPLE + indent + "}"));
        } else if (tag instanceof ListTag listTag) {
            lines.add(Component.literal(ChatFormatting.DARK_PURPLE + indent + name + ": ["));
            for (int i = 0; i < listTag.size(); i++) {
                appendPrettyTagLines(lines, listTag.get(i), depth + 1, Integer.toString(i));
            }
            lines.add(Component.literal(ChatFormatting.DARK_PURPLE + indent + "]"));
        } else {
            lines.add(Component.literal(ChatFormatting.DARK_PURPLE + indent + name + ": " + tag));
        }
    }

    private List<NbtRow> buildNbtRows() {
        List<NbtRow> rows = new ArrayList<>();
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null || tag.isEmpty()) {
            rows.add(new NbtRow("tag", "tag: {}", false, 0));
            return rows;
        }
        addNbtRows(rows, "tag", "tag", tag, 0);
        return rows;
    }

    private void addNbtRows(List<NbtRow> rows, String path, String name, Tag tag, int depth) {
        boolean expandable = tag instanceof CompoundTag || tag instanceof ListTag;
        String prefix = expandable ? (this.expandedNbtPaths.contains(path) ? "- " : "+ ") : "  ";
        rows.add(new NbtRow(path, prefix + name + ": " + summarizeTag(tag), expandable, depth));
        if (!expandable || !this.expandedNbtPaths.contains(path)) {
            return;
        }

        if (tag instanceof CompoundTag compoundTag) {
            for (String key : compoundTag.getAllKeys()) {
                addNbtRows(rows, path + "." + key, key, compoundTag.get(key), depth + 1);
            }
        } else if (tag instanceof ListTag listTag) {
            for (int i = 0; i < listTag.size(); i++) {
                addNbtRows(rows, path + "[" + i + "]", "[" + i + "]", listTag.get(i), depth + 1);
            }
        }
    }

    private String summarizeTag(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            return "{" + compoundTag.getAllKeys().size() + "}";
        }
        if (tag instanceof ListTag listTag) {
            return "[" + listTag.size() + "]";
        }
        String value = tag.toString();
        return value.length() > 120 ? value.substring(0, 117) + "..." : value;
    }

    private int getNbtAdvancedVisibleRows() {
        return Math.max(1, (this.height - 75) / 12);
    }

    private void readMainFieldsFromStack(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        this.itemIdValue = id == null ? "air" : stripMinecraftNamespace(id);
        this.countValue = Integer.toString(Math.max(1, Math.min(MAX_COUNT, stack.getCount())));
        this.damageValue = Integer.toString(Math.max(0, Math.min(getDamageMaxForField(stack), stack.getDamageValue())));
        this.nameValue = stack.getHoverName().getString();
        this.loreValues.clear();

        CompoundTag display = stack.getTagElement(DISPLAY_TAG);
        if (display != null && display.contains(LORE_TAG, Tag.TAG_LIST)) {
            ListTag lore = display.getList(LORE_TAG, Tag.TAG_STRING);
            for (int i = 0; i < lore.size(); i++) {
                this.loreValues.add(readLoreLine(lore.getString(i)));
            }
        }
    }

    private String readLoreLine(String raw) {
        try {
            Component component = Component.Serializer.fromJson(raw);
            return component == null ? raw : component.getString();
        } catch (RuntimeException exception) {
            return raw;
        }
    }

    private void updateLoreScrollFromMouse(double mouseY) {
        int spaces = loreLineSpaces();
        int max = Math.max(0, this.loreValues.size() - spaces);
        if (max == 0) {
            setLoreScroll(0);
            return;
        }
        float perc = (float) ((mouseY - 50.0D) / (this.height - 99.0D));
        setLoreScroll(Mth.clamp(Math.round(max * perc), 0, max));
    }

    private void setLoreScroll(int value) {
        int clamped = Mth.clamp(value, 0, Math.max(0, this.loreValues.size() - loreLineSpaces()));
        if (this.loreScroll != clamped) {
            this.loreScroll = clamped;
            rebuildWidgets();
        }
    }

    private void captureFieldValues() {
        if (this.itemIdBox != null) {
            this.itemIdValue = this.itemIdBox.getValue();
        }
        if (this.countBox != null) {
            this.countValue = this.countBox.getValue();
        }
        if (this.damageBox != null) {
            this.damageValue = this.damageBox.getValue();
        }
        if (this.nameBox != null) {
            this.nameValue = this.nameBox.getValue();
        }
        if (this.rawNbtBox != null) {
            this.rawNbtValue = this.rawNbtBox.getValue();
        }
        if (this.enchantFilterBox != null) {
            this.enchantFilterValue = this.enchantFilterBox.getValue();
        }
        if (this.enchantLevelBox != null) {
            this.enchantLevelValue = this.enchantLevelBox.getValue();
        }
        if (this.potionFilterBox != null) {
            this.potionFilterValue = this.potionFilterBox.getValue();
        }
        if (this.potionLevelBox != null) {
            this.potionLevelValue = this.potionLevelBox.getValue();
        }
        if (this.potionTimeBox != null) {
            this.potionTimeValue = this.potionTimeBox.getValue();
        }
        if (this.attributeAmountBox != null) {
            this.attributeAmountValue = this.attributeAmountBox.getValue();
        }
        if (this.attributeDecimalBox != null) {
            this.attributeDecimalValue = this.attributeDecimalBox.getValue();
        }
        if (this.colorHexBox != null) {
            this.colorHexValue = this.colorHexBox.getValue();
        }
    }

    private CompoundTag parseNbt(String nbt) throws CommandSyntaxException {
        String value = nbt == null ? "" : nbt.trim();
        if (value.isEmpty() || "{}".equals(value)) {
            return null;
        }
        return TagParser.parseTag(value);
    }

    private void cleanupEmptyDisplayTag() {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null) {
            return;
        }
        CompoundTag display = tag.getCompound(DISPLAY_TAG);
        if (display.isEmpty()) {
            tag.remove(DISPLAY_TAG);
        }
        cleanupEmptyTag();
    }

    private void cleanupEmptyTag() {
        CompoundTag tag = this.previewStack.getTag();
        if (tag != null && tag.isEmpty()) {
            this.previewStack.setTag(null);
        }
    }

    private int loreLineSpaces() {
        return Math.max(1, ((this.height - 70) / 30) - 1);
    }

    private boolean isMouseIn(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static int getDamageMaxForField(ItemStack stack) {
        return stack.isDamageableItem() ? Math.max(0, stack.getMaxDamage()) : 99999;
    }

    private static int parsePositiveOrZero(String value) {
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static String normalizeItemId(String id) {
        String value = id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
        return value.contains(":") ? value : "minecraft:" + value;
    }

    private static String stripMinecraftNamespace(ResourceLocation id) {
        return "minecraft".equals(id.getNamespace()) ? id.getPath() : id.toString();
    }

    private static String getInitialNbt(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || tag.isEmpty() ? "{}" : tag.toString();
    }

    private static boolean isColorApplicable(ItemStack stack) {
        return stack.getItem() instanceof DyeableLeatherItem || isPotionItem(stack) || isMapItem(stack);
    }

    private static boolean canShowEnchantingButton(ItemStack stack) {
        return stack.isEnchantable() || stack.is(Items.ENCHANTED_BOOK) || stack.is(Items.BOOK);
    }

    private static boolean isPotionItem(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION) || stack.is(Items.TIPPED_ARROW);
    }

    private static boolean isMapItem(ItemStack stack) {
        return stack.is(Items.MAP) || stack.is(Items.FILLED_MAP);
    }

    private static int getDefaultAttributeSlot(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armorItem) {
            return getAttributeSlotNumber(armorItem.getEquipmentSlot());
        }
        if (stack.is(Items.SHIELD) || stack.is(Items.TOTEM_OF_UNDYING)) {
            return 2;
        }
        return 1;
    }

    private static int getAttributeSlotNumber(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> 1;
            case OFFHAND -> 2;
            case HEAD -> 3;
            case CHEST -> 4;
            case LEGS -> 5;
            case FEET -> 6;
        };
    }

    private static String getAttributeSlotName(int slot) {
        return switch (slot) {
            case 1 -> EquipmentSlot.MAINHAND.getName();
            case 2 -> EquipmentSlot.OFFHAND.getName();
            case 3 -> EquipmentSlot.HEAD.getName();
            case 4 -> EquipmentSlot.CHEST.getName();
            case 5 -> EquipmentSlot.LEGS.getName();
            case 6 -> EquipmentSlot.FEET.getName();
            default -> null;
        };
    }

    private static String formatColorHex(int color) {
        return String.format(Locale.ROOT, "#%06x", color & 0xFFFFFF);
    }

    private static int getRed(int color) {
        return color >> 16 & 255;
    }

    private static int getGreen(int color) {
        return color >> 8 & 255;
    }

    private static int getBlue(int color) {
        return color & 255;
    }

    private static int argb(int alpha, int rgb) {
        return ((alpha & 255) << 24) | (rgb & 0xFFFFFF);
    }

    private String formatRingEnchantmentName(Enchantment enchantment) {
        return enchantment.getFullname(getDisplayLevel(enchantment)).getString();
    }

    private String formatStoredEnchantment(EnchantmentEntry entry) {
        if (entry.enchantment() == null) {
            return "Unknown ID (" + entry.id() + ") " + entry.level();
        }
        return entry.enchantment().getFullname(entry.level()).getString();
    }

    private static int findEnchantmentIndex(ListTag enchantments, ResourceLocation id) {
        for (int i = 0; i < enchantments.size(); i++) {
            ResourceLocation storedId = ResourceLocation.tryParse(enchantments.getCompound(i).getString("id"));
            if (id.equals(storedId)) {
                return i;
            }
        }
        return -1;
    }

    private static String getEnchantmentTagKey(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) ? BOOK_ENCHANTMENTS_TAG : ITEM_ENCHANTMENTS_TAG;
    }

    private static String key(String suffix) {
        return "screen." + ModSource.MODID + "." + suffix;
    }

    private static String messageKey(String suffix) {
        return "message." + ModSource.MODID + "." + suffix;
    }

    private enum Panel {
        ITEM,
        NBT,
        NBT_ADVANCED,
        HIDE_FLAGS,
        ENCHANTMENTS,
        POTION,
        ATTRIBUTES,
        COLOR,
        LORE,
        LORE_PAINTER
    }

    private enum HideFlag {
        ENCHANTMENTS(1, "flag.enchantment"),
        ATTRIBUTE_MODIFIERS(2, "flag.attributemod"),
        UNBREAKABLE(4, "flag.unbreakable"),
        CAN_DESTROY(8, "flag.candestroy"),
        CAN_PLACE_ON(16, "flag.canplaceon"),
        ITEM_INFO(32, "flag.iteminfo");

        private final int mask;
        private final String translationKey;

        HideFlag(int mask, String translationKey) {
            this.mask = mask;
            this.translationKey = translationKey;
        }

        public int mask() {
            return this.mask;
        }

        public String translationKey() {
            return this.translationKey;
        }
    }

    private record EnchantmentEntry(ResourceLocation id, Enchantment enchantment, int level) {
    }

    private record AttributeEntry(int tagIndex, String attributeName, Attribute attribute, double amount, int operation, String slotName) {
    }

    private record NbtRow(String path, String displayText, boolean isExpandable, int depth) {
    }

    private static class LegacyTextEditBox extends EditBox {
        private int legacyMaxLength = 32;

        private LegacyTextEditBox(Font font, int x, int y, int width, int height, Component message) {
            super(font, x, y, width, height, message);
        }

        @Override
        public void setMaxLength(int maxLength) {
            this.legacyMaxLength = maxLength;
            super.setMaxLength(maxLength);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            if (!this.active || !canConsumeInput()) {
                return false;
            }
            if (isLegacyAllowedCharacter(codePoint)) {
                insertText(Character.toString(codePoint));
                return true;
            }
            return false;
        }

        @Override
        public void insertText(String text) {
            if (!this.active) {
                return;
            }

            String filtered = filterLegacyText(text);
            String value = getValue();
            int[] range = getSelectionRange();
            int room = this.legacyMaxLength - value.length() + (range[1] - range[0]);
            if (room < filtered.length()) {
                filtered = filtered.substring(0, Math.max(0, room));
            }

            String next = value.substring(0, range[0]) + filtered + value.substring(range[1]);
            super.setValue(next);
            int cursor = range[0] + filtered.length();
            setCursorPosition(cursor);
            setHighlightPos(cursor);
        }

        private int[] getSelectionRange() {
            String highlighted = getHighlighted();
            int cursor = getCursorPosition();
            if (highlighted.isEmpty()) {
                return new int[]{cursor, cursor};
            }

            String value = getValue();
            int length = highlighted.length();
            int start;
            int end;
            if (cursor >= length && value.substring(cursor - length, cursor).equals(highlighted)) {
                start = cursor - length;
                end = cursor;
            } else {
                start = cursor;
                end = Math.min(value.length(), cursor + length);
            }
            return new int[]{Math.min(start, end), Math.max(start, end)};
        }

        private static String filterLegacyText(String input) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < input.length(); i++) {
                char character = input.charAt(i);
                if (isLegacyAllowedCharacter(character)) {
                    builder.append(character);
                }
            }
            return builder.toString();
        }

        private static boolean isLegacyAllowedCharacter(char character) {
            return SharedConstants.isAllowedChatCharacter(character) || character == ChatFormatting.PREFIX_CODE;
        }
    }

    private static class FixedDigitEditBox extends EditBox {
        private static final int DISABLED_COLOR = 0xFF707070;

        private final Font font;
        private final int digits;
        private final int minValue;
        private final int maxValue;
        private int digitCursor;
        private int cursorFrame;

        private FixedDigitEditBox(Font font, int x, int y, int width, int height, int digits, int minValue, int maxValue) {
            super(font, x, y, width, height, Component.empty());
            this.font = font;
            this.digits = Math.max(1, digits);
            this.minValue = minValue;
            this.maxValue = maxValue;
            super.setMaxLength(this.digits + (minValue < 0 ? 1 : 0));
            super.setTextColor(MAIN_COLOR);
            super.setTextColorUneditable(DISABLED_COLOR);
            setFixedValue(minValue);
        }

        @Override
        public void tick() {
            this.cursorFrame++;
        }

        @Override
        public void setValue(String value) {
            setFixedValue(parseFixedValue(value));
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!this.active || !canConsumeInput()) {
                return false;
            }

            if (Screen.isCopy(keyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(getValue());
                return true;
            }
            if (Screen.isPaste(keyCode)) {
                return true;
            }
            if (Screen.isCut(keyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(getValue());
                setFixedValue(0);
                return true;
            }

            return switch (keyCode) {
                case 259, 261 -> {
                    replaceDigit('0');
                    yield true;
                }
                case 262 -> {
                    moveDigitCursor(1);
                    yield true;
                }
                case 263 -> {
                    moveDigitCursor(-1);
                    yield true;
                }
                case 268 -> {
                    setDigitCursorPosition(0);
                    yield true;
                }
                case 269 -> {
                    setDigitCursorPosition(this.digits - 1);
                    yield true;
                }
                default -> false;
            };
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            if (!this.active || !canConsumeInput()) {
                return false;
            }

            if (codePoint == '-' && this.minValue < 0) {
                setFixedValue(-Math.abs(parseFixedValue(getValue())));
                return true;
            }
            if (codePoint == '+' && this.maxValue >= 0) {
                setFixedValue(Math.abs(parseFixedValue(getValue())));
                return true;
            }
            if (codePoint >= '0' && codePoint <= '9') {
                replaceDigit(codePoint);
                moveDigitCursor(1);
                return true;
            }
            return true;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            int localX = Mth.floor(mouseX) - getX() - 4;
            String value = getValue();
            int cursor = this.font.plainSubstrByWidth(value, Math.max(0, localX)).length();
            if (value.startsWith("-")) {
                cursor--;
            }
            setDigitCursorPosition(cursor);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (!isVisible()) {
                return;
            }

            int color = this.active ? MAIN_COLOR : DISABLED_COLOR;
            guiGraphics.fill(getX() - 1, getY() - 1, getX() + getWidth() + 1, getY() + getHeight() + 1, color);
            guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), ALT_COLOR);

            String value = getValue();
            int textX = getX() + 4;
            int textY = getY() + (getHeight() - 8) / 2;
            int cursorStringPosition = getCursorStringPosition();
            int cursorX = textX;

            if (!value.isEmpty()) {
                String beforeCursor = value.substring(0, Math.min(cursorStringPosition, value.length()));
                if (!beforeCursor.isEmpty()) {
                    cursorX = guiGraphics.drawString(this.font, beforeCursor, textX, textY, color, true) - 1;
                }

                if (cursorStringPosition < value.length()) {
                    guiGraphics.drawString(this.font, value.substring(cursorStringPosition), cursorX, textY, color, true);
                }
            }

            if (isFocused() && this.cursorFrame / 6 % 2 == 0) {
                guiGraphics.drawString(this.font, "_", cursorX, textY, CONTRAST_COLOR, true);
            }
        }

        private void replaceDigit(char digit) {
            char[] characters = getDigitCharacters();
            characters[this.digitCursor] = digit;
            int value = parseFixedValue(new String(characters));
            if (getValue().startsWith("-")) {
                value = -value;
            }
            int cursor = this.digitCursor;
            setFixedValue(value);
            setDigitCursorPosition(cursor);
        }

        private void moveDigitCursor(int amount) {
            setDigitCursorPosition(this.digitCursor + amount);
        }

        private void setDigitCursorPosition(int position) {
            this.digitCursor = Mth.clamp(position, 0, this.digits - 1);
            syncSuperCursor();
        }

        private void setFixedValue(int value) {
            int clamped = Mth.clamp(value, this.minValue, this.maxValue);
            super.setValue(formatFixedValue(clamped));
            this.digitCursor = Mth.clamp(this.digitCursor, 0, this.digits - 1);
            syncSuperCursor();
        }

        private void syncSuperCursor() {
            int cursor = getCursorStringPosition();
            super.setCursorPosition(cursor);
            super.setHighlightPos(cursor);
        }

        private int getCursorStringPosition() {
            return getValue().startsWith("-") ? this.digitCursor + 1 : this.digitCursor;
        }

        private char[] getDigitCharacters() {
            String value = getValue();
            String rawDigits = value.startsWith("-") ? value.substring(1) : value;
            if (rawDigits.length() < this.digits) {
                rawDigits = "0".repeat(this.digits - rawDigits.length()) + rawDigits;
            } else if (rawDigits.length() > this.digits) {
                rawDigits = rawDigits.substring(rawDigits.length() - this.digits);
            }
            return rawDigits.toCharArray();
        }

        private String formatFixedValue(int value) {
            boolean negative = value < 0;
            String rawDigits = Integer.toString(Math.abs(value));
            if (rawDigits.length() < this.digits) {
                rawDigits = "0".repeat(this.digits - rawDigits.length()) + rawDigits;
            }
            return negative ? "-" + rawDigits : rawDigits;
        }

        private int parseFixedValue(String value) {
            if (value == null || value.isBlank()) {
                return 0;
            }

            boolean negative = value.startsWith("-");
            long parsed = 0L;
            for (int i = negative ? 1 : 0; i < value.length(); i++) {
                char character = value.charAt(i);
                if (character >= '0' && character <= '9') {
                    parsed = parsed * 10L + character - '0';
                    if (parsed > Integer.MAX_VALUE) {
                        return negative ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                    }
                }
            }
            return (int) (negative ? -parsed : parsed);
        }
    }

    private static class ColorSlider extends AbstractSliderButton {
        private final Component label;
        private final IntConsumer responder;

        private ColorSlider(int x, int y, int width, int height, Component label, int value, IntConsumer responder) {
            super(x, y, width, height, Component.empty(), Mth.clamp(value, 0, 255) / 255.0D);
            this.label = label;
            this.responder = responder;
            updateMessage();
        }

        private int getIntValue() {
            return Mth.clamp((int) Math.round(this.value * 255.0D), 0, 255);
        }

        private void setIntValue(int value) {
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

    private static class LorePixel {
        private DyeColor color;
        private LoreSymbol symbol;

        private LorePixel() {
            this(DyeColor.WHITE, LoreSymbol.FULL_BLOCK);
        }

        private LorePixel(DyeColor color, LoreSymbol symbol) {
            this.color = color;
            this.symbol = symbol;
        }

        private LorePixel copy() {
            return new LorePixel(this.color, this.symbol);
        }

        private String format() {
            ChatFormatting formatting = ChatFormatting.getById(15 - this.color.getId());
            return (formatting == null ? ChatFormatting.WHITE : formatting).toString();
        }

        @Override
        public String toString() {
            if (this.symbol.whitespace()) {
                return this.symbol.symbol();
            }
            return format() + this.symbol.symbol();
        }
    }

    private enum LoreSymbol {
        FULL_BLOCK("fullblock", "\u2588", false),
        MEDIUM_SHADE("mediumshade", "\u2592", false),
        DARK_SHADE("darkshade", "\u2593", false),
        FULL_SPACE("fullspace", ChatFormatting.BOLD + " " + ChatFormatting.RESET + " ", true);

        private final String nameKey;
        private final String symbol;
        private final boolean whitespace;

        LoreSymbol(String nameKey, String symbol, boolean whitespace) {
            this.nameKey = nameKey;
            this.symbol = symbol;
            this.whitespace = whitespace;
        }

        private String nameKey() {
            return this.nameKey;
        }

        private String symbol() {
            return this.symbol;
        }

        private boolean whitespace() {
            return this.whitespace;
        }
    }
}

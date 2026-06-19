package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Axis;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.CreativeTabRefresher;
import io.github.seraphina.infinity_item_editor_re.data.realms.RealmController;
import io.github.seraphina.infinity_item_editor_re.util.GiveHelper;
import io.github.seraphina.infinity_item_editor_re.util.PlayerInventorySlots;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
    private static final int SIGN_LINES = 4;
    private static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    private static final String SIGN_FRONT_TEXT_TAG = "front_text";
    private static final String SIGN_MESSAGES_TAG = "messages";
    private static final String SIGN_FILTERED_MESSAGES_TAG = "filtered_messages";
    private static final String SIGN_COLOR_TAG = "color";
    private static final String SIGN_GLOWING_TEXT_TAG = "has_glowing_text";
    private static final String LEGACY_SIGN_TEXT_TAG_PREFIX = "Text";
    private static final String BANNER_PATTERNS_TAG = "Patterns";
    private static final String BANNER_PATTERN_TAG = "Pattern";
    private static final String BANNER_COLOR_TAG = "Color";
    private static final String BANNER_BASE_TAG = "Base";
    private static final int BANNER_PATTERN_ROWS = 8;
    private static final String BOOK_TITLE_TAG = "title";
    private static final String BOOK_AUTHOR_TAG = "author";
    private static final String BOOK_GENERATION_TAG = "generation";
    private static final String BOOK_RESOLVED_TAG = "resolved";
    private static final String BOOK_PAGES_TAG = "pages";
    private static final int MAX_BOOK_GENERATION = 3;
    private static final String SKULL_OWNER_TAG = "SkullOwner";
    private static final String SKULL_OWNER_ID_TAG = "Id";
    private static final String SKULL_OWNER_NAME_TAG = "Name";
    private static final String SKULL_PROPERTIES_TAG = "Properties";
    private static final String SKULL_TEXTURES_TAG = "textures";
    private static final String SKULL_TEXTURE_VALUE_TAG = "Value";
    private static final String SKULL_TEXTURE_SIGNATURE_TAG = "Signature";
    private static final String ENTITY_TAG = "EntityTag";
    private static final String ENTITY_ID_TAG = "id";
    private static final String ENTITY_CUSTOM_NAME_TAG = "CustomName";
    private static final String ARMOR_STAND_SHOW_ARMS_TAG = "ShowArms";
    private static final String ARMOR_STAND_SMALL_TAG = "Small";
    private static final String ARMOR_STAND_INVISIBLE_TAG = "Invisible";
    private static final String ARMOR_STAND_NO_BASE_PLATE_TAG = "NoBasePlate";
    private static final String ARMOR_STAND_MARKER_TAG = "Marker";
    private static final String ARMOR_STAND_NO_GRAVITY_TAG = "NoGravity";
    private static final String ARMOR_STAND_INVULNERABLE_TAG = "Invulnerable";
    private static final String FIREWORKS_TAG = "Fireworks";
    private static final String FIREWORK_FLIGHT_TAG = "Flight";
    private static final String FIREWORK_EXPLOSIONS_TAG = "Explosions";
    private static final String FIREWORK_EXPLOSION_TAG = "Explosion";
    private static final String FIREWORK_TYPE_TAG = "Type";
    private static final String FIREWORK_COLORS_TAG = "Colors";
    private static final String FIREWORK_FADE_COLORS_TAG = "FadeColors";
    private static final String FIREWORK_FLICKER_TAG = "Flicker";
    private static final String FIREWORK_TRAIL_TAG = "Trail";
    private static final int MAX_FIREWORK_FLIGHT = 4;
    private static final int FIREWORK_EXPLOSION_TYPES = 5;
    private static final String CONTAINER_ITEMS_TAG = "Items";
    private static final String CONTAINER_SLOT_TAG = "Slot";
    private static final int CONTAINER_ROWS = 3;
    private static final int CONTAINER_COLUMNS = 9;
    private static final int CONTAINER_SIZE = CONTAINER_ROWS * CONTAINER_COLUMNS;
    private static final int CONTAINER_SLOT_PIXEL_SIZE = 18;
    private static final int SPAWN_EGG_ENTITY_ROWS = 8;
    private static final int SPAWN_EGG_TAG_ROWS = 8;
    private static final int SPAWN_EGG_TAG_ROW_HEIGHT = 24;

    private final ItemStack originalStack;
    private final int targetContainerSlot;
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
    private final String[] signLineValues = new String[SIGN_LINES];
    private String signCommandValue = "";
    private String bookTitleValue = "";
    private String bookAuthorValue = "";
    private String headOwnerValue = "";
    private String headUuidValue = "";
    private String headTextureValue = "";
    private String headTextureSignatureValue = "";
    private String containerSlotNbtValue = "{}";
    private String bannerPatternFilterValue = "";
    private String spawnEggEntityFilterValue = "";
    private String spawnEggCustomNameValue = "";
    private String nbtFeedback = "";
    private boolean nbtFeedbackGood;
    private boolean showAllEnchantments;
    private boolean showPotionParticles = true;
    private boolean attributeInfinity;
    private boolean attributeNegative;
    private boolean syncingColorControls;
    private boolean lorePainterDragging;
    private boolean lorePainterPreview;
    private CompoundTag rememberedSignedBookData;
    private int rotOff;
    private int mouseDist;
    private int midX;
    private int midY;
    private int advancedScroll;
    private int loreScroll;
    private int attributeSlot = 1;
    private int attributeOperation;
    private int bannerBaseColor;
    private int bannerPatternColor = DyeColor.BLACK.getId();
    private int bannerPatternScroll;
    private int selectedBannerPatternIndex;
    private int fireworkExplosionType;
    private int fireworkColor = DyeColor.RED.getId();
    private int fireworkFadeColor = -1;
    private int selectedContainerSlot;
    private int spawnEggEntityScroll;
    private int spawnEggTagScroll;
    private int selectedSpawnEggEntityIndex;
    private int lorePainterWidth = 3;
    private int lorePainterHeight = 3;
    private boolean draggingLoreScroll;
    private boolean fireworkFlicker;
    private boolean fireworkTrail;

    private final List<String> loreValues = new ArrayList<>();
    private final List<List<LorePixel>> lorePainterRows = new ArrayList<>();
    private final List<EditBox> tickingBoxes = new ArrayList<>();
    private final List<EditBox> mainTextBoxes = new ArrayList<>();
    private final List<EditBox> loreBoxes = new ArrayList<>();
    private final List<EditBox> signBoxes = new ArrayList<>();
    private final List<InfinityEditorButton> loreActionButtons = new ArrayList<>();
    private final Map<String, String> spawnEggNumberValueOverrides = new HashMap<>();
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
    private EditBox signCommandBox;
    private EditBox bookTitleBox;
    private EditBox bookAuthorBox;
    private EditBox headOwnerBox;
    private EditBox headUuidBox;
    private EditBox headTextureBox;
    private EditBox headTextureSignatureBox;
    private EditBox containerSlotNbtBox;
    private EditBox bannerPatternFilterBox;
    private EditBox spawnEggEntityFilterBox;
    private EditBox spawnEggCustomNameBox;
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
        this(stack, -1);
    }

    public ItemEditorScreen(ItemStack stack, int targetContainerSlot) {
        super(Component.translatable(key("item")));
        this.originalStack = stack.copy();
        this.targetContainerSlot = targetContainerSlot;
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
        this.signBoxes.clear();
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
        this.signCommandBox = null;
        this.bookTitleBox = null;
        this.bookAuthorBox = null;
        this.headOwnerBox = null;
        this.headUuidBox = null;
        this.headTextureBox = null;
        this.headTextureSignatureBox = null;
        this.containerSlotNbtBox = null;
        this.bannerPatternFilterBox = null;
        this.spawnEggEntityFilterBox = null;
        this.spawnEggCustomNameBox = null;
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
            case SIGN -> addSignPanel();
            case HEAD -> addHeadPanel();
            case ARMOR_STAND -> addArmorStandPanel();
            case FIREWORK -> addFireworkPanel();
            case CONTAINER -> addContainerPanel();
            case BANNER -> addBannerPanel();
            case SPAWN_EGG -> addSpawnEggPanel();
            case BOOK -> addBookPanel();
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
            case SIGN -> renderSignPanel(guiGraphics);
            case HEAD -> renderHeadPanel(guiGraphics);
            case ARMOR_STAND -> renderArmorStandPanel(guiGraphics);
            case FIREWORK -> renderFireworkPanel(guiGraphics);
            case CONTAINER -> renderContainerPanel(guiGraphics);
            case BANNER -> renderBannerPanel(guiGraphics);
            case SPAWN_EGG -> renderSpawnEggPanel(guiGraphics);
            case BOOK -> renderBookPanel(guiGraphics);
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
            if (this.activePanel == Panel.CONTAINER) {
                updateContainerSlotFromNbt();
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
        if (this.activePanel == Panel.BANNER && this.bannerPatternFilterBox != null && this.bannerPatternFilterBox.isFocused()) {
            return this.bannerPatternFilterBox.charTyped(Character.toLowerCase(codePoint), modifiers);
        }
        if (this.activePanel == Panel.SPAWN_EGG && this.spawnEggEntityFilterBox != null && this.spawnEggEntityFilterBox.isFocused()) {
            return this.spawnEggEntityFilterBox.charTyped(Character.toLowerCase(codePoint), modifiers);
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
            case CONTAINER -> handleContainerClick(mouseX, mouseY);
            case BANNER -> handleBannerClick(mouseX, mouseY);
            case SPAWN_EGG -> handleSpawnEggClick(mouseX, mouseY);
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

        if (this.activePanel == Panel.BANNER) {
            setBannerPatternScroll(this.bannerPatternScroll - (int) Math.signum(delta));
            return true;
        }

        if (this.activePanel == Panel.SPAWN_EGG) {
            if (isMouseIn(mouseX, mouseY, 10, getSpawnEggEntityRowY(0) - 1, 170, SPAWN_EGG_ENTITY_ROWS * 10 + 2)) {
                setSpawnEggEntityScroll(this.spawnEggEntityScroll - (int) Math.signum(delta));
                rebuildWidgets();
            } else {
                setSpawnEggTagScroll(this.spawnEggTagScroll - (int) Math.signum(delta));
                rebuildWidgets();
            }
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
        if (!this.previewStack.isEmpty()) {
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

        if (isSignItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("sign")), button -> switchPanel(Panel.SIGN)));
            y += 30;
        }

        if (isPlayerHeadItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("head")), button -> switchPanel(Panel.HEAD)));
            y += 30;
        }

        if (isArmorStandItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("armorstand")), button -> switchPanel(Panel.ARMOR_STAND)));
            y += 30;
        }

        if (isFireworkEditableItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("firework")), button -> switchPanel(Panel.FIREWORK)));
            y += 30;
        }

        if (isContainerEditableItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("container")), button -> openContainerItemEditor()));
            y += 30;
        }

        if (isBannerEditableItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("banner")), button -> switchPanel(Panel.BANNER)));
            y += 30;
        }

        if (isSpawnEggItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("spawnegg")), button -> switchPanel(Panel.SPAWN_EGG)));
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
            y += 30;
        }

        if (isBookEditableItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("book")), button -> switchPanel(Panel.BOOK)));
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

    private void addSignPanel() {
        int fieldWidth = Math.max(120, Math.min(220, this.width - 160));
        int x = this.midX - fieldWidth / 2;

        for (int i = 0; i < SIGN_LINES; i++) {
            int line = i;
            EditBox lineBox = addTrackedBox(legacyTextBox(x, 60 + 30 * i, fieldWidth, FIELD_HEIGHT,
                    Component.translatable(key("sign.line"), line + 1)));
            lineBox.setMaxLength(384);
            lineBox.setValue(getSignLineValue(line));
            lineBox.setResponder(value -> {
                this.signLineValues[line] = value;
                applySignToStack();
            });
            this.signBoxes.add(lineBox);
        }

        int commandWidth = Math.max(120, Math.min(260, this.width - 120));
        this.signCommandBox = addTrackedBox(legacyTextBox(this.midX - commandWidth / 2, 190, commandWidth, FIELD_HEIGHT,
                Component.translatable(key("sign.command"))));
        this.signCommandBox.setMaxLength(512);
        this.signCommandBox.setValue(this.signCommandValue == null ? "" : this.signCommandValue);
        this.signCommandBox.setResponder(value -> {
            this.signCommandValue = value;
            applySignToStack();
        });
        this.signBoxes.add(this.signCommandBox);

        addFormatButtons();
    }

    private void addBookPanel() {
        boolean written = this.previewStack.is(Items.WRITTEN_BOOK);
        int fieldWidth = Math.max(120, Math.min(220, this.width - 160));
        int x = this.midX - fieldWidth / 2;

        this.bookTitleBox = addTrackedBox(legacyTextBox(x, 70, fieldWidth, FIELD_HEIGHT,
                Component.translatable(key("book.title"))));
        this.bookTitleBox.setMaxLength(100);
        this.bookTitleBox.setValue(this.bookTitleValue == null ? "" : this.bookTitleValue);
        this.bookTitleBox.setResponder(value -> {
            this.bookTitleValue = value;
            applyBookMetadataToStack();
        });
        this.mainTextBoxes.add(this.bookTitleBox);

        this.bookAuthorBox = addTrackedBox(legacyTextBox(x, 100, fieldWidth, FIELD_HEIGHT,
                Component.translatable(key("book.author"))));
        this.bookAuthorBox.setMaxLength(100);
        this.bookAuthorBox.setValue(this.bookAuthorValue == null ? "" : this.bookAuthorValue);
        this.bookAuthorBox.setResponder(value -> {
            this.bookAuthorValue = value;
            applyBookMetadataToStack();
        });
        this.mainTextBoxes.add(this.bookAuthorBox);

        InfinityEditorButton generation = addRenderableWidget(new InfinityEditorButton(this.midX - 75, 140, 150, FIELD_HEIGHT,
                Component.translatable(key("book.generation"), getBookGeneration()), button -> cycleBookGeneration()));
        generation.active = written;

        InfinityEditorButton resolved = addRenderableWidget(new InfinityEditorButton(this.midX - 75, 170, 150, FIELD_HEIGHT,
                getBookResolvedText(), button -> toggleBookResolved()));
        resolved.active = written;

        addRenderableWidget(new InfinityEditorButton(this.midX - 75, 200, 150, FIELD_HEIGHT,
                getBookSignButtonText(), button -> toggleBookSignedState()));

        addFormatButtons();
    }

    private void addHeadPanel() {
        int fieldWidth = Math.max(180, Math.min(320, this.width - 160));
        int x = this.midX - fieldWidth / 2;

        this.headOwnerBox = addTrackedBox(legacyTextBox(x, 64, fieldWidth, FIELD_HEIGHT,
                Component.translatable(key("head.owner"))));
        this.headOwnerBox.setMaxLength(64);
        this.headOwnerBox.setValue(this.headOwnerValue == null ? "" : this.headOwnerValue);
        this.headOwnerBox.setResponder(value -> {
            this.headOwnerValue = value;
            applyHeadToStack();
        });
        this.mainTextBoxes.add(this.headOwnerBox);

        this.headUuidBox = addTrackedBox(legacyTextBox(x, 94, fieldWidth, FIELD_HEIGHT,
                Component.translatable(key("head.uuid"))));
        this.headUuidBox.setMaxLength(36);
        this.headUuidBox.setValue(this.headUuidValue == null ? "" : this.headUuidValue);
        this.headUuidBox.setResponder(value -> {
            this.headUuidValue = value;
            applyHeadToStack();
        });
        this.mainTextBoxes.add(this.headUuidBox);

        this.headTextureBox = addTrackedBox(legacyTextBox(x, 124, fieldWidth, FIELD_HEIGHT,
                Component.translatable(key("head.texture"))));
        this.headTextureBox.setMaxLength(4096);
        this.headTextureBox.setValue(this.headTextureValue == null ? "" : this.headTextureValue);
        this.headTextureBox.setResponder(value -> {
            this.headTextureValue = value;
            applyHeadToStack();
        });
        this.mainTextBoxes.add(this.headTextureBox);

        this.headTextureSignatureBox = addTrackedBox(legacyTextBox(x, 154, fieldWidth, FIELD_HEIGHT,
                Component.translatable(key("head.signature"))));
        this.headTextureSignatureBox.setMaxLength(4096);
        this.headTextureSignatureBox.setValue(this.headTextureSignatureValue == null ? "" : this.headTextureSignatureValue);
        this.headTextureSignatureBox.setResponder(value -> {
            this.headTextureSignatureValue = value;
            applyHeadToStack();
        });
        this.mainTextBoxes.add(this.headTextureSignatureBox);

        addRenderableWidget(new InfinityEditorButton(this.midX - 105, 190, 100, FIELD_HEIGHT,
                Component.translatable(key("head.random_uuid")), button -> randomizeHeadUuid()));
        addRenderableWidget(new InfinityEditorButton(this.midX + 5, 190, 100, FIELD_HEIGHT,
                Component.translatable(key("head.clear_owner")), button -> clearHeadOwner()));
    }

    private void addArmorStandPanel() {
        int x = this.midX - 75;
        int y = 54;
        addArmorStandToggleButton(x, y, "show_arms", ARMOR_STAND_SHOW_ARMS_TAG);
        addArmorStandToggleButton(x, y + 26, "small", ARMOR_STAND_SMALL_TAG);
        addArmorStandToggleButton(x, y + 52, "invisible", ARMOR_STAND_INVISIBLE_TAG);
        addArmorStandToggleButton(x, y + 78, "no_base_plate", ARMOR_STAND_NO_BASE_PLATE_TAG);
        addArmorStandToggleButton(x, y + 104, "marker", ARMOR_STAND_MARKER_TAG);
        addArmorStandToggleButton(x, y + 130, "no_gravity", ARMOR_STAND_NO_GRAVITY_TAG);
        addArmorStandToggleButton(x, y + 156, "invulnerable", ARMOR_STAND_INVULNERABLE_TAG);
        addRenderableWidget(new InfinityEditorButton(x, y + 190, 150, FIELD_HEIGHT,
                Component.translatable(key("armorstand.clear_entity_tag")), button -> clearArmorStandEntityTag()));
    }

    private void addArmorStandToggleButton(int x, int y, String translationSuffix, String tagKey) {
        addRenderableWidget(new InfinityEditorButton(x, y, 150, FIELD_HEIGHT,
                getArmorStandToggleText(translationSuffix, tagKey), button -> toggleArmorStandFlag(tagKey, translationSuffix)));
    }

    private void addFireworkPanel() {
        int x = this.midX - 78;
        int y = 52;
        int width = 156;

        if (this.previewStack.is(Items.FIREWORK_ROCKET)) {
            addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                    Component.translatable(key("firework.flight"), getFireworkFlight()), button -> cycleFireworkFlight()));
            y += 26;
        }

        addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                Component.translatable(key("firework.type"), getFireworkTypeName(this.fireworkExplosionType)),
                button -> cycleFireworkExplosionType(Screen.hasShiftDown() ? -1 : 1)));
        y += 26;
        addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                Component.translatable(key("firework.flicker." + (this.fireworkFlicker ? 1 : 0))),
                button -> toggleFireworkFlicker()));
        y += 26;
        addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                Component.translatable(key("firework.trail." + (this.fireworkTrail ? 1 : 0))),
                button -> toggleFireworkTrail()));
        y += 26;
        addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                Component.translatable(key("firework.color"), getDyeColorName(getFireworkDyeColor(this.fireworkColor))),
                button -> cycleFireworkColor(false, Screen.hasShiftDown() ? -1 : 1)));
        y += 26;
        addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                Component.translatable(key("firework.fade_color"), getFireworkFadeColorText()),
                button -> cycleFireworkColor(true, Screen.hasShiftDown() ? -1 : 1)));
        y += 26;
        addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                Component.translatable(key("firework.random_colors")), button -> randomizeFireworkColors()));
        y += 26;

        if (this.previewStack.is(Items.FIREWORK_ROCKET)) {
            addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                    Component.translatable(key("firework.add_explosion")), button -> addFireworkExplosion()));
            y += 26;
            InfinityEditorButton remove = addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                    Component.translatable(key("firework.remove_explosion")), button -> removeLastFireworkExplosion()));
            remove.active = getFireworkExplosionCount() > 0;
            y += 26;
            InfinityEditorButton clear = addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                    Component.translatable(key("firework.clear_fireworks")), button -> clearFireworkData()));
            clear.active = hasFireworkData();
        } else {
            InfinityEditorButton clear = addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                    Component.translatable(key("firework.clear_explosion")), button -> clearFireworkData()));
            clear.active = hasFireworkData();
        }
    }

    private void addContainerPanel() {
        this.selectedContainerSlot = Mth.clamp(this.selectedContainerSlot, 0, CONTAINER_SIZE - 1);
        int boxWidth = Math.min(300, Math.max(180, this.width - 40));
        this.containerSlotNbtBox = addTrackedBox(legacyTextBox(this.midX - boxWidth / 2, 132, boxWidth, FIELD_HEIGHT,
                Component.translatable(key("container.slot_nbt"))));
        this.containerSlotNbtBox.setMaxLength(20000);
        this.containerSlotNbtBox.setValue(this.containerSlotNbtValue == null ? getContainerSelectedSlotNbt() : this.containerSlotNbtValue);
        this.containerSlotNbtBox.setResponder(value -> this.containerSlotNbtValue = value);

        int controlsWidth = 270;
        int x = this.midX - controlsWidth / 2;
        addRenderableWidget(new InfinityEditorButton(x, 158, 24, FIELD_HEIGHT,
                Component.literal("<"), button -> cycleContainerSlot(-1)));
        addRenderableWidget(new InfinityEditorButton(x + 28, 158, 24, FIELD_HEIGHT,
                Component.literal(">"), button -> cycleContainerSlot(1)));
        addRenderableWidget(new InfinityEditorButton(x + 58, 158, 84, FIELD_HEIGHT,
                Component.translatable(key("container.update_slot")), button -> updateContainerSlotFromNbt()));
        addRenderableWidget(new InfinityEditorButton(x + 146, 158, 58, FIELD_HEIGHT,
                Component.translatable(key("container.clear_slot")), button -> clearContainerSlot()));
        InfinityEditorButton clearAll = addRenderableWidget(new InfinityEditorButton(x + 208, 158, 62, FIELD_HEIGHT,
                Component.translatable(key("container.clear_all")), button -> clearContainerItems()));
        clearAll.active = getContainerItemCount() > 0;
    }

    private void addBannerPanel() {
        this.bannerPatternFilterBox = addTrackedBox(legacyTextBox(10, 28, 125, FIELD_HEIGHT,
                Component.translatable(key("banner.search"))));
        this.bannerPatternFilterBox.setMaxLength(32);
        this.bannerPatternFilterBox.setValue(this.bannerPatternFilterValue);
        this.bannerPatternFilterBox.setResponder(value -> {
            this.bannerPatternFilterValue = value.toLowerCase(Locale.ROOT);
            this.bannerPatternScroll = 0;
            this.selectedBannerPatternIndex = 0;
        });

        int controlsX = Math.max(this.midX + 76, this.width - 142);
        int width = 132;
        addRenderableWidget(new InfinityEditorButton(controlsX, 52, width, FIELD_HEIGHT,
                Component.translatable(key("banner.base"), getDyeColorName(getBannerBaseColor())),
                button -> cycleBannerBaseColor(Screen.hasShiftDown() ? -1 : 1)));
        addRenderableWidget(new InfinityEditorButton(controlsX, 78, width, FIELD_HEIGHT,
                Component.translatable(key("banner.pattern_color"), getDyeColorName(getBannerPatternColor())),
                button -> cycleBannerPatternColor(Screen.hasShiftDown() ? -1 : 1)));
        addRenderableWidget(new InfinityEditorButton(controlsX, 104, width, FIELD_HEIGHT,
                Component.translatable(key("banner.swap")), button -> swapBannerAndShield()));
        addRenderableWidget(new InfinityEditorButton(controlsX, 130, width, FIELD_HEIGHT,
                Component.translatable(key("banner.add")), button -> addSelectedBannerPattern()));

        InfinityEditorButton remove = addRenderableWidget(new InfinityEditorButton(controlsX, 156, width, FIELD_HEIGHT,
                Component.translatable(key("banner.remove")), button -> removeLastBannerPattern()));
        remove.active = getBannerPatternCount() > 0;

        InfinityEditorButton clear = addRenderableWidget(new InfinityEditorButton(controlsX, 182, width, FIELD_HEIGHT,
                Component.translatable(key("banner.clear")), button -> clearBannerPatterns()));
        clear.active = getBannerPatternCount() > 0;

        addRenderableWidget(new InfinityEditorButton(this.midX - 58, this.height - 64, 28, FIELD_HEIGHT,
                Component.literal("<"), button -> cycleSelectedBannerPattern(-1)));
        addRenderableWidget(new InfinityEditorButton(this.midX - 28, this.height - 64, 56, FIELD_HEIGHT,
                Component.translatable(key("banner.add")), button -> addSelectedBannerPattern()));
        addRenderableWidget(new InfinityEditorButton(this.midX + 30, this.height - 64, 28, FIELD_HEIGHT,
                Component.literal(">"), button -> cycleSelectedBannerPattern(1)));
    }

    private void addSpawnEggPanel() {
        this.spawnEggEntityFilterBox = addTrackedBox(legacyTextBox(10, 28, 145, FIELD_HEIGHT,
                Component.translatable(key("spawnegg.search"))));
        this.spawnEggEntityFilterBox.setMaxLength(48);
        this.spawnEggEntityFilterBox.setValue(this.spawnEggEntityFilterValue);
        this.spawnEggEntityFilterBox.setResponder(value -> {
            this.spawnEggEntityFilterValue = value.toLowerCase(Locale.ROOT);
            this.spawnEggEntityScroll = 0;
            this.selectedSpawnEggEntityIndex = 0;
            this.spawnEggTagScroll = 0;
            rebuildWidgets();
            if (this.spawnEggEntityFilterBox != null) {
                this.setFocused(this.spawnEggEntityFilterBox);
                this.spawnEggEntityFilterBox.setFocused(true);
                this.spawnEggEntityFilterBox.setCursorPosition(this.spawnEggEntityFilterBox.getValue().length());
            }
        });

        int controlsX = getSpawnEggControlsX();
        int width = getSpawnEggControlsWidth();
        addRenderableWidget(new InfinityEditorButton(controlsX, 52, width, FIELD_HEIGHT,
                Component.translatable(key("spawnegg.apply_entity")), button -> applySelectedSpawnEggEntity()));
        addRenderableWidget(new InfinityEditorButton(controlsX, 78, width, FIELD_HEIGHT,
                Component.translatable(key("spawnegg.sync_egg")), button -> syncSpawnEggToSelectedEntityItem()));

        InfinityEditorButton clear = addRenderableWidget(new InfinityEditorButton(controlsX, 104, width, FIELD_HEIGHT,
                Component.translatable(key("spawnegg.clear_entity_tag")), button -> clearSpawnEggEntityTag()));
        clear.active = this.previewStack.getTagElement(ENTITY_TAG) != null;

        List<SpawnEggTagRow> rows = getSpawnEggTagRows();
        setSpawnEggTagScroll(this.spawnEggTagScroll);
        int end = Math.min(rows.size(), this.spawnEggTagScroll + SPAWN_EGG_TAG_ROWS);
        for (int i = this.spawnEggTagScroll; i < end; i++) {
            addSpawnEggTagControl(rows.get(i), getSpawnEggTagRowY(i - this.spawnEggTagScroll), controlsX, width);
        }

        addRenderableWidget(new InfinityEditorButton(this.midX - 58, this.height - 64, 28, FIELD_HEIGHT,
                Component.literal("<"), button -> cycleSelectedSpawnEggEntity(-1)));
        addRenderableWidget(new InfinityEditorButton(this.midX - 28, this.height - 64, 56, FIELD_HEIGHT,
                Component.translatable(key("spawnegg.apply_entity")), button -> applySelectedSpawnEggEntity()));
        addRenderableWidget(new InfinityEditorButton(this.midX + 30, this.height - 64, 28, FIELD_HEIGHT,
                Component.literal(">"), button -> cycleSelectedSpawnEggEntity(1)));
    }

    private void addSpawnEggTagControl(SpawnEggTagRow row, int y, int controlsX, int width) {
        if (row.type() == SpawnEggTagRowType.BOOLEAN) {
            addRenderableWidget(new InfinityEditorButton(controlsX, y, width, FIELD_HEIGHT,
                    getSpawnEggBooleanText(row), button -> toggleSpawnEggBoolean(row)));
            return;
        }

        EditBox box = addTrackedBox(legacyTextBox(controlsX + 70, y, width - 70, FIELD_HEIGHT,
                Component.translatable(key("spawnegg." + row.translationSuffix()))));
        box.setMaxLength(row.type() == SpawnEggTagRowType.CUSTOM_NAME ? 256 : 16);
        box.setValue(row.type() == SpawnEggTagRowType.CUSTOM_NAME
                ? this.spawnEggCustomNameValue
                : getSpawnEggNumberValue(row));
        if (row.type() == SpawnEggTagRowType.CUSTOM_NAME) {
            this.spawnEggCustomNameBox = box;
            box.setResponder(value -> {
                this.spawnEggCustomNameValue = value;
                applySpawnEggCustomName(value);
            });
        } else {
            box.setFilter(value -> isAllowedSpawnEggNumber(value, row.numberType()));
            box.setResponder(value -> applySpawnEggNumber(row, value));
        }
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

    private void renderBookPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 40);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("book")), this.midX, 15, MAIN_COLOR);
        if (this.bookTitleBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("book.title")), this.bookTitleBox.getX() - 5, this.bookTitleBox.getY() + 6);
        }
        if (this.bookAuthorBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("book.author")), this.bookAuthorBox.getX() - 5, this.bookAuthorBox.getY() + 6);
        }
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("book.pages"), getBookPageCount()),
                this.midX, 128, CONTRAST_COLOR);
    }

    private void renderHeadPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 36);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("head")), this.midX, 15, MAIN_COLOR);
        if (this.headOwnerBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("head.owner")), this.headOwnerBox.getX() - 5, this.headOwnerBox.getY() + 6);
        }
        if (this.headUuidBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("head.uuid")), this.headUuidBox.getX() - 5, this.headUuidBox.getY() + 6);
        }
        if (this.headTextureBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("head.texture")), this.headTextureBox.getX() - 5, this.headTextureBox.getY() + 6);
        }
        if (this.headTextureSignatureBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("head.signature")),
                    this.headTextureSignatureBox.getX() - 5, this.headTextureSignatureBox.getY() + 6);
        }
    }

    private void renderArmorStandPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 36);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("armorstand")), this.midX, 15, MAIN_COLOR);
    }

    private void renderFireworkPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 36);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("firework")), this.midX, 15, MAIN_COLOR);
        int infoX = Math.min(this.width - 155, this.midX + 96);
        guiGraphics.drawString(this.font, Component.translatable(key("firework.explosions"), getFireworkExplosionCount()),
                infoX, 58, CONTRAST_COLOR);
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

    private void renderSignPanel(GuiGraphics guiGraphics) {
        renderSmallItem(guiGraphics, this.midX, 35);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("sign")), this.midX, 15, MAIN_COLOR);

        for (int i = 0; i < SIGN_LINES && i < this.signBoxes.size(); i++) {
            EditBox box = this.signBoxes.get(i);
            drawRightLabel(guiGraphics, Component.translatable(key("sign.line"), i + 1), box.getX() - 5, box.getY() + 6);
        }
        if (this.signCommandBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("sign.command")),
                    this.signCommandBox.getX() - 5, this.signCommandBox.getY() + 6);
        }
    }

    private void renderContainerPanel(GuiGraphics guiGraphics) {
        renderSmallItem(guiGraphics, this.midX, 34);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("container")), this.midX, 15, MAIN_COLOR);

        int gridX = getContainerGridX();
        int gridY = getContainerGridY();
        for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
            int x = gridX + (slot % CONTAINER_COLUMNS) * CONTAINER_SLOT_PIXEL_SIZE;
            int y = gridY + (slot / CONTAINER_COLUMNS) * CONTAINER_SLOT_PIXEL_SIZE;
            boolean selected = slot == this.selectedContainerSlot;
            guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, selected ? CONTRAST_COLOR : 0xFF555555);
            guiGraphics.fill(x, y, x + ITEM_SIZE, y + ITEM_SIZE, 0xFF1C1C1C);

            ItemStack slotStack = getContainerSlotItem(slot);
            if (!slotStack.isEmpty()) {
                guiGraphics.renderItem(slotStack, x, y);
                guiGraphics.renderItemDecorations(this.font, slotStack, x, y);
            }
        }

        Component selected = Component.translatable(key("container.slot"), this.selectedContainerSlot + 1);
        Component itemCount = Component.translatable(key("container.items"), getContainerItemCount());
        int infoY = gridY + CONTAINER_ROWS * CONTAINER_SLOT_PIXEL_SIZE + 6;
        guiGraphics.drawString(this.font, selected, gridX, infoY, MAIN_COLOR);
        guiGraphics.drawString(this.font, itemCount,
                gridX + CONTAINER_COLUMNS * CONTAINER_SLOT_PIXEL_SIZE - this.font.width(itemCount), infoY, ALT_COLOR);
        if (this.containerSlotNbtBox != null) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("container.slot_nbt")),
                    this.containerSlotNbtBox.getX() + this.containerSlotNbtBox.getWidth() / 2,
                    this.containerSlotNbtBox.getY() - 12, MAIN_COLOR);
        }
    }

    private void renderBannerPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("banner")), this.midX, 15, MAIN_COLOR);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.midX, 78, 100.0F);
        guiGraphics.pose().scale(4.0F, 4.0F, 1.0F);
        guiGraphics.renderItem(this.previewStack, -8, -8);
        guiGraphics.pose().popPose();

        guiGraphics.drawCenteredString(this.font, Component.translatable(key("banner.search")),
                this.bannerPatternFilterBox.getX() + this.bannerPatternFilterBox.getWidth() / 2,
                this.bannerPatternFilterBox.getY() - 12, MAIN_COLOR);

        List<BannerPatternEntry> patterns = getFilteredBannerPatterns();
        clampBannerPatternSelection(patterns);
        if (patterns.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable(key("banner.no_match")), 12, 58, BAD_RED);
        } else {
            int end = Math.min(patterns.size(), this.bannerPatternScroll + BANNER_PATTERN_ROWS);
            for (int i = this.bannerPatternScroll; i < end; i++) {
                int y = getBannerPatternRowY(i - this.bannerPatternScroll);
                int color = i == this.selectedBannerPatternIndex ? CONTRAST_COLOR : MAIN_COLOR;
                Component name = getBannerPatternName(patterns.get(i), getBannerPatternColor());
                guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(name.getString(), 145), 12, y, color);
            }
        }

        Component selected = patterns.isEmpty()
                ? Component.translatable(key("banner.no_match"))
                : getBannerPatternName(patterns.get(this.selectedBannerPatternIndex), getBannerPatternColor());
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("banner.selected"), selected),
                this.midX, this.height - 78, MAIN_COLOR);

        renderBannerPatternLayers(guiGraphics);
    }

    private void renderSpawnEggPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("spawnegg")), this.midX, 15, MAIN_COLOR);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.midX, 78, 100.0F);
        guiGraphics.pose().scale(4.0F, 4.0F, 1.0F);
        guiGraphics.renderItem(this.previewStack, -8, -8);
        guiGraphics.pose().popPose();

        guiGraphics.drawCenteredString(this.font, Component.translatable(key("spawnegg.search")),
                this.spawnEggEntityFilterBox.getX() + this.spawnEggEntityFilterBox.getWidth() / 2,
                this.spawnEggEntityFilterBox.getY() - 12, MAIN_COLOR);

        List<SpawnEggEntityEntry> entities = getFilteredSpawnEggEntities();
        clampSpawnEggEntitySelection(entities);
        if (entities.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable(key("spawnegg.no_match")), 12, 58, BAD_RED);
        } else {
            int end = Math.min(entities.size(), this.spawnEggEntityScroll + SPAWN_EGG_ENTITY_ROWS);
            for (int i = this.spawnEggEntityScroll; i < end; i++) {
                int y = getSpawnEggEntityRowY(i - this.spawnEggEntityScroll);
                int color = i == this.selectedSpawnEggEntityIndex ? CONTRAST_COLOR : MAIN_COLOR;
                guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(formatSpawnEggEntityEntry(entities.get(i)), 170),
                        12, y, color);
            }
        }

        Component selected = entities.isEmpty()
                ? Component.translatable(key("spawnegg.no_match"))
                : getSpawnEggEntityName(entities.get(this.selectedSpawnEggEntityIndex));
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("spawnegg.selected"), selected),
                this.midX, this.height - 78, MAIN_COLOR);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("spawnegg.current"), getCurrentSpawnEggEntityName()),
                this.midX, 120, ALT_COLOR);

        int controlsX = getSpawnEggControlsX();
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("spawnegg.tags")),
                controlsX + getSpawnEggControlsWidth() / 2, getSpawnEggTagRowY(0) - 12, MAIN_COLOR);

        List<SpawnEggTagRow> rows = getSpawnEggTagRows();
        this.spawnEggTagScroll = Mth.clamp(this.spawnEggTagScroll, 0, Math.max(0, rows.size() - SPAWN_EGG_TAG_ROWS));
        int end = Math.min(rows.size(), this.spawnEggTagScroll + SPAWN_EGG_TAG_ROWS);
        for (int i = this.spawnEggTagScroll; i < end; i++) {
            SpawnEggTagRow row = rows.get(i);
            if (row.type() != SpawnEggTagRowType.BOOLEAN) {
                drawRightLabel(guiGraphics, Component.translatable(key("spawnegg." + row.translationSuffix())),
                        controlsX + 66, getSpawnEggTagRowY(i - this.spawnEggTagScroll) + 6);
            }
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
        } else if (this.activePanel == Panel.CONTAINER) {
            int slot = getHoveredContainerSlot(mouseX, mouseY);
            if (slot >= 0) {
                ItemStack slotStack = getContainerSlotItem(slot);
                if (!slotStack.isEmpty()) {
                    guiGraphics.renderTooltip(this.font, slotStack, mouseX, mouseY);
                } else {
                    guiGraphics.renderTooltip(this.font, Component.translatable(key("container.empty_slot")), mouseX, mouseY);
                }
                return;
            }
            if (this.containerSlotNbtBox != null && this.containerSlotNbtBox.getValue().length() > 20
                    && isMouseIn(mouseX, mouseY, this.containerSlotNbtBox.getX(), this.containerSlotNbtBox.getY(),
                    this.containerSlotNbtBox.getWidth(), this.containerSlotNbtBox.getHeight())) {
                guiGraphics.renderTooltip(this.font, Component.literal(this.containerSlotNbtBox.getValue()), mouseX, mouseY);
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

    private boolean handleContainerClick(double mouseX, double mouseY) {
        int gridX = getContainerGridX();
        int gridY = getContainerGridY();
        int gridWidth = CONTAINER_COLUMNS * CONTAINER_SLOT_PIXEL_SIZE;
        int gridHeight = CONTAINER_ROWS * CONTAINER_SLOT_PIXEL_SIZE;
        if (!isMouseIn(mouseX, mouseY, gridX - 1, gridY - 1, gridWidth + 2, gridHeight + 2)) {
            return false;
        }

        int column = ((int) mouseX - gridX) / CONTAINER_SLOT_PIXEL_SIZE;
        int row = ((int) mouseY - gridY) / CONTAINER_SLOT_PIXEL_SIZE;
        int slot = column + row * CONTAINER_COLUMNS;
        if (column < 0 || column >= CONTAINER_COLUMNS || row < 0 || row >= CONTAINER_ROWS || slot < 0 || slot >= CONTAINER_SIZE) {
            return false;
        }

        this.selectedContainerSlot = slot;
        this.containerSlotNbtValue = getContainerSelectedSlotNbt();
        if (this.containerSlotNbtBox != null) {
            this.containerSlotNbtBox.setValue(this.containerSlotNbtValue);
            this.containerSlotNbtBox.setCursorPosition(0);
        }
        return true;
    }

    private boolean handleBannerClick(double mouseX, double mouseY) {
        if (!isMouseIn(mouseX, mouseY, 10, getBannerPatternRowY(0) - 1, 150, BANNER_PATTERN_ROWS * 10 + 2)) {
            return false;
        }

        List<BannerPatternEntry> patterns = getFilteredBannerPatterns();
        if (patterns.isEmpty()) {
            return false;
        }

        int row = ((int) mouseY - getBannerPatternRowY(0)) / 10;
        int index = this.bannerPatternScroll + row;
        if (row < 0 || row >= BANNER_PATTERN_ROWS || index < 0 || index >= patterns.size()) {
            return false;
        }

        this.selectedBannerPatternIndex = index;
        return true;
    }

    private boolean handleSpawnEggClick(double mouseX, double mouseY) {
        if (!isMouseIn(mouseX, mouseY, 10, getSpawnEggEntityRowY(0) - 1, 170, SPAWN_EGG_ENTITY_ROWS * 10 + 2)) {
            return false;
        }

        List<SpawnEggEntityEntry> entities = getFilteredSpawnEggEntities();
        if (entities.isEmpty()) {
            return false;
        }

        int row = ((int) mouseY - getSpawnEggEntityRowY(0)) / 10;
        int index = this.spawnEggEntityScroll + row;
        if (row < 0 || row >= SPAWN_EGG_ENTITY_ROWS || index < 0 || index >= entities.size()) {
            return false;
        }

        this.selectedSpawnEggEntityIndex = index;
        this.spawnEggTagScroll = 0;
        rebuildWidgets();
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

    private void openContainerItemEditor() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        if (!applyMainFieldsToStack(true) || !isContainerEditableItem(this.previewStack)) {
            return;
        }
        this.status = Component.empty();
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.minecraft.setScreen(ContainerItemScreen.create(this, this.minecraft.player, this.previewStack));
    }

    void refreshAfterContainerEdit() {
        readMainFieldsFromStack(this.previewStack);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.containerSlotNbtValue = getContainerSelectedSlotNbt();
        this.nbtFeedback = "";
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
        int containerSlot = this.targetContainerSlot >= 0
                ? this.targetContainerSlot
                : PlayerInventorySlots.HOTBAR_CONTAINER_SLOT_START + selected;
        ItemStack inventoryStack = this.previewStack.copy();
        if (!PlayerInventorySlots.setStack(this.minecraft.player, containerSlot, inventoryStack)) {
            this.status = Component.translatable(messageKey("editor_invalid_target_slot"));
            return;
        }

        this.minecraft.gameMode.handleCreativeModeItemAdd(inventoryStack.copy(), containerSlot);
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
            if (realmController.addItemStack(this.minecraft.player, this.previewStack.copy())) {
                CreativeTabRefresher.refreshRealm(this.minecraft);
                this.status = Component.translatable(messageKey("editor_saved"), this.previewStack.getHoverName());
            }
        }
    }

    private boolean applyMainFieldsToStack(boolean updateStatus) {
        try {
            captureFieldValues();
            tryApplyItemId(true);
            tryApplyCount(true);
            tryApplyDamage(true);
            applyNameToStack();
            applyLoreToStack();
            if (this.activePanel == Panel.SIGN) {
                applySignToStack();
            }
            if (this.activePanel == Panel.BOOK) {
                applyBookMetadataToStack();
            }
            if (this.activePanel == Panel.HEAD) {
                applyHeadToStack();
            }
            if (this.activePanel == Panel.BANNER) {
                readBannerFieldsFromStack(this.previewStack);
            }
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

    private void applySignToStack() {
        if (!isSignItem(this.previewStack)) {
            return;
        }

        boolean hasContent = hasSignContent();
        CompoundTag tag = hasContent ? this.previewStack.getOrCreateTag() : this.previewStack.getTag();
        if (tag == null) {
            return;
        }

        CompoundTag blockEntity = tag.getCompound(BLOCK_ENTITY_TAG);
        if (!hasContent) {
            blockEntity.remove(SIGN_FRONT_TEXT_TAG);
            removeLegacySignText(blockEntity);
            cleanupBlockEntityTag(tag, blockEntity);
            this.rawNbtValue = getInitialNbt(this.previewStack);
            return;
        }

        CompoundTag frontText = blockEntity.getCompound(SIGN_FRONT_TEXT_TAG);
        ListTag messages = new ListTag();
        for (int i = 0; i < SIGN_LINES; i++) {
            messages.add(StringTag.valueOf(Component.Serializer.toJson(createSignLineComponent(i))));
        }

        frontText.put(SIGN_MESSAGES_TAG, messages);
        frontText.remove(SIGN_FILTERED_MESSAGES_TAG);
        if (!frontText.contains(SIGN_COLOR_TAG, Tag.TAG_STRING)) {
            frontText.putString(SIGN_COLOR_TAG, "black");
        }
        if (!frontText.contains(SIGN_GLOWING_TEXT_TAG, Tag.TAG_BYTE)) {
            frontText.putBoolean(SIGN_GLOWING_TEXT_TAG, false);
        }

        blockEntity.put(SIGN_FRONT_TEXT_TAG, frontText);
        removeLegacySignText(blockEntity);
        cleanupBlockEntityTag(tag, blockEntity);
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    private void applyBookMetadataToStack() {
        if (!this.previewStack.is(Items.WRITTEN_BOOK)) {
            return;
        }

        CompoundTag tag = this.previewStack.getOrCreateTag();
        tag.putString(BOOK_TITLE_TAG, this.bookTitleValue == null ? "" : this.bookTitleValue);
        tag.putString(BOOK_AUTHOR_TAG, this.bookAuthorValue == null ? "" : this.bookAuthorValue);
        cleanupEmptyTag();
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    private void cycleBookGeneration() {
        if (!this.previewStack.is(Items.WRITTEN_BOOK)) {
            return;
        }

        CompoundTag tag = this.previewStack.getOrCreateTag();
        int next = Mth.positiveModulo(tag.getInt(BOOK_GENERATION_TAG) + 1, MAX_BOOK_GENERATION + 1);
        if (next == 0) {
            tag.remove(BOOK_GENERATION_TAG);
        } else {
            tag.putInt(BOOK_GENERATION_TAG, next);
        }
        cleanupEmptyTag();
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_book_generation_updated"), next);
        rebuildWidgets();
    }

    private void toggleBookResolved() {
        if (!this.previewStack.is(Items.WRITTEN_BOOK)) {
            return;
        }

        CompoundTag tag = this.previewStack.getOrCreateTag();
        if (tag.getBoolean(BOOK_RESOLVED_TAG)) {
            tag.remove(BOOK_RESOLVED_TAG);
        } else {
            tag.putBoolean(BOOK_RESOLVED_TAG, true);
        }
        cleanupEmptyTag();
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_book_resolved_updated"));
        rebuildWidgets();
    }

    private void toggleBookSignedState() {
        captureFieldValues();
        if (this.previewStack.is(Items.WRITTEN_BOOK)) {
            unsignBook();
        } else if (this.previewStack.is(Items.WRITABLE_BOOK)) {
            signBook();
        }
    }

    private void unsignBook() {
        CompoundTag originalTag = this.previewStack.getTag();
        this.rememberedSignedBookData = originalTag == null ? new CompoundTag() : originalTag.copy();
        ItemStack writableBook = new ItemStack(Items.WRITABLE_BOOK, Math.max(1, this.previewStack.getCount()));
        CompoundTag writableTag = originalTag == null ? null : originalTag.copy();
        if (writableTag != null) {
            convertWrittenPagesToWritable(writableTag);
            writableTag.remove(BOOK_TITLE_TAG);
            writableTag.remove(BOOK_AUTHOR_TAG);
            writableTag.remove(BOOK_GENERATION_TAG);
            writableTag.remove(BOOK_RESOLVED_TAG);
            if (writableTag.isEmpty()) {
                writableTag = null;
            }
        }
        writableBook.setTag(writableTag);
        this.previewStack = writableBook;
        readMainFieldsFromStack(this.previewStack);
        this.bookTitleValue = this.rememberedSignedBookData.getString(BOOK_TITLE_TAG);
        this.bookAuthorValue = this.rememberedSignedBookData.getString(BOOK_AUTHOR_TAG);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_book_unsigned"));
        rebuildWidgets();
    }

    private void signBook() {
        ItemStack writtenBook = new ItemStack(Items.WRITTEN_BOOK, Math.max(1, this.previewStack.getCount()));
        CompoundTag writableTag = this.previewStack.getTag();
        CompoundTag signedTag = writableTag == null ? new CompoundTag() : writableTag.copy();
        convertWritablePagesToWritten(signedTag);

        int generation = 0;
        boolean resolved = false;
        if (this.rememberedSignedBookData != null) {
            generation = Mth.clamp(this.rememberedSignedBookData.getInt(BOOK_GENERATION_TAG), 0, MAX_BOOK_GENERATION);
            resolved = this.rememberedSignedBookData.getBoolean(BOOK_RESOLVED_TAG);
        }

        signedTag.putString(BOOK_TITLE_TAG, this.bookTitleValue == null ? "" : this.bookTitleValue);
        signedTag.putString(BOOK_AUTHOR_TAG, this.bookAuthorValue == null ? "" : this.bookAuthorValue);
        if (generation == 0) {
            signedTag.remove(BOOK_GENERATION_TAG);
        } else {
            signedTag.putInt(BOOK_GENERATION_TAG, generation);
        }
        if (resolved) {
            signedTag.putBoolean(BOOK_RESOLVED_TAG, true);
        } else {
            signedTag.remove(BOOK_RESOLVED_TAG);
        }

        writtenBook.setTag(signedTag.isEmpty() ? null : signedTag);
        this.previewStack = writtenBook;
        this.rememberedSignedBookData = null;
        readMainFieldsFromStack(this.previewStack);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_book_signed"));
        rebuildWidgets();
    }

    private void convertWrittenPagesToWritable(CompoundTag tag) {
        if (!tag.contains(BOOK_PAGES_TAG, Tag.TAG_LIST)) {
            return;
        }

        ListTag pages = tag.getList(BOOK_PAGES_TAG, Tag.TAG_STRING);
        ListTag converted = new ListTag();
        for (int i = 0; i < pages.size(); i++) {
            converted.add(StringTag.valueOf(readSerializedComponent(pages.getString(i)).getString()));
        }
        tag.put(BOOK_PAGES_TAG, converted);
    }

    private void convertWritablePagesToWritten(CompoundTag tag) {
        if (!tag.contains(BOOK_PAGES_TAG, Tag.TAG_LIST)) {
            return;
        }

        ListTag pages = tag.getList(BOOK_PAGES_TAG, Tag.TAG_STRING);
        ListTag converted = new ListTag();
        for (int i = 0; i < pages.size(); i++) {
            converted.add(StringTag.valueOf(Component.Serializer.toJson(readBookPageComponent(pages.getString(i)))));
        }
        tag.put(BOOK_PAGES_TAG, converted);
    }

    private Component readBookPageComponent(String raw) {
        Component parsed = readSerializedComponent(raw);
        if (!parsed.getString().equals(raw) || isProbablyJsonText(raw)) {
            return parsed;
        }
        return Component.literal(raw);
    }

    private boolean isProbablyJsonText(String raw) {
        String value = raw == null ? "" : raw.trim();
        return value.startsWith("{") || value.startsWith("[") || value.startsWith("\"");
    }

    private void applyHeadToStack() {
        if (!isPlayerHeadItem(this.previewStack)) {
            return;
        }

        String ownerName = normalizeHeadText(this.headOwnerValue);
        String uuidText = normalizeHeadText(this.headUuidValue);
        String textureValue = normalizeHeadText(this.headTextureValue);
        String textureSignature = normalizeHeadText(this.headTextureSignatureValue);
        UUID uuid = parseUuidOrNull(uuidText);

        CompoundTag tag = this.previewStack.getOrCreateTag();
        if (ownerName.isEmpty() && uuidText.isEmpty() && textureValue.isEmpty()) {
            tag.remove(SKULL_OWNER_TAG);
            cleanupEmptyTag();
            this.rawNbtValue = getInitialNbt(this.previewStack);
            return;
        }

        if (textureValue.isEmpty() && uuid == null && !ownerName.isEmpty()) {
            tag.putString(SKULL_OWNER_TAG, ownerName);
            this.rawNbtValue = getInitialNbt(this.previewStack);
            return;
        }

        CompoundTag skullOwner = new CompoundTag();
        if (!ownerName.isEmpty()) {
            skullOwner.putString(SKULL_OWNER_NAME_TAG, ownerName);
        }
        if (uuid != null) {
            skullOwner.putUUID(SKULL_OWNER_ID_TAG, uuid);
        }
        if (!textureValue.isEmpty()) {
            CompoundTag properties = new CompoundTag();
            ListTag textures = new ListTag();
            CompoundTag texture = new CompoundTag();
            texture.putString(SKULL_TEXTURE_VALUE_TAG, textureValue);
            if (!textureSignature.isEmpty()) {
                texture.putString(SKULL_TEXTURE_SIGNATURE_TAG, textureSignature);
            }
            textures.add(texture);
            properties.put(SKULL_TEXTURES_TAG, textures);
            skullOwner.put(SKULL_PROPERTIES_TAG, properties);
        }

        tag.put(SKULL_OWNER_TAG, skullOwner);
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    private void clearHeadOwner() {
        this.headOwnerValue = "";
        this.headUuidValue = "";
        this.headTextureValue = "";
        this.headTextureSignatureValue = "";
        if (this.headOwnerBox != null) {
            this.headOwnerBox.setValue("");
        }
        if (this.headUuidBox != null) {
            this.headUuidBox.setValue("");
        }
        if (this.headTextureBox != null) {
            this.headTextureBox.setValue("");
        }
        if (this.headTextureSignatureBox != null) {
            this.headTextureSignatureBox.setValue("");
        }
        applyHeadToStack();
        this.status = Component.translatable(messageKey("editor_head_cleared"));
    }

    private void randomizeHeadUuid() {
        this.headUuidValue = UUID.randomUUID().toString();
        if (this.headUuidBox != null) {
            this.headUuidBox.setValue(this.headUuidValue);
        }
        applyHeadToStack();
        this.status = Component.translatable(messageKey("editor_head_uuid_randomized"));
    }

    private void toggleArmorStandFlag(String tagKey, String translationSuffix) {
        if (!isArmorStandItem(this.previewStack)) {
            return;
        }

        CompoundTag entityTag = getOrCreateArmorStandEntityTag();
        if (entityTag.getBoolean(tagKey)) {
            entityTag.remove(tagKey);
        } else {
            entityTag.putBoolean(tagKey, true);
        }
        cleanupArmorStandEntityTag(entityTag);
        this.status = Component.translatable(messageKey("editor_armor_stand_updated"),
                Component.translatable(key("armorstand." + translationSuffix + ".label")));
        rebuildWidgets();
    }

    private void clearArmorStandEntityTag() {
        if (!isArmorStandItem(this.previewStack)) {
            return;
        }

        CompoundTag tag = this.previewStack.getTag();
        if (tag == null || !tag.contains(ENTITY_TAG, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag currentEntityTag = tag.getCompound(ENTITY_TAG);
        CompoundTag clearedEntityTag = new CompoundTag();
        if (currentEntityTag.contains(ENTITY_ID_TAG, Tag.TAG_STRING)) {
            clearedEntityTag.putString(ENTITY_ID_TAG, currentEntityTag.getString(ENTITY_ID_TAG));
        }
        if (clearedEntityTag.isEmpty()) {
            tag.remove(ENTITY_TAG);
        } else {
            tag.put(ENTITY_TAG, clearedEntityTag);
        }
        cleanupEmptyTag();
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_armor_stand_cleared"));
        rebuildWidgets();
    }

    private CompoundTag getOrCreateArmorStandEntityTag() {
        CompoundTag tag = this.previewStack.getOrCreateTag();
        CompoundTag entityTag = tag.getCompound(ENTITY_TAG);
        tag.put(ENTITY_TAG, entityTag);
        return entityTag;
    }

    private void cleanupArmorStandEntityTag(CompoundTag entityTag) {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null) {
            return;
        }

        if (entityTag.isEmpty()) {
            tag.remove(ENTITY_TAG);
        } else {
            tag.put(ENTITY_TAG, entityTag);
        }
        cleanupEmptyTag();
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    private Component getArmorStandToggleText(String translationSuffix, String tagKey) {
        return Component.translatable(key("armorstand." + translationSuffix + "." + (getArmorStandFlag(tagKey) ? 1 : 0)));
    }

    private boolean getArmorStandFlag(String tagKey) {
        CompoundTag entityTag = this.previewStack.getTagElement(ENTITY_TAG);
        return entityTag != null && entityTag.getBoolean(tagKey);
    }

    private void cycleFireworkFlight() {
        if (!this.previewStack.is(Items.FIREWORK_ROCKET)) {
            return;
        }

        CompoundTag fireworks = getOrCreateFireworksTag();
        int next = getFireworkFlight() + 1;
        if (next > MAX_FIREWORK_FLIGHT) {
            next = 1;
        }
        fireworks.putByte(FIREWORK_FLIGHT_TAG, (byte) next);
        cleanupFireworksTag(fireworks);
        this.status = Component.translatable(messageKey("editor_firework_flight_updated"), next);
        rebuildWidgets();
    }

    private void cycleFireworkExplosionType(int direction) {
        if (!isFireworkEditableItem(this.previewStack)) {
            return;
        }

        this.fireworkExplosionType = Mth.positiveModulo(this.fireworkExplosionType + direction, FIREWORK_EXPLOSION_TYPES);
        applyFireworkControlsToStack();
        this.status = Component.translatable(messageKey("editor_firework_updated"));
        rebuildWidgets();
    }

    private void toggleFireworkFlicker() {
        if (!isFireworkEditableItem(this.previewStack)) {
            return;
        }

        this.fireworkFlicker = !this.fireworkFlicker;
        applyFireworkControlsToStack();
        this.status = Component.translatable(messageKey("editor_firework_updated"));
        rebuildWidgets();
    }

    private void toggleFireworkTrail() {
        if (!isFireworkEditableItem(this.previewStack)) {
            return;
        }

        this.fireworkTrail = !this.fireworkTrail;
        applyFireworkControlsToStack();
        this.status = Component.translatable(messageKey("editor_firework_updated"));
        rebuildWidgets();
    }

    private void cycleFireworkColor(boolean fade, int direction) {
        if (!isFireworkEditableItem(this.previewStack)) {
            return;
        }

        int colorCount = DyeColor.values().length;
        if (fade) {
            int selected = this.fireworkFadeColor < 0 ? 0 : this.fireworkFadeColor + 1;
            selected = Mth.positiveModulo(selected + direction, colorCount + 1);
            this.fireworkFadeColor = selected == 0 ? -1 : selected - 1;
        } else {
            this.fireworkColor = Mth.positiveModulo(this.fireworkColor + direction, colorCount);
        }
        applyFireworkControlsToStack();
        this.status = Component.translatable(messageKey("editor_firework_updated"));
        rebuildWidgets();
    }

    private void randomizeFireworkColors() {
        if (!isFireworkEditableItem(this.previewStack)) {
            return;
        }

        int colorCount = DyeColor.values().length;
        this.fireworkColor = ThreadLocalRandom.current().nextInt(colorCount);
        this.fireworkFadeColor = ThreadLocalRandom.current().nextInt(colorCount + 1) - 1;
        applyFireworkControlsToStack();
        this.status = Component.translatable(messageKey("editor_firework_updated"));
        rebuildWidgets();
    }

    private void addFireworkExplosion() {
        if (!this.previewStack.is(Items.FIREWORK_ROCKET)) {
            return;
        }

        CompoundTag fireworks = getOrCreateFireworksTag();
        if (!fireworks.contains(FIREWORK_FLIGHT_TAG, Tag.TAG_BYTE)) {
            fireworks.putByte(FIREWORK_FLIGHT_TAG, (byte) getFireworkFlight());
        }
        ListTag explosions = fireworks.contains(FIREWORK_EXPLOSIONS_TAG, Tag.TAG_LIST)
                ? fireworks.getList(FIREWORK_EXPLOSIONS_TAG, Tag.TAG_COMPOUND).copy()
                : new ListTag();
        explosions.add(createFireworkExplosionTag());
        fireworks.put(FIREWORK_EXPLOSIONS_TAG, explosions);
        cleanupFireworksTag(fireworks);
        this.status = Component.translatable(messageKey("editor_firework_explosion_added"), explosions.size());
        rebuildWidgets();
    }

    private void removeLastFireworkExplosion() {
        if (!this.previewStack.is(Items.FIREWORK_ROCKET)) {
            return;
        }

        CompoundTag fireworks = this.previewStack.getTagElement(FIREWORKS_TAG);
        if (fireworks == null || !fireworks.contains(FIREWORK_EXPLOSIONS_TAG, Tag.TAG_LIST)) {
            return;
        }

        ListTag explosions = fireworks.getList(FIREWORK_EXPLOSIONS_TAG, Tag.TAG_COMPOUND);
        if (explosions.isEmpty()) {
            return;
        }

        explosions.remove(explosions.size() - 1);
        if (explosions.isEmpty()) {
            fireworks.remove(FIREWORK_EXPLOSIONS_TAG);
        } else {
            fireworks.put(FIREWORK_EXPLOSIONS_TAG, explosions);
        }
        cleanupFireworksTag(fireworks);
        readFireworkFieldsFromStack(this.previewStack);
        this.status = Component.translatable(messageKey("editor_firework_explosion_removed"));
        rebuildWidgets();
    }

    private void clearFireworkData() {
        if (!isFireworkEditableItem(this.previewStack)) {
            return;
        }

        CompoundTag tag = this.previewStack.getTag();
        if (tag == null) {
            return;
        }

        if (this.previewStack.is(Items.FIREWORK_ROCKET)) {
            tag.remove(FIREWORKS_TAG);
        } else {
            tag.remove(FIREWORK_EXPLOSION_TAG);
        }
        cleanupEmptyTag();
        this.rawNbtValue = getInitialNbt(this.previewStack);
        readFireworkFieldsFromStack(this.previewStack);
        this.status = Component.translatable(messageKey("editor_firework_cleared"));
        rebuildWidgets();
    }

    private void applyFireworkControlsToStack() {
        if (this.previewStack.is(Items.FIREWORK_STAR)) {
            this.previewStack.getOrCreateTag().put(FIREWORK_EXPLOSION_TAG, createFireworkExplosionTag());
            this.rawNbtValue = getInitialNbt(this.previewStack);
            return;
        }

        if (!this.previewStack.is(Items.FIREWORK_ROCKET)) {
            return;
        }

        CompoundTag fireworks = this.previewStack.getTagElement(FIREWORKS_TAG);
        if (fireworks == null || !fireworks.contains(FIREWORK_EXPLOSIONS_TAG, Tag.TAG_LIST)) {
            return;
        }

        ListTag explosions = fireworks.getList(FIREWORK_EXPLOSIONS_TAG, Tag.TAG_COMPOUND);
        if (explosions.isEmpty()) {
            return;
        }

        explosions.set(explosions.size() - 1, createFireworkExplosionTag());
        fireworks.put(FIREWORK_EXPLOSIONS_TAG, explosions);
        cleanupFireworksTag(fireworks);
    }

    private CompoundTag createFireworkExplosionTag() {
        CompoundTag explosion = new CompoundTag();
        explosion.putByte(FIREWORK_TYPE_TAG, (byte) Mth.clamp(this.fireworkExplosionType, 0, FIREWORK_EXPLOSION_TYPES - 1));
        explosion.putIntArray(FIREWORK_COLORS_TAG, new int[]{getFireworkRgb(getFireworkDyeColor(this.fireworkColor))});
        if (this.fireworkFadeColor >= 0) {
            explosion.putIntArray(FIREWORK_FADE_COLORS_TAG, new int[]{getFireworkRgb(getFireworkDyeColor(this.fireworkFadeColor))});
        }
        if (this.fireworkFlicker) {
            explosion.putBoolean(FIREWORK_FLICKER_TAG, true);
        }
        if (this.fireworkTrail) {
            explosion.putBoolean(FIREWORK_TRAIL_TAG, true);
        }
        return explosion;
    }

    private CompoundTag getOrCreateFireworksTag() {
        CompoundTag tag = this.previewStack.getOrCreateTag();
        CompoundTag fireworks = tag.getCompound(FIREWORKS_TAG);
        tag.put(FIREWORKS_TAG, fireworks);
        return fireworks;
    }

    private void cleanupFireworksTag(CompoundTag fireworks) {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null) {
            return;
        }

        if (fireworks.isEmpty()) {
            tag.remove(FIREWORKS_TAG);
        } else {
            tag.put(FIREWORKS_TAG, fireworks);
        }
        cleanupEmptyTag();
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    private void cycleContainerSlot(int direction) {
        this.selectedContainerSlot = Mth.positiveModulo(this.selectedContainerSlot + direction, CONTAINER_SIZE);
        this.containerSlotNbtValue = getContainerSelectedSlotNbt();
        if (this.containerSlotNbtBox != null) {
            this.containerSlotNbtBox.setValue(this.containerSlotNbtValue);
            this.containerSlotNbtBox.setCursorPosition(0);
        }
    }

    private void updateContainerSlotFromNbt() {
        if (!isContainerEditableItem(this.previewStack)) {
            return;
        }

        captureFieldValues();
        try {
            ItemStack slotStack = parseContainerSlotItem(this.containerSlotNbtValue);
            setContainerSlotItem(this.selectedContainerSlot, slotStack);
            this.containerSlotNbtValue = getContainerSlotNbt(slotStack);
            this.status = slotStack.isEmpty()
                    ? Component.translatable(messageKey("editor_container_slot_cleared"), this.selectedContainerSlot + 1)
                    : Component.translatable(messageKey("editor_container_slot_updated"), this.selectedContainerSlot + 1, slotStack.getHoverName());
            rebuildWidgets();
        } catch (CommandSyntaxException exception) {
            this.status = Component.translatable(messageKey("editor_invalid_nbt"), exception.getMessage());
        } catch (IllegalArgumentException exception) {
            this.status = Component.literal(exception.getMessage());
        }
    }

    private ItemStack parseContainerSlotItem(String value) throws CommandSyntaxException {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty() || "{}".equals(trimmed)) {
            return ItemStack.EMPTY;
        }

        CompoundTag itemTag = TagParser.parseTag(trimmed);
        ItemStack slotStack = ItemStack.of(itemTag);
        if (slotStack.isEmpty()) {
            throw new IllegalArgumentException(Component.translatable(messageKey("editor_container_invalid_item")).getString());
        }
        return slotStack;
    }

    private void clearContainerSlot() {
        if (!isContainerEditableItem(this.previewStack)) {
            return;
        }

        setContainerSlotItem(this.selectedContainerSlot, ItemStack.EMPTY);
        this.containerSlotNbtValue = "{}";
        this.status = Component.translatable(messageKey("editor_container_slot_cleared"), this.selectedContainerSlot + 1);
        rebuildWidgets();
    }

    private void clearContainerItems() {
        if (!isContainerEditableItem(this.previewStack)) {
            return;
        }

        CompoundTag tag = this.previewStack.getTag();
        if (tag == null || !tag.contains(BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag blockEntity = tag.getCompound(BLOCK_ENTITY_TAG);
        blockEntity.remove(CONTAINER_ITEMS_TAG);
        cleanupBlockEntityTag(tag, blockEntity);
        this.containerSlotNbtValue = "{}";
        this.status = Component.translatable(messageKey("editor_container_cleared"));
        rebuildWidgets();
    }

    private void setContainerSlotItem(int slot, ItemStack slotStack) {
        CompoundTag tag = this.previewStack.getOrCreateTag();
        CompoundTag blockEntity = tag.contains(BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)
                ? tag.getCompound(BLOCK_ENTITY_TAG)
                : new CompoundTag();
        ListTag currentItems = blockEntity.contains(CONTAINER_ITEMS_TAG, Tag.TAG_LIST)
                ? blockEntity.getList(CONTAINER_ITEMS_TAG, Tag.TAG_COMPOUND)
                : new ListTag();
        List<CompoundTag> updatedItems = new ArrayList<>();
        for (int i = 0; i < currentItems.size(); i++) {
            CompoundTag itemTag = currentItems.getCompound(i);
            if ((itemTag.getByte(CONTAINER_SLOT_TAG) & 255) != slot) {
                updatedItems.add(itemTag.copy());
            }
        }

        if (!slotStack.isEmpty()) {
            CompoundTag itemTag = slotStack.save(new CompoundTag());
            itemTag.putByte(CONTAINER_SLOT_TAG, (byte) slot);
            updatedItems.add(itemTag);
        }

        updatedItems.sort(Comparator.comparingInt(itemTag -> itemTag.getByte(CONTAINER_SLOT_TAG) & 255));
        ListTag items = new ListTag();
        for (CompoundTag itemTag : updatedItems) {
            items.add(itemTag);
        }

        if (items.isEmpty()) {
            blockEntity.remove(CONTAINER_ITEMS_TAG);
        } else {
            blockEntity.put(CONTAINER_ITEMS_TAG, items);
        }
        cleanupBlockEntityTag(tag, blockEntity);
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    private ItemStack getContainerSlotItem(int slot) {
        if (!isContainerEditableItem(this.previewStack)) {
            return ItemStack.EMPTY;
        }

        ListTag items = getContainerItemsList();
        ItemStack found = ItemStack.EMPTY;
        for (int i = 0; i < items.size(); i++) {
            CompoundTag itemTag = items.getCompound(i);
            if ((itemTag.getByte(CONTAINER_SLOT_TAG) & 255) == slot) {
                found = ItemStack.of(itemTag);
            }
        }
        return found;
    }

    private ListTag getContainerItemsList() {
        CompoundTag blockEntity = this.previewStack.getTagElement(BLOCK_ENTITY_TAG);
        if (blockEntity == null || !blockEntity.contains(CONTAINER_ITEMS_TAG, Tag.TAG_LIST)) {
            return new ListTag();
        }
        return blockEntity.getList(CONTAINER_ITEMS_TAG, Tag.TAG_COMPOUND);
    }

    private int getContainerItemCount() {
        int count = 0;
        for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
            if (!getContainerSlotItem(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private String getContainerSelectedSlotNbt() {
        return getContainerSlotNbt(getContainerSlotItem(this.selectedContainerSlot));
    }

    private String getContainerSlotNbt(ItemStack stack) {
        if (stack.isEmpty()) {
            return "{}";
        }
        return stack.save(new CompoundTag()).toString();
    }

    private int getContainerGridX() {
        return this.midX - (CONTAINER_COLUMNS * CONTAINER_SLOT_PIXEL_SIZE) / 2;
    }

    private int getContainerGridY() {
        return 48;
    }

    private int getHoveredContainerSlot(int mouseX, int mouseY) {
        int gridX = getContainerGridX();
        int gridY = getContainerGridY();
        if (!isMouseIn(mouseX, mouseY, gridX - 1, gridY - 1,
                CONTAINER_COLUMNS * CONTAINER_SLOT_PIXEL_SIZE + 2,
                CONTAINER_ROWS * CONTAINER_SLOT_PIXEL_SIZE + 2)) {
            return -1;
        }

        int column = (mouseX - gridX) / CONTAINER_SLOT_PIXEL_SIZE;
        int row = (mouseY - gridY) / CONTAINER_SLOT_PIXEL_SIZE;
        int slot = column + row * CONTAINER_COLUMNS;
        return column < 0 || column >= CONTAINER_COLUMNS || row < 0 || row >= CONTAINER_ROWS ? -1 : slot;
    }

    private MutableComponent createSignLineComponent(int line) {
        MutableComponent component = Component.literal(getSignLineValue(line));
        if (line == 0) {
            String command = getNormalizedSignCommand();
            if (!command.isEmpty()) {
                component.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));
            }
        }
        return component;
    }

    private boolean hasSignContent() {
        if (!getNormalizedSignCommand().isEmpty()) {
            return true;
        }
        for (int i = 0; i < SIGN_LINES; i++) {
            if (!getSignLineValue(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private String getSignLineValue(int line) {
        if (line < 0 || line >= this.signLineValues.length || this.signLineValues[line] == null) {
            return "";
        }
        return this.signLineValues[line];
    }

    private String getNormalizedSignCommand() {
        return this.signCommandValue == null ? "" : this.signCommandValue.trim();
    }

    private void cleanupBlockEntityTag(CompoundTag tag, CompoundTag blockEntity) {
        if (blockEntity.isEmpty()) {
            tag.remove(BLOCK_ENTITY_TAG);
        } else {
            tag.put(BLOCK_ENTITY_TAG, blockEntity);
        }
        cleanupEmptyTag();
    }

    private static void removeLegacySignText(CompoundTag blockEntity) {
        for (int i = 0; i < SIGN_LINES; i++) {
            blockEntity.remove(LEGACY_SIGN_TEXT_TAG_PREFIX + (i + 1));
        }
    }

    private void addSelectedBannerPattern() {
        if (!isBannerEditableItem(this.previewStack)) {
            return;
        }

        List<BannerPatternEntry> patterns = getFilteredBannerPatterns();
        clampBannerPatternSelection(patterns);
        if (patterns.isEmpty()) {
            this.status = Component.translatable(key("banner.no_match"));
            return;
        }

        BannerPatternEntry entry = patterns.get(this.selectedBannerPatternIndex);
        DyeColor color = getBannerPatternColor();
        CompoundTag tag = this.previewStack.getOrCreateTag();
        CompoundTag blockEntity = getOrCreateBannerBlockEntityTag();
        if (this.previewStack.is(Items.SHIELD) && !blockEntity.contains(BANNER_BASE_TAG, Tag.TAG_INT)) {
            blockEntity.putInt(BANNER_BASE_TAG, getBannerBaseColor().getId());
        }

        ListTag bannerPatterns = blockEntity.contains(BANNER_PATTERNS_TAG, Tag.TAG_LIST)
                ? blockEntity.getList(BANNER_PATTERNS_TAG, Tag.TAG_COMPOUND).copy()
                : new ListTag();
        CompoundTag patternTag = new CompoundTag();
        patternTag.putString(BANNER_PATTERN_TAG, entry.hash());
        patternTag.putInt(BANNER_COLOR_TAG, color.getId());
        bannerPatterns.add(patternTag);
        blockEntity.put(BANNER_PATTERNS_TAG, bannerPatterns);
        cleanupBlockEntityTag(tag, blockEntity);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_banner_pattern_added"), getBannerPatternName(entry, color));
        rebuildWidgets();
    }

    private void removeLastBannerPattern() {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null) {
            return;
        }

        CompoundTag blockEntity = tag.getCompound(BLOCK_ENTITY_TAG);
        if (!blockEntity.contains(BANNER_PATTERNS_TAG, Tag.TAG_LIST)) {
            return;
        }

        ListTag patterns = blockEntity.getList(BANNER_PATTERNS_TAG, Tag.TAG_COMPOUND);
        if (patterns.isEmpty()) {
            return;
        }

        patterns.remove(patterns.size() - 1);
        if (patterns.isEmpty()) {
            blockEntity.remove(BANNER_PATTERNS_TAG);
        } else {
            blockEntity.put(BANNER_PATTERNS_TAG, patterns);
        }
        cleanupBlockEntityTag(tag, blockEntity);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_banner_pattern_removed"));
        rebuildWidgets();
    }

    private void clearBannerPatterns() {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null) {
            return;
        }

        CompoundTag blockEntity = tag.getCompound(BLOCK_ENTITY_TAG);
        if (!blockEntity.contains(BANNER_PATTERNS_TAG, Tag.TAG_LIST)) {
            return;
        }

        blockEntity.remove(BANNER_PATTERNS_TAG);
        cleanupBlockEntityTag(tag, blockEntity);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_banner_patterns_cleared"));
        rebuildWidgets();
    }

    private void cycleBannerBaseColor(int direction) {
        DyeColor color = DyeColor.byId(Mth.positiveModulo(getBannerBaseColor().getId() + direction, DyeColor.values().length));
        setBannerBaseColor(color);
        this.status = Component.translatable(messageKey("editor_banner_base_updated"), getDyeColorName(color));
        rebuildWidgets();
    }

    private void cycleBannerPatternColor(int direction) {
        this.bannerPatternColor = Mth.positiveModulo(this.bannerPatternColor + direction, DyeColor.values().length);
        rebuildWidgets();
    }

    private void cycleSelectedBannerPattern(int direction) {
        List<BannerPatternEntry> patterns = getFilteredBannerPatterns();
        if (patterns.isEmpty()) {
            return;
        }

        this.selectedBannerPatternIndex = Mth.positiveModulo(this.selectedBannerPatternIndex + direction, patterns.size());
        scrollBannerPatternSelectionIntoView(patterns);
    }

    private void setBannerPatternScroll(int value) {
        List<BannerPatternEntry> patterns = getFilteredBannerPatterns();
        int maxScroll = Math.max(0, patterns.size() - BANNER_PATTERN_ROWS);
        this.bannerPatternScroll = Mth.clamp(value, 0, maxScroll);
        clampBannerPatternSelection(patterns);
        if (!patterns.isEmpty()) {
            int lastVisible = Math.min(patterns.size() - 1, this.bannerPatternScroll + BANNER_PATTERN_ROWS - 1);
            this.selectedBannerPatternIndex = Mth.clamp(this.selectedBannerPatternIndex, this.bannerPatternScroll, lastVisible);
        }
    }

    private void scrollBannerPatternSelectionIntoView(List<BannerPatternEntry> patterns) {
        clampBannerPatternSelection(patterns);
        if (patterns.isEmpty()) {
            return;
        }

        if (this.selectedBannerPatternIndex < this.bannerPatternScroll) {
            this.bannerPatternScroll = this.selectedBannerPatternIndex;
        } else if (this.selectedBannerPatternIndex >= this.bannerPatternScroll + BANNER_PATTERN_ROWS) {
            this.bannerPatternScroll = this.selectedBannerPatternIndex - BANNER_PATTERN_ROWS + 1;
        }
        this.bannerPatternScroll = Mth.clamp(this.bannerPatternScroll, 0, Math.max(0, patterns.size() - BANNER_PATTERN_ROWS));
    }

    private void clampBannerPatternSelection(List<BannerPatternEntry> patterns) {
        if (patterns.isEmpty()) {
            this.selectedBannerPatternIndex = 0;
            this.bannerPatternScroll = 0;
            return;
        }

        this.selectedBannerPatternIndex = Mth.clamp(this.selectedBannerPatternIndex, 0, patterns.size() - 1);
        this.bannerPatternScroll = Mth.clamp(this.bannerPatternScroll, 0, Math.max(0, patterns.size() - BANNER_PATTERN_ROWS));
    }

    private int getBannerPatternRowY(int row) {
        return 58 + row * 10;
    }

    private List<BannerPatternEntry> getFilteredBannerPatterns() {
        String filter = this.bannerPatternFilterValue == null ? "" : this.bannerPatternFilterValue.trim().toLowerCase(Locale.ROOT);
        if (filter.isEmpty()) {
            return new ArrayList<>(BannerPatternCatalog.PATTERNS);
        }

        DyeColor color = getBannerPatternColor();
        List<BannerPatternEntry> patterns = new ArrayList<>();
        for (BannerPatternEntry entry : BannerPatternCatalog.PATTERNS) {
            String idName = entry.name().toLowerCase(Locale.ROOT);
            String spacedName = idName.replace('_', ' ');
            String hash = entry.hash().toLowerCase(Locale.ROOT);
            String displayName = getBannerPatternName(entry, color).getString().toLowerCase(Locale.ROOT);
            if (idName.contains(filter) || spacedName.contains(filter) || hash.contains(filter) || displayName.contains(filter)) {
                patterns.add(entry);
            }
        }
        return patterns;
    }

    private Component getBannerPatternName(BannerPatternEntry entry, DyeColor color) {
        return Component.translatable("block.minecraft.banner." + entry.name() + "." + color.getName());
    }

    private void renderBannerPatternLayers(GuiGraphics guiGraphics) {
        ListTag patterns = getBannerPatterns();
        int x = this.midX - 70;
        int y = 124;
        guiGraphics.drawString(this.font, Component.translatable(key("banner.layers")), x, y, MAIN_COLOR);
        if (patterns.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable(key("banner.no_layers")), x, y + 12, ALT_COLOR);
            return;
        }

        int first = Math.max(0, patterns.size() - 7);
        for (int i = first; i < patterns.size(); i++) {
            CompoundTag patternTag = patterns.getCompound(i);
            DyeColor color = DyeColor.byId(patternTag.getInt(BANNER_COLOR_TAG));
            BannerPatternEntry entry = getBannerPatternEntry(patternTag.getString(BANNER_PATTERN_TAG));
            Component name = entry == null
                    ? Component.literal(patternTag.getString(BANNER_PATTERN_TAG))
                    : getBannerPatternName(entry, color);
            String text = (i + 1) + ". " + name.getString();
            guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(text, 150), x, y + 12 + (i - first) * 10, MAIN_COLOR);
        }
    }

    private void swapBannerAndShield() {
        if (!isBannerEditableItem(this.previewStack)) {
            return;
        }

        DyeColor baseColor = getBannerBaseColor();
        if (this.previewStack.is(Items.SHIELD)) {
            replacePreviewItem(BannerPatternCatalog.ITEMS_BY_DYE[baseColor.getId()]);
            removeBannerBaseColorTag();
        } else {
            replacePreviewItem(Items.SHIELD);
            CompoundTag tag = this.previewStack.getOrCreateTag();
            CompoundTag blockEntity = getOrCreateBannerBlockEntityTag();
            blockEntity.putInt(BANNER_BASE_TAG, baseColor.getId());
            cleanupBlockEntityTag(tag, blockEntity);
        }

        this.bannerBaseColor = getBannerBaseColor().getId();
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_banner_swapped"));
        rebuildWidgets();
    }

    private void setBannerBaseColor(DyeColor color) {
        this.bannerBaseColor = color.getId();
        if (this.previewStack.is(Items.SHIELD)) {
            CompoundTag tag = this.previewStack.getOrCreateTag();
            CompoundTag blockEntity = getOrCreateBannerBlockEntityTag();
            blockEntity.putInt(BANNER_BASE_TAG, color.getId());
            cleanupBlockEntityTag(tag, blockEntity);
        } else if (this.previewStack.getItem() instanceof BannerItem) {
            replacePreviewItem(BannerPatternCatalog.ITEMS_BY_DYE[color.getId()]);
            removeBannerBaseColorTag();
        }
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    private void replacePreviewItem(Item item) {
        CompoundTag tag = this.previewStack.getTag() == null ? null : this.previewStack.getTag().copy();
        int count = this.previewStack.getCount() <= 0 ? 1 : this.previewStack.getCount();
        int damage = this.previewStack.getDamageValue();
        this.previewStack = new ItemStack(item, count);
        this.previewStack.setTag(tag);
        this.damageValue = Integer.toString(Math.min(getDamageMaxForField(this.previewStack), Math.max(0, damage)));
        this.previewStack.setDamageValue(Integer.parseInt(this.damageValue));
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        this.itemIdValue = id == null ? "air" : stripMinecraftNamespace(id);
        this.attributeSlot = getDefaultAttributeSlot(this.previewStack);
    }

    private void removeBannerBaseColorTag() {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null) {
            return;
        }

        CompoundTag blockEntity = tag.getCompound(BLOCK_ENTITY_TAG);
        blockEntity.remove(BANNER_BASE_TAG);
        cleanupBlockEntityTag(tag, blockEntity);
    }

    private DyeColor getBannerBaseColor() {
        if (this.previewStack.getItem() instanceof BannerItem bannerItem) {
            return bannerItem.getColor();
        }
        if (this.previewStack.is(Items.SHIELD)) {
            CompoundTag blockEntity = this.previewStack.getTagElement(BLOCK_ENTITY_TAG);
            if (blockEntity != null && blockEntity.contains(BANNER_BASE_TAG, Tag.TAG_INT)) {
                return DyeColor.byId(blockEntity.getInt(BANNER_BASE_TAG));
            }
        }
        return DyeColor.WHITE;
    }

    private DyeColor getBannerPatternColor() {
        this.bannerPatternColor = Mth.positiveModulo(this.bannerPatternColor, DyeColor.values().length);
        return DyeColor.byId(this.bannerPatternColor);
    }

    private Component getDyeColorName(DyeColor color) {
        return Component.translatable("color.minecraft." + color.getName());
    }

    private int getBannerPatternCount() {
        return getBannerPatterns().size();
    }

    private ListTag getBannerPatterns() {
        CompoundTag blockEntity = this.previewStack.getTagElement(BLOCK_ENTITY_TAG);
        if (blockEntity == null || !blockEntity.contains(BANNER_PATTERNS_TAG, Tag.TAG_LIST)) {
            return new ListTag();
        }
        return blockEntity.getList(BANNER_PATTERNS_TAG, Tag.TAG_COMPOUND);
    }

    private CompoundTag getOrCreateBannerBlockEntityTag() {
        CompoundTag tag = this.previewStack.getOrCreateTag();
        CompoundTag blockEntity = tag.getCompound(BLOCK_ENTITY_TAG);
        tag.put(BLOCK_ENTITY_TAG, blockEntity);
        return blockEntity;
    }

    private BannerPatternEntry getBannerPatternEntry(String hash) {
        for (BannerPatternEntry entry : BannerPatternCatalog.PATTERNS) {
            if (entry.hash().equals(hash)) {
                return entry;
            }
        }
        return null;
    }

    private void applySelectedSpawnEggEntity() {
        SpawnEggEntityEntry entry = getSelectedSpawnEggEntityEntry();
        if (entry == null) {
            this.status = Component.translatable(key("spawnegg.no_match"));
            return;
        }

        writeSpawnEggEntityId(entry);
        this.status = Component.translatable(messageKey("editor_spawn_egg_entity_updated"), getSpawnEggEntityName(entry));
        readSpawnEggFieldsFromStack(this.previewStack);
        rebuildWidgets();
    }

    private void syncSpawnEggToSelectedEntityItem() {
        SpawnEggEntityEntry entry = getSelectedSpawnEggEntityEntry();
        if (entry == null) {
            this.status = Component.translatable(key("spawnegg.no_match"));
            return;
        }

        SpawnEggItem eggItem = SpawnEggItem.byId(entry.type());
        if (eggItem == null) {
            this.status = Component.translatable(messageKey("editor_spawn_egg_no_matching_item"), getSpawnEggEntityName(entry));
            return;
        }

        replacePreviewItem(eggItem);
        writeSpawnEggEntityId(entry);
        readMainFieldsFromStack(this.previewStack);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_spawn_egg_synced"), getSpawnEggEntityName(entry));
        rebuildWidgets();
    }

    private void writeSpawnEggEntityId(SpawnEggEntityEntry entry) {
        CompoundTag entityTag = getOrCreateSpawnEggEntityTag();
        entityTag.putString(ENTITY_ID_TAG, entry.id().toString());
        cleanupSpawnEggEntityTag(entityTag);
    }

    private void clearSpawnEggEntityTag() {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null || !tag.contains(ENTITY_TAG, Tag.TAG_COMPOUND)) {
            return;
        }

        tag.remove(ENTITY_TAG);
        cleanupEmptyTag();
        this.spawnEggCustomNameValue = "";
        this.spawnEggNumberValueOverrides.clear();
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_spawn_egg_tag_cleared"));
        readSpawnEggFieldsFromStack(this.previewStack);
        rebuildWidgets();
    }

    private void toggleSpawnEggBoolean(SpawnEggTagRow row) {
        if (!isSpawnEggItem(this.previewStack)) {
            return;
        }

        CompoundTag entityTag = getOrCreateSpawnEggEntityTag();
        if (getSpawnEggBooleanValue(row)) {
            entityTag.remove(row.tagKey());
        } else {
            entityTag.putBoolean(row.tagKey(), true);
        }
        cleanupSpawnEggEntityTag(entityTag);
        this.status = Component.translatable(messageKey("editor_spawn_egg_field_updated"),
                Component.translatable(key("spawnegg." + row.translationSuffix())));
        rebuildWidgets();
    }

    private void applySpawnEggCustomName(String value) {
        if (!isSpawnEggItem(this.previewStack)) {
            return;
        }

        CompoundTag entityTag = getOrCreateSpawnEggEntityTag();
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            entityTag.remove(ENTITY_CUSTOM_NAME_TAG);
        } else {
            entityTag.putString(ENTITY_CUSTOM_NAME_TAG, Component.Serializer.toJson(Component.literal(value)));
        }
        cleanupSpawnEggEntityTag(entityTag);
    }

    private void applySpawnEggNumber(SpawnEggTagRow row, String value) {
        if (!isSpawnEggItem(this.previewStack)) {
            return;
        }

        String normalized = value == null ? "" : value.trim();
        this.spawnEggNumberValueOverrides.put(row.tagKey(), normalized);
        CompoundTag entityTag = getOrCreateSpawnEggEntityTag();
        if (normalized.isEmpty()) {
            entityTag.remove(row.tagKey());
            this.spawnEggNumberValueOverrides.remove(row.tagKey());
            cleanupSpawnEggEntityTag(entityTag);
            return;
        }
        if (isPartialSpawnEggNumber(normalized)) {
            return;
        }

        try {
            double parsed = row.numberType() == SpawnEggNumberType.FLOAT
                    ? Double.parseDouble(normalized)
                    : Long.parseLong(normalized);
            if (parsed < row.minValue() || parsed > row.maxValue()) {
                this.status = Component.translatable(messageKey("editor_spawn_egg_invalid_number"),
                        Component.translatable(key("spawnegg." + row.translationSuffix())),
                        formatSpawnEggNumber(row.minValue()),
                        formatSpawnEggNumber(row.maxValue()));
                return;
            }

            switch (row.numberType()) {
                case BYTE -> entityTag.putByte(row.tagKey(), (byte) parsed);
                case SHORT -> entityTag.putShort(row.tagKey(), (short) parsed);
                case INT -> entityTag.putInt(row.tagKey(), (int) parsed);
                case FLOAT -> entityTag.putFloat(row.tagKey(), (float) parsed);
            }
            cleanupSpawnEggEntityTag(entityTag);
        } catch (NumberFormatException exception) {
            this.status = Component.translatable(messageKey("editor_spawn_egg_invalid_number"),
                    Component.translatable(key("spawnegg." + row.translationSuffix())),
                    formatSpawnEggNumber(row.minValue()),
                    formatSpawnEggNumber(row.maxValue()));
        }
    }

    private CompoundTag getOrCreateSpawnEggEntityTag() {
        CompoundTag tag = this.previewStack.getOrCreateTag();
        CompoundTag entityTag = tag.getCompound(ENTITY_TAG);
        tag.put(ENTITY_TAG, entityTag);
        return entityTag;
    }

    private void cleanupSpawnEggEntityTag(CompoundTag entityTag) {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null) {
            return;
        }

        if (entityTag.isEmpty()) {
            tag.remove(ENTITY_TAG);
        } else {
            tag.put(ENTITY_TAG, entityTag);
        }
        cleanupEmptyTag();
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    private Component getSpawnEggBooleanText(SpawnEggTagRow row) {
        return Component.translatable(key("spawnegg.option_state"),
                Component.translatable(key("spawnegg." + row.translationSuffix())),
                Component.translatable(key("spawnegg.state." + (getSpawnEggBooleanValue(row) ? 1 : 0))));
    }

    private boolean getSpawnEggBooleanValue(SpawnEggTagRow row) {
        CompoundTag entityTag = this.previewStack.getTagElement(ENTITY_TAG);
        return entityTag != null && entityTag.contains(row.tagKey(), Tag.TAG_BYTE) && entityTag.getBoolean(row.tagKey());
    }

    private String getSpawnEggNumberValue(SpawnEggTagRow row) {
        String override = this.spawnEggNumberValueOverrides.get(row.tagKey());
        if (override != null) {
            return override;
        }

        CompoundTag entityTag = this.previewStack.getTagElement(ENTITY_TAG);
        if (entityTag == null || !entityTag.contains(row.tagKey())) {
            return "";
        }

        return switch (row.numberType()) {
            case BYTE -> Byte.toString(entityTag.getByte(row.tagKey()));
            case SHORT -> Short.toString(entityTag.getShort(row.tagKey()));
            case INT -> Integer.toString(entityTag.getInt(row.tagKey()));
            case FLOAT -> Float.toString(entityTag.getFloat(row.tagKey()));
        };
    }

    private boolean isAllowedSpawnEggNumber(String value, SpawnEggNumberType type) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return type == SpawnEggNumberType.FLOAT
                ? value.matches("-?\\d*(\\.\\d*)?")
                : value.matches("-?\\d*");
    }

    private boolean isPartialSpawnEggNumber(String value) {
        return "-".equals(value) || ".".equals(value) || "-.".equals(value);
    }

    private String formatSpawnEggNumber(double value) {
        if (value == (long) value) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    private void setSpawnEggEntityScroll(int value) {
        List<SpawnEggEntityEntry> entities = getFilteredSpawnEggEntities();
        int maxScroll = Math.max(0, entities.size() - SPAWN_EGG_ENTITY_ROWS);
        this.spawnEggEntityScroll = Mth.clamp(value, 0, maxScroll);
        clampSpawnEggEntitySelection(entities);
        if (!entities.isEmpty()) {
            int lastVisible = Math.min(entities.size() - 1, this.spawnEggEntityScroll + SPAWN_EGG_ENTITY_ROWS - 1);
            this.selectedSpawnEggEntityIndex = Mth.clamp(this.selectedSpawnEggEntityIndex, this.spawnEggEntityScroll, lastVisible);
        }
    }

    private void setSpawnEggTagScroll(int value) {
        int maxScroll = Math.max(0, getSpawnEggTagRows().size() - SPAWN_EGG_TAG_ROWS);
        this.spawnEggTagScroll = Mth.clamp(value, 0, maxScroll);
    }

    private void cycleSelectedSpawnEggEntity(int direction) {
        List<SpawnEggEntityEntry> entities = getFilteredSpawnEggEntities();
        if (entities.isEmpty()) {
            return;
        }

        this.selectedSpawnEggEntityIndex = Mth.positiveModulo(this.selectedSpawnEggEntityIndex + direction, entities.size());
        this.spawnEggTagScroll = 0;
        scrollSpawnEggSelectionIntoView(entities);
        rebuildWidgets();
    }

    private void scrollSpawnEggSelectionIntoView(List<SpawnEggEntityEntry> entities) {
        clampSpawnEggEntitySelection(entities);
        if (entities.isEmpty()) {
            return;
        }

        if (this.selectedSpawnEggEntityIndex < this.spawnEggEntityScroll) {
            this.spawnEggEntityScroll = this.selectedSpawnEggEntityIndex;
        } else if (this.selectedSpawnEggEntityIndex >= this.spawnEggEntityScroll + SPAWN_EGG_ENTITY_ROWS) {
            this.spawnEggEntityScroll = this.selectedSpawnEggEntityIndex - SPAWN_EGG_ENTITY_ROWS + 1;
        }
        this.spawnEggEntityScroll = Mth.clamp(this.spawnEggEntityScroll, 0, Math.max(0, entities.size() - SPAWN_EGG_ENTITY_ROWS));
    }

    private void clampSpawnEggEntitySelection(List<SpawnEggEntityEntry> entities) {
        if (entities.isEmpty()) {
            this.selectedSpawnEggEntityIndex = 0;
            this.spawnEggEntityScroll = 0;
            return;
        }

        this.selectedSpawnEggEntityIndex = Mth.clamp(this.selectedSpawnEggEntityIndex, 0, entities.size() - 1);
        this.spawnEggEntityScroll = Mth.clamp(this.spawnEggEntityScroll, 0, Math.max(0, entities.size() - SPAWN_EGG_ENTITY_ROWS));
    }

    private int getSpawnEggEntityRowY(int row) {
        return 58 + row * 10;
    }

    private int getSpawnEggTagRowY(int row) {
        return 138 + row * SPAWN_EGG_TAG_ROW_HEIGHT;
    }

    private int getSpawnEggControlsX() {
        int width = getSpawnEggControlsWidth();
        return Math.max(this.midX + 76, this.width - width - 10);
    }

    private int getSpawnEggControlsWidth() {
        return 132;
    }

    private SpawnEggEntityEntry getSelectedSpawnEggEntityEntry() {
        List<SpawnEggEntityEntry> entities = getFilteredSpawnEggEntities();
        clampSpawnEggEntitySelection(entities);
        if (entities.isEmpty()) {
            return null;
        }
        return entities.get(this.selectedSpawnEggEntityIndex);
    }

    private List<SpawnEggEntityEntry> getFilteredSpawnEggEntities() {
        String filter = this.spawnEggEntityFilterValue == null ? "" : this.spawnEggEntityFilterValue.trim().toLowerCase(Locale.ROOT);
        List<SpawnEggEntityEntry> entities = new ArrayList<>();
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            if (!type.canSummon()) {
                continue;
            }

            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
            if (id == null) {
                continue;
            }

            String idString = id.toString().toLowerCase(Locale.ROOT);
            String path = id.getPath().toLowerCase(Locale.ROOT);
            String name = type.getDescription().getString().toLowerCase(Locale.ROOT);
            if (filter.isEmpty() || idString.contains(filter) || path.contains(filter) || name.contains(filter)) {
                entities.add(new SpawnEggEntityEntry(id, type));
            }
        }

        entities.sort(Comparator
                .comparing((SpawnEggEntityEntry entry) -> entry.type().getDescription().getString(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(entry -> entry.id().toString()));
        return entities;
    }

    private String formatSpawnEggEntityEntry(SpawnEggEntityEntry entry) {
        return getSpawnEggEntityName(entry).getString() + " (" + stripMinecraftNamespace(entry.id()) + ")";
    }

    private Component getSpawnEggEntityName(SpawnEggEntityEntry entry) {
        return entry.type().getDescription();
    }

    private Component getCurrentSpawnEggEntityName() {
        EntityType<?> type = getCurrentSpawnEggEntityType(this.previewStack);
        if (type != null) {
            return type.getDescription();
        }

        String rawId = getSpawnEggEntityIdOverride(this.previewStack);
        return rawId.isEmpty() ? Component.translatable(key("spawnegg.default_entity")) : Component.literal(rawId);
    }

    private EntityType<?> getCurrentSpawnEggEntityType(ItemStack stack) {
        String rawId = getSpawnEggEntityIdOverride(stack);
        if (!rawId.isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(rawId);
            EntityType<?> type = id == null ? null : ForgeRegistries.ENTITY_TYPES.getValue(id);
            if (type != null) {
                return type;
            }
        }

        if (stack.getItem() instanceof SpawnEggItem spawnEggItem) {
            return spawnEggItem.getType(stack.getTag());
        }
        return null;
    }

    private String getSpawnEggEntityIdOverride(ItemStack stack) {
        CompoundTag entityTag = stack.getTagElement(ENTITY_TAG);
        if (entityTag != null && entityTag.contains(ENTITY_ID_TAG, Tag.TAG_STRING)) {
            return entityTag.getString(ENTITY_ID_TAG);
        }
        return "";
    }

    private List<SpawnEggTagRow> getSpawnEggTagRows() {
        SpawnEggEntityEntry entry = getSelectedSpawnEggEntityEntry();
        String path = entry == null ? "" : entry.id().getPath();
        List<SpawnEggTagRow> rows = new ArrayList<>(SpawnEggTagRows.GENERAL);
        rows.addAll(SpawnEggTagRows.forEntity(path));
        return rows;
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
        List<Enchantment> matchingEnchantments = new ArrayList<>();
        List<Enchantment> applicableMatchingEnchantments = new ArrayList<>();
        boolean hasApplicableEnchantments = false;
        String filter = this.enchantFilterValue == null ? "" : this.enchantFilterValue.trim().toLowerCase(Locale.ROOT);
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS.getValues()) {
            boolean applicable = canApplyEnchantment(stack, enchantment);
            hasApplicableEnchantments |= applicable;
            ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
            String name = Component.translatable(enchantment.getDescriptionId()).getString().toLowerCase(Locale.ROOT);
            String idString = id == null ? "" : id.toString().toLowerCase(Locale.ROOT);
            if (filter.isEmpty() || name.contains(filter) || idString.contains(filter)) {
                matchingEnchantments.add(enchantment);
                if (applicable) {
                    applicableMatchingEnchantments.add(enchantment);
                }
            }
        }

        List<Enchantment> enchantments = this.showAllEnchantments || !hasApplicableEnchantments
                ? matchingEnchantments
                : applicableMatchingEnchantments;
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
        for (EditBox box : this.signBoxes) {
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
        return NbtFormatter.prettyLines(this.previewStack.getTag());
    }

    private List<NbtRow> buildNbtRows() {
        return NbtFormatter.rows(this.previewStack.getTag(), this.expandedNbtPaths);
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

        readSignFieldsFromStack(stack);
        readBookFieldsFromStack(stack);
        readHeadFieldsFromStack(stack);
        readBannerFieldsFromStack(stack);
        readFireworkFieldsFromStack(stack);
        readContainerFieldsFromStack(stack);
        readSpawnEggFieldsFromStack(stack);
    }

    private String readLoreLine(String raw) {
        try {
            Component component = Component.Serializer.fromJson(raw);
            return component == null ? raw : component.getString();
        } catch (RuntimeException exception) {
            return raw;
        }
    }

    private void readSignFieldsFromStack(ItemStack stack) {
        Arrays.fill(this.signLineValues, "");
        this.signCommandValue = "";
        if (!isSignItem(stack)) {
            return;
        }

        CompoundTag blockEntity = stack.getTagElement(BLOCK_ENTITY_TAG);
        if (blockEntity == null) {
            return;
        }

        boolean readModernMessages = false;
        if (blockEntity.contains(SIGN_FRONT_TEXT_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag frontText = blockEntity.getCompound(SIGN_FRONT_TEXT_TAG);
            if (frontText.contains(SIGN_MESSAGES_TAG, Tag.TAG_LIST)) {
                ListTag messages = frontText.getList(SIGN_MESSAGES_TAG, Tag.TAG_STRING);
                for (int i = 0; i < SIGN_LINES && i < messages.size(); i++) {
                    readSignLine(i, messages.getString(i));
                }
                readModernMessages = !messages.isEmpty();
            }
        }

        if (!readModernMessages) {
            for (int i = 0; i < SIGN_LINES; i++) {
                String key = LEGACY_SIGN_TEXT_TAG_PREFIX + (i + 1);
                if (blockEntity.contains(key, Tag.TAG_STRING)) {
                    readSignLine(i, blockEntity.getString(key));
                }
            }
        }
    }

    private void readSignLine(int line, String raw) {
        Component component = readSerializedComponent(raw);
        this.signLineValues[line] = component.getString();
        if (line == 0) {
            ClickEvent clickEvent = component.getStyle().getClickEvent();
            if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                this.signCommandValue = clickEvent.getValue();
            }
        }
    }

    private void readBookFieldsFromStack(ItemStack stack) {
        this.bookTitleValue = "";
        this.bookAuthorValue = "";
        if (!isBookEditableItem(stack)) {
            this.rememberedSignedBookData = null;
            return;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }

        this.bookTitleValue = tag.getString(BOOK_TITLE_TAG);
        this.bookAuthorValue = tag.getString(BOOK_AUTHOR_TAG);
    }

    private void readHeadFieldsFromStack(ItemStack stack) {
        this.headOwnerValue = "";
        this.headUuidValue = "";
        this.headTextureValue = "";
        this.headTextureSignatureValue = "";
        if (!isPlayerHeadItem(stack)) {
            return;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }

        if (tag.contains(SKULL_OWNER_TAG, Tag.TAG_STRING)) {
            this.headOwnerValue = tag.getString(SKULL_OWNER_TAG);
            return;
        }

        if (!tag.contains(SKULL_OWNER_TAG, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag skullOwner = tag.getCompound(SKULL_OWNER_TAG);
        if (skullOwner.contains(SKULL_OWNER_NAME_TAG, Tag.TAG_STRING)) {
            this.headOwnerValue = skullOwner.getString(SKULL_OWNER_NAME_TAG);
        }
        if (skullOwner.hasUUID(SKULL_OWNER_ID_TAG)) {
            this.headUuidValue = skullOwner.getUUID(SKULL_OWNER_ID_TAG).toString();
        }
        CompoundTag properties = skullOwner.getCompound(SKULL_PROPERTIES_TAG);
        if (properties.contains(SKULL_TEXTURES_TAG, Tag.TAG_LIST)) {
            ListTag textures = properties.getList(SKULL_TEXTURES_TAG, Tag.TAG_COMPOUND);
            if (!textures.isEmpty()) {
                CompoundTag texture = textures.getCompound(0);
                this.headTextureValue = texture.getString(SKULL_TEXTURE_VALUE_TAG);
                this.headTextureSignatureValue = texture.getString(SKULL_TEXTURE_SIGNATURE_TAG);
            }
        }
    }

    private void readFireworkFieldsFromStack(ItemStack stack) {
        this.fireworkExplosionType = 0;
        this.fireworkColor = DyeColor.RED.getId();
        this.fireworkFadeColor = -1;
        this.fireworkFlicker = false;
        this.fireworkTrail = false;
        if (!isFireworkEditableItem(stack)) {
            return;
        }

        CompoundTag explosion = getFireworkExplosionForFields(stack);
        if (explosion == null) {
            return;
        }

        this.fireworkExplosionType = Mth.clamp(explosion.getByte(FIREWORK_TYPE_TAG), 0, FIREWORK_EXPLOSION_TYPES - 1);
        this.fireworkFlicker = explosion.getBoolean(FIREWORK_FLICKER_TAG);
        this.fireworkTrail = explosion.getBoolean(FIREWORK_TRAIL_TAG);

        int[] colors = explosion.getIntArray(FIREWORK_COLORS_TAG);
        if (colors.length > 0) {
            this.fireworkColor = getNearestFireworkDyeColorId(colors[0]);
        }
        int[] fadeColors = explosion.getIntArray(FIREWORK_FADE_COLORS_TAG);
        if (fadeColors.length > 0) {
            this.fireworkFadeColor = getNearestFireworkDyeColorId(fadeColors[0]);
        }
    }

    private void readContainerFieldsFromStack(ItemStack stack) {
        this.selectedContainerSlot = Mth.clamp(this.selectedContainerSlot, 0, CONTAINER_SIZE - 1);
        if (!isContainerEditableItem(stack)) {
            this.containerSlotNbtValue = "{}";
            return;
        }
        this.containerSlotNbtValue = getContainerSlotNbt(getContainerSlotItem(this.selectedContainerSlot));
    }

    private CompoundTag getFireworkExplosionForFields(ItemStack stack) {
        if (stack.is(Items.FIREWORK_STAR)) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(FIREWORK_EXPLOSION_TAG, Tag.TAG_COMPOUND)) {
                return tag.getCompound(FIREWORK_EXPLOSION_TAG);
            }
            return null;
        }

        CompoundTag fireworks = stack.getTagElement(FIREWORKS_TAG);
        if (fireworks == null || !fireworks.contains(FIREWORK_EXPLOSIONS_TAG, Tag.TAG_LIST)) {
            return null;
        }
        ListTag explosions = fireworks.getList(FIREWORK_EXPLOSIONS_TAG, Tag.TAG_COMPOUND);
        return explosions.isEmpty() ? null : explosions.getCompound(explosions.size() - 1);
    }

    private void readBannerFieldsFromStack(ItemStack stack) {
        this.bannerBaseColor = DyeColor.WHITE.getId();
        if (isBannerEditableItem(stack)) {
            this.bannerBaseColor = getBannerBaseColor().getId();
            ListTag patterns = getBannerPatterns();
            if (!patterns.isEmpty()) {
                this.bannerPatternColor = DyeColor.byId(patterns.getCompound(patterns.size() - 1).getInt(BANNER_COLOR_TAG)).getId();
            }
        }
        this.bannerPatternColor = Mth.positiveModulo(this.bannerPatternColor, DyeColor.values().length);
        clampBannerPatternSelection(getFilteredBannerPatterns());
    }

    private void readSpawnEggFieldsFromStack(ItemStack stack) {
        this.spawnEggCustomNameValue = "";
        this.spawnEggNumberValueOverrides.clear();
        this.spawnEggTagScroll = 0;
        if (!isSpawnEggItem(stack)) {
            return;
        }

        CompoundTag entityTag = stack.getTagElement(ENTITY_TAG);
        if (entityTag != null && entityTag.contains(ENTITY_CUSTOM_NAME_TAG, Tag.TAG_STRING)) {
            this.spawnEggCustomNameValue = readSerializedComponent(entityTag.getString(ENTITY_CUSTOM_NAME_TAG)).getString();
        }

        EntityType<?> type = getCurrentSpawnEggEntityType(stack);
        ResourceLocation id = type == null ? null : ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (id == null) {
            clampSpawnEggEntitySelection(getFilteredSpawnEggEntities());
            return;
        }

        List<SpawnEggEntityEntry> filtered = getFilteredSpawnEggEntities();
        for (int i = 0; i < filtered.size(); i++) {
            if (id.equals(filtered.get(i).id())) {
                this.selectedSpawnEggEntityIndex = i;
                scrollSpawnEggSelectionIntoView(filtered);
                return;
            }
        }
        clampSpawnEggEntitySelection(filtered);
    }

    private Component readSerializedComponent(String raw) {
        if (raw == null || raw.isBlank()) {
            return Component.empty();
        }
        try {
            Component component = Component.Serializer.fromJson(raw);
            return component == null ? Component.literal(raw) : component;
        } catch (RuntimeException exception) {
            try {
                Component component = Component.Serializer.fromJsonLenient(raw);
                return component == null ? Component.literal(raw) : component;
            } catch (RuntimeException ignored) {
                return Component.literal(raw);
            }
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
        for (int i = 0; i < SIGN_LINES && i < this.signBoxes.size(); i++) {
            this.signLineValues[i] = this.signBoxes.get(i).getValue();
        }
        if (this.signCommandBox != null) {
            this.signCommandValue = this.signCommandBox.getValue();
        }
        if (this.bookTitleBox != null) {
            this.bookTitleValue = this.bookTitleBox.getValue();
        }
        if (this.bookAuthorBox != null) {
            this.bookAuthorValue = this.bookAuthorBox.getValue();
        }
        if (this.headOwnerBox != null) {
            this.headOwnerValue = this.headOwnerBox.getValue();
        }
        if (this.headUuidBox != null) {
            this.headUuidValue = this.headUuidBox.getValue();
        }
        if (this.headTextureBox != null) {
            this.headTextureValue = this.headTextureBox.getValue();
        }
        if (this.headTextureSignatureBox != null) {
            this.headTextureSignatureValue = this.headTextureSignatureBox.getValue();
        }
        if (this.containerSlotNbtBox != null) {
            this.containerSlotNbtValue = this.containerSlotNbtBox.getValue();
        }
        if (this.bannerPatternFilterBox != null) {
            this.bannerPatternFilterValue = this.bannerPatternFilterBox.getValue();
        }
        if (this.spawnEggEntityFilterBox != null) {
            this.spawnEggEntityFilterValue = this.spawnEggEntityFilterBox.getValue();
        }
        if (this.spawnEggCustomNameBox != null) {
            this.spawnEggCustomNameValue = this.spawnEggCustomNameBox.getValue();
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
        return !stack.isEmpty();
    }

    private static boolean isPotionItem(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION) || stack.is(Items.TIPPED_ARROW);
    }

    private static boolean isMapItem(ItemStack stack) {
        return stack.is(Items.MAP) || stack.is(Items.FILLED_MAP);
    }

    private static boolean isSignItem(ItemStack stack) {
        return stack.getItem() instanceof SignItem;
    }

    private static boolean isBannerEditableItem(ItemStack stack) {
        return stack.getItem() instanceof BannerItem || stack.is(Items.SHIELD);
    }

    private static boolean isSpawnEggItem(ItemStack stack) {
        return stack.getItem() instanceof SpawnEggItem;
    }

    private static boolean isPlayerHeadItem(ItemStack stack) {
        return stack.getItem() instanceof PlayerHeadItem;
    }

    private static boolean isArmorStandItem(ItemStack stack) {
        return stack.is(Items.ARMOR_STAND);
    }

    private static boolean isFireworkEditableItem(ItemStack stack) {
        return stack.is(Items.FIREWORK_ROCKET) || stack.is(Items.FIREWORK_STAR);
    }

    private static boolean isContainerEditableItem(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        Block block = blockItem.getBlock();
        return block instanceof ChestBlock || block instanceof ShulkerBoxBlock;
    }

    private static boolean isBookEditableItem(ItemStack stack) {
        return stack.is(Items.WRITTEN_BOOK) || stack.is(Items.WRITABLE_BOOK);
    }

    private int getBookGeneration() {
        CompoundTag tag = this.previewStack.getTag();
        return tag == null ? 0 : Mth.clamp(tag.getInt(BOOK_GENERATION_TAG), 0, MAX_BOOK_GENERATION);
    }

    private int getBookPageCount() {
        CompoundTag tag = this.previewStack.getTag();
        return tag == null ? 0 : tag.getList(BOOK_PAGES_TAG, Tag.TAG_STRING).size();
    }

    private Component getBookResolvedText() {
        CompoundTag tag = this.previewStack.getTag();
        boolean resolved = tag != null && tag.getBoolean(BOOK_RESOLVED_TAG);
        return Component.translatable(key("book.resolved." + (resolved ? 1 : 0)));
    }

    private Component getBookSignButtonText() {
        if (this.previewStack.is(Items.WRITTEN_BOOK)) {
            return Component.translatable(key("book.unsign"));
        }
        return Component.translatable(key(this.rememberedSignedBookData == null ? "book.sign" : "book.resign"));
    }

    private int getFireworkFlight() {
        CompoundTag fireworks = this.previewStack.getTagElement(FIREWORKS_TAG);
        if (fireworks == null) {
            return 1;
        }
        return Mth.clamp(fireworks.getByte(FIREWORK_FLIGHT_TAG), 1, MAX_FIREWORK_FLIGHT);
    }

    private int getFireworkExplosionCount() {
        if (this.previewStack.is(Items.FIREWORK_STAR)) {
            CompoundTag tag = this.previewStack.getTag();
            return tag != null && tag.contains(FIREWORK_EXPLOSION_TAG, Tag.TAG_COMPOUND) ? 1 : 0;
        }

        CompoundTag fireworks = this.previewStack.getTagElement(FIREWORKS_TAG);
        if (fireworks == null || !fireworks.contains(FIREWORK_EXPLOSIONS_TAG, Tag.TAG_LIST)) {
            return 0;
        }
        return fireworks.getList(FIREWORK_EXPLOSIONS_TAG, Tag.TAG_COMPOUND).size();
    }

    private boolean hasFireworkData() {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null) {
            return false;
        }
        if (this.previewStack.is(Items.FIREWORK_ROCKET)) {
            return tag.contains(FIREWORKS_TAG, Tag.TAG_COMPOUND) && !tag.getCompound(FIREWORKS_TAG).isEmpty();
        }
        return tag.contains(FIREWORK_EXPLOSION_TAG, Tag.TAG_COMPOUND) && !tag.getCompound(FIREWORK_EXPLOSION_TAG).isEmpty();
    }

    private Component getFireworkTypeName(int type) {
        return Component.translatable(key("firework.type." + Mth.clamp(type, 0, FIREWORK_EXPLOSION_TYPES - 1)));
    }

    private Component getFireworkFadeColorText() {
        if (this.fireworkFadeColor < 0) {
            return Component.translatable(key("firework.fade.none"));
        }
        return getDyeColorName(getFireworkDyeColor(this.fireworkFadeColor));
    }

    private DyeColor getFireworkDyeColor(int colorId) {
        return DyeColor.byId(Mth.positiveModulo(colorId, DyeColor.values().length));
    }

    private static int getFireworkRgb(DyeColor color) {
        return color.getFireworkColor();
    }

    private static int getNearestFireworkDyeColorId(int rgb) {
        int normalized = rgb & 0xFFFFFF;
        DyeColor closest = DyeColor.WHITE;
        int closestDistance = Integer.MAX_VALUE;
        for (DyeColor color : DyeColor.values()) {
            int dyeRgb = getFireworkRgb(color);
            int red = getRed(normalized) - getRed(dyeRgb);
            int green = getGreen(normalized) - getGreen(dyeRgb);
            int blue = getBlue(normalized) - getBlue(dyeRgb);
            int distance = red * red + green * green + blue * blue;
            if (distance < closestDistance) {
                closest = color;
                closestDistance = distance;
            }
        }
        return closest.getId();
    }

    private static String normalizeHeadText(String value) {
        return value == null ? "" : value.trim();
    }

    private static UUID parseUuidOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            return null;
        }
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

}

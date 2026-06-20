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
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.BarrelBlock;
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
abstract class ItemEditorScreenState extends Screen {
    protected static final int MAX_COUNT = 64;
    protected static final int MAX_ENCHANTMENT_LEVEL = 32767;
    protected static final int MAX_POTION_LEVEL = 127;
    protected static final int MAX_POTION_SECONDS = 99999;
    protected static final int MAX_ATTRIBUTE_INTEGER = 99999999;
    protected static final int FIELD_HEIGHT = 20;
    protected static final int OLD_BUTTON_WIDTH = 60;
    protected static final int OLD_BUTTON_HEIGHT = 20;
    protected static final int ITEM_SIZE = 16;
    protected static final int RING_ICON_HIT_RADIUS = 10;
    protected static final int RING_HOVER_WIDTH = 16;
    protected static final int CENTER_HIT_RADIUS = 15;
    protected static final int MAIN_COLOR = InfinityEditorButton.MAIN_COLOR;
    protected static final int ALT_COLOR = InfinityEditorButton.ALT_COLOR;
    protected static final int CONTRAST_COLOR = InfinityEditorButton.CONTRAST_COLOR;
    protected static final int SIDEBAR_WIDTH_MIN = 136;
    protected static final int SIDEBAR_WIDTH_MAX = 168;
    protected static final int SIDEBAR_SAFE_MARGIN = 12;
    protected static final int SIDEBAR_CONTENT_GAP = 14;
    protected static final int SIDEBAR_BUTTON_HEIGHT = 22;
    protected static final int SIDEBAR_PANEL_COLOR = 0xE0100E1C;
    protected static final int SIDEBAR_CARD_COLOR = 0xC81B1830;
    protected static final int SIDEBAR_CARD_SOFT_COLOR = 0x7A241F3A;
    protected static final int SIDEBAR_BORDER_COLOR = 0xAA7E5CC8;
    protected static final int SIDEBAR_ACCENT_COLOR = 0xFF2EC8FF;
    protected static final int SIDEBAR_MUTED_COLOR = 0xFFB9A8CE;
    protected static final int BAD_RED = 0xFFF44262;
    protected static final int GOOD_GREEN = 0xFF32CC64;
    protected static final int CYAN = 0xFF00FFFF;
    protected static final String ITEM_ENCHANTMENTS_TAG = "Enchantments";
    protected static final String BOOK_ENCHANTMENTS_TAG = "StoredEnchantments";
    protected static final String DISPLAY_TAG = "display";
    protected static final String LORE_TAG = "Lore";
    protected static final String HIDE_FLAGS_TAG = "HideFlags";
    protected static final String CUSTOM_POTION_EFFECTS_TAG = "CustomPotionEffects";
    protected static final String ATTRIBUTE_MODIFIERS_TAG = "AttributeModifiers";
    protected static final String CUSTOM_POTION_COLOR_TAG = PotionUtils.TAG_CUSTOM_POTION_COLOR;
    protected static final String MAP_COLOR_TAG = "MapColor";
    protected static final int SIGN_LINES = 4;
    protected static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    protected static final String SPAWNER_BLOCK_ENTITY_ID = "minecraft:mob_spawner";
    protected static final String SPAWNER_SPAWN_DATA_TAG = "SpawnData";
    protected static final String SPAWNER_ENTITY_TAG = "entity";
    protected static final String SPAWNER_SPAWN_POTENTIALS_TAG = "SpawnPotentials";
    protected static final String SPAWNER_POTENTIAL_DATA_TAG = "data";
    protected static final String SPAWNER_POTENTIAL_LEGACY_ENTITY_TAG = "Entity";
    protected static final String SPAWNER_CUSTOM_SPAWN_RULES_TAG = "custom_spawn_rules";
    protected static final String SIGN_FRONT_TEXT_TAG = "front_text";
    protected static final String SIGN_MESSAGES_TAG = "messages";
    protected static final String SIGN_FILTERED_MESSAGES_TAG = "filtered_messages";
    protected static final String SIGN_COLOR_TAG = "color";
    protected static final String SIGN_GLOWING_TEXT_TAG = "has_glowing_text";
    protected static final String LEGACY_SIGN_TEXT_TAG_PREFIX = "Text";
    protected static final String BANNER_PATTERNS_TAG = "Patterns";
    protected static final String BANNER_PATTERN_TAG = "Pattern";
    protected static final String BANNER_COLOR_TAG = "Color";
    protected static final String BANNER_BASE_TAG = "Base";
    protected static final int BANNER_PATTERN_ROWS = 8;
    protected static final String BOOK_TITLE_TAG = "title";
    protected static final String BOOK_FILTERED_TITLE_TAG = "filtered_title";
    protected static final String BOOK_AUTHOR_TAG = "author";
    protected static final String BOOK_GENERATION_TAG = "generation";
    protected static final String BOOK_RESOLVED_TAG = "resolved";
    protected static final String BOOK_PAGES_TAG = "pages";
    protected static final String BOOK_FILTERED_PAGES_TAG = "filtered_pages";
    protected static final int MAX_BOOK_GENERATION = WrittenBookItem.MAX_GENERATION;
    protected static final String SKULL_OWNER_TAG = "SkullOwner";
    protected static final String SKULL_OWNER_ID_TAG = "Id";
    protected static final String SKULL_OWNER_NAME_TAG = "Name";
    protected static final String SKULL_PROPERTIES_TAG = "Properties";
    protected static final String SKULL_TEXTURES_TAG = "textures";
    protected static final String SKULL_TEXTURE_VALUE_TAG = "Value";
    protected static final String SKULL_TEXTURE_SIGNATURE_TAG = "Signature";
    protected static final String ENTITY_TAG = "EntityTag";
    protected static final String ENTITY_ID_TAG = "id";
    protected static final String ENTITY_CUSTOM_NAME_TAG = "CustomName";
    protected static final String ENTITY_OWNER_TAG = "Owner";
    protected static final String VILLAGER_DATA_TAG = "VillagerData";
    protected static final String VILLAGER_TYPE_TAG = "type";
    protected static final String VILLAGER_PROFESSION_TAG = "profession";
    protected static final String VILLAGER_LEVEL_TAG = "level";
    protected static final String DEFAULT_VILLAGER_TYPE = "minecraft:plains";
    protected static final String DEFAULT_VILLAGER_PROFESSION = "minecraft:farmer";
    protected static final int DEFAULT_VILLAGER_LEVEL = 1;
    protected static final String ARMOR_STAND_SHOW_ARMS_TAG = "ShowArms";
    protected static final String ARMOR_STAND_SMALL_TAG = "Small";
    protected static final String ARMOR_STAND_INVISIBLE_TAG = "Invisible";
    protected static final String ARMOR_STAND_NO_BASE_PLATE_TAG = "NoBasePlate";
    protected static final String ARMOR_STAND_MARKER_TAG = "Marker";
    protected static final String ARMOR_STAND_NO_GRAVITY_TAG = "NoGravity";
    protected static final String ARMOR_STAND_INVULNERABLE_TAG = "Invulnerable";
    protected static final String FIREWORKS_TAG = "Fireworks";
    protected static final String FIREWORK_FLIGHT_TAG = "Flight";
    protected static final String FIREWORK_EXPLOSIONS_TAG = "Explosions";
    protected static final String FIREWORK_EXPLOSION_TAG = "Explosion";
    protected static final String FIREWORK_TYPE_TAG = "Type";
    protected static final String FIREWORK_COLORS_TAG = "Colors";
    protected static final String FIREWORK_FADE_COLORS_TAG = "FadeColors";
    protected static final String FIREWORK_FLICKER_TAG = "Flicker";
    protected static final String FIREWORK_TRAIL_TAG = "Trail";
    protected static final int MAX_FIREWORK_FLIGHT = 4;
    protected static final int FIREWORK_EXPLOSION_TYPES = 5;
    protected static final FireworkRocketItem.Shape[] FIREWORK_SHAPES = {
            FireworkRocketItem.Shape.SMALL_BALL,
            FireworkRocketItem.Shape.LARGE_BALL,
            FireworkRocketItem.Shape.STAR,
            FireworkRocketItem.Shape.CREEPER,
            FireworkRocketItem.Shape.BURST
    };
    protected static final String CONTAINER_ITEMS_TAG = "Items";
    protected static final String CONTAINER_SLOT_TAG = "Slot";
    protected static final int CONTAINER_ROWS = 3;
    protected static final int CONTAINER_COLUMNS = 9;
    protected static final int CONTAINER_SIZE = CONTAINER_ROWS * CONTAINER_COLUMNS;
    protected static final int CONTAINER_SLOT_PIXEL_SIZE = 18;
    protected static final int SPAWN_EGG_ENTITY_ROWS = 8;
    protected static final int SPAWN_EGG_TAG_ROWS = 8;
    protected static final int SPAWN_EGG_TAG_ROW_HEIGHT = 24;
    protected static final int SPAWN_EGG_OWNER_MAX_LENGTH = 128;
    protected static final String OFFERS_TAG = "Offers";
    protected static final String RECIPES_TAG = "Recipes";
    protected static final String TRADE_BUY_TAG = "buy";
    protected static final String TRADE_BUY_B_TAG = "buyB";
    protected static final String TRADE_SELL_TAG = "sell";
    protected static final String TRADE_USES_TAG = "uses";
    protected static final String TRADE_MAX_USES_TAG = "maxUses";
    protected static final String TRADE_REWARD_EXP_TAG = "rewardExp";
    protected static final String TRADE_XP_TAG = "xp";
    protected static final String TRADE_PRICE_MULTIPLIER_TAG = "priceMultiplier";
    protected static final String TRADE_SPECIAL_PRICE_TAG = "specialPrice";
    protected static final String TRADE_DEMAND_TAG = "demand";
    protected static final int TRADE_ROWS = 8;
    protected static final int TRADE_ROW_HEIGHT = 12;
    protected static final int TRADE_LIST_ROW_HEIGHT = 20;
    protected static final int TRADE_LIST_ROW_TEXT_HEIGHT = 8;
    protected static final int TRADE_SLOT_FIRST_BUY = 0;
    protected static final int TRADE_SLOT_SECOND_BUY = 1;
    protected static final int TRADE_SLOT_SELL = 2;
    protected static final int TRADE_SLOT_COUNT = 3;
    protected static final int TRADE_MAX_USES_DIGITS = 4;
    protected static final int TRADE_MAX_USES_LIMIT = 9999;
    protected static final int TRADE_DEFAULT_MAX_USES = 7;
    protected static final float TRADE_DEFAULT_PRICE_MULTIPLIER = 0.05F;

    protected final ItemStack originalStack;
    protected final int targetContainerSlot;
    protected final ItemEditorScreen parentTradeScreen;
    protected final int parentTradeIndex;
    protected final int parentTradeSlot;
    protected ItemStack previewStack;
    protected Panel activePanel = Panel.ITEM;
    protected Component status = Component.empty();

    protected String itemIdValue;
    protected String countValue;
    protected String damageValue;
    protected String nameValue;
    protected String rawNbtValue;
    protected String enchantFilterValue = "";
    protected String enchantLevelValue = "1";
    protected String potionFilterValue = "";
    protected String potionLevelValue = "1";
    protected String potionTimeValue = "1";
    protected String attributeAmountValue = "0";
    protected String attributeDecimalValue = "0";
    protected String colorHexValue;
    protected final String[] signLineValues = new String[SIGN_LINES];
    protected String signCommandValue = "";
    protected String bookTitleValue = "";
    protected String bookAuthorValue = "";
    protected String headOwnerValue = "";
    protected String headUuidValue = "";
    protected String headTextureValue = "";
    protected String headTextureSignatureValue = "";
    protected String containerSlotNbtValue = "{}";
    protected String bannerPatternFilterValue = "";
    protected String spawnEggEntityFilterValue = "";
    protected String spawnEggCustomNameValue = "";
    protected String spawnEggOwnerValue = "";
    protected String tradeItemNbtValue = "{}";
    protected String tradeUsesValue = "0";
    protected String tradeMaxUsesValue = Integer.toString(TRADE_DEFAULT_MAX_USES);
    protected String tradeXpValue = "0";
    protected String tradePriceMultiplierValue = Float.toString(TRADE_DEFAULT_PRICE_MULTIPLIER);
    protected String tradeSpecialPriceValue = "0";
    protected String tradeDemandValue = "0";
    protected String nbtFeedback = "";
    protected boolean nbtFeedbackGood;
    protected boolean showAllEnchantments;
    protected boolean showPotionParticles = true;
    protected boolean attributeInfinity;
    protected boolean attributeNegative;
    protected boolean syncingColorControls;
    protected boolean lorePainterDragging;
    protected boolean lorePainterPreview;
    protected boolean tradeRewardExp = true;
    protected CompoundTag rememberedSignedBookData;
    protected int rotOff;
    protected int mouseDist;
    protected int midX;
    protected int midY;
    protected int advancedScroll;
    protected int loreScroll;
    protected int attributeSlot = 1;
    protected int attributeOperation;
    protected int bannerBaseColor;
    protected int bannerPatternColor = DyeColor.BLACK.getId();
    protected int bannerPatternScroll;
    protected int selectedBannerPatternIndex;
    protected int fireworkExplosionType;
    protected int fireworkColor = DyeColor.RED.getId();
    protected int fireworkFadeColor = -1;
    protected int selectedContainerSlot;
    protected int spawnEggEntityScroll;
    protected int spawnEggTagScroll;
    protected int selectedSpawnEggEntityIndex;
    protected int selectedTradeIndex;
    protected int selectedTradeSlot;
    protected int tradeScroll;
    protected int lorePainterWidth = 3;
    protected int lorePainterHeight = 3;
    protected boolean draggingLoreScroll;
    protected boolean fireworkFlicker;
    protected boolean fireworkTrail;

    protected final List<String> loreValues = new ArrayList<>();
    protected final List<List<LorePixel>> lorePainterRows = new ArrayList<>();
    protected final List<EditBox> tickingBoxes = new ArrayList<>();
    protected final List<EditBox> mainTextBoxes = new ArrayList<>();
    protected final List<EditBox> loreBoxes = new ArrayList<>();
    protected final List<EditBox> signBoxes = new ArrayList<>();
    protected final List<InfinityEditorButton> loreActionButtons = new ArrayList<>();
    protected final Map<String, String> spawnEggNumberValueOverrides = new HashMap<>();
    protected final Set<String> expandedNbtPaths = new HashSet<>();
    protected final ItemStack enchantBook = new ItemStack(Items.ENCHANTED_BOOK);
    protected final ItemStack potionIcon = new ItemStack(Items.POTION);
    protected final ItemStack attributeIcon = new ItemStack(Items.PAPER);
    protected final LorePixel currentLorePixel = new LorePixel();

    protected EditBox itemIdBox;
    protected EditBox countBox;
    protected EditBox damageBox;
    protected EditBox nameBox;
    protected EditBox rawNbtBox;
    protected EditBox enchantFilterBox;
    protected EditBox enchantLevelBox;
    protected EditBox potionFilterBox;
    protected EditBox potionLevelBox;
    protected EditBox potionTimeBox;
    protected EditBox attributeAmountBox;
    protected EditBox attributeDecimalBox;
    protected EditBox colorHexBox;
    protected EditBox signCommandBox;
    protected EditBox bookTitleBox;
    protected EditBox bookAuthorBox;
    protected EditBox headOwnerBox;
    protected EditBox headUuidBox;
    protected EditBox headTextureBox;
    protected EditBox headTextureSignatureBox;
    protected EditBox containerSlotNbtBox;
    protected EditBox bannerPatternFilterBox;
    protected EditBox spawnEggEntityFilterBox;
    protected EditBox spawnEggCustomNameBox;
    protected EditBox spawnEggOwnerBox;
    protected EditBox tradeItemNbtBox;
    protected EditBox tradeUsesBox;
    protected EditBox tradeMaxUsesBox;
    protected EditBox tradeXpBox;
    protected EditBox tradePriceMultiplierBox;
    protected EditBox tradeSpecialPriceBox;
    protected EditBox tradeDemandBox;
    protected InfinityEditorButton attributeInfinityButton;
    protected InfinityEditorButton attributeOperationButton;
    protected InfinityEditorButton attributeSlotButton;
    protected InfinityEditorButton lorePainterScaleButton;
    protected InfinityEditorButton lorePainterAddRowButton;
    protected InfinityEditorButton lorePainterRemoveRowButton;
    protected InfinityEditorButton lorePainterAddColumnButton;
    protected InfinityEditorButton lorePainterRemoveColumnButton;
    protected InfinityEditorButton lorePainterPreviewButton;
    protected InfinityEditorButton copyLoreButton;
    protected ColorSlider redSlider;
    protected ColorSlider greenSlider;
    protected ColorSlider blueSlider;

    protected ItemEditorScreenState(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(Component.translatable(key("item")));
        this.originalStack = stack.copy();
        this.targetContainerSlot = targetContainerSlot;
        this.parentTradeScreen = parentTradeScreen;
        this.parentTradeIndex = parentTradeIndex;
        this.parentTradeSlot = parentTradeSlot;
        this.previewStack = stack.copy();
    }











    protected abstract void addItemPanel();

    protected abstract void addNameAndLoreWidgets();

    protected abstract void addLoreTextField(int line, boolean realLine);

    protected abstract void addSpecialButtons();

    protected abstract void addFormatButtons();

    protected abstract void addNbtPanel();

    protected abstract void addNbtAdvancedPanel();

    protected abstract void addHideFlagsPanel();

    protected abstract void addEnchantmentsPanel();

    protected abstract void addPotionPanel();

    protected abstract void addSignPanel();

    protected abstract void addBookPanel();

    protected abstract void addHeadPanel();

    protected abstract void addArmorStandPanel();

    protected abstract void addArmorStandToggleButton(int x, int y, String translationSuffix, String tagKey);

    protected abstract void addFireworkPanel();

    protected abstract void addContainerPanel();

    protected abstract void addBannerPanel();

    protected abstract void addSpawnEggPanel();

    protected abstract void addSpawnEggTagControl(SpawnEggTagRow row, int y, int controlsX, int width);

    protected abstract void addTradesPanel();

    protected abstract void addTradePanel();

    protected abstract EditBox addTradeFieldBox(int x, int y, int width, String value, java.util.function.Predicate<String> filter,
                                     java.util.function.Consumer<String> responder);

    protected abstract void addAttributesPanel();

    protected abstract void addColorPanel();

    protected abstract void addLorePainterPanel();

    protected abstract void addLorePanel();

    protected abstract InfinityEditorButton addTopButton(int x, String keySuffix, InfinityEditorButton.PressAction action);

    protected abstract void addBottomButtons();

    protected abstract EditBox addTrackedBox(EditBox box);

    protected abstract EditBox legacyTextBox(int x, int y, int width, int height, Component message);

    protected abstract EditBox numberBox(int x, int y, int width, int height, int digits, String value, int minValue, int maxValue);

    protected abstract void renderItemPanel(GuiGraphics guiGraphics, int mouseX, int mouseY);

    protected abstract void renderNbtPanel(GuiGraphics guiGraphics, int mouseX, int mouseY);

    protected abstract void renderNbtAdvancedPanel(GuiGraphics guiGraphics, int mouseX, int mouseY);

    protected abstract void renderHideFlagsPanel(GuiGraphics guiGraphics);

    protected abstract void renderBookPanel(GuiGraphics guiGraphics);

    protected abstract void renderHeadPanel(GuiGraphics guiGraphics);

    protected abstract void renderArmorStandPanel(GuiGraphics guiGraphics);

    protected abstract void renderFireworkPanel(GuiGraphics guiGraphics);

    protected abstract void renderSimpleItemPanelTitle(GuiGraphics guiGraphics, String titleKey, int itemY);

    protected abstract void renderEnchantmentsPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    protected abstract void renderPotionPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    protected abstract void renderAttributesPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    protected abstract void renderColorPanel(GuiGraphics guiGraphics);

    protected abstract void renderSignPanel(GuiGraphics guiGraphics);

    protected abstract void renderContainerPanel(GuiGraphics guiGraphics);

    protected abstract void renderBannerPanel(GuiGraphics guiGraphics);

    protected abstract void renderSpawnEggPanel(GuiGraphics guiGraphics);

    protected abstract void renderTradesPanel(GuiGraphics guiGraphics, int mouseX, int mouseY);

    protected abstract void renderTradePanel(GuiGraphics guiGraphics);

    protected abstract void renderLorePanel(GuiGraphics guiGraphics, int mouseX, int mouseY);

    protected abstract void renderLorePainterPanel(GuiGraphics guiGraphics, int mouseX, int mouseY);

    protected abstract void renderItemTooltipPreview(GuiGraphics guiGraphics);

    protected abstract void renderPrettyNbt(GuiGraphics guiGraphics);

    protected abstract void renderSmallItem(GuiGraphics guiGraphics, int centerX, int centerY);

    protected abstract void renderLargePreviewItem(GuiGraphics guiGraphics);

    protected abstract void renderPanelTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY);

    protected abstract void drawRightLabel(GuiGraphics guiGraphics, Component text, int rightX, int y);

    protected abstract void drawTradeFieldLabels(GuiGraphics guiGraphics);

    protected abstract boolean handleEnchantingClick(double mouseX, double mouseY);

    protected abstract boolean handlePotionClick(double mouseX, double mouseY);

    protected abstract boolean handleAttributesClick(double mouseX, double mouseY);

    protected abstract boolean handleColorClick(double mouseX, double mouseY);

    protected abstract boolean handleContainerClick(double mouseX, double mouseY);

    protected abstract boolean handleBannerClick(double mouseX, double mouseY);

    protected abstract boolean handleSpawnEggClick(double mouseX, double mouseY);

    protected abstract boolean handleTradesClick(double mouseX, double mouseY, int button);

    protected abstract boolean handleTradeClick(double mouseX, double mouseY);

    protected abstract boolean handleLoreClick(double mouseX, double mouseY);

    protected abstract boolean handleLorePainterClick(double mouseX, double mouseY);

    protected abstract boolean handleNbtAdvancedClick(double mouseX, double mouseY);

    protected abstract void switchPanel(Panel panel);

    protected abstract void openContainerItemEditor();

    protected abstract void openBookItemEditor();

    abstract void refreshAfterContainerEdit();

    abstract void refreshAfterBookEdit();

    protected abstract void goBack();

    public abstract void onClose();

    protected abstract void applyToSelectedSlot();

    protected abstract void dropEditedStack();

    protected abstract void copyGiveCommand();

    protected abstract void resetStack();

    protected abstract void saveRealm();

    protected abstract boolean applyMainFieldsToStack(boolean updateStatus);

    protected abstract boolean tryApplyItemId(boolean throwOnError);

    protected abstract void tryApplyCount(boolean throwOnError);

    protected abstract void tryApplyDamage(boolean throwOnError);

    protected abstract void applyNameToStack();

    protected abstract void clearCustomName();

    protected abstract void applyLoreToStack();

    protected abstract void setLoreLine(int line, String value);

    protected abstract void removeLoreLine(int line);

    protected abstract void moveLoreLine(int line, int direction);

    protected abstract void copyLoreOnly();

    protected abstract void copyFullTooltip();

    protected abstract void pasteLore();

    protected abstract void applySignToStack();

    protected abstract void applyBookMetadataToStack();

    protected abstract void cycleBookGeneration();

    protected abstract void toggleBookResolved();

    protected abstract void toggleBookSignedState();

    protected abstract void unsignBook();

    protected abstract void signBook();

    protected abstract void convertWrittenPagesToWritable(CompoundTag tag);

    protected abstract void convertWritablePagesToWritten(CompoundTag tag);

    protected abstract Component readBookPageComponent(String raw);

    protected abstract boolean isProbablyJsonText(String raw);

    protected abstract void applyHeadToStack();

    protected abstract void clearHeadOwner();

    protected abstract void randomizeHeadUuid();

    protected abstract void toggleArmorStandFlag(String tagKey, String translationSuffix);

    protected abstract void clearArmorStandEntityTag();

    protected abstract CompoundTag getOrCreateArmorStandEntityTag();

    protected abstract void cleanupArmorStandEntityTag(CompoundTag entityTag);

    protected abstract Component getArmorStandToggleText(String translationSuffix, String tagKey);

    protected abstract boolean getArmorStandFlag(String tagKey);

    protected abstract void cycleFireworkFlight();

    protected abstract void cycleFireworkExplosionType(int direction);

    protected abstract void toggleFireworkFlicker();

    protected abstract void toggleFireworkTrail();

    protected abstract void cycleFireworkColor(boolean fade, int direction);

    protected abstract void randomizeFireworkColors();

    protected abstract void addFireworkExplosion();

    protected abstract void removeLastFireworkExplosion();

    protected abstract void clearFireworkData();

    protected abstract void applyFireworkControlsToStack();

    protected abstract CompoundTag createFireworkExplosionTag();

    protected abstract CompoundTag getOrCreateFireworksTag();

    protected abstract void cleanupFireworksTag(CompoundTag fireworks);

    protected abstract void cycleContainerSlot(int direction);

    protected abstract void updateContainerSlotFromNbt();

    protected abstract ItemStack parseContainerSlotItem(String value) throws CommandSyntaxException;

    protected abstract void clearContainerSlot();

    protected abstract void clearContainerItems();

    protected abstract void setContainerSlotItem(int slot, ItemStack slotStack);

    protected abstract ItemStack getContainerSlotItem(int slot);

    protected abstract ListTag getContainerItemsList();

    protected abstract int getContainerItemCount();

    protected abstract String getContainerSelectedSlotNbt();

    protected abstract String getContainerSlotNbt(ItemStack stack);

    protected abstract int getContainerGridX();

    protected abstract int getContainerGridY();

    protected abstract int getHoveredContainerSlot(int mouseX, int mouseY);

    protected abstract MutableComponent createSignLineComponent(int line);

    protected abstract boolean hasSignContent();

    protected abstract String getSignLineValue(int line);

    protected abstract String getNormalizedSignCommand();

    protected abstract void cleanupBlockEntityTag(CompoundTag tag, CompoundTag blockEntity);

    protected abstract void addSelectedBannerPattern();

    protected abstract void removeLastBannerPattern();

    protected abstract void clearBannerPatterns();

    protected abstract void cycleBannerBaseColor(int direction);

    protected abstract void cycleBannerPatternColor(int direction);

    protected abstract void cycleSelectedBannerPattern(int direction);

    protected abstract void setBannerPatternScroll(int value);

    protected abstract void scrollBannerPatternSelectionIntoView(List<BannerPatternEntry> patterns);

    protected abstract void clampBannerPatternSelection(List<BannerPatternEntry> patterns);

    protected abstract int getBannerPatternRowY(int row);

    protected abstract List<BannerPatternEntry> getFilteredBannerPatterns();

    protected abstract Component getBannerPatternName(BannerPatternEntry entry, DyeColor color);

    protected abstract void renderBannerPatternLayers(GuiGraphics guiGraphics);

    protected abstract void swapBannerAndShield();

    protected abstract void setBannerBaseColor(DyeColor color);

    protected abstract void replacePreviewItem(Item item);

    protected abstract void removeBannerBaseColorTag();

    protected abstract DyeColor getBannerBaseColor();

    protected abstract DyeColor getBannerPatternColor();

    protected abstract Component getDyeColorName(DyeColor color);

    protected abstract int getBannerPatternCount();

    protected abstract ListTag getBannerPatterns();

    protected abstract CompoundTag getOrCreateBannerBlockEntityTag();

    protected abstract BannerPatternEntry getBannerPatternEntry(String hash);

    protected abstract void applySelectedSpawnEggEntity();

    protected abstract void syncSpawnEggToSelectedEntityItem();

    protected abstract void writeSpawnEggEntityId(SpawnEggEntityEntry entry);

    protected abstract void clearSpawnEggEntityTag();

    protected abstract void toggleSpawnEggBoolean(SpawnEggTagRow row);

    protected abstract void applySpawnEggCustomName(String value);

    protected abstract void applySpawnEggOwner(String value);

    protected abstract void applySpawnEggNumber(SpawnEggTagRow row, String value);

    protected abstract void cycleSpawnEggChoice(SpawnEggTagRow row);

    protected abstract CompoundTag getOrCreateSpawnEditorEntityTag();

    protected abstract void cleanupSpawnEggEntityTag(CompoundTag entityTag);

    protected abstract CompoundTag getOrCreateSpawnerEntityTag();

    protected abstract CompoundTag getOrCreateSpawnerBlockEntityTag();

    protected abstract void cleanupSpawnerEntityTag(CompoundTag entityTag);

    protected abstract void clearSpawnerSpawnData();

    protected abstract void clearSpawnerSpawnPotentials();

    protected abstract void cleanupSpawnerBlockEntityTag(CompoundTag blockEntity);

    protected abstract boolean isOnlySpawnerBlockEntityId(CompoundTag blockEntity);

    protected abstract Component getSpawnEggBooleanText(SpawnEggTagRow row);

    protected abstract Component getSpawnEggChoiceText(SpawnEggTagRow row);

    protected abstract Component getSpawnEggChoiceOptionText(SpawnEggTagRow row);

    protected abstract String getSpawnEggChoiceValue(SpawnEggTagRow row);

    protected abstract int getSpawnEggChoiceIndex(SpawnEggTagRow row, String value);

    protected abstract boolean getSpawnEggBooleanValue(SpawnEggTagRow row);

    protected abstract String getSpawnEggNumberValue(SpawnEggTagRow row);

    protected abstract int getSpawnEggTagTextMaxLength(SpawnEggTagRow row);

    protected abstract String getSpawnEggTagTextValue(SpawnEggTagRow row);

    protected abstract CompoundTag getSpawnEggTagParent(CompoundTag entityTag, String tagPath, boolean create);

    protected abstract String getSpawnEggLeafTagKey(String tagPath);

    protected abstract void putSpawnEggBooleanValue(CompoundTag entityTag, String tagPath, boolean value);

    protected abstract void putSpawnEggStringValue(CompoundTag entityTag, String tagPath, String value);

    protected abstract void putSpawnEggIntValue(CompoundTag entityTag, String tagPath, int value);

    protected abstract void putSpawnEggNumberValue(CompoundTag entityTag, SpawnEggTagRow row, double storedValue);

    protected abstract void removeSpawnEggTagValue(CompoundTag entityTag, String tagPath);

    protected abstract boolean removeSpawnEggTagValue(CompoundTag current, String[] parts, int index);

    protected abstract boolean isAllowedSpawnEggNumber(String value, SpawnEggNumberType type);

    protected abstract boolean isPartialSpawnEggNumber(String value);

    protected abstract String formatSpawnEggNumber(double value);

    protected abstract void setSpawnEggEntityScroll(int value);

    protected abstract void setSpawnEggTagScroll(int value);

    protected abstract void cycleSelectedSpawnEggEntity(int direction);

    protected abstract void scrollSpawnEggSelectionIntoView(List<SpawnEggEntityEntry> entities);

    protected abstract void clampSpawnEggEntitySelection(List<SpawnEggEntityEntry> entities);

    protected abstract int getSpawnEggEntityRowY(int row);

    protected abstract int getSpawnEggTagRowY(int row);

    protected abstract int getSpawnEggControlsX();

    protected abstract int getSpawnEggControlsWidth();

    protected abstract SpawnEggEntityEntry getSelectedSpawnEggEntityEntry();

    protected abstract List<SpawnEggEntityEntry> getFilteredSpawnEggEntities();

    protected abstract String formatSpawnEggEntityEntry(SpawnEggEntityEntry entry);

    protected abstract Component getSpawnEggEntityName(SpawnEggEntityEntry entry);

    protected abstract Component getCurrentSpawnEggEntityName();

    protected abstract EntityType<?> getCurrentSpawnEggEntityType(ItemStack stack);

    protected abstract String getSpawnEggEntityIdOverride(ItemStack stack);

    protected abstract CompoundTag getSpawnEditorEntityTag(ItemStack stack);

    protected abstract CompoundTag getSpawnerEntityTag(ItemStack stack);

    protected abstract CompoundTag getFirstSpawnerPotentialEntity(CompoundTag blockEntity);

    protected abstract CompoundTag getSpawnerEntityFromPotential(CompoundTag potential);

    protected abstract CompoundTag getSpawnerEntityFromSpawnData(CompoundTag spawnData);

    protected abstract void putSpawnerSpawnData(CompoundTag blockEntity, CompoundTag originalSpawnData, CompoundTag entityTag);

    protected abstract boolean hasSpawnEditorEntityData(ItemStack stack);

    protected abstract String getSpawnEditorTitleKey();

    protected abstract String getSpawnEditorClearKey();

    protected abstract String getSpawnEditorDefaultEntityKey();

    protected abstract List<SpawnEggTagRow> getSpawnEggTagRows();

    protected abstract void ensureVillagerTradeOffers();

    protected abstract void ensureVillagerData(CompoundTag entityTag);

    protected abstract void addVillagerTrade();

    protected abstract void removeSelectedVillagerTrade();

    protected abstract void removeVillagerTrade(int index);

    protected abstract void clearVillagerTrades();

    protected abstract void updateSelectedTradeFromFields();

    protected abstract void toggleSelectedTradeRewardExp();

    protected abstract void updateSelectedTradeMaxUses();

    protected abstract void openVillagerTrade(int index);

    protected abstract void selectVillagerTrade(int index);

    protected abstract void selectTradeSlot(int slot);

    protected abstract void openTradeSlotItemEditor(int slot);

    protected abstract void applyTradeSlotEditorAndReturn();

    protected abstract void setTradeSlotItem(int tradeIndex, int slot, ItemStack stack);

    protected abstract void readTradeFieldsFromStack(ItemStack stack);

    protected abstract void resetTradeFieldValues();

    protected abstract ListTag getVillagerTradeRecipes();

    protected abstract ListTag getVillagerTradeRecipes(ItemStack stack);

    protected abstract int getVillagerTradeCount();

    protected abstract CompoundTag getSelectedTradeRecipe();

    protected abstract ListTag copyTradeRecipes(ListTag recipes);

    protected abstract void putVillagerTradeRecipes(ListTag recipes);

    protected abstract CompoundTag createDefaultTradeTag();

    protected abstract void putTradeSlotItem(CompoundTag recipe, int slot, ItemStack stack);

    protected abstract ItemStack getTradeSlotItem(CompoundTag recipe, int slot);

    protected abstract String getTradeSlotTagName(int slot);

    protected abstract ItemStack parseTradeSlotItem(String value) throws CommandSyntaxException;

    protected abstract String getTradeSlotItemNbt(ItemStack stack);

    protected abstract int parseTradeIntField(String value, String fieldSuffix, int defaultValue, int minValue, int maxValue);

    protected abstract float parseTradeFloatField(String value, String fieldSuffix, float defaultValue);

    protected abstract int getTradeInt(CompoundTag recipe, String tagName, int defaultValue);

    protected abstract float getTradeFloat(CompoundTag recipe, String tagName, float defaultValue);

    protected abstract String formatTradeRecipe(CompoundTag recipe);

    protected abstract String formatTradeStack(ItemStack stack);

    protected abstract void renderTradeSlotItem(GuiGraphics guiGraphics, CompoundTag recipe, int slot);

    protected abstract int getTradeListRowY(int index, int size);

    protected abstract int getHoveredTradeListIndex(int mouseX, int mouseY);

    protected abstract boolean isMouseOverAddTrade(int mouseX, int mouseY);

    protected abstract boolean isMouseOverCenteredText(int mouseX, int mouseY, String text, int centerX, int y);

    protected abstract int getSingleTradeSlotX(int slot);

    protected abstract int getSingleTradeSlotY();

    protected abstract int getHoveredSingleTradeSlot(int mouseX, int mouseY);

    protected abstract void clampTradeSelection(ListTag trades);

    protected abstract void setTradeScroll(int value);

    protected abstract void scrollTradeSelectionIntoView(ListTag trades);

    protected abstract int getTradeRowY(int row);

    protected abstract int getTradeListWidth();

    protected abstract int getTradeControlsX();

    protected abstract int getTradeControlsWidth();

    protected abstract int getTradeSlotButtonX(int slot);

    protected abstract int getTradeSlotIconX(int slot);

    protected abstract int getTradeSlotIconY();

    protected abstract int getHoveredTradeSlot(int mouseX, int mouseY);

    protected abstract void updateRawNbt();

    protected abstract void toggleUnbreakable();

    protected abstract Component getUnbreakableText();

    protected abstract void toggleHideFlag(HideFlag flag);

    protected abstract Component getHideFlagText(HideFlag flag);

    protected abstract void toggleEnchantmentsScope();

    protected abstract void renderActiveEnchantments(GuiGraphics guiGraphics);

    protected abstract boolean tryRemoveActiveEnchantment(double mouseX, double mouseY);

    protected abstract boolean tryAddRingEnchantment(double mouseX, double mouseY);

    protected abstract void addEnchantment(Enchantment enchantment);

    protected abstract void addMatchingEnchantments();

    protected abstract int getLevelForEnchantment(Enchantment enchantment);

    protected abstract int getDisplayLevel(Enchantment enchantment);

    protected abstract void putEnchantment(Enchantment enchantment, int level);

    protected abstract boolean removeEnchantmentAtIndex(int index);

    protected abstract ListTag getOrCreateEnchantmentsTag();

    protected abstract List<Enchantment> getFilteredEnchantments(ItemStack stack);

    protected abstract boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment);

    protected abstract List<EnchantmentEntry> getStoredEnchantments(ItemStack stack);

    protected abstract boolean tryRemoveActivePotionEffect(double mouseX, double mouseY);

    protected abstract boolean tryAddRingPotionEffect(double mouseX, double mouseY);

    protected abstract void addMatchingPotionEffects();

    protected abstract boolean addPotionEffect(MobEffect effect);

    protected abstract void removeCustomPotionEffectAt(int index);

    protected abstract List<MobEffectInstance> getCustomPotionEffects();

    protected abstract List<MobEffect> getFilteredPotionEffects();

    protected abstract int parsePotionLevel();

    protected abstract int parsePotionSeconds();

    protected abstract void togglePotionParticles();

    protected abstract String formatPotionEffect(MobEffectInstance effect);

    protected abstract String formatPotionRingName(MobEffect effect);

    protected abstract void toggleAttributeInfinity();

    protected abstract void cycleAttributeOperation();

    protected abstract void cycleAttributeSlot();

    protected abstract boolean tryRemoveActiveAttributeModifier(double mouseX, double mouseY);

    protected abstract boolean tryAddRingAttribute(double mouseX, double mouseY);

    protected abstract void addAttributeModifier(Attribute attribute);

    protected abstract Double getAttributeAmount();

    protected abstract int parseBoundedAttributeNumber(String value, int max);

    protected abstract AttributeModifier.Operation getAttributeOperation();

    protected abstract ListTag getOrCreateAttributeModifiersTag();

    protected abstract List<AttributeEntry> getAttributeModifierEntries();

    protected abstract void removeAttributeModifierAt(int tagIndex);

    protected abstract String formatAttributeEntry(AttributeEntry entry);

    protected abstract Component getAttributeDisplayName(AttributeEntry entry);

    protected abstract Attribute getAttributeByName(String name);

    protected abstract List<Attribute> getSharedAttributes();

    protected abstract void applyColorFromHex(boolean updateStatus);

    protected abstract void setColorComponent(int shift, int value);

    protected abstract void syncColorControlsFromStack();

    protected abstract int getEditorColor();

    protected abstract void setEditorColor(int color);

    protected abstract void addDyeToColor(DyeColor dyeColor);

    protected abstract void renderDyeGrid(GuiGraphics guiGraphics);

    protected abstract boolean shouldShowDyeGrid();

    protected abstract void ensureLorePainterRows();

    protected abstract void addLorePainterRow();

    protected abstract void removeLorePainterRow();

    protected abstract void addLorePainterColumn();

    protected abstract void removeLorePainterColumn();

    protected abstract void insertLorePainterRows();

    protected abstract void cycleGuiScale();

    protected abstract void paintLorePainterAt(double mouseX, double mouseY);

    protected abstract String buildLorePainterRow(List<LorePixel> row);

    protected abstract String buildLorePainterSymbols();

    protected abstract String buildLorePainterColors();

    protected abstract int getLorePainterColorX();

    protected abstract int getLorePainterGridX();

    protected abstract int getLorePainterGridY();

    protected abstract int getLorePainterSizeX();

    protected abstract int getLorePainterSizeY();

    protected abstract void updateMouseDistance(int mouseX, int mouseY);

    protected abstract int getRingRadius();

    protected abstract boolean isMouseOverCenter(double mouseX, double mouseY);

    protected abstract void insertFormattingPrefix();

    protected abstract void stripFocusedFormatting();

    protected abstract void insertFocusedText(String text);

    protected abstract EditBox getFocusedTextBox();

    protected abstract List<Component> getPrettyNbtLines();

    protected abstract List<NbtRow> buildNbtRows();

    protected abstract int getNbtAdvancedVisibleRows();

    protected abstract void readMainFieldsFromStack(ItemStack stack);

    protected abstract String readLoreLine(String raw);

    protected abstract void readSignFieldsFromStack(ItemStack stack);

    protected abstract void readSignLine(int line, String raw);

    protected abstract void readBookFieldsFromStack(ItemStack stack);

    protected abstract void readHeadFieldsFromStack(ItemStack stack);

    protected abstract void readFireworkFieldsFromStack(ItemStack stack);

    protected abstract void readContainerFieldsFromStack(ItemStack stack);

    protected abstract CompoundTag getFireworkExplosionForFields(ItemStack stack);

    protected abstract void readBannerFieldsFromStack(ItemStack stack);

    protected abstract void readSpawnEggFieldsFromStack(ItemStack stack);

    protected abstract Component readSerializedComponent(String raw);

    protected abstract void updateLoreScrollFromMouse(double mouseY);

    protected abstract void setLoreScroll(int value);

    protected abstract void captureFieldValues();

    protected abstract CompoundTag parseNbt(String nbt) throws CommandSyntaxException;

    protected abstract void cleanupEmptyDisplayTag();

    protected abstract void cleanupEmptyTag();

    protected abstract int loreLineSpaces();

    protected abstract boolean isMouseIn(double mouseX, double mouseY, int x, int y, int width, int height);

    protected abstract boolean isTradeSlotEditor();

    protected abstract boolean isNameFollowingDefault(ItemStack stack);

    protected abstract String getDefaultHoverName(ItemStack stack);

    protected abstract boolean isVillagerTradeEditableItem(ItemStack stack);

    protected abstract int getBookGeneration();

    protected abstract int getBookPageCount();

    protected abstract Component getBookResolvedText();

    protected abstract Component getBookSignButtonText();

    protected abstract int getFireworkFlight();

    protected abstract int getFireworkExplosionCount();

    protected abstract boolean hasFireworkData();

    protected abstract Component getFireworkTypeName(int type);

    protected abstract Component getFireworkFadeColorText();

    protected abstract DyeColor getFireworkDyeColor(int colorId);

    protected abstract String formatRingEnchantmentName(Enchantment enchantment);

    protected abstract String formatStoredEnchantment(EnchantmentEntry entry);

    protected static String key(String suffix) {
        return "screen." + ModSource.MODID + "." + suffix;
    }

    protected static String messageKey(String suffix) {
        return "message." + ModSource.MODID + "." + suffix;
    }
}

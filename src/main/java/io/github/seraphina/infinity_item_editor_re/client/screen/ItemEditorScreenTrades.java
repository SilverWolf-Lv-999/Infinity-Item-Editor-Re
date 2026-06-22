package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Axis;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.CreativeTabRefresher;
import io.github.seraphina.infinity_item_editor_re.client.screen.modern.ModernUi;
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
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.WrittenBookItem;
import io.github.seraphina.infinity_item_editor_re.util.PotionCompat;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import io.github.seraphina.infinity_item_editor_re.util.CompatRegistries;

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
abstract class ItemEditorScreenTrades extends ItemEditorScreenBannerSpawn {
    protected ItemEditorScreenTrades(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(stack, targetContainerSlot, parentTradeScreen, parentTradeIndex, parentTradeSlot);
    }

    protected void ensureVillagerTradeOffers() {
        if (!isVillagerTradeEditableItem(this.previewStack)) {
            return;
        }

        CompoundTag tag = ItemStackNbt.getOrCreate(this.previewStack);
        CompoundTag entityTag = tag.getCompound(ENTITY_TAG);
        if (!entityTag.contains(ENTITY_ID_TAG, Tag.TAG_STRING)) {
            entityTag.putString(ENTITY_ID_TAG, "minecraft:villager");
        }
        ensureVillagerData(entityTag);
        if (!entityTag.contains(OFFERS_TAG, Tag.TAG_COMPOUND)) {
            entityTag.put(OFFERS_TAG, new CompoundTag());
        }
        CompoundTag offers = entityTag.getCompound(OFFERS_TAG);
        if (!offers.contains(RECIPES_TAG, Tag.TAG_LIST)) {
            offers.put(RECIPES_TAG, new ListTag());
        }
        entityTag.put(OFFERS_TAG, offers);
        tag.put(ENTITY_TAG, entityTag);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        readTradeFieldsFromStack(this.previewStack);
    }

    protected void ensureVillagerData(CompoundTag entityTag) {
        CompoundTag villagerData = entityTag.contains(VILLAGER_DATA_TAG, Tag.TAG_COMPOUND)
                ? entityTag.getCompound(VILLAGER_DATA_TAG)
                : new CompoundTag();
        if (!villagerData.contains(VILLAGER_TYPE_TAG, Tag.TAG_STRING)) {
            villagerData.putString(VILLAGER_TYPE_TAG, DEFAULT_VILLAGER_TYPE);
        }
        if (!villagerData.contains(VILLAGER_PROFESSION_TAG, Tag.TAG_STRING)) {
            villagerData.putString(VILLAGER_PROFESSION_TAG, DEFAULT_VILLAGER_PROFESSION);
        }
        if (!villagerData.contains(VILLAGER_LEVEL_TAG, Tag.TAG_ANY_NUMERIC)) {
            villagerData.putInt(VILLAGER_LEVEL_TAG, DEFAULT_VILLAGER_LEVEL);
        } else {
            villagerData.putInt(VILLAGER_LEVEL_TAG, Mth.clamp(villagerData.getInt(VILLAGER_LEVEL_TAG), 1, 5));
        }
        entityTag.put(VILLAGER_DATA_TAG, villagerData);
    }

    protected void addVillagerTrade() {
        if (!isVillagerTradeEditableItem(this.previewStack)) {
            return;
        }

        ListTag recipes = copyTradeRecipes(getVillagerTradeRecipes());
        recipes.add(createDefaultTradeTag());
        putVillagerTradeRecipes(recipes);
        this.selectedTradeIndex = recipes.size() - 1;
        scrollTradeSelectionIntoView(recipes);
        readTradeFieldsFromStack(this.previewStack);
        this.status = Component.translatable(messageKey("editor_trade_added"), recipes.size());
        rebuildWidgets();
    }

    protected void removeSelectedVillagerTrade() {
        removeVillagerTrade(this.selectedTradeIndex);
    }

    protected void removeVillagerTrade(int index) {
        if (!isVillagerTradeEditableItem(this.previewStack)) {
            return;
        }

        ListTag recipes = copyTradeRecipes(getVillagerTradeRecipes());
        if (recipes.isEmpty()) {
            return;
        }

        int removedIndex = Mth.clamp(index, 0, recipes.size() - 1);
        recipes.remove(removedIndex);
        this.selectedTradeIndex = Mth.clamp(removedIndex, 0, Math.max(0, recipes.size() - 1));
        putVillagerTradeRecipes(recipes);
        readTradeFieldsFromStack(this.previewStack);
        this.status = Component.translatable(messageKey("editor_trade_removed"), removedIndex + 1);
        rebuildWidgets();
    }

    protected void clearVillagerTrades() {
        if (!isVillagerTradeEditableItem(this.previewStack)) {
            return;
        }

        putVillagerTradeRecipes(new ListTag());
        this.selectedTradeIndex = 0;
        this.tradeScroll = 0;
        resetTradeFieldValues();
        this.status = Component.translatable(messageKey("editor_trades_cleared"));
        rebuildWidgets();
    }

    protected void updateSelectedTradeFromFields() {
        if (!isVillagerTradeEditableItem(this.previewStack)) {
            return;
        }

        captureFieldValues();
        ListTag recipes = copyTradeRecipes(getVillagerTradeRecipes());
        if (recipes.isEmpty()) {
            return;
        }

        clampTradeSelection(recipes);
        CompoundTag recipe = recipes.getCompound(this.selectedTradeIndex).copy();
        try {
            ItemStack slotStack = parseTradeSlotItem(this.tradeItemNbtValue);
            if (slotStack.isEmpty() && this.selectedTradeSlot != TRADE_SLOT_SECOND_BUY) {
                throw new IllegalArgumentException(Component.translatable(messageKey("editor_trade_invalid_item")).getString());
            }
            putTradeSlotItem(recipe, this.selectedTradeSlot, slotStack);
            recipe.putInt(TRADE_USES_TAG, parseTradeIntField(this.tradeUsesValue, "uses", 0, 0, Integer.MAX_VALUE));
            recipe.putInt(TRADE_MAX_USES_TAG, parseTradeIntField(this.tradeMaxUsesValue, "max_uses",
                    TRADE_DEFAULT_MAX_USES, 0, Integer.MAX_VALUE));
            recipe.putInt(TRADE_XP_TAG, parseTradeIntField(this.tradeXpValue, "xp", 0, 0, Integer.MAX_VALUE));
            recipe.putFloat(TRADE_PRICE_MULTIPLIER_TAG, parseTradeFloatField(this.tradePriceMultiplierValue,
                    "price_multiplier", TRADE_DEFAULT_PRICE_MULTIPLIER));
            recipe.putInt(TRADE_SPECIAL_PRICE_TAG, parseTradeIntField(this.tradeSpecialPriceValue, "special_price",
                    0, Integer.MIN_VALUE, Integer.MAX_VALUE));
            recipe.putInt(TRADE_DEMAND_TAG, parseTradeIntField(this.tradeDemandValue, "demand",
                    0, Integer.MIN_VALUE, Integer.MAX_VALUE));
            recipe.putBoolean(TRADE_REWARD_EXP_TAG, this.tradeRewardExp);
            recipes.set(this.selectedTradeIndex, recipe);
            putVillagerTradeRecipes(recipes);
            this.tradeItemNbtValue = getTradeSlotItemNbt(slotStack);
            this.status = Component.translatable(messageKey("editor_trade_updated"), this.selectedTradeIndex + 1);
            readTradeFieldsFromStack(this.previewStack);
            rebuildWidgets();
        } catch (CommandSyntaxException exception) {
            this.status = Component.translatable(messageKey("editor_invalid_nbt"), exception.getMessage());
        } catch (IllegalArgumentException exception) {
            this.status = Component.literal(exception.getMessage());
        }
    }

    protected void toggleSelectedTradeRewardExp() {
        ListTag recipes = copyTradeRecipes(getVillagerTradeRecipes());
        if (recipes.isEmpty()) {
            return;
        }

        clampTradeSelection(recipes);
        CompoundTag recipe = recipes.getCompound(this.selectedTradeIndex).copy();
        this.tradeRewardExp = !this.tradeRewardExp;
        recipe.putBoolean(TRADE_REWARD_EXP_TAG, this.tradeRewardExp);
        recipes.set(this.selectedTradeIndex, recipe);
        putVillagerTradeRecipes(recipes);
        this.status = Component.translatable(messageKey("editor_trade_updated"), this.selectedTradeIndex + 1);
        rebuildWidgets();
    }

    protected void updateSelectedTradeMaxUses() {
        ListTag recipes = copyTradeRecipes(getVillagerTradeRecipes());
        if (recipes.isEmpty()) {
            return;
        }

        clampTradeSelection(recipes);
        CompoundTag recipe = recipes.getCompound(this.selectedTradeIndex).copy();
        int maxUses = parseTradeIntField(this.tradeMaxUsesValue, "max_uses",
                TRADE_DEFAULT_MAX_USES, -TRADE_MAX_USES_LIMIT, TRADE_MAX_USES_LIMIT);
        recipe.putInt(TRADE_MAX_USES_TAG, maxUses);
        recipes.set(this.selectedTradeIndex, recipe);
        putVillagerTradeRecipes(recipes);
    }

    protected void openVillagerTrade(int index) {
        ListTag trades = getVillagerTradeRecipes();
        if (trades.isEmpty()) {
            return;
        }

        this.selectedTradeIndex = Mth.clamp(index, 0, trades.size() - 1);
        this.selectedTradeSlot = TRADE_SLOT_FIRST_BUY;
        this.activePanel = Panel.TRADE;
        this.status = Component.empty();
        readTradeFieldsFromStack(this.previewStack);
        rebuildWidgets();
    }

    protected void selectVillagerTrade(int index) {
        ListTag trades = getVillagerTradeRecipes();
        if (trades.isEmpty()) {
            return;
        }

        this.selectedTradeIndex = Mth.clamp(index, 0, trades.size() - 1);
        scrollTradeSelectionIntoView(trades);
        readTradeFieldsFromStack(this.previewStack);
        rebuildWidgets();
    }

    protected void selectTradeSlot(int slot) {
        this.selectedTradeSlot = Mth.clamp(slot, 0, TRADE_SLOT_COUNT - 1);
        readTradeFieldsFromStack(this.previewStack);
        rebuildWidgets();
    }

    protected void openTradeSlotItemEditor(int slot) {
        if (this.minecraft == null) {
            return;
        }

        ListTag trades = getVillagerTradeRecipes();
        if (trades.isEmpty()) {
            return;
        }

        clampTradeSelection(trades);
        int clampedSlot = Mth.clamp(slot, 0, TRADE_SLOT_COUNT - 1);
        this.selectedTradeSlot = clampedSlot;
        ItemStack slotStack = getTradeSlotItem(trades.getCompound(this.selectedTradeIndex), clampedSlot);
        this.minecraft.setScreen(new ItemEditorScreen(slotStack, (ItemEditorScreen) this, this.selectedTradeIndex, clampedSlot));
    }

    protected void applyTradeSlotEditorAndReturn() {
        if (this.parentTradeScreen == null || this.minecraft == null) {
            onClose();
            return;
        }

        if (!applyMainFieldsToStack(true)) {
            return;
        }

        this.parentTradeScreen.setTradeSlotItem(this.parentTradeIndex, this.parentTradeSlot, this.previewStack.copy());
        this.parentTradeScreen.activePanel = Panel.TRADE;
        this.parentTradeScreen.status = Component.empty();
        this.parentTradeScreen.rebuildWidgets();
        this.minecraft.setScreen(this.parentTradeScreen);
    }

    protected void setTradeSlotItem(int tradeIndex, int slot, ItemStack stack) {
        if (!isVillagerTradeEditableItem(this.previewStack)) {
            return;
        }

        ListTag recipes = copyTradeRecipes(getVillagerTradeRecipes());
        if (recipes.isEmpty()) {
            return;
        }

        int clampedIndex = Mth.clamp(tradeIndex, 0, recipes.size() - 1);
        int clampedSlot = Mth.clamp(slot, 0, TRADE_SLOT_COUNT - 1);
        CompoundTag recipe = recipes.getCompound(clampedIndex).copy();
        putTradeSlotItem(recipe, clampedSlot, stack);
        recipes.set(clampedIndex, recipe);
        this.selectedTradeIndex = clampedIndex;
        this.selectedTradeSlot = clampedSlot;
        putVillagerTradeRecipes(recipes);
        readTradeFieldsFromStack(this.previewStack);
    }

    protected void readTradeFieldsFromStack(ItemStack stack) {
        ListTag trades = getVillagerTradeRecipes(stack);
        clampTradeSelection(trades);
        if (!isVillagerTradeEditableItem(stack) || trades.isEmpty()) {
            resetTradeFieldValues();
            return;
        }

        CompoundTag recipe = trades.getCompound(this.selectedTradeIndex);
        ItemStack slotStack = getTradeSlotItem(recipe, this.selectedTradeSlot);
        this.tradeItemNbtValue = getTradeSlotItemNbt(slotStack);
        this.tradeUsesValue = Integer.toString(getTradeInt(recipe, TRADE_USES_TAG, 0));
        this.tradeMaxUsesValue = Integer.toString(getTradeInt(recipe, TRADE_MAX_USES_TAG, TRADE_DEFAULT_MAX_USES));
        this.tradeXpValue = Integer.toString(getTradeInt(recipe, TRADE_XP_TAG, 0));
        this.tradePriceMultiplierValue = Float.toString(getTradeFloat(recipe, TRADE_PRICE_MULTIPLIER_TAG,
                TRADE_DEFAULT_PRICE_MULTIPLIER));
        this.tradeSpecialPriceValue = Integer.toString(getTradeInt(recipe, TRADE_SPECIAL_PRICE_TAG, 0));
        this.tradeDemandValue = Integer.toString(getTradeInt(recipe, TRADE_DEMAND_TAG, 0));
        this.tradeRewardExp = !recipe.contains(TRADE_REWARD_EXP_TAG, Tag.TAG_BYTE) || recipe.getBoolean(TRADE_REWARD_EXP_TAG);
    }

    protected void resetTradeFieldValues() {
        this.tradeItemNbtValue = "{}";
        this.tradeUsesValue = "0";
        this.tradeMaxUsesValue = Integer.toString(TRADE_DEFAULT_MAX_USES);
        this.tradeXpValue = "0";
        this.tradePriceMultiplierValue = Float.toString(TRADE_DEFAULT_PRICE_MULTIPLIER);
        this.tradeSpecialPriceValue = "0";
        this.tradeDemandValue = "0";
        this.tradeRewardExp = true;
    }

    protected ListTag getVillagerTradeRecipes() {
        return getVillagerTradeRecipes(this.previewStack);
    }

    protected ListTag getVillagerTradeRecipes(ItemStack stack) {
        CompoundTag entityTag = ItemStackNbt.getElement(stack, ENTITY_TAG);
        if (entityTag == null || !entityTag.contains(OFFERS_TAG, Tag.TAG_COMPOUND)) {
            return new ListTag();
        }

        CompoundTag offers = entityTag.getCompound(OFFERS_TAG);
        if (!offers.contains(RECIPES_TAG, Tag.TAG_LIST)) {
            return new ListTag();
        }
        return offers.getList(RECIPES_TAG, Tag.TAG_COMPOUND);
    }

    protected int getVillagerTradeCount() {
        return getVillagerTradeRecipes().size();
    }

    protected CompoundTag getSelectedTradeRecipe() {
        ListTag trades = getVillagerTradeRecipes();
        if (trades.isEmpty()) {
            return null;
        }
        clampTradeSelection(trades);
        return trades.getCompound(this.selectedTradeIndex);
    }

    protected ListTag copyTradeRecipes(ListTag recipes) {
        ListTag copy = new ListTag();
        for (int i = 0; i < recipes.size(); i++) {
            copy.add(recipes.getCompound(i).copy());
        }
        return copy;
    }

    protected void putVillagerTradeRecipes(ListTag recipes) {
        CompoundTag tag = ItemStackNbt.getOrCreate(this.previewStack);
        CompoundTag entityTag = tag.getCompound(ENTITY_TAG);
        if (!recipes.isEmpty() && !entityTag.contains(ENTITY_ID_TAG, Tag.TAG_STRING)) {
            entityTag.putString(ENTITY_ID_TAG, "minecraft:villager");
        }
        if (!recipes.isEmpty()) {
            ensureVillagerData(entityTag);
        }

        CompoundTag offers = entityTag.contains(OFFERS_TAG, Tag.TAG_COMPOUND)
                ? entityTag.getCompound(OFFERS_TAG)
                : new CompoundTag();
        offers.put(RECIPES_TAG, recipes);
        entityTag.put(OFFERS_TAG, offers);
        tag.put(ENTITY_TAG, entityTag);
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    protected CompoundTag createDefaultTradeTag() {
        CompoundTag recipe = new CompoundTag();
        putTradeSlotItem(recipe, TRADE_SLOT_FIRST_BUY, new ItemStack(Items.EMERALD));
        putTradeSlotItem(recipe, TRADE_SLOT_SELL, new ItemStack(Items.EMERALD));
        recipe.putInt(TRADE_USES_TAG, 0);
        recipe.putInt(TRADE_MAX_USES_TAG, TRADE_DEFAULT_MAX_USES);
        recipe.putBoolean(TRADE_REWARD_EXP_TAG, true);
        recipe.putInt(TRADE_XP_TAG, 0);
        recipe.putFloat(TRADE_PRICE_MULTIPLIER_TAG, TRADE_DEFAULT_PRICE_MULTIPLIER);
        recipe.putInt(TRADE_SPECIAL_PRICE_TAG, 0);
        recipe.putInt(TRADE_DEMAND_TAG, 0);
        return recipe;
    }

    protected void putTradeSlotItem(CompoundTag recipe, int slot, ItemStack stack) {
        String tagName = getTradeSlotTagName(slot);
        if (stack.isEmpty()) {
            recipe.remove(tagName);
        } else {
            recipe.put(tagName, ItemStackNbt.save(stack));
        }
    }

    protected ItemStack getTradeSlotItem(CompoundTag recipe, int slot) {
        String tagName = getTradeSlotTagName(slot);
        if (!recipe.contains(tagName, Tag.TAG_COMPOUND)) {
            return ItemStack.EMPTY;
        }
        return ItemStackNbt.parse(recipe.getCompound(tagName));
    }

    protected String getTradeSlotTagName(int slot) {
        return switch (slot) {
            case TRADE_SLOT_SECOND_BUY -> TRADE_BUY_B_TAG;
            case TRADE_SLOT_SELL -> TRADE_SELL_TAG;
            default -> TRADE_BUY_TAG;
        };
    }

    protected ItemStack parseTradeSlotItem(String value) throws CommandSyntaxException {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty() || "{}".equals(trimmed)) {
            return ItemStack.EMPTY;
        }

        CompoundTag itemTag = TagParser.parseTag(trimmed);
        ItemStack stack = ItemStackNbt.parse(itemTag);
        if (stack.isEmpty()) {
            throw new IllegalArgumentException(Component.translatable(messageKey("editor_trade_invalid_item")).getString());
        }
        return stack;
    }

    protected String getTradeSlotItemNbt(ItemStack stack) {
        if (stack.isEmpty()) {
            return "{}";
        }
        return ItemStackNbt.save(stack).toString();
    }

    protected int parseTradeIntField(String value, String fieldSuffix, int defaultValue, int minValue, int maxValue) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            return defaultValue;
        }

        try {
            long parsed = Long.parseLong(normalized);
            if (parsed < minValue || parsed > maxValue) {
                throw new NumberFormatException();
            }
            return (int) parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(Component.translatable(messageKey("editor_trade_invalid_number"),
                    Component.translatable(key("trades." + fieldSuffix)), minValue, maxValue).getString());
        }
    }

    protected float parseTradeFloatField(String value, String fieldSuffix, float defaultValue) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            return defaultValue;
        }

        try {
            float parsed = Float.parseFloat(normalized);
            if (!Float.isFinite(parsed) || parsed < 0.0F) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(Component.translatable(messageKey("editor_trade_invalid_decimal"),
                    Component.translatable(key("trades." + fieldSuffix))).getString());
        }
    }

    protected int getTradeInt(CompoundTag recipe, String tagName, int defaultValue) {
        return recipe.contains(tagName, Tag.TAG_ANY_NUMERIC) ? recipe.getInt(tagName) : defaultValue;
    }

    protected float getTradeFloat(CompoundTag recipe, String tagName, float defaultValue) {
        return recipe.contains(tagName, Tag.TAG_ANY_NUMERIC) ? recipe.getFloat(tagName) : defaultValue;
    }

    protected String formatTradeRecipe(CompoundTag recipe) {
        ItemStack firstBuy = getTradeSlotItem(recipe, TRADE_SLOT_FIRST_BUY);
        ItemStack secondBuy = getTradeSlotItem(recipe, TRADE_SLOT_SECOND_BUY);
        ItemStack sell = getTradeSlotItem(recipe, TRADE_SLOT_SELL);
        String cost = formatTradeStack(firstBuy);
        if (!secondBuy.isEmpty()) {
            cost += " + " + formatTradeStack(secondBuy);
        }
        return cost + " -> " + formatTradeStack(sell);
    }

    protected String formatTradeStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return Component.translatable(key("trades.empty_slot")).getString();
        }
        return stack.getHoverName().getString();
    }

    protected void renderTradeSlotItem(GuiGraphics guiGraphics, CompoundTag recipe, int slot) {
        ItemStack stack = getTradeSlotItem(recipe, slot);
        int x = getSingleTradeSlotX(slot);
        int y = getSingleTradeSlotY();
        if (isSidebarUi()) {
            ModernUi.fillItemWell(guiGraphics, x + 8, y + 8, 30);
        } else if (stack.isEmpty()) {
            guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF555555);
            guiGraphics.fill(x, y, x + ITEM_SIZE, y + ITEM_SIZE, 0xFF1C1C1C);
        }
        if (stack.isEmpty()) {
            return;
        }
        guiGraphics.renderItem(stack, x, y);
        guiGraphics.renderItemDecorations(this.font, stack, x, y);
    }

    protected int getTradeListRowY(int index, int size) {
        return this.midY - TRADE_LIST_ROW_HEIGHT * (size + 1) / 2 + TRADE_LIST_ROW_HEIGHT * index;
    }

    protected int getHoveredTradeListIndex(int mouseX, int mouseY) {
        ListTag trades = getVillagerTradeRecipes();
        int size = trades.size();
        for (int i = 0; i < size; i++) {
            if (isMouseOverCenteredText(mouseX, mouseY, formatTradeRecipe(trades.getCompound(i)),
                    this.midX, getTradeListRowY(i, size))) {
                return i;
            }
        }
        return -1;
    }

    protected boolean isMouseOverAddTrade(int mouseX, int mouseY) {
        int size = getVillagerTradeCount();
        return isMouseOverCenteredText(mouseX, mouseY, Component.translatable(key("trades.addtrade")).getString(),
                this.midX, getTradeListRowY(size, size));
    }

    protected boolean isMouseOverCenteredText(int mouseX, int mouseY, String text, int centerX, int y) {
        int width = this.font.width(text);
        return isMouseIn(mouseX, mouseY, centerX - width / 2, y, width, TRADE_LIST_ROW_TEXT_HEIGHT);
    }

    protected int getSingleTradeSlotX(int slot) {
        if (isSidebarUi()) {
            int spacing = Mth.clamp(contentWidth() / 4, 58, 92);
            int center = contentMidX();
            return switch (slot) {
                case TRADE_SLOT_SECOND_BUY -> center - 8;
                case TRADE_SLOT_SELL -> center + spacing - 8;
                default -> center - spacing - 8;
            };
        }

        int part = this.midX / 2;
        return switch (slot) {
            case TRADE_SLOT_SECOND_BUY -> 2 * part;
            case TRADE_SLOT_SELL -> 3 * part;
            default -> part;
        };
    }

    protected int getSingleTradeSlotY() {
        return this.midY;
    }

    protected int getHoveredSingleTradeSlot(int mouseX, int mouseY) {
        for (int slot = 0; slot < TRADE_SLOT_COUNT; slot++) {
            if (isMouseIn(mouseX, mouseY, getSingleTradeSlotX(slot), getSingleTradeSlotY(), ITEM_SIZE, ITEM_SIZE)) {
                return slot;
            }
        }
        return -1;
    }

    protected void clampTradeSelection(ListTag trades) {
        this.selectedTradeSlot = Mth.clamp(this.selectedTradeSlot, 0, TRADE_SLOT_COUNT - 1);
        if (trades.isEmpty()) {
            this.selectedTradeIndex = 0;
            this.tradeScroll = 0;
            return;
        }

        this.selectedTradeIndex = Mth.clamp(this.selectedTradeIndex, 0, trades.size() - 1);
        this.tradeScroll = Mth.clamp(this.tradeScroll, 0, Math.max(0, trades.size() - TRADE_ROWS));
        scrollTradeSelectionIntoView(trades);
    }

    protected void setTradeScroll(int value) {
        ListTag trades = getVillagerTradeRecipes();
        this.tradeScroll = Mth.clamp(value, 0, Math.max(0, trades.size() - TRADE_ROWS));
    }

    protected void scrollTradeSelectionIntoView(ListTag trades) {
        if (trades.isEmpty()) {
            this.tradeScroll = 0;
            return;
        }

        if (this.selectedTradeIndex < this.tradeScroll) {
            this.tradeScroll = this.selectedTradeIndex;
        } else if (this.selectedTradeIndex >= this.tradeScroll + TRADE_ROWS) {
            this.tradeScroll = this.selectedTradeIndex - TRADE_ROWS + 1;
        }
        this.tradeScroll = Mth.clamp(this.tradeScroll, 0, Math.max(0, trades.size() - TRADE_ROWS));
    }

    protected int getTradeRowY(int row) {
        return 58 + row * TRADE_ROW_HEIGHT;
    }

    protected int getTradeListWidth() {
        return Math.min(210, Math.max(130, this.midX - 20));
    }

    protected int getTradeControlsX() {
        int width = getTradeControlsWidth();
        int rightAligned = this.width - width - 10;
        return Math.min(Math.max(this.midX + 100, rightAligned), rightAligned);
    }

    protected int getTradeControlsWidth() {
        return 132;
    }

    protected int getTradeSlotButtonX(int slot) {
        return this.midX - 117 + slot * 80;
    }

    protected int getTradeSlotIconX(int slot) {
        return getTradeSlotButtonX(slot) + 29;
    }

    protected int getTradeSlotIconY() {
        return 68;
    }

    protected int getHoveredTradeSlot(int mouseX, int mouseY) {
        for (int slot = 0; slot < TRADE_SLOT_COUNT; slot++) {
            if (isMouseIn(mouseX, mouseY, getTradeSlotIconX(slot) - 2, getTradeSlotIconY() - 2, 20, 20)) {
                return slot;
            }
        }
        return -1;
    }
}

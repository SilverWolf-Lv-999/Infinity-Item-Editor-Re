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
abstract class ItemEditorScreenCommon extends ItemEditorScreenState {
    protected ItemEditorScreenCommon(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(stack, targetContainerSlot, parentTradeScreen, parentTradeIndex, parentTradeSlot);
    }

protected void updateMouseDistance(int mouseX, int mouseY) {
        int distX = this.midX - mouseX;
        int distY = this.midY - mouseY;
        this.mouseDist = (int) Math.sqrt(distX * distX + distY * distY);
    }

    protected int getRingRadius() {
        return this.height / 3;
    }

    protected boolean isMouseOverCenter(double mouseX, double mouseY) {
        return mouseX > this.midX - CENTER_HIT_RADIUS
                && mouseX < this.midX + CENTER_HIT_RADIUS
                && mouseY > this.midY - CENTER_HIT_RADIUS
                && mouseY < this.midY + CENTER_HIT_RADIUS;
    }

    protected void insertFormattingPrefix() {
        insertFocusedText(String.valueOf(ChatFormatting.PREFIX_CODE));
    }

    protected void stripFocusedFormatting() {
        EditBox focused = getFocusedTextBox();
        if (focused == null) {
            return;
        }
        focused.setValue(Objects.requireNonNullElse(ChatFormatting.stripFormatting(focused.getValue()), ""));
    }

    protected void insertFocusedText(String text) {
        EditBox focused = getFocusedTextBox();
        if (focused != null) {
            focused.insertText(text);
        }
    }

    protected EditBox getFocusedTextBox() {
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

    protected List<Component> getPrettyNbtLines() {
        return NbtFormatter.prettyLines(this.previewStack.getTag());
    }

    protected List<NbtRow> buildNbtRows() {
        return NbtFormatter.rows(this.previewStack.getTag(), this.expandedNbtPaths);
    }

    protected int getNbtAdvancedVisibleRows() {
        return Math.max(1, (this.height - 75) / 12);
    }

    protected void readMainFieldsFromStack(ItemStack stack) {
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
        readTradeFieldsFromStack(stack);
    }

    protected String readLoreLine(String raw) {
        try {
            Component component = Component.Serializer.fromJson(raw);
            return component == null ? raw : component.getString();
        } catch (RuntimeException exception) {
            return raw;
        }
    }

    protected void readSignFieldsFromStack(ItemStack stack) {
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

    protected void readSignLine(int line, String raw) {
        Component component = readSerializedComponent(raw);
        this.signLineValues[line] = component.getString();
        if (line == 0) {
            ClickEvent clickEvent = component.getStyle().getClickEvent();
            if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                this.signCommandValue = clickEvent.getValue();
            }
        }
    }

    protected void readBookFieldsFromStack(ItemStack stack) {
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

    protected void readHeadFieldsFromStack(ItemStack stack) {
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

    protected void readFireworkFieldsFromStack(ItemStack stack) {
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

        this.fireworkExplosionType = Mth.clamp(FireworkRocketItem.Shape.getShape(explosion).getId(), 0, FIREWORK_EXPLOSION_TYPES - 1);
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

    protected void readContainerFieldsFromStack(ItemStack stack) {
        this.selectedContainerSlot = Mth.clamp(this.selectedContainerSlot, 0, CONTAINER_SIZE - 1);
        if (!isContainerEditableItem(stack)) {
            this.containerSlotNbtValue = "{}";
            return;
        }
        this.containerSlotNbtValue = getContainerSlotNbt(getContainerSlotItem(this.selectedContainerSlot));
    }

    protected CompoundTag getFireworkExplosionForFields(ItemStack stack) {
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

    protected void readBannerFieldsFromStack(ItemStack stack) {
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

    protected void readSpawnEggFieldsFromStack(ItemStack stack) {
        this.spawnEggCustomNameValue = "";
        this.spawnEggOwnerValue = "";
        this.spawnEggNumberValueOverrides.clear();
        this.spawnEggTagScroll = 0;
        if (!isSpawnEditorItem(stack)) {
            return;
        }

        CompoundTag entityTag = getSpawnEditorEntityTag(stack);
        if (entityTag != null) {
            if (entityTag.contains(ENTITY_CUSTOM_NAME_TAG, Tag.TAG_STRING)) {
                this.spawnEggCustomNameValue = readSerializedComponent(entityTag.getString(ENTITY_CUSTOM_NAME_TAG)).getString();
            }
            if (entityTag.hasUUID(ENTITY_OWNER_TAG)) {
                this.spawnEggOwnerValue = entityTag.getUUID(ENTITY_OWNER_TAG).toString();
            } else if (entityTag.contains(ENTITY_OWNER_TAG, Tag.TAG_STRING)) {
                this.spawnEggOwnerValue = entityTag.getString(ENTITY_OWNER_TAG);
            }
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

    protected Component readSerializedComponent(String raw) {
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

    protected void updateLoreScrollFromMouse(double mouseY) {
        int spaces = loreLineSpaces();
        int max = Math.max(0, this.loreValues.size() - spaces);
        if (max == 0) {
            setLoreScroll(0);
            return;
        }
        float perc = (float) ((mouseY - 50.0D) / (this.height - 99.0D));
        setLoreScroll(Mth.clamp(Math.round(max * perc), 0, max));
    }

    protected void setLoreScroll(int value) {
        int clamped = Mth.clamp(value, 0, Math.max(0, this.loreValues.size() - loreLineSpaces()));
        if (this.loreScroll != clamped) {
            this.loreScroll = clamped;
            rebuildWidgets();
        }
    }

    protected void captureFieldValues() {
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
        if (this.spawnEggOwnerBox != null) {
            this.spawnEggOwnerValue = this.spawnEggOwnerBox.getValue();
        }
        if (this.tradeItemNbtBox != null) {
            this.tradeItemNbtValue = this.tradeItemNbtBox.getValue();
        }
        if (this.tradeUsesBox != null) {
            this.tradeUsesValue = this.tradeUsesBox.getValue();
        }
        if (this.tradeMaxUsesBox != null) {
            this.tradeMaxUsesValue = this.tradeMaxUsesBox.getValue();
        }
        if (this.tradeXpBox != null) {
            this.tradeXpValue = this.tradeXpBox.getValue();
        }
        if (this.tradePriceMultiplierBox != null) {
            this.tradePriceMultiplierValue = this.tradePriceMultiplierBox.getValue();
        }
        if (this.tradeSpecialPriceBox != null) {
            this.tradeSpecialPriceValue = this.tradeSpecialPriceBox.getValue();
        }
        if (this.tradeDemandBox != null) {
            this.tradeDemandValue = this.tradeDemandBox.getValue();
        }
    }

    protected CompoundTag parseNbt(String nbt) throws CommandSyntaxException {
        String value = nbt == null ? "" : nbt.trim();
        if (value.isEmpty() || "{}".equals(value)) {
            return null;
        }
        return TagParser.parseTag(value);
    }

    protected void cleanupEmptyDisplayTag() {
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

    protected void cleanupEmptyTag() {
        CompoundTag tag = this.previewStack.getTag();
        if (tag != null && tag.isEmpty()) {
            this.previewStack.setTag(null);
        }
    }

    protected int loreLineSpaces() {
        return Math.max(1, ((this.height - 70) / 30) - 1);
    }

    protected boolean isMouseIn(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    protected boolean isTradeSlotEditor() {
        return this.parentTradeScreen != null && this.parentTradeIndex >= 0 && this.parentTradeSlot >= 0;
    }

    protected boolean isNameFollowingDefault(ItemStack stack) {
        String currentName = this.nameBox == null ? this.nameValue : this.nameBox.getValue();
        return !stack.hasCustomHoverName() && Objects.equals(currentName, getDefaultHoverName(stack));
    }

    protected String getDefaultHoverName(ItemStack stack) {
        ItemStack withoutName = stack.copy();
        withoutName.resetHoverName();
        return withoutName.getHoverName().getString();
    }

    protected static int getDamageMaxForField(ItemStack stack) {
        return stack.isDamageableItem() ? Math.max(0, stack.getMaxDamage()) : 99999;
    }

    protected static int parsePositiveOrZero(String value) {
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    protected static String normalizeItemId(String id) {
        String value = id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
        return value.contains(":") ? value : "minecraft:" + value;
    }

    protected static String stripMinecraftNamespace(ResourceLocation id) {
        return "minecraft".equals(id.getNamespace()) ? id.getPath() : id.toString();
    }

    protected static String getInitialNbt(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || tag.isEmpty() ? "{}" : tag.toString();
    }

    protected static boolean isColorApplicable(ItemStack stack) {
        return stack.getItem() instanceof DyeableLeatherItem || isPotionItem(stack) || isMapItem(stack);
    }

    protected static boolean canShowEnchantingButton(ItemStack stack) {
        return !stack.isEmpty();
    }

    protected static boolean isPotionItem(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION) || stack.is(Items.TIPPED_ARROW);
    }

    protected static boolean isMapItem(ItemStack stack) {
        return stack.is(Items.MAP) || stack.is(Items.FILLED_MAP);
    }

    protected static boolean isSignItem(ItemStack stack) {
        return stack.getItem() instanceof SignItem;
    }

    protected static boolean isBannerEditableItem(ItemStack stack) {
        return stack.getItem() instanceof BannerItem || stack.is(Items.SHIELD);
    }

    protected static boolean isSpawnEggItem(ItemStack stack) {
        return stack.getItem() instanceof SpawnEggItem;
    }

    protected static boolean isSpawnerItem(ItemStack stack) {
        return stack.is(Items.SPAWNER);
    }

    protected static boolean isSpawnEditorItem(ItemStack stack) {
        return isSpawnEggItem(stack) || isSpawnerItem(stack);
    }

    protected boolean isVillagerTradeEditableItem(ItemStack stack) {
        return isSpawnEggItem(stack) && EntityType.VILLAGER.equals(getCurrentSpawnEggEntityType(stack));
    }

    protected static boolean isPlayerHeadItem(ItemStack stack) {
        return stack.getItem() instanceof PlayerHeadItem;
    }

    protected static boolean isArmorStandItem(ItemStack stack) {
        return stack.is(Items.ARMOR_STAND);
    }

    protected static boolean isFireworkEditableItem(ItemStack stack) {
        return stack.is(Items.FIREWORK_ROCKET) || stack.is(Items.FIREWORK_STAR);
    }

    protected static boolean isContainerEditableItem(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        Block block = blockItem.getBlock();
        return block instanceof ChestBlock || block instanceof BarrelBlock || block instanceof ShulkerBoxBlock;
    }

    protected static boolean isBookEditableItem(ItemStack stack) {
        return stack.is(Items.WRITTEN_BOOK) || stack.is(Items.WRITABLE_BOOK);
    }

    protected int getBookGeneration() {
        CompoundTag tag = this.previewStack.getTag();
        return tag == null ? 0 : Mth.clamp(tag.getInt(BOOK_GENERATION_TAG), 0, MAX_BOOK_GENERATION);
    }

    protected int getBookPageCount() {
        CompoundTag tag = this.previewStack.getTag();
        return tag == null ? 0 : tag.getList(BOOK_PAGES_TAG, Tag.TAG_STRING).size();
    }

    protected Component getBookResolvedText() {
        CompoundTag tag = this.previewStack.getTag();
        boolean resolved = tag != null && tag.getBoolean(BOOK_RESOLVED_TAG);
        return Component.translatable(key("book.resolved." + (resolved ? 1 : 0)));
    }

    protected Component getBookSignButtonText() {
        if (this.previewStack.is(Items.WRITTEN_BOOK)) {
            return Component.translatable(key("book.unsign"));
        }
        return Component.translatable(key(this.rememberedSignedBookData == null ? "book.sign" : "book.resign"));
    }

    protected int getFireworkFlight() {
        CompoundTag fireworks = this.previewStack.getTagElement(FIREWORKS_TAG);
        if (fireworks == null) {
            return 1;
        }
        return Mth.clamp(fireworks.getByte(FIREWORK_FLIGHT_TAG), 1, MAX_FIREWORK_FLIGHT);
    }

    protected int getFireworkExplosionCount() {
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

    protected boolean hasFireworkData() {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null) {
            return false;
        }
        if (this.previewStack.is(Items.FIREWORK_ROCKET)) {
            return tag.contains(FIREWORKS_TAG, Tag.TAG_COMPOUND) && !tag.getCompound(FIREWORKS_TAG).isEmpty();
        }
        return tag.contains(FIREWORK_EXPLOSION_TAG, Tag.TAG_COMPOUND) && !tag.getCompound(FIREWORK_EXPLOSION_TAG).isEmpty();
    }

    protected Component getFireworkTypeName(int type) {
        return Component.translatable(key("firework.type." + Mth.clamp(type, 0, FIREWORK_EXPLOSION_TYPES - 1)));
    }

    protected Component getFireworkFadeColorText() {
        if (this.fireworkFadeColor < 0) {
            return Component.translatable(key("firework.fade.none"));
        }
        return getDyeColorName(getFireworkDyeColor(this.fireworkFadeColor));
    }

    protected DyeColor getFireworkDyeColor(int colorId) {
        return DyeColor.byId(Mth.positiveModulo(colorId, DyeColor.values().length));
    }

    protected static int getFireworkRgb(DyeColor color) {
        return color.getFireworkColor();
    }

    protected static FireworkRocketItem.Shape getFireworkShape(int type) {
        return FIREWORK_SHAPES[Mth.clamp(type, 0, FIREWORK_SHAPES.length - 1)];
    }

    protected static int getNearestFireworkDyeColorId(int rgb) {
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

    protected static String normalizeHeadText(String value) {
        return value == null ? "" : value.trim();
    }

    protected static UUID parseUuidOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    protected static int getDefaultAttributeSlot(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armorItem) {
            return getAttributeSlotNumber(armorItem.getEquipmentSlot());
        }
        if (stack.is(Items.SHIELD) || stack.is(Items.TOTEM_OF_UNDYING)) {
            return 2;
        }
        return 1;
    }

    protected static int getAttributeSlotNumber(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> 1;
            case OFFHAND -> 2;
            case HEAD -> 3;
            case CHEST -> 4;
            case LEGS -> 5;
            case FEET -> 6;
        };
    }

    protected static String getAttributeSlotName(int slot) {
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

    protected static String formatColorHex(int color) {
        return String.format(Locale.ROOT, "#%06x", color & 0xFFFFFF);
    }

    protected static int getRed(int color) {
        return color >> 16 & 255;
    }

    protected static int getGreen(int color) {
        return color >> 8 & 255;
    }

    protected static int getBlue(int color) {
        return color & 255;
    }

    protected static int argb(int alpha, int rgb) {
        return ((alpha & 255) << 24) | (rgb & 0xFFFFFF);
    }

    protected String formatRingEnchantmentName(Enchantment enchantment) {
        return enchantment.getFullname(getDisplayLevel(enchantment)).getString();
    }

    protected String formatStoredEnchantment(EnchantmentEntry entry) {
        if (entry.enchantment() == null) {
            return "Unknown ID (" + entry.id() + ") " + entry.level();
        }
        return entry.enchantment().getFullname(entry.level()).getString();
    }

    protected static int findEnchantmentIndex(ListTag enchantments, ResourceLocation id) {
        for (int i = 0; i < enchantments.size(); i++) {
            ResourceLocation storedId = ResourceLocation.tryParse(enchantments.getCompound(i).getString("id"));
            if (id.equals(storedId)) {
                return i;
            }
        }
        return -1;
    }

    protected static String getEnchantmentTagKey(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) ? BOOK_ENCHANTMENTS_TAG : ITEM_ENCHANTMENTS_TAG;
    }
}

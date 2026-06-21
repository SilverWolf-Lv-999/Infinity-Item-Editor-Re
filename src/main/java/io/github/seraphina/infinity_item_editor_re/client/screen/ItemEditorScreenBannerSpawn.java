package io.github.seraphina.infinity_item_editor_re.client.screen;

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
abstract class ItemEditorScreenBannerSpawn extends ItemEditorScreenItemData {
    protected ItemEditorScreenBannerSpawn(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(stack, targetContainerSlot, parentTradeScreen, parentTradeIndex, parentTradeSlot);
    }

protected void addSelectedBannerPattern() {
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

    protected void removeLastBannerPattern() {
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

    protected void clearBannerPatterns() {
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

    protected void cycleBannerBaseColor(int direction) {
        DyeColor color = DyeColor.byId(Mth.positiveModulo(getBannerBaseColor().getId() + direction, DyeColor.values().length));
        setBannerBaseColor(color);
        this.status = Component.translatable(messageKey("editor_banner_base_updated"), getDyeColorName(color));
        rebuildWidgets();
    }

    protected void cycleBannerPatternColor(int direction) {
        this.bannerPatternColor = Mth.positiveModulo(this.bannerPatternColor + direction, DyeColor.values().length);
        rebuildWidgets();
    }

    protected void cycleSelectedBannerPattern(int direction) {
        List<BannerPatternEntry> patterns = getFilteredBannerPatterns();
        if (patterns.isEmpty()) {
            return;
        }

        this.selectedBannerPatternIndex = Mth.positiveModulo(this.selectedBannerPatternIndex + direction, patterns.size());
        scrollBannerPatternSelectionIntoView(patterns);
    }

    protected void setBannerPatternScroll(int value) {
        List<BannerPatternEntry> patterns = getFilteredBannerPatterns();
        int maxScroll = Math.max(0, patterns.size() - BANNER_PATTERN_ROWS);
        this.bannerPatternScroll = Mth.clamp(value, 0, maxScroll);
        clampBannerPatternSelection(patterns);
        if (!patterns.isEmpty()) {
            int lastVisible = Math.min(patterns.size() - 1, this.bannerPatternScroll + BANNER_PATTERN_ROWS - 1);
            this.selectedBannerPatternIndex = Mth.clamp(this.selectedBannerPatternIndex, this.bannerPatternScroll, lastVisible);
        }
    }

    protected void scrollBannerPatternSelectionIntoView(List<BannerPatternEntry> patterns) {
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

    protected void clampBannerPatternSelection(List<BannerPatternEntry> patterns) {
        if (patterns.isEmpty()) {
            this.selectedBannerPatternIndex = 0;
            this.bannerPatternScroll = 0;
            return;
        }

        this.selectedBannerPatternIndex = Mth.clamp(this.selectedBannerPatternIndex, 0, patterns.size() - 1);
        this.bannerPatternScroll = Mth.clamp(this.bannerPatternScroll, 0, Math.max(0, patterns.size() - BANNER_PATTERN_ROWS));
    }

    protected int getBannerPatternRowY(int row) {
        return sideListStartY() + row * 10;
    }

    protected List<BannerPatternEntry> getFilteredBannerPatterns() {
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

    protected Component getBannerPatternName(BannerPatternEntry entry, DyeColor color) {
        return Component.translatable("block.minecraft.banner." + entry.name() + "." + color.getName());
    }

    protected void renderBannerPatternLayers(GuiGraphics guiGraphics) {
        ListTag patterns = getBannerPatterns();
        int x = this.midX - 70;
        int y = 124;
        if (isSidebarUi()) {
            ModernUi.fillPanel(guiGraphics, x - 8, y - 8, x + 164, y + 94, 8, ModernUi.SURFACE, ModernUi.BORDER);
        }
        guiGraphics.drawString(this.font, Component.translatable(key("banner.layers")), x, y,
                isSidebarUi() ? ModernUi.TEXT_MUTED : MAIN_COLOR);
        if (patterns.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable(key("banner.no_layers")), x, y + 12,
                    isSidebarUi() ? ModernUi.TEXT_SECONDARY : ALT_COLOR);
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
            guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(text, 150), x, y + 12 + (i - first) * 10,
                    isSidebarUi() ? ModernUi.TEXT_PRIMARY : MAIN_COLOR);
        }
    }

    protected void swapBannerAndShield() {
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

    protected void setBannerBaseColor(DyeColor color) {
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

    protected void replacePreviewItem(Item item) {
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

    protected void removeBannerBaseColorTag() {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null) {
            return;
        }

        CompoundTag blockEntity = tag.getCompound(BLOCK_ENTITY_TAG);
        blockEntity.remove(BANNER_BASE_TAG);
        cleanupBlockEntityTag(tag, blockEntity);
    }

    protected DyeColor getBannerBaseColor() {
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

    protected DyeColor getBannerPatternColor() {
        this.bannerPatternColor = Mth.positiveModulo(this.bannerPatternColor, DyeColor.values().length);
        return DyeColor.byId(this.bannerPatternColor);
    }

    protected Component getDyeColorName(DyeColor color) {
        return Component.translatable("color.minecraft." + color.getName());
    }

    protected int getBannerPatternCount() {
        return getBannerPatterns().size();
    }

    protected ListTag getBannerPatterns() {
        CompoundTag blockEntity = this.previewStack.getTagElement(BLOCK_ENTITY_TAG);
        if (blockEntity == null || !blockEntity.contains(BANNER_PATTERNS_TAG, Tag.TAG_LIST)) {
            return new ListTag();
        }
        return blockEntity.getList(BANNER_PATTERNS_TAG, Tag.TAG_COMPOUND);
    }

    protected CompoundTag getOrCreateBannerBlockEntityTag() {
        CompoundTag tag = this.previewStack.getOrCreateTag();
        CompoundTag blockEntity = tag.getCompound(BLOCK_ENTITY_TAG);
        tag.put(BLOCK_ENTITY_TAG, blockEntity);
        return blockEntity;
    }

    protected BannerPatternEntry getBannerPatternEntry(String hash) {
        for (BannerPatternEntry entry : BannerPatternCatalog.PATTERNS) {
            if (entry.hash().equals(hash)) {
                return entry;
            }
        }
        return null;
    }

    protected void applySelectedSpawnEggEntity() {
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

    protected void syncSpawnEggToSelectedEntityItem() {
        if (!isSpawnEggItem(this.previewStack)) {
            return;
        }

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

    protected void writeSpawnEggEntityId(SpawnEggEntityEntry entry) {
        CompoundTag entityTag = getOrCreateSpawnEditorEntityTag();
        entityTag.putString(ENTITY_ID_TAG, entry.id().toString());
        cleanupSpawnEggEntityTag(entityTag);
        if (isSpawnerItem(this.previewStack)) {
            clearSpawnerSpawnPotentials();
        }
    }

    protected void clearSpawnEggEntityTag() {
        if (!hasSpawnEditorEntityData(this.previewStack)) {
            return;
        }

        if (isSpawnEggItem(this.previewStack)) {
            CompoundTag tag = this.previewStack.getTag();
            if (tag != null) {
                tag.remove(ENTITY_TAG);
                cleanupEmptyTag();
            }
        } else if (isSpawnerItem(this.previewStack)) {
            clearSpawnerSpawnData();
        }
        this.spawnEggCustomNameValue = "";
        this.spawnEggOwnerValue = "";
        this.spawnEggNumberValueOverrides.clear();
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey(isSpawnerItem(this.previewStack)
                ? "editor_spawner_tag_cleared"
                : "editor_spawn_egg_tag_cleared"));
        readSpawnEggFieldsFromStack(this.previewStack);
        rebuildWidgets();
    }

    protected void toggleSpawnEggBoolean(SpawnEggTagRow row) {
        if (!isSpawnEditorItem(this.previewStack)) {
            return;
        }

        CompoundTag entityTag = getOrCreateSpawnEditorEntityTag();
        if (getSpawnEggBooleanValue(row)) {
            removeSpawnEggTagValue(entityTag, row.tagKey());
        } else {
            putSpawnEggBooleanValue(entityTag, row.tagKey(), true);
        }
        cleanupSpawnEggEntityTag(entityTag);
        this.status = Component.translatable(messageKey("editor_spawn_egg_field_updated"),
                Component.translatable(key("spawnegg." + row.translationSuffix())));
        rebuildWidgets();
    }

    protected void applySpawnEggCustomName(String value) {
        if (!isSpawnEditorItem(this.previewStack)) {
            return;
        }

        CompoundTag entityTag = getOrCreateSpawnEditorEntityTag();
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            entityTag.remove(ENTITY_CUSTOM_NAME_TAG);
        } else {
            entityTag.putString(ENTITY_CUSTOM_NAME_TAG, Component.Serializer.toJson(Component.literal(value)));
        }
        cleanupSpawnEggEntityTag(entityTag);
    }

    protected void applySpawnEggOwner(String value) {
        if (!isSpawnEditorItem(this.previewStack)) {
            return;
        }

        CompoundTag entityTag = getOrCreateSpawnEditorEntityTag();
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            entityTag.remove(ENTITY_OWNER_TAG);
        } else {
            UUID uuid = parseUuidOrNull(normalized);
            if (uuid == null) {
                entityTag.putString(ENTITY_OWNER_TAG, normalized);
            } else {
                entityTag.putUUID(ENTITY_OWNER_TAG, uuid);
            }
        }
        cleanupSpawnEggEntityTag(entityTag);
    }

    protected void applySpawnEggNumber(SpawnEggTagRow row, String value) {
        if (!isSpawnEditorItem(this.previewStack)) {
            return;
        }

        String normalized = value == null ? "" : value.trim();
        this.spawnEggNumberValueOverrides.put(row.tagKey(), normalized);
        CompoundTag entityTag = getOrCreateSpawnEditorEntityTag();
        if (normalized.isEmpty()) {
            removeSpawnEggTagValue(entityTag, row.tagKey());
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

            double storedValue = row.toStoredNumber(parsed);
            putSpawnEggNumberValue(entityTag, row, storedValue);
            cleanupSpawnEggEntityTag(entityTag);
        } catch (NumberFormatException exception) {
            this.status = Component.translatable(messageKey("editor_spawn_egg_invalid_number"),
                    Component.translatable(key("spawnegg." + row.translationSuffix())),
                    formatSpawnEggNumber(row.minValue()),
                    formatSpawnEggNumber(row.maxValue()));
        }
    }

    protected void cycleSpawnEggChoice(SpawnEggTagRow row) {
        if (!isSpawnEditorItem(this.previewStack) || row.choices() == null || row.choices().isEmpty()) {
            return;
        }

        CompoundTag entityTag = getOrCreateSpawnEditorEntityTag();
        String currentValue = getSpawnEggChoiceValue(row);
        int currentIndex = getSpawnEggChoiceIndex(row, currentValue);
        int nextIndex = currentIndex < 0 ? 0 : Mth.positiveModulo(currentIndex + 1, row.choices().size());
        SpawnEggChoiceOption nextOption = row.choices().get(nextIndex);
        if (nextOption.value().isEmpty()) {
            removeSpawnEggTagValue(entityTag, row.tagKey());
        } else if (row.choiceStorage() == SpawnEggChoiceStorage.INT) {
            putSpawnEggIntValue(entityTag, row.tagKey(), Integer.parseInt(nextOption.value()));
        } else {
            putSpawnEggStringValue(entityTag, row.tagKey(), nextOption.value());
        }
        cleanupSpawnEggEntityTag(entityTag);
        this.status = Component.translatable(messageKey("editor_spawn_egg_field_updated"),
                Component.translatable(key("spawnegg." + row.translationSuffix())));
        rebuildWidgets();
    }

    protected CompoundTag getOrCreateSpawnEditorEntityTag() {
        if (isSpawnerItem(this.previewStack)) {
            return getOrCreateSpawnerEntityTag();
        }

        CompoundTag tag = this.previewStack.getOrCreateTag();
        CompoundTag entityTag = tag.getCompound(ENTITY_TAG);
        tag.put(ENTITY_TAG, entityTag);
        return entityTag;
    }

    protected void cleanupSpawnEggEntityTag(CompoundTag entityTag) {
        if (isSpawnerItem(this.previewStack)) {
            cleanupSpawnerEntityTag(entityTag);
            return;
        }

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

    protected CompoundTag getOrCreateSpawnerEntityTag() {
        CompoundTag blockEntity = getOrCreateSpawnerBlockEntityTag();
        CompoundTag spawnData = blockEntity.getCompound(SPAWNER_SPAWN_DATA_TAG);
        CompoundTag entityTag = getSpawnerEntityFromSpawnData(spawnData);
        if (entityTag == null) {
            entityTag = getFirstSpawnerPotentialEntity(blockEntity);
        }
        if (entityTag == null) {
            entityTag = new CompoundTag();
        }
        if (!entityTag.contains(ENTITY_ID_TAG, Tag.TAG_STRING)) {
            SpawnEggEntityEntry entry = getSelectedSpawnEggEntityEntry();
            if (entry != null) {
                entityTag.putString(ENTITY_ID_TAG, entry.id().toString());
            }
        }
        putSpawnerSpawnData(blockEntity, spawnData, entityTag);
        return entityTag;
    }

    protected CompoundTag getOrCreateSpawnerBlockEntityTag() {
        CompoundTag tag = this.previewStack.getOrCreateTag();
        CompoundTag blockEntity = tag.getCompound(BLOCK_ENTITY_TAG);
        blockEntity.putString(ENTITY_ID_TAG, SPAWNER_BLOCK_ENTITY_ID);
        tag.put(BLOCK_ENTITY_TAG, blockEntity);
        return blockEntity;
    }

    protected void cleanupSpawnerEntityTag(CompoundTag entityTag) {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null) {
            return;
        }

        CompoundTag blockEntity = tag.getCompound(BLOCK_ENTITY_TAG);
        if (entityTag.isEmpty()) {
            CompoundTag spawnData = blockEntity.getCompound(SPAWNER_SPAWN_DATA_TAG);
            spawnData.remove(SPAWNER_ENTITY_TAG);
            if (spawnData.isEmpty()) {
                blockEntity.remove(SPAWNER_SPAWN_DATA_TAG);
            } else {
                blockEntity.put(SPAWNER_SPAWN_DATA_TAG, spawnData);
            }
        } else {
            CompoundTag spawnData = blockEntity.getCompound(SPAWNER_SPAWN_DATA_TAG);
            putSpawnerSpawnData(blockEntity, spawnData, entityTag);
            blockEntity.putString(ENTITY_ID_TAG, SPAWNER_BLOCK_ENTITY_ID);
        }
        cleanupSpawnerBlockEntityTag(blockEntity);
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    protected void clearSpawnerSpawnData() {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null || !tag.contains(BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag blockEntity = tag.getCompound(BLOCK_ENTITY_TAG);
        blockEntity.remove(SPAWNER_SPAWN_DATA_TAG);
        blockEntity.remove(SPAWNER_SPAWN_POTENTIALS_TAG);
        cleanupSpawnerBlockEntityTag(blockEntity);
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    protected void clearSpawnerSpawnPotentials() {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null || !tag.contains(BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag blockEntity = tag.getCompound(BLOCK_ENTITY_TAG);
        blockEntity.remove(SPAWNER_SPAWN_POTENTIALS_TAG);
        cleanupSpawnerBlockEntityTag(blockEntity);
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    protected void cleanupSpawnerBlockEntityTag(CompoundTag blockEntity) {
        CompoundTag tag = this.previewStack.getTag();
        if (tag == null) {
            return;
        }

        if (blockEntity.isEmpty() || isOnlySpawnerBlockEntityId(blockEntity)) {
            tag.remove(BLOCK_ENTITY_TAG);
        } else {
            tag.put(BLOCK_ENTITY_TAG, blockEntity);
        }
        cleanupEmptyTag();
    }

    protected boolean isOnlySpawnerBlockEntityId(CompoundTag blockEntity) {
        return blockEntity.size() == 1
                && blockEntity.contains(ENTITY_ID_TAG, Tag.TAG_STRING)
                && SPAWNER_BLOCK_ENTITY_ID.equals(blockEntity.getString(ENTITY_ID_TAG));
    }

    protected Component getSpawnEggBooleanText(SpawnEggTagRow row) {
        return Component.translatable(key("spawnegg.option_state"),
                Component.translatable(key("spawnegg." + row.translationSuffix())),
                Component.translatable(key("spawnegg.state." + (getSpawnEggBooleanValue(row) ? 1 : 0))));
    }

    protected Component getSpawnEggChoiceText(SpawnEggTagRow row) {
        return Component.translatable(key("spawnegg.option_state"),
                Component.translatable(key("spawnegg." + row.translationSuffix())),
                getSpawnEggChoiceOptionText(row));
    }

    protected Component getSpawnEggChoiceOptionText(SpawnEggTagRow row) {
        String currentValue = getSpawnEggChoiceValue(row);
        int index = getSpawnEggChoiceIndex(row, currentValue);
        if (index >= 0) {
            SpawnEggChoiceOption option = row.choices().get(index);
            return Component.translatable(key("spawnegg." + row.translationSuffix() + "." + option.translationSuffix()));
        }
        return currentValue.isEmpty()
                ? Component.translatable(key("spawnegg.choice.empty"))
                : Component.literal(currentValue);
    }

    protected String getSpawnEggChoiceValue(SpawnEggTagRow row) {
        CompoundTag entityTag = getSpawnEditorEntityTag(this.previewStack);
        CompoundTag parent = getSpawnEggTagParent(entityTag, row.tagKey(), false);
        if (parent == null) {
            return "";
        }

        String leafKey = getSpawnEggLeafTagKey(row.tagKey());
        if (row.choiceStorage() == SpawnEggChoiceStorage.INT) {
            return parent.contains(leafKey, Tag.TAG_ANY_NUMERIC) ? Integer.toString(parent.getInt(leafKey)) : "";
        }
        return parent.contains(leafKey, Tag.TAG_STRING) ? parent.getString(leafKey) : "";
    }

    protected int getSpawnEggChoiceIndex(SpawnEggTagRow row, String value) {
        for (int i = 0; i < row.choices().size(); i++) {
            if (Objects.equals(row.choices().get(i).value(), value)) {
                return i;
            }
        }
        return -1;
    }

    protected boolean getSpawnEggBooleanValue(SpawnEggTagRow row) {
        CompoundTag entityTag = getSpawnEditorEntityTag(this.previewStack);
        CompoundTag parent = getSpawnEggTagParent(entityTag, row.tagKey(), false);
        if (parent == null) {
            return false;
        }
        String leafKey = getSpawnEggLeafTagKey(row.tagKey());
        return parent.contains(leafKey, Tag.TAG_BYTE) && parent.getBoolean(leafKey);
    }

    protected String getSpawnEggNumberValue(SpawnEggTagRow row) {
        String override = this.spawnEggNumberValueOverrides.get(row.tagKey());
        if (override != null) {
            return override;
        }

        CompoundTag entityTag = getSpawnEditorEntityTag(this.previewStack);
        CompoundTag parent = getSpawnEggTagParent(entityTag, row.tagKey(), false);
        if (parent == null) {
            return "";
        }

        String leafKey = getSpawnEggLeafTagKey(row.tagKey());
        if (!parent.contains(leafKey, Tag.TAG_ANY_NUMERIC)) {
            return "";
        }
        return switch (row.numberType()) {
            case BYTE -> formatSpawnEggNumber(row.toDisplayNumber(parent.getByte(leafKey)));
            case SHORT -> formatSpawnEggNumber(row.toDisplayNumber(parent.getShort(leafKey)));
            case INT -> formatSpawnEggNumber(row.toDisplayNumber(parent.getInt(leafKey)));
            case FLOAT -> Float.toString((float) row.toDisplayNumber(parent.getFloat(leafKey)));
        };
    }

    protected int getSpawnEggTagTextMaxLength(SpawnEggTagRow row) {
        return switch (row.type()) {
            case CUSTOM_NAME -> 256;
            case OWNER -> SPAWN_EGG_OWNER_MAX_LENGTH;
            default -> 16;
        };
    }

    protected String getSpawnEggTagTextValue(SpawnEggTagRow row) {
        return switch (row.type()) {
            case CUSTOM_NAME -> this.spawnEggCustomNameValue;
            case OWNER -> this.spawnEggOwnerValue;
            default -> getSpawnEggNumberValue(row);
        };
    }

    protected CompoundTag getSpawnEggTagParent(CompoundTag entityTag, String tagPath, boolean create) {
        if (entityTag == null) {
            return null;
        }

        String[] parts = tagPath.split("\\.");
        CompoundTag current = entityTag;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!current.contains(part, Tag.TAG_COMPOUND)) {
                if (!create) {
                    return null;
                }
                current.put(part, new CompoundTag());
            }
            current = current.getCompound(part);
        }
        return current;
    }

    protected String getSpawnEggLeafTagKey(String tagPath) {
        int dot = tagPath.lastIndexOf('.');
        return dot < 0 ? tagPath : tagPath.substring(dot + 1);
    }

    protected void putSpawnEggBooleanValue(CompoundTag entityTag, String tagPath, boolean value) {
        CompoundTag parent = getSpawnEggTagParent(entityTag, tagPath, true);
        if (parent != null) {
            parent.putBoolean(getSpawnEggLeafTagKey(tagPath), value);
        }
    }

    protected void putSpawnEggStringValue(CompoundTag entityTag, String tagPath, String value) {
        CompoundTag parent = getSpawnEggTagParent(entityTag, tagPath, true);
        if (parent != null) {
            parent.putString(getSpawnEggLeafTagKey(tagPath), value);
        }
    }

    protected void putSpawnEggIntValue(CompoundTag entityTag, String tagPath, int value) {
        CompoundTag parent = getSpawnEggTagParent(entityTag, tagPath, true);
        if (parent != null) {
            parent.putInt(getSpawnEggLeafTagKey(tagPath), value);
        }
    }

    protected void putSpawnEggNumberValue(CompoundTag entityTag, SpawnEggTagRow row, double storedValue) {
        CompoundTag parent = getSpawnEggTagParent(entityTag, row.tagKey(), true);
        if (parent == null) {
            return;
        }

        String leafKey = getSpawnEggLeafTagKey(row.tagKey());
        switch (row.numberType()) {
            case BYTE -> parent.putByte(leafKey, (byte) storedValue);
            case SHORT -> parent.putShort(leafKey, (short) storedValue);
            case INT -> parent.putInt(leafKey, (int) storedValue);
            case FLOAT -> parent.putFloat(leafKey, (float) storedValue);
        }
    }

    protected void removeSpawnEggTagValue(CompoundTag entityTag, String tagPath) {
        if (entityTag == null) {
            return;
        }
        removeSpawnEggTagValue(entityTag, tagPath.split("\\."), 0);
    }

    protected boolean removeSpawnEggTagValue(CompoundTag current, String[] parts, int index) {
        if (index >= parts.length - 1) {
            current.remove(parts[index]);
            return current.isEmpty();
        }

        String part = parts[index];
        if (!current.contains(part, Tag.TAG_COMPOUND)) {
            return current.isEmpty();
        }

        CompoundTag child = current.getCompound(part);
        if (removeSpawnEggTagValue(child, parts, index + 1)) {
            current.remove(part);
        } else {
            current.put(part, child);
        }
        return current.isEmpty();
    }

    protected boolean isAllowedSpawnEggNumber(String value, SpawnEggNumberType type) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return type == SpawnEggNumberType.FLOAT
                ? value.matches("-?\\d*(\\.\\d*)?")
                : value.matches("-?\\d*");
    }

    protected boolean isPartialSpawnEggNumber(String value) {
        return "-".equals(value) || ".".equals(value) || "-.".equals(value);
    }

    protected String formatSpawnEggNumber(double value) {
        if (value == (long) value) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    protected void setSpawnEggEntityScroll(int value) {
        List<SpawnEggEntityEntry> entities = getFilteredSpawnEggEntities();
        int maxScroll = Math.max(0, entities.size() - SPAWN_EGG_ENTITY_ROWS);
        this.spawnEggEntityScroll = Mth.clamp(value, 0, maxScroll);
        clampSpawnEggEntitySelection(entities);
        if (!entities.isEmpty()) {
            int lastVisible = Math.min(entities.size() - 1, this.spawnEggEntityScroll + SPAWN_EGG_ENTITY_ROWS - 1);
            this.selectedSpawnEggEntityIndex = Mth.clamp(this.selectedSpawnEggEntityIndex, this.spawnEggEntityScroll, lastVisible);
        }
    }

    protected void setSpawnEggTagScroll(int value) {
        int maxScroll = Math.max(0, getSpawnEggTagRows().size() - SPAWN_EGG_TAG_ROWS);
        this.spawnEggTagScroll = Mth.clamp(value, 0, maxScroll);
    }

    protected void cycleSelectedSpawnEggEntity(int direction) {
        List<SpawnEggEntityEntry> entities = getFilteredSpawnEggEntities();
        if (entities.isEmpty()) {
            return;
        }

        this.selectedSpawnEggEntityIndex = Mth.positiveModulo(this.selectedSpawnEggEntityIndex + direction, entities.size());
        this.spawnEggTagScroll = 0;
        scrollSpawnEggSelectionIntoView(entities);
        rebuildWidgets();
    }

    protected void scrollSpawnEggSelectionIntoView(List<SpawnEggEntityEntry> entities) {
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

    protected void clampSpawnEggEntitySelection(List<SpawnEggEntityEntry> entities) {
        if (entities.isEmpty()) {
            this.selectedSpawnEggEntityIndex = 0;
            this.spawnEggEntityScroll = 0;
            return;
        }

        this.selectedSpawnEggEntityIndex = Mth.clamp(this.selectedSpawnEggEntityIndex, 0, entities.size() - 1);
        this.spawnEggEntityScroll = Mth.clamp(this.spawnEggEntityScroll, 0, Math.max(0, entities.size() - SPAWN_EGG_ENTITY_ROWS));
    }

    protected int getSpawnEggEntityRowY(int row) {
        return sideListStartY() + row * 10;
    }

    protected int getSpawnEggTagRowY(int row) {
        return 138 + row * SPAWN_EGG_TAG_ROW_HEIGHT;
    }

    protected int getSpawnEggControlsX() {
        int width = getSpawnEggControlsWidth();
        return rightControlsX(width, spawnEggEntityListX(), spawnEggEntityListWidth());
    }

    protected int getSpawnEggControlsWidth() {
        return 132;
    }

    protected SpawnEggEntityEntry getSelectedSpawnEggEntityEntry() {
        List<SpawnEggEntityEntry> entities = getFilteredSpawnEggEntities();
        clampSpawnEggEntitySelection(entities);
        if (entities.isEmpty()) {
            return null;
        }
        return entities.get(this.selectedSpawnEggEntityIndex);
    }

    protected List<SpawnEggEntityEntry> getFilteredSpawnEggEntities() {
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

    protected String formatSpawnEggEntityEntry(SpawnEggEntityEntry entry) {
        return getSpawnEggEntityName(entry).getString() + " (" + stripMinecraftNamespace(entry.id()) + ")";
    }

    protected Component getSpawnEggEntityName(SpawnEggEntityEntry entry) {
        return entry.type().getDescription();
    }

    protected Component getCurrentSpawnEggEntityName() {
        EntityType<?> type = getCurrentSpawnEggEntityType(this.previewStack);
        if (type != null) {
            return type.getDescription();
        }

        String rawId = getSpawnEggEntityIdOverride(this.previewStack);
        return rawId.isEmpty() ? Component.translatable(key(getSpawnEditorDefaultEntityKey())) : Component.literal(rawId);
    }

    protected EntityType<?> getCurrentSpawnEggEntityType(ItemStack stack) {
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

    protected String getSpawnEggEntityIdOverride(ItemStack stack) {
        CompoundTag entityTag = getSpawnEditorEntityTag(stack);
        if (entityTag != null && entityTag.contains(ENTITY_ID_TAG, Tag.TAG_STRING)) {
            return entityTag.getString(ENTITY_ID_TAG);
        }
        return "";
    }

    protected CompoundTag getSpawnEditorEntityTag(ItemStack stack) {
        if (isSpawnerItem(stack)) {
            return getSpawnerEntityTag(stack);
        }
        return stack.getTagElement(ENTITY_TAG);
    }

    protected CompoundTag getSpawnerEntityTag(ItemStack stack) {
        CompoundTag blockEntity = stack.getTagElement(BLOCK_ENTITY_TAG);
        if (blockEntity == null) {
            return null;
        }
        if (blockEntity.contains(SPAWNER_SPAWN_DATA_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag spawnData = blockEntity.getCompound(SPAWNER_SPAWN_DATA_TAG);
            CompoundTag entityTag = getSpawnerEntityFromSpawnData(spawnData);
            if (entityTag != null) {
                return entityTag;
            }
        }
        return getFirstSpawnerPotentialEntity(blockEntity);
    }

    protected CompoundTag getFirstSpawnerPotentialEntity(CompoundTag blockEntity) {
        if (blockEntity.contains(SPAWNER_SPAWN_POTENTIALS_TAG, Tag.TAG_LIST)) {
            ListTag potentials = blockEntity.getList(SPAWNER_SPAWN_POTENTIALS_TAG, Tag.TAG_COMPOUND);
            if (!potentials.isEmpty()) {
                CompoundTag potential = potentials.getCompound(0);
                CompoundTag entityTag = getSpawnerEntityFromPotential(potential);
                if (entityTag != null) {
                    return entityTag;
                }
            }
        }
        return null;
    }

    protected CompoundTag getSpawnerEntityFromPotential(CompoundTag potential) {
        if (potential.contains(SPAWNER_POTENTIAL_DATA_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag entityTag = getSpawnerEntityFromSpawnData(potential.getCompound(SPAWNER_POTENTIAL_DATA_TAG));
            if (entityTag != null) {
                return entityTag;
            }
        }
        if (potential.contains(SPAWNER_POTENTIAL_LEGACY_ENTITY_TAG, Tag.TAG_COMPOUND)) {
            return potential.getCompound(SPAWNER_POTENTIAL_LEGACY_ENTITY_TAG).copy();
        }
        return null;
    }

    protected CompoundTag getSpawnerEntityFromSpawnData(CompoundTag spawnData) {
        if (spawnData.contains(SPAWNER_ENTITY_TAG, Tag.TAG_COMPOUND)) {
            return spawnData.getCompound(SPAWNER_ENTITY_TAG).copy();
        }
        if (spawnData.contains(ENTITY_ID_TAG, Tag.TAG_STRING)) {
            return spawnData.copy();
        }
        return null;
    }

    protected void putSpawnerSpawnData(CompoundTag blockEntity, CompoundTag originalSpawnData, CompoundTag entityTag) {
        CompoundTag spawnData = new CompoundTag();
        if (originalSpawnData.contains(SPAWNER_CUSTOM_SPAWN_RULES_TAG, Tag.TAG_COMPOUND)) {
            spawnData.put(SPAWNER_CUSTOM_SPAWN_RULES_TAG, originalSpawnData.getCompound(SPAWNER_CUSTOM_SPAWN_RULES_TAG).copy());
        }
        spawnData.put(SPAWNER_ENTITY_TAG, entityTag);
        blockEntity.put(SPAWNER_SPAWN_DATA_TAG, spawnData);
    }

    protected boolean hasSpawnEditorEntityData(ItemStack stack) {
        if (isSpawnerItem(stack)) {
            CompoundTag blockEntity = stack.getTagElement(BLOCK_ENTITY_TAG);
            return blockEntity != null
                    && (blockEntity.contains(SPAWNER_SPAWN_DATA_TAG, Tag.TAG_COMPOUND)
                    || blockEntity.contains(SPAWNER_SPAWN_POTENTIALS_TAG, Tag.TAG_LIST));
        }
        return stack.getTagElement(ENTITY_TAG) != null;
    }

    protected String getSpawnEditorTitleKey() {
        return isSpawnerItem(this.previewStack) ? "spawner" : "spawnegg";
    }

    protected String getSpawnEditorClearKey() {
        return isSpawnerItem(this.previewStack) ? "spawner.clear_entity_tag" : "spawnegg.clear_entity_tag";
    }

    protected String getSpawnEditorDefaultEntityKey() {
        return isSpawnerItem(this.previewStack) ? "spawner.default_entity" : "spawnegg.default_entity";
    }

    protected List<SpawnEggTagRow> getSpawnEggTagRows() {
        SpawnEggEntityEntry entry = getSelectedSpawnEggEntityEntry();
        String path = entry == null ? "" : entry.id().getPath();
        List<SpawnEggTagRow> rows = new ArrayList<>(SpawnEggTagRows.GENERAL);
        rows.addAll(SpawnEggTagRows.forEntity(path));
        return rows;
    }
}

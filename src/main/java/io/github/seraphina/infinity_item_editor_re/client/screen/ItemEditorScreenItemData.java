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
abstract class ItemEditorScreenItemData extends ItemEditorScreenCommon {
    protected ItemEditorScreenItemData(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(stack, targetContainerSlot, parentTradeScreen, parentTradeIndex, parentTradeSlot);
    }

protected void applySignToStack() {
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

    protected void applyBookMetadataToStack() {
        if (!this.previewStack.is(Items.WRITTEN_BOOK)) {
            return;
        }

        CompoundTag tag = this.previewStack.getOrCreateTag();
        tag.putString(BOOK_TITLE_TAG, this.bookTitleValue == null ? "" : this.bookTitleValue);
        tag.putString(BOOK_AUTHOR_TAG, this.bookAuthorValue == null ? "" : this.bookAuthorValue);
        cleanupEmptyTag();
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    protected void cycleBookGeneration() {
        if (!this.previewStack.is(Items.WRITTEN_BOOK)) {
            return;
        }

        CompoundTag tag = this.previewStack.getOrCreateTag();
        int current = Mth.clamp(tag.getInt(BOOK_GENERATION_TAG), 0, MAX_BOOK_GENERATION);
        int next = Mth.positiveModulo(current + 1, MAX_BOOK_GENERATION + 1);
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

    protected void toggleBookResolved() {
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

    protected void toggleBookSignedState() {
        captureFieldValues();
        if (this.previewStack.is(Items.WRITTEN_BOOK)) {
            unsignBook();
        } else if (this.previewStack.is(Items.WRITABLE_BOOK)) {
            signBook();
        }
    }

    protected void unsignBook() {
        CompoundTag originalTag = this.previewStack.getTag();
        this.rememberedSignedBookData = originalTag == null ? new CompoundTag() : originalTag.copy();
        ItemStack writableBook = new ItemStack(Items.WRITABLE_BOOK, Math.max(1, this.previewStack.getCount()));
        CompoundTag writableTag = originalTag == null ? null : originalTag.copy();
        if (writableTag != null) {
            convertWrittenPagesToWritable(writableTag);
            writableTag.remove(BOOK_TITLE_TAG);
            writableTag.remove(BOOK_FILTERED_TITLE_TAG);
            writableTag.remove(BOOK_AUTHOR_TAG);
            writableTag.remove(BOOK_GENERATION_TAG);
            writableTag.remove(BOOK_RESOLVED_TAG);
            writableTag.remove(BOOK_FILTERED_PAGES_TAG);
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

    protected void signBook() {
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
        signedTag.remove(BOOK_FILTERED_TITLE_TAG);
        signedTag.putString(BOOK_AUTHOR_TAG, this.bookAuthorValue == null ? "" : this.bookAuthorValue);
        signedTag.remove(BOOK_FILTERED_PAGES_TAG);
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

    protected void convertWrittenPagesToWritable(CompoundTag tag) {
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

    protected void convertWritablePagesToWritten(CompoundTag tag) {
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

    protected Component readBookPageComponent(String raw) {
        Component parsed = readSerializedComponent(raw);
        if (!parsed.getString().equals(raw) || isProbablyJsonText(raw)) {
            return parsed;
        }
        return Component.literal(raw);
    }

    protected boolean isProbablyJsonText(String raw) {
        String value = raw == null ? "" : raw.trim();
        return value.startsWith("{") || value.startsWith("[") || value.startsWith("\"");
    }

    protected void applyHeadToStack() {
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

    protected void clearHeadOwner() {
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

    protected void randomizeHeadUuid() {
        this.headUuidValue = UUID.randomUUID().toString();
        if (this.headUuidBox != null) {
            this.headUuidBox.setValue(this.headUuidValue);
        }
        applyHeadToStack();
        this.status = Component.translatable(messageKey("editor_head_uuid_randomized"));
    }

    protected void toggleArmorStandFlag(String tagKey, String translationSuffix) {
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

    protected void clearArmorStandEntityTag() {
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

    protected CompoundTag getOrCreateArmorStandEntityTag() {
        CompoundTag tag = this.previewStack.getOrCreateTag();
        CompoundTag entityTag = tag.getCompound(ENTITY_TAG);
        tag.put(ENTITY_TAG, entityTag);
        return entityTag;
    }

    protected void cleanupArmorStandEntityTag(CompoundTag entityTag) {
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

    protected Component getArmorStandToggleText(String translationSuffix, String tagKey) {
        return Component.translatable(key("armorstand." + translationSuffix + "." + (getArmorStandFlag(tagKey) ? 1 : 0)));
    }

    protected boolean getArmorStandFlag(String tagKey) {
        CompoundTag entityTag = this.previewStack.getTagElement(ENTITY_TAG);
        return entityTag != null && entityTag.getBoolean(tagKey);
    }

    protected void cycleFireworkFlight() {
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

    protected void cycleFireworkExplosionType(int direction) {
        if (!isFireworkEditableItem(this.previewStack)) {
            return;
        }

        this.fireworkExplosionType = Mth.positiveModulo(this.fireworkExplosionType + direction, FIREWORK_EXPLOSION_TYPES);
        applyFireworkControlsToStack();
        this.status = Component.translatable(messageKey("editor_firework_updated"));
        rebuildWidgets();
    }

    protected void toggleFireworkFlicker() {
        if (!isFireworkEditableItem(this.previewStack)) {
            return;
        }

        this.fireworkFlicker = !this.fireworkFlicker;
        applyFireworkControlsToStack();
        this.status = Component.translatable(messageKey("editor_firework_updated"));
        rebuildWidgets();
    }

    protected void toggleFireworkTrail() {
        if (!isFireworkEditableItem(this.previewStack)) {
            return;
        }

        this.fireworkTrail = !this.fireworkTrail;
        applyFireworkControlsToStack();
        this.status = Component.translatable(messageKey("editor_firework_updated"));
        rebuildWidgets();
    }

    protected void cycleFireworkColor(boolean fade, int direction) {
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

    protected void randomizeFireworkColors() {
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

    protected void addFireworkExplosion() {
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

    protected void removeLastFireworkExplosion() {
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

    protected void clearFireworkData() {
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

    protected void applyFireworkControlsToStack() {
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

    protected CompoundTag createFireworkExplosionTag() {
        CompoundTag explosion = new CompoundTag();
        getFireworkShape(this.fireworkExplosionType).save(explosion);
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

    protected CompoundTag getOrCreateFireworksTag() {
        CompoundTag tag = this.previewStack.getOrCreateTag();
        CompoundTag fireworks = tag.getCompound(FIREWORKS_TAG);
        tag.put(FIREWORKS_TAG, fireworks);
        return fireworks;
    }

    protected void cleanupFireworksTag(CompoundTag fireworks) {
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

    protected void cycleContainerSlot(int direction) {
        this.selectedContainerSlot = Mth.positiveModulo(this.selectedContainerSlot + direction, CONTAINER_SIZE);
        this.containerSlotNbtValue = getContainerSelectedSlotNbt();
        if (this.containerSlotNbtBox != null) {
            this.containerSlotNbtBox.setValue(this.containerSlotNbtValue);
            this.containerSlotNbtBox.setCursorPosition(0);
        }
    }

    protected void updateContainerSlotFromNbt() {
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

    protected ItemStack parseContainerSlotItem(String value) throws CommandSyntaxException {
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

    protected void clearContainerSlot() {
        if (!isContainerEditableItem(this.previewStack)) {
            return;
        }

        setContainerSlotItem(this.selectedContainerSlot, ItemStack.EMPTY);
        this.containerSlotNbtValue = "{}";
        this.status = Component.translatable(messageKey("editor_container_slot_cleared"), this.selectedContainerSlot + 1);
        rebuildWidgets();
    }

    protected void clearContainerItems() {
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

    protected void setContainerSlotItem(int slot, ItemStack slotStack) {
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

    protected ItemStack getContainerSlotItem(int slot) {
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

    protected ListTag getContainerItemsList() {
        CompoundTag blockEntity = this.previewStack.getTagElement(BLOCK_ENTITY_TAG);
        if (blockEntity == null || !blockEntity.contains(CONTAINER_ITEMS_TAG, Tag.TAG_LIST)) {
            return new ListTag();
        }
        return blockEntity.getList(CONTAINER_ITEMS_TAG, Tag.TAG_COMPOUND);
    }

    protected int getContainerItemCount() {
        int count = 0;
        for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
            if (!getContainerSlotItem(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    protected String getContainerSelectedSlotNbt() {
        return getContainerSlotNbt(getContainerSlotItem(this.selectedContainerSlot));
    }

    protected String getContainerSlotNbt(ItemStack stack) {
        if (stack.isEmpty()) {
            return "{}";
        }
        return stack.save(new CompoundTag()).toString();
    }

    protected int getContainerGridX() {
        return this.midX - (CONTAINER_COLUMNS * CONTAINER_SLOT_PIXEL_SIZE) / 2;
    }

    protected int getContainerGridY() {
        return 48;
    }

    protected int getHoveredContainerSlot(int mouseX, int mouseY) {
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

    protected MutableComponent createSignLineComponent(int line) {
        MutableComponent component = Component.literal(getSignLineValue(line));
        if (line == 0) {
            String command = getNormalizedSignCommand();
            if (!command.isEmpty()) {
                component.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));
            }
        }
        return component;
    }

    protected boolean hasSignContent() {
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

    protected String getSignLineValue(int line) {
        if (line < 0 || line >= this.signLineValues.length || this.signLineValues[line] == null) {
            return "";
        }
        return this.signLineValues[line];
    }

    protected String getNormalizedSignCommand() {
        return this.signCommandValue == null ? "" : this.signCommandValue.trim();
    }

    protected void cleanupBlockEntityTag(CompoundTag tag, CompoundTag blockEntity) {
        if (blockEntity.isEmpty()) {
            tag.remove(BLOCK_ENTITY_TAG);
        } else {
            tag.put(BLOCK_ENTITY_TAG, blockEntity);
        }
        cleanupEmptyTag();
    }

    protected static void removeLegacySignText(CompoundTag blockEntity) {
        for (int i = 0; i < SIGN_LINES; i++) {
            blockEntity.remove(LEGACY_SIGN_TEXT_TAG_PREFIX + (i + 1));
        }
    }
}

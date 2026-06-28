package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;
import io.github.seraphina.infinity_item_editor_re.util.NbtCompat;

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
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
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
abstract class ItemEditorScreenEffects extends ItemEditorScreenTrades {
    protected ItemEditorScreenEffects(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(stack, targetContainerSlot, parentTradeScreen, parentTradeIndex, parentTradeSlot);
    }

    protected void updateRawNbt() {
        String raw = this.rawNbtBox == null ? this.rawNbtValue : this.rawNbtBox.getValue();
        try {
            CompoundTag tag = parseNbt(raw);
            this.previewStack = tag == null ? this.previewStack.copy() : ItemStackNbt.parseEditorNbt(this.previewStack, tag);
            if (tag == null) {
                ItemStackNbt.set(this.previewStack, null);
            }
            readMainFieldsFromStack(this.previewStack);
            syncNbtEditorValuesFromStack();
            this.nbtFeedbackGood = true;
            this.nbtFeedback = "Looks good";
        } catch (CommandSyntaxException exception) {
            this.nbtFeedbackGood = false;
            this.nbtFeedback = exception.getMessage();
        }
    }

    protected void updateComponentNbt() {
        String raw = this.componentNbtBox == null ? this.componentNbtValue : this.componentNbtBox.getValue();
        try {
            CompoundTag components = parseNbt(raw);
            this.previewStack = parseStackWithComponents(this.previewStack, components == null ? new CompoundTag() : components);
            readMainFieldsFromStack(this.previewStack);
            syncNbtEditorValuesFromStack();
            this.nbtFeedbackGood = true;
            this.nbtFeedback = "Looks good";
        } catch (CommandSyntaxException exception) {
            this.nbtFeedbackGood = false;
            this.nbtFeedback = exception.getMessage();
        } catch (RuntimeException exception) {
            this.nbtFeedbackGood = false;
            this.nbtFeedback = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        }
    }

    protected ItemStack parseStackWithComponents(ItemStack current, CompoundTag components) {
        CompoundTag stackTag = new CompoundTag();
        Identifier id = CompatRegistries.ITEMS.getKey(current.getItem());
        stackTag.putString("id", id == null ? "minecraft:air" : id.toString());
        stackTag.putInt("count", Math.max(1, current.getCount()));
        if (components != null && !components.isEmpty()) {
            stackTag.put("components", components.copy());
        }
        ItemStack parsed = ItemStackNbt.parseStrict(stackTag);
        if (parsed.isEmpty() && !current.is(Items.AIR)) {
            throw new IllegalArgumentException("Invalid components");
        }
        return parsed;
    }

    protected void toggleUnbreakable() {
        CompoundTag tag = ItemStackNbt.getOrCreate(this.previewStack);
        boolean current = NbtCompat.getBoolean(tag, "Unbreakable");
        if (current) {
            tag.remove("Unbreakable");
        } else {
            tag.putBoolean("Unbreakable", true);
        }
        cleanupEmptyTag();
        syncNbtEditorValuesFromStack();
        rebuildWidgets();
    }

    protected Component getUnbreakableText() {
        CompoundTag tag = ItemStackNbt.get(this.previewStack);
        boolean unbreakable = tag != null && NbtCompat.getBoolean(tag, "Unbreakable");
        return Component.translatable(key("tag.unbreakable." + (unbreakable ? 1 : 0)));
    }

    protected void toggleHideFlag(HideFlag flag) {
        CompoundTag tag = ItemStackNbt.getOrCreate(this.previewStack);
        int value = NbtCompat.getInt(tag, HIDE_FLAGS_TAG);
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
        syncNbtEditorValuesFromStack();
    }

    protected Component getHideFlagText(HideFlag flag) {
        CompoundTag tag = ItemStackNbt.get(this.previewStack);
        boolean hidden = tag != null && (NbtCompat.getInt(tag, HIDE_FLAGS_TAG) & flag.mask()) != 0;
        return Component.translatable(key(flag.translationKey() + "." + (hidden ? 1 : 0)));
    }

    protected void toggleEnchantmentsScope() {
        this.showAllEnchantments = !this.showAllEnchantments;
        rebuildWidgets();
    }

    protected void renderActiveEnchantments(GuiGraphics guiGraphics) {
        List<EnchantmentEntry> activeEnchantments = getStoredEnchantments(this.previewStack);
        int startY = this.midY - activeEnchantments.size() * 5;
        for (int i = 0; i < activeEnchantments.size(); i++) {
            EnchantmentEntry entry = activeEnchantments.get(i);
            int color = entry.enchantment() == null ? BAD_RED : MAIN_COLOR;
            guiGraphics.drawString(this.font, formatStoredEnchantment(entry), editorListTextLeft(), startY + i * 10, color);
        }
    }

    protected boolean tryRemoveActiveEnchantment(double mouseX, double mouseY) {
        List<EnchantmentEntry> activeEnchantments = getStoredEnchantments(this.previewStack);
        if (activeEnchantments.isEmpty()) {
            return false;
        }

        int startY = this.midY - activeEnchantments.size() * 5;
        int listWidth = this.font.width("Unbreaking 32767");
        for (EnchantmentEntry entry : activeEnchantments) {
            listWidth = Math.max(listWidth, this.font.width(formatStoredEnchantment(entry)));
        }

        int listLeft = editorListTextLeft();
        if (mouseX < listLeft || mouseX > listLeft + listWidth || mouseY < startY || mouseY >= startY + activeEnchantments.size() * 10) {
            return false;
        }

        int index = (int) ((mouseY - startY) / 10);
        EnchantmentEntry entry = activeEnchantments.get(index);
        if (removeEnchantmentAtIndex(index)) {
            Component name = entry.enchantment() == null
                    ? Component.literal(String.valueOf(entry.id()))
                    : entry.enchantment().description();
            this.status = Component.translatable(messageKey("editor_enchantment_removed"), name);
        }
        return true;
    }

    protected boolean tryAddRingEnchantment(double mouseX, double mouseY) {
        List<Enchantment> filteredEnchantments = getVisibleEnchantments(this.previewStack);
        if (filteredEnchantments.isEmpty() || Math.abs(this.mouseDist - getRingRadius()) >= RING_HOVER_WIDTH) {
            return false;
        }

        double angle = (2.0D * Math.PI) / filteredEnchantments.size();
        int lowDist = Integer.MAX_VALUE;
        Enchantment closestEnchantment = null;
        for (int i = 0; i < filteredEnchantments.size(); i++) {
            double enchantmentAngle = this.rotOff / 60.0D + angle * i;
            int x = (int) (contentMidX() + getRingRadius() * Math.cos(enchantmentAngle));
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

    protected void addEnchantment(Enchantment enchantment) {
        int level = getLevelForEnchantment(enchantment);
        if (level < 1) {
            return;
        }
        putEnchantment(enchantment, level);
        this.status = Component.translatable(messageKey("editor_enchantment_added"),
                enchantment.description(), level);
    }

    protected void addMatchingEnchantments() {
        List<Enchantment> enchantments = getVisibleEnchantments(this.previewStack);
        if (!getFoldedEnchantmentGroups(this.previewStack).isEmpty()) {
            this.status = Component.translatable(messageKey("editor_select_enchantment_group"));
            return;
        }
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

    protected int getLevelForEnchantment(Enchantment enchantment) {
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

    protected int getDisplayLevel(Enchantment enchantment) {
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

    protected void putEnchantment(Enchantment enchantment, int level) {
        Identifier id = CompatRegistries.ENCHANTMENTS.getKey(enchantment);
        if (id == null) {
            return;
        }
        ListTag enchantments = getOrCreateEnchantmentsTag();
        int index = findEnchantmentIndex(enchantments, id);
        CompoundTag enchantmentTag = new CompoundTag();
        enchantmentTag.putString("id", id.toString());
        enchantmentTag.putShort("lvl", (short) level);
        if (index >= 0) {
            enchantments.set(index, enchantmentTag);
        } else {
            enchantments.add(enchantmentTag);
        }
        syncNbtEditorValuesFromStack();
    }

    protected boolean removeEnchantmentAtIndex(int index) {
        CompoundTag tag = ItemStackNbt.get(this.previewStack);
        if (tag == null) {
            return false;
        }

        String key = getEnchantmentTagKey(this.previewStack);
        ListTag enchantments = NbtCompat.getList(tag, key, Tag.TAG_COMPOUND);
        if (index < 0 || index >= enchantments.size()) {
            return false;
        }

        enchantments.remove(index);
        if (enchantments.isEmpty()) {
            tag.remove(key);
            cleanupEmptyTag();
        }
        syncNbtEditorValuesFromStack();
        return true;
    }

    protected ListTag getOrCreateEnchantmentsTag() {
        String key = getEnchantmentTagKey(this.previewStack);
        CompoundTag tag = ItemStackNbt.getOrCreate(this.previewStack);
        if (!NbtCompat.contains(tag, key, Tag.TAG_LIST)) {
            tag.put(key, new ListTag());
        }
        return NbtCompat.getList(tag, key, Tag.TAG_COMPOUND);
    }

    protected List<Enchantment> getFilteredEnchantments(ItemStack stack) {
        List<Enchantment> matchingEnchantments = new ArrayList<>();
        List<Enchantment> applicableMatchingEnchantments = new ArrayList<>();
        boolean hasApplicableEnchantments = false;
        String filter = this.enchantFilterValue == null ? "" : this.enchantFilterValue.trim().toLowerCase(Locale.ROOT);
        for (Enchantment enchantment : CompatRegistries.ENCHANTMENTS.getValues()) {
            boolean applicable = canApplyEnchantment(stack, enchantment);
            hasApplicableEnchantments |= applicable;
            Identifier id = CompatRegistries.ENCHANTMENTS.getKey(enchantment);
            String name = enchantment.description().getString().toLowerCase(Locale.ROOT);
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
        enchantments.sort(Comparator.comparing(enchantment -> enchantment.description().getString(),
                String.CASE_INSENSITIVE_ORDER));
        return enchantments;
    }

    protected List<Enchantment> getVisibleEnchantments(ItemStack stack) {
        List<Enchantment> enchantments = getFilteredEnchantments(stack);
        if (this.selectedEnchantmentNamespace.isBlank()) {
            return enchantments;
        }

        List<Enchantment> visibleEnchantments = new ArrayList<>();
        for (Enchantment enchantment : enchantments) {
            Identifier id = CompatRegistries.ENCHANTMENTS.getKey(enchantment);
            if (id != null && this.selectedEnchantmentNamespace.equals(id.getNamespace())) {
                visibleEnchantments.add(enchantment);
            }
        }

        if (visibleEnchantments.isEmpty()) {
            this.selectedEnchantmentNamespace = "";
            return enchantments;
        }
        return visibleEnchantments;
    }

    protected List<EnchantmentGroupEntry> getFoldedEnchantmentGroups(ItemStack stack) {
        if (!this.selectedEnchantmentNamespace.isBlank()) {
            return List.of();
        }

        List<Enchantment> enchantments = getFilteredEnchantments(stack);
        Map<String, List<Enchantment>> groupedEnchantments = new HashMap<>();
        for (Enchantment enchantment : enchantments) {
            Identifier id = CompatRegistries.ENCHANTMENTS.getKey(enchantment);
            if (id != null) {
                groupedEnchantments.computeIfAbsent(id.getNamespace(), namespace -> new ArrayList<>()).add(enchantment);
            }
        }

        if (!shouldFoldRegistryEntries(enchantments.size(), groupedEnchantments)) {
            return List.of();
        }

        List<EnchantmentGroupEntry> groups = new ArrayList<>();
        for (Map.Entry<String, List<Enchantment>> entry : groupedEnchantments.entrySet()) {
            groups.add(new EnchantmentGroupEntry(entry.getKey(), entry.getValue()));
        }
        groups.sort(Comparator.comparing(EnchantmentGroupEntry::namespace, String.CASE_INSENSITIVE_ORDER));
        return groups;
    }

    protected boolean trySelectRingEnchantmentGroup(double mouseX, double mouseY) {
        List<EnchantmentGroupEntry> groups = getFoldedEnchantmentGroups(this.previewStack);
        if (groups.isEmpty() || Math.abs(this.mouseDist - getRingRadius()) >= RING_HOVER_WIDTH) {
            return false;
        }

        double angle = (2.0D * Math.PI) / groups.size();
        int lowDist = Integer.MAX_VALUE;
        EnchantmentGroupEntry closestGroup = null;
        for (int i = 0; i < groups.size(); i++) {
            double groupAngle = this.rotOff / 60.0D + angle * i;
            int x = (int) (contentMidX() + getRingRadius() * Math.cos(groupAngle));
            int y = (int) (this.midY + getRingRadius() * Math.sin(groupAngle));
            int distX = x - (int) mouseX;
            int distY = y - (int) mouseY;
            int dist = (int) Math.sqrt(distX * distX + distY * distY);
            if (dist < RING_ICON_HIT_RADIUS && dist < lowDist) {
                lowDist = dist;
                closestGroup = groups.get(i);
            }
        }

        if (closestGroup == null) {
            return false;
        }

        this.selectedEnchantmentNamespace = closestGroup.namespace();
        this.status = Component.translatable(messageKey("editor_enchantment_group_selected"), closestGroup.namespace());
        rebuildWidgets();
        return true;
    }

    protected boolean shouldFoldRegistryEntries(int entryCount, Map<String, ?> groupedEntries) {
        if (entryCount <= FOLDED_REGISTRY_ENTRY_LIMIT || groupedEntries.isEmpty()) {
            return false;
        }
        return groupedEntries.size() > 1 || !groupedEntries.containsKey("minecraft");
    }

    protected boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
        return enchantment.canEnchant(stack)
                || stack.is(Items.BOOK)
                || stack.is(Items.ENCHANTED_BOOK);
    }

    protected List<EnchantmentEntry> getStoredEnchantments(ItemStack stack) {
        List<EnchantmentEntry> entries = new ArrayList<>();
        CompoundTag tag = ItemStackNbt.get(stack);
        if (tag == null) {
            return entries;
        }

        ListTag enchantments = NbtCompat.getList(tag, getEnchantmentTagKey(stack), Tag.TAG_COMPOUND);
        for (int i = 0; i < enchantments.size(); i++) {
            CompoundTag enchantmentTag = NbtCompat.getCompound(enchantments, i);
            Identifier id = Identifier.tryParse(NbtCompat.getString(enchantmentTag, "id"));
            Enchantment enchantment = id == null ? null : CompatRegistries.ENCHANTMENTS.getValue(id);
            entries.add(new EnchantmentEntry(id, enchantment, NbtCompat.getInt(enchantmentTag, "lvl")));
        }
        return entries;
    }

    protected boolean tryRemoveActivePotionEffect(double mouseX, double mouseY) {
        List<MobEffectInstance> effects = getCustomPotionEffects();
        if (effects.isEmpty()) {
            return false;
        }

        int startY = this.midY - effects.size() * 5;
        int listWidth = this.font.width("Unbreaking 32767");
        for (MobEffectInstance effect : effects) {
            listWidth = Math.max(listWidth, this.font.width(formatPotionEffect(effect)));
        }

        int listLeft = editorListTextLeft();
        if (mouseX < listLeft || mouseX > listLeft + listWidth || mouseY < startY || mouseY >= startY + effects.size() * 10) {
            return false;
        }

        int index = (int) ((mouseY - startY) / 10);
        removeCustomPotionEffectAt(index);
        return true;
    }

    protected boolean tryAddRingPotionEffect(double mouseX, double mouseY) {
        List<MobEffect> filteredEffects = getFilteredPotionEffects();
        if (filteredEffects.isEmpty() || Math.abs(this.mouseDist - getRingRadius()) >= RING_HOVER_WIDTH) {
            return false;
        }

        double angle = (2.0D * Math.PI) / filteredEffects.size();
        int lowDist = Integer.MAX_VALUE;
        MobEffect closestEffect = null;
        for (int i = 0; i < filteredEffects.size(); i++) {
            double effectAngle = this.rotOff / 60.0D + angle * i;
            int x = (int) (contentMidX() + getRingRadius() * Math.cos(effectAngle));
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

    protected void addMatchingPotionEffects() {
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

    protected boolean addPotionEffect(MobEffect effect) {
        int level = parsePotionLevel();
        int seconds = parsePotionSeconds();
        if (level < 1 || seconds < 1) {
            return false;
        }

        Holder<MobEffect> holder = CompatRegistries.MOB_EFFECTS.getHolder(effect);
        if (holder == null) {
            return false;
        }

        MobEffectInstance instance = new MobEffectInstance(holder, seconds * 20, level - 1, false, this.showPotionParticles);
        List<MobEffectInstance> effects = new ArrayList<>(getCustomPotionEffects());
        effects.removeIf(existing -> existing.getEffect().is(holder));
        effects.add(instance);
        PotionCompat.setCustomEffects(this.previewStack, effects);
        syncNbtEditorValuesFromStack();
        this.status = Component.translatable(messageKey("editor_potion_added"), effect.getDisplayName(), level);
        return true;
    }

    protected void removeCustomPotionEffectAt(int index) {
        List<MobEffectInstance> effects = new ArrayList<>(getCustomPotionEffects());
        if (index < 0 || index >= effects.size()) {
            return;
        }
        MobEffectInstance removed = effects.remove(index);
        if (effects.isEmpty()) {
            CompoundTag tag = ItemStackNbt.get(this.previewStack);
            if (tag != null) {
                tag.remove(CUSTOM_POTION_EFFECTS_TAG);
            }
            cleanupEmptyTag();
        } else {
            PotionCompat.setCustomEffects(this.previewStack, effects);
        }
        syncNbtEditorValuesFromStack();
        this.status = Component.translatable(messageKey("editor_potion_removed"), removed.getEffect().value().getDisplayName());
    }

    protected List<MobEffectInstance> getCustomPotionEffects() {
        return new ArrayList<>(PotionCompat.getCustomEffects(this.previewStack));
    }

    protected List<MobEffect> getFilteredPotionEffects() {
        List<MobEffect> effects = new ArrayList<>();
        String filter = this.potionFilterValue == null ? "" : this.potionFilterValue.trim().toLowerCase(Locale.ROOT);
        for (MobEffect effect : CompatRegistries.MOB_EFFECTS.getValues()) {
            Identifier id = CompatRegistries.MOB_EFFECTS.getKey(effect);
            String name = effect.getDisplayName().getString().toLowerCase(Locale.ROOT);
            String idString = id == null ? "" : id.toString().toLowerCase(Locale.ROOT);
            if (filter.isEmpty() || name.contains(filter) || idString.contains(filter)) {
                effects.add(effect);
            }
        }
        effects.sort(Comparator.comparing(effect -> effect.getDisplayName().getString(), String.CASE_INSENSITIVE_ORDER));
        return effects;
    }

    protected int parsePotionLevel() {
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

    protected int parsePotionSeconds() {
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

    protected void togglePotionParticles() {
        this.showPotionParticles = !this.showPotionParticles;
        rebuildWidgets();
    }

    protected String formatPotionEffect(MobEffectInstance effect) {
        int amplifier = effect.getAmplifier();
        String text = effect.getEffect().value().getDisplayName().getString() + " (" + (amplifier + 1) + ")";
        if (amplifier > 1) {
            text += " " + Component.translatable("potion.potency." + amplifier).getString().trim();
        }
        text += effect.isVisible() ? " P:S" : " P:H";
        return text;
    }

    protected String formatPotionRingName(MobEffect effect) {
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

    protected void toggleAttributeInfinity() {
        this.attributeInfinity = !this.attributeInfinity;
        rebuildWidgets();
    }

    protected void cycleAttributeOperation() {
        this.attributeOperation = (this.attributeOperation + 1) % 3;
        rebuildWidgets();
    }

    protected void cycleAttributeSlot() {
        this.attributeSlot = (this.attributeSlot + 1) % 7;
        rebuildWidgets();
    }

    protected boolean tryRemoveActiveAttributeModifier(double mouseX, double mouseY) {
        List<AttributeEntry> entries = getAttributeModifierEntries();
        if (entries.isEmpty()) {
            return false;
        }

        int startY = this.midY - entries.size() * 5;
        int listWidth = this.font.width("Unbreaking 32767");
        for (AttributeEntry entry : entries) {
            listWidth = Math.max(listWidth, this.font.width(formatAttributeEntry(entry)));
        }

        int listLeft = editorListTextLeft();
        if (mouseX < listLeft || mouseX > listLeft + listWidth || mouseY < startY || mouseY >= startY + entries.size() * 10) {
            return false;
        }

        int index = (int) ((mouseY - startY) / 10);
        AttributeEntry entry = entries.get(index);
        removeAttributeModifierAt(entry.tagIndex());
        this.status = Component.translatable(messageKey("editor_attribute_removed"), getAttributeDisplayName(entry));
        return true;
    }

    protected boolean tryAddRingAttribute(double mouseX, double mouseY) {
        List<Attribute> attributes = getVisibleAttributes();
        if (attributes.isEmpty() || Math.abs(this.mouseDist - getRingRadius()) >= RING_HOVER_WIDTH) {
            return false;
        }

        double angle = (2.0D * Math.PI) / attributes.size();
        int lowDist = Integer.MAX_VALUE;
        Attribute closestAttribute = null;
        for (int i = 0; i < attributes.size(); i++) {
            double attributeAngle = this.rotOff / 60.0D + angle * i;
            int x = (int) (contentMidX() + getRingRadius() * Math.cos(attributeAngle));
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

    protected void addAttributeModifier(Attribute attribute) {
        Double amount = getAttributeAmount();
        if (amount == null) {
            return;
        }

        Identifier id = CompatRegistries.ATTRIBUTES.getKey(attribute);
        if (id == null) {
            return;
        }

        AttributeModifier.Operation operation = getAttributeOperation();
        UUID uuid = UUID.randomUUID();
        AttributeModifier modifier = new AttributeModifier(id.withSuffix("/" + uuid), amount, operation);
        CompoundTag modifierTag = new CompoundTag();
        modifierTag.putString("id", modifier.id().toString());
        modifierTag.putString("Name", id.toString());
        NbtCompat.putUUID(modifierTag, "UUID", uuid);
        modifierTag.putString("AttributeName", id.toString());
        modifierTag.putDouble("Amount", amount);
        modifierTag.putInt("Operation", operation.id());
        String slotName = getAttributeSlotName(this.attributeSlot);
        if (slotName != null) {
            modifierTag.putString("Slot", slotName);
        }

        getOrCreateAttributeModifiersTag().add(modifierTag);
        syncNbtEditorValuesFromStack();
        this.status = Component.translatable(messageKey("editor_attribute_added"), Component.translatable(attribute.getDescriptionId()));
    }

    protected Double getAttributeAmount() {
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

    protected int parseBoundedAttributeNumber(String value, int max) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        int parsed = Integer.parseInt(value);
        if (parsed < 0 || parsed > max) {
            throw new NumberFormatException();
        }
        return parsed;
    }

    protected AttributeModifier.Operation getAttributeOperation() {
        if (this.attributeInfinity) {
            return AttributeModifier.Operation.ADD_VALUE;
        }
        return switch (this.attributeOperation) {
            case 1 -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case 2 -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default -> AttributeModifier.Operation.ADD_VALUE;
        };
    }

    protected ListTag getOrCreateAttributeModifiersTag() {
        CompoundTag tag = ItemStackNbt.getOrCreate(this.previewStack);
        if (!NbtCompat.contains(tag, ATTRIBUTE_MODIFIERS_TAG, Tag.TAG_LIST)) {
            tag.put(ATTRIBUTE_MODIFIERS_TAG, new ListTag());
        }
        return NbtCompat.getList(tag, ATTRIBUTE_MODIFIERS_TAG, Tag.TAG_COMPOUND);
    }

    protected List<AttributeEntry> getAttributeModifierEntries() {
        List<AttributeEntry> entries = new ArrayList<>();
        CompoundTag tag = ItemStackNbt.get(this.previewStack);
        if (tag == null || !NbtCompat.contains(tag, ATTRIBUTE_MODIFIERS_TAG, Tag.TAG_LIST)) {
            return entries;
        }

        ListTag modifiers = NbtCompat.getList(tag, ATTRIBUTE_MODIFIERS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < modifiers.size(); i++) {
            CompoundTag modifierTag = NbtCompat.getCompound(modifiers, i);
            String attributeName = NbtCompat.getString(modifierTag, "AttributeName");
            Attribute attribute = getAttributeByName(attributeName);
            double amount = NbtCompat.contains(modifierTag, "Amount", Tag.TAG_DOUBLE) ? NbtCompat.getDouble(modifierTag, "Amount") : 0.0D;
            int operation = Mth.positiveModulo(NbtCompat.getInt(modifierTag, "Operation"), 3);
            String slotName = NbtCompat.contains(modifierTag, "Slot", Tag.TAG_STRING) ? NbtCompat.getString(modifierTag, "Slot") : "any";
            entries.add(new AttributeEntry(i, attributeName, attribute, amount, operation, slotName));
        }
        return entries;
    }

    protected void removeAttributeModifierAt(int tagIndex) {
        CompoundTag tag = ItemStackNbt.get(this.previewStack);
        if (tag == null || !NbtCompat.contains(tag, ATTRIBUTE_MODIFIERS_TAG, Tag.TAG_LIST)) {
            return;
        }

        ListTag modifiers = NbtCompat.getList(tag, ATTRIBUTE_MODIFIERS_TAG, Tag.TAG_COMPOUND);
        if (tagIndex < 0 || tagIndex >= modifiers.size()) {
            return;
        }

        modifiers.remove(tagIndex);
        if (modifiers.isEmpty()) {
            tag.remove(ATTRIBUTE_MODIFIERS_TAG);
            cleanupEmptyTag();
        }
        syncNbtEditorValuesFromStack();
    }

    protected String formatAttributeEntry(AttributeEntry entry) {
        String[] prefixes = {entry.amount() < 0.0D ? "" : "+", "*", "**"};
        return getAttributeDisplayName(entry).getString() + " " + prefixes[entry.operation()] + entry.amount() + " (" + entry.slotName() + ")";
    }

    protected Component getAttributeDisplayName(AttributeEntry entry) {
        if (entry.attribute() != null) {
            return Component.translatable(entry.attribute().getDescriptionId());
        }
        if (!entry.attributeName().isBlank()) {
            return Component.literal(entry.attributeName());
        }
        return Component.literal("Unknown Attribute");
    }

    protected Attribute getAttributeByName(String name) {
        Identifier id = Identifier.tryParse(name);
        return id == null ? null : CompatRegistries.ATTRIBUTES.getValue(id);
    }

    protected List<Attribute> getSharedAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        String filter = this.attributeFilterValue == null ? "" : this.attributeFilterValue.trim().toLowerCase(Locale.ROOT);
        for (Attribute attribute : CompatRegistries.ATTRIBUTES.getValues()) {
            if (attribute != null && matchesAttributeFilter(attribute, filter)) {
                attributes.add(attribute);
            }
        }
        attributes.sort(Comparator.comparing(attribute -> Component.translatable(attribute.getDescriptionId()).getString(),
                String.CASE_INSENSITIVE_ORDER));
        return attributes;
    }

    protected List<Attribute> getVisibleAttributes() {
        List<Attribute> attributes = getSharedAttributes();
        if (this.selectedAttributeNamespace.isBlank()) {
            return attributes;
        }

        List<Attribute> visibleAttributes = new ArrayList<>();
        for (Attribute attribute : attributes) {
            Identifier id = CompatRegistries.ATTRIBUTES.getKey(attribute);
            if (id != null && this.selectedAttributeNamespace.equals(id.getNamespace())) {
                visibleAttributes.add(attribute);
            }
        }

        if (visibleAttributes.isEmpty()) {
            this.selectedAttributeNamespace = "";
            return attributes;
        }
        return visibleAttributes;
    }

    protected List<AttributeGroupEntry> getFoldedAttributeGroups() {
        if (!this.selectedAttributeNamespace.isBlank()) {
            return List.of();
        }

        List<Attribute> attributes = getSharedAttributes();
        Map<String, List<Attribute>> groupedAttributes = new HashMap<>();
        for (Attribute attribute : attributes) {
            Identifier id = CompatRegistries.ATTRIBUTES.getKey(attribute);
            if (id != null) {
                groupedAttributes.computeIfAbsent(id.getNamespace(), namespace -> new ArrayList<>()).add(attribute);
            }
        }

        if (!shouldFoldRegistryEntries(attributes.size(), groupedAttributes)) {
            return List.of();
        }

        List<AttributeGroupEntry> groups = new ArrayList<>();
        for (Map.Entry<String, List<Attribute>> entry : groupedAttributes.entrySet()) {
            groups.add(new AttributeGroupEntry(entry.getKey(), entry.getValue()));
        }
        groups.sort(Comparator.comparing(AttributeGroupEntry::namespace, String.CASE_INSENSITIVE_ORDER));
        return groups;
    }

    protected boolean trySelectRingAttributeGroup(double mouseX, double mouseY) {
        List<AttributeGroupEntry> groups = getFoldedAttributeGroups();
        if (groups.isEmpty() || Math.abs(this.mouseDist - getRingRadius()) >= RING_HOVER_WIDTH) {
            return false;
        }

        double angle = (2.0D * Math.PI) / groups.size();
        int lowDist = Integer.MAX_VALUE;
        AttributeGroupEntry closestGroup = null;
        for (int i = 0; i < groups.size(); i++) {
            double groupAngle = this.rotOff / 60.0D + angle * i;
            int x = (int) (contentMidX() + getRingRadius() * Math.cos(groupAngle));
            int y = (int) (this.midY + getRingRadius() * Math.sin(groupAngle));
            int distX = x - (int) mouseX;
            int distY = y - (int) mouseY;
            int dist = (int) Math.sqrt(distX * distX + distY * distY);
            if (dist < RING_ICON_HIT_RADIUS && dist < lowDist) {
                lowDist = dist;
                closestGroup = groups.get(i);
            }
        }

        if (closestGroup == null) {
            return false;
        }

        this.selectedAttributeNamespace = closestGroup.namespace();
        this.status = Component.translatable(messageKey("editor_attribute_group_selected"), closestGroup.namespace());
        rebuildWidgets();
        return true;
    }

    protected boolean matchesAttributeFilter(Attribute attribute, String filter) {
        if (filter.isBlank()) {
            return true;
        }

        Identifier id = CompatRegistries.ATTRIBUTES.getKey(attribute);
        String idString = id == null ? "" : id.toString().toLowerCase(Locale.ROOT);
        String descriptionId = attribute.getDescriptionId().toLowerCase(Locale.ROOT);
        String name = Component.translatable(attribute.getDescriptionId()).getString().toLowerCase(Locale.ROOT);
        return idString.contains(filter)
                || descriptionId.contains(filter)
                || name.contains(filter);
    }
}

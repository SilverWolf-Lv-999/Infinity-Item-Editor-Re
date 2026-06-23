package io.github.seraphina.infinity_item_editor_re.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ItemStackNbt {
    private static final Set<String> FULL_STACK_KEYS = Set.of("id", "item", "Item", "count", "Count", "components", "tag", "nbt");
    private static final Set<String> LEGACY_COMPONENT_KEYS = Set.of(
            "minecraft:custom_data",
            "minecraft:damage",
            "minecraft:repair_cost",
            "minecraft:custom_model_data",
            "minecraft:block_state",
            "minecraft:entity_data",
            "minecraft:block_entity_data",
            "minecraft:unbreakable",
            "minecraft:enchantments",
            "minecraft:stored_enchantments",
            "minecraft:enchantment_glint_override",
            "minecraft:custom_name",
            "minecraft:item_name",
            "minecraft:lore",
            "minecraft:dyed_color",
            "minecraft:map_color",
            "minecraft:can_break",
            "minecraft:can_place_on",
            "minecraft:attribute_modifiers",
            "minecraft:trim",
            "minecraft:tooltip_display",
            "minecraft:hide_additional_tooltip",
            "minecraft:charged_projectiles",
            "minecraft:bundle_contents",
            "minecraft:map_id",
            "minecraft:map_decorations",
            "minecraft:potion_contents",
            "minecraft:writable_book_content",
            "minecraft:written_book_content",
            "minecraft:suspicious_stew_effects",
            "minecraft:debug_stick_state",
            "minecraft:bucket_entity_data",
            "minecraft:instrument",
            "minecraft:recipes",
            "minecraft:lodestone_tracker",
            "minecraft:fireworks",
            "minecraft:firework_explosion",
            "minecraft:profile",
            "minecraft:note_block_sound",
            "minecraft:banner_patterns",
            "minecraft:base_color",
            "minecraft:container",
            "minecraft:bees",
            "minecraft:lock",
            "minecraft:container_loot",
            "minecraft:pot_decorations"
    );
    private static final Map<String, String> BANNER_HASH_TO_PATTERN = Map.ofEntries(
            Map.entry("bl", "square_bottom_left"),
            Map.entry("br", "square_bottom_right"),
            Map.entry("tl", "square_top_left"),
            Map.entry("tr", "square_top_right"),
            Map.entry("bs", "stripe_bottom"),
            Map.entry("ts", "stripe_top"),
            Map.entry("ls", "stripe_left"),
            Map.entry("rs", "stripe_right"),
            Map.entry("cs", "stripe_center"),
            Map.entry("ms", "stripe_middle"),
            Map.entry("drs", "stripe_downright"),
            Map.entry("dls", "stripe_downleft"),
            Map.entry("ss", "small_stripes"),
            Map.entry("cr", "cross"),
            Map.entry("sc", "straight_cross"),
            Map.entry("bt", "triangle_bottom"),
            Map.entry("tt", "triangle_top"),
            Map.entry("bts", "triangles_bottom"),
            Map.entry("tts", "triangles_top"),
            Map.entry("ld", "diagonal_left"),
            Map.entry("rd", "diagonal_up_right"),
            Map.entry("lud", "diagonal_up_left"),
            Map.entry("rud", "diagonal_right"),
            Map.entry("mc", "circle"),
            Map.entry("mr", "rhombus"),
            Map.entry("vh", "half_vertical"),
            Map.entry("hh", "half_horizontal"),
            Map.entry("vhr", "half_vertical_right"),
            Map.entry("hhb", "half_horizontal_bottom"),
            Map.entry("bo", "border"),
            Map.entry("cbo", "curly_border"),
            Map.entry("gra", "gradient"),
            Map.entry("gru", "gradient_up"),
            Map.entry("bri", "bricks"),
            Map.entry("glb", "globe"),
            Map.entry("cre", "creeper"),
            Map.entry("sku", "skull"),
            Map.entry("flo", "flower"),
            Map.entry("moj", "mojang"),
            Map.entry("pig", "piglin")
    );
    private static final Map<String, String> BANNER_PATTERN_TO_HASH = invert(BANNER_HASH_TO_PATTERN);
    private static final Map<String, String> BLOCK_ENTITY_BY_ITEM = Map.ofEntries(
            Map.entry("barrel", "barrel"),
            Map.entry("beacon", "beacon"),
            Map.entry("beehive", "beehive"),
            Map.entry("bee_nest", "beehive"),
            Map.entry("blast_furnace", "blast_furnace"),
            Map.entry("brewing_stand", "brewing_stand"),
            Map.entry("campfire", "campfire"),
            Map.entry("soul_campfire", "campfire"),
            Map.entry("chest", "chest"),
            Map.entry("trapped_chest", "chest"),
            Map.entry("command_block", "command_block"),
            Map.entry("chain_command_block", "command_block"),
            Map.entry("repeating_command_block", "command_block"),
            Map.entry("comparator", "comparator"),
            Map.entry("conduit", "conduit"),
            Map.entry("crafter", "crafter"),
            Map.entry("decorated_pot", "decorated_pot"),
            Map.entry("dispenser", "dispenser"),
            Map.entry("dropper", "dropper"),
            Map.entry("furnace", "furnace"),
            Map.entry("hopper", "hopper"),
            Map.entry("jigsaw", "jigsaw"),
            Map.entry("jukebox", "jukebox"),
            Map.entry("lectern", "lectern"),
            Map.entry("smoker", "smoker"),
            Map.entry("spawner", "mob_spawner"),
            Map.entry("structure_block", "structure_block"),
            Map.entry("suspicious_gravel", "brushable_block"),
            Map.entry("suspicious_sand", "brushable_block"),
            Map.entry("trial_spawner", "trial_spawner"),
            Map.entry("vault", "vault")
    );

    private ItemStackNbt() {
    }

    public static CompoundTag get(ItemStack stack) {
        CompoundTag tag = buildLegacyTag(stack);
        return tag == null || tag.isEmpty() ? null : syncedRoot(stack, tag);
    }

    public static CompoundTag getOrCreate(ItemStack stack) {
        CompoundTag tag = buildLegacyTag(stack);
        return syncedRoot(stack, tag == null ? new CompoundTag() : tag);
    }

    public static CompoundTag getElement(ItemStack stack, String key) {
        CompoundTag tag = get(stack);
        if (tag == null || !NbtCompat.contains(tag, key, Tag.TAG_COMPOUND)) {
            return null;
        }
        return NbtCompat.getCompound(tag, key);
    }

    public static CompoundTag getOrCreateElement(ItemStack stack, String key) {
        CompoundTag tag = getOrCreate(stack);
        if (!NbtCompat.contains(tag, key, Tag.TAG_COMPOUND)) {
            tag.put(key, new CompoundTag());
        }
        return NbtCompat.getCompound(tag, key);
    }

    public static void set(ItemStack stack, CompoundTag tag) {
        applyLegacyTag(stack, tag == null ? new CompoundTag() : tag);
    }

    public static CompoundTag save(ItemStack stack) {
        return save(stack, provider());
    }

    public static CompoundTag save(ItemStack stack, HolderLookup.Provider provider) {
        Tag saved = ItemStack.OPTIONAL_CODEC
                .encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack)
                .getOrThrow();
        return saved instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
    }

    public static ItemStack parse(CompoundTag tag) {
        return parse(tag, provider());
    }

    public static ItemStack parse(CompoundTag tag, HolderLookup.Provider provider) {
        ItemStack parsed = parseStackTag(normalizeStackTag(tag), provider);
        if (!parsed.isEmpty() || !isLegacyStackTag(tag)) {
            return parsed;
        }

        ResourceLocation id = ResourceLocation.tryParse(NbtCompat.getString(tag, "id"));
        Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.getValue(id);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        int count = tag.contains("Count") ? NbtCompat.getByte(tag, "Count") : 1;
        ItemStack stack = new ItemStack(item, Math.max(1, count));
        if (NbtCompat.contains(tag, "tag", Tag.TAG_COMPOUND)) {
            set(stack, NbtCompat.getCompound(tag, "tag").copy());
        }
        return stack;
    }

    public static ItemStack parseStrict(CompoundTag tag) {
        return parseStrict(tag, provider());
    }

    public static ItemStack parseStrict(CompoundTag tag, HolderLookup.Provider provider) {
        return ItemStack.OPTIONAL_CODEC
                .parse(provider.createSerializationContext(NbtOps.INSTANCE), normalizeStackTag(tag))
                .getOrThrow();
    }

    public static ItemStack rebind(ItemStack stack, HolderLookup.Provider targetProvider) {
        ItemStack rebound = parse(save(stack), targetProvider);
        return rebound.isEmpty() && !stack.isEmpty() ? stack.copy() : rebound;
    }

    public static ItemStack parseEditorNbt(ItemStack current, CompoundTag tag) {
        if (isFullStackTag(tag)) {
            ItemStack parsed = parse(tag);
            if (!parsed.isEmpty() || "minecraft:air".equals(NbtCompat.getString(tag, "id"))) {
                return parsed;
            }
        }

        ItemStack edited = current.copy();
        set(edited, tag);
        return edited;
    }

    public static HolderLookup.Provider provider() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                return minecraft.level.registryAccess();
            }
        } catch (RuntimeException | LinkageError ignored) {
        }

        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    public static RegistryAccess registryAccess() {
        HolderLookup.Provider provider = provider();
        return provider instanceof RegistryAccess registryAccess ? registryAccess : RegistryAccess.EMPTY;
    }

    private static CompoundTag syncedRoot(ItemStack stack, CompoundTag tag) {
        final CompoundTag[] holder = new CompoundTag[1];
        holder[0] = syncedCompound(tag, () -> applyLegacyTag(stack, holder[0]));
        return holder[0];
    }

    private static CompoundTag buildLegacyTag(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        CompoundTag saved = save(stack);
        CompoundTag components = NbtCompat.getCompound(saved, "components");
        CompoundTag tag = new CompoundTag();
        int hideFlags = 0;

        if (NbtCompat.contains(components, "minecraft:custom_data", Tag.TAG_COMPOUND)) {
            tag.merge(NbtCompat.getCompound(components, "minecraft:custom_data").copy());
        }

        copyComponent(components, "minecraft:damage", tag, "Damage");
        copyComponent(components, "minecraft:repair_cost", tag, "RepairCost");
        copyComponent(components, "minecraft:custom_model_data", tag, "CustomModelData");
        copyComponent(components, "minecraft:block_state", tag, "BlockStateTag");
        copyComponent(components, "minecraft:entity_data", tag, "EntityTag");

        hideFlags = readComponentDisplay(components, tag, hideFlags);
        hideFlags = readComponentEnchantments(components, tag, hideFlags);
        hideFlags = readComponentUnbreakable(components, tag, hideFlags);
        hideFlags = readComponentAttributes(components, tag, hideFlags);
        hideFlags = readComponentAdventure(components, tag, hideFlags);
        readComponentPotion(components, tag);
        readComponentBooks(components, tag);
        readComponentFireworks(components, tag);
        readComponentBundleContents(components, tag);
        readComponentProfile(components, tag);
        readComponentBlockEntity(stack, components, tag);

        if (NbtCompat.contains(components, "minecraft:trim", Tag.TAG_COMPOUND)
                && !NbtCompat.getBoolean(NbtCompat.getCompound(components, "minecraft:trim"), "show_in_tooltip")) {
            hideFlags |= 128;
        }
        if (components.contains("minecraft:hide_additional_tooltip")) {
            hideFlags |= 32;
        }
        if (hideFlags != 0) {
            tag.putInt("HideFlags", hideFlags);
        }

        return tag;
    }

    private static void applyLegacyTag(ItemStack stack, CompoundTag tag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        CompoundTag stackTag = stackTagFromLegacy(stack, tag == null ? new CompoundTag() : tag);
        ItemStack parsed = parseStackTag(stackTag);
        if (parsed.isEmpty() && !stack.is(Items.AIR)) {
            ItemStack fallback = new ItemStack(stack.getItem(), stack.getCount());
            if (tag != null && !tag.isEmpty()) {
                fallback.set(DataComponents.CUSTOM_DATA, CustomData.of(tag.copy()));
            }
            applyLegacyComponentsFrom(stack, fallback);
            return;
        }

        applyLegacyComponentsFrom(stack, parsed);
    }

    private static void applyLegacyComponentsFrom(ItemStack stack, ItemStack source) {
        for (String key : LEGACY_COMPONENT_KEYS) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            DataComponentType<?> component = id == null ? null : BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(id);
            if (component != null) {
                copyDataComponent(source, stack, component);
            }
        }
    }

    private static <T> void copyDataComponent(ItemStack source, ItemStack target, DataComponentType<T> component) {
        T value = source.get(component);
        if (value == null) {
            target.remove(component);
        } else {
            target.set(component, value);
        }
    }

    private static ItemStack parseStackTag(CompoundTag tag) {
        return parseStackTag(tag, provider());
    }

    private static ItemStack parseStackTag(CompoundTag tag, HolderLookup.Provider provider) {
        return ItemStack.OPTIONAL_CODEC
                .parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
                .result()
                .orElse(ItemStack.EMPTY);
    }

    private static CompoundTag stackTagFromLegacy(ItemStack stack, CompoundTag legacyTag) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        CompoundTag components = NbtCompat.getCompound(save(stack), "components").copy();
        for (String key : LEGACY_COMPONENT_KEYS) {
            components.remove(key);
        }
        return stackTagFromLegacy(itemId == null ? "minecraft:air" : itemId.toString(), Math.max(1, stack.getCount()), legacyTag, components);
    }

    private static CompoundTag stackTagFromLegacy(String itemId, int count, CompoundTag legacyTag) {
        return stackTagFromLegacy(itemId, count, legacyTag, new CompoundTag());
    }

    private static CompoundTag stackTagFromLegacy(String itemId, int count, CompoundTag legacyTag, CompoundTag baseComponents) {
        CompoundTag stackTag = new CompoundTag();
        stackTag.putString("id", itemId);
        stackTag.putInt("count", Math.max(1, count));

        CompoundTag customData = legacyTag.copy();
        CompoundTag components = baseComponents == null ? new CompoundTag() : baseComponents.copy();
        int hideFlags = removeInt(customData, "HideFlags");

        moveComponent(customData, "Damage", components, "minecraft:damage");
        moveComponent(customData, "RepairCost", components, "minecraft:repair_cost");
        moveComponent(customData, "CustomModelData", components, "minecraft:custom_model_data");
        moveComponent(customData, "BlockStateTag", components, "minecraft:block_state");
        writeEntityDataComponent(itemId, customData, components);

        writeDisplayComponents(customData, components, hideFlags);
        writeEnchantmentsComponent(customData, components, "Enchantments", "minecraft:enchantments", (hideFlags & 1) != 0);
        writeEnchantmentsComponent(customData, components, "StoredEnchantments", "minecraft:stored_enchantments", (hideFlags & 32) != 0);
        writeUnbreakableComponent(customData, components, hideFlags);
        writeAttributeComponents(customData, components, hideFlags);
        writeAdventureComponents(customData, components, hideFlags);
        writePotionComponents(customData, components);
        writeBookComponents(customData, components);
        writeFireworkComponents(customData, components);
        writeBundleContentsComponent(itemId, customData, components);
        writeProfileComponent(customData, components);
        writeBlockEntityComponents(itemId, customData, components);

        if ((hideFlags & 32) != 0) {
            components.put("minecraft:hide_additional_tooltip", new CompoundTag());
        }
        if (!customData.isEmpty()) {
            components.put("minecraft:custom_data", customData);
        }
        if (!components.isEmpty()) {
            stackTag.put("components", components);
        }
        return stackTag;
    }

    private static CompoundTag normalizeStackTag(CompoundTag tag) {
        if (!isFullStackTag(tag)) {
            return tag;
        }

        CompoundTag normalized = tag.copy();
        if (!NbtCompat.contains(normalized, "id", Tag.TAG_STRING)) {
            if (NbtCompat.contains(normalized, "item", Tag.TAG_STRING)) {
                normalized.putString("id", NbtCompat.getString(normalized, "item"));
            } else if (NbtCompat.contains(normalized, "Item", Tag.TAG_STRING)) {
                normalized.putString("id", NbtCompat.getString(normalized, "Item"));
            }
        }
        if (!normalized.contains("count") && normalized.contains("Count")) {
            normalized.putInt("count", NbtCompat.getByte(normalized, "Count") & 255);
        }
        if (!normalized.contains("count")) {
            normalized.putInt("count", 1);
        }
        if (!NbtCompat.contains(normalized, "components", Tag.TAG_COMPOUND)) {
            CompoundTag legacy = NbtCompat.contains(normalized, "tag", Tag.TAG_COMPOUND)
                    ? NbtCompat.getCompound(normalized, "tag")
                    : NbtCompat.contains(normalized, "nbt", Tag.TAG_COMPOUND) ? NbtCompat.getCompound(normalized, "nbt") : new CompoundTag();
            normalized = stackTagFromLegacy(NbtCompat.getString(normalized, "id"), NbtCompat.getInt(normalized, "count"), legacy);
        }
        return normalized;
    }

    private static boolean isFullStackTag(CompoundTag tag) {
        for (String key : tag.keySet()) {
            if (FULL_STACK_KEYS.contains(key)) {
                return NbtCompat.contains(tag, "id", Tag.TAG_STRING) || NbtCompat.contains(tag, "item", Tag.TAG_STRING) || NbtCompat.contains(tag, "Item", Tag.TAG_STRING);
            }
        }
        return false;
    }

    private static boolean isLegacyStackTag(CompoundTag tag) {
        return tag.contains("id") && (tag.contains("Count") || tag.contains("tag"));
    }

    private static void writeDisplayComponents(CompoundTag customData, CompoundTag components, int hideFlags) {
        if (!NbtCompat.contains(customData, "display", Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag display = NbtCompat.getCompound(customData, "display").copy();
        writeTextComponent(display, "Name", components, "minecraft:custom_name");
        writeLoreComponent(display, components);
        if (NbtCompat.contains(display, "LocName", Tag.TAG_STRING)) {
            CompoundTag itemName = new CompoundTag();
            itemName.putString("translate", NbtCompat.getString(display, "LocName"));
            components.put("minecraft:item_name", itemName);
            display.remove("LocName");
        }
        if (display.contains("color")) {
            CompoundTag color = new CompoundTag();
            color.putInt("rgb", NbtCompat.getInt(display, "color"));
            if ((hideFlags & 64) != 0) {
                color.putBoolean("show_in_tooltip", false);
            }
            components.put("minecraft:dyed_color", color);
            display.remove("color");
        } else if ((hideFlags & 64) != 0) {
            CompoundTag color = new CompoundTag();
            color.putInt("rgb", 10511680);
            color.putBoolean("show_in_tooltip", false);
            components.put("minecraft:dyed_color", color);
        }
        moveComponent(display, "MapColor", components, "minecraft:map_color");

        if (display.isEmpty()) {
            customData.remove("display");
        } else {
            customData.put("display", display);
        }
    }

    private static void writeTextComponent(CompoundTag source, String sourceKey, CompoundTag components, String componentKey) {
        Tag value = source.get(sourceKey);
        if (value == null) {
            return;
        }

        Tag component = legacyTextComponentToComponentTag(value);
        if (component != null) {
            components.put(componentKey, component);
        }
        source.remove(sourceKey);
    }

    private static void writeLoreComponent(CompoundTag display, CompoundTag components) {
        if (!NbtCompat.contains(display, "Lore", Tag.TAG_LIST)) {
            return;
        }

        ListTag oldLore = NbtCompat.getList(display, "Lore", Tag.TAG_STRING);
        ListTag lore = new ListTag();
        for (int i = 0; i < oldLore.size(); i++) {
            Tag component = legacyTextComponentToComponentTag(oldLore.get(i));
            if (component != null) {
                lore.add(component);
            }
        }
        if (!lore.isEmpty()) {
            components.put("minecraft:lore", lore);
        }
        display.remove("Lore");
    }

    private static Tag legacyTextComponentToComponentTag(Tag tag) {
        if (tag == null) {
            return null;
        }

        if (tag instanceof StringTag stringTag) {
            String raw = stringTag.value();
            Component component;
            try {
                component = ComponentCompat.fromJson(raw);
            } catch (RuntimeException exception) {
                component = Component.literal(raw);
            }
            return componentToTag(component, tag);
        }

        return ComponentSerialization.CODEC
                .parse(provider().createSerializationContext(NbtOps.INSTANCE), tag)
                .map(component -> componentToTag(component, tag))
                .result()
                .orElse(tag.copy());
    }

    private static Tag componentToTag(Component component, Tag fallback) {
        return ComponentSerialization.CODEC
                .encodeStart(provider().createSerializationContext(NbtOps.INSTANCE), component)
                .result()
                .orElseGet(fallback::copy);
    }

    private static void writeEnchantmentsComponent(CompoundTag customData, CompoundTag components, String oldKey, String componentKey, boolean hidden) {
        if (!NbtCompat.contains(customData, oldKey, Tag.TAG_LIST) && !hidden) {
            return;
        }

        ListTag oldEnchantments = NbtCompat.getList(customData, oldKey, Tag.TAG_COMPOUND);
        CompoundTag levels = new CompoundTag();
        for (int i = 0; i < oldEnchantments.size(); i++) {
            CompoundTag enchantment = NbtCompat.getCompound(oldEnchantments, i);
            if (NbtCompat.contains(enchantment, "id", Tag.TAG_STRING)) {
                int level = NbtCompat.getInt(enchantment, "lvl");
                if (level > 0) {
                    levels.putInt(NbtCompat.getString(enchantment, "id"), clamp(level, 1, 255));
                }
            }
        }

        if (!levels.isEmpty() || hidden) {
            if (hidden) {
                hideTooltipComponent(components, componentKey);
            }
            components.put(componentKey, levels);
            if (oldEnchantments.isEmpty()) {
                components.putBoolean("minecraft:enchantment_glint_override", true);
            }
        }
        customData.remove(oldKey);
    }

    private static void hideTooltipComponent(CompoundTag components, String hiddenComponent) {
        CompoundTag tooltipDisplay = NbtCompat.contains(components, "minecraft:tooltip_display", Tag.TAG_COMPOUND)
                ? NbtCompat.getCompound(components, "minecraft:tooltip_display").copy()
                : new CompoundTag();
        ListTag hiddenComponents = NbtCompat.contains(tooltipDisplay, "hidden_components", Tag.TAG_LIST)
                ? NbtCompat.getList(tooltipDisplay, "hidden_components", Tag.TAG_STRING).copy()
                : new ListTag();
        if (!listContainsString(hiddenComponents, hiddenComponent)) {
            hiddenComponents.add(StringTag.valueOf(hiddenComponent));
        }
        tooltipDisplay.put("hidden_components", hiddenComponents);
        components.put("minecraft:tooltip_display", tooltipDisplay);
    }

    private static boolean isTooltipComponentHidden(CompoundTag components, String componentKey) {
        if (!NbtCompat.contains(components, "minecraft:tooltip_display", Tag.TAG_COMPOUND)) {
            return false;
        }

        CompoundTag tooltipDisplay = NbtCompat.getCompound(components, "minecraft:tooltip_display");
        return NbtCompat.getBoolean(tooltipDisplay, "hide_tooltip")
                || listContainsString(NbtCompat.getList(tooltipDisplay, "hidden_components", Tag.TAG_STRING), componentKey);
    }

    private static boolean listContainsString(ListTag list, String value) {
        for (int i = 0; i < list.size(); i++) {
            if (value.equals(NbtCompat.getString(list, i))) {
                return true;
            }
        }
        return false;
    }

    private static void writeUnbreakableComponent(CompoundTag customData, CompoundTag components, int hideFlags) {
        if (!NbtCompat.getBoolean(customData, "Unbreakable")) {
            customData.remove("Unbreakable");
            return;
        }

        CompoundTag unbreakable = new CompoundTag();
        if ((hideFlags & 4) != 0) {
            unbreakable.putBoolean("show_in_tooltip", false);
        }
        components.put("minecraft:unbreakable", unbreakable);
        customData.remove("Unbreakable");
    }

    private static void writeAttributeComponents(CompoundTag customData, CompoundTag components, int hideFlags) {
        if (!NbtCompat.contains(customData, "AttributeModifiers", Tag.TAG_LIST)) {
            return;
        }

        ListTag oldModifiers = NbtCompat.getList(customData, "AttributeModifiers", Tag.TAG_COMPOUND);
        ListTag modifiers = new ListTag();
        for (int i = 0; i < oldModifiers.size(); i++) {
            CompoundTag oldModifier = NbtCompat.getCompound(oldModifiers, i);
            CompoundTag modifier = new CompoundTag();
            String type = NbtCompat.contains(oldModifier, "AttributeName", Tag.TAG_STRING)
                    ? NbtCompat.getString(oldModifier, "AttributeName")
                    : NbtCompat.getString(oldModifier, "type");
            if (type.isBlank()) {
                continue;
            }
            modifier.putString("type", type);
            modifier.putString("id", NbtCompat.contains(oldModifier, "id", Tag.TAG_STRING)
                    ? NbtCompat.getString(oldModifier, "id")
                    : type + "/" + UUID.randomUUID());
            modifier.putDouble("amount", oldModifier.contains("Amount") ? NbtCompat.getDouble(oldModifier, "Amount") : NbtCompat.getDouble(oldModifier, "amount"));
            modifier.putString("operation", operationName(oldModifier.contains("Operation") ? NbtCompat.getInt(oldModifier, "Operation") : 0));
            if (NbtCompat.contains(oldModifier, "operation", Tag.TAG_STRING)) {
                modifier.putString("operation", NbtCompat.getString(oldModifier, "operation"));
            }
            if (NbtCompat.contains(oldModifier, "Slot", Tag.TAG_STRING)) {
                modifier.putString("slot", NbtCompat.getString(oldModifier, "Slot"));
            } else if (NbtCompat.contains(oldModifier, "slot", Tag.TAG_STRING)) {
                modifier.putString("slot", NbtCompat.getString(oldModifier, "slot"));
            }
            modifiers.add(modifier);
        }
        if ((hideFlags & 2) != 0) {
            hideTooltipComponent(components, "minecraft:attribute_modifiers");
        }
        if (!modifiers.isEmpty()) {
            components.put("minecraft:attribute_modifiers", modifiers);
        }
        customData.remove("AttributeModifiers");
    }

    private static int readComponentDisplay(CompoundTag components, CompoundTag tag, int hideFlags) {
        CompoundTag display = NbtCompat.contains(tag, "display", Tag.TAG_COMPOUND) ? NbtCompat.getCompound(tag, "display").copy() : new CompoundTag();
        readTextComponent(components, "minecraft:custom_name", display, "Name");
        readTextComponent(components, "minecraft:item_name", display, "Name");
        readLoreComponent(components, display);
        if (components.contains("minecraft:dyed_color")) {
            Tag dyed = components.get("minecraft:dyed_color");
            if (dyed instanceof CompoundTag dyedTag) {
                display.putInt("color", NbtCompat.getInt(dyedTag, "rgb"));
                if (dyedTag.contains("show_in_tooltip") && !NbtCompat.getBoolean(dyedTag, "show_in_tooltip")) {
                    hideFlags |= 64;
                }
            } else if (dyed != null) {
                display.put("color", dyed.copy());
            }
        }
        copyComponent(components, "minecraft:map_color", display, "MapColor");
        if (display.isEmpty()) {
            tag.remove("display");
        } else {
            tag.put("display", display);
        }
        return hideFlags;
    }

    private static void readTextComponent(CompoundTag components, String componentKey, CompoundTag display, String oldKey) {
        Tag component = components.get(componentKey);
        if (component != null) {
            display.putString(oldKey, componentTagToLegacyJson(component));
        }
    }

    private static void readLoreComponent(CompoundTag components, CompoundTag display) {
        if (!NbtCompat.contains(components, "minecraft:lore", Tag.TAG_LIST)) {
            return;
        }

        ListTag componentLore = NbtCompat.getList(components, "minecraft:lore", Tag.TAG_STRING);
        ListTag lore = new ListTag();
        for (int i = 0; i < componentLore.size(); i++) {
            lore.add(StringTag.valueOf(componentTagToLegacyJson(componentLore.get(i))));
        }
        if (!lore.isEmpty()) {
            display.put("Lore", lore);
        }
    }

    private static String componentTagToLegacyJson(Tag componentTag) {
        return ComponentSerialization.CODEC
                .parse(provider().createSerializationContext(NbtOps.INSTANCE), componentTag)
                .map(ComponentCompat::toJson)
                .result()
                .orElseGet(componentTag::toString);
    }

    private static int readComponentAttributes(CompoundTag components, CompoundTag tag, int hideFlags) {
        if (!components.contains("minecraft:attribute_modifiers")) {
            return hideFlags;
        }

        Tag componentTag = components.get("minecraft:attribute_modifiers");
        CompoundTag component = componentTag instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
        ListTag modifiers = componentTag instanceof ListTag listTag
                ? listTag
                : NbtCompat.getList(component, "modifiers", Tag.TAG_COMPOUND);
        ListTag oldModifiers = new ListTag();
        for (int i = 0; i < modifiers.size(); i++) {
            CompoundTag modifier = NbtCompat.getCompound(modifiers, i);
            CompoundTag oldModifier = new CompoundTag();
            oldModifier.putString("AttributeName", NbtCompat.getString(modifier, "type"));
            oldModifier.putString("id", NbtCompat.getString(modifier, "id"));
            oldModifier.putDouble("Amount", NbtCompat.getDouble(modifier, "amount"));
            oldModifier.putString("operation", NbtCompat.getString(modifier, "operation"));
            oldModifier.putInt("Operation", operationId(NbtCompat.getString(modifier, "operation")));
            if (NbtCompat.contains(modifier, "slot", Tag.TAG_STRING)) {
                oldModifier.putString("Slot", NbtCompat.getString(modifier, "slot"));
            }
            oldModifiers.add(oldModifier);
        }
        if (!oldModifiers.isEmpty()) {
            tag.put("AttributeModifiers", oldModifiers);
        }
        if (isTooltipComponentHidden(components, "minecraft:attribute_modifiers")
                || component.contains("show_in_tooltip") && !NbtCompat.getBoolean(component, "show_in_tooltip")) {
            hideFlags |= 2;
        }
        return hideFlags;
    }

    private static int readComponentAdventure(CompoundTag components, CompoundTag tag, int hideFlags) {
        hideFlags = readStringPredicateComponent(components, tag, "minecraft:can_break", "CanDestroy", hideFlags, 8);
        return readStringPredicateComponent(components, tag, "minecraft:can_place_on", "CanPlaceOn", hideFlags, 16);
    }

    private static int readStringPredicateComponent(CompoundTag components, CompoundTag tag, String componentKey, String oldKey, int hideFlags, int hideMask) {
        if (!NbtCompat.contains(components, componentKey, Tag.TAG_COMPOUND)) {
            return hideFlags;
        }

        CompoundTag component = NbtCompat.getCompound(components, componentKey);
        ListTag predicates = NbtCompat.getList(component, "predicates", Tag.TAG_COMPOUND);
        ListTag oldList = new ListTag();
        for (int i = 0; i < predicates.size(); i++) {
            CompoundTag predicate = NbtCompat.getCompound(predicates, i);
            if (NbtCompat.contains(predicate, "blocks", Tag.TAG_STRING)) {
                oldList.add(StringTag.valueOf(NbtCompat.getString(predicate, "blocks")));
            }
        }
        if (!oldList.isEmpty()) {
            tag.put(oldKey, oldList);
        }
        if (component.contains("show_in_tooltip") && !NbtCompat.getBoolean(component, "show_in_tooltip")) {
            hideFlags |= hideMask;
        }
        return hideFlags;
    }

    private static void writeAdventureComponents(CompoundTag customData, CompoundTag components, int hideFlags) {
        writeStringPredicateComponent(customData, components, "CanDestroy", "minecraft:can_break", (hideFlags & 8) != 0);
        writeStringPredicateComponent(customData, components, "CanPlaceOn", "minecraft:can_place_on", (hideFlags & 16) != 0);
    }

    private static void writeStringPredicateComponent(CompoundTag customData, CompoundTag components, String oldKey, String componentKey, boolean hidden) {
        if (!NbtCompat.contains(customData, oldKey, Tag.TAG_LIST)) {
            return;
        }

        ListTag oldList = NbtCompat.getList(customData, oldKey, Tag.TAG_STRING);
        ListTag predicates = new ListTag();
        for (int i = 0; i < oldList.size(); i++) {
            CompoundTag predicate = new CompoundTag();
            predicate.putString("blocks", NbtCompat.getString(oldList, i));
            predicates.add(predicate);
        }
        CompoundTag component = new CompoundTag();
        component.put("predicates", predicates);
        if (hidden) {
            component.putBoolean("show_in_tooltip", false);
        }
        components.put(componentKey, component);
        customData.remove(oldKey);
    }

    private static void writePotionComponents(CompoundTag customData, CompoundTag components) {
        CompoundTag potion = new CompoundTag();
        if (NbtCompat.contains(customData, "Potion", Tag.TAG_STRING) && !"minecraft:empty".equals(NbtCompat.getString(customData, "Potion"))) {
            potion.putString("potion", NbtCompat.getString(customData, "Potion"));
        }
        moveInto(customData, potion, "CustomPotionColor", "custom_color");
        if (NbtCompat.contains(customData, "CustomPotionEffects", Tag.TAG_LIST)) {
            potion.put("custom_effects", convertPotionEffects(NbtCompat.getList(customData, "CustomPotionEffects", Tag.TAG_COMPOUND)));
            customData.remove("CustomPotionEffects");
        } else {
            moveInto(customData, potion, "custom_potion_effects", "custom_effects");
        }
        customData.remove("Potion");
        if (!potion.isEmpty()) {
            components.put("minecraft:potion_contents", potion);
        }
    }

    private static void writeBookComponents(CompoundTag customData, CompoundTag components) {
        if (NbtCompat.contains(customData, "pages", Tag.TAG_LIST) && !customData.contains("title")) {
            CompoundTag book = new CompoundTag();
            book.put("pages", filterableStringList(NbtCompat.getList(customData, "pages", Tag.TAG_STRING), null));
            components.put("minecraft:writable_book_content", book);
            customData.remove("pages");
            return;
        }

        if (NbtCompat.contains(customData, "title") || customData.contains("author") || NbtCompat.contains(customData, "pages", Tag.TAG_LIST)) {
            CompoundTag book = new CompoundTag();
            book.put("title", filterableText(NbtCompat.getString(customData, "title"),
                    NbtCompat.contains(customData, "filtered_title", Tag.TAG_STRING) ? NbtCompat.getString(customData, "filtered_title") : null));
            book.putString("author", NbtCompat.getString(customData, "author"));
            if (customData.contains("generation")) {
                book.putInt("generation", clamp(NbtCompat.getInt(customData, "generation"), 0, 3));
            }
            if (customData.contains("resolved")) {
                book.putBoolean("resolved", NbtCompat.getBoolean(customData, "resolved"));
            }
            if (NbtCompat.contains(customData, "pages", Tag.TAG_LIST)) {
                book.put("pages", filterableStringList(NbtCompat.getList(customData, "pages", Tag.TAG_STRING),
                        NbtCompat.contains(customData, "filtered_pages", Tag.TAG_COMPOUND) ? NbtCompat.getCompound(customData, "filtered_pages") : null));
            }
            components.put("minecraft:written_book_content", book);
            customData.remove("title");
            customData.remove("filtered_title");
            customData.remove("author");
            customData.remove("generation");
            customData.remove("resolved");
            customData.remove("pages");
            customData.remove("filtered_pages");
        }
    }

    private static void writeFireworkComponents(CompoundTag customData, CompoundTag components) {
        if (NbtCompat.contains(customData, "Explosion", Tag.TAG_COMPOUND)) {
            components.put("minecraft:firework_explosion", fireworkExplosionToComponent(NbtCompat.getCompound(customData, "Explosion")));
            customData.remove("Explosion");
        }
        if (NbtCompat.contains(customData, "Fireworks", Tag.TAG_COMPOUND)) {
            CompoundTag oldFireworks = NbtCompat.getCompound(customData, "Fireworks");
            CompoundTag fireworks = new CompoundTag();
            fireworks.putInt("flight_duration", NbtCompat.getByte(oldFireworks, "Flight") & 255);
            if (NbtCompat.contains(oldFireworks, "Explosions", Tag.TAG_LIST)) {
                ListTag explosions = new ListTag();
                ListTag oldExplosions = NbtCompat.getList(oldFireworks, "Explosions", Tag.TAG_COMPOUND);
                for (int i = 0; i < oldExplosions.size(); i++) {
                    explosions.add(fireworkExplosionToComponent(NbtCompat.getCompound(oldExplosions, i)));
                }
                fireworks.put("explosions", explosions);
            }
            components.put("minecraft:fireworks", fireworks);
            customData.remove("Fireworks");
        }
    }

    private static void writeBundleContentsComponent(String itemId, CompoundTag customData, CompoundTag components) {
        if (!isBundleItemId(itemId) || !NbtCompat.contains(customData, "Items", Tag.TAG_LIST)) {
            return;
        }

        components.put("minecraft:bundle_contents", bundleItemsToComponent(NbtCompat.getList(customData, "Items", Tag.TAG_COMPOUND)));
        customData.remove("Items");
    }

    private static void writeProfileComponent(CompoundTag customData, CompoundTag components) {
        if (!customData.contains("SkullOwner")) {
            return;
        }

        Tag skullOwner = customData.get("SkullOwner");
        if (skullOwner instanceof StringTag stringTag) {
            components.putString("minecraft:profile", stringTag.value());
        } else if (skullOwner instanceof CompoundTag skullOwnerTag) {
            CompoundTag profile = new CompoundTag();
            if (NbtCompat.contains(skullOwnerTag, "Name", Tag.TAG_STRING)) {
                profile.putString("name", NbtCompat.getString(skullOwnerTag, "Name"));
            }
            if (NbtCompat.hasUUID(skullOwnerTag, "Id")) {
                NbtCompat.putUUID(profile, "id", NbtCompat.getUUID(skullOwnerTag, "Id"));
            }
            if (NbtCompat.contains(skullOwnerTag, "Properties", Tag.TAG_COMPOUND)) {
                profile.put("properties", profilePropertiesToComponent(NbtCompat.getCompound(skullOwnerTag, "Properties")));
            }
            components.put("minecraft:profile", profile);
        }
        customData.remove("SkullOwner");
    }

    private static void writeEntityDataComponent(String itemId, CompoundTag customData, CompoundTag components) {
        if (!NbtCompat.contains(customData, "EntityTag", Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag entity = NbtCompat.getCompound(customData, "EntityTag").copy();
        ensureId(entity, inferEntityId(itemId));
        components.put("minecraft:entity_data", entity);
        customData.remove("EntityTag");
    }

    private static void writeBlockEntityComponents(String itemId, CompoundTag customData, CompoundTag components) {
        if (!NbtCompat.contains(customData, "BlockEntityTag", Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag blockEntity = NbtCompat.getCompound(customData, "BlockEntityTag").copy();
        ensureId(blockEntity, inferBlockEntityId(itemId));
        if (NbtCompat.contains(blockEntity, "Items", Tag.TAG_LIST)) {
            components.put("minecraft:container", containerItemsToComponent(NbtCompat.getList(blockEntity, "Items", Tag.TAG_COMPOUND)));
            blockEntity.remove("Items");
        }
        if (NbtCompat.contains(blockEntity, "Patterns", Tag.TAG_LIST)) {
            components.put("minecraft:banner_patterns", bannerPatternsToComponent(NbtCompat.getList(blockEntity, "Patterns", Tag.TAG_COMPOUND)));
            blockEntity.remove("Patterns");
        }
        if (blockEntity.contains("Base")) {
            components.putString("minecraft:base_color", dyeNameById(NbtCompat.getInt(blockEntity, "Base")));
            blockEntity.remove("Base");
        }
        if ("minecraft:decorated_pot".equals(itemId) && NbtCompat.contains(blockEntity, "sherds", Tag.TAG_LIST)) {
            components.put("minecraft:pot_decorations", potDecorationsToComponent(NbtCompat.getList(blockEntity, "sherds", Tag.TAG_STRING)));
            blockEntity.remove("sherds");
        }
        moveInto(blockEntity, components, "Lock", "minecraft:lock");
        if (NbtCompat.contains(blockEntity, "LootTable", Tag.TAG_STRING)) {
            CompoundTag loot = new CompoundTag();
            loot.putString("loot_table", NbtCompat.getString(blockEntity, "LootTable"));
            if (blockEntity.contains("LootTableSeed")) {
                loot.putLong("seed", NbtCompat.getLong(blockEntity, "LootTableSeed"));
            }
            components.put("minecraft:container_loot", loot);
            blockEntity.remove("LootTable");
            blockEntity.remove("LootTableSeed");
        }
        if (!blockEntity.isEmpty() && !isOnlyId(blockEntity)) {
            components.put("minecraft:block_entity_data", blockEntity);
        }
        customData.remove("BlockEntityTag");
    }

    private static int readComponentEnchantments(CompoundTag components, CompoundTag tag, int hideFlags) {
        hideFlags = readEnchantmentsComponent(components, tag, "minecraft:enchantments", "Enchantments", hideFlags, 1);
        hideFlags = readEnchantmentsComponent(components, tag, "minecraft:stored_enchantments", "StoredEnchantments", hideFlags, 32);
        return hideFlags;
    }

    private static int readEnchantmentsComponent(CompoundTag components, CompoundTag tag, String componentKey, String oldKey, int hideFlags, int hideMask) {
        if (!components.contains(componentKey)) {
            return hideFlags;
        }

        CompoundTag component = components.get(componentKey) instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
        CompoundTag levels = NbtCompat.contains(component, "levels", Tag.TAG_COMPOUND) ? NbtCompat.getCompound(component, "levels") : component;
        ListTag enchantments = new ListTag();
        for (String id : levels.keySet()) {
            int level = NbtCompat.getInt(levels, id);
            if (level <= 0) {
                continue;
            }
            CompoundTag enchantment = new CompoundTag();
            enchantment.putString("id", id);
            enchantment.putShort("lvl", (short) level);
            enchantments.add(enchantment);
        }
        if (!enchantments.isEmpty()) {
            tag.put(oldKey, enchantments);
        }
        if (isTooltipComponentHidden(components, componentKey)
                || component.contains("show_in_tooltip") && !NbtCompat.getBoolean(component, "show_in_tooltip")) {
            hideFlags |= hideMask;
        }
        return hideFlags;
    }

    private static int readComponentUnbreakable(CompoundTag components, CompoundTag tag, int hideFlags) {
        if (components.contains("minecraft:unbreakable")) {
            tag.putBoolean("Unbreakable", true);
            Tag unbreakable = components.get("minecraft:unbreakable");
            if (unbreakable instanceof CompoundTag compoundTag
                    && compoundTag.contains("show_in_tooltip")
                    && !NbtCompat.getBoolean(compoundTag, "show_in_tooltip")) {
                hideFlags |= 4;
            }
        }
        return hideFlags;
    }

    private static void readComponentPotion(CompoundTag components, CompoundTag tag) {
        if (!NbtCompat.contains(components, "minecraft:potion_contents", Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag potion = NbtCompat.getCompound(components, "minecraft:potion_contents");
        copyTag(potion, "potion", tag, "Potion");
        copyTag(potion, "custom_color", tag, "CustomPotionColor");
        copyTag(potion, "custom_effects", tag, "CustomPotionEffects");
    }

    private static void readComponentBooks(CompoundTag components, CompoundTag tag) {
        if (NbtCompat.contains(components, "minecraft:writable_book_content", Tag.TAG_COMPOUND)) {
            CompoundTag book = NbtCompat.getCompound(components, "minecraft:writable_book_content");
            tag.put("pages", readFilterableList(NbtCompat.getList(book, "pages", Tag.TAG_COMPOUND)));
        }
        if (NbtCompat.contains(components, "minecraft:written_book_content", Tag.TAG_COMPOUND)) {
            CompoundTag book = NbtCompat.getCompound(components, "minecraft:written_book_content");
            tag.putString("title", readFilterableString(book.get("title")));
            tag.putString("author", NbtCompat.getString(book, "author"));
            if (book.contains("generation")) {
                tag.putInt("generation", NbtCompat.getInt(book, "generation"));
            }
            if (book.contains("resolved")) {
                tag.putBoolean("resolved", NbtCompat.getBoolean(book, "resolved"));
            }
            tag.put("pages", readFilterableList(NbtCompat.getList(book, "pages", Tag.TAG_COMPOUND)));
        }
    }

    private static void readComponentFireworks(CompoundTag components, CompoundTag tag) {
        if (NbtCompat.contains(components, "minecraft:firework_explosion", Tag.TAG_COMPOUND)) {
            tag.put("Explosion", fireworkExplosionFromComponent(NbtCompat.getCompound(components, "minecraft:firework_explosion")));
        }
        if (NbtCompat.contains(components, "minecraft:fireworks", Tag.TAG_COMPOUND)) {
            CompoundTag component = NbtCompat.getCompound(components, "minecraft:fireworks");
            CompoundTag fireworks = new CompoundTag();
            fireworks.putByte("Flight", (byte) NbtCompat.getInt(component, "flight_duration"));
            ListTag oldExplosions = new ListTag();
            ListTag explosions = NbtCompat.getList(component, "explosions", Tag.TAG_COMPOUND);
            for (int i = 0; i < explosions.size(); i++) {
                oldExplosions.add(fireworkExplosionFromComponent(NbtCompat.getCompound(explosions, i)));
            }
            if (!oldExplosions.isEmpty()) {
                fireworks.put("Explosions", oldExplosions);
            }
            tag.put("Fireworks", fireworks);
        }
    }

    private static void readComponentBundleContents(CompoundTag components, CompoundTag tag) {
        if (components.contains("minecraft:bundle_contents")) {
            tag.put("Items", bundleItemsFromComponent(NbtCompat.getList(components, "minecraft:bundle_contents", Tag.TAG_COMPOUND)));
        }
    }

    private static void readComponentProfile(CompoundTag components, CompoundTag tag) {
        if (!components.contains("minecraft:profile")) {
            return;
        }

        Tag profile = components.get("minecraft:profile");
        if (profile instanceof StringTag stringTag) {
            tag.putString("SkullOwner", stringTag.value());
        } else if (profile instanceof CompoundTag profileTag) {
            CompoundTag skullOwner = new CompoundTag();
            copyTag(profileTag, "name", skullOwner, "Name");
            if (NbtCompat.hasUUID(profileTag, "id")) {
                NbtCompat.putUUID(skullOwner, "Id", NbtCompat.getUUID(profileTag, "id"));
            }
            if (NbtCompat.contains(profileTag, "properties", Tag.TAG_LIST)) {
                skullOwner.put("Properties", profilePropertiesFromComponent(NbtCompat.getList(profileTag, "properties", Tag.TAG_COMPOUND)));
            }
            tag.put("SkullOwner", skullOwner);
        }
    }

    private static void readComponentBlockEntity(ItemStack stack, CompoundTag components, CompoundTag tag) {
        CompoundTag blockEntity = NbtCompat.contains(tag, "BlockEntityTag", Tag.TAG_COMPOUND) ? NbtCompat.getCompound(tag, "BlockEntityTag").copy() : new CompoundTag();
        if (NbtCompat.contains(components, "minecraft:block_entity_data", Tag.TAG_COMPOUND)) {
            blockEntity.merge(NbtCompat.getCompound(components, "minecraft:block_entity_data").copy());
        }
        if (components.contains("minecraft:container")) {
            blockEntity.put("Items", containerItemsFromComponent(NbtCompat.getList(components, "minecraft:container", Tag.TAG_COMPOUND)));
        }
        if (components.contains("minecraft:banner_patterns")) {
            blockEntity.put("Patterns", bannerPatternsFromComponent(NbtCompat.getList(components, "minecraft:banner_patterns", Tag.TAG_COMPOUND)));
        }
        if (NbtCompat.contains(components, "minecraft:base_color", Tag.TAG_STRING)) {
            blockEntity.putInt("Base", dyeIdByName(NbtCompat.getString(components, "minecraft:base_color")));
        }
        if (stack.is(Items.DECORATED_POT) && NbtCompat.contains(components, "minecraft:pot_decorations", Tag.TAG_LIST)) {
            blockEntity.put("sherds", potDecorationsFromComponent(NbtCompat.getList(components, "minecraft:pot_decorations", Tag.TAG_STRING)));
        }
        copyTag(components, "minecraft:lock", blockEntity, "Lock");
        if (NbtCompat.contains(components, "minecraft:container_loot", Tag.TAG_COMPOUND)) {
            CompoundTag loot = NbtCompat.getCompound(components, "minecraft:container_loot");
            copyTag(loot, "loot_table", blockEntity, "LootTable");
            copyTag(loot, "seed", blockEntity, "LootTableSeed");
        }
        if (!blockEntity.isEmpty()) {
            tag.put("BlockEntityTag", blockEntity);
        }
    }

    private static ListTag containerItemsToComponent(ListTag oldItems) {
        ListTag items = new ListTag();
        for (int i = 0; i < oldItems.size(); i++) {
            CompoundTag oldItem = NbtCompat.getCompound(oldItems, i);
            CompoundTag item = new CompoundTag();
            item.putInt("slot", NbtCompat.getByte(oldItem, "Slot") & 255);
            CompoundTag stackTag = oldItem.copy();
            stackTag.remove("Slot");
            item.put("item", normalizeStackTag(stackTag));
            items.add(item);
        }
        return items;
    }

    private static ListTag containerItemsFromComponent(ListTag componentItems) {
        ListTag items = new ListTag();
        for (int i = 0; i < componentItems.size(); i++) {
            CompoundTag componentItem = NbtCompat.getCompound(componentItems, i);
            if (!NbtCompat.contains(componentItem, "item", Tag.TAG_COMPOUND)) {
                continue;
            }
            CompoundTag item = NbtCompat.getCompound(componentItem, "item").copy();
            item.putByte("Slot", (byte) NbtCompat.getInt(componentItem, "slot"));
            items.add(item);
        }
        return items;
    }

    private static ListTag bundleItemsToComponent(ListTag oldItems) {
        ListTag items = new ListTag();
        for (int i = 0; i < oldItems.size(); i++) {
            Tag oldItem = oldItems.get(i);
            if (oldItem instanceof CompoundTag oldItemTag) {
                CompoundTag stackTag = oldItemTag.copy();
                stackTag.remove("Slot");
                items.add(normalizeStackTag(stackTag));
            } else if (oldItem != null) {
                items.add(oldItem.copy());
            }
        }
        return items;
    }

    private static ListTag bundleItemsFromComponent(ListTag componentItems) {
        ListTag items = new ListTag();
        for (int i = 0; i < componentItems.size(); i++) {
            Tag componentItem = componentItems.get(i);
            if (componentItem instanceof CompoundTag itemTag) {
                items.add(itemTag.copy());
            } else if (componentItem instanceof StringTag stringTag) {
                CompoundTag item = new CompoundTag();
                item.putString("id", stringTag.value());
                item.putInt("count", 1);
                items.add(item);
            }
        }
        return items;
    }

    private static ListTag bannerPatternsToComponent(ListTag oldPatterns) {
        ListTag patterns = new ListTag();
        for (int i = 0; i < oldPatterns.size(); i++) {
            CompoundTag oldPattern = NbtCompat.getCompound(oldPatterns, i);
            CompoundTag pattern = new CompoundTag();
            pattern.putString("pattern", bannerPatternId(NbtCompat.getString(oldPattern, "Pattern")));
            pattern.putString("color", dyeNameById(NbtCompat.getInt(oldPattern, "Color")));
            patterns.add(pattern);
        }
        return patterns;
    }

    private static ListTag bannerPatternsFromComponent(ListTag componentPatterns) {
        ListTag patterns = new ListTag();
        for (int i = 0; i < componentPatterns.size(); i++) {
            CompoundTag componentPattern = NbtCompat.getCompound(componentPatterns, i);
            CompoundTag pattern = new CompoundTag();
            pattern.putString("Pattern", bannerPatternHash(NbtCompat.getString(componentPattern, "pattern")));
            pattern.putInt("Color", dyeIdByName(NbtCompat.getString(componentPattern, "color")));
            patterns.add(pattern);
        }
        return patterns;
    }

    private static ListTag potDecorationsToComponent(ListTag oldSherds) {
        ListTag sherds = new ListTag();
        for (int i = 0; i < 4; i++) {
            String itemId = i < oldSherds.size() ? NbtCompat.getString(oldSherds, i) : "minecraft:brick";
            sherds.add(StringTag.valueOf(normalizeItemId(itemId)));
        }
        return sherds;
    }

    private static ListTag potDecorationsFromComponent(ListTag componentSherds) {
        ListTag sherds = new ListTag();
        for (int i = 0; i < Math.min(4, componentSherds.size()); i++) {
            sherds.add(StringTag.valueOf(normalizeItemId(NbtCompat.getString(componentSherds, i))));
        }
        return sherds;
    }

    private static String normalizeItemId(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.isEmpty()) {
            return "minecraft:brick";
        }
        return normalized.contains(":") ? normalized : "minecraft:" + normalized;
    }

    private static CompoundTag fireworkExplosionToComponent(CompoundTag oldExplosion) {
        CompoundTag explosion = new CompoundTag();
        explosion.putString("shape", switch (NbtCompat.getByte(oldExplosion, "Type")) {
            case 1 -> "large_ball";
            case 2 -> "star";
            case 3 -> "creeper";
            case 4 -> "burst";
            default -> "small_ball";
        });
        if (oldExplosion.contains("Colors")) {
            explosion.put("colors", intArrayToList(NbtCompat.getIntArray(oldExplosion, "Colors")));
        }
        if (oldExplosion.contains("FadeColors")) {
            explosion.put("fade_colors", intArrayToList(NbtCompat.getIntArray(oldExplosion, "FadeColors")));
        }
        if (oldExplosion.contains("Trail")) {
            explosion.putBoolean("has_trail", NbtCompat.getBoolean(oldExplosion, "Trail"));
        }
        if (oldExplosion.contains("Flicker")) {
            explosion.putBoolean("has_twinkle", NbtCompat.getBoolean(oldExplosion, "Flicker"));
        }
        return explosion;
    }

    private static CompoundTag fireworkExplosionFromComponent(CompoundTag explosion) {
        CompoundTag oldExplosion = new CompoundTag();
        oldExplosion.putByte("Type", (byte) switch (NbtCompat.getString(explosion, "shape")) {
            case "large_ball" -> 1;
            case "star" -> 2;
            case "creeper" -> 3;
            case "burst" -> 4;
            default -> 0;
        });
        if (NbtCompat.contains(explosion, "colors", Tag.TAG_LIST)) {
            oldExplosion.putIntArray("Colors", listToIntArray(NbtCompat.getList(explosion, "colors", Tag.TAG_INT)));
        }
        if (NbtCompat.contains(explosion, "fade_colors", Tag.TAG_LIST)) {
            oldExplosion.putIntArray("FadeColors", listToIntArray(NbtCompat.getList(explosion, "fade_colors", Tag.TAG_INT)));
        }
        if (explosion.contains("has_trail")) {
            oldExplosion.putBoolean("Trail", NbtCompat.getBoolean(explosion, "has_trail"));
        }
        if (explosion.contains("has_twinkle")) {
            oldExplosion.putBoolean("Flicker", NbtCompat.getBoolean(explosion, "has_twinkle"));
        }
        return oldExplosion;
    }

    private static ListTag convertPotionEffects(ListTag oldEffects) {
        ListTag effects = new ListTag();
        for (int i = 0; i < oldEffects.size(); i++) {
            CompoundTag oldEffect = NbtCompat.getCompound(oldEffects, i);
            CompoundTag effect = oldEffect.copy();
            String id = potionEffectId(oldEffect);
            if (!id.isBlank()) {
                effect.putString("id", id);
            }
            if (oldEffect.contains("Amplifier")) {
                effect.putInt("amplifier", NbtCompat.getByte(oldEffect, "Amplifier") & 255);
            }
            if (oldEffect.contains("Duration")) {
                effect.putInt("duration", NbtCompat.getInt(oldEffect, "Duration"));
            }
            if (oldEffect.contains("Ambient")) {
                effect.putBoolean("ambient", NbtCompat.getBoolean(oldEffect, "Ambient"));
            }
            if (oldEffect.contains("ShowParticles")) {
                effect.putBoolean("show_particles", NbtCompat.getBoolean(oldEffect, "ShowParticles"));
            }
            if (oldEffect.contains("ShowIcon")) {
                effect.putBoolean("show_icon", NbtCompat.getBoolean(oldEffect, "ShowIcon"));
            }
            effect.remove("Id");
            effect.remove("Amplifier");
            effect.remove("Duration");
            effect.remove("Ambient");
            effect.remove("ShowParticles");
            effect.remove("ShowIcon");
            effects.add(effect);
        }
        return effects;
    }

    private static String potionEffectId(CompoundTag effect) {
        if (NbtCompat.contains(effect, "Id", Tag.TAG_STRING)) {
            return NbtCompat.getString(effect, "Id");
        }
        if (NbtCompat.contains(effect, "Id", NbtCompat.TAG_ANY_NUMERIC)) {
            MobEffect mobEffect = BuiltInRegistries.MOB_EFFECT.byId(NbtCompat.getInt(effect, "Id"));
            ResourceLocation id = mobEffect == null ? null : BuiltInRegistries.MOB_EFFECT.getKey(mobEffect);
            return id == null ? "" : id.toString();
        }
        return NbtCompat.contains(effect, "id", Tag.TAG_STRING) ? NbtCompat.getString(effect, "id") : "";
    }

    private static CompoundTag filterableText(String raw, String filtered) {
        CompoundTag tag = new CompoundTag();
        tag.putString("raw", raw == null ? "" : raw);
        if (filtered != null) {
            tag.putString("filtered", filtered);
        }
        return tag;
    }

    private static ListTag filterableStringList(ListTag strings, CompoundTag filteredPages) {
        ListTag pages = new ListTag();
        for (int i = 0; i < strings.size(); i++) {
            String filtered = filteredPages != null && NbtCompat.contains(filteredPages, Integer.toString(i), Tag.TAG_STRING)
                    ? NbtCompat.getString(filteredPages, Integer.toString(i))
                    : null;
            pages.add(filterableText(NbtCompat.getString(strings, i), filtered));
        }
        return pages;
    }

    private static ListTag readFilterableList(ListTag pages) {
        ListTag strings = new ListTag();
        for (int i = 0; i < pages.size(); i++) {
            strings.add(StringTag.valueOf(readFilterableString(pages.get(i))));
        }
        return strings;
    }

    private static String readFilterableString(Tag tag) {
        if (tag instanceof StringTag stringTag) {
            return stringTag.value();
        }
        if (tag instanceof CompoundTag compoundTag) {
            if (NbtCompat.contains(compoundTag, "raw", Tag.TAG_STRING)) {
                return NbtCompat.getString(compoundTag, "raw");
            }
            if (NbtCompat.contains(compoundTag, "filtered", Tag.TAG_STRING)) {
                return NbtCompat.getString(compoundTag, "filtered");
            }
        }
        return "";
    }

    private static ListTag profilePropertiesToComponent(CompoundTag properties) {
        ListTag converted = new ListTag();
        for (String name : properties.keySet()) {
            ListTag values = NbtCompat.getList(properties, name, Tag.TAG_COMPOUND);
            for (int i = 0; i < values.size(); i++) {
                CompoundTag value = NbtCompat.getCompound(values, i);
                CompoundTag property = new CompoundTag();
                property.putString("name", name);
                property.putString("value", NbtCompat.getString(value, "Value"));
                if (NbtCompat.contains(value, "Signature", Tag.TAG_STRING)) {
                    property.putString("signature", NbtCompat.getString(value, "Signature"));
                }
                converted.add(property);
            }
        }
        return converted;
    }

    private static CompoundTag profilePropertiesFromComponent(ListTag properties) {
        CompoundTag converted = new CompoundTag();
        for (int i = 0; i < properties.size(); i++) {
            CompoundTag property = NbtCompat.getCompound(properties, i);
            String name = NbtCompat.getString(property, "name");
            ListTag values = NbtCompat.contains(converted, name, Tag.TAG_LIST) ? NbtCompat.getList(converted, name, Tag.TAG_COMPOUND) : new ListTag();
            CompoundTag value = new CompoundTag();
            value.putString("Value", NbtCompat.getString(property, "value"));
            if (NbtCompat.contains(property, "signature", Tag.TAG_STRING)) {
                value.putString("Signature", NbtCompat.getString(property, "signature"));
            }
            values.add(value);
            converted.put(name, values);
        }
        return converted;
    }

    private static ListTag intArrayToList(int[] values) {
        ListTag list = new ListTag();
        for (int value : values) {
            list.add(IntTag.valueOf(value));
        }
        return list;
    }

    private static int[] listToIntArray(ListTag list) {
        int[] values = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            values[i] = NbtCompat.getInt(list, i);
        }
        return values;
    }

    private static void copyComponent(CompoundTag components, String componentKey, CompoundTag tag, String oldKey) {
        copyTag(components, componentKey, tag, oldKey);
    }

    private static void copyTag(CompoundTag source, String sourceKey, CompoundTag target, String targetKey) {
        Tag value = source.get(sourceKey);
        if (value != null) {
            target.put(targetKey, value.copy());
        }
    }

    private static void moveComponent(CompoundTag source, String oldKey, CompoundTag components, String componentKey) {
        Tag value = source.get(oldKey);
        if (value != null) {
            components.put(componentKey, value.copy());
            source.remove(oldKey);
        }
    }

    private static void moveInto(CompoundTag source, CompoundTag target, String sourceKey, String targetKey) {
        Tag value = source.get(sourceKey);
        if (value != null) {
            target.put(targetKey, value.copy());
            source.remove(sourceKey);
        }
    }

    private static int removeInt(CompoundTag tag, String key) {
        int value = NbtCompat.getInt(tag, key);
        tag.remove(key);
        return value;
    }

    private static String operationName(int id) {
        return switch (id) {
            case 1 -> "add_multiplied_base";
            case 2 -> "add_multiplied_total";
            default -> "add_value";
        };
    }

    private static int operationId(String name) {
        return switch (name) {
            case "add_multiplied_base" -> 1;
            case "add_multiplied_total" -> 2;
            default -> 0;
        };
    }

    private static void ensureId(CompoundTag tag, String inferredId) {
        if (!NbtCompat.contains(tag, "id", Tag.TAG_STRING) && inferredId != null && !inferredId.isBlank()) {
            tag.putString("id", inferredId);
        }
    }

    private static boolean isOnlyId(CompoundTag tag) {
        return tag.size() == 1 && NbtCompat.contains(tag, "id", Tag.TAG_STRING);
    }

    private static String inferEntityId(String itemId) {
        String namespace = namespace(itemId);
        String path = path(itemId);
        if ("armor_stand".equals(path)) {
            return "minecraft:armor_stand";
        }
        if ("item_frame".equals(path)) {
            return "minecraft:item_frame";
        }
        if ("glow_item_frame".equals(path)) {
            return "minecraft:glow_item_frame";
        }
        if ("command_block_minecart".equals(path)) {
            return "minecraft:command_block_minecart";
        }
        if (path.endsWith("_spawn_egg")) {
            return namespace + ":" + path.substring(0, path.length() - "_spawn_egg".length());
        }
        return null;
    }

    private static String inferBlockEntityId(String itemId) {
        String path = path(itemId);
        if (path.endsWith("_shulker_box")) {
            return "minecraft:shulker_box";
        }
        if (path.endsWith("_banner")) {
            return "minecraft:banner";
        }
        if (path.endsWith("_bed")) {
            return "minecraft:bed";
        }
        if (path.endsWith("_hanging_sign")) {
            return "minecraft:hanging_sign";
        }
        if (path.endsWith("_sign")) {
            return "minecraft:sign";
        }
        if (path.endsWith("_head") || path.endsWith("_skull")) {
            return "minecraft:skull";
        }

        String id = BLOCK_ENTITY_BY_ITEM.get(path);
        return id == null ? null : "minecraft:" + id;
    }

    private static boolean isBundleItemId(String itemId) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        return id != null && BuiltInRegistries.ITEM.getValue(id) instanceof BundleItem;
    }

    private static String namespace(String id) {
        int separator = id.indexOf(':');
        return separator >= 0 ? id.substring(0, separator) : "minecraft";
    }

    private static String path(String id) {
        int separator = id.indexOf(':');
        return separator >= 0 ? id.substring(separator + 1) : id;
    }

    private static String bannerPatternId(String hash) {
        String id = hash.contains(":") ? hash.substring(hash.indexOf(':') + 1) : hash;
        return "minecraft:" + BANNER_HASH_TO_PATTERN.getOrDefault(id, id);
    }

    private static String bannerPatternHash(String pattern) {
        String id = pattern.contains(":") ? pattern.substring(pattern.indexOf(':') + 1) : pattern;
        return BANNER_PATTERN_TO_HASH.getOrDefault(id, id);
    }

    private static String dyeNameById(int id) {
        return DyeColor.byId(id).getName();
    }

    private static int dyeIdByName(String name) {
        for (DyeColor color : DyeColor.values()) {
            if (color.getName().equals(name)) {
                return color.getId();
            }
        }
        return DyeColor.WHITE.getId();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Map<String, String> invert(Map<String, String> input) {
        Map<String, String> inverted = new HashMap<>();
        for (Map.Entry<String, String> entry : input.entrySet()) {
            inverted.put(entry.getValue(), entry.getKey());
        }
        return inverted;
    }

    private static final Constructor<CompoundTag> COMPOUND_TAG_CONSTRUCTOR = compoundTagConstructor();
    private static final Constructor<ListTag> LIST_TAG_CONSTRUCTOR = listTagConstructor();

    private static CompoundTag syncedCompound(CompoundTag source, Runnable onChanged) {
        ListeningMap map = new ListeningMap(onChanged);
        map.muted = true;
        for (String key : source.keySet()) {
            Tag value = source.get(key);
            if (value != null) {
                map.put(key, syncedTag(value, onChanged));
            }
        }
        map.muted = false;
        try {
            return COMPOUND_TAG_CONSTRUCTOR.newInstance(map);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Unable to create synced CompoundTag", exception);
        }
    }

    private static ListTag syncedList(ListTag source, Runnable onChanged) {
        ListeningList list = new ListeningList(onChanged);
        list.muted = true;
        for (int i = 0; i < source.size(); i++) {
            list.add(syncedTag(source.get(i), onChanged));
        }
        list.muted = false;
        try {
            return LIST_TAG_CONSTRUCTOR.newInstance(list);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Unable to create synced ListTag", exception);
        }
    }

    private static Tag syncedTag(Tag tag, Runnable onChanged) {
        if (tag instanceof CompoundTag compoundTag) {
            return syncedCompound(compoundTag, onChanged);
        }
        if (tag instanceof ListTag listTag) {
            return syncedList(listTag, onChanged);
        }
        return tag.copy();
    }

    private static Constructor<CompoundTag> compoundTagConstructor() {
        try {
            Constructor<CompoundTag> constructor = CompoundTag.class.getDeclaredConstructor(Map.class);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("CompoundTag map constructor is unavailable", exception);
        }
    }

    private static Constructor<ListTag> listTagConstructor() {
        try {
            Constructor<ListTag> constructor = ListTag.class.getDeclaredConstructor(List.class);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("ListTag list constructor is unavailable", exception);
        }
    }

    private static final class ListeningMap extends HashMap<String, Tag> {
        private final Runnable onChanged;
        private boolean muted;

        private ListeningMap(Runnable onChanged) {
            this.onChanged = onChanged;
        }

        @Override
        public Tag put(String key, Tag value) {
            Tag old = super.put(key, syncedTag(value, this.onChanged));
            changed();
            return old;
        }

        @Override
        public Tag remove(Object key) {
            Tag old = super.remove(key);
            changed();
            return old;
        }

        @Override
        public void clear() {
            super.clear();
            changed();
        }

        private void changed() {
            if (!this.muted) {
                this.onChanged.run();
            }
        }
    }

    private static final class ListeningList extends ArrayList<Tag> {
        private final Runnable onChanged;
        private boolean muted;

        private ListeningList(Runnable onChanged) {
            this.onChanged = onChanged;
        }

        @Override
        public Tag set(int index, Tag element) {
            Tag old = super.set(index, syncedTag(element, this.onChanged));
            changed();
            return old;
        }

        @Override
        public void add(int index, Tag element) {
            super.add(index, syncedTag(element, this.onChanged));
            changed();
        }

        @Override
        public boolean add(Tag tag) {
            boolean added = super.add(syncedTag(tag, this.onChanged));
            if (added) {
                changed();
            }
            return added;
        }

        @Override
        public Tag remove(int index) {
            Tag old = super.remove(index);
            changed();
            return old;
        }

        @Override
        public boolean remove(Object value) {
            boolean removed = super.remove(value);
            if (removed) {
                changed();
            }
            return removed;
        }

        @Override
        public void clear() {
            if (isEmpty()) {
                return;
            }
            super.clear();
            changed();
        }

        private void changed() {
            if (!this.muted) {
                this.onChanged.run();
            }
        }
    }
}

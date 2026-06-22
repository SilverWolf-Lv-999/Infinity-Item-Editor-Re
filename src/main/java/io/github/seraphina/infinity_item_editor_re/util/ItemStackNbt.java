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
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ItemStackNbt {
    private static final Set<String> FULL_STACK_KEYS = Set.of("id", "item", "Item", "count", "Count", "components", "tag", "nbt");
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
        if (tag == null || !tag.contains(key, Tag.TAG_COMPOUND)) {
            return null;
        }
        return tag.getCompound(key);
    }

    public static CompoundTag getOrCreateElement(ItemStack stack, String key) {
        CompoundTag tag = getOrCreate(stack);
        if (!tag.contains(key, Tag.TAG_COMPOUND)) {
            tag.put(key, new CompoundTag());
        }
        return tag.getCompound(key);
    }

    public static void set(ItemStack stack, CompoundTag tag) {
        applyLegacyTag(stack, tag == null ? new CompoundTag() : tag);
    }

    public static CompoundTag save(ItemStack stack) {
        Tag saved = stack.saveOptional(provider());
        return saved instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
    }

    public static ItemStack parse(CompoundTag tag) {
        ItemStack parsed = ItemStack.parseOptional(provider(), normalizeStackTag(tag));
        if (!parsed.isEmpty() || !isLegacyStackTag(tag)) {
            return parsed;
        }

        ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
        Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        int count = tag.contains("Count") ? tag.getByte("Count") : 1;
        ItemStack stack = new ItemStack(item, Math.max(1, count));
        if (tag.contains("tag", Tag.TAG_COMPOUND)) {
            set(stack, tag.getCompound("tag").copy());
        }
        return stack;
    }

    public static ItemStack parseEditorNbt(ItemStack current, CompoundTag tag) {
        if (isFullStackTag(tag)) {
            ItemStack parsed = parse(tag);
            if (!parsed.isEmpty() || "minecraft:air".equals(tag.getString("id"))) {
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

    private static SyncedCompoundTag syncedRoot(ItemStack stack, CompoundTag tag) {
        final SyncedCompoundTag[] holder = new SyncedCompoundTag[1];
        holder[0] = new SyncedCompoundTag(tag, () -> applyLegacyTag(stack, holder[0]));
        return holder[0];
    }

    private static CompoundTag buildLegacyTag(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        CompoundTag saved = save(stack);
        CompoundTag components = saved.getCompound("components");
        CompoundTag tag = new CompoundTag();
        int hideFlags = 0;

        if (components.contains("minecraft:custom_data", Tag.TAG_COMPOUND)) {
            tag.merge(components.getCompound("minecraft:custom_data").copy());
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
        readComponentProfile(components, tag);
        readComponentBlockEntity(stack, components, tag);

        if (components.contains("minecraft:trim", Tag.TAG_COMPOUND)
                && !components.getCompound("minecraft:trim").getBoolean("show_in_tooltip")) {
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

        if (tag == null || tag.isEmpty()) {
            applyLegacyComponentsFrom(stack, new ItemStack(stack.getItem(), stack.getCount()));
            return;
        }

        CompoundTag stackTag = stackTagFromLegacy(stack, tag);
        ItemStack parsed = ItemStack.parseOptional(provider(), stackTag);
        if (parsed.isEmpty() && !stack.is(Items.AIR)) {
            ItemStack fallback = new ItemStack(stack.getItem(), stack.getCount());
            fallback.set(DataComponents.CUSTOM_DATA, CustomData.of(tag.copy()));
            applyLegacyComponentsFrom(stack, fallback);
            return;
        }

        applyLegacyComponentsFrom(stack, parsed);
    }

    private static void applyLegacyComponentsFrom(ItemStack stack, ItemStack source) {
        copyDataComponent(source, stack, DataComponents.CUSTOM_DATA);
        copyDataComponent(source, stack, DataComponents.DAMAGE);
        copyDataComponent(source, stack, DataComponents.REPAIR_COST);
        copyDataComponent(source, stack, DataComponents.CUSTOM_MODEL_DATA);
        copyDataComponent(source, stack, DataComponents.ENTITY_DATA);
        copyDataComponent(source, stack, DataComponents.BLOCK_ENTITY_DATA);
        copyDataComponent(source, stack, DataComponents.UNBREAKABLE);
        copyDataComponent(source, stack, DataComponents.ENCHANTMENTS);
        copyDataComponent(source, stack, DataComponents.STORED_ENCHANTMENTS);
        copyDataComponent(source, stack, DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        copyDataComponent(source, stack, DataComponents.CUSTOM_NAME);
        copyDataComponent(source, stack, DataComponents.ITEM_NAME);
        copyDataComponent(source, stack, DataComponents.LORE);
        copyDataComponent(source, stack, DataComponents.DYED_COLOR);
        copyDataComponent(source, stack, DataComponents.MAP_COLOR);
        copyDataComponent(source, stack, DataComponents.CAN_BREAK);
        copyDataComponent(source, stack, DataComponents.CAN_PLACE_ON);
        copyDataComponent(source, stack, DataComponents.ATTRIBUTE_MODIFIERS);
        copyDataComponent(source, stack, DataComponents.TRIM);
        copyDataComponent(source, stack, DataComponents.HIDE_ADDITIONAL_TOOLTIP);
        copyDataComponent(source, stack, DataComponents.HIDE_TOOLTIP);
        copyDataComponent(source, stack, DataComponents.CHARGED_PROJECTILES);
        copyDataComponent(source, stack, DataComponents.BUNDLE_CONTENTS);
        copyDataComponent(source, stack, DataComponents.MAP_ID);
        copyDataComponent(source, stack, DataComponents.MAP_DECORATIONS);
        copyDataComponent(source, stack, DataComponents.POTION_CONTENTS);
        copyDataComponent(source, stack, DataComponents.WRITABLE_BOOK_CONTENT);
        copyDataComponent(source, stack, DataComponents.WRITTEN_BOOK_CONTENT);
        copyDataComponent(source, stack, DataComponents.SUSPICIOUS_STEW_EFFECTS);
        copyDataComponent(source, stack, DataComponents.DEBUG_STICK_STATE);
        copyDataComponent(source, stack, DataComponents.BUCKET_ENTITY_DATA);
        copyDataComponent(source, stack, DataComponents.INSTRUMENT);
        copyDataComponent(source, stack, DataComponents.RECIPES);
        copyDataComponent(source, stack, DataComponents.LODESTONE_TRACKER);
        copyDataComponent(source, stack, DataComponents.FIREWORKS);
        copyDataComponent(source, stack, DataComponents.FIREWORK_EXPLOSION);
        copyDataComponent(source, stack, DataComponents.PROFILE);
        copyDataComponent(source, stack, DataComponents.NOTE_BLOCK_SOUND);
        copyDataComponent(source, stack, DataComponents.BANNER_PATTERNS);
        copyDataComponent(source, stack, DataComponents.BASE_COLOR);
        copyDataComponent(source, stack, DataComponents.CONTAINER);
        copyDataComponent(source, stack, DataComponents.BEES);
        copyDataComponent(source, stack, DataComponents.LOCK);
        copyDataComponent(source, stack, DataComponents.CONTAINER_LOOT);
        copyDataComponent(source, stack, DataComponents.POT_DECORATIONS);
    }

    private static <T> void copyDataComponent(ItemStack source, ItemStack target, DataComponentType<T> component) {
        target.set(component, source.get(component));
    }

    private static CompoundTag stackTagFromLegacy(ItemStack stack, CompoundTag legacyTag) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return stackTagFromLegacy(itemId == null ? "minecraft:air" : itemId.toString(), Math.max(1, stack.getCount()), legacyTag);
    }

    private static CompoundTag stackTagFromLegacy(String itemId, int count, CompoundTag legacyTag) {
        CompoundTag stackTag = new CompoundTag();
        stackTag.putString("id", itemId);
        stackTag.putInt("count", Math.max(1, count));

        CompoundTag customData = legacyTag.copy();
        CompoundTag components = new CompoundTag();
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
        if (!normalized.contains("id", Tag.TAG_STRING)) {
            if (normalized.contains("item", Tag.TAG_STRING)) {
                normalized.putString("id", normalized.getString("item"));
            } else if (normalized.contains("Item", Tag.TAG_STRING)) {
                normalized.putString("id", normalized.getString("Item"));
            }
        }
        if (!normalized.contains("count") && normalized.contains("Count")) {
            normalized.putInt("count", normalized.getByte("Count") & 255);
        }
        if (!normalized.contains("count")) {
            normalized.putInt("count", 1);
        }
        if (!normalized.contains("components", Tag.TAG_COMPOUND)) {
            CompoundTag legacy = normalized.contains("tag", Tag.TAG_COMPOUND)
                    ? normalized.getCompound("tag")
                    : normalized.contains("nbt", Tag.TAG_COMPOUND) ? normalized.getCompound("nbt") : new CompoundTag();
            normalized = stackTagFromLegacy(normalized.getString("id"), normalized.getInt("count"), legacy);
        }
        return normalized;
    }

    private static boolean isFullStackTag(CompoundTag tag) {
        for (String key : tag.getAllKeys()) {
            if (FULL_STACK_KEYS.contains(key)) {
                return tag.contains("id", Tag.TAG_STRING) || tag.contains("item", Tag.TAG_STRING) || tag.contains("Item", Tag.TAG_STRING);
            }
        }
        return false;
    }

    private static boolean isLegacyStackTag(CompoundTag tag) {
        return tag.contains("id") && (tag.contains("Count") || tag.contains("tag"));
    }

    private static void writeDisplayComponents(CompoundTag customData, CompoundTag components, int hideFlags) {
        if (!customData.contains("display", Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag display = customData.getCompound("display").copy();
        moveComponent(display, "Name", components, "minecraft:custom_name");
        moveComponent(display, "Lore", components, "minecraft:lore");
        if (display.contains("LocName", Tag.TAG_STRING)) {
            CompoundTag itemName = new CompoundTag();
            itemName.putString("translate", display.getString("LocName"));
            components.put("minecraft:item_name", itemName);
            display.remove("LocName");
        }
        if (display.contains("color")) {
            CompoundTag color = new CompoundTag();
            color.putInt("rgb", display.getInt("color"));
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

    private static void writeEnchantmentsComponent(CompoundTag customData, CompoundTag components, String oldKey, String componentKey, boolean hidden) {
        if (!customData.contains(oldKey, Tag.TAG_LIST) && !hidden) {
            return;
        }

        ListTag oldEnchantments = customData.getList(oldKey, Tag.TAG_COMPOUND);
        CompoundTag levels = new CompoundTag();
        for (int i = 0; i < oldEnchantments.size(); i++) {
            CompoundTag enchantment = oldEnchantments.getCompound(i);
            if (enchantment.contains("id", Tag.TAG_STRING)) {
                levels.putInt(enchantment.getString("id"), clamp(enchantment.getInt("lvl"), 0, 255));
            }
        }

        if (!levels.isEmpty() || hidden) {
            CompoundTag component = new CompoundTag();
            component.put("levels", levels);
            if (hidden) {
                component.putBoolean("show_in_tooltip", false);
            }
            components.put(componentKey, component);
            if (oldEnchantments.isEmpty()) {
                components.putBoolean("minecraft:enchantment_glint_override", true);
            }
        }
        customData.remove(oldKey);
    }

    private static void writeUnbreakableComponent(CompoundTag customData, CompoundTag components, int hideFlags) {
        if (!customData.getBoolean("Unbreakable")) {
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
        if (!customData.contains("AttributeModifiers", Tag.TAG_LIST)) {
            return;
        }

        ListTag oldModifiers = customData.getList("AttributeModifiers", Tag.TAG_COMPOUND);
        ListTag modifiers = new ListTag();
        for (int i = 0; i < oldModifiers.size(); i++) {
            CompoundTag oldModifier = oldModifiers.getCompound(i);
            CompoundTag modifier = new CompoundTag();
            String type = oldModifier.contains("AttributeName", Tag.TAG_STRING)
                    ? oldModifier.getString("AttributeName")
                    : oldModifier.getString("type");
            if (type.isBlank()) {
                continue;
            }
            modifier.putString("type", type);
            modifier.putString("id", oldModifier.contains("id", Tag.TAG_STRING)
                    ? oldModifier.getString("id")
                    : type + "/" + UUID.randomUUID());
            modifier.putDouble("amount", oldModifier.contains("Amount") ? oldModifier.getDouble("Amount") : oldModifier.getDouble("amount"));
            modifier.putString("operation", operationName(oldModifier.contains("Operation") ? oldModifier.getInt("Operation") : 0));
            if (oldModifier.contains("operation", Tag.TAG_STRING)) {
                modifier.putString("operation", oldModifier.getString("operation"));
            }
            if (oldModifier.contains("Slot", Tag.TAG_STRING)) {
                modifier.putString("slot", oldModifier.getString("Slot"));
            } else if (oldModifier.contains("slot", Tag.TAG_STRING)) {
                modifier.putString("slot", oldModifier.getString("slot"));
            }
            modifiers.add(modifier);
        }
        if (!modifiers.isEmpty() || (hideFlags & 2) != 0) {
            CompoundTag component = new CompoundTag();
            component.put("modifiers", modifiers);
            if ((hideFlags & 2) != 0) {
                component.putBoolean("show_in_tooltip", false);
            }
            components.put("minecraft:attribute_modifiers", component);
        }
        customData.remove("AttributeModifiers");
    }

    private static void writeAdventureComponents(CompoundTag customData, CompoundTag components, int hideFlags) {
        writeStringPredicateComponent(customData, components, "CanDestroy", "minecraft:can_break", (hideFlags & 8) != 0);
        writeStringPredicateComponent(customData, components, "CanPlaceOn", "minecraft:can_place_on", (hideFlags & 16) != 0);
    }

    private static void writeStringPredicateComponent(CompoundTag customData, CompoundTag components, String oldKey, String componentKey, boolean hidden) {
        if (!customData.contains(oldKey, Tag.TAG_LIST)) {
            return;
        }

        ListTag oldList = customData.getList(oldKey, Tag.TAG_STRING);
        ListTag predicates = new ListTag();
        for (int i = 0; i < oldList.size(); i++) {
            CompoundTag predicate = new CompoundTag();
            predicate.putString("blocks", oldList.getString(i));
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
        if (customData.contains("Potion", Tag.TAG_STRING) && !"minecraft:empty".equals(customData.getString("Potion"))) {
            potion.putString("potion", customData.getString("Potion"));
        }
        moveInto(customData, potion, "CustomPotionColor", "custom_color");
        if (customData.contains("CustomPotionEffects", Tag.TAG_LIST)) {
            potion.put("custom_effects", convertPotionEffects(customData.getList("CustomPotionEffects", Tag.TAG_COMPOUND)));
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
        if (customData.contains("pages", Tag.TAG_LIST) && !customData.contains("title")) {
            CompoundTag book = new CompoundTag();
            book.put("pages", filterableStringList(customData.getList("pages", Tag.TAG_STRING), null));
            components.put("minecraft:writable_book_content", book);
            customData.remove("pages");
            return;
        }

        if (customData.contains("title") || customData.contains("author") || customData.contains("pages", Tag.TAG_LIST)) {
            CompoundTag book = new CompoundTag();
            book.put("title", filterableText(customData.getString("title"),
                    customData.contains("filtered_title", Tag.TAG_STRING) ? customData.getString("filtered_title") : null));
            book.putString("author", customData.getString("author"));
            if (customData.contains("generation")) {
                book.putInt("generation", clamp(customData.getInt("generation"), 0, 3));
            }
            if (customData.contains("resolved")) {
                book.putBoolean("resolved", customData.getBoolean("resolved"));
            }
            if (customData.contains("pages", Tag.TAG_LIST)) {
                book.put("pages", filterableStringList(customData.getList("pages", Tag.TAG_STRING),
                        customData.contains("filtered_pages", Tag.TAG_COMPOUND) ? customData.getCompound("filtered_pages") : null));
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
        if (customData.contains("Explosion", Tag.TAG_COMPOUND)) {
            components.put("minecraft:firework_explosion", fireworkExplosionToComponent(customData.getCompound("Explosion")));
            customData.remove("Explosion");
        }
        if (customData.contains("Fireworks", Tag.TAG_COMPOUND)) {
            CompoundTag oldFireworks = customData.getCompound("Fireworks");
            CompoundTag fireworks = new CompoundTag();
            fireworks.putInt("flight_duration", oldFireworks.getByte("Flight") & 255);
            if (oldFireworks.contains("Explosions", Tag.TAG_LIST)) {
                ListTag explosions = new ListTag();
                ListTag oldExplosions = oldFireworks.getList("Explosions", Tag.TAG_COMPOUND);
                for (int i = 0; i < oldExplosions.size(); i++) {
                    explosions.add(fireworkExplosionToComponent(oldExplosions.getCompound(i)));
                }
                fireworks.put("explosions", explosions);
            }
            components.put("minecraft:fireworks", fireworks);
            customData.remove("Fireworks");
        }
    }

    private static void writeProfileComponent(CompoundTag customData, CompoundTag components) {
        if (!customData.contains("SkullOwner")) {
            return;
        }

        Tag skullOwner = customData.get("SkullOwner");
        if (skullOwner instanceof StringTag stringTag) {
            components.putString("minecraft:profile", stringTag.getAsString());
        } else if (skullOwner instanceof CompoundTag skullOwnerTag) {
            CompoundTag profile = new CompoundTag();
            if (skullOwnerTag.contains("Name", Tag.TAG_STRING)) {
                profile.putString("name", skullOwnerTag.getString("Name"));
            }
            if (skullOwnerTag.hasUUID("Id")) {
                profile.putUUID("id", skullOwnerTag.getUUID("Id"));
            }
            if (skullOwnerTag.contains("Properties", Tag.TAG_COMPOUND)) {
                profile.put("properties", profilePropertiesToComponent(skullOwnerTag.getCompound("Properties")));
            }
            components.put("minecraft:profile", profile);
        }
        customData.remove("SkullOwner");
    }

    private static void writeEntityDataComponent(String itemId, CompoundTag customData, CompoundTag components) {
        if (!customData.contains("EntityTag", Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag entity = customData.getCompound("EntityTag").copy();
        ensureId(entity, inferEntityId(itemId));
        components.put("minecraft:entity_data", entity);
        customData.remove("EntityTag");
    }

    private static void writeBlockEntityComponents(String itemId, CompoundTag customData, CompoundTag components) {
        if (!customData.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag blockEntity = customData.getCompound("BlockEntityTag").copy();
        ensureId(blockEntity, inferBlockEntityId(itemId));
        if (blockEntity.contains("Items", Tag.TAG_LIST)) {
            components.put("minecraft:container", containerItemsToComponent(blockEntity.getList("Items", Tag.TAG_COMPOUND)));
            blockEntity.remove("Items");
        }
        if (blockEntity.contains("Patterns", Tag.TAG_LIST)) {
            components.put("minecraft:banner_patterns", bannerPatternsToComponent(blockEntity.getList("Patterns", Tag.TAG_COMPOUND)));
            blockEntity.remove("Patterns");
        }
        if (blockEntity.contains("Base")) {
            components.putString("minecraft:base_color", dyeNameById(blockEntity.getInt("Base")));
            blockEntity.remove("Base");
        }
        if ("minecraft:decorated_pot".equals(itemId) && blockEntity.contains("sherds", Tag.TAG_LIST)) {
            components.put("minecraft:pot_decorations", potDecorationsToComponent(blockEntity.getList("sherds", Tag.TAG_STRING)));
            blockEntity.remove("sherds");
        }
        moveInto(blockEntity, components, "Lock", "minecraft:lock");
        if (blockEntity.contains("LootTable", Tag.TAG_STRING)) {
            CompoundTag loot = new CompoundTag();
            loot.putString("loot_table", blockEntity.getString("LootTable"));
            if (blockEntity.contains("LootTableSeed")) {
                loot.putLong("seed", blockEntity.getLong("LootTableSeed"));
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

    private static int readComponentDisplay(CompoundTag components, CompoundTag tag, int hideFlags) {
        CompoundTag display = tag.contains("display", Tag.TAG_COMPOUND) ? tag.getCompound("display").copy() : new CompoundTag();
        copyComponent(components, "minecraft:custom_name", display, "Name");
        copyComponent(components, "minecraft:item_name", display, "Name");
        copyComponent(components, "minecraft:lore", display, "Lore");
        if (components.contains("minecraft:dyed_color")) {
            Tag dyed = components.get("minecraft:dyed_color");
            if (dyed instanceof CompoundTag dyedTag) {
                display.putInt("color", dyedTag.getInt("rgb"));
                if (dyedTag.contains("show_in_tooltip") && !dyedTag.getBoolean("show_in_tooltip")) {
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
        CompoundTag levels = component.contains("levels", Tag.TAG_COMPOUND) ? component.getCompound("levels") : component;
        ListTag enchantments = new ListTag();
        for (String id : levels.getAllKeys()) {
            CompoundTag enchantment = new CompoundTag();
            enchantment.putString("id", id);
            enchantment.putShort("lvl", (short) levels.getInt(id));
            enchantments.add(enchantment);
        }
        if (!enchantments.isEmpty()) {
            tag.put(oldKey, enchantments);
        }
        if (component.contains("show_in_tooltip") && !component.getBoolean("show_in_tooltip")) {
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
                    && !compoundTag.getBoolean("show_in_tooltip")) {
                hideFlags |= 4;
            }
        }
        return hideFlags;
    }

    private static int readComponentAttributes(CompoundTag components, CompoundTag tag, int hideFlags) {
        if (!components.contains("minecraft:attribute_modifiers", Tag.TAG_COMPOUND)) {
            return hideFlags;
        }

        CompoundTag component = components.getCompound("minecraft:attribute_modifiers");
        ListTag modifiers = component.getList("modifiers", Tag.TAG_COMPOUND);
        ListTag oldModifiers = new ListTag();
        for (int i = 0; i < modifiers.size(); i++) {
            CompoundTag modifier = modifiers.getCompound(i);
            CompoundTag oldModifier = new CompoundTag();
            oldModifier.putString("AttributeName", modifier.getString("type"));
            oldModifier.putString("id", modifier.getString("id"));
            oldModifier.putDouble("Amount", modifier.getDouble("amount"));
            oldModifier.putString("operation", modifier.getString("operation"));
            oldModifier.putInt("Operation", operationId(modifier.getString("operation")));
            if (modifier.contains("slot", Tag.TAG_STRING)) {
                oldModifier.putString("Slot", modifier.getString("slot"));
            }
            oldModifiers.add(oldModifier);
        }
        if (!oldModifiers.isEmpty()) {
            tag.put("AttributeModifiers", oldModifiers);
        }
        if (component.contains("show_in_tooltip") && !component.getBoolean("show_in_tooltip")) {
            hideFlags |= 2;
        }
        return hideFlags;
    }

    private static int readComponentAdventure(CompoundTag components, CompoundTag tag, int hideFlags) {
        hideFlags = readStringPredicateComponent(components, tag, "minecraft:can_break", "CanDestroy", hideFlags, 8);
        return readStringPredicateComponent(components, tag, "minecraft:can_place_on", "CanPlaceOn", hideFlags, 16);
    }

    private static int readStringPredicateComponent(CompoundTag components, CompoundTag tag, String componentKey, String oldKey, int hideFlags, int hideMask) {
        if (!components.contains(componentKey, Tag.TAG_COMPOUND)) {
            return hideFlags;
        }

        CompoundTag component = components.getCompound(componentKey);
        ListTag predicates = component.getList("predicates", Tag.TAG_COMPOUND);
        ListTag oldList = new ListTag();
        for (int i = 0; i < predicates.size(); i++) {
            CompoundTag predicate = predicates.getCompound(i);
            if (predicate.contains("blocks", Tag.TAG_STRING)) {
                oldList.add(StringTag.valueOf(predicate.getString("blocks")));
            }
        }
        if (!oldList.isEmpty()) {
            tag.put(oldKey, oldList);
        }
        if (component.contains("show_in_tooltip") && !component.getBoolean("show_in_tooltip")) {
            hideFlags |= hideMask;
        }
        return hideFlags;
    }

    private static void readComponentPotion(CompoundTag components, CompoundTag tag) {
        if (!components.contains("minecraft:potion_contents", Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag potion = components.getCompound("minecraft:potion_contents");
        copyTag(potion, "potion", tag, "Potion");
        copyTag(potion, "custom_color", tag, "CustomPotionColor");
        copyTag(potion, "custom_effects", tag, "CustomPotionEffects");
    }

    private static void readComponentBooks(CompoundTag components, CompoundTag tag) {
        if (components.contains("minecraft:writable_book_content", Tag.TAG_COMPOUND)) {
            CompoundTag book = components.getCompound("minecraft:writable_book_content");
            tag.put("pages", readFilterableList(book.getList("pages", Tag.TAG_COMPOUND)));
        }
        if (components.contains("minecraft:written_book_content", Tag.TAG_COMPOUND)) {
            CompoundTag book = components.getCompound("minecraft:written_book_content");
            tag.putString("title", readFilterableString(book.get("title")));
            tag.putString("author", book.getString("author"));
            if (book.contains("generation")) {
                tag.putInt("generation", book.getInt("generation"));
            }
            if (book.contains("resolved")) {
                tag.putBoolean("resolved", book.getBoolean("resolved"));
            }
            tag.put("pages", readFilterableList(book.getList("pages", Tag.TAG_COMPOUND)));
        }
    }

    private static void readComponentFireworks(CompoundTag components, CompoundTag tag) {
        if (components.contains("minecraft:firework_explosion", Tag.TAG_COMPOUND)) {
            tag.put("Explosion", fireworkExplosionFromComponent(components.getCompound("minecraft:firework_explosion")));
        }
        if (components.contains("minecraft:fireworks", Tag.TAG_COMPOUND)) {
            CompoundTag component = components.getCompound("minecraft:fireworks");
            CompoundTag fireworks = new CompoundTag();
            fireworks.putByte("Flight", (byte) component.getInt("flight_duration"));
            ListTag oldExplosions = new ListTag();
            ListTag explosions = component.getList("explosions", Tag.TAG_COMPOUND);
            for (int i = 0; i < explosions.size(); i++) {
                oldExplosions.add(fireworkExplosionFromComponent(explosions.getCompound(i)));
            }
            if (!oldExplosions.isEmpty()) {
                fireworks.put("Explosions", oldExplosions);
            }
            tag.put("Fireworks", fireworks);
        }
    }

    private static void readComponentProfile(CompoundTag components, CompoundTag tag) {
        if (!components.contains("minecraft:profile")) {
            return;
        }

        Tag profile = components.get("minecraft:profile");
        if (profile instanceof StringTag stringTag) {
            tag.putString("SkullOwner", stringTag.getAsString());
        } else if (profile instanceof CompoundTag profileTag) {
            CompoundTag skullOwner = new CompoundTag();
            copyTag(profileTag, "name", skullOwner, "Name");
            if (profileTag.hasUUID("id")) {
                skullOwner.putUUID("Id", profileTag.getUUID("id"));
            }
            if (profileTag.contains("properties", Tag.TAG_LIST)) {
                skullOwner.put("Properties", profilePropertiesFromComponent(profileTag.getList("properties", Tag.TAG_COMPOUND)));
            }
            tag.put("SkullOwner", skullOwner);
        }
    }

    private static void readComponentBlockEntity(ItemStack stack, CompoundTag components, CompoundTag tag) {
        CompoundTag blockEntity = tag.contains("BlockEntityTag", Tag.TAG_COMPOUND) ? tag.getCompound("BlockEntityTag").copy() : new CompoundTag();
        if (components.contains("minecraft:block_entity_data", Tag.TAG_COMPOUND)) {
            blockEntity.merge(components.getCompound("minecraft:block_entity_data").copy());
        }
        if (components.contains("minecraft:container")) {
            blockEntity.put("Items", containerItemsFromComponent(components.getList("minecraft:container", Tag.TAG_COMPOUND)));
        }
        if (components.contains("minecraft:banner_patterns")) {
            blockEntity.put("Patterns", bannerPatternsFromComponent(components.getList("minecraft:banner_patterns", Tag.TAG_COMPOUND)));
        }
        if (components.contains("minecraft:base_color", Tag.TAG_STRING)) {
            blockEntity.putInt("Base", dyeIdByName(components.getString("minecraft:base_color")));
        }
        if (stack.is(Items.DECORATED_POT) && components.contains("minecraft:pot_decorations", Tag.TAG_LIST)) {
            blockEntity.put("sherds", potDecorationsFromComponent(components.getList("minecraft:pot_decorations", Tag.TAG_STRING)));
        }
        copyTag(components, "minecraft:lock", blockEntity, "Lock");
        if (components.contains("minecraft:container_loot", Tag.TAG_COMPOUND)) {
            CompoundTag loot = components.getCompound("minecraft:container_loot");
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
            CompoundTag oldItem = oldItems.getCompound(i);
            CompoundTag item = new CompoundTag();
            item.putInt("slot", oldItem.getByte("Slot") & 255);
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
            CompoundTag componentItem = componentItems.getCompound(i);
            if (!componentItem.contains("item", Tag.TAG_COMPOUND)) {
                continue;
            }
            CompoundTag item = componentItem.getCompound("item").copy();
            item.putByte("Slot", (byte) componentItem.getInt("slot"));
            items.add(item);
        }
        return items;
    }

    private static ListTag bannerPatternsToComponent(ListTag oldPatterns) {
        ListTag patterns = new ListTag();
        for (int i = 0; i < oldPatterns.size(); i++) {
            CompoundTag oldPattern = oldPatterns.getCompound(i);
            CompoundTag pattern = new CompoundTag();
            pattern.putString("pattern", bannerPatternId(oldPattern.getString("Pattern")));
            pattern.putString("color", dyeNameById(oldPattern.getInt("Color")));
            patterns.add(pattern);
        }
        return patterns;
    }

    private static ListTag bannerPatternsFromComponent(ListTag componentPatterns) {
        ListTag patterns = new ListTag();
        for (int i = 0; i < componentPatterns.size(); i++) {
            CompoundTag componentPattern = componentPatterns.getCompound(i);
            CompoundTag pattern = new CompoundTag();
            pattern.putString("Pattern", bannerPatternHash(componentPattern.getString("pattern")));
            pattern.putInt("Color", dyeIdByName(componentPattern.getString("color")));
            patterns.add(pattern);
        }
        return patterns;
    }

    private static ListTag potDecorationsToComponent(ListTag oldSherds) {
        ListTag sherds = new ListTag();
        for (int i = 0; i < 4; i++) {
            String itemId = i < oldSherds.size() ? oldSherds.getString(i) : "minecraft:brick";
            sherds.add(StringTag.valueOf(normalizeItemId(itemId)));
        }
        return sherds;
    }

    private static ListTag potDecorationsFromComponent(ListTag componentSherds) {
        ListTag sherds = new ListTag();
        for (int i = 0; i < Math.min(4, componentSherds.size()); i++) {
            sherds.add(StringTag.valueOf(normalizeItemId(componentSherds.getString(i))));
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
        explosion.putString("shape", switch (oldExplosion.getByte("Type")) {
            case 1 -> "large_ball";
            case 2 -> "star";
            case 3 -> "creeper";
            case 4 -> "burst";
            default -> "small_ball";
        });
        if (oldExplosion.contains("Colors")) {
            explosion.put("colors", intArrayToList(oldExplosion.getIntArray("Colors")));
        }
        if (oldExplosion.contains("FadeColors")) {
            explosion.put("fade_colors", intArrayToList(oldExplosion.getIntArray("FadeColors")));
        }
        if (oldExplosion.contains("Trail")) {
            explosion.putBoolean("has_trail", oldExplosion.getBoolean("Trail"));
        }
        if (oldExplosion.contains("Flicker")) {
            explosion.putBoolean("has_twinkle", oldExplosion.getBoolean("Flicker"));
        }
        return explosion;
    }

    private static CompoundTag fireworkExplosionFromComponent(CompoundTag explosion) {
        CompoundTag oldExplosion = new CompoundTag();
        oldExplosion.putByte("Type", (byte) switch (explosion.getString("shape")) {
            case "large_ball" -> 1;
            case "star" -> 2;
            case "creeper" -> 3;
            case "burst" -> 4;
            default -> 0;
        });
        if (explosion.contains("colors", Tag.TAG_LIST)) {
            oldExplosion.putIntArray("Colors", listToIntArray(explosion.getList("colors", Tag.TAG_INT)));
        }
        if (explosion.contains("fade_colors", Tag.TAG_LIST)) {
            oldExplosion.putIntArray("FadeColors", listToIntArray(explosion.getList("fade_colors", Tag.TAG_INT)));
        }
        if (explosion.contains("has_trail")) {
            oldExplosion.putBoolean("Trail", explosion.getBoolean("has_trail"));
        }
        if (explosion.contains("has_twinkle")) {
            oldExplosion.putBoolean("Flicker", explosion.getBoolean("has_twinkle"));
        }
        return oldExplosion;
    }

    private static ListTag convertPotionEffects(ListTag oldEffects) {
        ListTag effects = new ListTag();
        for (int i = 0; i < oldEffects.size(); i++) {
            CompoundTag oldEffect = oldEffects.getCompound(i);
            CompoundTag effect = oldEffect.copy();
            String id = potionEffectId(oldEffect);
            if (!id.isBlank()) {
                effect.putString("id", id);
            }
            if (oldEffect.contains("Amplifier")) {
                effect.putInt("amplifier", oldEffect.getByte("Amplifier") & 255);
            }
            if (oldEffect.contains("Duration")) {
                effect.putInt("duration", oldEffect.getInt("Duration"));
            }
            if (oldEffect.contains("Ambient")) {
                effect.putBoolean("ambient", oldEffect.getBoolean("Ambient"));
            }
            if (oldEffect.contains("ShowParticles")) {
                effect.putBoolean("show_particles", oldEffect.getBoolean("ShowParticles"));
            }
            if (oldEffect.contains("ShowIcon")) {
                effect.putBoolean("show_icon", oldEffect.getBoolean("ShowIcon"));
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
        if (effect.contains("Id", Tag.TAG_STRING)) {
            return effect.getString("Id");
        }
        if (effect.contains("Id", Tag.TAG_ANY_NUMERIC)) {
            MobEffect mobEffect = BuiltInRegistries.MOB_EFFECT.byId(effect.getInt("Id"));
            ResourceLocation id = mobEffect == null ? null : BuiltInRegistries.MOB_EFFECT.getKey(mobEffect);
            return id == null ? "" : id.toString();
        }
        return effect.contains("id", Tag.TAG_STRING) ? effect.getString("id") : "";
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
            String filtered = filteredPages != null && filteredPages.contains(Integer.toString(i), Tag.TAG_STRING)
                    ? filteredPages.getString(Integer.toString(i))
                    : null;
            pages.add(filterableText(strings.getString(i), filtered));
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
            return stringTag.getAsString();
        }
        if (tag instanceof CompoundTag compoundTag) {
            if (compoundTag.contains("raw", Tag.TAG_STRING)) {
                return compoundTag.getString("raw");
            }
            if (compoundTag.contains("filtered", Tag.TAG_STRING)) {
                return compoundTag.getString("filtered");
            }
        }
        return "";
    }

    private static ListTag profilePropertiesToComponent(CompoundTag properties) {
        ListTag converted = new ListTag();
        for (String name : properties.getAllKeys()) {
            ListTag values = properties.getList(name, Tag.TAG_COMPOUND);
            for (int i = 0; i < values.size(); i++) {
                CompoundTag value = values.getCompound(i);
                CompoundTag property = new CompoundTag();
                property.putString("name", name);
                property.putString("value", value.getString("Value"));
                if (value.contains("Signature", Tag.TAG_STRING)) {
                    property.putString("signature", value.getString("Signature"));
                }
                converted.add(property);
            }
        }
        return converted;
    }

    private static CompoundTag profilePropertiesFromComponent(ListTag properties) {
        CompoundTag converted = new CompoundTag();
        for (int i = 0; i < properties.size(); i++) {
            CompoundTag property = properties.getCompound(i);
            String name = property.getString("name");
            ListTag values = converted.contains(name, Tag.TAG_LIST) ? converted.getList(name, Tag.TAG_COMPOUND) : new ListTag();
            CompoundTag value = new CompoundTag();
            value.putString("Value", property.getString("value"));
            if (property.contains("signature", Tag.TAG_STRING)) {
                value.putString("Signature", property.getString("signature"));
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
            values[i] = list.getInt(i);
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
        int value = tag.getInt(key);
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
        if (!tag.contains("id", Tag.TAG_STRING) && inferredId != null && !inferredId.isBlank()) {
            tag.putString("id", inferredId);
        }
    }

    private static boolean isOnlyId(CompoundTag tag) {
        return tag.size() == 1 && tag.contains("id", Tag.TAG_STRING);
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

    private static final class SyncedCompoundTag extends CompoundTag {
        private final ListeningMap map;
        private Runnable onChanged = () -> {
        };
        private boolean muted;

        private SyncedCompoundTag(CompoundTag source, Runnable onChanged) {
            this(new ListeningMap());
            this.onChanged = onChanged;
            this.muted = true;
            for (String key : source.getAllKeys()) {
                Tag value = source.get(key);
                if (value != null) {
                    this.map.put(key, value.copy());
                }
            }
            this.muted = false;
        }

        private SyncedCompoundTag(ListeningMap map) {
            super(map);
            this.map = map;
            this.map.owner = this;
        }

        @Override
        public CompoundTag getCompound(String key) {
            CompoundTag source = contains(key, Tag.TAG_COMPOUND) ? super.getCompound(key) : new CompoundTag();
            final SyncedCompoundTag[] child = new SyncedCompoundTag[1];
            child[0] = new SyncedCompoundTag(source, () -> {
                if (child[0].isEmpty()) {
                    remove(key);
                } else {
                    put(key, child[0]);
                }
            });
            return child[0];
        }

        @Override
        public ListTag getList(String key, int type) {
            ListTag source = contains(key, Tag.TAG_LIST) ? super.getList(key, type) : new ListTag();
            final SyncedListTag[] child = new SyncedListTag[1];
            child[0] = new SyncedListTag(source, () -> {
                if (child[0].isEmpty()) {
                    remove(key);
                } else {
                    put(key, child[0]);
                }
            });
            return child[0];
        }

        private void changed() {
            if (!this.muted) {
                this.onChanged.run();
            }
        }
    }

    private static final class SyncedListTag extends ListTag {
        private Runnable onChanged;
        private boolean muted;

        private SyncedListTag(ListTag source, Runnable onChanged) {
            this.onChanged = onChanged;
            this.muted = true;
            for (int i = 0; i < source.size(); i++) {
                super.add(i, source.get(i).copy());
            }
            this.muted = false;
        }

        @Override
        public CompoundTag getCompound(int index) {
            CompoundTag source = index >= 0 && index < size() && get(index) instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
            final SyncedCompoundTag[] child = new SyncedCompoundTag[1];
            child[0] = new SyncedCompoundTag(source, () -> {
                if (index >= 0 && index < size()) {
                    set(index, child[0]);
                }
            });
            return child[0];
        }

        @Override
        public ListTag getList(int index) {
            ListTag source = index >= 0 && index < size() && get(index) instanceof ListTag listTag ? listTag : new ListTag();
            final SyncedListTag[] child = new SyncedListTag[1];
            child[0] = new SyncedListTag(source, () -> {
                if (index >= 0 && index < size()) {
                    set(index, child[0]);
                }
            });
            return child[0];
        }

        @Override
        public Tag set(int index, Tag tag) {
            Tag old = super.set(index, tag);
            changed();
            return old;
        }

        @Override
        public void add(int index, Tag tag) {
            super.add(index, tag);
            changed();
        }

        @Override
        public Tag remove(int index) {
            Tag old = super.remove(index);
            changed();
            return old;
        }

        @Override
        public boolean setTag(int index, Tag tag) {
            boolean changed = super.setTag(index, tag);
            if (changed) {
                changed();
            }
            return changed;
        }

        @Override
        public boolean addTag(int index, Tag tag) {
            boolean changed = super.addTag(index, tag);
            if (changed) {
                changed();
            }
            return changed;
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

    private static final class ListeningMap extends HashMap<String, Tag> {
        private SyncedCompoundTag owner;

        @Override
        public Tag put(String key, Tag value) {
            Tag old = super.put(key, value);
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
            if (this.owner != null) {
                this.owner.changed();
            }
        }
    }
}

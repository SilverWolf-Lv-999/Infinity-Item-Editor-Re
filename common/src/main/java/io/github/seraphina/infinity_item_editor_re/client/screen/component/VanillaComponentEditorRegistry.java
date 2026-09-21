package io.github.seraphina.infinity_item_editor_re.client.screen.component;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class VanillaComponentEditorRegistry {
    private static final List<ComponentEditorDefinition> DEFINITIONS = createDefinitions();

    private VanillaComponentEditorRegistry() {
    }

    public static List<ComponentEditorDefinition> definitions() {
        return DEFINITIONS;
    }

    public static ComponentEditorDefinition find(String id) {
        for (ComponentEditorDefinition definition : DEFINITIONS) {
            if (definition.id().equals(id)) {
                return definition;
            }
        }
        return null;
    }

    private static List<ComponentEditorDefinition> createDefinitions() {
        List<ComponentEditorDefinition> definitions = new ArrayList<>();

        // Display and tooltip.
        add(definitions, componentText("minecraft:custom_name", "display", ""));
        add(definitions, componentText("minecraft:item_name", "display", ""));
        add(definitions, string("minecraft:item_model", "display", "", "minecraft:stick"));
        add(definitions, rootTextList("minecraft:lore", "display", ""));
        add(definitions, string("minecraft:rarity", "display", "", "common"));
        add(definitions, compound("minecraft:tooltip_display", "display", "",
                bool("hide_tooltip", "", "false"),
                stringList("hidden_components", "", "")));
        add(definitions, string("minecraft:tooltip_style", "display", "", "minecraft:default"));
        add(definitions, bool("minecraft:enchantment_glint_override", "display", "", "true"));
        add(definitions, integer("minecraft:dyed_color", "display", "", "16777215"));
        add(definitions, compound("minecraft:custom_model_data", "display", "",
                decimalList("floats", "", "1.0"),
                booleanList("flags", "", "false"),
                stringList("strings", "", ""),
                integerList("colors", "", "")));

        // Stack and durability.
        add(definitions, integer("minecraft:max_stack_size", "stack_durability", "", "64"));
        add(definitions, integer("minecraft:max_damage", "stack_durability", "", "100"));
        add(definitions, integer("minecraft:damage", "stack_durability", "", "0"));
        add(definitions, marker("minecraft:unbreakable", "stack_durability", ""));
        add(definitions, integer("minecraft:repair_cost", "stack_durability", "", "0"));
        add(definitions, compound("minecraft:repairable", "stack_durability", "",
                identifier("items", "", "minecraft:iron_ingot")));
        add(definitions, compound("minecraft:damage_resistant", "stack_durability", "",
                identifier("types", "", "#minecraft:is_fire")));
        add(definitions, string("minecraft:break_sound", "stack_durability", "", "minecraft:block.stone.break"));

        // Enchantments and combat.
        add(definitions, compound("minecraft:enchantments", "enchantments_combat", "",
                integerMap("levels", "", "minecraft:sharpness:1"),
                bool("show_in_tooltip", "", "true")));
        add(definitions, compound("minecraft:stored_enchantments", "enchantments_combat", "",
                integerMap("levels", "", "minecraft:sharpness:1"),
                bool("show_in_tooltip", "", "true")));
        add(definitions, compound("minecraft:enchantable", "enchantments_combat", "",
                integer("value", "", "10")));
        add(definitions, compound("minecraft:attribute_modifiers", "enchantments_combat", "",
                attributeList("modifiers", "", "minecraft:generic.attack_damage|1|add_value|mainhand"),
                bool("show_in_tooltip", "", "true")));
        add(definitions, compound("minecraft:weapon", "enchantments_combat", "",
                integer("item_damage_per_attack", "", "1"),
                decimal("disable_blocking_for_seconds", "", "0")));
        add(definitions, compound("minecraft:blocks_attacks", "enchantments_combat", "",
                decimal("block_delay_seconds", "", "0"),
                decimal("disable_cooldown_scale", "", "1"),
                snbtList("damage_reductions", "", "[{horizontal_blocking_angle:90.0f,base:0.0f,factor:1.0f}]"),
                snbtCompound("item_damage", "", "{threshold:0.0f,base:1.0f,factor:0.0f}"),
                optionalIdentifier("bypassed_by", "", ""),
                optionalIdentifier("block_sound", "", ""),
                optionalIdentifier("disabled_sound", "", "")));
        add(definitions, compound("minecraft:death_protection", "enchantments_combat", "",
                snbtList("death_effects", "", "[]")));

        // Tools and blocks.
        add(definitions, compound("minecraft:tool", "tools_blocks", "",
                toolRuleList("rules", "", "#minecraft:mineable/pickaxe|6|true"),
                decimal("default_mining_speed", "", "1"),
                integer("damage_per_block", "", "1"),
                bool("can_destroy_blocks_in_creative", "", "true")));
        add(definitions, compound("minecraft:can_place_on", "tools_blocks", "",
                blockPredicateList("predicates", "", "minecraft:stone"),
                bool("show_in_tooltip", "", "true")));
        add(definitions, compound("minecraft:can_break", "tools_blocks", "",
                blockPredicateList("predicates", "", "minecraft:stone"),
                bool("show_in_tooltip", "", "true")));
        add(definitions, stringMap("minecraft:block_state", "tools_blocks", "", "facing=north"));
        add(definitions, compound("minecraft:block_entity_data", "tools_blocks", "",
                identifier("id", "", ""),
                textComponent("CustomName", "", ""),
                stringField("Lock", "", ""),
                identifier("LootTable", "", ""),
                integer("LootTableSeed", "", "0")));
        add(definitions, string("minecraft:lock", "tools_blocks", "", ""));
        add(definitions, marker("minecraft:creative_slot_lock", "tools_blocks", ""));
        add(definitions, stringMap("minecraft:debug_stick_state", "tools_blocks", "", "facing=north"));

        // Food and use.
        add(definitions, compound("minecraft:food", "food_use", "",
                integer("nutrition", "", "4"),
                decimal("saturation", "", "1.2"),
                bool("can_always_eat", "", "false")));
        add(definitions, compound("minecraft:consumable", "food_use", "",
                decimal("consume_seconds", "", "1.6"),
                enumeration("animation", "", "eat", "eat", "drink", "block", "bow", "crossbow", "spyglass", "toot_horn", "brush"),
                identifier("sound", "", "minecraft:entity.generic.eat"),
                bool("has_consume_particles", "", "true"),
                snbtList("on_consume_effects", "", "[]")));
        add(definitions, item("minecraft:use_remainder", "food_use", "", "minecraft:bowl*1"));
        add(definitions, compound("minecraft:use_cooldown", "food_use", "",
                decimal("seconds", "", "1"),
                identifier("cooldown_group", "", "")));

        // Equipment.
        add(definitions, compound("minecraft:equippable", "equipment", "",
                enumeration("slot", "", "head", "mainhand", "offhand", "feet", "legs", "chest", "head", "body"),
                identifier("equip_sound", "", "minecraft:item.armor.equip_generic"),
                optionalIdentifier("asset_id", "", ""),
                optionalIdentifier("camera_overlay", "", ""),
                optionalSnbtTag("allowed_entities", "", ""),
                bool("dispensable", "", "true"),
                bool("swappable", "", "true"),
                bool("damage_on_hurt", "", "true"),
                bool("equip_on_interact", "", "false"),
                bool("can_be_sheared", "", "false"),
                identifier("shearing_sound", "", "minecraft:item.shears.snip")));
        add(definitions, marker("minecraft:glider", "equipment", ""));
        add(definitions, compound("minecraft:trim", "equipment", "",
                identifier("material", "", "minecraft:iron"),
                identifier("pattern", "", "minecraft:sentry"),
                bool("show_in_tooltip", "", "true")));

        // Containers.
        add(definitions, itemList("minecraft:container", "containers", "", "minecraft:stone*1"));
        add(definitions, itemList("minecraft:bundle_contents", "containers", "", "minecraft:stone*1"));
        add(definitions, compound("minecraft:container_loot", "containers", "",
                identifier("loot_table", "", "minecraft:chests/simple_dungeon"),
                longInteger("seed", "", "0")));
        add(definitions, itemList("minecraft:charged_projectiles", "containers", "", "minecraft:arrow*1"));

        // Potions and effects.
        add(definitions, compound("minecraft:potion_contents", "potions_effects", "",
                identifier("potion", "", "minecraft:water"),
                integer("custom_color", "", "0"),
                effectList("custom_effects", "", "")));
        add(definitions, decimal("minecraft:potion_duration_scale", "potions_effects", "", "1"));
        add(definitions, effectListRoot("minecraft:suspicious_stew_effects", "potions_effects", "", "minecraft:saturation|100|0|1"));
        add(definitions, integer("minecraft:ominous_bottle_amplifier", "potions_effects", "", "1"));

        // Books, maps and music.
        add(definitions, compound("minecraft:writable_book_content", "books_maps_music", "",
                textList("pages", "", "")));
        add(definitions, compound("minecraft:written_book_content", "books_maps_music", "",
                text("title", "", "Title"),
                text("author", "", "Player"),
                integer("generation", "", "0"),
                textList("pages", "", ""),
                bool("resolved", "", "false")));
        add(definitions, integer("minecraft:map_color", "books_maps_music", "", "16777215"));
        add(definitions, integer("minecraft:map_id", "books_maps_music", "", "0"));
        add(definitions, rootSnbtCompound("minecraft:map_decorations", "books_maps_music", "", "{}"));
        add(definitions, string("minecraft:map_post_processing", "books_maps_music", "", "lock"));
        add(definitions, stringListRoot("minecraft:recipes", "books_maps_music", "", "minecraft:crafting_table"));
        add(definitions, compound("minecraft:lodestone_tracker", "books_maps_music", "",
                bool("tracked", "", "true"),
                optionalSnbtCompound("target", "", "")));
        add(definitions, compound("minecraft:jukebox_playable", "books_maps_music", "",
                identifier("song", "", "minecraft:music_disc.13"),
                bool("show_in_tooltip", "", "true")));
        add(definitions, string("minecraft:note_block_sound", "books_maps_music", "", "minecraft:block.note_block.harp"));
        add(definitions, string("minecraft:instrument", "books_maps_music", "", "minecraft:ponder_goat_horn"));

        // Blocks and decoration.
        add(definitions, compound("minecraft:firework_explosion", "blocks_decor", "",
                enumeration("shape", "", "small_ball", "small_ball", "large_ball", "star", "creeper", "burst"),
                integerList("colors", "", "16711680"),
                integerList("fade_colors", "", ""),
                bool("has_trail", "", "false"),
                bool("has_twinkle", "", "false")));
        add(definitions, compound("minecraft:fireworks", "blocks_decor", "",
                integer("flight_duration", "", "1"),
                fireworkList("explosions", "", "")));
        add(definitions, patternListRoot("minecraft:banner_patterns", "blocks_decor", "", "minecraft:flower:white"));
        add(definitions, string("minecraft:base_color", "blocks_decor", "", "white"));
        add(definitions, stringListRoot("minecraft:pot_decorations", "blocks_decor", "",
                "minecraft:brick,minecraft:brick,minecraft:brick,minecraft:brick"));
        add(definitions, string("minecraft:provides_banner_patterns", "blocks_decor", "", "minecraft:pattern_item/flower"));
        add(definitions, compound("minecraft:provides_trim_material", "blocks_decor", "",
                text("asset_name", "", "iron"),
                identifier("ingredient", "", "minecraft:iron_ingot"),
                decimal("item_model_index", "", "0"),
                textComponent("description", "", "")));
        add(definitions, beeListRoot("minecraft:bees", "blocks_decor", "", "minecraft:bee|0|600"));

        // Entity data and variants.
        add(definitions, entityData("minecraft:entity_data", ""));
        add(definitions, entityData("minecraft:bucket_entity_data", ""));
        add(definitions, compound("minecraft:profile", "entities", "",
                optionalText("name", "", ""),
                uuid("id", "UUID", ""),
                optionalSnbtCompound("properties", "", ""),
                optionalSnbtCompound("skin_patch", "", "")));
        addVariantDefinitions(definitions);

        // Advanced.
        add(definitions, rootSnbtCompound("minecraft:custom_data", "advanced", "", "{}"));
        add(definitions, marker("minecraft:intangible_projectile", "advanced", ""));

        return List.copyOf(definitions);
    }

    private static void addVariantDefinitions(List<ComponentEditorDefinition> definitions) {
        String[] variants = {
                "minecraft:villager/variant", "minecraft:wolf/variant", "minecraft:wolf/sound_variant",
                "minecraft:wolf/collar", "minecraft:fox/variant", "minecraft:salmon/size",
                "minecraft:parrot/variant", "minecraft:tropical_fish/pattern", "minecraft:tropical_fish/base_color",
                "minecraft:tropical_fish/pattern_color", "minecraft:mooshroom/variant", "minecraft:rabbit/variant",
                "minecraft:pig/variant", "minecraft:cow/variant", "minecraft:chicken/variant",
                "minecraft:frog/variant", "minecraft:horse/variant", "minecraft:painting/variant",
                "minecraft:llama/variant", "minecraft:axolotl/variant", "minecraft:cat/variant",
                "minecraft:cat/collar", "minecraft:sheep/color", "minecraft:shulker/color"
        };
        String[] defaults = {
                "minecraft:plains", "minecraft:ashen", "minecraft:classic", "red", "red", "medium",
                "red_blue", "kob", "white", "orange", "red", "brown", "minecraft:temperate",
                "minecraft:temperate", "minecraft:temperate", "minecraft:temperate", "white", "minecraft:kebab",
                "creamy", "lucy", "minecraft:tabby", "red", "white", "purple"
        };
        for (int index = 0; index < variants.length; index++) {
            add(definitions, string(variants[index], "entities", "", defaults[index]));
        }
    }

    private static ComponentEditorDefinition entityData(String id, String title) {
        return compound(id, "entities", title,
                identifier("id", "", "minecraft:pig"),
                textComponent("CustomName", "", ""),
                bool("Silent", "", "false"),
                bool("NoAI", "", "false"),
                bool("NoGravity", "", "false"),
                bool("Invulnerable", "", "false"),
                bool("Glowing", "", "false"));
    }

    private static void add(List<ComponentEditorDefinition> definitions, ComponentEditorDefinition definition) {
        definitions.add(definition);
    }

    private static ComponentEditorDefinition marker(String id, String category, String title) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.MARKER, List.of());
    }

    private static ComponentEditorDefinition componentText(String id, String category, String title) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.ROOT_COMPONENT_TEXT,
                List.of(text("value", "", "")));
    }

    private static ComponentEditorDefinition string(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.ROOT_STRING,
                List.of(text("value", "", defaultValue)));
    }

    private static ComponentEditorDefinition integer(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.ROOT_INTEGER,
                List.of(integer("value", "", defaultValue)));
    }

    private static ComponentEditorDefinition decimal(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.ROOT_DECIMAL,
                List.of(decimal("value", "", defaultValue)));
    }

    private static ComponentEditorDefinition bool(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.ROOT_BOOLEAN,
                List.of(bool("value", "", defaultValue)));
    }

    private static ComponentEditorDefinition item(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.ROOT_ITEM,
                List.of(item("value", "", defaultValue)));
    }

    private static ComponentEditorDefinition itemList(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.ROOT_ITEM_LIST,
                List.of(itemList("entries", "", defaultValue)));
    }

    private static ComponentEditorDefinition stringListRoot(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.ROOT_STRING_LIST,
                List.of(stringList("entries", "", defaultValue)));
    }

    private static ComponentEditorDefinition rootTextList(String id, String category, String title) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.ROOT_TEXT_LIST,
                List.of(textList("entries", "", "")));
    }

    private static ComponentEditorDefinition effectListRoot(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.ROOT_EFFECT_LIST,
                List.of(effectList("entries", "", defaultValue)));
    }

    private static ComponentEditorDefinition patternListRoot(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.ROOT_PATTERN_LIST,
                List.of(patternList("entries", "", defaultValue)));
    }

    private static ComponentEditorDefinition beeListRoot(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.ROOT_BEE_LIST,
                List.of(beeList("entries", "", defaultValue)));
    }

    private static ComponentEditorDefinition stringMap(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.ROOT_STRING_MAP,
                List.of(stringMap("entries", "", defaultValue)));
    }

    private static ComponentEditorDefinition rootSnbtCompound(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.ROOT_SNBT_COMPOUND,
                List.of(snbtCompound("value", "SNBT", defaultValue)));
    }

    private static ComponentEditorDefinition compound(String id, String category, String title, ComponentEditorField... fields) {
        return new ComponentEditorDefinition(id, category, titleKey(id), ComponentValueShape.COMPOUND, List.of(fields));
    }

    private static ComponentEditorField text(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.TEXT, defaultValue);
    }

    private static ComponentEditorField textComponent(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.TEXT_COMPONENT, defaultValue);
    }

    private static ComponentEditorField stringField(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.TEXT, defaultValue);
    }

    private static ComponentEditorField identifier(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.IDENTIFIER, defaultValue);
    }

    private static ComponentEditorField integer(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.INTEGER, defaultValue);
    }

    private static ComponentEditorField decimal(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.DECIMAL, defaultValue);
    }

    private static ComponentEditorField bool(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.BOOLEAN, defaultValue);
    }

    private static ComponentEditorField enumeration(String key, String label, String defaultValue, String... values) {
        return new ComponentEditorField(key, fieldLabelKey(key), ComponentFieldKind.ENUM, defaultValue, List.of(values), false);
    }

    private static ComponentEditorField stringList(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.STRING_LIST, defaultValue);
    }

    private static ComponentEditorField textList(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.TEXT_LIST, defaultValue);
    }

    private static ComponentEditorField integerList(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.INTEGER_LIST, defaultValue);
    }

    private static ComponentEditorField decimalList(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.DECIMAL_LIST, defaultValue);
    }

    private static ComponentEditorField booleanList(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.BOOLEAN_LIST, defaultValue);
    }

    private static ComponentEditorField item(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.ITEM, defaultValue);
    }

    private static ComponentEditorField itemList(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.ITEM_LIST, defaultValue);
    }

    private static ComponentEditorField integerMap(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.INTEGER_MAP, defaultValue);
    }

    private static ComponentEditorField stringMap(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.STRING_MAP, defaultValue);
    }

    private static ComponentEditorField blockPredicateList(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.BLOCK_PREDICATE_LIST, defaultValue);
    }

    private static ComponentEditorField toolRuleList(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.TOOL_RULE_LIST, defaultValue);
    }

    private static ComponentEditorField effectList(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.EFFECT_LIST, defaultValue);
    }

    private static ComponentEditorField attributeList(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.ATTRIBUTE_LIST, defaultValue);
    }

    private static ComponentEditorField fireworkList(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.FIREWORK_LIST, defaultValue);
    }

    private static ComponentEditorField patternList(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.PATTERN_LIST, defaultValue);
    }

    private static ComponentEditorField beeList(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.BEE_LIST, defaultValue);
    }

    private static ComponentEditorField uuid(String key, String label, String defaultValue) {
        return new ComponentEditorField(key, fieldLabelKey(key), ComponentFieldKind.UUID, defaultValue, List.of(), true);
    }

    private static ComponentEditorField longInteger(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.LONG, defaultValue);
    }

    private static ComponentEditorField snbtTag(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.SNBT_TAG, defaultValue);
    }

    private static ComponentEditorField snbtCompound(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.SNBT_COMPOUND, defaultValue);
    }

    private static ComponentEditorField snbtList(String key, String label, String defaultValue) {
        return field(key, label, ComponentFieldKind.SNBT_LIST, defaultValue);
    }

    private static ComponentEditorField optionalText(String key, String label, String defaultValue) {
        return optionalField(key, label, ComponentFieldKind.TEXT, defaultValue);
    }

    private static ComponentEditorField optionalIdentifier(String key, String label, String defaultValue) {
        return optionalField(key, label, ComponentFieldKind.IDENTIFIER, defaultValue);
    }

    private static ComponentEditorField optionalSnbtTag(String key, String label, String defaultValue) {
        return optionalField(key, label, ComponentFieldKind.SNBT_TAG, defaultValue);
    }

    private static ComponentEditorField optionalSnbtCompound(String key, String label, String defaultValue) {
        return optionalField(key, label, ComponentFieldKind.SNBT_COMPOUND, defaultValue);
    }

    private static ComponentEditorField optionalSnbtList(String key, String label, String defaultValue) {
        return optionalField(key, label, ComponentFieldKind.SNBT_LIST, defaultValue);
    }

    private static ComponentEditorField field(String key, String label, ComponentFieldKind kind, String defaultValue) {
        return new ComponentEditorField(key, fieldLabelKey(key), kind, defaultValue, List.of(), false);
    }

    private static ComponentEditorField optionalField(String key, String label, ComponentFieldKind kind, String defaultValue) {
        return new ComponentEditorField(key, fieldLabelKey(key), kind, defaultValue, List.of(), true);
    }

    private static String titleKey(String id) {
        return "screen." + ModSource.MODID + ".component." + id.replace(':', '.').replace('/', '.');
    }

    private static String fieldLabelKey(String key) {
        return "screen." + ModSource.MODID + ".component.field." + key.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    }
}

record ComponentEditorDefinition(String id, String category, String title, ComponentValueShape shape,
                                 List<ComponentEditorField> fields) {
    @Override
    public String title() {
        return Component.translatable(this.title).getString();
    }

    Component displayName() {
        return Component.translatable(this.title);
    }
}

record ComponentEditorField(String key, String label, ComponentFieldKind kind, String defaultValue, List<String> options,
                            boolean optional) {
}

enum ComponentValueShape {
    MARKER,
    ROOT_COMPONENT_TEXT,
    ROOT_STRING,
    ROOT_INTEGER,
    ROOT_DECIMAL,
    ROOT_BOOLEAN,
    ROOT_ITEM,
    ROOT_ITEM_LIST,
    ROOT_STRING_LIST,
    ROOT_TEXT_LIST,
    ROOT_EFFECT_LIST,
    ROOT_PATTERN_LIST,
    ROOT_BEE_LIST,
    ROOT_STRING_MAP,
    ROOT_SNBT_COMPOUND,
    COMPOUND
}

enum ComponentFieldKind {
    TEXT,
    TEXT_COMPONENT,
    IDENTIFIER,
    INTEGER,
    LONG,
    DECIMAL,
    BOOLEAN,
    ENUM,
    STRING_LIST,
    TEXT_LIST,
    INTEGER_LIST,
    DECIMAL_LIST,
    BOOLEAN_LIST,
    ITEM,
    ITEM_LIST,
    INTEGER_MAP,
    STRING_MAP,
    BLOCK_PREDICATE_LIST,
    TOOL_RULE_LIST,
    EFFECT_LIST,
    ATTRIBUTE_LIST,
    FIREWORK_LIST,
    PATTERN_LIST,
    BEE_LIST,
    UUID,
    SNBT_TAG,
    SNBT_COMPOUND,
    SNBT_LIST
}

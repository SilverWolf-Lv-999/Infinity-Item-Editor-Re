package io.github.seraphina.infinity_item_editor_re.client.screen.component;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

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
        add(definitions, componentText("minecraft:custom_name", "display", "自定义名称"));
        add(definitions, componentText("minecraft:item_name", "display", "物品名称"));
        add(definitions, string("minecraft:item_model", "display", "物品模型", "minecraft:stick"));
        add(definitions, rootTextList("minecraft:lore", "display", "描述文本"));
        add(definitions, string("minecraft:rarity", "display", "稀有度", "common"));
        add(definitions, compound("minecraft:tooltip_display", "display", "提示框显示",
                bool("hide_tooltip", "隐藏全部提示", "false"),
                stringList("hidden_components", "隐藏的组件", "")));
        add(definitions, string("minecraft:tooltip_style", "display", "提示框样式", "minecraft:default"));
        add(definitions, bool("minecraft:enchantment_glint_override", "display", "附魔光效", "true"));
        add(definitions, integer("minecraft:dyed_color", "display", "染色", "16777215"));
        add(definitions, compound("minecraft:custom_model_data", "display", "自定义模型数据",
                decimalList("floats", "浮点模型数据", "1.0"),
                booleanList("flags", "启用标记", "false"),
                stringList("strings", "文本模型数据", ""),
                integerList("colors", "颜色模型数据", "")));

        // Stack and durability.
        add(definitions, integer("minecraft:max_stack_size", "stack_durability", "最大堆叠数", "64"));
        add(definitions, integer("minecraft:max_damage", "stack_durability", "最大耐久", "100"));
        add(definitions, integer("minecraft:damage", "stack_durability", "已损耐久", "0"));
        add(definitions, marker("minecraft:unbreakable", "stack_durability", "不可破坏"));
        add(definitions, integer("minecraft:repair_cost", "stack_durability", "铁砧惩罚", "0"));
        add(definitions, compound("minecraft:repairable", "stack_durability", "可修复材料",
                identifier("items", "材料物品或标签", "minecraft:iron_ingot")));
        add(definitions, compound("minecraft:damage_resistant", "stack_durability", "伤害抗性",
                identifier("types", "伤害类型或标签", "#minecraft:is_fire")));
        add(definitions, string("minecraft:break_sound", "stack_durability", "损坏声音", "minecraft:block.stone.break"));

        // Enchantments and combat.
        add(definitions, compound("minecraft:enchantments", "enchantments_combat", "附魔",
                integerMap("levels", "附魔与等级", "minecraft:sharpness:1"),
                bool("show_in_tooltip", "在提示框显示", "true")));
        add(definitions, compound("minecraft:stored_enchantments", "enchantments_combat", "储存附魔",
                integerMap("levels", "附魔与等级", "minecraft:sharpness:1"),
                bool("show_in_tooltip", "在提示框显示", "true")));
        add(definitions, compound("minecraft:enchantable", "enchantments_combat", "可附魔性",
                integer("value", "附魔等级", "10")));
        add(definitions, compound("minecraft:attribute_modifiers", "enchantments_combat", "属性修饰符",
                attributeList("modifiers", "属性|数值|操作|槽位", "minecraft:generic.attack_damage|1|add_value|mainhand"),
                bool("show_in_tooltip", "在提示框显示", "true")));
        add(definitions, compound("minecraft:weapon", "enchantments_combat", "武器",
                integer("item_damage_per_attack", "每次攻击耐久消耗", "1"),
                decimal("disable_blocking_for_seconds", "破盾秒数", "0")));
        add(definitions, compound("minecraft:blocks_attacks", "enchantments_combat", "格挡攻击",
                decimal("block_delay_seconds", "格挡延迟秒数", "0"),
                decimal("disable_cooldown_scale", "禁用冷却倍率", "1"),
                snbtList("damage_reductions", "减伤规则 SNBT", "[{horizontal_blocking_angle:90.0f,base:0.0f,factor:1.0f}]"),
                snbtCompound("item_damage", "物品损耗 SNBT", "{threshold:0.0f,base:1.0f,factor:0.0f}"),
                optionalIdentifier("bypassed_by", "绕过格挡的伤害标签", ""),
                optionalIdentifier("block_sound", "格挡声音", ""),
                optionalIdentifier("disabled_sound", "格挡禁用声音", "")));
        add(definitions, compound("minecraft:death_protection", "enchantments_combat", "死亡保护",
                snbtList("death_effects", "死亡效果 SNBT", "[]")));

        // Tools and blocks.
        add(definitions, compound("minecraft:tool", "tools_blocks", "工具规则",
                toolRuleList("rules", "方块|速度|正确掉落", "#minecraft:mineable/pickaxe|6|true"),
                decimal("default_mining_speed", "默认挖掘速度", "1"),
                integer("damage_per_block", "每个方块耐久消耗", "1"),
                bool("can_destroy_blocks_in_creative", "创造模式可破坏方块", "true")));
        add(definitions, compound("minecraft:can_place_on", "tools_blocks", "可放置方块",
                blockPredicateList("predicates", "方块或方块标签", "minecraft:stone"),
                bool("show_in_tooltip", "在提示框显示", "true")));
        add(definitions, compound("minecraft:can_break", "tools_blocks", "可破坏方块",
                blockPredicateList("predicates", "方块或方块标签", "minecraft:stone"),
                bool("show_in_tooltip", "在提示框显示", "true")));
        add(definitions, stringMap("minecraft:block_state", "tools_blocks", "方块状态", "facing=north"));
        add(definitions, compound("minecraft:block_entity_data", "tools_blocks", "方块实体数据",
                identifier("id", "方块实体 ID", ""),
                textComponent("CustomName", "自定义名称", ""),
                stringField("Lock", "锁定密码", ""),
                identifier("LootTable", "战利品表", ""),
                integer("LootTableSeed", "战利品种子", "0")));
        add(definitions, string("minecraft:lock", "tools_blocks", "锁定密码", ""));
        add(definitions, marker("minecraft:creative_slot_lock", "tools_blocks", "创造物品栏锁定"));
        add(definitions, stringMap("minecraft:debug_stick_state", "tools_blocks", "调试棒状态", "facing=north"));

        // Food and use.
        add(definitions, compound("minecraft:food", "food_use", "食物",
                integer("nutrition", "饱食度", "4"),
                decimal("saturation", "饱和度", "1.2"),
                bool("can_always_eat", "满饱食度可食用", "false")));
        add(definitions, compound("minecraft:consumable", "food_use", "使用行为",
                decimal("consume_seconds", "使用秒数", "1.6"),
                enumeration("animation", "动画", "eat", "eat", "drink", "block", "bow", "crossbow", "spyglass", "toot_horn", "brush"),
                identifier("sound", "使用声音", "minecraft:entity.generic.eat"),
                bool("has_consume_particles", "显示使用粒子", "true"),
                snbtList("on_consume_effects", "使用后效果 SNBT", "[]")));
        add(definitions, item("minecraft:use_remainder", "food_use", "使用后的物品", "minecraft:bowl*1"));
        add(definitions, compound("minecraft:use_cooldown", "food_use", "使用冷却",
                decimal("seconds", "冷却秒数", "1"),
                identifier("cooldown_group", "冷却组", "")));

        // Equipment.
        add(definitions, compound("minecraft:equippable", "equipment", "可装备",
                enumeration("slot", "装备槽位", "head", "mainhand", "offhand", "feet", "legs", "chest", "head", "body"),
                identifier("equip_sound", "装备声音", "minecraft:item.armor.equip_generic"),
                optionalIdentifier("asset_id", "外观资源", ""),
                optionalIdentifier("camera_overlay", "相机覆盖层", ""),
                optionalSnbtTag("allowed_entities", "允许实体 SNBT", ""),
                bool("dispensable", "可由发射器装备", "true"),
                bool("swappable", "可交换", "true"),
                bool("damage_on_hurt", "受伤时损耗", "true"),
                bool("equip_on_interact", "交互时装备", "false"),
                bool("can_be_sheared", "可被剪下", "false"),
                identifier("shearing_sound", "剪下声音", "minecraft:item.shears.snip")));
        add(definitions, marker("minecraft:glider", "equipment", "滑翔"));
        add(definitions, compound("minecraft:trim", "equipment", "盔甲纹饰",
                identifier("material", "材料", "minecraft:iron"),
                identifier("pattern", "纹饰", "minecraft:sentry"),
                bool("show_in_tooltip", "在提示框显示", "true")));

        // Containers.
        add(definitions, itemList("minecraft:container", "containers", "容器物品", "minecraft:stone*1"));
        add(definitions, itemList("minecraft:bundle_contents", "containers", "收纳袋内容", "minecraft:stone*1"));
        add(definitions, compound("minecraft:container_loot", "containers", "容器战利品",
                identifier("loot_table", "战利品表", "minecraft:chests/simple_dungeon"),
                longInteger("seed", "种子", "0")));
        add(definitions, itemList("minecraft:charged_projectiles", "containers", "已装填投射物", "minecraft:arrow*1"));

        // Potions and effects.
        add(definitions, compound("minecraft:potion_contents", "potions_effects", "药水内容",
                identifier("potion", "药水", "minecraft:water"),
                integer("custom_color", "自定义颜色", "0"),
                effectList("custom_effects", "效果|持续 tick|等级|概率", "")));
        add(definitions, decimal("minecraft:potion_duration_scale", "potions_effects", "药水时长倍率", "1"));
        add(definitions, effectListRoot("minecraft:suspicious_stew_effects", "potions_effects", "谜之炖菜效果", "minecraft:saturation|100|0|1"));
        add(definitions, integer("minecraft:ominous_bottle_amplifier", "potions_effects", "不祥之瓶等级", "1"));

        // Books, maps and music.
        add(definitions, compound("minecraft:writable_book_content", "books_maps_music", "可书写书内容",
                textList("pages", "书页", "")));
        add(definitions, compound("minecraft:written_book_content", "books_maps_music", "成书内容",
                text("title", "标题", "Title"),
                text("author", "作者", "Player"),
                integer("generation", "副本代数", "0"),
                textList("pages", "书页", ""),
                bool("resolved", "已解析", "false")));
        add(definitions, integer("minecraft:map_color", "books_maps_music", "地图颜色", "16777215"));
        add(definitions, integer("minecraft:map_id", "books_maps_music", "地图 ID", "0"));
        add(definitions, rootSnbtCompound("minecraft:map_decorations", "books_maps_music", "地图标记", "{}"));
        add(definitions, string("minecraft:map_post_processing", "books_maps_music", "地图后处理", "lock"));
        add(definitions, stringListRoot("minecraft:recipes", "books_maps_music", "配方列表", "minecraft:crafting_table"));
        add(definitions, compound("minecraft:lodestone_tracker", "books_maps_music", "磁石追踪器",
                bool("tracked", "正在追踪", "true"),
                optionalSnbtCompound("target", "目标 SNBT", "")));
        add(definitions, compound("minecraft:jukebox_playable", "books_maps_music", "唱片",
                identifier("song", "音乐", "minecraft:music_disc.13"),
                bool("show_in_tooltip", "在提示框显示", "true")));
        add(definitions, string("minecraft:note_block_sound", "books_maps_music", "音符盒声音", "minecraft:block.note_block.harp"));
        add(definitions, string("minecraft:instrument", "books_maps_music", "乐器", "minecraft:ponder_goat_horn"));

        // Blocks and decoration.
        add(definitions, compound("minecraft:firework_explosion", "blocks_decor", "烟花爆炸",
                enumeration("shape", "形状", "small_ball", "small_ball", "large_ball", "star", "creeper", "burst"),
                integerList("colors", "颜色 RGB", "16711680"),
                integerList("fade_colors", "渐变颜色 RGB", ""),
                bool("has_trail", "拖尾", "false"),
                bool("has_twinkle", "闪烁", "false")));
        add(definitions, compound("minecraft:fireworks", "blocks_decor", "烟花火箭",
                integer("flight_duration", "飞行时长", "1"),
                fireworkList("explosions", "形状|颜色 RGB|渐变 RGB|拖尾|闪烁", "")));
        add(definitions, patternListRoot("minecraft:banner_patterns", "blocks_decor", "旗帜图案", "minecraft:flower:white"));
        add(definitions, string("minecraft:base_color", "blocks_decor", "潜影盒颜色", "white"));
        add(definitions, stringListRoot("minecraft:pot_decorations", "blocks_decor", "饰纹陶罐碎片",
                "minecraft:brick,minecraft:brick,minecraft:brick,minecraft:brick"));
        add(definitions, string("minecraft:provides_banner_patterns", "blocks_decor", "旗帜图案提供者", "minecraft:pattern_item/flower"));
        add(definitions, compound("minecraft:provides_trim_material", "blocks_decor", "纹饰材料提供者",
                text("asset_name", "资源名称", "iron"),
                identifier("ingredient", "材料物品", "minecraft:iron_ingot"),
                decimal("item_model_index", "模型索引", "0"),
                textComponent("description", "描述", "")));
        add(definitions, beeListRoot("minecraft:bees", "blocks_decor", "蜜蜂", "minecraft:bee|0|600"));

        // Entity data and variants.
        add(definitions, entityData("minecraft:entity_data", "实体数据"));
        add(definitions, entityData("minecraft:bucket_entity_data", "桶装实体数据"));
        add(definitions, compound("minecraft:profile", "entities", "头颅档案",
                optionalText("name", "玩家名称", ""),
                uuid("id", "UUID", ""),
                optionalSnbtCompound("properties", "属性映射 SNBT", ""),
                optionalSnbtCompound("skin_patch", "皮肤修补 SNBT", "")));
        addVariantDefinitions(definitions);

        // Advanced.
        add(definitions, rootSnbtCompound("minecraft:custom_data", "advanced", "自定义数据", "{}"));
        add(definitions, marker("minecraft:intangible_projectile", "advanced", "无实体投射物"));

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
            add(definitions, string(variants[index], "entities", "实体变种", defaults[index]));
        }
    }

    private static ComponentEditorDefinition entityData(String id, String title) {
        return compound(id, "entities", title,
                identifier("id", "实体 ID", "minecraft:pig"),
                textComponent("CustomName", "自定义名称", ""),
                bool("Silent", "静音", "false"),
                bool("NoAI", "无 AI", "false"),
                bool("NoGravity", "无重力", "false"),
                bool("Invulnerable", "无敌", "false"),
                bool("Glowing", "发光", "false"));
    }

    private static void add(List<ComponentEditorDefinition> definitions, ComponentEditorDefinition definition) {
        definitions.add(definition);
    }

    private static ComponentEditorDefinition marker(String id, String category, String title) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.MARKER, List.of());
    }

    private static ComponentEditorDefinition componentText(String id, String category, String title) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.ROOT_COMPONENT_TEXT,
                List.of(text("value", "文本", "")));
    }

    private static ComponentEditorDefinition string(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.ROOT_STRING,
                List.of(text("value", "值", defaultValue)));
    }

    private static ComponentEditorDefinition integer(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.ROOT_INTEGER,
                List.of(integer("value", "数值", defaultValue)));
    }

    private static ComponentEditorDefinition decimal(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.ROOT_DECIMAL,
                List.of(decimal("value", "数值", defaultValue)));
    }

    private static ComponentEditorDefinition bool(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.ROOT_BOOLEAN,
                List.of(bool("value", "开关", defaultValue)));
    }

    private static ComponentEditorDefinition item(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.ROOT_ITEM,
                List.of(item("value", "物品 ID*数量", defaultValue)));
    }

    private static ComponentEditorDefinition itemList(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.ROOT_ITEM_LIST,
                List.of(itemList("entries", "物品 ID*数量，逗号分隔", defaultValue)));
    }

    private static ComponentEditorDefinition stringListRoot(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.ROOT_STRING_LIST,
                List.of(stringList("entries", "条目，逗号分隔", defaultValue)));
    }

    private static ComponentEditorDefinition rootTextList(String id, String category, String title) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.ROOT_TEXT_LIST,
                List.of(textList("entries", "每行文字，用 | 分隔", "")));
    }

    private static ComponentEditorDefinition effectListRoot(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.ROOT_EFFECT_LIST,
                List.of(effectList("entries", "效果|持续 tick|等级|概率", defaultValue)));
    }

    private static ComponentEditorDefinition patternListRoot(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.ROOT_PATTERN_LIST,
                List.of(patternList("entries", "图案:颜色，逗号分隔", defaultValue)));
    }

    private static ComponentEditorDefinition beeListRoot(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.ROOT_BEE_LIST,
                List.of(beeList("entries", "实体 ID|已在巢 tick|最少 tick", defaultValue)));
    }

    private static ComponentEditorDefinition stringMap(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.ROOT_STRING_MAP,
                List.of(stringMap("entries", "键=值，逗号分隔", defaultValue)));
    }

    private static ComponentEditorDefinition rootSnbtCompound(String id, String category, String title, String defaultValue) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.ROOT_SNBT_COMPOUND,
                List.of(snbtCompound("value", "SNBT", defaultValue)));
    }

    private static ComponentEditorDefinition compound(String id, String category, String title, ComponentEditorField... fields) {
        return new ComponentEditorDefinition(id, category, title, ComponentValueShape.COMPOUND, List.of(fields));
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
        return new ComponentEditorField(key, label, ComponentFieldKind.ENUM, defaultValue, List.of(values), false);
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
        return new ComponentEditorField(key, label, ComponentFieldKind.UUID, defaultValue, List.of(), true);
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
        return new ComponentEditorField(key, label, kind, defaultValue, List.of(), false);
    }

    private static ComponentEditorField optionalField(String key, String label, ComponentFieldKind kind, String defaultValue) {
        return new ComponentEditorField(key, label, kind, defaultValue, List.of(), true);
    }
}

record ComponentEditorDefinition(String id, String category, String title, ComponentValueShape shape,
                                 List<ComponentEditorField> fields) {
    Component displayName() {
        return Component.literal(this.title);
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

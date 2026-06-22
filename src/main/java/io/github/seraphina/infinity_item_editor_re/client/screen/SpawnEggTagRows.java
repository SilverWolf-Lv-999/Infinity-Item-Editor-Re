package io.github.seraphina.infinity_item_editor_re.client.screen;

import java.util.ArrayList;
import java.util.List;

final class SpawnEggTagRows {
    private static final List<SpawnEggChoiceOption> VILLAGER_PROFESSION_OPTIONS = List.of(
            new SpawnEggChoiceOption("", "random"),
            new SpawnEggChoiceOption("minecraft:farmer", "farmer"),
            new SpawnEggChoiceOption("minecraft:librarian", "librarian"),
            new SpawnEggChoiceOption("minecraft:cleric", "cleric"),
            new SpawnEggChoiceOption("minecraft:armorer", "armorer"),
            new SpawnEggChoiceOption("minecraft:butcher", "butcher"),
            new SpawnEggChoiceOption("minecraft:nitwit", "nitwit"),
            new SpawnEggChoiceOption("minecraft:none", "none"),
            new SpawnEggChoiceOption("minecraft:cartographer", "cartographer"),
            new SpawnEggChoiceOption("minecraft:fisherman", "fisherman"),
            new SpawnEggChoiceOption("minecraft:fletcher", "fletcher"),
            new SpawnEggChoiceOption("minecraft:leatherworker", "leatherworker"),
            new SpawnEggChoiceOption("minecraft:mason", "mason"),
            new SpawnEggChoiceOption("minecraft:shepherd", "shepherd"),
            new SpawnEggChoiceOption("minecraft:toolsmith", "toolsmith"),
            new SpawnEggChoiceOption("minecraft:weaponsmith", "weaponsmith")
    );
    private static final List<SpawnEggChoiceOption> VILLAGER_TYPE_OPTIONS = List.of(
            new SpawnEggChoiceOption("", "random"),
            new SpawnEggChoiceOption("minecraft:plains", "plains"),
            new SpawnEggChoiceOption("minecraft:desert", "desert"),
            new SpawnEggChoiceOption("minecraft:jungle", "jungle"),
            new SpawnEggChoiceOption("minecraft:savanna", "savanna"),
            new SpawnEggChoiceOption("minecraft:snow", "snow"),
            new SpawnEggChoiceOption("minecraft:swamp", "swamp"),
            new SpawnEggChoiceOption("minecraft:taiga", "taiga")
    );
    private static final List<SpawnEggChoiceOption> VILLAGER_LEVEL_OPTIONS = List.of(
            new SpawnEggChoiceOption("", "random"),
            new SpawnEggChoiceOption("1", "1"),
            new SpawnEggChoiceOption("2", "2"),
            new SpawnEggChoiceOption("3", "3"),
            new SpawnEggChoiceOption("4", "4"),
            new SpawnEggChoiceOption("5", "5")
    );

    static final List<SpawnEggTagRow> GENERAL = List.of(
            customName(),
            floatNumber("health", "Health", 0.0D, 2048.0D),
            intNumber("age", "Age", -24000.0D, 24000.0D),
            shortNumber("fire", "Fire", 0.0D, Short.MAX_VALUE),
            floatNumber("absorption", "AbsorptionAmount", 0.0D, 2048.0D),
            booleanRow("no_ai", "NoAI"),
            booleanRow("no_gravity", "NoGravity"),
            booleanRow("invulnerable", "Invulnerable"),
            booleanRow("silent", "Silent"),
            booleanRow("glowing", "Glowing"),
            booleanRow("custom_name_visible", "CustomNameVisible"),
            booleanRow("can_pick_up_loot", "CanPickUpLoot"),
            booleanRow("persistence_required", "PersistenceRequired"),
            booleanRow("fall_flying", "FallFlying")
    );

    private SpawnEggTagRows() {
    }

    static List<SpawnEggTagRow> forEntity(String path) {
        List<SpawnEggTagRow> rows = new ArrayList<>();
        switch (path) {
            case "chicken" -> rows.add(intNumber("egg_lay_time", "EggLayTime", 0.0D, 20000.0D));
            case "creeper" -> {
                rows.add(booleanRow("creeper_powered", "powered"));
                rows.add(byteNumber("explosion_radius", "ExplosionRadius", 0.0D, Byte.MAX_VALUE));
                rows.add(shortNumber("fuse", "Fuse", 0.0D, 500.0D));
                rows.add(booleanRow("ignited", "ignited"));
            }
            case "creaking" -> rows.add(presenceRow("creaking_heart_bound", "home_pos"));
            case "endermite" -> {
                rows.add(intNumber("life_time", "Lifetime", 0.0D, 3000.0D));
            }
            case "cat", "wolf" -> rows.add(owner());
            case "parrot" -> {
                rows.add(owner());
                rows.add(intNumber("variant", "Variant", 0.0D, 4.0D));
            }
            case "phantom" -> rows.add(intNumber("size", "Size", 0.0D, 64.0D));
            case "pig" -> rows.add(booleanRow("saddle", "Saddle"));
            case "sheep" -> {
                rows.add(intNumber("color", "Color", 0.0D, 15.0D));
                rows.add(booleanRow("sheared", "Sheared"));
            }
            case "shulker" -> rows.add(intNumber("color", "Color", 0.0D, 16.0D));
            case "slime", "magma_cube" -> rows.add(displayOffsetIntNumber("size", "Size", 1.0D, 50.0D, 1.0D));
            case "vindicator" -> rows.add(booleanRow("johnny", "Johnny"));
            case "zombie", "husk", "drowned", "zombie_villager", "zombified_piglin" -> {
                rows.add(booleanRow("is_baby", "IsBaby"));
                rows.add(booleanRow("can_break_doors", "CanBreakDoors"));
                if ("zombie_villager".equals(path)) {
                    rows.add(villagerProfession());
                    rows.add(villagerType());
                    rows.add(villagerLevel());
                }
                if ("zombified_piglin".equals(path)) {
                    rows.add(intNumber("anger", "AngerTime", 0.0D, 32767.0D));
                }
            }
            case "villager" -> {
                rows.add(villagerProfession());
                rows.add(booleanRow("villager_willing", "Willing"));
                rows.add(villagerType());
                rows.add(villagerLevel());
            }
            default -> {
            }
        }
        return rows;
    }

    private static SpawnEggTagRow booleanRow(String translationSuffix, String tagKey) {
        return new SpawnEggTagRow(translationSuffix, tagKey, SpawnEggTagRowType.BOOLEAN, null, 0.0D, 1.0D);
    }

    private static SpawnEggTagRow presenceRow(String translationSuffix, String tagKey) {
        return new SpawnEggTagRow(translationSuffix, tagKey, SpawnEggTagRowType.PRESENCE, null, 0.0D, 1.0D);
    }

    private static SpawnEggTagRow customName() {
        return new SpawnEggTagRow("custom_name", "CustomName", SpawnEggTagRowType.CUSTOM_NAME, null, 0.0D, 0.0D);
    }

    private static SpawnEggTagRow owner() {
        return new SpawnEggTagRow("owner", "Owner", SpawnEggTagRowType.OWNER, null, 0.0D, 0.0D);
    }

    private static SpawnEggTagRow villagerProfession() {
        return choice("villager_profession", "VillagerData.profession",
                SpawnEggChoiceStorage.STRING, VILLAGER_PROFESSION_OPTIONS);
    }

    private static SpawnEggTagRow villagerType() {
        return choice("villager_type", "VillagerData.type", SpawnEggChoiceStorage.STRING, VILLAGER_TYPE_OPTIONS);
    }

    private static SpawnEggTagRow villagerLevel() {
        return choice("villager_level", "VillagerData.level", SpawnEggChoiceStorage.INT, VILLAGER_LEVEL_OPTIONS);
    }

    private static SpawnEggTagRow byteNumber(String translationSuffix, String tagKey, double minValue, double maxValue) {
        return number(translationSuffix, tagKey, SpawnEggNumberType.BYTE, minValue, maxValue);
    }

    private static SpawnEggTagRow shortNumber(String translationSuffix, String tagKey, double minValue, double maxValue) {
        return number(translationSuffix, tagKey, SpawnEggNumberType.SHORT, minValue, maxValue);
    }

    private static SpawnEggTagRow intNumber(String translationSuffix, String tagKey, double minValue, double maxValue) {
        return number(translationSuffix, tagKey, SpawnEggNumberType.INT, minValue, maxValue);
    }

    private static SpawnEggTagRow displayOffsetIntNumber(String translationSuffix, String tagKey, double minValue,
                                                         double maxValue, double displayOffset) {
        return number(translationSuffix, tagKey, SpawnEggNumberType.INT, minValue, maxValue, displayOffset);
    }

    private static SpawnEggTagRow floatNumber(String translationSuffix, String tagKey, double minValue, double maxValue) {
        return number(translationSuffix, tagKey, SpawnEggNumberType.FLOAT, minValue, maxValue);
    }

    private static SpawnEggTagRow number(String translationSuffix, String tagKey,
                                         SpawnEggNumberType numberType, double minValue, double maxValue) {
        return number(translationSuffix, tagKey, numberType, minValue, maxValue, 0.0D);
    }

    private static SpawnEggTagRow number(String translationSuffix, String tagKey,
                                         SpawnEggNumberType numberType, double minValue, double maxValue,
                                         double displayOffset) {
        return new SpawnEggTagRow(translationSuffix, tagKey, SpawnEggTagRowType.NUMBER, numberType,
                minValue, maxValue, displayOffset, null, null);
    }

    private static SpawnEggTagRow choice(String translationSuffix, String tagKey,
                                         SpawnEggChoiceStorage choiceStorage, List<SpawnEggChoiceOption> choices) {
        return new SpawnEggTagRow(translationSuffix, tagKey, SpawnEggTagRowType.CHOICE, null,
                0.0D, 0.0D, 0.0D, choiceStorage, choices);
    }
}

record SpawnEggTagRow(String translationSuffix, String tagKey, SpawnEggTagRowType type,
                      SpawnEggNumberType numberType, double minValue, double maxValue, double displayOffset,
                      SpawnEggChoiceStorage choiceStorage, List<SpawnEggChoiceOption> choices) {
    SpawnEggTagRow(String translationSuffix, String tagKey, SpawnEggTagRowType type,
                   SpawnEggNumberType numberType, double minValue, double maxValue) {
        this(translationSuffix, tagKey, type, numberType, minValue, maxValue, 0.0D, null, null);
    }

    double toStoredNumber(double displayValue) {
        return displayValue - this.displayOffset;
    }

    double toDisplayNumber(double storedValue) {
        return storedValue + this.displayOffset;
    }
}

record SpawnEggChoiceOption(String value, String translationSuffix) {
}

enum SpawnEggTagRowType {
    BOOLEAN,
    PRESENCE,
    NUMBER,
    CUSTOM_NAME,
    OWNER,
    CHOICE
}

enum SpawnEggNumberType {
    BYTE,
    SHORT,
    INT,
    FLOAT
}

enum SpawnEggChoiceStorage {
    STRING,
    INT
}

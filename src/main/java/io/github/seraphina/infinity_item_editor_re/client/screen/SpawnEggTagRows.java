package io.github.seraphina.infinity_item_editor_re.client.screen;

import java.util.ArrayList;
import java.util.List;

final class SpawnEggTagRows {
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
            case "endermite" -> {
                rows.add(intNumber("life_time", "LifeTime", 0.0D, 3000.0D));
                rows.add(booleanRow("player_spawned", "PlayerSpawned"));
            }
            case "parrot" -> rows.add(intNumber("variant", "Variant", 0.0D, 4.0D));
            case "pig" -> rows.add(booleanRow("saddle", "Saddle"));
            case "sheep" -> {
                rows.add(intNumber("color", "Color", 0.0D, 15.0D));
                rows.add(booleanRow("sheared", "Sheared"));
            }
            case "shulker" -> rows.add(intNumber("color", "Color", 0.0D, 16.0D));
            case "slime", "magma_cube" -> rows.add(intNumber("size", "Size", 1.0D, 50.0D));
            case "vindicator" -> rows.add(booleanRow("johnny", "Johnny"));
            case "zombie", "husk", "drowned", "zombie_villager", "zombified_piglin" -> {
                rows.add(booleanRow("is_baby", "IsBaby"));
                rows.add(booleanRow("can_break_doors", "CanBreakDoors"));
                if ("zombified_piglin".equals(path)) {
                    rows.add(shortNumber("anger", "Anger", 0.0D, 1000.0D));
                }
            }
            default -> {
            }
        }
        return rows;
    }

    private static SpawnEggTagRow booleanRow(String translationSuffix, String tagKey) {
        return new SpawnEggTagRow(translationSuffix, tagKey, SpawnEggTagRowType.BOOLEAN, null, 0.0D, 1.0D);
    }

    private static SpawnEggTagRow customName() {
        return new SpawnEggTagRow("custom_name", "CustomName", SpawnEggTagRowType.CUSTOM_NAME, null, 0.0D, 0.0D);
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

    private static SpawnEggTagRow floatNumber(String translationSuffix, String tagKey, double minValue, double maxValue) {
        return number(translationSuffix, tagKey, SpawnEggNumberType.FLOAT, minValue, maxValue);
    }

    private static SpawnEggTagRow number(String translationSuffix, String tagKey,
                                         SpawnEggNumberType numberType, double minValue, double maxValue) {
        return new SpawnEggTagRow(translationSuffix, tagKey, SpawnEggTagRowType.NUMBER, numberType, minValue, maxValue);
    }
}

record SpawnEggTagRow(String translationSuffix, String tagKey, SpawnEggTagRowType type,
                      SpawnEggNumberType numberType, double minValue, double maxValue) {
}

enum SpawnEggTagRowType {
    BOOLEAN,
    NUMBER,
    CUSTOM_NAME
}

enum SpawnEggNumberType {
    BYTE,
    SHORT,
    INT,
    FLOAT
}

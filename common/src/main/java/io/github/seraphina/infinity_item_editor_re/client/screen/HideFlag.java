package io.github.seraphina.infinity_item_editor_re.client.screen;

enum HideFlag {
    ENCHANTMENTS(1, "flag.enchantment"),
    ATTRIBUTE_MODIFIERS(2, "flag.attributemod"),
    UNBREAKABLE(4, "flag.unbreakable"),
    CAN_DESTROY(8, "flag.candestroy"),
    CAN_PLACE_ON(16, "flag.canplaceon"),
    ITEM_INFO(32, "flag.iteminfo"),
    DYE(64, "flag.dye"),
    UPGRADES(128, "flag.upgrades");

    private final int mask;
    private final String translationKey;

    HideFlag(int mask, String translationKey) {
        this.mask = mask;
        this.translationKey = translationKey;
    }

    int mask() {
        return this.mask;
    }

    String translationKey() {
        return this.translationKey;
    }
}

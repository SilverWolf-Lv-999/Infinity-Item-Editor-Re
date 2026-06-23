package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.client.screen.modern.ModernUi;
import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;
import io.github.seraphina.infinity_item_editor_re.util.NbtCompat;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

abstract class ItemEditorScreenComponents extends ItemEditorScreenActions {
    private static final int COMPONENT_TAB_QUICK = 0;
    private static final int COMPONENT_TAB_BASIC = 1;
    private static final int COMPONENT_TAB_FOOD = 2;
    private static final int COMPONENT_TAB_TOOL = 3;
    private static final int COMPONENT_TAB_EQUIPMENT = 4;
    private static final int COMPONENT_TAB_ADVANCED = 5;
    private static final String[] COMPONENT_TAB_KEYS = {
            "components.tab.quick",
            "components.tab.basic",
            "components.tab.food",
            "components.tab.tool",
            "components.tab.equipment",
            "components.tab.advanced"
    };
    private static final String[] CONSUME_ANIMATIONS = {
            "eat", "drink", "none", "block", "bow", "spear", "crossbow", "spyglass", "toot_horn", "brush", "bundle"
    };
    private static final String[] EQUIPMENT_SLOTS = {
            "head", "chest", "legs", "feet", "mainhand", "offhand", "body", "saddle"
    };

    protected ItemEditorScreenComponents(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(stack, targetContainerSlot, parentTradeScreen, parentTradeIndex, parentTradeSlot);
    }

    protected void addComponentEditorPanel() {
        int left = componentEditorLeft();
        int top = componentEditorTop();
        int width = componentEditorWidth();
        int formTop = addComponentTabButtons(left, top, width);
        switch (this.componentEditorTab) {
            case COMPONENT_TAB_QUICK -> addQuickComponentPanel(left, formTop, width);
            case COMPONENT_TAB_BASIC -> addBasicComponentPanel(left, formTop, width);
            case COMPONENT_TAB_FOOD -> addFoodComponentPanel(left, formTop, width);
            case COMPONENT_TAB_TOOL -> addToolComponentPanel(left, formTop, width);
            case COMPONENT_TAB_EQUIPMENT -> addEquipmentComponentPanel(left, formTop, width);
            case COMPONENT_TAB_ADVANCED -> addRawComponentPanel(left, formTop, width);
            default -> {
                this.componentEditorTab = COMPONENT_TAB_QUICK;
                addQuickComponentPanel(left, formTop, width);
            }
        }
    }

    protected void applyComponentEditor() {
        if (isAdvancedComponentEditorTab()) {
            updateComponentNbt();
            return;
        }

        switch (this.componentEditorTab) {
            case COMPONENT_TAB_QUICK -> applyQuickDurability();
            case COMPONENT_TAB_BASIC -> applyBasicComponents();
            case COMPONENT_TAB_FOOD -> applyFoodComponents();
            case COMPONENT_TAB_TOOL -> applyToolComponents();
            case COMPONENT_TAB_EQUIPMENT -> applyEquipmentComponents();
            default -> updateComponentNbt();
        }
    }

    protected boolean isAdvancedComponentEditorTab() {
        return this.componentEditorTab == COMPONENT_TAB_ADVANCED;
    }

    private int addComponentTabButtons(int left, int top, int width) {
        int gap = 4;
        int columns = width >= 360 ? COMPONENT_TAB_KEYS.length : Math.min(3, COMPONENT_TAB_KEYS.length);
        int buttonWidth = Math.max(46, (width - gap * (columns - 1)) / columns);
        int buttonHeight = isSidebarUi() ? SIDEBAR_BUTTON_HEIGHT : FIELD_HEIGHT;
        for (int i = 0; i < COMPONENT_TAB_KEYS.length; i++) {
            int tab = i;
            int row = i / columns;
            int column = i % columns;
            Component text = Component.translatable(key(COMPONENT_TAB_KEYS[i]));
            addRenderableWidget(new InfinityEditorButton(left + column * (buttonWidth + gap), top + row * (buttonHeight + gap),
                    buttonWidth, buttonHeight, text, button -> {
                this.componentEditorTab = tab;
                this.nbtFeedback = "";
                rebuildWidgets();
            }));
        }
        int rows = (COMPONENT_TAB_KEYS.length + columns - 1) / columns;
        return top + rows * (buttonHeight + gap) + 8;
    }

    private void addQuickComponentPanel(int left, int top, int width) {
        CompoundTag components = currentComponents();
        CompoundTag food = componentCompound(components, "minecraft:food");
        CompoundTag tool = componentCompound(components, "minecraft:tool");
        CompoundTag firstRule = firstToolRule(tool);
        CompoundTag repairable = componentCompound(components, "minecraft:repairable");

        addComponentField(left, top, width, 0, "component.quick.durability", "quick.durability",
                numericComponentValue(components, "minecraft:max_damage", "1000"));
        addComponentField(left, top, width, 1, "component.quick.food_points", "quick.food_points",
                food.isEmpty() ? "4" : Integer.toString(NbtCompat.getInt(food, "nutrition")));
        addComponentField(left, top, width, 2, "component.quick.mining_speed", "quick.mining_speed",
                firstRule.isEmpty() ? "6.0" : Float.toString(NbtCompat.getFloat(firstRule, "speed")));
        addComponentField(left, top, width, 3, "component.quick.repair_item", "quick.repair_item",
                componentString(repairable, "items", "iron_ingot"));

        int y = componentFieldsBottom(top, width, 4) + 8;
        int columns = 4;
        addComponentActionButton(left, y, width, 0, columns, Component.translatable(key("components.quick.durable")), button -> applyQuickDurability());
        addComponentActionButton(left, y, width, 1, columns, Component.translatable(key("components.quick.food")), button -> applyQuickFood(false, false));
        addComponentActionButton(left, y, width, 2, columns, Component.translatable(key("components.quick.drink")), button -> applyQuickFood(false, true));
        addComponentActionButton(left, y, width, 3, columns, Component.translatable(key("components.quick.tool")), button -> applyQuickTool("#minecraft:mineable/pickaxe"));
        addComponentActionButton(left, y, width, 4, columns, Component.translatable(key("components.quick.weapon")), button -> applyQuickWeapon());
        addComponentActionButton(left, y, width, 5, columns, Component.translatable(key("components.quick.head")), button -> applyQuickEquipment("head", false));
        addComponentActionButton(left, y, width, 6, columns, Component.translatable(key("components.quick.chest")), button -> applyQuickEquipment("chest", false));
        addComponentActionButton(left, y, width, 7, columns, Component.translatable(key("components.quick.feet")), button -> applyQuickEquipment("feet", false));
        addComponentActionButton(left, y, width, 8, columns, Component.translatable(key("components.quick.glider")), button -> applyQuickEquipment("chest", true));
        addComponentActionButton(left, y, width, 9, columns, getMarkerText("minecraft:unbreakable", "components.quick.unbreakable"), button -> toggleMarkerComponent("minecraft:unbreakable"));
        addComponentActionButton(left, y, width, 10, columns, getGlintText(components), button -> cycleGlintOverride());
        addComponentActionButton(left, y, width, 11, columns, getMarkerText("minecraft:death_protection", "components.quick.death_protection"), button -> toggleMarkerComponent("minecraft:death_protection"));
        addComponentActionButton(left, y, width, 12, columns, Component.translatable(key("components.quick.clear")), button -> clearQuickComponents());
    }

    private void addBasicComponentPanel(int left, int top, int width) {
        CompoundTag components = currentComponents();
        addComponentField(left, top, width, 0, "component.basic.max_stack_size", "basic.max_stack_size",
                numericComponentValue(components, "minecraft:max_stack_size", ""));
        addComponentField(left, top, width, 1, "component.basic.max_damage", "basic.max_damage",
                numericComponentValue(components, "minecraft:max_damage", ""));
        addComponentField(left, top, width, 2, "component.basic.damage", "basic.damage",
                numericComponentValue(components, "minecraft:damage", ""));
        addComponentField(left, top, width, 3, "component.basic.repair_cost", "basic.repair_cost",
                numericComponentValue(components, "minecraft:repair_cost", ""));
        addComponentField(left, top, width, 4, "component.basic.enchantable", "basic.enchantable",
                componentCompoundInt(components, "minecraft:enchantable", "value", ""));

        int y = componentFieldsBottom(top, width, 5) + 8;
        addComponentActionButton(left, y, width, 0, 5, Component.translatable(key("components.apply")), button -> applyBasicComponents());
        addComponentActionButton(left, y, width, 1, 5, getMarkerText("minecraft:unbreakable", "components.unbreakable"), button -> toggleMarkerComponent("minecraft:unbreakable"));
        addComponentActionButton(left, y, width, 2, 5, getMarkerText("minecraft:glider", "components.glider"), button -> toggleMarkerComponent("minecraft:glider"));
        addComponentActionButton(left, y, width, 3, 5, getGlintText(components), button -> cycleGlintOverride());
        addComponentActionButton(left, y, width, 4, 5, Component.translatable(key("components.remove.basic")), button -> removeBasicComponents());
    }

    private void addFoodComponentPanel(int left, int top, int width) {
        CompoundTag components = currentComponents();
        CompoundTag food = componentCompound(components, "minecraft:food");
        CompoundTag consumable = componentCompound(components, "minecraft:consumable");
        CompoundTag cooldown = componentCompound(components, "minecraft:use_cooldown");

        addComponentField(left, top, width, 0, "component.food.nutrition", "food.nutrition",
                food.isEmpty() ? "4" : Integer.toString(NbtCompat.getInt(food, "nutrition")));
        addComponentField(left, top, width, 1, "component.food.saturation", "food.saturation",
                food.isEmpty() ? "0.3" : Float.toString(NbtCompat.getFloat(food, "saturation")));
        addComponentField(left, top, width, 2, "component.food.always_eat", "food.always_eat",
                Boolean.toString(NbtCompat.getBoolean(food, "can_always_eat")));
        addComponentField(left, top, width, 3, "component.food.consume_seconds", "food.consume_seconds",
                consumable.isEmpty() ? "1.6" : Float.toString(NbtCompat.getFloat(consumable, "consume_seconds")));
        addComponentField(left, top, width, 4, "component.food.animation", "food.animation",
                componentString(consumable, "animation", "eat"));
        addComponentField(left, top, width, 5, "component.food.sound", "food.sound",
                componentString(consumable, "sound", "minecraft:entity.generic.eat"));
        addComponentField(left, top, width, 6, "component.food.particles", "food.particles",
                Boolean.toString(componentBoolean(consumable, "has_consume_particles", true)));
        addComponentField(left, top, width, 7, "component.food.cooldown_seconds", "food.cooldown_seconds",
                cooldown.isEmpty() ? "" : Float.toString(NbtCompat.getFloat(cooldown, "seconds")));
        addComponentField(left, top, width, 8, "component.food.cooldown_group", "food.cooldown_group",
                componentString(cooldown, "cooldown_group", ""));

        int y = componentFieldsBottom(top, width, 9) + 8;
        addComponentActionButton(left, y, width, 0, 6, Component.translatable(key("components.apply")), button -> applyFoodComponents());
        addComponentActionButton(left, y, width, 1, 6, Component.translatable(key("components.toggle.always_eat")), button -> toggleBooleanField("food.always_eat", false));
        addComponentActionButton(left, y, width, 2, 6, Component.translatable(key("components.toggle.particles")), button -> toggleBooleanField("food.particles", true));
        addComponentActionButton(left, y, width, 3, 6, Component.translatable(key("components.cycle.animation")), button -> cycleTextField("food.animation", CONSUME_ANIMATIONS));
        addComponentActionButton(left, y, width, 4, 6, Component.translatable(key("components.remove.food")), button -> removeComponents("minecraft:food"));
        addComponentActionButton(left, y, width, 5, 6, Component.translatable(key("components.remove.consumable")), button -> removeComponents("minecraft:consumable", "minecraft:use_cooldown"));
    }

    private void addToolComponentPanel(int left, int top, int width) {
        CompoundTag components = currentComponents();
        CompoundTag tool = componentCompound(components, "minecraft:tool");
        CompoundTag firstRule = firstToolRule(tool);
        CompoundTag weapon = componentCompound(components, "minecraft:weapon");
        CompoundTag repairable = componentCompound(components, "minecraft:repairable");
        CompoundTag resistant = componentCompound(components, "minecraft:damage_resistant");

        addComponentField(left, top, width, 0, "component.tool.blocks", "tool.blocks",
                componentString(firstRule, "blocks", "#minecraft:mineable/pickaxe"));
        addComponentField(left, top, width, 1, "component.tool.rule_speed", "tool.rule_speed",
                firstRule.isEmpty() ? "6.0" : Float.toString(NbtCompat.getFloat(firstRule, "speed")));
        addComponentField(left, top, width, 2, "component.tool.correct_drops", "tool.correct_drops",
                Boolean.toString(componentBoolean(firstRule, "correct_for_drops", true)));
        addComponentField(left, top, width, 3, "component.tool.default_speed", "tool.default_speed",
                tool.isEmpty() ? "1.0" : Float.toString(NbtCompat.getFloat(tool, "default_mining_speed")));
        addComponentField(left, top, width, 4, "component.tool.damage_per_block", "tool.damage_per_block",
                tool.isEmpty() ? "1" : Integer.toString(NbtCompat.getInt(tool, "damage_per_block")));
        addComponentField(left, top, width, 5, "component.tool.creative", "tool.creative",
                Boolean.toString(componentBoolean(tool, "can_destroy_blocks_in_creative", true)));
        addComponentField(left, top, width, 6, "component.tool.weapon_damage", "tool.weapon_damage",
                weapon.isEmpty() ? "1" : Integer.toString(NbtCompat.getInt(weapon, "item_damage_per_attack")));
        addComponentField(left, top, width, 7, "component.tool.disable_seconds", "tool.disable_seconds",
                weapon.isEmpty() ? "0.0" : Float.toString(NbtCompat.getFloat(weapon, "disable_blocking_for_seconds")));
        addComponentField(left, top, width, 8, "component.tool.repair_items", "tool.repair_items",
                componentString(repairable, "items", ""));
        addComponentField(left, top, width, 9, "component.tool.resistant_types", "tool.resistant_types",
                componentString(resistant, "types", ""));

        int y = componentFieldsBottom(top, width, 10) + 8;
        addComponentActionButton(left, y, width, 0, 6, Component.translatable(key("components.apply")), button -> applyToolComponents());
        addComponentActionButton(left, y, width, 1, 6, Component.translatable(key("components.toggle.correct_drops")), button -> toggleBooleanField("tool.correct_drops", true));
        addComponentActionButton(left, y, width, 2, 6, Component.translatable(key("components.toggle.creative_blocks")), button -> toggleBooleanField("tool.creative", true));
        addComponentActionButton(left, y, width, 3, 6, Component.translatable(key("components.remove.tool")), button -> removeComponents("minecraft:tool"));
        addComponentActionButton(left, y, width, 4, 6, Component.translatable(key("components.remove.weapon")), button -> removeComponents("minecraft:weapon"));
        addComponentActionButton(left, y, width, 5, 6, Component.translatable(key("components.remove.repairable")), button -> removeComponents("minecraft:repairable", "minecraft:damage_resistant"));
    }

    private void addEquipmentComponentPanel(int left, int top, int width) {
        CompoundTag components = currentComponents();
        CompoundTag equippable = componentCompound(components, "minecraft:equippable");

        addComponentField(left, top, width, 0, "component.equipment.slot", "equipment.slot",
                componentString(equippable, "slot", "head"));
        addComponentField(left, top, width, 1, "component.equipment.equip_sound", "equipment.equip_sound",
                componentString(equippable, "equip_sound", "minecraft:item.armor.equip_generic"));
        addComponentField(left, top, width, 2, "component.equipment.asset_id", "equipment.asset_id",
                componentString(equippable, "asset_id", ""));
        addComponentField(left, top, width, 3, "component.equipment.camera_overlay", "equipment.camera_overlay",
                componentString(equippable, "camera_overlay", ""));
        addComponentField(left, top, width, 4, "component.equipment.allowed_entities", "equipment.allowed_entities",
                componentString(equippable, "allowed_entities", ""));
        addComponentField(left, top, width, 5, "component.equipment.dispensable", "equipment.dispensable",
                Boolean.toString(componentBoolean(equippable, "dispensable", true)));
        addComponentField(left, top, width, 6, "component.equipment.swappable", "equipment.swappable",
                Boolean.toString(componentBoolean(equippable, "swappable", true)));
        addComponentField(left, top, width, 7, "component.equipment.damage_on_hurt", "equipment.damage_on_hurt",
                Boolean.toString(componentBoolean(equippable, "damage_on_hurt", true)));
        addComponentField(left, top, width, 8, "component.equipment.equip_on_interact", "equipment.equip_on_interact",
                Boolean.toString(componentBoolean(equippable, "equip_on_interact", false)));
        addComponentField(left, top, width, 9, "component.equipment.can_be_sheared", "equipment.can_be_sheared",
                Boolean.toString(componentBoolean(equippable, "can_be_sheared", false)));
        addComponentField(left, top, width, 10, "component.equipment.shearing_sound", "equipment.shearing_sound",
                componentString(equippable, "shearing_sound", "minecraft:item.shears.snip"));

        int y = componentFieldsBottom(top, width, 11) + 8;
        addComponentActionButton(left, y, width, 0, 6, Component.translatable(key("components.apply")), button -> applyEquipmentComponents());
        addComponentActionButton(left, y, width, 1, 6, Component.translatable(key("components.cycle.slot")), button -> cycleTextField("equipment.slot", EQUIPMENT_SLOTS));
        addComponentActionButton(left, y, width, 2, 6, Component.translatable(key("components.toggle.dispensable")), button -> toggleBooleanField("equipment.dispensable", true));
        addComponentActionButton(left, y, width, 3, 6, Component.translatable(key("components.toggle.swappable")), button -> toggleBooleanField("equipment.swappable", true));
        addComponentActionButton(left, y, width, 4, 6, getMarkerText("minecraft:death_protection", "components.death_protection"), button -> toggleMarkerComponent("minecraft:death_protection"));
        addComponentActionButton(left, y, width, 5, 6, Component.translatable(key("components.remove.equippable")), button -> removeComponents("minecraft:equippable"));
    }

    private void addRawComponentPanel(int left, int top, int width) {
        this.componentNbtBox = addTrackedBox(legacyTextBox(left, top, width, FIELD_HEIGHT,
                Component.translatable(key("components.raw"))));
        this.componentNbtBox.setMaxLength(30000);
        this.componentNbtBox.setTextColor(componentInputTextColor());
        this.componentNbtBox.setValue(this.componentNbtValue == null ? getInitialComponentsNbt(this.previewStack) : this.componentNbtValue);
        this.componentNbtBox.setResponder(value -> this.componentNbtValue = value);

        int buttonWidth = Math.min(width, Math.max(90, width / 3));
        addRenderableWidget(new InfinityEditorButton(left + width - buttonWidth, top + 28, buttonWidth, SIDEBAR_BUTTON_HEIGHT,
                Component.translatable(key("components.update_raw")), button -> updateComponentNbt()));
        addFormatButtons();
    }

    private EditBox addComponentField(int left, int top, int width, int index, String labelSuffix, String fieldKey, String value) {
        int columns = componentFieldColumns(width);
        int gap = 8;
        int columnWidth = (width - gap * (columns - 1)) / columns;
        int row = index / columns;
        int column = index % columns;
        int x = left + column * (columnWidth + gap);
        int y = top + row * 34;
        Component label = Component.translatable(key(labelSuffix));
        this.componentEditorLabels.add(new ComponentEditorLabel(label, x, y));
        EditBox box = addTrackedBox(legacyTextBox(x, y + 10, columnWidth, 18, label));
        box.setMaxLength(128);
        box.setTextColor(componentInputTextColor());
        box.setValue(value == null ? "" : value);
        this.componentEditorBoxes.put(fieldKey, box);
        return box;
    }

    private int componentInputTextColor() {
        return isSidebarUi() ? ModernUi.TEXT_PRIMARY : 0xFFFFFFFF;
    }

    private void addComponentActionButton(int left, int top, int width, int index, int total, Component text, InfinityEditorButton.PressAction action) {
        int gap = 4;
        int columns = width >= 360 ? Math.min(total, 6) : Math.min(total, 3);
        int buttonWidth = Math.max(46, (width - gap * (columns - 1)) / columns);
        int buttonHeight = isSidebarUi() ? SIDEBAR_BUTTON_HEIGHT : FIELD_HEIGHT;
        int row = index / columns;
        int column = index % columns;
        addRenderableWidget(new InfinityEditorButton(left + column * (buttonWidth + gap), top + row * (buttonHeight + gap),
                buttonWidth, buttonHeight, text, action));
    }

    private int componentEditorLeft() {
        return centeredContentX(componentEditorWidth());
    }

    private int componentEditorTop() {
        return isSidebarUi() ? 58 : 58;
    }

    private int componentEditorWidth() {
        if (isSidebarUi()) {
            return Math.max(160, contentWidth() - 20);
        }
        return contentLimitedWidth(430, 300, 24);
    }

    private int componentFieldColumns(int width) {
        return width >= 300 ? 2 : 1;
    }

    private int componentFieldsBottom(int top, int width, int fieldCount) {
        int rows = (fieldCount + componentFieldColumns(width) - 1) / componentFieldColumns(width);
        return top + rows * 34;
    }

    private void applyQuickDurability() {
        try {
            CompoundTag components = currentComponents();
            applyDurabilityFields(components);
            applyComponentsAndRefresh(components, "editor_components_quick_durable");
        } catch (IllegalArgumentException exception) {
            showComponentError(exception);
        }
    }

    private void applyQuickFood(boolean alwaysEat, boolean drink) {
        try {
            CompoundTag components = currentComponents();
            int nutrition = parseIntField("quick.food_points", "4", 0, 1000);

            CompoundTag food = new CompoundTag();
            food.putInt("nutrition", nutrition);
            food.putFloat("saturation", Math.max(0.1F, nutrition * 0.3F));
            if (alwaysEat) {
                food.putBoolean("can_always_eat", true);
            }
            components.put("minecraft:food", food);

            CompoundTag consumable = new CompoundTag();
            if (drink) {
                consumable.putString("animation", "drink");
                consumable.putString("sound", "minecraft:entity.generic.drink");
            } else {
                consumable.putString("sound", "minecraft:entity.generic.eat");
            }
            components.put("minecraft:consumable", consumable);
            applyComponentsAndRefresh(components, drink ? "editor_components_quick_drink" : "editor_components_quick_food");
        } catch (IllegalArgumentException exception) {
            showComponentError(exception);
        }
    }

    private void applyQuickTool(String blocks) {
        try {
            CompoundTag components = currentComponents();
            applyDurabilityFields(components);

            CompoundTag tool = new CompoundTag();
            ListTag rules = new ListTag();
            CompoundTag rule = new CompoundTag();
            rule.putString("blocks", blocks);
            rule.putFloat("speed", parseFloatField("quick.mining_speed", "6.0", 0.01F, 100000.0F));
            rule.putBoolean("correct_for_drops", true);
            rules.add(rule);
            tool.put("rules", rules);
            components.put("minecraft:tool", tool);
            putHolderSetComponentValue(components, "minecraft:repairable", "items", componentFieldValue("quick.repair_item", "iron_ingot"));
            applyComponentsAndRefresh(components, "editor_components_quick_tool");
        } catch (IllegalArgumentException exception) {
            showComponentError(exception);
        }
    }

    private void applyQuickWeapon() {
        try {
            CompoundTag components = currentComponents();
            applyDurabilityFields(components);

            CompoundTag weapon = new CompoundTag();
            weapon.putInt("item_damage_per_attack", 1);
            components.put("minecraft:weapon", weapon);
            putHolderSetComponentValue(components, "minecraft:repairable", "items", componentFieldValue("quick.repair_item", "iron_ingot"));
            applyComponentsAndRefresh(components, "editor_components_quick_weapon");
        } catch (IllegalArgumentException exception) {
            showComponentError(exception);
        }
    }

    private void applyQuickEquipment(String slot, boolean glider) {
        try {
            CompoundTag components = currentComponents();
            applyDurabilityFields(components);

            CompoundTag equippable = new CompoundTag();
            equippable.putString("slot", slot);
            equippable.putString("equip_sound", "minecraft:item.armor.equip_generic");
            components.put("minecraft:equippable", equippable);
            if (glider) {
                components.put("minecraft:glider", new CompoundTag());
            } else {
                components.remove("minecraft:glider");
            }
            putHolderSetComponentValue(components, "minecraft:repairable", "items", componentFieldValue("quick.repair_item", "iron_ingot"));
            applyComponentsAndRefresh(components, glider ? "editor_components_quick_glider" : "editor_components_quick_equipment");
        } catch (IllegalArgumentException exception) {
            showComponentError(exception);
        }
    }

    private void applyBasicComponents() {
        try {
            CompoundTag components = currentComponents();
            putOptionalIntComponent(components, "minecraft:max_stack_size", "basic.max_stack_size", 1, 999);
            putOptionalIntComponent(components, "minecraft:max_damage", "basic.max_damage", 1, 999999);
            putOptionalIntComponent(components, "minecraft:damage", "basic.damage", 0, 999999);
            putOptionalIntComponent(components, "minecraft:repair_cost", "basic.repair_cost", 0, 999999);
            putEnchantableComponent(components);
            applyComponentsAndRefresh(components);
        } catch (IllegalArgumentException exception) {
            showComponentError(exception);
        }
    }

    private void applyFoodComponents() {
        try {
            CompoundTag components = currentComponents();
            CompoundTag food = new CompoundTag();
            food.putInt("nutrition", parseIntField("food.nutrition", "4", 0, 1000));
            food.putFloat("saturation", parseFloatField("food.saturation", "0.3", 0.0F, 1000.0F));
            if (parseBooleanField("food.always_eat", false)) {
                food.putBoolean("can_always_eat", true);
            }
            components.put("minecraft:food", food);

            CompoundTag consumable = new CompoundTag();
            putFloatIfDifferent(consumable, "consume_seconds", parseFloatField("food.consume_seconds", "1.6", 0.0F, 1000.0F), 1.6F);
            putStringIfNotDefault(consumable, "animation", normalizeLowerField("food.animation", "eat"), "eat");
            putStringIfNotBlank(consumable, "sound", normalizeResourceField("food.sound", "minecraft:entity.generic.eat"));
            if (!parseBooleanField("food.particles", true)) {
                consumable.putBoolean("has_consume_particles", false);
            }
            components.put("minecraft:consumable", consumable);

            String cooldownSeconds = componentFieldValue("food.cooldown_seconds", "").trim();
            if (cooldownSeconds.isEmpty()) {
                components.remove("minecraft:use_cooldown");
            } else {
                CompoundTag cooldown = new CompoundTag();
                cooldown.putFloat("seconds", parseFloatField("food.cooldown_seconds", "1.0", 0.01F, 100000.0F));
                putStringIfNotBlank(cooldown, "cooldown_group", normalizeResourceField("food.cooldown_group", ""));
                components.put("minecraft:use_cooldown", cooldown);
            }

            applyComponentsAndRefresh(components);
        } catch (IllegalArgumentException exception) {
            showComponentError(exception);
        }
    }

    private void applyToolComponents() {
        try {
            CompoundTag components = currentComponents();
            CompoundTag tool = new CompoundTag();
            ListTag rules = new ListTag();
            String blocks = componentFieldValue("tool.blocks", "").trim();
            if (!blocks.isEmpty()) {
                CompoundTag rule = new CompoundTag();
                rule.putString("blocks", normalizeHolderSetInput(blocks));
                rule.putFloat("speed", parseFloatField("tool.rule_speed", "6.0", 0.01F, 100000.0F));
                rule.putBoolean("correct_for_drops", parseBooleanField("tool.correct_drops", true));
                rules.add(rule);
            }
            tool.put("rules", rules);
            putFloatIfDifferent(tool, "default_mining_speed", parseFloatField("tool.default_speed", "1.0", 0.0F, 100000.0F), 1.0F);
            putIntIfDifferent(tool, "damage_per_block", parseIntField("tool.damage_per_block", "1", 0, 100000), 1);
            if (!parseBooleanField("tool.creative", true)) {
                tool.putBoolean("can_destroy_blocks_in_creative", false);
            }
            components.put("minecraft:tool", tool);

            CompoundTag weapon = new CompoundTag();
            putIntIfDifferent(weapon, "item_damage_per_attack", parseIntField("tool.weapon_damage", "1", 0, 100000), 1);
            putFloatIfDifferent(weapon, "disable_blocking_for_seconds", parseFloatField("tool.disable_seconds", "0.0", 0.0F, 100000.0F), 0.0F);
            components.put("minecraft:weapon", weapon);

            putHolderSetComponent(components, "minecraft:repairable", "items", "tool.repair_items");
            putHolderSetComponent(components, "minecraft:damage_resistant", "types", "tool.resistant_types");
            applyComponentsAndRefresh(components);
        } catch (IllegalArgumentException exception) {
            showComponentError(exception);
        }
    }

    private void applyEquipmentComponents() {
        try {
            CompoundTag components = currentComponents();
            CompoundTag equippable = new CompoundTag();
            equippable.putString("slot", normalizeLowerField("equipment.slot", "head"));
            putStringIfNotBlank(equippable, "equip_sound", normalizeResourceField("equipment.equip_sound", "minecraft:item.armor.equip_generic"));
            putStringIfNotBlank(equippable, "asset_id", normalizeResourceField("equipment.asset_id", ""));
            putStringIfNotBlank(equippable, "camera_overlay", normalizeResourceField("equipment.camera_overlay", ""));
            putStringIfNotBlank(equippable, "allowed_entities", normalizeHolderSetInput(componentFieldValue("equipment.allowed_entities", "")));
            if (!parseBooleanField("equipment.dispensable", true)) {
                equippable.putBoolean("dispensable", false);
            }
            if (!parseBooleanField("equipment.swappable", true)) {
                equippable.putBoolean("swappable", false);
            }
            if (!parseBooleanField("equipment.damage_on_hurt", true)) {
                equippable.putBoolean("damage_on_hurt", false);
            }
            if (parseBooleanField("equipment.equip_on_interact", false)) {
                equippable.putBoolean("equip_on_interact", true);
            }
            if (parseBooleanField("equipment.can_be_sheared", false)) {
                equippable.putBoolean("can_be_sheared", true);
                putStringIfNotBlank(equippable, "shearing_sound", normalizeResourceField("equipment.shearing_sound", "minecraft:item.shears.snip"));
            }
            components.put("minecraft:equippable", equippable);
            applyComponentsAndRefresh(components);
        } catch (IllegalArgumentException exception) {
            showComponentError(exception);
        }
    }

    private void removeBasicComponents() {
        removeComponents("minecraft:max_stack_size", "minecraft:max_damage", "minecraft:damage", "minecraft:repair_cost", "minecraft:enchantable",
                "minecraft:unbreakable", "minecraft:glider", "minecraft:enchantment_glint_override");
    }

    private void clearQuickComponents() {
        CompoundTag components = currentComponents();
        String[] quickComponents = {
                "minecraft:max_stack_size", "minecraft:max_damage", "minecraft:damage", "minecraft:food", "minecraft:consumable",
                "minecraft:use_cooldown", "minecraft:tool", "minecraft:weapon", "minecraft:repairable", "minecraft:equippable",
                "minecraft:glider", "minecraft:unbreakable", "minecraft:death_protection", "minecraft:enchantment_glint_override"
        };
        for (String componentKey : quickComponents) {
            components.remove(componentKey);
        }
        applyComponentsAndRefresh(components, "editor_components_quick_cleared");
    }

    private void removeComponents(String... componentKeys) {
        CompoundTag components = currentComponents();
        for (String componentKey : componentKeys) {
            components.remove(componentKey);
        }
        applyComponentsAndRefresh(components);
    }

    private void toggleMarkerComponent(String componentKey) {
        CompoundTag components = currentComponents();
        if (components.contains(componentKey)) {
            components.remove(componentKey);
        } else {
            components.put(componentKey, new CompoundTag());
        }
        applyComponentsAndRefresh(components);
    }

    private void cycleGlintOverride() {
        CompoundTag components = currentComponents();
        Tag value = components.get("minecraft:enchantment_glint_override");
        if (value == null) {
            components.putBoolean("minecraft:enchantment_glint_override", true);
        } else if (value instanceof NumericTag numericTag && numericTag.byteValue() != 0) {
            components.putBoolean("minecraft:enchantment_glint_override", false);
        } else {
            components.remove("minecraft:enchantment_glint_override");
        }
        applyComponentsAndRefresh(components);
    }

    private void applyComponentsAndRefresh(CompoundTag components) {
        applyComponentsAndRefresh(components, "editor_components_applied");
    }

    private void applyComponentsAndRefresh(CompoundTag components, String feedbackMessageKey) {
        try {
            this.previewStack = parseStackWithComponents(this.previewStack, components);
            readMainFieldsFromStack(this.previewStack);
            syncNbtEditorValuesFromStack();
            this.nbtFeedbackGood = true;
            this.nbtFeedback = Component.translatable(messageKey(feedbackMessageKey)).getString();
            rebuildWidgets();
        } catch (RuntimeException exception) {
            showComponentError(exception);
        }
    }

    private void showComponentError(RuntimeException exception) {
        this.nbtFeedbackGood = false;
        this.nbtFeedback = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }

    private CompoundTag currentComponents() {
        CompoundTag saved = ItemStackNbt.save(this.previewStack);
        return NbtCompat.getCompound(saved, "components").copy();
    }

    private CompoundTag componentCompound(CompoundTag components, String key) {
        return NbtCompat.contains(components, key, Tag.TAG_COMPOUND) ? NbtCompat.getCompound(components, key) : new CompoundTag();
    }

    private CompoundTag firstToolRule(CompoundTag tool) {
        ListTag rules = NbtCompat.getList(tool, "rules", Tag.TAG_COMPOUND);
        return rules.isEmpty() ? new CompoundTag() : NbtCompat.getCompound(rules, 0);
    }

    private String numericComponentValue(CompoundTag components, String componentKey, String fallback) {
        Tag value = components.get(componentKey);
        if (value instanceof NumericTag numericTag) {
            Number number = numericTag.box();
            return number == null ? fallback : number.toString();
        }
        return fallback;
    }

    private String componentCompoundInt(CompoundTag components, String componentKey, String valueKey, String fallback) {
        CompoundTag component = componentCompound(components, componentKey);
        return component.isEmpty() ? fallback : Integer.toString(NbtCompat.getInt(component, valueKey));
    }

    private String componentString(CompoundTag tag, String key, String fallback) {
        return NbtCompat.contains(tag, key, Tag.TAG_STRING) ? NbtCompat.getString(tag, key) : fallback;
    }

    private boolean componentBoolean(CompoundTag tag, String key, boolean fallback) {
        return NbtCompat.contains(tag, key) ? NbtCompat.getBoolean(tag, key) : fallback;
    }

    private String componentFieldValue(String key, String fallback) {
        EditBox box = this.componentEditorBoxes.get(key);
        return box == null ? fallback : box.getValue();
    }

    private void putOptionalIntComponent(CompoundTag components, String componentKey, String fieldKey, int min, int max) {
        String value = componentFieldValue(fieldKey, "").trim();
        if (value.isEmpty()) {
            components.remove(componentKey);
            return;
        }
        components.putInt(componentKey, parseIntValue(value, min, max));
    }

    private void putEnchantableComponent(CompoundTag components) {
        String value = componentFieldValue("basic.enchantable", "").trim();
        if (value.isEmpty()) {
            components.remove("minecraft:enchantable");
            return;
        }
        CompoundTag enchantable = new CompoundTag();
        enchantable.putInt("value", parseIntValue(value, 1, 100000));
        components.put("minecraft:enchantable", enchantable);
    }

    private void applyDurabilityFields(CompoundTag components) {
        int maxDamage = parseIntField("quick.durability", "1000", 1, 999999);
        components.putInt("minecraft:max_stack_size", 1);
        components.putInt("minecraft:max_damage", maxDamage);
        components.putInt("minecraft:damage", 0);
    }

    private void putHolderSetComponent(CompoundTag components, String componentKey, String valueKey, String fieldKey) {
        putHolderSetComponentValue(components, componentKey, valueKey, componentFieldValue(fieldKey, ""));
    }

    private void putHolderSetComponentValue(CompoundTag components, String componentKey, String valueKey, String input) {
        String value = normalizeHolderSetInput(input);
        if (value.isEmpty()) {
            components.remove(componentKey);
            return;
        }
        CompoundTag component = new CompoundTag();
        component.putString(valueKey, value);
        components.put(componentKey, component);
    }

    private void putIntIfDifferent(CompoundTag tag, String key, int value, int defaultValue) {
        if (value != defaultValue) {
            tag.putInt(key, value);
        }
    }

    private void putFloatIfDifferent(CompoundTag tag, String key, float value, float defaultValue) {
        if (Math.abs(value - defaultValue) > 0.0001F) {
            tag.putFloat(key, value);
        }
    }

    private void putStringIfNotDefault(CompoundTag tag, String key, String value, String defaultValue) {
        if (!value.equals(defaultValue)) {
            tag.putString(key, value);
        }
    }

    private void putStringIfNotBlank(CompoundTag tag, String key, String value) {
        if (value != null && !value.isBlank()) {
            tag.putString(key, value.trim());
        }
    }

    private int parseIntField(String fieldKey, String fallback, int min, int max) {
        return parseIntValue(componentFieldValue(fieldKey, fallback).trim(), min, max);
    }

    private int parseIntValue(String value, int min, int max) {
        try {
            return Mth.clamp(Integer.parseInt(value), min, max);
        } catch (NumberFormatException exception) {
            throw invalidComponentValue(value);
        }
    }

    private float parseFloatField(String fieldKey, String fallback, float min, float max) {
        String value = componentFieldValue(fieldKey, fallback).trim();
        try {
            return Mth.clamp(Float.parseFloat(value), min, max);
        } catch (NumberFormatException exception) {
            throw invalidComponentValue(value);
        }
    }

    private boolean parseBooleanField(String fieldKey, boolean fallback) {
        return parseBooleanText(componentFieldValue(fieldKey, Boolean.toString(fallback)), fallback);
    }

    private boolean parseBooleanText(String value, boolean fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.equals("true") || normalized.equals("1") || normalized.equals("yes") || normalized.equals("on");
    }

    private String normalizeLowerField(String fieldKey, String fallback) {
        String value = componentFieldValue(fieldKey, fallback).trim().toLowerCase();
        return value.isEmpty() ? fallback : value;
    }

    private String normalizeResourceField(String fieldKey, String fallback) {
        String value = componentFieldValue(fieldKey, fallback).trim().toLowerCase();
        if (value.isEmpty()) {
            return "";
        }
        return value.contains(":") ? value : "minecraft:" + value;
    }

    private String normalizeHolderSetInput(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim().toLowerCase();
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("[") || trimmed.contains(":")) {
            return trimmed;
        }
        return "minecraft:" + trimmed;
    }

    private IllegalArgumentException invalidComponentValue(String value) {
        return new IllegalArgumentException(Component.translatable(messageKey("editor_invalid_component_value"), value).getString());
    }

    private void toggleBooleanField(String fieldKey, boolean fallback) {
        EditBox box = this.componentEditorBoxes.get(fieldKey);
        if (box != null) {
            box.setValue(Boolean.toString(!parseBooleanText(box.getValue(), fallback)));
        }
    }

    private void cycleTextField(String fieldKey, String[] values) {
        EditBox box = this.componentEditorBoxes.get(fieldKey);
        if (box == null || values.length == 0) {
            return;
        }
        String current = box.getValue().trim().toLowerCase();
        int index = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                index = (i + 1) % values.length;
                break;
            }
        }
        box.setValue(values[index]);
    }

    private Component getMarkerText(String componentKey, String translationPrefix) {
        return Component.translatable(key(translationPrefix + "." + (currentComponents().contains(componentKey) ? 1 : 0)));
    }

    private Component getGlintText(CompoundTag components) {
        Tag value = components.get("minecraft:enchantment_glint_override");
        int state = 0;
        if (value instanceof NumericTag numericTag) {
            state = numericTag.byteValue() == 0 ? 2 : 1;
        }
        return Component.translatable(key("components.glint." + state));
    }
}

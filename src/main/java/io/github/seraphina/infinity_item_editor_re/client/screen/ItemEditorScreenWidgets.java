package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Axis;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.CreativeTabRefresher;
import io.github.seraphina.infinity_item_editor_re.client.screen.legacy.LegacyTextEditBox;
import io.github.seraphina.infinity_item_editor_re.client.screen.modern.ModernTextEditBox;
import io.github.seraphina.infinity_item_editor_re.client.screen.modern.ModernUi;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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

@OnlyIn(Dist.CLIENT)
abstract class ItemEditorScreenWidgets extends ItemEditorScreenComponents {
    protected ItemEditorScreenWidgets(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(stack, targetContainerSlot, parentTradeScreen, parentTradeIndex, parentTradeSlot);
    }

protected void addItemPanel() {
        if (isSidebarUi()) {
            addSidebarItemPanel();
            return;
        }

        this.itemIdBox = addTrackedBox(plainTextBox(this.midX, 55, 75, FIELD_HEIGHT,
                Component.translatable(key("item.id"))));
        this.itemIdBox.setMaxLength(100);
        this.itemIdBox.setTextColor(MAIN_COLOR);
        this.itemIdBox.setValue(this.itemIdValue);
        this.itemIdBox.setResponder(value -> {
            this.itemIdValue = value;
            if (tryApplyItemId(false)) {
                rebuildWidgets();
            }
        });
        this.mainTextBoxes.add(this.itemIdBox);

        this.countBox = addTrackedBox(numberBox(this.midX, 85, 20, FIELD_HEIGHT, 2, this.countValue, 1, MAX_COUNT));
        this.countBox.setResponder(value -> {
            this.countValue = value;
            tryApplyCount(false);
        });

        int maxDamage = getDamageMaxForField(this.previewStack);
        int damageDigits = Math.max(1, Integer.toString(maxDamage).length());
        this.damageBox = addTrackedBox(numberBox(this.midX, 115, Math.max(10 * damageDigits, 15), FIELD_HEIGHT,
                damageDigits, this.damageValue, 0, maxDamage));
        this.damageBox.setResponder(value -> {
            this.damageValue = value;
            tryApplyDamage(false);
        });

        int editorButtonWidth = 72;
        int editorButtonGap = 4;
        int editorButtonLeft = this.midX - (editorButtonWidth * 4 + editorButtonGap * 3) / 2;
        addRenderableWidget(new InfinityEditorButton(editorButtonLeft, 145, editorButtonWidth, FIELD_HEIGHT,
                Component.translatable(key("nbt")), button -> switchPanel(Panel.NBT)));
        addRenderableWidget(new InfinityEditorButton(editorButtonLeft + editorButtonWidth + editorButtonGap, 145, editorButtonWidth, FIELD_HEIGHT,
                Component.translatable(key("components")), button -> switchPanel(Panel.COMPONENTS)));
        addRenderableWidget(new InfinityEditorButton(editorButtonLeft + (editorButtonWidth + editorButtonGap) * 2, 145, editorButtonWidth, FIELD_HEIGHT,
                Component.translatable(key("nbtadv")), button -> switchPanel(Panel.NBT_ADVANCED)));
        addRenderableWidget(new InfinityEditorButton(editorButtonLeft + (editorButtonWidth + editorButtonGap) * 3, 145, editorButtonWidth, FIELD_HEIGHT,
                Component.translatable(key("json")), button -> openJsonEditor()));
        addRenderableWidget(new InfinityEditorButton(this.width - 75, 74, 70, FIELD_HEIGHT,
                Component.translatable(key("hideflags")), button -> switchPanel(Panel.HIDE_FLAGS)));
        addRenderableWidget(new InfinityEditorButton(this.width / 8 - 40, this.midY - 45, 80, FIELD_HEIGHT,
                Component.translatable(key("pick")), button -> openItemPicker()));

        addSpecialButtons();
        addNameAndLoreWidgets();
        addFormatButtons();
    }

    protected void addSidebarItemPanel() {
        int detailsWidth = sidebarDetailsWidth();
        int detailsX = sidebarItemDetailsX(detailsWidth);
        int fieldX = sidebarItemFieldX();
        int fieldWidth = sidebarItemFieldWidth(detailsX);

        this.itemIdBox = addTrackedBox(plainTextBox(fieldX, sidebarItemIdY(), fieldWidth, FIELD_HEIGHT,
                Component.translatable(key("item.id"))));
        this.itemIdBox.setMaxLength(100);
        this.itemIdBox.setTextColor(MAIN_COLOR);
        this.itemIdBox.setValue(this.itemIdValue);
        this.itemIdBox.setResponder(value -> {
            this.itemIdValue = value;
            if (tryApplyItemId(false)) {
                rebuildWidgets();
            }
        });
        this.mainTextBoxes.add(this.itemIdBox);

        this.countBox = addTrackedBox(numberBox(fieldX, sidebarItemCountY(), 48, FIELD_HEIGHT, 2, this.countValue, 1, MAX_COUNT));
        this.countBox.setResponder(value -> {
            this.countValue = value;
            tryApplyCount(false);
        });

        int maxDamage = getDamageMaxForField(this.previewStack);
        int damageDigits = Math.max(1, Integer.toString(maxDamage).length());
        this.damageBox = addTrackedBox(numberBox(fieldX, sidebarItemDamageY(), Math.max(10 * damageDigits, 48), FIELD_HEIGHT,
                damageDigits, this.damageValue, 0, maxDamage));
        this.damageBox.setResponder(value -> {
            this.damageValue = value;
            tryApplyDamage(false);
        });

        int sidebarX = SIDEBAR_SAFE_MARGIN;
        int sidebarButtonWidth = sidebarWidth() - SIDEBAR_SAFE_MARGIN * 2;
        int y = 100;
        addRenderableWidget(new InfinityEditorButton(sidebarX, y, sidebarButtonWidth, FIELD_HEIGHT,
                Component.translatable(key("pick")), button -> openItemPicker()));
        y += 24;
        addRenderableWidget(new InfinityEditorButton(sidebarX, y, sidebarButtonWidth, FIELD_HEIGHT,
                Component.translatable(key("nbt")), button -> switchPanel(Panel.NBT)));
        y += 24;
        addRenderableWidget(new InfinityEditorButton(sidebarX, y, sidebarButtonWidth, FIELD_HEIGHT,
                Component.translatable(key("components")), button -> switchPanel(Panel.COMPONENTS)));
        y += 24;
        addRenderableWidget(new InfinityEditorButton(sidebarX, y, sidebarButtonWidth, FIELD_HEIGHT,
                Component.translatable(key("nbtadv")), button -> switchPanel(Panel.NBT_ADVANCED)));
        y += 24;
        addRenderableWidget(new InfinityEditorButton(sidebarX, y, sidebarButtonWidth, FIELD_HEIGHT,
                Component.translatable(key("json")), button -> openJsonEditor()));
        y += 24;
        if (isCommandBlockEditableItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(sidebarX, y, sidebarButtonWidth, FIELD_HEIGHT,
                    Component.translatable(key("commandblock")), button -> openCommandBlockEditor()));
            y += 24;
        }
        addRenderableWidget(new InfinityEditorButton(sidebarX, y, sidebarButtonWidth, FIELD_HEIGHT,
                Component.translatable(key("hideflags")), button -> switchPanel(Panel.HIDE_FLAGS)));
        y += 24;
        if (canShowEnchantingButton(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(sidebarX, y, sidebarButtonWidth, FIELD_HEIGHT,
                    Component.translatable(key("enchanting")), button -> switchPanel(Panel.ENCHANTMENTS)));
        }

        addSidebarNameAndLoreWidgets(detailsX, sidebarItemNameY(), detailsWidth);
        if (canShowSidebarActionGrid()) {
            addSidebarSpecialButtons(getActionGridX(), getActionGridY(), getActionGridButtonWidth());
        }
        addFormatButtons();
    }

    protected int sidebarDetailsWidth() {
        if (isCompactSidebarItemPanel()) {
            return contentWidth();
        }

        return Mth.clamp(contentWidth() / 3, 176, 236);
    }

    protected int sidebarDetailsX(int detailsWidth) {
        int x = safeRight() - detailsWidth;
        if (x < safeLeft() + 268) {
            return safeLeft();
        }

        return x;
    }

    protected void addSidebarNameAndLoreWidgets(int x, int y, int width) {
        int controlX = x + SIDEBAR_DRAWER_PADDING;
        int controlWidth = Math.max(1, width - SIDEBAR_DRAWER_PADDING * 2);
        int buttonGap = 4;
        int clearButtonWidth = Math.min(42, Math.max(20, controlWidth / 3));
        int nameFieldWidth = Math.max(1, controlWidth - clearButtonWidth - buttonGap);
        this.nameBox = addTrackedBox(legacyTextBox(controlX, y, nameFieldWidth, FIELD_HEIGHT,
                Component.translatable(key("item.name"))));
        this.nameBox.setMaxLength(100);
        this.nameBox.setTextColor(MAIN_COLOR);
        this.nameBox.setValue(this.nameValue);
        this.nameBox.setResponder(value -> {
            this.nameValue = value;
            applyNameToStack();
        });
        this.mainTextBoxes.add(this.nameBox);

        addRenderableWidget(new InfinityEditorButton(controlX + nameFieldWidth + buttonGap, y, clearButtonWidth, FIELD_HEIGHT,
                Component.translatable(key("clear")), button -> clearCustomName()));

        int visibleLoreLines = sidebarVisibleLoreLines();
        for (int i = 0; i < visibleLoreLines; i++) {
            boolean realLine = i < this.loreValues.size();
            addSidebarLoreTextField(controlX, y + 50 + 26 * i, controlWidth, i, realLine);
        }

        if (canShowSidebarLoreButton()) {
            int loreButtonY = visibleLoreLines == 0 ? y + 28 : y + 50 + 26 * visibleLoreLines + 6;
            addRenderableWidget(new InfinityEditorButton(controlX, loreButtonY, controlWidth, FIELD_HEIGHT,
                    Component.translatable(key("lore")), button -> switchPanel(Panel.LORE)));
        }
    }

    protected void addSidebarLoreTextField(int x, int y, int width, int line, boolean realLine) {
        int fieldX = realLine ? x + 18 : x;
        int fieldWidth = realLine ? width - 18 : width;
        EditBox loreBox = addTrackedBox(legacyTextBox(fieldX, y, fieldWidth, FIELD_HEIGHT,
                Component.literal("Lore " + (line + 1))));
        loreBox.setMaxLength(100);
        loreBox.setTextColor(MAIN_COLOR);
        loreBox.setValue(realLine ? this.loreValues.get(line) : "");
        loreBox.setResponder(value -> {
            if (!realLine && value.isEmpty()) {
                return;
            }
            setLoreLine(line, value);
            applyLoreToStack();
            if (!realLine && line < 5) {
                rebuildWidgets();
            }
        });
        this.loreBoxes.add(loreBox);

        if (realLine) {
            addRenderableWidget(new InfinityEditorButton(x, y, 16, FIELD_HEIGHT,
                    Component.literal(ChatFormatting.DARK_RED + "X"), button -> {
                removeLoreLine(line);
                rebuildWidgets();
            }));
        }
    }

    protected int getActionGridX() {
        return safeLeft() + SIDEBAR_DRAWER_PADDING;
    }

    protected int getActionGridButtonWidth() {
        int available;
        int preferredMin;
        if (isCompactSidebarItemPanel()) {
            available = contentWidth();
            preferredMin = 64;
        } else {
            int detailsX = sidebarItemDetailsX(sidebarDetailsWidth());
            available = detailsX - safeLeft() - SIDEBAR_CONTENT_GAP;
            preferredMin = 74;
        }

        available = Math.max(1, available - SIDEBAR_DRAWER_PADDING * 2);
        int maxColumnWidth = Math.max(1, (available - SIDEBAR_CONTENT_GAP) / 2);
        int minColumnWidth = Math.min(preferredMin, maxColumnWidth);
        return Mth.clamp(maxColumnWidth, minColumnWidth, 126);
    }

    protected void addSidebarSpecialButtons(int x, int y, int width) {
        int index = 0;
        index = addSidebarActionButton(x, y, width, index, getUnbreakableText(), button -> toggleUnbreakable(), !this.previewStack.isEmpty());

        if (!this.previewStack.isEmpty()) {
            index = addSidebarActionButton(x, y, width, index,
                    Component.translatable(key("attributes")), button -> switchPanel(Panel.ATTRIBUTES));
        }

        if (isColorApplicable(this.previewStack)) {
            index = addSidebarActionButton(x, y, width, index,
                    Component.translatable(key("color")), button -> switchPanel(Panel.COLOR));
        }

        if (isSignItem(this.previewStack)) {
            index = addSidebarActionButton(x, y, width, index,
                    Component.translatable(key("sign")), button -> switchPanel(Panel.SIGN));
        }

        if (isPlayerHeadItem(this.previewStack)) {
            index = addSidebarActionButton(x, y, width, index,
                    Component.translatable(key("head")), button -> switchPanel(Panel.HEAD));
        }

        if (isArmorStandItem(this.previewStack)) {
            index = addSidebarActionButton(x, y, width, index,
                    Component.translatable(key("armorstand")), button -> switchPanel(Panel.ARMOR_STAND));
        }

        if (isFireworkEditableItem(this.previewStack)) {
            index = addSidebarActionButton(x, y, width, index,
                    Component.translatable(key("firework")), button -> switchPanel(Panel.FIREWORK));
        }

        if (isContainerEditableItem(this.previewStack)) {
            index = addSidebarActionButton(x, y, width, index,
                    Component.translatable(key("container")), button -> openContainerItemEditor());
        }

        if (isBannerEditableItem(this.previewStack)) {
            index = addSidebarActionButton(x, y, width, index,
                    Component.translatable(key("banner")), button -> switchPanel(Panel.BANNER));
        }

        if (isDecoratedPotItem(this.previewStack)) {
            index = addSidebarActionButton(x, y, width, index,
                    Component.translatable(key("decorated_pot")), button -> switchPanel(Panel.DECORATED_POT));
        }

        if (isSpawnEditorItem(this.previewStack)) {
            index = addSidebarActionButton(x, y, width, index,
                    Component.translatable(key(getSpawnEditorTitleKey())), button -> switchPanel(Panel.SPAWN_EGG));
        }

        if (isVillagerTradeEditableItem(this.previewStack)) {
            index = addSidebarActionButton(x, y, width, index,
                    Component.translatable(key("trades")), button -> switchPanel(Panel.TRADES));
        }

        if (isPotionItem(this.previewStack)) {
            index = addSidebarActionButton(x, y, width, index,
                    Component.translatable(key("potion")), button -> switchPanel(Panel.POTION));
        }

        if (isBookEditableItem(this.previewStack)) {
            index = addSidebarActionButton(x, y, width, index,
                    Component.translatable(key("book")), button -> switchPanel(Panel.BOOK));
            addSidebarActionButton(x, y, width, index,
                    Component.translatable(key("book.edit_pages")), button -> openBookItemEditor());
        }
    }

    protected int addSidebarActionButton(int startX, int startY, int width, int index,
                                         Component text, InfinityEditorButton.PressAction action) {
        return addSidebarActionButton(startX, startY, width, index, text, action, true);
    }

    protected int addSidebarActionButton(int startX, int startY, int width, int index,
                                         Component text, InfinityEditorButton.PressAction action, boolean active) {
        int column = index % 2;
        int row = index / 2;
        int buttonX = startX + column * (width + SIDEBAR_CONTENT_GAP);
        int buttonY = startY + row * (SIDEBAR_BUTTON_HEIGHT + 6);
        if (buttonY + SIDEBAR_BUTTON_HEIGHT > sidebarBottomButtonY() - 8) {
            return index;
        }

        InfinityEditorButton button = addRenderableWidget(new InfinityEditorButton(buttonX, buttonY, width, SIDEBAR_BUTTON_HEIGHT, text, action));
        button.active = active;

        return index + 1;
    }

    protected void addNameAndLoreWidgets() {
        this.nameBox = addTrackedBox(legacyTextBox(this.width - 180, 50, 130, FIELD_HEIGHT,
                Component.translatable(key("item.name"))));
        this.nameBox.setMaxLength(100);
        this.nameBox.setTextColor(MAIN_COLOR);
        this.nameBox.setValue(this.nameValue);
        this.nameBox.setResponder(value -> {
            this.nameValue = value;
            applyNameToStack();
        });
        this.mainTextBoxes.add(this.nameBox);

        addRenderableWidget(new InfinityEditorButton(this.width - 45, 50, 40, FIELD_HEIGHT,
                Component.translatable(key("clear")), button -> clearCustomName()));

        int visibleLoreLines = Math.min(5, this.loreValues.size() + 1);
        for (int i = 0; i < visibleLoreLines; i++) {
            boolean realLine = i < this.loreValues.size();
            addLoreTextField(i, realLine);
        }

        addRenderableWidget(new InfinityEditorButton(this.width - 180, 100 + 30 * visibleLoreLines, 170, FIELD_HEIGHT,
                Component.translatable(key("lore")), button -> switchPanel(Panel.LORE)));
    }

    protected void addLoreTextField(int line, boolean realLine) {
        EditBox loreBox = addTrackedBox(legacyTextBox(this.width - 180, 100 + 30 * line, 170, FIELD_HEIGHT,
                Component.literal("Lore " + (line + 1))));
        loreBox.setMaxLength(100);
        loreBox.setTextColor(MAIN_COLOR);
        loreBox.setValue(realLine ? this.loreValues.get(line) : "");
        loreBox.setResponder(value -> {
            if (!realLine && value.isEmpty()) {
                return;
            }
            setLoreLine(line, value);
            applyLoreToStack();
            if (!realLine && line < 4) {
                rebuildWidgets();
            }
        });
        this.loreBoxes.add(loreBox);

        if (realLine) {
            addRenderableWidget(new InfinityEditorButton(this.width - 195, 100 + 30 * line, 14, FIELD_HEIGHT,
                    Component.literal(ChatFormatting.DARK_RED + "X"), button -> {
                removeLoreLine(line);
                rebuildWidgets();
            }));
        }
    }

    protected void addSpecialButtons() {
        int y = 175;
        if (!this.previewStack.isEmpty()) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    getUnbreakableText(), button -> toggleUnbreakable()));
            y += 30;
        }

        if (!this.previewStack.isEmpty()) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("attributes")), button -> switchPanel(Panel.ATTRIBUTES)));
            y += 30;
        }

        if (isColorApplicable(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("color")), button -> switchPanel(Panel.COLOR)));
            y += 30;
        }

        if (isSignItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("sign")), button -> switchPanel(Panel.SIGN)));
            y += 30;
        }

        if (isPlayerHeadItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("head")), button -> switchPanel(Panel.HEAD)));
            y += 30;
        }

        if (isArmorStandItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("armorstand")), button -> switchPanel(Panel.ARMOR_STAND)));
            y += 30;
        }

        if (isFireworkEditableItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("firework")), button -> switchPanel(Panel.FIREWORK)));
            y += 30;
        }

        if (isContainerEditableItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("container")), button -> openContainerItemEditor()));
            y += 30;
        }

        if (isCommandBlockEditableItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("commandblock")), button -> openCommandBlockEditor()));
            y += 30;
        }

        if (isBannerEditableItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("banner")), button -> switchPanel(Panel.BANNER)));
            y += 30;
        }

        if (isDecoratedPotItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("decorated_pot")), button -> switchPanel(Panel.DECORATED_POT)));
            y += 30;
        }

        if (isSpawnEditorItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key(getSpawnEditorTitleKey())), button -> switchPanel(Panel.SPAWN_EGG)));
            y += 30;
        }

        if (isVillagerTradeEditableItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("trades")), button -> switchPanel(Panel.TRADES)));
            y += 30;
        }

        if (canShowEnchantingButton(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("enchanting")), button -> switchPanel(Panel.ENCHANTMENTS)));
            y += 30;
        }

        if (isPotionItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("potion")), button -> switchPanel(Panel.POTION)));
            y += 30;
        }

        if (isBookEditableItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("book")), button -> switchPanel(Panel.BOOK)));
            y += 30;
            addRenderableWidget(new InfinityEditorButton(this.midX - 50, y, 100, FIELD_HEIGHT,
                    Component.translatable(key("book.edit_pages")), button -> openBookItemEditor()));
        }
    }

    protected void addFormatButtons() {
        if (isSidebarUi()) {
            addSidebarFormatButtons();
            return;
        }

        ChatFormatting[] formats = ChatFormatting.values();
        int colorAmount = 2 + formats.length;
        int columns = colorAmount / 2;
        int startX = this.width - 1 - 13 * ((colorAmount + 2) / 2);

        addRenderableWidget(new InfinityEditorButton(startX + 13, this.height - 30, 13, 15,
                Component.literal(String.valueOf(ChatFormatting.PREFIX_CODE)), button -> insertFormattingPrefix()));
        addRenderableWidget(new InfinityEditorButton(startX + 26, this.height - 30, 13, 15,
                Component.literal(ChatFormatting.DARK_RED + "%"), button -> stripFocusedFormatting()));

        for (int i = 2; i < colorAmount; i++) {
            ChatFormatting format = formats[i - 2];
            int x = startX + 13 * ((i % columns) + 1);
            int y = this.height - 30 + 15 * (i / columns);
            addRenderableWidget(new InfinityEditorButton(x, y, 13, 15,
                    Component.literal(format.toString() + format.getChar()), button -> insertFocusedText(format.toString())));
        }
    }

    protected void addSidebarFormatButtons() {
        ChatFormatting[] formats = ChatFormatting.values();
        int columns = 8;
        int buttonWidth = 13;
        int buttonHeight = 15;
        int colorAmount = 2 + formats.length;
        int rows = (colorAmount + columns - 1) / columns;
        int paletteWidth = columns * buttonWidth;
        int paletteHeight = rows * buttonHeight;
        int startY = Math.max(82, this.height - 38 - paletteHeight);
        int startX = Math.max(4, (sidebarWidth() - paletteWidth) / 2);
        addSidebarFormatButton(startX, startY, columns, 0,
                Component.literal(String.valueOf(ChatFormatting.PREFIX_CODE)), button -> insertFormattingPrefix());
        addSidebarFormatButton(startX, startY, columns, 1,
                Component.literal(ChatFormatting.DARK_RED + "%"), button -> stripFocusedFormatting());

        for (int i = 2; i < colorAmount; i++) {
            ChatFormatting format = formats[i - 2];
            addSidebarFormatButton(startX, startY, columns, i,
                    Component.literal(format.toString() + format.getChar()), button -> insertFocusedText(format.toString()));
        }
    }

    protected void addSidebarFormatButton(int startX, int startY, int columns, int index,
                                          Component text, InfinityEditorButton.PressAction action) {
        int x = startX + 13 * (index % columns);
        int y = startY + 15 * (index / columns);
        addRenderableWidget(new InfinityEditorButton(x, y, 13, 15, text, action));
    }

    protected void addNbtPanel() {
        int boxWidth = nbtEditorWidth();
        this.rawNbtBox = addTrackedBox(legacyTextBox(nbtEditorX(), nbtEditorBoxY(), boxWidth, FIELD_HEIGHT,
                Component.translatable(key("nbt"))));
        this.rawNbtBox.setMaxLength(20000);
        this.rawNbtBox.setTextColor(MAIN_COLOR);
        this.rawNbtBox.setValue(this.rawNbtValue == null ? getInitialNbt(this.previewStack) : this.rawNbtValue);
        this.rawNbtBox.setResponder(value -> this.rawNbtValue = value);

        int buttonWidth = nbtEditorButtonWidth();
        addRenderableWidget(new InfinityEditorButton(nbtEditorButtonX(buttonWidth), nbtEditorButtonY(), buttonWidth, SIDEBAR_BUTTON_HEIGHT,
                Component.translatable(key("nbt.update")), button -> updateRawNbt()));
        addFormatButtons();
    }

    protected void addComponentsPanel() {
        addComponentEditorPanel();
    }

    protected void addNbtAdvancedPanel() {
        this.advancedScroll = Mth.clamp(this.advancedScroll, 0, Math.max(0, buildNbtRows().size() - getNbtAdvancedVisibleRows()));
    }

    protected void addHideFlagsPanel() {
        for (int i = 0; i < HideFlag.values().length; i++) {
            HideFlag flag = HideFlag.values()[i];
            addRenderableWidget(new InfinityEditorButton(this.midX - 60, 60 + 26 * i, 120, FIELD_HEIGHT,
                    getHideFlagText(flag), button -> {
                toggleHideFlag(flag);
                rebuildWidgets();
            }));
        }
    }

    protected void addEnchantmentsPanel() {
        this.enchantFilterBox = addTrackedBox(plainTextBox(searchFilterX(), searchFilterY(), searchFilterWidth(), 18,
                Component.translatable(key("enchantment_filter"))));
        this.enchantFilterBox.setMaxLength(20);
        this.enchantFilterBox.setFilter(value -> value.matches("[a-z]*"));
        this.enchantFilterBox.setTextColor(MAIN_COLOR);
        this.enchantFilterBox.setValue(this.enchantFilterValue);
        this.enchantFilterBox.setResponder(value -> {
            this.enchantFilterValue = value.toLowerCase(Locale.ROOT);
        });

        int controlLeft = editorControlLeft();
        this.enchantLevelBox = addTrackedBox(numberBox(controlLeft, this.height - 33, 40, 18, 5,
                this.enchantLevelValue, 1, MAX_ENCHANTMENT_LEVEL));
        this.enchantLevelBox.setResponder(value -> this.enchantLevelValue = value);

        addRenderableWidget(new InfinityEditorButton(controlLeft, this.height - 63, 90, OLD_BUTTON_HEIGHT,
                Component.translatable(key("enchanting.enchanttoggle." + (this.showAllEnchantments ? 0 : 1))),
                button -> toggleEnchantmentsScope()));
    }

    protected void addPotionPanel() {
        this.potionFilterBox = addTrackedBox(plainTextBox(searchFilterX(), searchFilterY(), searchFilterWidth(), 18,
                Component.translatable(key("potion_filter"))));
        this.potionFilterBox.setMaxLength(20);
        this.potionFilterBox.setFilter(value -> value.matches("[a-z]*"));
        this.potionFilterBox.setTextColor(MAIN_COLOR);
        this.potionFilterBox.setValue(this.potionFilterValue);
        this.potionFilterBox.setResponder(value -> this.potionFilterValue = value.toLowerCase(Locale.ROOT));

        int controlLeft = editorControlLeft();
        this.potionLevelBox = addTrackedBox(numberBox(controlLeft, this.height - 33, 40, 18, 3,
                this.potionLevelValue, 1, MAX_POTION_LEVEL));
        this.potionLevelBox.setResponder(value -> this.potionLevelValue = value);

        this.potionTimeBox = addTrackedBox(numberBox(controlLeft, this.height - 60, 40, 18, 5,
                this.potionTimeValue, 1, MAX_POTION_SECONDS));
        this.potionTimeBox.setResponder(value -> this.potionTimeValue = value);

        addRenderableWidget(new InfinityEditorButton(controlLeft, this.height - 120, 80, OLD_BUTTON_HEIGHT,
                Component.translatable(key("color")), button -> switchPanel(Panel.COLOR)));
        addRenderableWidget(new InfinityEditorButton(controlLeft, this.height - 90, 80, OLD_BUTTON_HEIGHT,
                Component.translatable(key("potion.showparticles." + (this.showPotionParticles ? 1 : 0))),
                button -> togglePotionParticles()));
    }

    protected void addSignPanel() {
        int fieldWidth = contentLimitedWidth(220, 120, 80);
        int x = centeredContentX(fieldWidth);

        for (int i = 0; i < SIGN_LINES; i++) {
            int line = i;
            EditBox lineBox = addTrackedBox(legacyTextBox(x, 60 + 30 * i, fieldWidth, FIELD_HEIGHT,
                    Component.translatable(key("sign.line"), line + 1)));
            lineBox.setMaxLength(384);
            lineBox.setValue(getSignLineValue(line));
            lineBox.setResponder(value -> {
                this.signLineValues[line] = value;
                applySignToStack();
            });
            this.signBoxes.add(lineBox);
        }

        int commandWidth = contentLimitedWidth(260, 120, 60);
        this.signCommandBox = addTrackedBox(legacyTextBox(centeredContentX(commandWidth), 190, commandWidth, FIELD_HEIGHT,
                Component.translatable(key("sign.command"))));
        this.signCommandBox.setMaxLength(512);
        this.signCommandBox.setValue(this.signCommandValue == null ? "" : this.signCommandValue);
        this.signCommandBox.setResponder(value -> {
            this.signCommandValue = value;
            applySignToStack();
        });
        this.signBoxes.add(this.signCommandBox);

        addFormatButtons();
    }

    protected void addBookPanel() {
        boolean written = this.previewStack.is(Items.WRITTEN_BOOK);
        int fieldWidth = contentLimitedWidth(220, 120, 80);
        int x = centeredContentX(fieldWidth);

        this.bookTitleBox = addTrackedBox(legacyTextBox(x, 70, fieldWidth, FIELD_HEIGHT,
                Component.translatable(key("book.title"))));
        this.bookTitleBox.setMaxLength(100);
        this.bookTitleBox.setValue(this.bookTitleValue == null ? "" : this.bookTitleValue);
        this.bookTitleBox.setResponder(value -> {
            this.bookTitleValue = value;
            applyBookMetadataToStack();
        });
        this.mainTextBoxes.add(this.bookTitleBox);

        this.bookAuthorBox = addTrackedBox(legacyTextBox(x, 100, fieldWidth, FIELD_HEIGHT,
                Component.translatable(key("book.author"))));
        this.bookAuthorBox.setMaxLength(100);
        this.bookAuthorBox.setValue(this.bookAuthorValue == null ? "" : this.bookAuthorValue);
        this.bookAuthorBox.setResponder(value -> {
            this.bookAuthorValue = value;
            applyBookMetadataToStack();
        });
        this.mainTextBoxes.add(this.bookAuthorBox);

        int buttonWidth = contentLimitedWidth(150, 100, 32);
        int buttonX = centeredContentX(buttonWidth);
        InfinityEditorButton generation = addRenderableWidget(new InfinityEditorButton(buttonX, 140, buttonWidth, FIELD_HEIGHT,
                Component.translatable(key("book.generation"), getBookGeneration()), button -> cycleBookGeneration()));
        generation.active = written;

        InfinityEditorButton resolved = addRenderableWidget(new InfinityEditorButton(buttonX, 170, buttonWidth, FIELD_HEIGHT,
                getBookResolvedText(), button -> toggleBookResolved()));
        resolved.active = written;

        addRenderableWidget(new InfinityEditorButton(buttonX, 200, buttonWidth, FIELD_HEIGHT,
                getBookSignButtonText(), button -> toggleBookSignedState()));

        addRenderableWidget(new InfinityEditorButton(buttonX, 230, buttonWidth, FIELD_HEIGHT,
                Component.translatable(key("book.edit_pages")), button -> openBookItemEditor()));

        addFormatButtons();
    }

    protected void addHeadPanel() {
        int fieldWidth = contentLimitedWidth(320, 180, 80);
        int x = centeredContentX(fieldWidth);

        this.headOwnerBox = addTrackedBox(legacyTextBox(x, 64, fieldWidth, FIELD_HEIGHT,
                Component.translatable(key("head.owner"))));
        this.headOwnerBox.setMaxLength(64);
        this.headOwnerBox.setValue(this.headOwnerValue == null ? "" : this.headOwnerValue);
        this.headOwnerBox.setResponder(value -> {
            this.headOwnerValue = value;
            applyHeadToStack();
        });
        this.mainTextBoxes.add(this.headOwnerBox);

        this.headUuidBox = addTrackedBox(legacyTextBox(x, 94, fieldWidth, FIELD_HEIGHT,
                Component.translatable(key("head.uuid"))));
        this.headUuidBox.setMaxLength(36);
        this.headUuidBox.setValue(this.headUuidValue == null ? "" : this.headUuidValue);
        this.headUuidBox.setResponder(value -> {
            this.headUuidValue = value;
            applyHeadToStack();
        });
        this.mainTextBoxes.add(this.headUuidBox);

        this.headTextureBox = addTrackedBox(legacyTextBox(x, 124, fieldWidth, FIELD_HEIGHT,
                Component.translatable(key("head.texture"))));
        this.headTextureBox.setMaxLength(4096);
        this.headTextureBox.setValue(this.headTextureValue == null ? "" : this.headTextureValue);
        this.headTextureBox.setResponder(value -> {
            this.headTextureValue = value;
            applyHeadToStack();
        });
        this.mainTextBoxes.add(this.headTextureBox);

        this.headTextureSignatureBox = addTrackedBox(legacyTextBox(x, 154, fieldWidth, FIELD_HEIGHT,
                Component.translatable(key("head.signature"))));
        this.headTextureSignatureBox.setMaxLength(4096);
        this.headTextureSignatureBox.setValue(this.headTextureSignatureValue == null ? "" : this.headTextureSignatureValue);
        this.headTextureSignatureBox.setResponder(value -> {
            this.headTextureSignatureValue = value;
            applyHeadToStack();
        });
        this.mainTextBoxes.add(this.headTextureSignatureBox);

        int buttonGap = 8;
        int buttonAreaWidth = contentLimitedWidth(208, 120, 20);
        int buttonWidth = Math.max(1, (buttonAreaWidth - buttonGap) / 2);
        int buttonX = centeredContentX(buttonAreaWidth);
        addRenderableWidget(new InfinityEditorButton(buttonX, 190, buttonWidth, FIELD_HEIGHT,
                Component.translatable(key("head.random_uuid")), button -> randomizeHeadUuid()));
        addRenderableWidget(new InfinityEditorButton(buttonX + buttonWidth + buttonGap, 190, buttonWidth, FIELD_HEIGHT,
                Component.translatable(key("head.clear_owner")), button -> clearHeadOwner()));
    }

    protected void addArmorStandPanel() {
        int width = contentLimitedWidth(150, 100, 32);
        int x = centeredContentX(width);
        int y = 54;
        addArmorStandToggleButton(x, y, width, "show_arms", ARMOR_STAND_SHOW_ARMS_TAG);
        addArmorStandToggleButton(x, y + 26, width, "small", ARMOR_STAND_SMALL_TAG);
        addArmorStandToggleButton(x, y + 52, width, "invisible", ARMOR_STAND_INVISIBLE_TAG);
        addArmorStandToggleButton(x, y + 78, width, "no_base_plate", ARMOR_STAND_NO_BASE_PLATE_TAG);
        addArmorStandToggleButton(x, y + 104, width, "marker", ARMOR_STAND_MARKER_TAG);
        addArmorStandToggleButton(x, y + 130, width, "no_gravity", ARMOR_STAND_NO_GRAVITY_TAG);
        addArmorStandToggleButton(x, y + 156, width, "invulnerable", ARMOR_STAND_INVULNERABLE_TAG);
        addRenderableWidget(new InfinityEditorButton(x, y + 190, width, FIELD_HEIGHT,
                Component.translatable(key("armorstand.clear_entity_tag")), button -> clearArmorStandEntityTag()));
    }

    protected void addArmorStandToggleButton(int x, int y, int width, String translationSuffix, String tagKey) {
        addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                getArmorStandToggleText(translationSuffix, tagKey), button -> toggleArmorStandFlag(tagKey, translationSuffix)));
    }

    protected void addFireworkPanel() {
        int width = contentLimitedWidth(156, 104, 32);
        int x = centeredContentX(width);
        int y = 52;

        if (this.previewStack.is(Items.FIREWORK_ROCKET)) {
            addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                    Component.translatable(key("firework.flight"), getFireworkFlight()), button -> cycleFireworkFlight()));
            y += 26;
        }

        addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                Component.translatable(key("firework.type"), getFireworkTypeName(this.fireworkExplosionType)),
                button -> cycleFireworkExplosionType(Screen.hasShiftDown() ? -1 : 1)));
        y += 26;
        addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                Component.translatable(key("firework.flicker." + (this.fireworkFlicker ? 1 : 0))),
                button -> toggleFireworkFlicker()));
        y += 26;
        addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                Component.translatable(key("firework.trail." + (this.fireworkTrail ? 1 : 0))),
                button -> toggleFireworkTrail()));
        y += 26;
        addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                Component.translatable(key("firework.color"), getDyeColorName(getFireworkDyeColor(this.fireworkColor))),
                button -> cycleFireworkColor(false, Screen.hasShiftDown() ? -1 : 1)));
        y += 26;
        addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                Component.translatable(key("firework.fade_color"), getFireworkFadeColorText()),
                button -> cycleFireworkColor(true, Screen.hasShiftDown() ? -1 : 1)));
        y += 26;
        addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                Component.translatable(key("firework.random_colors")), button -> randomizeFireworkColors()));
        y += 26;

        if (this.previewStack.is(Items.FIREWORK_ROCKET)) {
            addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                    Component.translatable(key("firework.add_explosion")), button -> addFireworkExplosion()));
            y += 26;
            InfinityEditorButton remove = addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                    Component.translatable(key("firework.remove_explosion")), button -> removeLastFireworkExplosion()));
            remove.active = getFireworkExplosionCount() > 0;
            y += 26;
            InfinityEditorButton clear = addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                    Component.translatable(key("firework.clear_fireworks")), button -> clearFireworkData()));
            clear.active = hasFireworkData();
        } else {
            InfinityEditorButton clear = addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT,
                    Component.translatable(key("firework.clear_explosion")), button -> clearFireworkData()));
            clear.active = hasFireworkData();
        }
    }

    protected void addContainerPanel() {
        this.selectedContainerSlot = Mth.clamp(this.selectedContainerSlot, 0, CONTAINER_SIZE - 1);
        int boxWidth = contentLimitedWidth(300, 180, 20);
        this.containerSlotNbtBox = addTrackedBox(legacyTextBox(centeredContentX(boxWidth), 132, boxWidth, FIELD_HEIGHT,
                Component.translatable(key("container.slot_nbt"))));
        this.containerSlotNbtBox.setMaxLength(20000);
        this.containerSlotNbtBox.setValue(this.containerSlotNbtValue == null ? getContainerSelectedSlotNbt() : this.containerSlotNbtValue);
        this.containerSlotNbtBox.setResponder(value -> this.containerSlotNbtValue = value);

        int controlsWidth = contentLimitedWidth(270, 160, 20);
        int x = centeredContentX(controlsWidth);
        int gap = isSidebarUi() ? 4 : 4;
        int smallWidth = isSidebarUi() ? 20 : 24;
        int remainingWidth = Math.max(1, controlsWidth - smallWidth * 2 - gap * 4);
        int updateWidth = isSidebarUi() ? Math.max(1, remainingWidth * 84 / 204) : 84;
        int clearWidth = isSidebarUi() ? Math.max(1, remainingWidth * 58 / 204) : 58;
        int clearAllWidth = isSidebarUi() ? Math.max(1, remainingWidth - updateWidth - clearWidth) : 62;

        addRenderableWidget(new InfinityEditorButton(x, 158, smallWidth, FIELD_HEIGHT,
                Component.literal("<"), button -> cycleContainerSlot(-1)));
        addRenderableWidget(new InfinityEditorButton(x + smallWidth + gap, 158, smallWidth, FIELD_HEIGHT,
                Component.literal(">"), button -> cycleContainerSlot(1)));
        int updateX = x + smallWidth * 2 + gap * 2;
        addRenderableWidget(new InfinityEditorButton(updateX, 158, updateWidth, FIELD_HEIGHT,
                Component.translatable(key("container.update_slot")), button -> updateContainerSlotFromNbt()));
        int clearX = updateX + updateWidth + gap;
        addRenderableWidget(new InfinityEditorButton(clearX, 158, clearWidth, FIELD_HEIGHT,
                Component.translatable(key("container.clear_slot")), button -> clearContainerSlot()));
        InfinityEditorButton clearAll = addRenderableWidget(new InfinityEditorButton(clearX + clearWidth + gap, 158, clearAllWidth, FIELD_HEIGHT,
                Component.translatable(key("container.clear_all")), button -> clearContainerItems()));
        clearAll.active = getContainerItemCount() > 0;
    }

    protected void addBannerPanel() {
        int listX = bannerPatternListX();
        int listWidth = bannerPatternListWidth();
        this.bannerPatternFilterBox = addTrackedBox(legacyTextBox(listX, sideListSearchY(), Math.min(160, listWidth), FIELD_HEIGHT,
                Component.translatable(key("banner.search"))));
        this.bannerPatternFilterBox.setMaxLength(32);
        this.bannerPatternFilterBox.setValue(this.bannerPatternFilterValue);
        this.bannerPatternFilterBox.setResponder(value -> {
            this.bannerPatternFilterValue = value.toLowerCase(Locale.ROOT);
            this.bannerPatternScroll = 0;
            this.selectedBannerPatternIndex = 0;
        });

        int width = isSidebarUi() ? contentLimitedWidth(132, 88, 20) : 132;
        int controlsX = rightControlsX(width, listX, listWidth);
        addRenderableWidget(new InfinityEditorButton(controlsX, 52, width, FIELD_HEIGHT,
                Component.translatable(key("banner.base"), getDyeColorName(getBannerBaseColor())),
                button -> cycleBannerBaseColor(Screen.hasShiftDown() ? -1 : 1)));
        addRenderableWidget(new InfinityEditorButton(controlsX, 78, width, FIELD_HEIGHT,
                Component.translatable(key("banner.pattern_color"), getDyeColorName(getBannerPatternColor())),
                button -> cycleBannerPatternColor(Screen.hasShiftDown() ? -1 : 1)));
        addRenderableWidget(new InfinityEditorButton(controlsX, 104, width, FIELD_HEIGHT,
                Component.translatable(key("banner.swap")), button -> swapBannerAndShield()));
        addRenderableWidget(new InfinityEditorButton(controlsX, 130, width, FIELD_HEIGHT,
                Component.translatable(key("banner.add")), button -> addSelectedBannerPattern()));

        InfinityEditorButton remove = addRenderableWidget(new InfinityEditorButton(controlsX, 156, width, FIELD_HEIGHT,
                Component.translatable(key("banner.remove")), button -> removeLastBannerPattern()));
        remove.active = getBannerPatternCount() > 0;

        InfinityEditorButton clear = addRenderableWidget(new InfinityEditorButton(controlsX, 182, width, FIELD_HEIGHT,
                Component.translatable(key("banner.clear")), button -> clearBannerPatterns()));
        clear.active = getBannerPatternCount() > 0;

        if (!isSidebarUi()) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 58, this.height - 64, 28, FIELD_HEIGHT,
                    Component.literal("<"), button -> cycleSelectedBannerPattern(-1)));
            addRenderableWidget(new InfinityEditorButton(this.midX - 28, this.height - 64, 56, FIELD_HEIGHT,
                    Component.translatable(key("banner.add")), button -> addSelectedBannerPattern()));
            addRenderableWidget(new InfinityEditorButton(this.midX + 30, this.height - 64, 28, FIELD_HEIGHT,
                    Component.literal(">"), button -> cycleSelectedBannerPattern(1)));
        }
    }

    protected void addDecoratedPotPanel() {
        int listX = potterySherdListX();
        int listWidth = potterySherdListWidth();
        this.potterySherdFilterBox = addTrackedBox(legacyTextBox(listX, sideListSearchY(), Math.min(180, listWidth), FIELD_HEIGHT,
                Component.translatable(key("decorated_pot.search"))));
        this.potterySherdFilterBox.setMaxLength(48);
        this.potterySherdFilterBox.setValue(this.potterySherdFilterValue);
        this.potterySherdFilterBox.setResponder(value -> {
            this.potterySherdFilterValue = value.toLowerCase(Locale.ROOT);
            this.potterySherdScroll = 0;
            this.selectedPotterySherdIndex = 0;
        });

        int width = isSidebarUi() ? contentLimitedWidth(132, 88, 20) : 132;
        int controlsX = rightControlsX(width, listX, listWidth);
        int sideButtonWidth = (width - 4) / 2;
        for (int i = 0; i < DECORATED_POT_DISPLAY_SIDES.length; i++) {
            int side = DECORATED_POT_DISPLAY_SIDES[i];
            int x = controlsX + (i % 2) * (sideButtonWidth + 4);
            int y = 52 + (i / 2) * 26;
            addDecoratedPotSideButton(x, y, sideButtonWidth, side);
        }

        addRenderableWidget(new InfinityEditorButton(controlsX, 104, width, FIELD_HEIGHT,
                Component.translatable(key("decorated_pot.apply")), button -> applySelectedPotterySherd()));
        addRenderableWidget(new InfinityEditorButton(controlsX, 130, width, FIELD_HEIGHT,
                Component.translatable(key("decorated_pot.clear_side")), button -> clearDecoratedPotSide()));
        InfinityEditorButton clearAll = addRenderableWidget(new InfinityEditorButton(controlsX, 156, width, FIELD_HEIGHT,
                Component.translatable(key("decorated_pot.clear_all")), button -> clearDecoratedPotDecorations()));
        clearAll.active = getDecoratedPotDecorationCount() > 0;

        if (!isSidebarUi()) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 58, this.height - 64, 28, FIELD_HEIGHT,
                    Component.literal("<"), button -> cycleSelectedPotterySherd(-1)));
            addRenderableWidget(new InfinityEditorButton(this.midX - 28, this.height - 64, 56, FIELD_HEIGHT,
                    Component.translatable(key("decorated_pot.apply")), button -> applySelectedPotterySherd()));
            addRenderableWidget(new InfinityEditorButton(this.midX + 30, this.height - 64, 28, FIELD_HEIGHT,
                    Component.literal(">"), button -> cycleSelectedPotterySherd(1)));
        }
    }

    protected void addDecoratedPotSideButton(int x, int y, int width, int side) {
        Component text = side == this.selectedDecoratedPotSide
                ? Component.literal("> ").append(getDecoratedPotSideName(side))
                : getDecoratedPotSideName(side);
        addRenderableWidget(new InfinityEditorButton(x, y, width, FIELD_HEIGHT, text,
                button -> selectDecoratedPotSide(side)));
    }

    protected void addSpawnEggPanel() {
        int listX = spawnEggEntityListX();
        int listWidth = spawnEggEntityListWidth();
        this.spawnEggEntityFilterBox = addTrackedBox(legacyTextBox(listX, sideListSearchY(), Math.min(180, listWidth), FIELD_HEIGHT,
                Component.translatable(key("spawnegg.search"))));
        this.spawnEggEntityFilterBox.setMaxLength(48);
        this.spawnEggEntityFilterBox.setValue(this.spawnEggEntityFilterValue);
        this.spawnEggEntityFilterBox.setResponder(value -> {
            this.spawnEggEntityFilterValue = value.toLowerCase(Locale.ROOT);
            this.spawnEggEntityScroll = 0;
            this.selectedSpawnEggEntityIndex = 0;
            this.spawnEggTagScroll = 0;
            rebuildWidgets();
            if (this.spawnEggEntityFilterBox != null) {
                this.setFocused(this.spawnEggEntityFilterBox);
                this.spawnEggEntityFilterBox.setFocused(true);
                this.spawnEggEntityFilterBox.setCursorPosition(this.spawnEggEntityFilterBox.getValue().length());
            }
        });

        int controlsX = getSpawnEggControlsX();
        int width = getSpawnEggControlsWidth();
        int buttonY = 52;
        addRenderableWidget(new InfinityEditorButton(controlsX, 52, width, FIELD_HEIGHT,
                Component.translatable(key("spawnegg.apply_entity")), button -> applySelectedSpawnEggEntity()));
        buttonY += 26;
        if (isSpawnEggItem(this.previewStack)) {
            addRenderableWidget(new InfinityEditorButton(controlsX, buttonY, width, FIELD_HEIGHT,
                    Component.translatable(key("spawnegg.sync_egg")), button -> syncSpawnEggToSelectedEntityItem()));
            buttonY += 26;
        }

        InfinityEditorButton clear = addRenderableWidget(new InfinityEditorButton(controlsX, buttonY, width, FIELD_HEIGHT,
                Component.translatable(key(getSpawnEditorClearKey())), button -> clearSpawnEggEntityTag()));
        clear.active = hasSpawnEditorEntityData(this.previewStack);

        List<SpawnEggTagRow> rows = getSpawnEggTagRows();
        setSpawnEggTagScroll(this.spawnEggTagScroll);
        int end = Math.min(rows.size(), this.spawnEggTagScroll + SPAWN_EGG_TAG_ROWS);
        for (int i = this.spawnEggTagScroll; i < end; i++) {
            addSpawnEggTagControl(rows.get(i), getSpawnEggTagRowY(i - this.spawnEggTagScroll), controlsX, width);
        }

        if (!isSidebarUi()) {
            addRenderableWidget(new InfinityEditorButton(this.midX - 58, this.height - 64, 28, FIELD_HEIGHT,
                    Component.literal("<"), button -> cycleSelectedSpawnEggEntity(-1)));
            addRenderableWidget(new InfinityEditorButton(this.midX - 28, this.height - 64, 56, FIELD_HEIGHT,
                    Component.translatable(key("spawnegg.apply_entity")), button -> applySelectedSpawnEggEntity()));
            addRenderableWidget(new InfinityEditorButton(this.midX + 30, this.height - 64, 28, FIELD_HEIGHT,
                    Component.literal(">"), button -> cycleSelectedSpawnEggEntity(1)));
        }
    }

    protected void addSpawnEggTagControl(SpawnEggTagRow row, int y, int controlsX, int width) {
        if (row.type() == SpawnEggTagRowType.CHOICE) {
            addRenderableWidget(new InfinityEditorButton(controlsX, y, width, FIELD_HEIGHT,
                    getSpawnEggChoiceText(row), button -> cycleSpawnEggChoice(row)));
            return;
        }

        if (row.type() == SpawnEggTagRowType.BOOLEAN) {
            addRenderableWidget(new InfinityEditorButton(controlsX, y, width, FIELD_HEIGHT,
                    getSpawnEggBooleanText(row), button -> toggleSpawnEggBoolean(row)));
            return;
        }

        if (row.type() == SpawnEggTagRowType.PRESENCE) {
            addRenderableWidget(new InfinityEditorButton(controlsX, y, width, FIELD_HEIGHT,
                    getSpawnEggPresenceText(row), button -> toggleSpawnEggPresence(row)));
            return;
        }

        EditBox box = addTrackedBox(legacyTextBox(controlsX + 70, y, width - 70, FIELD_HEIGHT,
                Component.translatable(key("spawnegg." + row.translationSuffix()))));
        box.setMaxLength(getSpawnEggTagTextMaxLength(row));
        box.setValue(getSpawnEggTagTextValue(row));
        if (row.type() == SpawnEggTagRowType.CUSTOM_NAME) {
            this.spawnEggCustomNameBox = box;
            box.setResponder(value -> {
                this.spawnEggCustomNameValue = value;
                applySpawnEggCustomName(value);
            });
        } else if (row.type() == SpawnEggTagRowType.OWNER) {
            this.spawnEggOwnerBox = box;
            box.setResponder(value -> {
                this.spawnEggOwnerValue = value;
                applySpawnEggOwner(value);
            });
        } else {
            box.setFilter(value -> isAllowedSpawnEggNumber(value, row.numberType()));
            box.setResponder(value -> applySpawnEggNumber(row, value));
        }
    }

    protected void addTradesPanel() {
        ensureVillagerTradeOffers();
    }

    protected void addTradePanel() {
        readTradeFieldsFromStack(this.previewStack);
        int nbtWidth = contentLimitedWidth(420, 180, 48);
        int nbtY = Math.max(66, Math.min(getSingleTradeSlotY() + 52, sidebarBottomButtonY() - 132));
        this.tradeItemNbtBox = addTrackedBox(legacyTextBox(centeredContentX(nbtWidth), nbtY, nbtWidth, FIELD_HEIGHT,
                Component.translatable(key("trades.item_nbt"))));
        this.tradeItemNbtBox.setMaxLength(20000);
        this.tradeItemNbtBox.setValue(this.tradeItemNbtValue);
        this.tradeItemNbtBox.setResponder(value -> this.tradeItemNbtValue = value);
        this.tradeItemNbtBox.active = getVillagerTradeCount() > 0;

        int labelWidth = isSidebarUi() ? 78 : 86;
        int fieldWidth = 58;
        int columnGap = 24;
        int columnWidth = labelWidth + fieldWidth;
        int fieldsLeft = centeredContentX(columnWidth * 2 + columnGap);
        int leftFieldX = fieldsLeft + labelWidth;
        int rightFieldX = fieldsLeft + columnWidth + columnGap + labelWidth;
        int fieldsY = nbtY + 34;

        this.tradeUsesBox = addTradeFieldBox(leftFieldX, fieldsY, fieldWidth, this.tradeUsesValue,
                value -> value.matches("\\d{0,10}"), value -> this.tradeUsesValue = value);
        this.tradeMaxUsesBox = addTradeFieldBox(rightFieldX, fieldsY, fieldWidth, this.tradeMaxUsesValue,
                value -> value.matches("-?\\d{0,10}"), value -> this.tradeMaxUsesValue = value);
        this.tradeXpBox = addTradeFieldBox(leftFieldX, fieldsY + 24, fieldWidth, this.tradeXpValue,
                value -> value.matches("\\d{0,10}"), value -> this.tradeXpValue = value);
        this.tradeSpecialPriceBox = addTradeFieldBox(rightFieldX, fieldsY + 24, fieldWidth, this.tradeSpecialPriceValue,
                value -> value.matches("-?\\d{0,10}"), value -> this.tradeSpecialPriceValue = value);
        this.tradeDemandBox = addTradeFieldBox(leftFieldX, fieldsY + 48, fieldWidth, this.tradeDemandValue,
                value -> value.matches("-?\\d{0,10}"), value -> this.tradeDemandValue = value);
        this.tradePriceMultiplierBox = addTradeFieldBox(rightFieldX, fieldsY + 48, fieldWidth, this.tradePriceMultiplierValue,
                value -> value.matches("\\d{0,8}(\\.\\d{0,6})?"), value -> this.tradePriceMultiplierValue = value);

        int buttonY = fieldsY + 76;
        int buttonGap = 8;
        int buttonAreaWidth = contentLimitedWidth(196, 130, 32);
        int buttonWidth = Math.max(1, (buttonAreaWidth - buttonGap) / 2);
        int buttonX = centeredContentX(buttonAreaWidth);
        InfinityEditorButton update = addRenderableWidget(new InfinityEditorButton(buttonX, buttonY,
                buttonWidth, FIELD_HEIGHT, Component.translatable(key("trades.update")), button -> updateSelectedTradeFromFields()));
        update.active = getVillagerTradeCount() > 0;
        InfinityEditorButton reward = addRenderableWidget(new InfinityEditorButton(buttonX + buttonWidth + buttonGap,
                buttonY, buttonWidth, FIELD_HEIGHT,
                Component.translatable(key("trades.reward_exp." + (this.tradeRewardExp ? 1 : 0))),
                button -> toggleSelectedTradeRewardExp()));
        reward.active = getVillagerTradeCount() > 0;
    }

    protected EditBox addTradeFieldBox(int x, int y, int width, String value, java.util.function.Predicate<String> filter,
                                     java.util.function.Consumer<String> responder) {
        EditBox box = addTrackedBox(legacyTextBox(x, y, width, FIELD_HEIGHT, Component.empty()));
        box.setMaxLength(16);
        box.setFilter(filter);
        box.setValue(value);
        box.setResponder(responder);
        box.active = getVillagerTradeCount() > 0;
        return box;
    }

    protected void addAttributesPanel() {
        int controlLeft = editorControlLeft();
        this.attributeInfinityButton = addRenderableWidget(new InfinityEditorButton(controlLeft, this.height - 123, 80, OLD_BUTTON_HEIGHT,
                Component.translatable(key("attributes.infinity." + (this.attributeInfinity ? 1 : 0))),
                button -> toggleAttributeInfinity()));

        this.attributeOperationButton = addRenderableWidget(new InfinityEditorButton(controlLeft, this.height - 93, 80, OLD_BUTTON_HEIGHT,
                Component.translatable(key("attributes.operation." + this.attributeOperation)),
                button -> cycleAttributeOperation()));
        this.attributeOperationButton.active = !this.attributeInfinity;

        this.attributeSlotButton = addRenderableWidget(new InfinityEditorButton(controlLeft, this.height - 63, 80, OLD_BUTTON_HEIGHT,
                Component.translatable(key("attributes.slot." + this.attributeSlot)),
                button -> cycleAttributeSlot()));

        addRenderableWidget(new InfinityEditorButton(controlLeft, this.height - 33, 20, OLD_BUTTON_HEIGHT,
                Component.literal(this.attributeNegative ? "-" : "+"), button -> {
            this.attributeNegative = !this.attributeNegative;
            rebuildWidgets();
        }));

        this.attributeAmountBox = addTrackedBox(numberBox(controlLeft + 23, this.height - 32, 55, 18, 8,
                this.attributeAmountValue, 0, MAX_ATTRIBUTE_INTEGER));
        this.attributeAmountBox.setResponder(value -> this.attributeAmountValue = value);
        this.attributeAmountBox.active = !this.attributeInfinity;

        this.attributeDecimalBox = addTrackedBox(numberBox(controlLeft + 85, this.height - 32, 25, 18, 3,
                this.attributeDecimalValue, 0, 999));
        this.attributeDecimalBox.setResponder(value -> this.attributeDecimalValue = value);
        this.attributeDecimalBox.active = !this.attributeInfinity;
    }

    protected void addColorPanel() {
        int color = getEditorColor();
        this.colorHexValue = formatColorHex(color);

        this.colorHexBox = addTrackedBox(plainTextBox(centeredContentX(50), this.midY - 85, 50, OLD_BUTTON_HEIGHT,
                Component.translatable(key("color.hex"))));
        this.colorHexBox.setMaxLength(7);
        this.colorHexBox.setFilter(value -> value.matches("#?[0-9a-fA-F]{0,6}"));
        this.colorHexBox.setTextColor(MAIN_COLOR);
        this.colorHexBox.setValue(this.colorHexValue);
        this.colorHexBox.setResponder(value -> {
            this.colorHexValue = value;
            if (!this.syncingColorControls) {
                applyColorFromHex(false);
            }
        });

        int sliderWidth = contentLimitedWidth(160, 100, 32);
        int sliderX = centeredContentX(sliderWidth);
        this.redSlider = addRenderableWidget(new ColorSlider(sliderX, this.midY - 50, sliderWidth, OLD_BUTTON_HEIGHT,
                Component.translatable(key("color.red")), getRed(color), value -> setColorComponent(16, value)));
        this.greenSlider = addRenderableWidget(new ColorSlider(sliderX, this.midY - 10, sliderWidth, OLD_BUTTON_HEIGHT,
                Component.translatable(key("color.green")), getGreen(color), value -> setColorComponent(8, value)));
        this.blueSlider = addRenderableWidget(new ColorSlider(sliderX, this.midY + 30, sliderWidth, OLD_BUTTON_HEIGHT,
                Component.translatable(key("color.blue")), getBlue(color), value -> setColorComponent(0, value)));

        addRenderableWidget(new InfinityEditorButton(centeredContentX(60), this.midY + 65, 60, OLD_BUTTON_HEIGHT,
                Component.translatable(key("color.random")), button -> {
            setEditorColor(ThreadLocalRandom.current().nextInt(0x1000000));
            syncColorControlsFromStack();
            this.status = Component.translatable(messageKey("editor_color_updated"), this.colorHexValue);
        }));
    }

    protected void addLorePainterPanel() {
        ensureLorePainterRows();

        Component insertText = Component.translatable(key("lorepainter.insert"));
        int insertWidth = this.font.width(insertText) + 5;
        addRenderableWidget(new InfinityEditorButton(this.midX - insertWidth / 2, this.height - 55, insertWidth, OLD_BUTTON_HEIGHT,
                insertText, button -> insertLorePainterRows()));

        Component scaleText = Component.translatable(key("lorepainter.scale"));
        int scaleWidth = this.font.width(scaleText) + 5;
        this.lorePainterScaleButton = addRenderableWidget(new InfinityEditorButton(this.width - scaleWidth, this.height - 20, scaleWidth, OLD_BUTTON_HEIGHT,
                scaleText, button -> cycleGuiScale()));

        this.lorePainterAddRowButton = addRenderableWidget(new InfinityEditorButton(this.midX - 20, this.height - 50, 20, OLD_BUTTON_HEIGHT,
                Component.literal("+"), button -> addLorePainterRow()));
        this.lorePainterRemoveRowButton = addRenderableWidget(new InfinityEditorButton(this.midX, this.height - 50, 20, OLD_BUTTON_HEIGHT,
                Component.literal("-"), button -> removeLorePainterRow()));
        this.lorePainterAddColumnButton = addRenderableWidget(new InfinityEditorButton(this.width - 50, this.midY - 20, 20, OLD_BUTTON_HEIGHT,
                Component.literal("+"), button -> addLorePainterColumn()));
        this.lorePainterRemoveColumnButton = addRenderableWidget(new InfinityEditorButton(this.width - 50, this.midY, 20, OLD_BUTTON_HEIGHT,
                Component.literal("-"), button -> removeLorePainterColumn()));

        Component previewText = Component.translatable(key("lorepainter.preview"));
        this.lorePainterPreviewButton = addRenderableWidget(new InfinityEditorButton(0, this.height - 20, this.font.width(previewText) + 5, OLD_BUTTON_HEIGHT,
                previewText, button -> this.lorePainterPreview = !this.lorePainterPreview));
    }

    protected void addLorePanel() {
        this.loreScroll = Mth.clamp(this.loreScroll, 0, Math.max(0, this.loreValues.size() - loreLineSpaces()));
        int x = lorePanelLeft();
        int padding = 2;
        InfinityEditorButton button = addTopButton(x, "lore.addline", pressed -> {
            this.loreValues.add("");
            this.loreScroll = Math.max(0, this.loreValues.size() - loreLineSpaces());
            applyLoreToStack();
            rebuildWidgets();
        });
        x += button.getWidth() + padding;
        this.copyLoreButton = addTopButton(x, "lore.copylore", pressed -> copyLoreOnly());
        x += this.copyLoreButton.getWidth() + padding;
        button = addTopButton(x, "lore.copyall", pressed -> copyFullTooltip());
        x += button.getWidth() + padding;
        button = addTopButton(x, "lore.paste", pressed -> pasteLore());
        x += button.getWidth() + padding;
        button = addTopButton(x, "hideflags", pressed -> switchPanel(Panel.HIDE_FLAGS));
        x += button.getWidth() + padding;
        addTopButton(x, "lorepainter", pressed -> switchPanel(Panel.LORE_PAINTER));

        int spaces = loreLineSpaces();
        int end = Math.min(this.loreValues.size(), this.loreScroll + spaces);
        for (int i = this.loreScroll; i < end; i++) {
            int line = i;
            int row = i - this.loreScroll;
            int y = 55 + 30 * row;
            EditBox field = addTrackedBox(legacyTextBox(lorePanelLeft(), y, loreFieldWidth(), FIELD_HEIGHT,
                    Component.literal("Line " + (line + 1))));
            field.setMaxLength(500);
            field.setTextColor(MAIN_COLOR);
            field.setValue(this.loreValues.get(line));
            field.setResponder(value -> {
                setLoreLine(line, value);
                applyLoreToStack();
            });
            this.loreBoxes.add(field);

            InfinityEditorButton up = addRenderableWidget(new InfinityEditorButton(field.getX() + field.getWidth() + 3, y, 20, FIELD_HEIGHT,
                    Component.literal("\u2B06"), pressed -> moveLoreLine(line, -1)));
            up.active = line > 0;
            InfinityEditorButton down = addRenderableWidget(new InfinityEditorButton(field.getX() + field.getWidth() + 24, y, 20, FIELD_HEIGHT,
                    Component.literal("\u2B07"), pressed -> moveLoreLine(line, 1)));
            down.active = line < this.loreValues.size() - 1;
            addRenderableWidget(new InfinityEditorButton(field.getX() + field.getWidth() + 45, y, 20, FIELD_HEIGHT,
                    Component.literal("\u2715"), pressed -> {
                removeLoreLine(line);
                rebuildWidgets();
            }));
        }
        addFormatButtons();
    }

    protected InfinityEditorButton addTopButton(int x, String keySuffix, InfinityEditorButton.PressAction action) {
        Component text = Component.translatable(key(keySuffix));
        int width = this.font.width(text) + 6;
        if (isSidebarUi()) {
            width = Math.min(width, Math.max(24, safeRight() - lorePanelLeft()));
        }
        int clampedX = isSidebarUi() ? Math.min(x, Math.max(lorePanelLeft(), safeRight() - width)) : x;
        InfinityEditorButton button = addRenderableWidget(new InfinityEditorButton(clampedX, 10, width, FIELD_HEIGHT, text, action));
        this.loreActionButtons.add(button);
        return button;
    }

    protected void addBottomButtons() {
        if (isSidebarUi()) {
            addSidebarBottomButtons();
            return;
        }

        switch (this.activePanel) {
            case ITEM -> {
                addRenderableWidget(new InfinityEditorButton(5, legacyUiModeButtonY(), 80, OLD_BUTTON_HEIGHT,
                        getUiModeButtonText(), button -> toggleUiMode()));
                if (isTradeSlotEditor()) {
                    addRenderableWidget(new InfinityEditorButton(this.midX - 90, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                            Component.translatable(key("back")), button -> goBack()));
                    addRenderableWidget(new InfinityEditorButton(this.midX - 30, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                            Component.translatable(key("reset")), button -> resetStack()));
                } else {
                    addRenderableWidget(new InfinityEditorButton(this.midX - 90, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                            Component.translatable(key("close")), button -> onClose()));
                    addRenderableWidget(new InfinityEditorButton(this.midX - 30, this.height - 25, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                            Component.translatable(key("save")), button -> applyToSelectedSlot()));
                    addRenderableWidget(new InfinityEditorButton(this.midX - 30, this.height - 45, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                            Component.translatable(key("reset")), button -> resetStack()));
                }
                addRenderableWidget(new InfinityEditorButton(this.midX + 30, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("drop")), button -> dropEditedStack()));
            }
            case NBT -> {
                addRenderableWidget(new InfinityEditorButton(this.midX - 60, this.height - 25, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("back")), button -> goBack()));
                addRenderableWidget(new InfinityEditorButton(this.midX, this.height - 25, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("reset")), button -> resetStack()));
            }
            case NBT_ADVANCED -> addRenderableWidget(new InfinityEditorButton(this.midX - 60, this.height - 25, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                    Component.translatable(key("back")), button -> goBack()));
            case TRADE -> {
                addRenderableWidget(new InfinityEditorButton(this.midX - 90, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("back")), button -> goBack()));
                InfinityEditorButton reset = addRenderableWidget(new InfinityEditorButton(this.midX - 30, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("reset")), button -> {
                }));
                reset.active = false;
                addRenderableWidget(new InfinityEditorButton(this.midX + 30, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("drop")), button -> dropEditedStack()));
            }
            default -> {
                addRenderableWidget(new InfinityEditorButton(this.midX - 90, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("back")), button -> goBack()));
                addRenderableWidget(new InfinityEditorButton(this.midX - 30, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("reset")), button -> resetStack()));
                addRenderableWidget(new InfinityEditorButton(this.midX + 30, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT,
                        Component.translatable(key("drop")), button -> dropEditedStack()));
            }
        }
    }

    protected void addSidebarBottomButtons() {
        int buttonHeight = SIDEBAR_BUTTON_HEIGHT;
        int gap = 6;
        int y = sidebarBottomButtonY();

        addRenderableWidget(new InfinityEditorButton(10, sidebarUiModeButtonY(), sidebarWidth() - 20, buttonHeight,
                getUiModeButtonText(), button -> toggleUiMode()));

        if (this.activePanel == Panel.ITEM) {
            int count = isTradeSlotEditor() ? 3 : 4;
            int buttonWidth = sidebarBottomButtonWidth(count, gap);
            int x = sidebarBottomStartX(count, buttonWidth, gap);
            addRenderableWidget(new InfinityEditorButton(x, y, buttonWidth, buttonHeight,
                    Component.translatable(key(isTradeSlotEditor() ? "back" : "close")), button -> {
                if (isTradeSlotEditor()) {
                    goBack();
                } else {
                    onClose();
                }
            }));
            addRenderableWidget(new InfinityEditorButton(x + buttonWidth + gap, y, buttonWidth, buttonHeight,
                    Component.translatable(key("reset")), button -> resetStack()));
            if (!isTradeSlotEditor()) {
                addRenderableWidget(new InfinityEditorButton(x + 2 * (buttonWidth + gap), y, buttonWidth, buttonHeight,
                        Component.translatable(key("save")), button -> applyToSelectedSlot()));
                addRenderableWidget(new InfinityEditorButton(x + 3 * (buttonWidth + gap), y, buttonWidth, buttonHeight,
                        Component.translatable(key("drop")), button -> dropEditedStack()));
            } else {
                addRenderableWidget(new InfinityEditorButton(x + 2 * (buttonWidth + gap), y, buttonWidth, buttonHeight,
                        Component.translatable(key("drop")), button -> dropEditedStack()));
            }
            return;
        }

        if (this.activePanel == Panel.NBT_ADVANCED) {
            int buttonWidth = sidebarBottomButtonWidth(1, gap);
            addRenderableWidget(new InfinityEditorButton(contentLeft() + 16, y, buttonWidth, buttonHeight,
                    Component.translatable(key("back")), button -> goBack()));
            return;
        }

        if (this.activePanel == Panel.TRADE) {
            int buttonWidth = sidebarBottomButtonWidth(3, gap);
            int x = sidebarBottomStartX(3, buttonWidth, gap);
            addRenderableWidget(new InfinityEditorButton(x, y, buttonWidth, buttonHeight,
                    Component.translatable(key("back")), button -> goBack()));
            InfinityEditorButton reset = addRenderableWidget(new InfinityEditorButton(x + buttonWidth + gap, y, buttonWidth, buttonHeight,
                    Component.translatable(key("reset")), button -> {
            }));
            reset.active = false;
            addRenderableWidget(new InfinityEditorButton(x + 2 * (buttonWidth + gap), y, buttonWidth, buttonHeight,
                    Component.translatable(key("drop")), button -> dropEditedStack()));
            return;
        }

        int buttonWidth = sidebarBottomButtonWidth(3, gap);
        int x = sidebarBottomStartX(3, buttonWidth, gap);
        addRenderableWidget(new InfinityEditorButton(x, y, buttonWidth, buttonHeight,
                Component.translatable(key("back")), button -> goBack()));
        addRenderableWidget(new InfinityEditorButton(x + buttonWidth + gap, y, buttonWidth, buttonHeight,
                Component.translatable(key("reset")), button -> resetStack()));
        addRenderableWidget(new InfinityEditorButton(x + 2 * (buttonWidth + gap), y, buttonWidth, buttonHeight,
                Component.translatable(key("drop")), button -> dropEditedStack()));
    }

    protected EditBox addTrackedBox(EditBox box) {
        this.tickingBoxes.add(box);
        return addRenderableWidget(box);
    }

    protected EditBox legacyTextBox(int x, int y, int width, int height, Component message) {
        EditBox box = isSidebarUi()
                ? new ModernTextEditBox(this.font, x, y, width, height, message)
                : new LegacyTextEditBox(this.font, x, y, width, height, message);
        box.setTextColor(isSidebarUi() ? ModernUi.TEXT_PRIMARY : MAIN_COLOR);
        return box;
    }

    protected EditBox plainTextBox(int x, int y, int width, int height, Component message) {
        EditBox box = isSidebarUi()
                ? new ModernTextEditBox(this.font, x, y, width, height, message)
                : new EditBox(this.font, x, y, width, height, message);
        box.setTextColor(isSidebarUi() ? ModernUi.TEXT_PRIMARY : MAIN_COLOR);
        return box;
    }

    protected EditBox numberBox(int x, int y, int width, int height, int digits, String value, int minValue, int maxValue) {
        if (isSidebarUi()) {
            EditBox box = plainTextBox(x, y, width, height, Component.empty());
            int normalizedDigits = Math.max(1, digits);
            box.setMaxLength(normalizedDigits + (minValue < 0 ? 1 : 0));
            box.setFilter(text -> isAllowedSidebarNumber(text, normalizedDigits, minValue < 0));
            box.setValue(value == null ? "" : value);
            return box;
        }

        EditBox box = new FixedDigitEditBox(this.font, x, y, width, height, digits, minValue, maxValue);
        box.setValue(value);
        return box;
    }

    private boolean isAllowedSidebarNumber(String value, int digits, boolean allowNegative) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        int start = 0;
        if (value.charAt(0) == '-') {
            if (!allowNegative || value.length() == 1) {
                return allowNegative;
            }
            start = 1;
        }
        if (value.length() - start > digits) {
            return false;
        }
        for (int i = start; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character < '0' || character > '9') {
                return false;
            }
        }
        return true;
    }
}

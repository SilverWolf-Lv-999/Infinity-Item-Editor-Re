package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackCompat;

import io.github.seraphina.infinity_item_editor_re.util.ComponentCompat;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Axis;
import io.github.seraphina.infinity_item_editor_re.Config;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
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
import net.minecraft.world.item.component.FireworkExplosion;
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
abstract class ItemEditorScreenCommon extends ItemEditorScreenState {
    protected ItemEditorScreenCommon(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(stack, targetContainerSlot, parentTradeScreen, parentTradeIndex, parentTradeSlot);
    }

protected void updateMouseDistance(int mouseX, int mouseY) {
        int distX = contentMidX() - mouseX;
        int distY = this.midY - mouseY;
        this.mouseDist = (int) Math.sqrt(distX * distX + distY * distY);
    }

    protected int getRingRadius() {
        int verticalRadius = this.height / 3;
        if (!isSidebarUi()) {
            return verticalRadius;
        }

        return Math.max(42, Math.min(verticalRadius, contentWidth() / 3));
    }

    protected boolean isMouseOverCenter(double mouseX, double mouseY) {
        int centerX = contentMidX();
        return mouseX > centerX - CENTER_HIT_RADIUS
                && mouseX < centerX + CENTER_HIT_RADIUS
                && mouseY > this.midY - CENTER_HIT_RADIUS
                && mouseY < this.midY + CENTER_HIT_RADIUS;
    }

    protected boolean isSidebarUi() {
        return Config.getItemGuiMode() == Config.ItemEditorUiMode.SIDEBAR;
    }

    protected int sidebarWidth() {
        return Mth.clamp(this.width / 5, SIDEBAR_WIDTH_MIN, SIDEBAR_WIDTH_MAX);
    }

    protected int contentLeft() {
        return isSidebarUi() ? sidebarWidth() + SIDEBAR_CONTENT_GAP : 0;
    }

    protected int safeLeft() {
        return isSidebarUi() ? contentLeft() + SIDEBAR_SAFE_MARGIN : SIDEBAR_SAFE_MARGIN;
    }

    protected int safeRight() {
        return this.width - SIDEBAR_SAFE_MARGIN;
    }

    protected int safeTop() {
        return SIDEBAR_SAFE_MARGIN;
    }

    protected int safeBottom() {
        return this.height - SIDEBAR_SAFE_MARGIN;
    }

    protected int contentWidth() {
        return Math.max(1, safeRight() - safeLeft());
    }

    protected int contentMidX() {
        if (!isSidebarUi()) {
            return this.midX;
        }

        return safeLeft() + contentWidth() / 2;
    }

    protected int centeredContentX(int width) {
        int x = contentMidX() - width / 2;
        if (!isSidebarUi()) {
            return x;
        }

        return Mth.clamp(x, safeLeft(), Math.max(safeLeft(), safeRight() - width));
    }

    protected int contentLimitedWidth(int preferred, int minWidth, int margin) {
        int available = isSidebarUi() ? contentWidth() - margin * 2 : this.width - margin * 2;
        return Mth.clamp(preferred, Math.min(minWidth, Math.max(1, available)), Math.max(1, available));
    }

    protected int editorControlLeft() {
        return isSidebarUi() ? safeLeft() + 8 : 15;
    }

    protected int editorListTextLeft() {
        return isSidebarUi() ? safeLeft() + 10 : 5;
    }

    protected int itemPreviewCenterX() {
        return isSidebarUi() ? sidebarWidth() / 2 : this.midX;
    }

    protected int itemPreviewCenterY() {
        return isSidebarUi() ? 45 : 40;
    }

    protected boolean isCompactSidebarItemPanel() {
        return isSidebarUi() && this.activePanel == Panel.ITEM && contentWidth() < 420;
    }

    protected boolean isNarrowSidebarItemPanel() {
        return isSidebarUi() && this.activePanel == Panel.ITEM && contentWidth() < 260;
    }

    protected boolean isShortSidebarItemPanel() {
        return isCompactSidebarItemPanel() && this.height < 300;
    }

    protected int sidebarItemLabelRightX() {
        if (isNarrowSidebarItemPanel()) {
            return safeLeft() + 8;
        }

        return safeLeft() + (isCompactSidebarItemPanel() ? 74 : 88);
    }

    protected int sidebarItemFieldX() {
        if (isNarrowSidebarItemPanel()) {
            return safeLeft() + 8;
        }

        return sidebarItemLabelRightX() + 10;
    }

    protected int sidebarItemDetailsY() {
        return isCompactSidebarItemPanel() ? 204 : 76;
    }

    protected int sidebarItemDetailsX(int detailsWidth) {
        if (isCompactSidebarItemPanel()) {
            return safeLeft();
        }

        return sidebarDetailsX(detailsWidth);
    }

    protected int sidebarDetailsX(int detailsWidth) {
        int x = safeRight() - detailsWidth;
        if (x < safeLeft() + 268) {
            return safeLeft();
        }

        return x;
    }

    protected int sidebarItemFieldWidth(int detailsX) {
        int maxRight = isCompactSidebarItemPanel() ? safeRight() : detailsX - SIDEBAR_CONTENT_GAP;
        if (isNarrowSidebarItemPanel()) {
            return Math.max(48, maxRight - sidebarItemFieldX());
        }

        return Mth.clamp(maxRight - sidebarItemFieldX(), 96, 240);
    }

    protected int sidebarItemCoreHeight() {
        return isNarrowSidebarItemPanel() ? 128 : 124;
    }

    protected int sidebarItemNameY() {
        if (isNarrowSidebarItemPanel() || isShortSidebarItemPanel()) {
            return 184;
        }

        return sidebarItemDetailsY();
    }

    protected int sidebarItemIdY() {
        return isNarrowSidebarItemPanel() ? 76 : 78;
    }

    protected int sidebarItemCountY() {
        return isNarrowSidebarItemPanel() ? 112 : 108;
    }

    protected int sidebarItemDamageY() {
        return isNarrowSidebarItemPanel() ? 148 : 138;
    }

    protected int sidebarVisibleLoreLines() {
        if (isCompactSidebarItemPanel()) {
            return 0;
        }

        int maxBottom = sidebarBottomButtonY() - 8;
        int maxLines = (maxBottom - sidebarItemNameY() - 76) / 26;
        return Mth.clamp(Math.min(this.loreValues.size() + 1, maxLines), 0, 6);
    }

    protected boolean canShowSidebarLoreButton() {
        return sidebarNameCardBottom() <= sidebarBottomButtonY() - 8;
    }

    protected int sidebarNameCardBottom() {
        int visibleLoreLines = sidebarVisibleLoreLines();
        if (visibleLoreLines == 0) {
            return sidebarItemNameY() + 48;
        }

        return sidebarItemNameY() + 50 + 26 * visibleLoreLines + 26;
    }

    protected boolean canShowSidebarActionGrid() {
        int actionY = getActionGridY();
        if (!isCompactSidebarItemPanel()) {
            return actionY + SIDEBAR_BUTTON_HEIGHT <= sidebarBottomButtonY() - 8;
        }

        return actionY >= sidebarNameCardBottom() + SIDEBAR_CONTENT_GAP
                && actionY + SIDEBAR_BUTTON_HEIGHT <= sidebarBottomButtonY() - 8;
    }

    protected int getActionGridY() {
        if (isCompactSidebarItemPanel()) {
            return Math.max(sidebarNameCardBottom() + SIDEBAR_CONTENT_GAP, safeBottom() - 78);
        }

        return 184;
    }

    protected int sidebarBottomButtonY() {
        return this.height - 34;
    }

    protected int sidebarBottomButtonHeight() {
        return 22;
    }

    protected int sidebarBottomButtonWidth(int count, int gap) {
        int availableWidth = Math.max(24, this.width - contentLeft() - 32);
        int fittedWidth = (availableWidth - (count - 1) * gap) / count;
        return Mth.clamp(fittedWidth, 24, 66);
    }

    protected int sidebarBottomStartX(int count, int buttonWidth, int gap) {
        int totalWidth = count * buttonWidth + (count - 1) * gap;
        return Mth.clamp(contentMidX() - totalWidth / 2, contentLeft() + 16, this.width - 16 - totalWidth);
    }

    protected int sidebarItemDropButtonX() {
        int gap = 6;
        int buttonWidth = sidebarBottomButtonWidth(4, gap);
        return sidebarBottomStartX(4, buttonWidth, gap) + 3 * (buttonWidth + gap);
    }

    protected int sidebarItemDropButtonWidth() {
        return sidebarBottomButtonWidth(4, 6);
    }

    protected int sidebarUiModeButtonY() {
        int formatRows = (2 + ChatFormatting.values().length + 7) / 8;
        int formatTop = Math.max(82, this.height - 38 - formatRows * 15);
        return Math.max(54, formatTop - 28);
    }

    protected int legacyUiModeButtonY() {
        return this.midX - 90 <= 90 ? 5 : this.height - 25;
    }

    protected int searchFilterWidth() {
        return isSidebarUi() ? Math.min(140, Math.max(92, contentWidth() / 4)) : 100;
    }

    protected int searchFilterX() {
        return isSidebarUi() ? contentMidX() - searchFilterWidth() / 2 : this.width - 115;
    }

    protected int searchFilterY() {
        return isSidebarUi() ? 58 : this.height - 33;
    }

    protected int sideListSearchY() {
        return isSidebarUi() ? 48 : 28;
    }

    protected int sideListStartY() {
        return isSidebarUi() ? 80 : 58;
    }

    protected int bannerPatternListX() {
        return isSidebarUi() ? safeLeft() + 10 : 10;
    }

    protected int bannerPatternListWidth() {
        if (!isSidebarUi()) {
            return 150;
        }

        int maxWidth = Math.max(1, Math.min(176, contentWidth() - 20));
        return Mth.clamp(contentWidth() / 4, Math.min(132, maxWidth), maxWidth);
    }

    protected int potterySherdListX() {
        return isSidebarUi() ? safeLeft() + 10 : 10;
    }

    protected int potterySherdListWidth() {
        if (!isSidebarUi()) {
            return 170;
        }

        int maxWidth = Math.max(1, Math.min(210, contentWidth() - 20));
        return Mth.clamp(contentWidth() / 4, Math.min(150, maxWidth), maxWidth);
    }

    protected int spawnEggEntityListX() {
        return isSidebarUi() ? safeLeft() + 10 : 10;
    }

    protected int spawnEggEntityListWidth() {
        if (!isSidebarUi()) {
            return 170;
        }

        int maxWidth = Math.max(1, Math.min(210, contentWidth() - 20));
        return Mth.clamp(contentWidth() / 4, Math.min(150, maxWidth), maxWidth);
    }

    protected int rightControlsX(int width, int listX, int listWidth) {
        if (!isSidebarUi()) {
            return Math.max(this.midX + 76, this.width - width - 10);
        }

        int rightAligned = safeRight() - width;
        int afterList = listX + listWidth + SIDEBAR_CONTENT_GAP;
        int x = afterList <= rightAligned ? rightAligned : Math.max(safeLeft(), rightAligned);
        return Mth.clamp(x, safeLeft(), Math.max(safeLeft(), safeRight() - width));
    }

    protected int nbtEditorWidth() {
        if (isSidebarUi()) {
            return Math.max(120, contentWidth() - 48);
        }

        return contentLimitedWidth(this.width / 2, 120, 32);
    }

    protected int nbtEditorX() {
        if (isSidebarUi()) {
            return safeLeft() + 24;
        }

        return centeredContentX(nbtEditorWidth());
    }

    protected int nbtEditorBoxY() {
        return isSidebarUi() ? 96 : 80;
    }

    protected int nbtEditorButtonWidth() {
        if (isSidebarUi()) {
            return Math.min(96, nbtEditorWidth());
        }

        return contentLimitedWidth(this.width / 7, 70, 32);
    }

    protected int nbtEditorButtonX(int buttonWidth) {
        if (isSidebarUi()) {
            return nbtEditorX() + nbtEditorWidth() - buttonWidth;
        }

        return centeredContentX(buttonWidth);
    }

    protected int nbtEditorButtonY() {
        return isSidebarUi() ? 128 : 100;
    }

    protected int lorePanelLeft() {
        return isSidebarUi() ? safeLeft() + 18 : 100;
    }

    protected int lorePanelRight() {
        return isSidebarUi() ? safeRight() - 18 : this.width - 100;
    }

    protected int lorePanelWidth() {
        return Math.max(80, lorePanelRight() - lorePanelLeft());
    }

    protected int loreLineLabelRightX() {
        return isSidebarUi() ? lorePanelLeft() - 10 : 90;
    }

    protected int loreFieldWidth() {
        int actionButtonWidth = 66;
        return Math.max(80, lorePanelWidth() - actionButtonWidth);
    }

    protected int loreScrollBarX() {
        return isSidebarUi() ? safeRight() - 10 : this.width - 15;
    }

    protected int loreScrollTop() {
        return 50;
    }

    protected int loreScrollBottom() {
        return isSidebarUi() ? sidebarBottomButtonY() - 10 : this.height - 50;
    }

    protected int loreScrollHeight() {
        return Math.max(1, loreScrollBottom() - loreScrollTop() + 1);
    }

    protected int lorePreviewCenterX() {
        return contentMidX();
    }

    protected Component getUiModeButtonText() {
        return Component.translatable(key("ui.mode"), getUiModeDisplayName(Config.getItemGuiMode()));
    }

    protected Component getUiModeDisplayName(Config.ItemEditorUiMode mode) {
        return Component.translatable(key("ui.mode." + mode.name().toLowerCase(Locale.ROOT)));
    }

    protected void toggleUiMode() {
        captureFieldValues();
        Config.ItemEditorUiMode mode = Config.toggleItemGuiMode();
        this.status = Component.translatable(messageKey("editor_ui_mode_updated"), getUiModeDisplayName(mode));
        rebuildWidgets();
    }

    protected void insertFormattingPrefix() {
        insertFocusedText(String.valueOf(ChatFormatting.PREFIX_CODE));
    }

    protected void stripFocusedFormatting() {
        EditBox focused = getFocusedTextBox();
        if (focused == null) {
            return;
        }
        focused.setValue(Objects.requireNonNullElse(ChatFormatting.stripFormatting(focused.getValue()), ""));
    }

    protected void insertFocusedText(String text) {
        EditBox focused = getFocusedTextBox();
        if (focused != null) {
            focused.insertText(text);
        }
    }

    protected EditBox getFocusedTextBox() {
        for (EditBox box : this.mainTextBoxes) {
            if (box.isFocused()) {
                return box;
            }
        }
        for (EditBox box : this.loreBoxes) {
            if (box.isFocused()) {
                return box;
            }
        }
        for (EditBox box : this.signBoxes) {
            if (box.isFocused()) {
                return box;
            }
        }
        if (this.rawNbtBox != null && this.rawNbtBox.isFocused()) {
            return this.rawNbtBox;
        }
        if (this.componentFilterBox != null && this.componentFilterBox.isFocused()) {
            return this.componentFilterBox;
        }
        if (this.componentNbtBox != null && this.componentNbtBox.isFocused()) {
            return this.componentNbtBox;
        }
        return null;
    }

    protected List<Component> getPrettyNbtLines() {
        return NbtFormatter.prettyLines(ItemStackNbt.get(this.previewStack));
    }

    protected List<NbtRow> buildNbtRows() {
        return NbtFormatter.rows(ItemStackNbt.get(this.previewStack), this.expandedNbtPaths);
    }

    protected int getNbtAdvancedVisibleRows() {
        int bottom = isSidebarUi() ? sidebarBottomButtonY() - 10 : this.height - 20;
        return Math.max(1, (bottom - 48) / 12);
    }

    protected void readMainFieldsFromStack(ItemStack stack) {
        ResourceLocation id = CompatRegistries.ITEMS.getKey(stack.getItem());
        this.itemIdValue = id == null ? "air" : stripMinecraftNamespace(id);
        this.countValue = Integer.toString(Math.max(1, Math.min(MAX_COUNT, stack.getCount())));
        this.damageValue = Integer.toString(Math.max(0, Math.min(getDamageMaxForField(stack), stack.getDamageValue())));
        this.nameValue = stack.getHoverName().getString();
        this.loreValues.clear();

        CompoundTag display = ItemStackNbt.getElement(stack, DISPLAY_TAG);
        if (display != null && display.contains(LORE_TAG, Tag.TAG_LIST)) {
            ListTag lore = display.getList(LORE_TAG, Tag.TAG_STRING);
            for (int i = 0; i < lore.size(); i++) {
                this.loreValues.add(readLoreLine(lore.getString(i)));
            }
        }

        readSignFieldsFromStack(stack);
        readBookFieldsFromStack(stack);
        readHeadFieldsFromStack(stack);
        readBannerFieldsFromStack(stack);
        readFireworkFieldsFromStack(stack);
        readContainerFieldsFromStack(stack);
        readDecoratedPotFieldsFromStack(stack);
        readSpawnEggFieldsFromStack(stack);
        readTradeFieldsFromStack(stack);
    }

    protected String readLoreLine(String raw) {
        try {
            Component component = ComponentCompat.fromJson(raw);
            return component == null ? raw : component.getString();
        } catch (RuntimeException exception) {
            return raw;
        }
    }

    protected void readSignFieldsFromStack(ItemStack stack) {
        Arrays.fill(this.signLineValues, "");
        this.signCommandValue = "";
        if (!isSignItem(stack)) {
            return;
        }

        CompoundTag blockEntity = ItemStackNbt.getElement(stack, BLOCK_ENTITY_TAG);
        if (blockEntity == null) {
            return;
        }

        boolean readModernMessages = false;
        if (blockEntity.contains(SIGN_FRONT_TEXT_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag frontText = blockEntity.getCompound(SIGN_FRONT_TEXT_TAG);
            if (frontText.contains(SIGN_MESSAGES_TAG, Tag.TAG_LIST)) {
                ListTag messages = frontText.getList(SIGN_MESSAGES_TAG, Tag.TAG_STRING);
                for (int i = 0; i < SIGN_LINES && i < messages.size(); i++) {
                    readSignLine(i, messages.getString(i));
                }
                readModernMessages = !messages.isEmpty();
            }
        }

        if (!readModernMessages) {
            for (int i = 0; i < SIGN_LINES; i++) {
                String key = LEGACY_SIGN_TEXT_TAG_PREFIX + (i + 1);
                if (blockEntity.contains(key, Tag.TAG_STRING)) {
                    readSignLine(i, blockEntity.getString(key));
                }
            }
        }
    }

    protected void readSignLine(int line, String raw) {
        Component component = readSerializedComponent(raw);
        this.signLineValues[line] = component.getString();
        if (line == 0) {
            ClickEvent clickEvent = component.getStyle().getClickEvent();
            if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                this.signCommandValue = clickEvent.getValue();
            }
        }
    }

    protected void readBookFieldsFromStack(ItemStack stack) {
        this.bookTitleValue = "";
        this.bookAuthorValue = "";
        if (!isBookEditableItem(stack)) {
            this.rememberedSignedBookData = null;
            return;
        }

        CompoundTag tag = ItemStackNbt.get(stack);
        if (tag == null) {
            return;
        }

        this.bookTitleValue = tag.getString(BOOK_TITLE_TAG);
        this.bookAuthorValue = tag.getString(BOOK_AUTHOR_TAG);
    }

    protected void readHeadFieldsFromStack(ItemStack stack) {
        this.headOwnerValue = "";
        this.headUuidValue = "";
        this.headTextureValue = "";
        this.headTextureSignatureValue = "";
        if (!isPlayerHeadItem(stack)) {
            return;
        }

        CompoundTag tag = ItemStackNbt.get(stack);
        if (tag == null) {
            return;
        }

        if (tag.contains(SKULL_OWNER_TAG, Tag.TAG_STRING)) {
            this.headOwnerValue = tag.getString(SKULL_OWNER_TAG);
            return;
        }

        if (!tag.contains(SKULL_OWNER_TAG, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag skullOwner = tag.getCompound(SKULL_OWNER_TAG);
        if (skullOwner.contains(SKULL_OWNER_NAME_TAG, Tag.TAG_STRING)) {
            this.headOwnerValue = skullOwner.getString(SKULL_OWNER_NAME_TAG);
        }
        if (skullOwner.hasUUID(SKULL_OWNER_ID_TAG)) {
            this.headUuidValue = skullOwner.getUUID(SKULL_OWNER_ID_TAG).toString();
        }
        CompoundTag properties = skullOwner.getCompound(SKULL_PROPERTIES_TAG);
        if (properties.contains(SKULL_TEXTURES_TAG, Tag.TAG_LIST)) {
            ListTag textures = properties.getList(SKULL_TEXTURES_TAG, Tag.TAG_COMPOUND);
            if (!textures.isEmpty()) {
                CompoundTag texture = textures.getCompound(0);
                this.headTextureValue = texture.getString(SKULL_TEXTURE_VALUE_TAG);
                this.headTextureSignatureValue = texture.getString(SKULL_TEXTURE_SIGNATURE_TAG);
            }
        }
    }

    protected void readFireworkFieldsFromStack(ItemStack stack) {
        this.fireworkExplosionType = 0;
        this.fireworkColor = DyeColor.RED.getId();
        this.fireworkFadeColor = -1;
        this.fireworkFlicker = false;
        this.fireworkTrail = false;
        if (!isFireworkEditableItem(stack)) {
            return;
        }

        CompoundTag explosion = getFireworkExplosionForFields(stack);
        if (explosion == null) {
            return;
        }

        this.fireworkExplosionType = Mth.clamp(FireworkExplosion.Shape.byId(explosion.getByte(FIREWORK_TYPE_TAG)).getId(), 0, FIREWORK_EXPLOSION_TYPES - 1);
        this.fireworkFlicker = explosion.getBoolean(FIREWORK_FLICKER_TAG);
        this.fireworkTrail = explosion.getBoolean(FIREWORK_TRAIL_TAG);

        int[] colors = explosion.getIntArray(FIREWORK_COLORS_TAG);
        if (colors.length > 0) {
            this.fireworkColor = getNearestFireworkDyeColorId(colors[0]);
        }
        int[] fadeColors = explosion.getIntArray(FIREWORK_FADE_COLORS_TAG);
        if (fadeColors.length > 0) {
            this.fireworkFadeColor = getNearestFireworkDyeColorId(fadeColors[0]);
        }
    }

    protected void readContainerFieldsFromStack(ItemStack stack) {
        this.selectedContainerSlot = Mth.clamp(this.selectedContainerSlot, 0, CONTAINER_SIZE - 1);
        if (!isContainerEditableItem(stack)) {
            this.containerSlotNbtValue = "{}";
            return;
        }
        this.containerSlotNbtValue = getContainerSlotNbt(getContainerSlotItem(this.selectedContainerSlot));
    }

    protected CompoundTag getFireworkExplosionForFields(ItemStack stack) {
        if (stack.is(Items.FIREWORK_STAR)) {
            CompoundTag tag = ItemStackNbt.get(stack);
            if (tag != null && tag.contains(FIREWORK_EXPLOSION_TAG, Tag.TAG_COMPOUND)) {
                return tag.getCompound(FIREWORK_EXPLOSION_TAG);
            }
            return null;
        }

        CompoundTag fireworks = ItemStackNbt.getElement(stack, FIREWORKS_TAG);
        if (fireworks == null || !fireworks.contains(FIREWORK_EXPLOSIONS_TAG, Tag.TAG_LIST)) {
            return null;
        }
        ListTag explosions = fireworks.getList(FIREWORK_EXPLOSIONS_TAG, Tag.TAG_COMPOUND);
        return explosions.isEmpty() ? null : explosions.getCompound(explosions.size() - 1);
    }

    protected void readBannerFieldsFromStack(ItemStack stack) {
        this.bannerBaseColor = DyeColor.WHITE.getId();
        if (isBannerEditableItem(stack)) {
            this.bannerBaseColor = getBannerBaseColor().getId();
            ListTag patterns = getBannerPatterns();
            if (!patterns.isEmpty()) {
                this.bannerPatternColor = DyeColor.byId(patterns.getCompound(patterns.size() - 1).getInt(BANNER_COLOR_TAG)).getId();
            }
        }
        this.bannerPatternColor = Mth.positiveModulo(this.bannerPatternColor, DyeColor.values().length);
        clampBannerPatternSelection(getFilteredBannerPatterns());
    }

    protected void readSpawnEggFieldsFromStack(ItemStack stack) {
        this.spawnEggCustomNameValue = "";
        this.spawnEggOwnerValue = "";
        this.spawnEggNumberValueOverrides.clear();
        this.spawnEggTagScroll = 0;
        if (!isSpawnEditorItem(stack)) {
            return;
        }

        CompoundTag entityTag = getSpawnEditorEntityTag(stack);
        if (entityTag != null) {
            if (entityTag.contains(ENTITY_CUSTOM_NAME_TAG, Tag.TAG_STRING)) {
                this.spawnEggCustomNameValue = readSerializedComponent(entityTag.getString(ENTITY_CUSTOM_NAME_TAG)).getString();
            }
            if (entityTag.hasUUID(ENTITY_OWNER_TAG)) {
                this.spawnEggOwnerValue = entityTag.getUUID(ENTITY_OWNER_TAG).toString();
            } else if (entityTag.contains(ENTITY_OWNER_TAG, Tag.TAG_STRING)) {
                this.spawnEggOwnerValue = entityTag.getString(ENTITY_OWNER_TAG);
            }
        }

        EntityType<?> type = getCurrentSpawnEggEntityType(stack);
        ResourceLocation id = type == null ? null : CompatRegistries.ENTITY_TYPES.getKey(type);
        if (id == null) {
            clampSpawnEggEntitySelection(getFilteredSpawnEggEntities());
            return;
        }

        List<SpawnEggEntityEntry> filtered = getFilteredSpawnEggEntities();
        for (int i = 0; i < filtered.size(); i++) {
            if (id.equals(filtered.get(i).id())) {
                this.selectedSpawnEggEntityIndex = i;
                scrollSpawnEggSelectionIntoView(filtered);
                return;
            }
        }
        clampSpawnEggEntitySelection(filtered);
    }

    protected Component readSerializedComponent(String raw) {
        if (raw == null || raw.isBlank()) {
            return Component.empty();
        }
        try {
            Component component = ComponentCompat.fromJson(raw);
            return component == null ? Component.literal(raw) : component;
        } catch (RuntimeException exception) {
            try {
                Component component = ComponentCompat.fromJsonLenient(raw);
                return component == null ? Component.literal(raw) : component;
            } catch (RuntimeException ignored) {
                return Component.literal(raw);
            }
        }
    }

    protected void updateLoreScrollFromMouse(double mouseY) {
        int spaces = loreLineSpaces();
        int max = Math.max(0, this.loreValues.size() - spaces);
        if (max == 0) {
            setLoreScroll(0);
            return;
        }
        float perc = (float) ((mouseY - loreScrollTop()) / (double) loreScrollHeight());
        setLoreScroll(Mth.clamp(Math.round(max * perc), 0, max));
    }

    protected void setLoreScroll(int value) {
        int clamped = Mth.clamp(value, 0, Math.max(0, this.loreValues.size() - loreLineSpaces()));
        if (this.loreScroll != clamped) {
            this.loreScroll = clamped;
            rebuildWidgets();
        }
    }

    protected void captureFieldValues() {
        if (this.itemIdBox != null) {
            this.itemIdValue = this.itemIdBox.getValue();
        }
        if (this.countBox != null) {
            this.countValue = this.countBox.getValue();
        }
        if (this.damageBox != null) {
            this.damageValue = this.damageBox.getValue();
        }
        if (this.nameBox != null) {
            this.nameValue = this.nameBox.getValue();
        }
        if (this.rawNbtBox != null) {
            this.rawNbtValue = this.rawNbtBox.getValue();
        }
        if (this.componentFilterBox != null) {
            this.componentFilterValue = this.componentFilterBox.getValue();
        }
        if (this.componentValueSearchBox != null) {
            this.componentValueFilterValue = this.componentValueSearchBox.getValue();
        }
        if (this.componentNbtBox != null) {
            this.componentNbtValue = this.componentNbtBox.getValue();
        }
        if (this.enchantFilterBox != null) {
            this.enchantFilterValue = this.enchantFilterBox.getValue();
        }
        if (this.enchantLevelBox != null) {
            this.enchantLevelValue = this.enchantLevelBox.getValue();
        }
        if (this.potionFilterBox != null) {
            this.potionFilterValue = this.potionFilterBox.getValue();
        }
        if (this.potionLevelBox != null) {
            this.potionLevelValue = this.potionLevelBox.getValue();
        }
        if (this.potionTimeBox != null) {
            this.potionTimeValue = this.potionTimeBox.getValue();
        }
        if (this.attributeAmountBox != null) {
            this.attributeAmountValue = this.attributeAmountBox.getValue();
        }
        if (this.attributeDecimalBox != null) {
            this.attributeDecimalValue = this.attributeDecimalBox.getValue();
        }
        if (this.colorHexBox != null) {
            this.colorHexValue = this.colorHexBox.getValue();
        }
        for (int i = 0; i < SIGN_LINES && i < this.signBoxes.size(); i++) {
            this.signLineValues[i] = this.signBoxes.get(i).getValue();
        }
        if (this.signCommandBox != null) {
            this.signCommandValue = this.signCommandBox.getValue();
        }
        if (this.bookTitleBox != null) {
            this.bookTitleValue = this.bookTitleBox.getValue();
        }
        if (this.bookAuthorBox != null) {
            this.bookAuthorValue = this.bookAuthorBox.getValue();
        }
        if (this.headOwnerBox != null) {
            this.headOwnerValue = this.headOwnerBox.getValue();
        }
        if (this.headUuidBox != null) {
            this.headUuidValue = this.headUuidBox.getValue();
        }
        if (this.headTextureBox != null) {
            this.headTextureValue = this.headTextureBox.getValue();
        }
        if (this.headTextureSignatureBox != null) {
            this.headTextureSignatureValue = this.headTextureSignatureBox.getValue();
        }
        if (this.containerSlotNbtBox != null) {
            this.containerSlotNbtValue = this.containerSlotNbtBox.getValue();
        }
        if (this.bannerPatternFilterBox != null) {
            this.bannerPatternFilterValue = this.bannerPatternFilterBox.getValue();
        }
        if (this.potterySherdFilterBox != null) {
            this.potterySherdFilterValue = this.potterySherdFilterBox.getValue();
        }
        if (this.spawnEggEntityFilterBox != null) {
            this.spawnEggEntityFilterValue = this.spawnEggEntityFilterBox.getValue();
        }
        if (this.spawnEggCustomNameBox != null) {
            this.spawnEggCustomNameValue = this.spawnEggCustomNameBox.getValue();
        }
        if (this.spawnEggOwnerBox != null) {
            this.spawnEggOwnerValue = this.spawnEggOwnerBox.getValue();
        }
        if (this.tradeItemNbtBox != null) {
            this.tradeItemNbtValue = this.tradeItemNbtBox.getValue();
        }
        if (this.tradeUsesBox != null) {
            this.tradeUsesValue = this.tradeUsesBox.getValue();
        }
        if (this.tradeMaxUsesBox != null) {
            this.tradeMaxUsesValue = this.tradeMaxUsesBox.getValue();
        }
        if (this.tradeXpBox != null) {
            this.tradeXpValue = this.tradeXpBox.getValue();
        }
        if (this.tradePriceMultiplierBox != null) {
            this.tradePriceMultiplierValue = this.tradePriceMultiplierBox.getValue();
        }
        if (this.tradeSpecialPriceBox != null) {
            this.tradeSpecialPriceValue = this.tradeSpecialPriceBox.getValue();
        }
        if (this.tradeDemandBox != null) {
            this.tradeDemandValue = this.tradeDemandBox.getValue();
        }
    }

    protected CompoundTag parseNbt(String nbt) throws CommandSyntaxException {
        String value = nbt == null ? "" : nbt.trim();
        if (value.isEmpty() || "{}".equals(value)) {
            return null;
        }
        return TagParser.parseTag(value);
    }

    protected void cleanupEmptyDisplayTag() {
        CompoundTag tag = ItemStackNbt.get(this.previewStack);
        if (tag == null) {
            return;
        }
        CompoundTag display = tag.getCompound(DISPLAY_TAG);
        if (display.isEmpty()) {
            tag.remove(DISPLAY_TAG);
        }
        cleanupEmptyTag();
    }

    protected void cleanupEmptyTag() {
        CompoundTag tag = ItemStackNbt.get(this.previewStack);
        if (tag != null && tag.isEmpty()) {
            ItemStackNbt.set(this.previewStack, null);
        }
    }

    protected int loreLineSpaces() {
        int bottom = isSidebarUi() ? sidebarBottomButtonY() - 36 : this.height - 70;
        return Math.max(1, (bottom / 30) - 1);
    }

    protected boolean isMouseIn(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    protected boolean isTradeSlotEditor() {
        return this.parentTradeScreen != null && this.parentTradeIndex >= 0 && this.parentTradeSlot >= 0;
    }

    protected boolean isNameFollowingDefault(ItemStack stack) {
        String currentName = this.nameBox == null ? this.nameValue : this.nameBox.getValue();
        return !ItemStackCompat.hasCustomHoverName(stack) && Objects.equals(currentName, getDefaultHoverName(stack));
    }

    protected String getDefaultHoverName(ItemStack stack) {
        ItemStack withoutName = stack.copy();
        ItemStackCompat.resetHoverName(withoutName);
        return withoutName.getHoverName().getString();
    }

    protected static int getDamageMaxForField(ItemStack stack) {
        return stack.isDamageableItem() ? Math.max(0, stack.getMaxDamage()) : 99999;
    }

    protected static int parsePositiveOrZero(String value) {
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    protected static String normalizeItemId(String id) {
        String value = id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
        return value.contains(":") ? value : "minecraft:" + value;
    }

    protected static String stripMinecraftNamespace(ResourceLocation id) {
        return "minecraft".equals(id.getNamespace()) ? id.getPath() : id.toString();
    }

    protected static String getInitialNbt(ItemStack stack) {
        CompoundTag tag = ItemStackNbt.get(stack);
        return tag == null || tag.isEmpty() ? "{}" : tag.toString();
    }

    protected static String getInitialComponentsNbt(ItemStack stack) {
        CompoundTag components = ItemStackNbt.save(stack).getCompound("components");
        return components.isEmpty() ? "{}" : components.toString();
    }

    protected void syncNbtEditorValuesFromStack() {
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.componentNbtValue = getInitialComponentsNbt(this.previewStack);
    }

    protected static boolean isColorApplicable(ItemStack stack) {
        return stack.is(ItemTags.DYEABLE) || isPotionItem(stack) || isMapItem(stack);
    }

    protected static boolean canShowEnchantingButton(ItemStack stack) {
        return !stack.isEmpty();
    }

    protected static boolean isPotionItem(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION) || stack.is(Items.TIPPED_ARROW);
    }

    protected static boolean isMapItem(ItemStack stack) {
        return stack.is(Items.MAP) || stack.is(Items.FILLED_MAP);
    }

    protected static boolean isSignItem(ItemStack stack) {
        return stack.getItem() instanceof SignItem;
    }

    protected static boolean isBannerEditableItem(ItemStack stack) {
        return stack.getItem() instanceof BannerItem || stack.is(Items.SHIELD);
    }

    protected static boolean isDecoratedPotItem(ItemStack stack) {
        return stack.is(Items.DECORATED_POT);
    }

    protected static boolean isSpawnEggItem(ItemStack stack) {
        return stack.getItem() instanceof SpawnEggItem;
    }

    protected static boolean isTrialSpawnerItem(ItemStack stack) {
        return stack.is(Items.TRIAL_SPAWNER);
    }

    protected static boolean isSpawnerItem(ItemStack stack) {
        return stack.is(Items.SPAWNER) || isTrialSpawnerItem(stack);
    }

    protected static boolean isSpawnEditorItem(ItemStack stack) {
        return isSpawnEggItem(stack) || isSpawnerItem(stack);
    }

    protected static boolean isCommandBlockEditableItem(ItemStack stack) {
        return stack.is(Items.COMMAND_BLOCK)
                || stack.is(Items.CHAIN_COMMAND_BLOCK)
                || stack.is(Items.REPEATING_COMMAND_BLOCK)
                || stack.is(Items.COMMAND_BLOCK_MINECART);
    }

    protected boolean isVillagerTradeEditableItem(ItemStack stack) {
        return isSpawnEggItem(stack) && EntityType.VILLAGER.equals(getCurrentSpawnEggEntityType(stack));
    }

    protected static boolean isPlayerHeadItem(ItemStack stack) {
        return stack.getItem() instanceof PlayerHeadItem;
    }

    protected static boolean isArmorStandItem(ItemStack stack) {
        return stack.is(Items.ARMOR_STAND);
    }

    protected static boolean isFireworkEditableItem(ItemStack stack) {
        return stack.is(Items.FIREWORK_ROCKET) || stack.is(Items.FIREWORK_STAR);
    }

    protected static boolean isContainerEditableItem(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        Block block = blockItem.getBlock();
        return block instanceof ChestBlock || block instanceof BarrelBlock || block instanceof ShulkerBoxBlock;
    }

    protected static boolean isBookEditableItem(ItemStack stack) {
        return stack.is(Items.WRITTEN_BOOK) || stack.is(Items.WRITABLE_BOOK);
    }

    protected int getBookGeneration() {
        CompoundTag tag = ItemStackNbt.get(this.previewStack);
        return tag == null ? 0 : Mth.clamp(tag.getInt(BOOK_GENERATION_TAG), 0, MAX_BOOK_GENERATION);
    }

    protected int getBookPageCount() {
        CompoundTag tag = ItemStackNbt.get(this.previewStack);
        return tag == null ? 0 : tag.getList(BOOK_PAGES_TAG, Tag.TAG_STRING).size();
    }

    protected Component getBookResolvedText() {
        CompoundTag tag = ItemStackNbt.get(this.previewStack);
        boolean resolved = tag != null && tag.getBoolean(BOOK_RESOLVED_TAG);
        return Component.translatable(key("book.resolved." + (resolved ? 1 : 0)));
    }

    protected Component getBookSignButtonText() {
        if (this.previewStack.is(Items.WRITTEN_BOOK)) {
            return Component.translatable(key("book.unsign"));
        }
        return Component.translatable(key(this.rememberedSignedBookData == null ? "book.sign" : "book.resign"));
    }

    protected int getFireworkFlight() {
        CompoundTag fireworks = ItemStackNbt.getElement(this.previewStack, FIREWORKS_TAG);
        if (fireworks == null) {
            return 1;
        }
        return Mth.clamp(fireworks.getByte(FIREWORK_FLIGHT_TAG), 1, MAX_FIREWORK_FLIGHT);
    }

    protected int getFireworkExplosionCount() {
        if (this.previewStack.is(Items.FIREWORK_STAR)) {
            CompoundTag tag = ItemStackNbt.get(this.previewStack);
            return tag != null && tag.contains(FIREWORK_EXPLOSION_TAG, Tag.TAG_COMPOUND) ? 1 : 0;
        }

        CompoundTag fireworks = ItemStackNbt.getElement(this.previewStack, FIREWORKS_TAG);
        if (fireworks == null || !fireworks.contains(FIREWORK_EXPLOSIONS_TAG, Tag.TAG_LIST)) {
            return 0;
        }
        return fireworks.getList(FIREWORK_EXPLOSIONS_TAG, Tag.TAG_COMPOUND).size();
    }

    protected boolean hasFireworkData() {
        CompoundTag tag = ItemStackNbt.get(this.previewStack);
        if (tag == null) {
            return false;
        }
        if (this.previewStack.is(Items.FIREWORK_ROCKET)) {
            return tag.contains(FIREWORKS_TAG, Tag.TAG_COMPOUND) && !tag.getCompound(FIREWORKS_TAG).isEmpty();
        }
        return tag.contains(FIREWORK_EXPLOSION_TAG, Tag.TAG_COMPOUND) && !tag.getCompound(FIREWORK_EXPLOSION_TAG).isEmpty();
    }

    protected Component getFireworkTypeName(int type) {
        return Component.translatable(key("firework.type." + Mth.clamp(type, 0, FIREWORK_EXPLOSION_TYPES - 1)));
    }

    protected Component getFireworkFadeColorText() {
        if (this.fireworkFadeColor < 0) {
            return Component.translatable(key("firework.fade.none"));
        }
        return getDyeColorName(getFireworkDyeColor(this.fireworkFadeColor));
    }

    protected DyeColor getFireworkDyeColor(int colorId) {
        return DyeColor.byId(Mth.positiveModulo(colorId, DyeColor.values().length));
    }

    protected static int getFireworkRgb(DyeColor color) {
        return color.getFireworkColor();
    }

    protected static FireworkExplosion.Shape getFireworkShape(int type) {
        return FIREWORK_SHAPES[Mth.clamp(type, 0, FIREWORK_SHAPES.length - 1)];
    }

    protected static int getNearestFireworkDyeColorId(int rgb) {
        int normalized = rgb & 0xFFFFFF;
        DyeColor closest = DyeColor.WHITE;
        int closestDistance = Integer.MAX_VALUE;
        for (DyeColor color : DyeColor.values()) {
            int dyeRgb = getFireworkRgb(color);
            int red = getRed(normalized) - getRed(dyeRgb);
            int green = getGreen(normalized) - getGreen(dyeRgb);
            int blue = getBlue(normalized) - getBlue(dyeRgb);
            int distance = red * red + green * green + blue * blue;
            if (distance < closestDistance) {
                closest = color;
                closestDistance = distance;
            }
        }
        return closest.getId();
    }

    protected static String normalizeHeadText(String value) {
        return value == null ? "" : value.trim();
    }

    protected static UUID parseUuidOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    protected static int getDefaultAttributeSlot(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armorItem) {
            return getAttributeSlotNumber(armorItem.getEquipmentSlot());
        }
        if (stack.is(Items.SHIELD) || stack.is(Items.TOTEM_OF_UNDYING)) {
            return 2;
        }
        return 1;
    }

    protected static int getAttributeSlotNumber(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> 1;
            case OFFHAND -> 2;
            case HEAD -> 3;
            case CHEST -> 4;
            case LEGS -> 5;
            case FEET -> 6;
            default -> 1;
        };
    }

    protected static String getAttributeSlotName(int slot) {
        return switch (slot) {
            case 1 -> EquipmentSlot.MAINHAND.getName();
            case 2 -> EquipmentSlot.OFFHAND.getName();
            case 3 -> EquipmentSlot.HEAD.getName();
            case 4 -> EquipmentSlot.CHEST.getName();
            case 5 -> EquipmentSlot.LEGS.getName();
            case 6 -> EquipmentSlot.FEET.getName();
            default -> null;
        };
    }

    protected static String formatColorHex(int color) {
        return String.format(Locale.ROOT, "#%06x", color & 0xFFFFFF);
    }

    protected static int getRed(int color) {
        return color >> 16 & 255;
    }

    protected static int getGreen(int color) {
        return color >> 8 & 255;
    }

    protected static int getBlue(int color) {
        return color & 255;
    }

    protected static int argb(int alpha, int rgb) {
        return ((alpha & 255) << 24) | (rgb & 0xFFFFFF);
    }

    protected String formatRingEnchantmentName(Enchantment enchantment) {
        Holder<Enchantment> holder = CompatRegistries.ENCHANTMENTS.getHolder(enchantment);
        return holder == null
                ? enchantment.description().getString()
                : Enchantment.getFullname(holder, getDisplayLevel(enchantment)).getString();
    }

    protected String formatStoredEnchantment(EnchantmentEntry entry) {
        if (entry.enchantment() == null) {
            return "Unknown ID (" + entry.id() + ") " + entry.level();
        }
        Holder<Enchantment> holder = CompatRegistries.ENCHANTMENTS.getHolder(entry.enchantment());
        return holder == null
                ? entry.enchantment().description().getString() + " " + entry.level()
                : Enchantment.getFullname(holder, entry.level()).getString();
    }

    protected static int findEnchantmentIndex(ListTag enchantments, ResourceLocation id) {
        for (int i = 0; i < enchantments.size(); i++) {
            ResourceLocation storedId = ResourceLocation.tryParse(enchantments.getCompound(i).getString("id"));
            if (id.equals(storedId)) {
                return i;
            }
        }
        return -1;
    }

    protected static String getEnchantmentTagKey(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) ? BOOK_ENCHANTMENTS_TAG : ITEM_ENCHANTMENTS_TAG;
    }
}

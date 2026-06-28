package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Axis;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.CreativeTabRefresher;
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
abstract class ItemEditorScreenRendering extends ItemEditorScreenWidgets {
    protected ItemEditorScreenRendering(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(stack, targetContainerSlot, parentTradeScreen, parentTradeIndex, parentTradeSlot);
    }

    protected void renderEditorBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        EditorBackgrounds.render(guiGraphics, this.width, this.height);

        if (!isSidebarUi()) {
            return;
        }

        int sidebarWidth = sidebarWidth();
        ModernUi.fillToolboxBackdrop(guiGraphics, this.width, this.height);
        ModernUi.fillContentGlow(guiGraphics, sidebarWidth, 0, this.width, Math.min(this.height, 72));
        ModernUi.fillToolboxSidebar(guiGraphics, 0, 0, sidebarWidth, this.height);
        guiGraphics.fill(sidebarWidth, SIDEBAR_SAFE_MARGIN, sidebarWidth + 1, this.height - SIDEBAR_SAFE_MARGIN, ModernUi.BORDER_SOFT);
        guiGraphics.fill(sidebarWidth + 1, SIDEBAR_SAFE_MARGIN, sidebarWidth + 7, this.height - SIDEBAR_SAFE_MARGIN, 0x4A000000);

        ModernUi.fillHeaderPanel(guiGraphics, SIDEBAR_SAFE_MARGIN, 10, sidebarWidth - SIDEBAR_SAFE_MARGIN, 82, 8);
        ModernUi.drawWindowControls(guiGraphics, SIDEBAR_SAFE_MARGIN + 8, 18);
        String appName = this.font.plainSubstrByWidth(ModSource.NAME, Math.max(20, sidebarWidth - SIDEBAR_SAFE_MARGIN * 2 - 48));
        guiGraphics.drawString(this.font, appName, SIDEBAR_SAFE_MARGIN + 44, 17, ModernUi.TEXT_SECONDARY, false);
        renderSidebarHeaderItem(guiGraphics);
        guiGraphics.drawCenteredString(this.font, activePanelTitle(), sidebarWidth / 2, 68, SIDEBAR_MUTED_COLOR);

        int left = safeLeft();
        int top = safeTop();
        int right = safeRight();
        int bottom = safeBottom();
        if (right > left && bottom > top) {
            ModernUi.fillWorkSurface(guiGraphics, left, top, right, bottom);
            int accentRight = Math.min(right - 18, left + 160);
            guiGraphics.fill(left + 18, top + 15, accentRight, top + 16, ModernUi.ACCENT_HOVER);
            renderAnimatedContentGlow(guiGraphics, left, top, right, bottom);
        }
    }

    private void renderSidebarHeaderItem(GuiGraphics guiGraphics) {
        if (this.previewStack.isEmpty()) {
            return;
        }

        int centerX = itemPreviewCenterX();
        int centerY = itemPreviewCenterY();
        ModernUi.fillItemWell(guiGraphics, centerX, centerY, 34);
        int x = centerX - 8;
        int y = centerY - 8;
        guiGraphics.renderItem(this.previewStack, x, y);
        guiGraphics.renderItemDecorations(this.font, this.previewStack, x, y);
    }

    private void renderAnimatedContentGlow(GuiGraphics guiGraphics, int left, int top, int right, int bottom) {
        int range = Math.max(1, bottom - top - 56);
        float phase = (System.currentTimeMillis() % 3600L) / 3600.0F;
        int y = top + 16 + (int) (range * phase);
        guiGraphics.fillGradient(left + 2, y, right - 2, Math.min(bottom - 2, y + 24),
                0x00FFB347, ModernUi.alpha(0xFFB347, 18));
    }

    private Component activePanelTitle() {
        return switch (this.activePanel) {
            case ITEM -> Component.translatable(key("item"));
            case NBT -> Component.translatable(key("nbt"));
            case COMPONENTS -> Component.translatable(key("components"));
            case NBT_ADVANCED -> Component.translatable(key("nbtadv"));
            case HIDE_FLAGS -> Component.translatable(key("hideflags"));
            case ENCHANTMENTS -> Component.translatable(key("enchanting"));
            case POTION -> Component.translatable(key("potion"));
            case ATTRIBUTES -> Component.translatable(key("attributes"));
            case COLOR -> Component.translatable(key("color"));
            case SIGN -> Component.translatable(key("sign"));
            case HEAD -> Component.translatable(key("head"));
            case ARMOR_STAND -> Component.translatable(key("armorstand"));
            case FIREWORK -> Component.translatable(key("firework"));
            case CONTAINER -> Component.translatable(key("container"));
            case BANNER -> Component.translatable(key("banner"));
            case DECORATED_POT -> Component.translatable(key("decorated_pot"));
            case SPAWN_EGG -> Component.translatable(key(getSpawnEditorTitleKey()));
            case TRADES -> Component.translatable(key("trades"));
            case TRADE -> Component.translatable(key("trade"));
            case BOOK -> Component.translatable(key("book"));
            case LORE -> Component.translatable(key("lore"));
            case LORE_PAINTER -> Component.translatable(key("lorepainter"));
        };
    }

    private int panelTitleColor() {
        return isSidebarUi() ? ModernUi.TEXT_PRIMARY : MAIN_COLOR;
    }

    private int panelLabelColor() {
        return isSidebarUi() ? ModernUi.TEXT_MUTED : MAIN_COLOR;
    }

    private int panelAccentColor() {
        return isSidebarUi() ? ModernUi.ACCENT_HOVER : CONTRAST_COLOR;
    }

    private int panelSecondaryColor() {
        return isSidebarUi() ? ModernUi.TEXT_SECONDARY : ALT_COLOR;
    }

    private void drawPanelTitle(GuiGraphics guiGraphics, Component title) {
        if (isSidebarUi()) {
            int textWidth = this.font.width(title);
            int plateWidth = Mth.clamp(textWidth + 38, Math.min(72, Math.max(1, contentWidth() - 36)),
                    Math.max(72, contentWidth() - 36));
            int left = contentMidX() - plateWidth / 2;
            int top = 13;
            ModernUi.fillToolDrawer(guiGraphics, left, top, left + plateWidth, top + 22, true);
            guiGraphics.drawCenteredString(this.font, title, contentMidX(), top + 7, ModernUi.TEXT_PRIMARY);
            return;
        }

        guiGraphics.drawCenteredString(this.font, title, this.midX, 15, panelTitleColor());
    }

    private void drawCenteredLabel(GuiGraphics guiGraphics, Component label, int centerX, int y) {
        guiGraphics.drawCenteredString(this.font, label, centerX, y, panelLabelColor());
    }

    private void drawModernListRow(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, boolean selected, boolean hovered) {
        if (isSidebarUi() && (selected || hovered)) {
            ModernUi.fillSelection(guiGraphics, x1, y1, x2, y2, 5, selected || hovered);
        }
    }

    protected void renderItemPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isSidebarUi()) {
            renderSidebarItemPanel(guiGraphics);
            return;
        }

        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 40);
        drawPanelTitle(guiGraphics, Component.translatable(key("item")));

        drawRightLabel(guiGraphics, Component.translatable(key("item.id")), this.midX - 5, 61);
        drawRightLabel(guiGraphics, Component.translatable(key("item.count")), this.midX - 5, 91);
        drawRightLabel(guiGraphics, Component.translatable(key("item.meta")), this.midX - 5, 121);

        guiGraphics.drawString(this.font, Component.translatable(key("item.name")), this.width - 110, 35, MAIN_COLOR);
        guiGraphics.drawString(this.font, Component.translatable(key("item.lore")), this.width - 110, 80, MAIN_COLOR);
    }

    private void renderSidebarItemPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);

        renderSidebarItemDrawers(guiGraphics);

        drawPanelTitle(guiGraphics, Component.translatable(key("item")));
        drawFieldLabel(guiGraphics, Component.translatable(key("item.id")), this.itemIdBox);
        drawFieldLabel(guiGraphics, Component.translatable(key("item.count")), this.countBox);
        drawFieldLabel(guiGraphics, Component.translatable(key("item.meta")), this.damageBox);
        drawFieldLabel(guiGraphics, Component.translatable(key("item.name")), this.nameBox);

        if (!this.loreBoxes.isEmpty()) {
            for (int i = 0; i < this.loreBoxes.size(); i++) {
                EditBox loreBox = this.loreBoxes.get(i);
                guiGraphics.drawString(this.font, Component.literal("Lore " + (i + 1)),
                        loreBox.getX(), loreBox.getY() - 9, ModernUi.TEXT_MUTED, false);
            }
        }

        if (canShowSidebarActionGrid()) {
            guiGraphics.drawString(this.font, Component.translatable(key("ui.actions")),
                    getActionGridX(), getActionGridY() - 12, ModernUi.TEXT_MUTED, false);
        }
    }

    private void renderSidebarItemDrawers(GuiGraphics guiGraphics) {
        if (this.itemIdBox != null && this.countBox != null && this.damageBox != null) {
            int left = safeLeft() + 8;
            int right = Math.min(safeRight() - 8, Math.max(this.itemIdBox.getX() + this.itemIdBox.getWidth(),
                    this.damageBox.getX() + this.damageBox.getWidth()) + 12);
            int top = Math.max(42, this.itemIdBox.getY() - 20);
            int bottom = this.damageBox.getY() + this.damageBox.getHeight() + 12;
            if (right > left && bottom > top) {
                ModernUi.fillToolDrawer(guiGraphics, left, top, right, bottom, false);
            }
        }

        if (this.nameBox != null) {
            int left = Math.max(safeLeft(), this.nameBox.getX() - SIDEBAR_DRAWER_PADDING);
            int right = Math.min(safeRight(), this.nameBox.getX() + this.nameBox.getWidth() + 58);
            int top = Math.max(42, this.nameBox.getY() - 20);
            int bottom = Math.min(sidebarBottomButtonY() - 8, sidebarNameCardBottom() + 10);
            if (right > left && bottom > top) {
                ModernUi.fillToolDrawer(guiGraphics, left, top, right, bottom, false);
            }
        }

        if (canShowSidebarActionGrid()) {
            int width = getActionGridButtonWidth();
            int left = Math.max(safeLeft(), getActionGridX() - SIDEBAR_DRAWER_PADDING);
            int right = Math.min(safeRight(), getActionGridX() + width * 2 + SIDEBAR_CONTENT_GAP + SIDEBAR_DRAWER_PADDING);
            int top = getActionGridY() - 18;
            int bottom = sidebarBottomButtonY() - 8;
            if (right > left && bottom > top) {
                ModernUi.fillToolDrawer(guiGraphics, left, top, right, bottom, false);
            }
        }
    }

    private void drawFieldLabel(GuiGraphics guiGraphics, Component text, EditBox box) {
        if (box != null) {
            guiGraphics.drawString(this.font, text, box.getX(), box.getY() - 9, ModernUi.TEXT_MUTED, false);
        }
    }

    protected void renderNbtPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 38);
        drawPanelTitle(guiGraphics, Component.translatable(key("nbt")));
        if (!this.nbtFeedback.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.nbtFeedback, this.midX, 130, this.nbtFeedbackGood ? GOOD_GREEN : BAD_RED);
        }
    }

    protected void renderComponentsPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawPanelTitle(guiGraphics, Component.translatable(key("components")));
        renderComponentEditorPanel(guiGraphics, mouseX, mouseY);
    }

    protected void renderNbtAdvancedPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!isSidebarUi()) {
            int mainColor = 0xFF1EC8FF;
            guiGraphics.fillGradient(18, 18, this.width - 18, 38, 0xFF323232, mainColor);
            guiGraphics.fill(20, 40, this.width - 20, this.height - 20, 0xDE323232);
            guiGraphics.fill(18, 38, this.width - 18, 40, mainColor);
            guiGraphics.fill(18, 40, 20, this.height - 20, mainColor);
            guiGraphics.fill(this.width - 20, 40, this.width - 18, this.height - 20, mainColor);
            guiGraphics.fill(18, this.height - 20, this.width - 18, this.height - 18, mainColor);
            guiGraphics.drawString(this.font, ModSource.MODID + " - " + Component.translatable(key("nbtadv")).getString(), 25, 26, CYAN);

            List<NbtRow> rows = buildNbtRows();
            int visibleRows = getNbtAdvancedVisibleRows();
            this.advancedScroll = Mth.clamp(this.advancedScroll, 0, Math.max(0, rows.size() - visibleRows));
            int end = Math.min(rows.size(), this.advancedScroll + visibleRows);
            for (int i = this.advancedScroll; i < end; i++) {
                NbtRow row = rows.get(i);
                int y = 48 + (i - this.advancedScroll) * 12;
                int x = 25 + row.depth() * 12;
                int color = row.isExpandable() ? MAIN_COLOR : 0xFFFFFFFF;
                guiGraphics.drawString(this.font, row.displayText(), x, y, color);
            }

            String unfinished = Component.translatable(key("nbtadv.unfinished")).getString();
            guiGraphics.drawString(this.font, unfinished, this.width - this.font.width(unfinished) - 25, this.height - 30, 0xFFFFFF32);
            return;
        }

        int left = isSidebarUi() ? safeLeft() + 10 : 18;
        int top = isSidebarUi() ? safeTop() + 10 : 18;
        int right = isSidebarUi() ? safeRight() - 10 : this.width - 18;
        int bottom = isSidebarUi() ? sidebarBottomButtonY() - 8 : this.height - 18;
        ModernUi.fillGlassPanel(guiGraphics, left, top, right, bottom, 8);
        guiGraphics.fillGradient(left + 1, top + 1, right - 1, top + 22,
                ModernUi.alpha(0xFFB347, 64), ModernUi.alpha(0x62D1C7, 18));
        guiGraphics.drawString(this.font, ModSource.MODID + " / " + Component.translatable(key("nbtadv")).getString(),
                left + 9, top + 8, ModernUi.TEXT_PRIMARY, false);

        List<NbtRow> rows = buildNbtRows();
        int visibleRows = getNbtAdvancedVisibleRows();
        this.advancedScroll = Mth.clamp(this.advancedScroll, 0, Math.max(0, rows.size() - visibleRows));
        int end = Math.min(rows.size(), this.advancedScroll + visibleRows);
        for (int i = this.advancedScroll; i < end; i++) {
            NbtRow row = rows.get(i);
            int y = top + 32 + (i - this.advancedScroll) * 12;
            int x = left + 9 + row.depth() * 12;
            int color = row.isExpandable() ? ModernUi.ACCENT_HOVER : ModernUi.TEXT_PRIMARY;
            if ((i - this.advancedScroll) % 2 == 0) {
                guiGraphics.fill(left + 5, y - 2, right - 5, y + 10, 0x171FFFFF);
            }
            guiGraphics.drawString(this.font, row.displayText(), x, y, color, false);
        }

        String unfinished = Component.translatable(key("nbtadv.unfinished")).getString();
        guiGraphics.drawString(this.font, unfinished, right - this.font.width(unfinished) - 10, bottom - 13, ModernUi.WARM);
    }

    protected void renderHideFlagsPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderSimpleItemPanelTitle(guiGraphics, "hideflags", 40);
    }

    protected void renderBookPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 40);
        drawPanelTitle(guiGraphics, Component.translatable(key("book")));
        if (this.bookTitleBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("book.title")), this.bookTitleBox.getX() - 5, this.bookTitleBox.getY() + 6);
        }
        if (this.bookAuthorBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("book.author")), this.bookAuthorBox.getX() - 5, this.bookAuthorBox.getY() + 6);
        }
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("book.pages"), getBookPageCount()),
                this.midX, 128, panelAccentColor());
    }

    protected void renderHeadPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 36);
        drawPanelTitle(guiGraphics, Component.translatable(key("head")));
        if (this.headOwnerBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("head.owner")), this.headOwnerBox.getX() - 5, this.headOwnerBox.getY() + 6);
        }
        if (this.headUuidBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("head.uuid")), this.headUuidBox.getX() - 5, this.headUuidBox.getY() + 6);
        }
        if (this.headTextureBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("head.texture")), this.headTextureBox.getX() - 5, this.headTextureBox.getY() + 6);
        }
        if (this.headTextureSignatureBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("head.signature")),
                    this.headTextureSignatureBox.getX() - 5, this.headTextureSignatureBox.getY() + 6);
        }
    }

    protected void renderArmorStandPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 36);
        drawPanelTitle(guiGraphics, Component.translatable(key("armorstand")));
    }

    protected void renderFireworkPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 36);
        drawPanelTitle(guiGraphics, Component.translatable(key("firework")));
        int infoX = Math.min(this.width - 155, this.midX + 96);
        guiGraphics.drawString(this.font, Component.translatable(key("firework.explosions"), getFireworkExplosionCount()),
                infoX, 58, panelAccentColor());
    }

    protected void renderSimpleItemPanelTitle(GuiGraphics guiGraphics, String titleKey, int itemY) {
        renderSmallItem(guiGraphics, this.midX, itemY);
        drawPanelTitle(guiGraphics, Component.translatable(key(titleKey)));
    }

    protected void renderEnchantmentsPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = contentMidX();
        drawPanelTitle(guiGraphics, Component.translatable(key("enchanting")));
        drawCenteredLabel(guiGraphics, Component.translatable(key("enchanting.search")),
                this.enchantFilterBox.getX() + this.enchantFilterBox.getWidth() / 2,
                this.enchantFilterBox.getY() - 12);
        renderActiveEnchantments(guiGraphics);
        updateMouseDistance(mouseX, mouseY);
        renderLargePreviewItem(guiGraphics);

        List<Enchantment> filteredEnchantments = getVisibleEnchantments(this.previewStack);
        List<EnchantmentGroupEntry> enchantmentGroups = getFoldedEnchantmentGroups(this.previewStack);
        if (!enchantmentGroups.isEmpty()) {
            int radius = getRingRadius();
            double angle = (2.0D * Math.PI) / enchantmentGroups.size();
            double rotation = (this.rotOff + (Math.abs(this.mouseDist - radius) >= RING_HOVER_WIDTH ? partialTick : 0.0D)) / 60.0D;
            for (int i = 0; i < enchantmentGroups.size(); i++) {
                EnchantmentGroupEntry group = enchantmentGroups.get(i);
                double groupAngle = rotation + angle * i;
                int x = (int) (centerX + radius * Math.cos(groupAngle));
                int y = (int) (this.midY + radius * Math.sin(groupAngle));
                String label = Component.translatable(key("registry_group.entry"), group.namespace(), group.enchantments().size()).getString();
                guiGraphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(label, 118), x, y - 17, panelTitleColor());
                guiGraphics.renderItem(this.enchantBook, x - 8, y - 8);
                guiGraphics.fill(x - 1, y - 1, x + 1, y + 1, isSidebarUi() ? ModernUi.ACCENT_HOVER : 0xFFFFFFFF);
            }
            return;
        }

        if (filteredEnchantments.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("no_enchantment_matches")),
                    centerX, this.midY + 34, panelAccentColor());
            return;
        }

        int radius = getRingRadius();
        double angle = (2.0D * Math.PI) / filteredEnchantments.size();
        double rotation = (this.rotOff + (Math.abs(this.mouseDist - radius) >= RING_HOVER_WIDTH ? partialTick : 0.0D)) / 60.0D;
        for (int i = 0; i < filteredEnchantments.size(); i++) {
            Enchantment enchantment = filteredEnchantments.get(i);
            double enchantmentAngle = rotation + angle * i;
            int x = (int) (centerX + radius * Math.cos(enchantmentAngle));
            int y = (int) (this.midY + radius * Math.sin(enchantmentAngle));
            guiGraphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(formatRingEnchantmentName(enchantment), 118), x, y - 17, panelTitleColor());
            guiGraphics.renderItem(this.enchantBook, x - 8, y - 8);
            guiGraphics.fill(x - 1, y - 1, x + 1, y + 1, isSidebarUi() ? ModernUi.ACCENT_HOVER : 0xFFFFFFFF);
        }

        if (isMouseOverCenter(mouseX, mouseY)) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("enchanting.addall")),
                    centerX, this.midY, panelAccentColor());
        }
    }

    protected void renderPotionPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = contentMidX();
        drawPanelTitle(guiGraphics, Component.translatable(key("potion")));
        List<MobEffectInstance> activeEffects = getCustomPotionEffects();
        int start = this.midY - 5 * activeEffects.size();
        for (int i = 0; i < activeEffects.size(); i++) {
            MobEffectInstance effect = activeEffects.get(i);
            int rowY = start + i * 10;
            if (isSidebarUi()) {
                ModernUi.fillSelection(guiGraphics, editorListTextLeft() - 4, rowY - 2,
                        editorListTextLeft() + Math.min(174, this.font.width(formatPotionEffect(effect)) + 12), rowY + 10, 4, false);
            }
            int color = effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL ? BAD_RED : panelAccentColor();
            guiGraphics.drawString(this.font, formatPotionEffect(effect), editorListTextLeft(), rowY, color, false);
        }

        drawCenteredLabel(guiGraphics, Component.translatable(key("enchanting.search")),
                this.potionFilterBox.getX() + this.potionFilterBox.getWidth() / 2,
                this.potionFilterBox.getY() - 12);
        guiGraphics.drawString(this.font, Component.translatable(key("potion.time")), editorControlLeft() + 47, this.height - 56, panelLabelColor());
        guiGraphics.drawString(this.font, Component.translatable(key("potion.level")), editorControlLeft() + 47, this.height - 29, panelLabelColor());

        updateMouseDistance(mouseX, mouseY);
        renderLargePreviewItem(guiGraphics);

        List<MobEffect> filteredEffects = getFilteredPotionEffects();
        if (filteredEffects.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("no_potion_matches")),
                    centerX, this.midY + 34, panelAccentColor());
            return;
        }

        int radius = getRingRadius();
        double angle = (2.0D * Math.PI) / filteredEffects.size();
        double rotation = (this.rotOff + (Math.abs(this.mouseDist - radius) >= RING_HOVER_WIDTH ? partialTick : 0.0D)) / 60.0D;
        for (int i = 0; i < filteredEffects.size(); i++) {
            MobEffect effect = filteredEffects.get(i);
            double effectAngle = rotation + angle * i;
            int x = (int) (centerX + radius * Math.cos(effectAngle));
            int y = (int) (this.midY + radius * Math.sin(effectAngle));
            guiGraphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(formatPotionRingName(effect), 118), x, y - 17, panelTitleColor());
            guiGraphics.renderItem(this.potionIcon, x - 8, y - 8);
            guiGraphics.fill(x - 1, y - 1, x + 1, y + 1, isSidebarUi() ? ModernUi.ACCENT_HOVER : 0xFFFFFFFF);
        }

        if (isMouseOverCenter(mouseX, mouseY)) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("enchanting.addall")),
                    centerX, this.midY, panelAccentColor());
        }
    }

    protected void renderAttributesPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = contentMidX();
        drawPanelTitle(guiGraphics, Component.translatable(key("attributes")));
        drawCenteredLabel(guiGraphics, Component.translatable(key("enchanting.search")),
                this.attributeFilterBox.getX() + this.attributeFilterBox.getWidth() / 2,
                this.attributeFilterBox.getY() - 12);
        List<AttributeEntry> entries = getAttributeModifierEntries();
        int start = this.midY - 5 * entries.size();
        for (int i = 0; i < entries.size(); i++) {
            int rowY = start + i * 10;
            if (isSidebarUi()) {
                ModernUi.fillSelection(guiGraphics, editorListTextLeft() - 4, rowY - 2,
                        editorListTextLeft() + Math.min(188, this.font.width(formatAttributeEntry(entries.get(i))) + 12), rowY + 10, 4, false);
            }
            guiGraphics.drawString(this.font, formatAttributeEntry(entries.get(i)), editorListTextLeft(), rowY, panelTitleColor(), false);
        }

        guiGraphics.drawString(this.font, ".", editorControlLeft() + 81, this.height - 26, panelTitleColor());
        updateMouseDistance(mouseX, mouseY);
        renderLargePreviewItem(guiGraphics);

        List<Attribute> attributes = getVisibleAttributes();
        List<AttributeGroupEntry> attributeGroups = getFoldedAttributeGroups();
        if (!attributeGroups.isEmpty()) {
            int radius = getRingRadius();
            double angle = (2.0D * Math.PI) / attributeGroups.size();
            double rotation = (this.rotOff + (Math.abs(this.mouseDist - radius) >= RING_HOVER_WIDTH ? partialTick : 0.0D)) / 60.0D;
            for (int i = 0; i < attributeGroups.size(); i++) {
                AttributeGroupEntry group = attributeGroups.get(i);
                double groupAngle = rotation + angle * i;
                int x = (int) (centerX + radius * Math.cos(groupAngle));
                int y = (int) (this.midY + radius * Math.sin(groupAngle));
                String label = Component.translatable(key("registry_group.entry"), group.namespace(), group.attributes().size()).getString();
                guiGraphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(label, 118), x, y - 17, panelTitleColor());
                guiGraphics.renderItem(this.attributeIcon, x - 8, y - 8);
                guiGraphics.fill(x - 1, y - 1, x + 1, y + 1, isSidebarUi() ? ModernUi.ACCENT_HOVER : 0xFFFFFFFF);
            }
            return;
        }

        if (attributes.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("no_attribute_matches")),
                    centerX, this.midY + 34, panelAccentColor());
            return;
        }

        int radius = getRingRadius();
        double angle = (2.0D * Math.PI) / attributes.size();
        double rotation = (this.rotOff + (Math.abs(this.mouseDist - radius) >= RING_HOVER_WIDTH ? partialTick : 0.0D)) / 60.0D;
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            double attributeAngle = rotation + angle * i;
            int x = (int) (centerX + radius * Math.cos(attributeAngle));
            int y = (int) (this.midY + radius * Math.sin(attributeAngle));
            guiGraphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(Component.translatable(attribute.getDescriptionId()).getString(), 118),
                    x, y - 17, panelTitleColor());
            guiGraphics.renderItem(this.attributeIcon, x - 8, y - 8);
            guiGraphics.fill(x - 1, y - 1, x + 1, y + 1, isSidebarUi() ? ModernUi.ACCENT_HOVER : 0xFFFFFFFF);
        }
    }

    protected void renderColorPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(4.0F, 4.0F, 1.0F);
        guiGraphics.renderItem(this.previewStack, isSidebarUi() ? this.midX / 4 - 8 : this.width / 8 - 8, 5);
        guiGraphics.pose().popPose();

        drawPanelTitle(guiGraphics, Component.translatable(key("color")));

        int color = getEditorColor();
        if (this.redSlider != null && this.greenSlider != null && this.blueSlider != null) {
            if (isSidebarUi()) {
                ModernUi.fillPanel(guiGraphics, this.redSlider.getX() - 9, this.redSlider.getY() - 9,
                        this.blueSlider.getX() + this.blueSlider.getWidth() + 9,
                        this.blueSlider.getY() + this.blueSlider.getHeight() + 9,
                        8, ModernUi.lerpColor(ModernUi.SURFACE_SOFT, argb(110, color), 0.35F), ModernUi.BORDER);
                guiGraphics.fill(this.redSlider.getX() - 2, this.redSlider.getY() - 2,
                        this.redSlider.getX() + this.redSlider.getWidth() + 2,
                        this.redSlider.getY() + this.redSlider.getHeight() + 2,
                        argb(120, getRed(color) << 16));
                guiGraphics.fill(this.greenSlider.getX() - 2, this.greenSlider.getY() - 2,
                        this.greenSlider.getX() + this.greenSlider.getWidth() + 2,
                        this.greenSlider.getY() + this.greenSlider.getHeight() + 2,
                        argb(120, getGreen(color) << 8));
                guiGraphics.fill(this.blueSlider.getX() - 2, this.blueSlider.getY() - 2,
                        this.blueSlider.getX() + this.blueSlider.getWidth() + 2,
                        this.blueSlider.getY() + this.blueSlider.getHeight() + 2,
                        argb(120, getBlue(color)));
            } else {
                guiGraphics.fill(this.redSlider.getX() - 5, this.redSlider.getY() - 5,
                        this.blueSlider.getX() + this.blueSlider.getWidth() + 5,
                        this.blueSlider.getY() + this.blueSlider.getHeight() + 5,
                        argb(100, color));
                guiGraphics.fill(this.redSlider.getX() - 2, this.redSlider.getY() - 2,
                        this.redSlider.getX() + this.redSlider.getWidth() + 2,
                        this.redSlider.getY() + this.redSlider.getHeight() + 2,
                        argb(255, getRed(color) << 16));
                guiGraphics.fill(this.greenSlider.getX() - 2, this.greenSlider.getY() - 2,
                        this.greenSlider.getX() + this.greenSlider.getWidth() + 2,
                        this.greenSlider.getY() + this.greenSlider.getHeight() + 2,
                        argb(255, getGreen(color) << 8));
                guiGraphics.fill(this.blueSlider.getX() - 2, this.blueSlider.getY() - 2,
                        this.blueSlider.getX() + this.blueSlider.getWidth() + 2,
                        this.blueSlider.getY() + this.blueSlider.getHeight() + 2,
                        argb(255, getBlue(color)));
            }
            renderDyeGrid(guiGraphics);
        }
    }

    protected void renderSignPanel(GuiGraphics guiGraphics) {
        renderSmallItem(guiGraphics, this.midX, 35);
        drawPanelTitle(guiGraphics, Component.translatable(key("sign")));

        for (int i = 0; i < SIGN_LINES && i < this.signBoxes.size(); i++) {
            EditBox box = this.signBoxes.get(i);
            drawRightLabel(guiGraphics, Component.translatable(key("sign.line"), i + 1), box.getX() - 5, box.getY() + 6);
        }
        if (this.signCommandBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("sign.command")),
                    this.signCommandBox.getX() - 5, this.signCommandBox.getY() + 6);
        }
    }

    protected void renderContainerPanel(GuiGraphics guiGraphics) {
        renderSmallItem(guiGraphics, this.midX, 34);
        drawPanelTitle(guiGraphics, Component.translatable(key("container")));

        int gridX = getContainerGridX();
        int gridY = getContainerGridY();
        if (isSidebarUi()) {
            ModernUi.fillPanel(guiGraphics, gridX - 7, gridY - 7,
                    gridX + CONTAINER_COLUMNS * CONTAINER_SLOT_PIXEL_SIZE + 7,
                    gridY + CONTAINER_ROWS * CONTAINER_SLOT_PIXEL_SIZE + 7, 8, 0xA8101821, ModernUi.BORDER);
        }
        for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
            int x = gridX + (slot % CONTAINER_COLUMNS) * CONTAINER_SLOT_PIXEL_SIZE;
            int y = gridY + (slot / CONTAINER_COLUMNS) * CONTAINER_SLOT_PIXEL_SIZE;
            boolean selected = slot == this.selectedContainerSlot;
            if (isSidebarUi()) {
                ModernUi.fillSlot(guiGraphics, x, y, selected);
            } else {
                guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, selected ? CONTRAST_COLOR : 0xFF555555);
                guiGraphics.fill(x, y, x + ITEM_SIZE, y + ITEM_SIZE, 0xFF1C1C1C);
            }

            ItemStack slotStack = getContainerSlotItem(slot);
            if (!slotStack.isEmpty()) {
                guiGraphics.renderItem(slotStack, x, y);
                guiGraphics.renderItemDecorations(this.font, slotStack, x, y);
            }
        }

        Component selected = Component.translatable(key("container.slot"), this.selectedContainerSlot + 1);
        Component itemCount = Component.translatable(key("container.items"), getContainerItemCount());
        int infoY = gridY + CONTAINER_ROWS * CONTAINER_SLOT_PIXEL_SIZE + 6;
        guiGraphics.drawString(this.font, selected, gridX, infoY, isSidebarUi() ? ModernUi.TEXT_PRIMARY : MAIN_COLOR);
        guiGraphics.drawString(this.font, itemCount,
                gridX + CONTAINER_COLUMNS * CONTAINER_SLOT_PIXEL_SIZE - this.font.width(itemCount), infoY,
                isSidebarUi() ? ModernUi.TEXT_MUTED : ALT_COLOR);
        if (this.containerSlotNbtBox != null) {
            drawCenteredLabel(guiGraphics, Component.translatable(key("container.slot_nbt")),
                    this.containerSlotNbtBox.getX() + this.containerSlotNbtBox.getWidth() / 2,
                    this.containerSlotNbtBox.getY() - 12);
        }
    }

    protected void renderBannerPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        drawPanelTitle(guiGraphics, Component.translatable(key("banner")));

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.midX, 78, 100.0F);
        guiGraphics.pose().scale(4.0F, 4.0F, 1.0F);
        guiGraphics.renderItem(this.previewStack, -8, -8);
        guiGraphics.pose().popPose();

        drawCenteredLabel(guiGraphics, Component.translatable(key("banner.search")),
                this.bannerPatternFilterBox.getX() + this.bannerPatternFilterBox.getWidth() / 2,
                this.bannerPatternFilterBox.getY() - 12);

        List<BannerPatternEntry> patterns = getFilteredBannerPatterns();
        clampBannerPatternSelection(patterns);
        int listX = bannerPatternListX();
        int listWidth = bannerPatternListWidth();
        if (isSidebarUi()) {
            ModernUi.fillPanel(guiGraphics, listX - 5, getBannerPatternRowY(0) - 7, listX + listWidth + 5,
                    getBannerPatternRowY(BANNER_PATTERN_ROWS - 1) + 15, 8, ModernUi.SURFACE, ModernUi.BORDER);
        }
        if (patterns.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable(key("banner.no_match")),
                    listX + 2, getBannerPatternRowY(0), BAD_RED);
        } else {
            int end = Math.min(patterns.size(), this.bannerPatternScroll + BANNER_PATTERN_ROWS);
            for (int i = this.bannerPatternScroll; i < end; i++) {
                int y = getBannerPatternRowY(i - this.bannerPatternScroll);
                boolean selectedPattern = i == this.selectedBannerPatternIndex;
                int color = isSidebarUi()
                        ? (selectedPattern ? ModernUi.ACCENT_HOVER : ModernUi.TEXT_PRIMARY)
                        : (selectedPattern ? CONTRAST_COLOR : MAIN_COLOR);
                Component name = getBannerPatternName(patterns.get(i), getBannerPatternColor());
                drawModernListRow(guiGraphics, listX, y - 3, listX + listWidth, y + 10, selectedPattern, false);
                guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(name.getString(), listWidth - 5),
                        listX + 2, y, color);
            }
        }

        Component selected = patterns.isEmpty()
                ? Component.translatable(key("banner.no_match"))
                : getBannerPatternName(patterns.get(this.selectedBannerPatternIndex), getBannerPatternColor());
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("banner.selected"), selected),
                this.midX, this.height - 78, panelTitleColor());

        renderBannerPatternLayers(guiGraphics);
    }

    protected void renderDecoratedPotPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        drawPanelTitle(guiGraphics, Component.translatable(key("decorated_pot")));

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.midX, 78, 100.0F);
        guiGraphics.pose().scale(4.0F, 4.0F, 1.0F);
        guiGraphics.renderItem(this.previewStack, -8, -8);
        guiGraphics.pose().popPose();

        drawCenteredLabel(guiGraphics, Component.translatable(key("decorated_pot.search")),
                this.potterySherdFilterBox.getX() + this.potterySherdFilterBox.getWidth() / 2,
                this.potterySherdFilterBox.getY() - 12);

        List<PotterySherdEntry> sherds = getFilteredPotterySherds();
        clampPotterySherdSelection(sherds);
        int listX = potterySherdListX();
        int listWidth = potterySherdListWidth();
        if (isSidebarUi()) {
            ModernUi.fillPanel(guiGraphics, listX - 5, getPotterySherdRowY(0) - 7, listX + listWidth + 5,
                    getPotterySherdRowY(POTTERY_SHERD_ROWS - 1) + 15, 8, ModernUi.SURFACE, ModernUi.BORDER);
        }
        if (sherds.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable(key("decorated_pot.no_match")),
                    listX + 2, getPotterySherdRowY(0), BAD_RED);
        } else {
            int end = Math.min(sherds.size(), this.potterySherdScroll + POTTERY_SHERD_ROWS);
            for (int i = this.potterySherdScroll; i < end; i++) {
                int y = getPotterySherdRowY(i - this.potterySherdScroll);
                boolean selectedSherd = i == this.selectedPotterySherdIndex;
                int color = isSidebarUi()
                        ? (selectedSherd ? ModernUi.ACCENT_HOVER : ModernUi.TEXT_PRIMARY)
                        : (selectedSherd ? CONTRAST_COLOR : MAIN_COLOR);
                Component name = getPotterySherdName(sherds.get(i));
                drawModernListRow(guiGraphics, listX, y - 3, listX + listWidth, y + 10, selectedSherd, false);
                guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(name.getString(), listWidth - 5),
                        listX + 2, y, color);
            }
        }

        Component selected = sherds.isEmpty()
                ? Component.translatable(key("decorated_pot.no_match"))
                : getPotterySherdName(sherds.get(this.selectedPotterySherdIndex));
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("decorated_pot.selected"), selected),
                this.midX, this.height - 78, panelTitleColor());
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("decorated_pot.editing"),
                getDecoratedPotSideName(this.selectedDecoratedPotSide), getDecoratedPotSideItemName(this.selectedDecoratedPotSide)),
                this.midX, 112, panelSecondaryColor());

        renderDecoratedPotSides(guiGraphics);
    }

    protected void renderSpawnEggPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        drawPanelTitle(guiGraphics, Component.translatable(key(getSpawnEditorTitleKey())));

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.midX, 78, 100.0F);
        guiGraphics.pose().scale(4.0F, 4.0F, 1.0F);
        guiGraphics.renderItem(this.previewStack, -8, -8);
        guiGraphics.pose().popPose();

        drawCenteredLabel(guiGraphics, Component.translatable(key("spawnegg.search")),
                this.spawnEggEntityFilterBox.getX() + this.spawnEggEntityFilterBox.getWidth() / 2,
                this.spawnEggEntityFilterBox.getY() - 12);

        List<SpawnEggEntityEntry> entities = getFilteredSpawnEggEntities();
        clampSpawnEggEntitySelection(entities);
        int listX = spawnEggEntityListX();
        int listWidth = spawnEggEntityListWidth();
        if (isSidebarUi()) {
            ModernUi.fillPanel(guiGraphics, listX - 5, getSpawnEggEntityRowY(0) - 7, listX + listWidth + 5,
                    getSpawnEggEntityRowY(SPAWN_EGG_ENTITY_ROWS - 1) + 15, 8, ModernUi.SURFACE, ModernUi.BORDER);
        }
        if (entities.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable(key("spawnegg.no_match")),
                    listX + 2, getSpawnEggEntityRowY(0), BAD_RED);
        } else {
            int end = Math.min(entities.size(), this.spawnEggEntityScroll + SPAWN_EGG_ENTITY_ROWS);
            for (int i = this.spawnEggEntityScroll; i < end; i++) {
                int y = getSpawnEggEntityRowY(i - this.spawnEggEntityScroll);
                boolean selectedEntity = i == this.selectedSpawnEggEntityIndex;
                int color = isSidebarUi()
                        ? (selectedEntity ? ModernUi.ACCENT_HOVER : ModernUi.TEXT_PRIMARY)
                        : (selectedEntity ? CONTRAST_COLOR : MAIN_COLOR);
                drawModernListRow(guiGraphics, listX, y - 3, listX + listWidth, y + 10, selectedEntity, false);
                guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(formatSpawnEggEntityEntry(entities.get(i)), listWidth - 5),
                        listX + 2, y, color);
            }
        }

        Component selected = entities.isEmpty()
                ? Component.translatable(key("spawnegg.no_match"))
                : getSpawnEggEntityName(entities.get(this.selectedSpawnEggEntityIndex));
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("spawnegg.selected"), selected),
                this.midX, this.height - 78, panelTitleColor());
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("spawnegg.current"), getCurrentSpawnEggEntityName()),
                this.midX, 120, panelSecondaryColor());

        int controlsX = getSpawnEggControlsX();
        drawCenteredLabel(guiGraphics, Component.translatable(key("spawnegg.tags")),
                controlsX + getSpawnEggControlsWidth() / 2, getSpawnEggTagRowY(0) - 12);

        List<SpawnEggTagRow> rows = getSpawnEggTagRows();
        this.spawnEggTagScroll = Mth.clamp(this.spawnEggTagScroll, 0, Math.max(0, rows.size() - SPAWN_EGG_TAG_ROWS));
        int end = Math.min(rows.size(), this.spawnEggTagScroll + SPAWN_EGG_TAG_ROWS);
        for (int i = this.spawnEggTagScroll; i < end; i++) {
            SpawnEggTagRow row = rows.get(i);
            if (row.type() != SpawnEggTagRowType.BOOLEAN && row.type() != SpawnEggTagRowType.CHOICE) {
                drawRightLabel(guiGraphics, Component.translatable(key("spawnegg." + row.translationSuffix())),
                        controlsX + 66, getSpawnEggTagRowY(i - this.spawnEggTagScroll) + 6);
            }
        }
    }

    protected void renderTradesPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderSmallItem(guiGraphics, this.midX, 35);
        drawPanelTitle(guiGraphics, Component.translatable(key("trades")));

        ListTag trades = getVillagerTradeRecipes();
        int size = trades.size();
        boolean foundHover = false;
        if (isSidebarUi()) {
            int listWidth = Math.min(getTradeListWidth(), Math.max(1, contentWidth() - 48));
            int top = size == 0 ? getTradeListRowY(0, 0) - 8 : getTradeListRowY(0, size) - 8;
            int bottom = getTradeListRowY(size, size) + 18;
            ModernUi.fillPanel(guiGraphics, this.midX - listWidth / 2 - 8, top,
                    this.midX + listWidth / 2 + 8, bottom, 8, ModernUi.SURFACE, ModernUi.BORDER);
        }
        for (int i = 0; i < size; i++) {
            String tradeText = formatTradeRecipe(trades.getCompound(i));
            boolean hovered = !foundHover && isMouseOverCenteredText(mouseX, mouseY, tradeText, this.midX, getTradeListRowY(i, size));
            foundHover = foundHover || hovered;
            int rowY = getTradeListRowY(i, size);
            if (isSidebarUi() && (hovered || i == this.selectedTradeIndex)) {
                int rowWidth = Math.min(getTradeListWidth(), Math.max(1, contentWidth() - 48));
                ModernUi.fillSelection(guiGraphics, this.midX - rowWidth / 2, rowY - 5, this.midX + rowWidth / 2, rowY + 12,
                        5, true);
            }
            guiGraphics.drawCenteredString(this.font, tradeText, this.midX, getTradeListRowY(i, size),
                    isSidebarUi()
                            ? (hovered ? ModernUi.ACCENT_HOVER : ModernUi.TEXT_PRIMARY)
                            : (hovered ? CONTRAST_COLOR : MAIN_COLOR));
        }

        Component addTrade = Component.translatable(key("trades.addtrade"));
        String addTradeText = addTrade.getString();
        boolean addHovered = !foundHover && isMouseOverCenteredText(mouseX, mouseY, addTradeText,
                this.midX, getTradeListRowY(size, size));
        guiGraphics.drawCenteredString(this.font, addTrade, this.midX, getTradeListRowY(size, size),
                isSidebarUi()
                        ? (addHovered ? ModernUi.ACCENT_HOVER : ModernUi.WARM)
                        : (addHovered ? CONTRAST_COLOR : MAIN_COLOR));
    }

    protected void renderTradePanel(GuiGraphics guiGraphics) {
        renderSmallItem(guiGraphics, this.midX, 35);
        drawPanelTitle(guiGraphics, Component.translatable(key("trade")));

        if (this.tradeItemNbtBox != null) {
            drawCenteredLabel(guiGraphics, Component.translatable(key("trades.item_nbt")),
                    this.tradeItemNbtBox.getX() + this.tradeItemNbtBox.getWidth() / 2,
                    this.tradeItemNbtBox.getY() - this.font.lineHeight - 4);
        }
        drawTradeFieldLabels(guiGraphics);

        if (isSidebarUi()) {
            int tradeTop = this.midY - 26;
            ModernUi.fillPanel(guiGraphics, contentMidX() - Math.min(190, contentWidth() / 2), tradeTop,
                    contentMidX() + Math.min(190, contentWidth() / 2), this.midY + 42, 8, ModernUi.SURFACE, ModernUi.BORDER);
        }
        for (int slot = 0; slot < TRADE_SLOT_COUNT; slot++) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("trades.slot." + slot)),
                    getSingleTradeSlotX(slot) + 8, this.midY - 10,
                    slot == TRADE_SLOT_SELL ? panelTitleColor() : panelAccentColor());
        }

        CompoundTag recipe = getSelectedTradeRecipe();
        if (recipe == null) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("trades.no_trades")),
                    this.midX, this.midY + 26, panelAccentColor());
            return;
        }

        renderTradeSlotItem(guiGraphics, recipe, TRADE_SLOT_FIRST_BUY);
        renderTradeSlotItem(guiGraphics, recipe, TRADE_SLOT_SECOND_BUY);
        renderTradeSlotItem(guiGraphics, recipe, TRADE_SLOT_SELL);
    }

    protected void renderLorePanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderSmallItem(guiGraphics, this.midX, 35);
        drawPanelTitle(guiGraphics, Component.translatable(key("lore")));
        int spaces = loreLineSpaces();
        int size = this.loreValues.size();
        this.loreScroll = Mth.clamp(this.loreScroll, 0, Math.max(0, size - spaces));
        int y = 25;
        for (int i = this.loreScroll; i < this.loreScroll + spaces && i < size; i++) {
            String label = "Line " + (i + 1);
            y = 55 + 30 * (i - this.loreScroll);
            guiGraphics.drawString(this.font, label, loreLineLabelRightX() - this.font.width(label), y + 6,
                    isSidebarUi() ? ModernUi.TEXT_MUTED : 0xFFFFFFFF);
        }

        int actionY = y + 30;
        for (InfinityEditorButton button : this.loreActionButtons) {
            button.setY(actionY);
        }
        if (this.copyLoreButton != null) {
            this.copyLoreButton.active = size > 0;
        }

        int scrollX = loreScrollBarX();
        int scrollTop = loreScrollTop();
        int scrollHeight = loreScrollHeight();
        if (isSidebarUi()) {
            ModernUi.fillRounded(guiGraphics, scrollX, scrollTop, scrollX + 6, scrollTop + scrollHeight, 3,
                    ModernUi.alpha(0x000000, 90));
        } else {
            guiGraphics.hLine(this.width - 15, this.width - 5, 50, 0xFFAAAAAA);
            guiGraphics.hLine(this.width - 15, this.width - 5, this.height - 50, 0xFFAAAAAA);
            scrollHeight = this.height - 103;
            scrollTop = 52;
        }
        float covered = size < spaces || size == 0 ? 1.0F : spaces / (float) size;
        int coveredHeight = (int) Math.max(1, scrollHeight * covered);
        float div = size - spaces;
        float perc = div <= 0.0F ? 0.0F : this.loreScroll / div;
        int scrollY = (int) (scrollTop + (scrollHeight - coveredHeight) * perc);
        if (isSidebarUi()) {
            ModernUi.fillRounded(guiGraphics, scrollX + 1, scrollY, scrollX + 5, scrollY + coveredHeight, 2, ModernUi.ACCENT_HOVER);
        } else {
            guiGraphics.fill(this.width - 14, scrollY, this.width - 5, scrollY + coveredHeight, 0xFF666666);
        }
    }

    protected void renderLorePainterPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ensureLorePainterRows();
        if (this.lorePainterDragging) {
            paintLorePainterAt(mouseX, mouseY);
        }

        drawPanelTitle(guiGraphics, Component.translatable(key("lorepainter")));

        int gridX = getLorePainterGridX();
        int gridY = getLorePainterGridY();
        guiGraphics.drawCenteredString(this.font, this.lorePainterWidth + "x" + this.lorePainterHeight,
                this.midX, gridY - 15, panelLabelColor());

        if (isSidebarUi()) {
            ModernUi.fillPanel(guiGraphics, gridX - 8, gridY - 8,
                    gridX + getLorePainterSizeX() + 8, gridY + getLorePainterSizeY() + 8,
                    8, ModernUi.SURFACE, ModernUi.BORDER);
        }

        int yOffset = 0;
        for (List<LorePixel> row : this.lorePainterRows) {
            guiGraphics.drawString(this.font, buildLorePainterRow(row), gridX, gridY + yOffset, 0xFFFFFFFF, false);
            yOffset += 9;
        }

        int rowButtonY = gridY + getLorePainterSizeY() + 9;
        if (this.lorePainterAddRowButton != null) {
            this.lorePainterAddRowButton.setY(rowButtonY);
        }
        if (this.lorePainterRemoveRowButton != null) {
            this.lorePainterRemoveRowButton.setY(rowButtonY);
            this.lorePainterRemoveRowButton.active = this.lorePainterHeight > 1;
        }

        int columnButtonX = gridX + getLorePainterSizeX() + 9;
        if (this.lorePainterAddColumnButton != null) {
            this.lorePainterAddColumnButton.setX(columnButtonX);
        }
        if (this.lorePainterRemoveColumnButton != null) {
            this.lorePainterRemoveColumnButton.setX(columnButtonX);
            this.lorePainterRemoveColumnButton.active = this.lorePainterWidth > 1;
        }

        String symbols = buildLorePainterSymbols();
        guiGraphics.drawString(this.font, symbols, 0, 0, 0xFFFFFFFF, false);
        guiGraphics.drawString(this.font, this.currentLorePixel.format() + Component.translatable(key("lorepainter.symbol." + this.currentLorePixel.symbol.nameKey())).getString(),
                2, 10, 0xFFFFFFFF, false);

        String colors = buildLorePainterColors();
        int colorX = getLorePainterColorX();
        guiGraphics.drawString(this.font, colors, colorX, 0, 0xFFFFFFFF, false);
        String colorName = this.currentLorePixel.format() + this.currentLorePixel.color.getName();
        guiGraphics.drawString(this.font, colorName, this.width - this.font.width(colorName) - 2, 10, 0xFFFFFFFF, false);
    }

    protected void renderItemTooltipPreview(GuiGraphics guiGraphics) {
        if (this.previewStack.isEmpty() || this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        if (isSidebarUi()) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.8F, 0.8F, 1.0F);
        guiGraphics.renderTooltip(this.font, this.previewStack, 0, 25);
        guiGraphics.pose().popPose();
    }

    protected void renderPrettyNbt(GuiGraphics guiGraphics) {
        if (isSidebarUi()) {
            return;
        }
        List<Component> lines = getPrettyNbtLines();
        if (!lines.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(0.8F, 0.8F, 1.0F);
            guiGraphics.renderComponentTooltip(this.font, lines, 0, this.height);
            guiGraphics.pose().popPose();
        }
    }

    protected void renderSmallItem(GuiGraphics guiGraphics, int centerX, int centerY) {
        if (this.previewStack.isEmpty()) {
            return;
        }
        if (isSidebarUi() && centerY <= 45 && centerX != itemPreviewCenterX()) {
            return;
        }
        if (isSidebarUi()) {
            ModernUi.fillItemWell(guiGraphics, centerX, centerY, centerY > 45 ? 42 : 34);
        }
        int x = centerX - 8;
        int y = centerY - 8;
        guiGraphics.renderItem(this.previewStack, x, y);
        guiGraphics.renderItemDecorations(this.font, this.previewStack, x, y);
    }

    protected void renderLargePreviewItem(GuiGraphics guiGraphics) {
        if (this.previewStack.isEmpty()) {
            return;
        }

        int centerX = contentMidX();
        if (isSidebarUi()) {
            ModernUi.fillSoftHalo(guiGraphics, centerX, this.midY + 18, 104, 34, ModernUi.alpha(0xFFB347, 22));
            ModernUi.fillSoftHalo(guiGraphics, centerX, this.midY + 21, 76, 18, ModernUi.alpha(0x62D1C7, 18));
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, this.midY, 100.0F);
        guiGraphics.pose().mulPose(Axis.ZN.rotationDegrees(this.rotOff * 3.0F));
        guiGraphics.pose().scale(5.0F, 5.0F, 1.0F);
        guiGraphics.renderItem(this.previewStack, -8, -8);
        guiGraphics.pose().popPose();
    }

    protected void renderPanelTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.activePanel == Panel.ITEM) {
            int previewX = itemPreviewCenterX() - ITEM_SIZE / 2;
            int previewY = itemPreviewCenterY() - ITEM_SIZE / 2;
            if (isMouseIn(mouseX, mouseY, previewX, previewY, ITEM_SIZE, ITEM_SIZE)) {
                guiGraphics.renderTooltip(this.font, this.previewStack, mouseX, mouseY);
            }
            if (this.itemIdBox != null && this.itemIdBox.getValue().length() > 9
                    && isMouseIn(mouseX, mouseY, this.itemIdBox.getX(), this.itemIdBox.getY(), this.itemIdBox.getWidth(), this.itemIdBox.getHeight())) {
                guiGraphics.renderTooltip(this.font, Component.literal(this.itemIdBox.getValue()), mouseX, mouseY);
            }
            int dropX = isSidebarUi() ? itemPanelDropButtonX() : this.midX + 30;
            int dropY = isSidebarUi() ? sidebarBottomButtonY() : this.height - 35;
            int dropWidth = isSidebarUi() ? itemPanelDropButtonWidth() : OLD_BUTTON_WIDTH;
            int dropHeight = isSidebarUi() ? SIDEBAR_BUTTON_HEIGHT : OLD_BUTTON_HEIGHT;
            if (isMouseIn(mouseX, mouseY, dropX, dropY, dropWidth, dropHeight)) {
                guiGraphics.renderComponentTooltip(this.font, List.of(
                        Component.translatable(key("item.drop.tooltip.1")),
                        Component.translatable(key("item.drop.tooltip.2"))), mouseX, mouseY);
            }
        } else if (this.activePanel == Panel.LORE) {
            for (EditBox box : this.loreBoxes) {
                if (isMouseIn(mouseX, mouseY, box.getX(), box.getY(), box.getWidth(), box.getHeight())) {
                    String value = box.getValue().replace(ChatFormatting.PREFIX_CODE, '&');
                    if (!value.isEmpty()) {
                        guiGraphics.renderTooltip(this.font, Component.literal(value), mouseX, mouseY);
                    }
                    return;
                }
            }
            int previewX = isSidebarUi() ? itemPreviewCenterX() - ITEM_SIZE / 2 : this.midX - 9;
            int previewY = isSidebarUi() ? itemPreviewCenterY() - ITEM_SIZE / 2 : 27;
            if (isMouseIn(mouseX, mouseY, previewX, previewY, 18, 18)) {
                guiGraphics.renderTooltip(this.font, this.previewStack, mouseX, mouseY);
            }
        } else if (this.activePanel == Panel.CONTAINER) {
            int slot = getHoveredContainerSlot(mouseX, mouseY);
            if (slot >= 0) {
                ItemStack slotStack = getContainerSlotItem(slot);
                if (!slotStack.isEmpty()) {
                    guiGraphics.renderTooltip(this.font, slotStack, mouseX, mouseY);
                } else {
                    guiGraphics.renderTooltip(this.font, Component.translatable(key("container.empty_slot")), mouseX, mouseY);
                }
                return;
            }
            if (this.containerSlotNbtBox != null && this.containerSlotNbtBox.getValue().length() > 20
                    && isMouseIn(mouseX, mouseY, this.containerSlotNbtBox.getX(), this.containerSlotNbtBox.getY(),
                    this.containerSlotNbtBox.getWidth(), this.containerSlotNbtBox.getHeight())) {
                guiGraphics.renderTooltip(this.font, Component.literal(this.containerSlotNbtBox.getValue()), mouseX, mouseY);
            }
        } else if (this.activePanel == Panel.TRADES) {
            if (getHoveredTradeListIndex(mouseX, mouseY) >= 0) {
                guiGraphics.renderComponentTooltip(this.font, List.of(
                        Component.translatable(key("trades.leftclick")),
                        Component.translatable(key("trades.rightclick"))), mouseX, mouseY);
                return;
            }
            int resetX = this.midX - 30;
            int resetY = this.height - 35;
            int resetWidth = OLD_BUTTON_WIDTH;
            int resetHeight = OLD_BUTTON_HEIGHT;
            if (isSidebarUi()) {
                int gap = 6;
                resetWidth = sidebarBottomButtonWidth(3, gap);
                resetX = sidebarBottomStartX(3, resetWidth, gap) + resetWidth + gap;
                resetY = sidebarBottomButtonY();
                resetHeight = SIDEBAR_BUTTON_HEIGHT;
            }
            if (isMouseIn(mouseX, mouseY, resetX, resetY, resetWidth, resetHeight)) {
                guiGraphics.renderTooltip(this.font, Component.translatable(key("trades.reset")), mouseX, mouseY);
            }
        } else if (this.activePanel == Panel.TRADE) {
            int slot = getHoveredSingleTradeSlot(mouseX, mouseY);
            if (slot >= 0) {
                CompoundTag recipe = getSelectedTradeRecipe();
                ItemStack stack = recipe == null ? ItemStack.EMPTY : getTradeSlotItem(recipe, slot);
                if (!stack.isEmpty()) {
                    guiGraphics.renderTooltip(this.font, stack, mouseX, mouseY);
                }
                guiGraphics.renderTooltip(this.font, Component.translatable(key("trade.click_to_edit")), mouseX, mouseY - 16);
            }
        } else if (this.activePanel == Panel.LORE_PAINTER) {
            if (this.lorePainterScaleButton != null && isMouseIn(mouseX, mouseY,
                    this.lorePainterScaleButton.getX(), this.lorePainterScaleButton.getY(),
                    this.lorePainterScaleButton.getWidth(), this.lorePainterScaleButton.getHeight())
                    && this.minecraft != null) {
                guiGraphics.renderTooltip(this.font, Component.literal(String.valueOf(this.minecraft.options.guiScale().get())), mouseX, mouseY);
                return;
            }
            boolean hoveringPreview = this.lorePainterPreviewButton != null && isMouseIn(mouseX, mouseY,
                    this.lorePainterPreviewButton.getX(), this.lorePainterPreviewButton.getY(),
                    this.lorePainterPreviewButton.getWidth(), this.lorePainterPreviewButton.getHeight());
            if (this.lorePainterPreview || hoveringPreview) {
                List<Component> lines = new ArrayList<>();
                lines.add(Component.translatable(key("lorepainter")));
                for (List<LorePixel> row : this.lorePainterRows) {
                    lines.add(Component.literal(buildLorePainterRow(row)));
                }
                guiGraphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
            }
        }
    }

    private int itemPanelDropButtonX() {
        int gap = 6;
        int count = isTradeSlotEditor() ? 3 : 4;
        int buttonWidth = sidebarBottomButtonWidth(count, gap);
        int dropIndex = isTradeSlotEditor() ? 2 : 3;
        return sidebarBottomStartX(count, buttonWidth, gap) + dropIndex * (buttonWidth + gap);
    }

    private int itemPanelDropButtonWidth() {
        return sidebarBottomButtonWidth(isTradeSlotEditor() ? 3 : 4, 6);
    }

    protected void drawRightLabel(GuiGraphics guiGraphics, Component text, int rightX, int y) {
        guiGraphics.drawString(this.font, text, rightX - this.font.width(text), y, panelLabelColor());
    }

    protected void drawTradeFieldLabels(GuiGraphics guiGraphics) {
        if (this.tradeUsesBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("trades.uses")),
                    this.tradeUsesBox.getX() - 5, this.tradeUsesBox.getY() + 6);
        }
        if (this.tradeMaxUsesBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("trades.max_uses")),
                    this.tradeMaxUsesBox.getX() - 5, this.tradeMaxUsesBox.getY() + 6);
        }
        if (this.tradeXpBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("trades.xp")),
                    this.tradeXpBox.getX() - 5, this.tradeXpBox.getY() + 6);
        }
        if (this.tradeSpecialPriceBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("trades.special_price")),
                    this.tradeSpecialPriceBox.getX() - 5, this.tradeSpecialPriceBox.getY() + 6);
        }
        if (this.tradeDemandBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("trades.demand")),
                    this.tradeDemandBox.getX() - 5, this.tradeDemandBox.getY() + 6);
        }
        if (this.tradePriceMultiplierBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("trades.price_multiplier")),
                    this.tradePriceMultiplierBox.getX() - 5, this.tradePriceMultiplierBox.getY() + 6);
        }
    }

    protected boolean handleEnchantingClick(double mouseX, double mouseY) {
        if (tryRemoveActiveEnchantment(mouseX, mouseY)) {
            return true;
        }
        if (trySelectRingEnchantmentGroup(mouseX, mouseY)) {
            return true;
        }
        if (isMouseOverCenter(mouseX, mouseY)) {
            addMatchingEnchantments();
            return true;
        }
        return tryAddRingEnchantment(mouseX, mouseY);
    }

    protected boolean handlePotionClick(double mouseX, double mouseY) {
        if (tryRemoveActivePotionEffect(mouseX, mouseY)) {
            return true;
        }
        if (isMouseOverCenter(mouseX, mouseY)) {
            addMatchingPotionEffects();
            return true;
        }
        return tryAddRingPotionEffect(mouseX, mouseY);
    }

    protected boolean handleAttributesClick(double mouseX, double mouseY) {
        if (tryRemoveActiveAttributeModifier(mouseX, mouseY)) {
            return true;
        }
        if (trySelectRingAttributeGroup(mouseX, mouseY)) {
            return true;
        }
        return tryAddRingAttribute(mouseX, mouseY);
    }

    protected boolean handleColorClick(double mouseX, double mouseY) {
        if (!shouldShowDyeGrid() || this.blueSlider == null) {
            return false;
        }

        int columns = dyeGridColumns();
        int cellSize = dyeGridCellSize();
        int gridX = dyeGridX(columns, cellSize);
        int gridY = this.blueSlider.getY() + this.blueSlider.getHeight() + 10;
        if (!isMouseIn(mouseX, mouseY, gridX, gridY, columns * cellSize, dyeGridRows(columns) * cellSize)) {
            return false;
        }

        int column = ((int) mouseX - gridX) / cellSize;
        int row = ((int) mouseY - gridY) / cellSize;
        int index = column + row * columns;
        DyeColor[] colors = DyeColor.values();
        if (index < 0 || index >= colors.length) {
            return false;
        }

        addDyeToColor(colors[index]);
        syncColorControlsFromStack();
        this.status = Component.translatable(messageKey("editor_color_updated"), this.colorHexValue);
        return true;
    }

    protected boolean handleContainerClick(double mouseX, double mouseY) {
        int gridX = getContainerGridX();
        int gridY = getContainerGridY();
        int gridWidth = CONTAINER_COLUMNS * CONTAINER_SLOT_PIXEL_SIZE;
        int gridHeight = CONTAINER_ROWS * CONTAINER_SLOT_PIXEL_SIZE;
        if (!isMouseIn(mouseX, mouseY, gridX - 1, gridY - 1, gridWidth + 2, gridHeight + 2)) {
            return false;
        }

        int column = ((int) mouseX - gridX) / CONTAINER_SLOT_PIXEL_SIZE;
        int row = ((int) mouseY - gridY) / CONTAINER_SLOT_PIXEL_SIZE;
        int slot = column + row * CONTAINER_COLUMNS;
        if (column < 0 || column >= CONTAINER_COLUMNS || row < 0 || row >= CONTAINER_ROWS || slot < 0 || slot >= CONTAINER_SIZE) {
            return false;
        }

        this.selectedContainerSlot = slot;
        this.containerSlotNbtValue = getContainerSelectedSlotNbt();
        if (this.containerSlotNbtBox != null) {
            this.containerSlotNbtBox.setValue(this.containerSlotNbtValue);
            this.containerSlotNbtBox.setCursorPosition(0);
        }
        return true;
    }

    protected boolean handleBannerClick(double mouseX, double mouseY) {
        if (!isMouseIn(mouseX, mouseY, bannerPatternListX(), getBannerPatternRowY(0) - 1,
                bannerPatternListWidth(), BANNER_PATTERN_ROWS * 10 + 2)) {
            return false;
        }

        List<BannerPatternEntry> patterns = getFilteredBannerPatterns();
        if (patterns.isEmpty()) {
            return false;
        }

        int row = ((int) mouseY - getBannerPatternRowY(0)) / 10;
        int index = this.bannerPatternScroll + row;
        if (row < 0 || row >= BANNER_PATTERN_ROWS || index < 0 || index >= patterns.size()) {
            return false;
        }

        this.selectedBannerPatternIndex = index;
        return true;
    }

    protected boolean handleDecoratedPotClick(double mouseX, double mouseY) {
        if (!isMouseIn(mouseX, mouseY, potterySherdListX(), getPotterySherdRowY(0) - 1,
                potterySherdListWidth(), POTTERY_SHERD_ROWS * 10 + 2)) {
            return false;
        }

        List<PotterySherdEntry> sherds = getFilteredPotterySherds();
        if (sherds.isEmpty()) {
            return false;
        }

        int row = ((int) mouseY - getPotterySherdRowY(0)) / 10;
        int index = this.potterySherdScroll + row;
        if (row < 0 || row >= POTTERY_SHERD_ROWS || index < 0 || index >= sherds.size()) {
            return false;
        }

        this.selectedPotterySherdIndex = index;
        return true;
    }

    protected boolean handleSpawnEggClick(double mouseX, double mouseY) {
        if (!isMouseIn(mouseX, mouseY, spawnEggEntityListX(), getSpawnEggEntityRowY(0) - 1,
                spawnEggEntityListWidth(), SPAWN_EGG_ENTITY_ROWS * 10 + 2)) {
            return false;
        }

        List<SpawnEggEntityEntry> entities = getFilteredSpawnEggEntities();
        if (entities.isEmpty()) {
            return false;
        }

        int row = ((int) mouseY - getSpawnEggEntityRowY(0)) / 10;
        int index = this.spawnEggEntityScroll + row;
        if (row < 0 || row >= SPAWN_EGG_ENTITY_ROWS || index < 0 || index >= entities.size()) {
            return false;
        }

        this.selectedSpawnEggEntityIndex = index;
        this.spawnEggTagScroll = 0;
        rebuildWidgets();
        return true;
    }

    protected boolean handleTradesClick(double mouseX, double mouseY, int button) {
        int index = getHoveredTradeListIndex((int) mouseX, (int) mouseY);
        if (index >= 0) {
            if (button == 0) {
                openVillagerTrade(index);
                return true;
            }
            if (button == 1) {
                removeVillagerTrade(index);
                return true;
            }
        }

        if (button == 0 && isMouseOverAddTrade((int) mouseX, (int) mouseY)) {
            addVillagerTrade();
            return true;
        }

        return false;
    }

    protected boolean handleTradeClick(double mouseX, double mouseY) {
        int slot = getHoveredSingleTradeSlot((int) mouseX, (int) mouseY);
        if (slot < 0) {
            return false;
        }

        openTradeSlotItemEditor(slot);
        return true;
    }

    protected boolean handleLoreClick(double mouseX, double mouseY) {
        if (isMouseIn(mouseX, mouseY, loreScrollBarX(), loreScrollTop(),
                isSidebarUi() ? 6 : 11, loreScrollHeight())) {
            this.draggingLoreScroll = true;
            updateLoreScrollFromMouse(mouseY);
            return true;
        }
        return false;
    }

    protected boolean handleLorePainterClick(double mouseX, double mouseY) {
        this.lorePainterDragging = true;
        paintLorePainterAt(mouseX, mouseY);
        return true;
    }

    protected boolean handleNbtAdvancedClick(double mouseX, double mouseY) {
        if (mouseX < 20 || mouseX > this.width - 20 || mouseY < 40 || mouseY > this.height - 20) {
            return false;
        }

        int rowIndex = this.advancedScroll + ((int) mouseY - 48) / 12;
        List<NbtRow> rows = buildNbtRows();
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return false;
        }

        NbtRow row = rows.get(rowIndex);
        if (!row.isExpandable()) {
            return false;
        }

        if (this.expandedNbtPaths.contains(row.path())) {
            this.expandedNbtPaths.remove(row.path());
        } else {
            this.expandedNbtPaths.add(row.path());
        }
        return true;
    }
}

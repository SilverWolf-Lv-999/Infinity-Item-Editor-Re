package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Axis;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.CreativeTabRefresher;
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
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;

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

    protected void renderEditorBackground(GuiGraphics guiGraphics) {
        renderBackground(guiGraphics);

        if (!isSidebarUi()) {
            return;
        }

        int sidebarWidth = sidebarWidth();
        guiGraphics.fill(0, 0, this.width, this.height, 0x64000000);
        guiGraphics.fill(0, 0, sidebarWidth, this.height, SIDEBAR_PANEL_COLOR);
        guiGraphics.fill(sidebarWidth, SIDEBAR_SAFE_MARGIN, sidebarWidth + 1, this.height - SIDEBAR_SAFE_MARGIN, SIDEBAR_BORDER_COLOR);
        guiGraphics.fill(sidebarWidth + 1, SIDEBAR_SAFE_MARGIN, sidebarWidth + 3, this.height - SIDEBAR_SAFE_MARGIN, 0x54000000);
        guiGraphics.drawCenteredString(this.font, Component.literal(ModSource.NAME), sidebarWidth / 2, 13, SIDEBAR_ACCENT_COLOR);

        int left = safeLeft();
        int top = safeTop();
        int right = safeRight();
        int bottom = safeBottom();
        if (right > left && bottom > top) {
            guiGraphics.fill(left, top, right, bottom, SIDEBAR_CARD_SOFT_COLOR);
            guiGraphics.fill(left, top, right, top + 1, SIDEBAR_BORDER_COLOR);
            guiGraphics.fill(left, bottom - 1, right, bottom, SIDEBAR_BORDER_COLOR);
            guiGraphics.fill(left, top, left + 1, bottom, SIDEBAR_BORDER_COLOR);
            guiGraphics.fill(right - 1, top, right, bottom, SIDEBAR_BORDER_COLOR);
            guiGraphics.fill(left, top + 1, right, top + 3, 0x662EC8FF);
        }

        if (this.activePanel == Panel.ITEM) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("ui.sidebar")), sidebarWidth / 2, 78, SIDEBAR_MUTED_COLOR);
        }
    }

    protected void renderItemPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 40);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("item")), this.midX, 15, MAIN_COLOR);

        drawRightLabel(guiGraphics, Component.translatable(key("item.id")), this.midX - 5, 61);
        drawRightLabel(guiGraphics, Component.translatable(key("item.count")), this.midX - 5, 91);
        drawRightLabel(guiGraphics, Component.translatable(key("item.meta")), this.midX - 5, 121);

        guiGraphics.drawString(this.font, Component.translatable(key("item.name")), this.width - 110, 35, MAIN_COLOR);
        guiGraphics.drawString(this.font, Component.translatable(key("item.lore")), this.width - 110, 80, MAIN_COLOR);
    }

    protected void renderNbtPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 38);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("nbt")), this.midX, 15, MAIN_COLOR);
        if (!this.nbtFeedback.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.nbtFeedback, this.midX, 130, this.nbtFeedbackGood ? GOOD_GREEN : BAD_RED);
        }
    }

    protected void renderNbtAdvancedPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
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
    }

    protected void renderHideFlagsPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderSimpleItemPanelTitle(guiGraphics, "hideflags", 40);
    }

    protected void renderBookPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 40);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("book")), this.midX, 15, MAIN_COLOR);
        if (this.bookTitleBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("book.title")), this.bookTitleBox.getX() - 5, this.bookTitleBox.getY() + 6);
        }
        if (this.bookAuthorBox != null) {
            drawRightLabel(guiGraphics, Component.translatable(key("book.author")), this.bookAuthorBox.getX() - 5, this.bookAuthorBox.getY() + 6);
        }
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("book.pages"), getBookPageCount()),
                this.midX, 128, CONTRAST_COLOR);
    }

    protected void renderHeadPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 36);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("head")), this.midX, 15, MAIN_COLOR);
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
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("armorstand")), this.midX, 15, MAIN_COLOR);
    }

    protected void renderFireworkPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);
        renderSmallItem(guiGraphics, this.midX, 36);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("firework")), this.midX, 15, MAIN_COLOR);
        int infoX = Math.min(this.width - 155, this.midX + 96);
        guiGraphics.drawString(this.font, Component.translatable(key("firework.explosions"), getFireworkExplosionCount()),
                infoX, 58, CONTRAST_COLOR);
    }

    protected void renderSimpleItemPanelTitle(GuiGraphics guiGraphics, String titleKey, int itemY) {
        renderSmallItem(guiGraphics, this.midX, itemY);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key(titleKey)), this.midX, 15, MAIN_COLOR);
    }

    protected void renderEnchantmentsPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("enchanting")), this.midX, 15, MAIN_COLOR);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("enchanting.search")),
                this.enchantFilterBox.getX() + this.enchantFilterBox.getWidth() / 2,
                this.enchantFilterBox.getY() - 12, MAIN_COLOR);
        renderActiveEnchantments(guiGraphics);
        updateMouseDistance(mouseX, mouseY);
        renderLargePreviewItem(guiGraphics);

        List<Enchantment> filteredEnchantments = getFilteredEnchantments(this.previewStack);
        if (filteredEnchantments.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("no_enchantment_matches")),
                    this.midX, this.midY + 34, CONTRAST_COLOR);
            return;
        }

        int radius = getRingRadius();
        double angle = (2.0D * Math.PI) / filteredEnchantments.size();
        double rotation = (this.rotOff + (Math.abs(this.mouseDist - radius) >= RING_HOVER_WIDTH ? partialTick : 0.0D)) / 60.0D;
        for (int i = 0; i < filteredEnchantments.size(); i++) {
            Enchantment enchantment = filteredEnchantments.get(i);
            double enchantmentAngle = rotation + angle * i;
            int x = (int) (this.midX + radius * Math.cos(enchantmentAngle));
            int y = (int) (this.midY + radius * Math.sin(enchantmentAngle));
            guiGraphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(formatRingEnchantmentName(enchantment), 118), x, y - 17, MAIN_COLOR);
            guiGraphics.renderItem(this.enchantBook, x - 8, y - 8);
            guiGraphics.fill(x - 1, y - 1, x + 1, y + 1, 0xFFFFFFFF);
        }

        if (isMouseOverCenter(mouseX, mouseY)) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("enchanting.addall")),
                    this.midX, this.midY, CONTRAST_COLOR);
        }
    }

    protected void renderPotionPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("potion")), this.midX, 15, MAIN_COLOR);
        List<MobEffectInstance> activeEffects = getCustomPotionEffects();
        int start = this.midY - 5 * activeEffects.size();
        for (int i = 0; i < activeEffects.size(); i++) {
            MobEffectInstance effect = activeEffects.get(i);
            int color = effect.getEffect().getCategory() == MobEffectCategory.HARMFUL ? BAD_RED : CONTRAST_COLOR;
            guiGraphics.drawString(this.font, formatPotionEffect(effect), 5, start + i * 10, color);
        }

        guiGraphics.drawCenteredString(this.font, Component.translatable(key("enchanting.search")),
                this.potionFilterBox.getX() + this.potionFilterBox.getWidth() / 2,
                this.potionFilterBox.getY() - 12, MAIN_COLOR);
        guiGraphics.drawString(this.font, Component.translatable(key("potion.time")), 62, this.height - 56, MAIN_COLOR);
        guiGraphics.drawString(this.font, Component.translatable(key("potion.level")), 62, this.height - 29, MAIN_COLOR);

        updateMouseDistance(mouseX, mouseY);
        renderLargePreviewItem(guiGraphics);

        List<MobEffect> filteredEffects = getFilteredPotionEffects();
        if (filteredEffects.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("no_potion_matches")),
                    this.midX, this.midY + 34, CONTRAST_COLOR);
            return;
        }

        int radius = getRingRadius();
        double angle = (2.0D * Math.PI) / filteredEffects.size();
        double rotation = (this.rotOff + (Math.abs(this.mouseDist - radius) >= RING_HOVER_WIDTH ? partialTick : 0.0D)) / 60.0D;
        for (int i = 0; i < filteredEffects.size(); i++) {
            MobEffect effect = filteredEffects.get(i);
            double effectAngle = rotation + angle * i;
            int x = (int) (this.midX + radius * Math.cos(effectAngle));
            int y = (int) (this.midY + radius * Math.sin(effectAngle));
            guiGraphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(formatPotionRingName(effect), 118), x, y - 17, MAIN_COLOR);
            guiGraphics.renderItem(this.potionIcon, x - 8, y - 8);
            guiGraphics.fill(x - 1, y - 1, x + 1, y + 1, 0xFFFFFFFF);
        }

        if (isMouseOverCenter(mouseX, mouseY)) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("enchanting.addall")),
                    this.midX, this.midY, CONTRAST_COLOR);
        }
    }

    protected void renderAttributesPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("attributes")), this.midX, 15, MAIN_COLOR);
        List<AttributeEntry> entries = getAttributeModifierEntries();
        int start = this.midY - 5 * entries.size();
        for (int i = 0; i < entries.size(); i++) {
            guiGraphics.drawString(this.font, formatAttributeEntry(entries.get(i)), 5, start + i * 10, MAIN_COLOR);
        }

        guiGraphics.drawString(this.font, ".", 96, this.height - 26, MAIN_COLOR);
        updateMouseDistance(mouseX, mouseY);
        renderLargePreviewItem(guiGraphics);

        List<Attribute> attributes = getSharedAttributes();
        int radius = getRingRadius();
        double angle = (2.0D * Math.PI) / attributes.size();
        double rotation = (this.rotOff + (Math.abs(this.mouseDist - radius) >= RING_HOVER_WIDTH ? partialTick : 0.0D)) / 60.0D;
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            double attributeAngle = rotation + angle * i;
            int x = (int) (this.midX + radius * Math.cos(attributeAngle));
            int y = (int) (this.midY + radius * Math.sin(attributeAngle));
            guiGraphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(Component.translatable(attribute.getDescriptionId()).getString(), 118),
                    x, y - 17, MAIN_COLOR);
            guiGraphics.renderItem(this.attributeIcon, x - 8, y - 8);
            guiGraphics.fill(x - 1, y - 1, x + 1, y + 1, 0xFFFFFFFF);
        }
    }

    protected void renderColorPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        renderPrettyNbt(guiGraphics);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(4.0F, 4.0F, 1.0F);
        guiGraphics.renderItem(this.previewStack, this.width / 8 - 8, 5);
        guiGraphics.pose().popPose();

        guiGraphics.drawCenteredString(this.font, Component.translatable(key("color")), this.midX, 15, MAIN_COLOR);

        int color = getEditorColor();
        if (this.redSlider != null && this.greenSlider != null && this.blueSlider != null) {
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
            renderDyeGrid(guiGraphics);
        }
    }

    protected void renderSignPanel(GuiGraphics guiGraphics) {
        renderSmallItem(guiGraphics, this.midX, 35);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("sign")), this.midX, 15, MAIN_COLOR);

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
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("container")), this.midX, 15, MAIN_COLOR);

        int gridX = getContainerGridX();
        int gridY = getContainerGridY();
        for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
            int x = gridX + (slot % CONTAINER_COLUMNS) * CONTAINER_SLOT_PIXEL_SIZE;
            int y = gridY + (slot / CONTAINER_COLUMNS) * CONTAINER_SLOT_PIXEL_SIZE;
            boolean selected = slot == this.selectedContainerSlot;
            guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, selected ? CONTRAST_COLOR : 0xFF555555);
            guiGraphics.fill(x, y, x + ITEM_SIZE, y + ITEM_SIZE, 0xFF1C1C1C);

            ItemStack slotStack = getContainerSlotItem(slot);
            if (!slotStack.isEmpty()) {
                guiGraphics.renderItem(slotStack, x, y);
                guiGraphics.renderItemDecorations(this.font, slotStack, x, y);
            }
        }

        Component selected = Component.translatable(key("container.slot"), this.selectedContainerSlot + 1);
        Component itemCount = Component.translatable(key("container.items"), getContainerItemCount());
        int infoY = gridY + CONTAINER_ROWS * CONTAINER_SLOT_PIXEL_SIZE + 6;
        guiGraphics.drawString(this.font, selected, gridX, infoY, MAIN_COLOR);
        guiGraphics.drawString(this.font, itemCount,
                gridX + CONTAINER_COLUMNS * CONTAINER_SLOT_PIXEL_SIZE - this.font.width(itemCount), infoY, ALT_COLOR);
        if (this.containerSlotNbtBox != null) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key("container.slot_nbt")),
                    this.containerSlotNbtBox.getX() + this.containerSlotNbtBox.getWidth() / 2,
                    this.containerSlotNbtBox.getY() - 12, MAIN_COLOR);
        }
    }

    protected void renderBannerPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("banner")), this.midX, 15, MAIN_COLOR);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.midX, 78, 100.0F);
        guiGraphics.pose().scale(4.0F, 4.0F, 1.0F);
        guiGraphics.renderItem(this.previewStack, -8, -8);
        guiGraphics.pose().popPose();

        guiGraphics.drawCenteredString(this.font, Component.translatable(key("banner.search")),
                this.bannerPatternFilterBox.getX() + this.bannerPatternFilterBox.getWidth() / 2,
                this.bannerPatternFilterBox.getY() - 12, MAIN_COLOR);

        List<BannerPatternEntry> patterns = getFilteredBannerPatterns();
        clampBannerPatternSelection(patterns);
        if (patterns.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable(key("banner.no_match")), 12, 58, BAD_RED);
        } else {
            int end = Math.min(patterns.size(), this.bannerPatternScroll + BANNER_PATTERN_ROWS);
            for (int i = this.bannerPatternScroll; i < end; i++) {
                int y = getBannerPatternRowY(i - this.bannerPatternScroll);
                int color = i == this.selectedBannerPatternIndex ? CONTRAST_COLOR : MAIN_COLOR;
                Component name = getBannerPatternName(patterns.get(i), getBannerPatternColor());
                guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(name.getString(), 145), 12, y, color);
            }
        }

        Component selected = patterns.isEmpty()
                ? Component.translatable(key("banner.no_match"))
                : getBannerPatternName(patterns.get(this.selectedBannerPatternIndex), getBannerPatternColor());
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("banner.selected"), selected),
                this.midX, this.height - 78, MAIN_COLOR);

        renderBannerPatternLayers(guiGraphics);
    }

    protected void renderSpawnEggPanel(GuiGraphics guiGraphics) {
        renderItemTooltipPreview(guiGraphics);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key(getSpawnEditorTitleKey())), this.midX, 15, MAIN_COLOR);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.midX, 78, 100.0F);
        guiGraphics.pose().scale(4.0F, 4.0F, 1.0F);
        guiGraphics.renderItem(this.previewStack, -8, -8);
        guiGraphics.pose().popPose();

        guiGraphics.drawCenteredString(this.font, Component.translatable(key("spawnegg.search")),
                this.spawnEggEntityFilterBox.getX() + this.spawnEggEntityFilterBox.getWidth() / 2,
                this.spawnEggEntityFilterBox.getY() - 12, MAIN_COLOR);

        List<SpawnEggEntityEntry> entities = getFilteredSpawnEggEntities();
        clampSpawnEggEntitySelection(entities);
        if (entities.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable(key("spawnegg.no_match")), 12, 58, BAD_RED);
        } else {
            int end = Math.min(entities.size(), this.spawnEggEntityScroll + SPAWN_EGG_ENTITY_ROWS);
            for (int i = this.spawnEggEntityScroll; i < end; i++) {
                int y = getSpawnEggEntityRowY(i - this.spawnEggEntityScroll);
                int color = i == this.selectedSpawnEggEntityIndex ? CONTRAST_COLOR : MAIN_COLOR;
                guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(formatSpawnEggEntityEntry(entities.get(i)), 170),
                        12, y, color);
            }
        }

        Component selected = entities.isEmpty()
                ? Component.translatable(key("spawnegg.no_match"))
                : getSpawnEggEntityName(entities.get(this.selectedSpawnEggEntityIndex));
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("spawnegg.selected"), selected),
                this.midX, this.height - 78, MAIN_COLOR);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("spawnegg.current"), getCurrentSpawnEggEntityName()),
                this.midX, 120, ALT_COLOR);

        int controlsX = getSpawnEggControlsX();
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("spawnegg.tags")),
                controlsX + getSpawnEggControlsWidth() / 2, getSpawnEggTagRowY(0) - 12, MAIN_COLOR);

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
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("trades")), this.midX, 15, MAIN_COLOR);

        ListTag trades = getVillagerTradeRecipes();
        int size = trades.size();
        boolean foundHover = false;
        for (int i = 0; i < size; i++) {
            String tradeText = formatTradeRecipe(trades.getCompound(i));
            boolean hovered = !foundHover && isMouseOverCenteredText(mouseX, mouseY, tradeText, this.midX, getTradeListRowY(i, size));
            foundHover = foundHover || hovered;
            guiGraphics.drawCenteredString(this.font, tradeText, this.midX, getTradeListRowY(i, size),
                    hovered ? CONTRAST_COLOR : MAIN_COLOR);
        }

        Component addTrade = Component.translatable(key("trades.addtrade"));
        String addTradeText = addTrade.getString();
        boolean addHovered = !foundHover && isMouseOverCenteredText(mouseX, mouseY, addTradeText,
                this.midX, getTradeListRowY(size, size));
        guiGraphics.drawCenteredString(this.font, addTrade, this.midX, getTradeListRowY(size, size),
                addHovered ? CONTRAST_COLOR : MAIN_COLOR);
    }

    protected void renderTradePanel(GuiGraphics guiGraphics) {
        renderSmallItem(guiGraphics, this.midX, 35);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("trade")), this.midX, 15, MAIN_COLOR);

        if (this.tradeMaxUsesBox != null) {
            guiGraphics.drawCenteredString(this.font, Component.literal("Max Uses"),
                    this.width / 2, this.tradeMaxUsesBox.getY() - this.font.lineHeight - 4, MAIN_COLOR);
        }

        int part = this.midX / 2;
        guiGraphics.drawCenteredString(this.font, Component.literal("Price 1"), part + 8, this.midY - 10, CONTRAST_COLOR);
        guiGraphics.drawCenteredString(this.font, Component.literal("Price 2"), 2 * part + 8, this.midY - 10, CONTRAST_COLOR);
        guiGraphics.drawCenteredString(this.font, Component.literal("Product"), 3 * part + 8, this.midY - 10, MAIN_COLOR);

        CompoundTag recipe = getSelectedTradeRecipe();
        if (recipe == null) {
            return;
        }

        renderTradeSlotItem(guiGraphics, recipe, TRADE_SLOT_FIRST_BUY);
        ItemStack secondCost = getTradeSlotItem(recipe, TRADE_SLOT_SECOND_BUY);
        if (!secondCost.isEmpty()) {
            renderTradeSlotItem(guiGraphics, recipe, TRADE_SLOT_SECOND_BUY);
        }
        renderTradeSlotItem(guiGraphics, recipe, TRADE_SLOT_SELL);
    }

    protected void renderLorePanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderSmallItem(guiGraphics, this.midX, 35);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("lore")), this.midX, 15, MAIN_COLOR);
        int spaces = loreLineSpaces();
        int size = this.loreValues.size();
        this.loreScroll = Mth.clamp(this.loreScroll, 0, Math.max(0, size - spaces));
        int y = 25;
        for (int i = this.loreScroll; i < this.loreScroll + spaces && i < size; i++) {
            String label = "Line " + (i + 1);
            y = 55 + 30 * (i - this.loreScroll);
            guiGraphics.drawString(this.font, label, 90 - this.font.width(label), y + 6, 0xFFFFFFFF);
        }

        int actionY = y + 30;
        for (InfinityEditorButton button : this.loreActionButtons) {
            button.setY(actionY);
        }
        if (this.copyLoreButton != null) {
            this.copyLoreButton.active = size > 0;
        }

        guiGraphics.hLine(this.width - 15, this.width - 5, 50, 0xFFAAAAAA);
        guiGraphics.hLine(this.width - 15, this.width - 5, this.height - 50, 0xFFAAAAAA);
        int scrollHeight = this.height - 103;
        float covered = size < spaces || size == 0 ? 1.0F : spaces / (float) size;
        int coveredHeight = (int) Math.max(1, scrollHeight * covered);
        float div = size - spaces;
        float perc = div <= 0.0F ? 0.0F : this.loreScroll / div;
        int scrollY = (int) (52 + (scrollHeight - coveredHeight) * perc);
        guiGraphics.fill(this.width - 14, scrollY, this.width - 5, scrollY + coveredHeight, 0xFF666666);
    }

    protected void renderLorePainterPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ensureLorePainterRows();
        if (this.lorePainterDragging) {
            paintLorePainterAt(mouseX, mouseY);
        }

        guiGraphics.drawCenteredString(this.font, Component.translatable(key("lorepainter")), this.midX, 15, MAIN_COLOR);

        int gridX = getLorePainterGridX();
        int gridY = getLorePainterGridY();
        guiGraphics.drawCenteredString(this.font, this.lorePainterWidth + "x" + this.lorePainterHeight,
                this.midX, gridY - 15, MAIN_COLOR);

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

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.8F, 0.8F, 1.0F);
        int tooltipX = isSidebarUi() ? Math.round((safeLeft() + 4) / 0.8F) : 0;
        guiGraphics.renderTooltip(this.font, this.previewStack, tooltipX, 25);
        guiGraphics.pose().popPose();
    }

    protected void renderPrettyNbt(GuiGraphics guiGraphics) {
        List<Component> lines = getPrettyNbtLines();
        if (!lines.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(0.8F, 0.8F, 1.0F);
            int nbtX = isSidebarUi() ? Math.round((safeLeft() + 4) / 0.8F) : 0;
            guiGraphics.renderComponentTooltip(this.font, lines, nbtX, this.height);
            guiGraphics.pose().popPose();
        }
    }

    protected void renderSmallItem(GuiGraphics guiGraphics, int centerX, int centerY) {
        if (this.previewStack.isEmpty()) {
            return;
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

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.midX, this.midY, 100.0F);
        guiGraphics.pose().mulPose(Axis.ZN.rotationDegrees(this.rotOff * 3.0F));
        guiGraphics.pose().scale(5.0F, 5.0F, 1.0F);
        guiGraphics.renderItem(this.previewStack, -8, -8);
        guiGraphics.pose().popPose();
    }

    protected void renderPanelTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.activePanel == Panel.ITEM) {
            if (isMouseIn(mouseX, mouseY, this.midX - 8, 32, ITEM_SIZE, ITEM_SIZE)) {
                guiGraphics.renderTooltip(this.font, this.previewStack, mouseX, mouseY);
            }
            if (this.itemIdBox != null && this.itemIdBox.getValue().length() > 9
                    && isMouseIn(mouseX, mouseY, this.itemIdBox.getX(), this.itemIdBox.getY(), this.itemIdBox.getWidth(), this.itemIdBox.getHeight())) {
                guiGraphics.renderTooltip(this.font, Component.literal(this.itemIdBox.getValue()), mouseX, mouseY);
            }
            if (isMouseIn(mouseX, mouseY, this.midX + 30, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT)) {
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
            if (isMouseIn(mouseX, mouseY, this.midX - 9, 27, 18, 18)) {
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
            if (isMouseIn(mouseX, mouseY, this.midX - 30, this.height - 35, OLD_BUTTON_WIDTH, OLD_BUTTON_HEIGHT)) {
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

    protected void drawRightLabel(GuiGraphics guiGraphics, Component text, int rightX, int y) {
        guiGraphics.drawString(this.font, text, rightX - this.font.width(text), y, MAIN_COLOR);
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
        return tryAddRingAttribute(mouseX, mouseY);
    }

    protected boolean handleColorClick(double mouseX, double mouseY) {
        if (!shouldShowDyeGrid() || this.blueSlider == null) {
            return false;
        }

        int gridX = this.blueSlider.getX();
        int gridY = this.blueSlider.getY() + this.blueSlider.getHeight() + 10;
        if (!isMouseIn(mouseX, mouseY, gridX, gridY, 160, 40)) {
            return false;
        }

        int column = ((int) mouseX - gridX) / 20;
        int row = ((int) mouseY - gridY) / 20;
        int index = column + row * 8;
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
        if (!isMouseIn(mouseX, mouseY, 10, getBannerPatternRowY(0) - 1, 150, BANNER_PATTERN_ROWS * 10 + 2)) {
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

    protected boolean handleSpawnEggClick(double mouseX, double mouseY) {
        if (!isMouseIn(mouseX, mouseY, 10, getSpawnEggEntityRowY(0) - 1, 170, SPAWN_EGG_ENTITY_ROWS * 10 + 2)) {
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
        if (isMouseIn(mouseX, mouseY, this.width - 15, 50, 11, this.height - 99)) {
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

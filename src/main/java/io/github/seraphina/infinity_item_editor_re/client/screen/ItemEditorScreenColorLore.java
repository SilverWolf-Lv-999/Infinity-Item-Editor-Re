package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.util.NbtCompat;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;

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
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.DyedItemColor;
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
abstract class ItemEditorScreenColorLore extends ItemEditorScreenEffects {
    protected ItemEditorScreenColorLore(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(stack, targetContainerSlot, parentTradeScreen, parentTradeIndex, parentTradeSlot);
    }

protected void applyColorFromHex(boolean updateStatus) {
        String text = this.colorHexBox == null ? this.colorHexValue : this.colorHexBox.getValue();
        String normalized = text == null ? "" : text.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }

        if (normalized.length() != 6) {
            if (updateStatus && !normalized.isBlank()) {
                this.status = Component.translatable(messageKey("editor_invalid_color"));
            }
            return;
        }

        try {
            setEditorColor(Integer.parseInt(normalized, 16));
            syncColorControlsFromStack();
            if (updateStatus) {
                this.status = Component.translatable(messageKey("editor_color_updated"), this.colorHexValue);
            }
        } catch (NumberFormatException exception) {
            this.status = Component.translatable(messageKey("editor_invalid_color"));
        }
    }

    protected void setColorComponent(int shift, int value) {
        int color = getEditorColor();
        color &= ~(0xFF << shift);
        color |= (Mth.clamp(value, 0, 255) << shift);
        setEditorColor(color);
        syncColorControlsFromStack();
    }

    protected void syncColorControlsFromStack() {
        int color = getEditorColor();
        this.colorHexValue = formatColorHex(color);
        this.syncingColorControls = true;
        if (this.colorHexBox != null) {
            this.colorHexBox.setValue(this.colorHexValue);
        }
        if (this.redSlider != null) {
            this.redSlider.setIntValue(getRed(color));
        }
        if (this.greenSlider != null) {
            this.greenSlider.setIntValue(getGreen(color));
        }
        if (this.blueSlider != null) {
            this.blueSlider.setIntValue(getBlue(color));
        }
        this.syncingColorControls = false;
    }

    protected int getEditorColor() {
        if (isPotionItem(this.previewStack)) {
            return PotionCompat.getColor(this.previewStack) & 0xFFFFFF;
        }
        if (isMapItem(this.previewStack)) {
            CompoundTag display = ItemStackNbt.getElement(this.previewStack, DISPLAY_TAG);
            return display == null ? 0 : NbtCompat.getInt(display, MAP_COLOR_TAG) & 0xFFFFFF;
        }
        if (this.previewStack.is(ItemTags.DYEABLE)) {
            return DyedItemColor.getOrDefault(this.previewStack, DyedItemColor.LEATHER_COLOR) & 0xFFFFFF;
        }
        return 0;
    }

    protected void setEditorColor(int color) {
        color &= 0xFFFFFF;
        if (isPotionItem(this.previewStack)) {
            PotionCompat.setCustomColor(this.previewStack, color);
        } else if (isMapItem(this.previewStack)) {
            CompoundTag tag = ItemStackNbt.getOrCreate(this.previewStack);
            CompoundTag display = NbtCompat.getCompound(tag, DISPLAY_TAG);
            display.putInt(MAP_COLOR_TAG, color);
            tag.put(DISPLAY_TAG, display);
        } else if (this.previewStack.is(ItemTags.DYEABLE)) {
            this.previewStack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        }
        this.rawNbtValue = getInitialNbt(this.previewStack);
    }

    protected void addDyeToColor(DyeColor dyeColor) {
        if (!this.previewStack.is(ItemTags.DYEABLE)) {
            return;
        }

        int[] totals = new int[3];
        int totalBrightness = 0;
        int colors = 0;
        if (this.previewStack.has(DataComponents.DYED_COLOR)) {
            int current = this.previewStack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(DyedItemColor.LEATHER_COLOR)).rgb();
            int red = getRed(current);
            int green = getGreen(current);
            int blue = getBlue(current);
            totalBrightness += Math.max(red, Math.max(green, blue));
            totals[0] += red;
            totals[1] += green;
            totals[2] += blue;
            colors++;
        }

        int dyeRgb = dyeColor.getTextureDiffuseColor();
        int red = getRed(dyeRgb);
        int green = getGreen(dyeRgb);
        int blue = getBlue(dyeRgb);
        totalBrightness += Math.max(red, Math.max(green, blue));
        totals[0] += red;
        totals[1] += green;
        totals[2] += blue;
        colors++;

        int mixedRed = totals[0] / colors;
        int mixedGreen = totals[1] / colors;
        int mixedBlue = totals[2] / colors;
        float averageBrightness = (float) totalBrightness / (float) colors;
        float maxMixed = Math.max(mixedRed, Math.max(mixedGreen, mixedBlue));
        if (maxMixed > 0.0F) {
            mixedRed = (int) (mixedRed * averageBrightness / maxMixed);
            mixedGreen = (int) (mixedGreen * averageBrightness / maxMixed);
            mixedBlue = (int) (mixedBlue * averageBrightness / maxMixed);
        }
        setEditorColor((mixedRed << 16) | (mixedGreen << 8) | mixedBlue);
    }

    protected void renderDyeGrid(GuiGraphics guiGraphics) {
        if (!shouldShowDyeGrid() || this.blueSlider == null) {
            return;
        }

        int columns = dyeGridColumns();
        int cellSize = dyeGridCellSize();
        int gridWidth = columns * cellSize;
        int gridHeight = dyeGridRows(columns) * cellSize;
        int gridX = dyeGridX(columns, cellSize);
        int gridY = this.blueSlider.getY() + this.blueSlider.getHeight() + 10;
        if (isSidebarUi()) {
            ModernUi.fillPanel(guiGraphics, gridX - 5, gridY - 5, gridX + gridWidth + 5, gridY + gridHeight + 5, 7,
                    ModernUi.SURFACE, ModernUi.BORDER);
        }
        int index = 0;
        for (DyeColor dyeColor : DyeColor.values()) {
            int x = gridX + cellSize * (index % columns);
            int y = gridY + cellSize * (index / columns);
            if (isSidebarUi()) {
                ModernUi.fillRounded(guiGraphics, x + 1, y + 1, x + cellSize - 1, y + cellSize - 1, 4, argb(210, dyeColor.getTextColor()));
                guiGraphics.fill(x + 3, y + 2, x + cellSize - 3, y + 3, ModernUi.alpha(0xFFFFFF, 45));
            } else {
                guiGraphics.fill(x, y, x + cellSize, y + cellSize, argb(159, dyeColor.getTextColor()));
            }
            DyeItem dyeItem = DyeItem.byColor(dyeColor);
            if (dyeItem != null) {
                guiGraphics.renderItem(new ItemStack(dyeItem), x + (cellSize - ITEM_SIZE) / 2, y + (cellSize - ITEM_SIZE) / 2);
            }
            index++;
        }
    }

    protected int dyeGridColumns() {
        return isSidebarUi() && this.blueSlider != null && this.blueSlider.getWidth() < 160 ? 4 : 8;
    }

    protected int dyeGridCellSize() {
        return 20;
    }

    protected int dyeGridRows(int columns) {
        return (DyeColor.values().length + columns - 1) / columns;
    }

    protected int dyeGridX(int columns, int cellSize) {
        if (this.blueSlider == null) {
            return 0;
        }

        int gridWidth = columns * cellSize;
        return this.blueSlider.getX() + (this.blueSlider.getWidth() - gridWidth) / 2;
    }

    protected boolean shouldShowDyeGrid() {
        return this.previewStack.is(ItemTags.DYEABLE);
    }

    protected void ensureLorePainterRows() {
        while (this.lorePainterRows.size() < this.lorePainterHeight) {
            List<LorePixel> row = new ArrayList<>();
            for (int i = 0; i < this.lorePainterWidth; i++) {
                row.add(this.currentLorePixel.copy());
            }
            this.lorePainterRows.add(row);
        }
        while (this.lorePainterRows.size() > this.lorePainterHeight) {
            this.lorePainterRows.remove(this.lorePainterRows.size() - 1);
        }
        for (List<LorePixel> row : this.lorePainterRows) {
            while (row.size() < this.lorePainterWidth) {
                row.add(this.currentLorePixel.copy());
            }
            while (row.size() > this.lorePainterWidth) {
                row.remove(row.size() - 1);
            }
        }
    }

    protected void addLorePainterRow() {
        List<LorePixel> row = new ArrayList<>();
        for (int i = 0; i < this.lorePainterWidth; i++) {
            row.add(this.currentLorePixel.copy());
        }
        this.lorePainterRows.add(row);
        this.lorePainterHeight++;
    }

    protected void removeLorePainterRow() {
        if (this.lorePainterHeight <= 1) {
            return;
        }
        this.lorePainterRows.remove(this.lorePainterRows.size() - 1);
        this.lorePainterHeight--;
    }

    protected void addLorePainterColumn() {
        for (List<LorePixel> row : this.lorePainterRows) {
            row.add(this.currentLorePixel.copy());
        }
        this.lorePainterWidth++;
    }

    protected void removeLorePainterColumn() {
        if (this.lorePainterWidth <= 1) {
            return;
        }
        for (List<LorePixel> row : this.lorePainterRows) {
            row.remove(row.size() - 1);
        }
        this.lorePainterWidth--;
    }

    protected void insertLorePainterRows() {
        ensureLorePainterRows();
        for (List<LorePixel> row : this.lorePainterRows) {
            this.loreValues.add(buildLorePainterRow(row));
        }
        applyLoreToStack();
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.status = Component.translatable(messageKey("editor_lore_painted"));
    }

    protected void cycleGuiScale() {
        if (this.minecraft == null) {
            return;
        }
        int current = this.minecraft.options.guiScale().get();
        int next = current >= 4 ? 0 : current + 1;
        this.minecraft.options.guiScale().set(next);
        this.minecraft.resizeDisplay();
        rebuildWidgets();
    }

    protected void paintLorePainterAt(double mouseX, double mouseY) {
        ensureLorePainterRows();
        int gridX = getLorePainterGridX();
        int gridY = getLorePainterGridY();
        if (isMouseIn(mouseX, mouseY, gridX, gridY, getLorePainterSizeX(), getLorePainterSizeY())) {
            int x = Mth.clamp(((int) mouseX - gridX) / 9, 0, this.lorePainterWidth - 1);
            int y = Mth.clamp(((int) mouseY - gridY) / 9, 0, this.lorePainterHeight - 1);
            this.lorePainterRows.get(y).set(x, this.currentLorePixel.copy());
            return;
        }

        if (isMouseIn(mouseX, mouseY, 0, 0, LoreSymbol.values().length * 9, 9)) {
            int index = Mth.clamp((int) mouseX / 9, 0, LoreSymbol.values().length - 1);
            this.currentLorePixel.symbol = LoreSymbol.values()[index];
            return;
        }

        int colorX = getLorePainterColorX();
        if (isMouseIn(mouseX, mouseY, colorX, 0, DyeColor.values().length * 9, 9)) {
            int index = Mth.clamp(((int) mouseX - colorX) / 9, 0, DyeColor.values().length - 1);
            this.currentLorePixel.color = DyeColor.values()[index];
        }
    }

    protected String buildLorePainterRow(List<LorePixel> row) {
        StringBuilder builder = new StringBuilder();
        for (LorePixel pixel : row) {
            builder.append(pixel);
        }
        return builder.toString();
    }

    protected String buildLorePainterSymbols() {
        StringBuilder builder = new StringBuilder();
        for (LoreSymbol symbol : LoreSymbol.values()) {
            if (symbol == LoreSymbol.FULL_SPACE) {
                builder.append(ChatFormatting.ITALIC).append(ChatFormatting.BOLD).append("E");
            } else {
                builder.append(new LorePixel(this.currentLorePixel.color, symbol));
            }
        }
        return builder.toString();
    }

    protected String buildLorePainterColors() {
        StringBuilder builder = new StringBuilder();
        for (DyeColor color : DyeColor.values()) {
            builder.append(new LorePixel(color, this.currentLorePixel.symbol));
        }
        return builder.toString();
    }

    protected int getLorePainterColorX() {
        return this.width - this.font.width(buildLorePainterColors());
    }

    protected int getLorePainterGridX() {
        return this.midX - getLorePainterSizeX() / 2;
    }

    protected int getLorePainterGridY() {
        return this.midY - getLorePainterSizeY() / 2;
    }

    protected int getLorePainterSizeX() {
        return 9 * this.lorePainterWidth;
    }

    protected int getLorePainterSizeY() {
        return 9 * this.lorePainterHeight;
    }
}

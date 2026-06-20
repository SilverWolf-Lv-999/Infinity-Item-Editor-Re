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
abstract class ItemEditorScreenActions extends ItemEditorScreenColorLore {
    protected ItemEditorScreenActions(ItemStack stack, int targetContainerSlot, ItemEditorScreen parentTradeScreen, int parentTradeIndex, int parentTradeSlot) {
        super(stack, targetContainerSlot, parentTradeScreen, parentTradeIndex, parentTradeSlot);
    }

    protected void switchPanel(Panel panel) {
        if (this.activePanel == panel) {
            return;
        }
        captureFieldValues();
        if (this.activePanel == Panel.ITEM) {
            applyMainFieldsToStack(false);
        }
        this.activePanel = panel;
        this.status = Component.empty();
        rebuildWidgets();
    }

    protected void openContainerItemEditor() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        if (!applyMainFieldsToStack(true) || !isContainerEditableItem(this.previewStack)) {
            return;
        }
        this.status = Component.empty();
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.minecraft.setScreen(ContainerItemScreen.create((ItemEditorScreen) this, this.minecraft.player, this.previewStack));
    }

    protected void openBookItemEditor() {
        if (this.minecraft == null) {
            return;
        }
        if (!applyMainFieldsToStack(true) || !isBookEditableItem(this.previewStack)) {
            return;
        }
        this.status = Component.empty();
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.minecraft.setScreen(new BookItemScreen((ItemEditorScreen) this, this.previewStack));
    }

    protected void openItemPicker() {
        if (this.minecraft == null) {
            return;
        }
        if (this.activePanel == Panel.ITEM) {
            applyMainFieldsToStack(false);
        }
        this.minecraft.setScreen(new ItemPickScreen((ItemEditorScreen) this, this::replacePickedStack, () -> this.previewStack));
    }

    private void replacePickedStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        this.previewStack = stack.copy();
        readMainFieldsFromStack(this.previewStack);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.nbtFeedback = "";
        syncItemPanelFieldsFromStack();
    }

    private void syncItemPanelFieldsFromStack() {
        if (this.itemIdBox != null) {
            this.itemIdBox.setValue(this.itemIdValue);
        }
        if (this.countBox != null) {
            this.countBox.setValue(this.countValue);
        }
        if (this.damageBox != null) {
            this.damageBox.setValue(this.damageValue);
        }
        if (this.nameBox != null) {
            this.nameBox.setValue(this.nameValue);
        }
    }

    void refreshAfterContainerEdit() {
        readMainFieldsFromStack(this.previewStack);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.containerSlotNbtValue = getContainerSelectedSlotNbt();
        this.nbtFeedback = "";
    }

    void refreshAfterBookEdit() {
        readMainFieldsFromStack(this.previewStack);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.nbtFeedback = "";
    }

    protected void goBack() {
        if (this.activePanel == Panel.ITEM) {
            if (isTradeSlotEditor()) {
                applyTradeSlotEditorAndReturn();
                return;
            }
            onClose();
            return;
        }

        if (this.activePanel == Panel.TRADE) {
            this.activePanel = Panel.TRADES;
            ensureVillagerTradeOffers();
            rebuildWidgets();
            return;
        }

        this.activePanel = Panel.ITEM;
        readMainFieldsFromStack(this.previewStack);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        rebuildWidgets();
    }

    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    protected void applyToSelectedSlot() {
        if (!applyMainFieldsToStack(true) || this.previewStack.isEmpty() || this.minecraft == null || this.minecraft.player == null || this.minecraft.gameMode == null) {
            return;
        }

        if (!this.minecraft.player.getAbilities().instabuild) {
            this.status = Component.translatable(messageKey("editor_requires_creative"));
            return;
        }

        int selected = this.minecraft.player.getInventory().selected;
        int containerSlot = this.targetContainerSlot >= 0
                ? this.targetContainerSlot
                : PlayerInventorySlots.HOTBAR_CONTAINER_SLOT_START + selected;
        ItemStack inventoryStack = this.previewStack.copy();
        if (!PlayerInventorySlots.setStack(this.minecraft.player, containerSlot, inventoryStack)) {
            this.status = Component.translatable(messageKey("editor_invalid_target_slot"));
            return;
        }

        this.minecraft.gameMode.handleCreativeModeItemAdd(inventoryStack.copy(), containerSlot);
        this.status = Component.translatable(messageKey("editor_applied"), inventoryStack.getHoverName());
    }

    protected void dropEditedStack() {
        if (Screen.hasShiftDown()) {
            copyGiveCommand();
            return;
        }

        if (!applyMainFieldsToStack(true) || this.previewStack.isEmpty() || this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }

        if (this.minecraft.player == null || !this.minecraft.player.getAbilities().instabuild) {
            this.status = Component.translatable(messageKey("editor_requires_creative"));
            return;
        }

        ItemStack dropped = this.previewStack.copy();
        this.minecraft.gameMode.handleCreativeModeItemDrop(dropped);
        this.status = Component.translatable(messageKey("editor_dropped"), dropped.getHoverName());
    }

    protected void copyGiveCommand() {
        if (!applyMainFieldsToStack(true) || this.previewStack.isEmpty() || this.minecraft == null) {
            return;
        }
        this.minecraft.keyboardHandler.setClipboard(GiveHelper.getStringFromItemStack(this.previewStack));
        this.status = Component.translatable(messageKey("editor_copied"));
    }

    protected void resetStack() {
        if (this.activePanel == Panel.TRADES) {
            clearVillagerTrades();
            return;
        }

        if (this.activePanel == Panel.TRADE) {
            return;
        }

        if (this.activePanel == Panel.NBT) {
            this.previewStack.setTag(new CompoundTag());
        } else {
            this.previewStack.setTag(null);
        }
        readMainFieldsFromStack(this.previewStack);
        this.rawNbtValue = getInitialNbt(this.previewStack);
        this.nbtFeedback = "";
        rebuildWidgets();
    }

    protected void saveRealm() {
        if (!applyMainFieldsToStack(true) || this.previewStack.isEmpty() || this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        RealmController realmController = ModSource.getOrCreateRealmController(this.minecraft.gameDirectory);
        if (realmController != null) {
            if (realmController.addItemStack(this.minecraft.player, this.previewStack.copy())) {
                CreativeTabRefresher.refreshRealm(this.minecraft);
                this.status = Component.translatable(messageKey("editor_saved"), this.previewStack.getHoverName());
            }
        }
    }

    protected boolean applyMainFieldsToStack(boolean updateStatus) {
        try {
            captureFieldValues();
            tryApplyItemId(true);
            tryApplyCount(true);
            tryApplyDamage(true);
            applyNameToStack();
            applyLoreToStack();
            if (this.activePanel == Panel.SIGN) {
                applySignToStack();
            }
            if (this.activePanel == Panel.BOOK) {
                applyBookMetadataToStack();
            }
            if (this.activePanel == Panel.HEAD) {
                applyHeadToStack();
            }
            if (this.activePanel == Panel.BANNER) {
                readBannerFieldsFromStack(this.previewStack);
            }
            this.rawNbtValue = getInitialNbt(this.previewStack);
            return true;
        } catch (IllegalArgumentException exception) {
            if (updateStatus) {
                this.status = Component.literal(exception.getMessage());
            }
            return false;
        }
    }

    protected boolean tryApplyItemId(boolean throwOnError) {
        String idText = normalizeItemId(this.itemIdBox == null ? this.itemIdValue : this.itemIdBox.getValue());
        ResourceLocation id = ResourceLocation.tryParse(idText);
        Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == Items.AIR && !"minecraft:air".equals(idText)) {
            if (throwOnError) {
                throw new IllegalArgumentException(Component.translatable(messageKey("editor_invalid_item"), idText).getString());
            }
            return false;
        }

        this.itemIdValue = stripMinecraftNamespace(id);
        if (this.previewStack.is(item)) {
            return false;
        }

        boolean syncDefaultName = isNameFollowingDefault(this.previewStack);
        CompoundTag tag = this.previewStack.getTag() == null ? null : this.previewStack.getTag().copy();
        int count = this.previewStack.getCount() <= 0 ? 1 : this.previewStack.getCount();
        this.previewStack = new ItemStack(item, count);
        this.previewStack.setTag(tag);
        if (syncDefaultName) {
            this.nameValue = getDefaultHoverName(this.previewStack);
            if (this.nameBox != null) {
                this.nameBox.setValue(this.nameValue);
            }
        }
        this.damageValue = Integer.toString(Math.min(getDamageMaxForField(this.previewStack), parsePositiveOrZero(this.damageValue)));
        tryApplyDamage(false);
        this.attributeSlot = getDefaultAttributeSlot(this.previewStack);
        this.colorHexValue = formatColorHex(getEditorColor());
        return true;
    }

    protected void tryApplyCount(boolean throwOnError) {
        String value = this.countBox == null ? this.countValue : this.countBox.getValue();
        if (value == null || value.isBlank()) {
            if (throwOnError) {
                throw new IllegalArgumentException(Component.translatable(messageKey("editor_invalid_count"), MAX_COUNT).getString());
            }
            return;
        }

        int count = Integer.parseInt(value);
        if (count < 1 || count > MAX_COUNT) {
            if (throwOnError) {
                throw new IllegalArgumentException(Component.translatable(messageKey("editor_invalid_count"), MAX_COUNT).getString());
            }
            return;
        }

        this.countValue = Integer.toString(count);
        this.previewStack.setCount(count);
    }

    protected void tryApplyDamage(boolean throwOnError) {
        String value = this.damageBox == null ? this.damageValue : this.damageBox.getValue();
        if (value == null || value.isBlank()) {
            if (throwOnError) {
                throw new IllegalArgumentException(Component.translatable(messageKey("editor_invalid_damage"), getDamageMaxForField(this.previewStack)).getString());
            }
            return;
        }

        int damage = Integer.parseInt(value);
        int maxDamage = getDamageMaxForField(this.previewStack);
        if (damage < 0 || damage > maxDamage) {
            if (throwOnError) {
                throw new IllegalArgumentException(Component.translatable(messageKey("editor_invalid_damage"), maxDamage).getString());
            }
            return;
        }

        this.damageValue = Integer.toString(damage);
        this.previewStack.setDamageValue(damage);
    }

    protected void applyNameToStack() {
        String value = this.nameBox == null ? this.nameValue : this.nameBox.getValue();
        this.nameValue = value;
        if (value == null || value.isBlank()) {
            this.previewStack.resetHoverName();
            cleanupEmptyDisplayTag();
            return;
        }

        ItemStack withoutName = this.previewStack.copy();
        withoutName.resetHoverName();
        if (!this.previewStack.hasCustomHoverName() && value.equals(withoutName.getHoverName().getString())) {
            return;
        }

        this.previewStack.setHoverName(Component.literal(value));
    }

    protected void clearCustomName() {
        this.previewStack.resetHoverName();
        this.nameValue = this.previewStack.getHoverName().getString();
        if (this.nameBox != null) {
            this.nameBox.setValue(this.nameValue);
        }
        cleanupEmptyDisplayTag();
    }

    protected void applyLoreToStack() {
        CompoundTag tag = this.previewStack.getOrCreateTag();
        CompoundTag display = tag.getCompound(DISPLAY_TAG);
        if (this.loreValues.isEmpty()) {
            display.remove(LORE_TAG);
        } else {
            ListTag lore = new ListTag();
            for (String line : this.loreValues) {
                lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(line))));
            }
            display.put(LORE_TAG, lore);
        }

        if (display.isEmpty()) {
            tag.remove(DISPLAY_TAG);
        } else {
            tag.put(DISPLAY_TAG, display);
        }
        cleanupEmptyTag();
    }

    protected void setLoreLine(int line, String value) {
        while (this.loreValues.size() <= line) {
            this.loreValues.add("");
        }
        this.loreValues.set(line, value);
    }

    protected void removeLoreLine(int line) {
        if (line >= 0 && line < this.loreValues.size()) {
            this.loreValues.remove(line);
            this.loreScroll = Mth.clamp(this.loreScroll, 0, Math.max(0, this.loreValues.size() - loreLineSpaces()));
            applyLoreToStack();
        }
    }

    protected void moveLoreLine(int line, int direction) {
        int other = line + direction;
        if (line < 0 || line >= this.loreValues.size() || other < 0 || other >= this.loreValues.size()) {
            return;
        }
        String value = this.loreValues.get(line);
        this.loreValues.set(line, this.loreValues.get(other));
        this.loreValues.set(other, value);
        applyLoreToStack();
        rebuildWidgets();
    }

    protected void copyLoreOnly() {
        if (this.minecraft != null) {
            this.minecraft.keyboardHandler.setClipboard(String.join("\n", this.loreValues));
        }
    }

    protected void copyFullTooltip() {
        if (this.minecraft != null && this.minecraft.player != null) {
            List<Component> tooltip = this.previewStack.getTooltipLines(this.minecraft.player, this.minecraft.options.advancedItemTooltips ? net.minecraft.world.item.TooltipFlag.Default.ADVANCED : net.minecraft.world.item.TooltipFlag.Default.NORMAL);
            List<String> lines = new ArrayList<>();
            for (Component component : tooltip) {
                lines.add(component.getString());
            }
            this.minecraft.keyboardHandler.setClipboard(String.join("\n", lines));
        }
    }

    protected void pasteLore() {
        if (this.minecraft == null) {
            return;
        }
        String clipboard = this.minecraft.keyboardHandler.getClipboard();
        if (clipboard == null || clipboard.isBlank()) {
            return;
        }
        for (String line : clipboard.split("\\r?\\n")) {
            if (!line.startsWith(String.valueOf(ChatFormatting.PREFIX_CODE)) && line.length() > 1) {
                line = ChatFormatting.RESET.toString() + ChatFormatting.GRAY + line;
            }
            this.loreValues.add(line);
        }
        applyLoreToStack();
        rebuildWidgets();
    }
}

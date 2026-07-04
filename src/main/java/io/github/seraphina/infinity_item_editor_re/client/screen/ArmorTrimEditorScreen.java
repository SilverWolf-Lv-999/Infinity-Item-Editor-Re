package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.util.CompatRegistries;
import io.github.seraphina.infinity_item_editor_re.util.NbtCompat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class ArmorTrimEditorScreen extends CompatScreen {
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 6;
    private static final int STATUS_GOOD = 0xFF32CC64;
    private static final int STATUS_BAD = 0xFFF44262;
    private static final int STATUS_NEUTRAL = 0xFFFFD966;
    private static final int PREVIEW_ARMOR_STAND = 0;
    private static final int PREVIEW_PLAYER = 1;
    private static final int PREVIEW_ZOMBIE = 2;
    private static final String ARMOR_TRIMS_CUSTOM_DATA_TAG = "InfinityItemEditorArmorTrims";
    private static final String ARMOR_TRIM_MATERIAL_TAG = "material";
    private static final String ARMOR_TRIM_PATTERN_TAG = "pattern";

    private final ItemEditorScreen lastScreen;
    private ItemStack armorStack;
    private Component status = Component.empty();
    private int statusColor = STATUS_NEUTRAL;
    private int selectedMaterialIndex;
    private int selectedPatternIndex;
    private int previewEntity = PREVIEW_ARMOR_STAND;
    private ArmorStand armorStandPreview;
    private Zombie zombiePreview;

    ArmorTrimEditorScreen(ItemEditorScreen lastScreen, ItemStack stack) {
        super(Component.translatable(key("armortrim")));
        this.lastScreen = lastScreen;
        this.armorStack = stack.copy();
        syncSelectionFromStack();
    }

    @Override
    protected void init() {
        addControls();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            returnToLastScreen();
            return true;
        }
        if (isInventoryKey(keyCode, scanCode)) {
            returnToLastScreen();
            return true;
        }
        if (CompatScreen.hasControlDown() && keyCode == 83) {
            applySelectedTrim();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        EditorBackgrounds.render(guiGraphics, this.width, this.height);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, InfinityEditorButton.MAIN_COLOR);
        guiGraphics.renderItem(this.armorStack, 18, 14);
        guiGraphics.renderItemDecorations(this.font, this.armorStack, 18, 14);

        int centerX = this.width / 2;
        int controlsTop = controlsTop();
        int previewTop = 38;
        int statusY = controlsTop - 14;
        int previewBottom = Mth.clamp(statusY - 52, previewTop + 64, Math.max(previewTop + 64, controlsTop - 66));
        int halfWidth = Mth.clamp(this.width / 5, 56, 86);
        renderArmorTrimEntityPreview(guiGraphics, centerX - halfWidth, previewTop, centerX + halfWidth, previewBottom);

        ArmorTrimMaterialEntry material = getSelectedMaterialEntry();
        ArmorTrimPatternEntry pattern = getSelectedPatternEntry();
        Component materialName = getMaterialName(material);
        Component patternName = getPatternName(pattern, material);
        int infoY = previewBottom + 8;
        drawCenteredClipped(guiGraphics, Component.translatable(key("armortrim.selected"), patternName, materialName),
                centerX, infoY, this.width - 24, InfinityEditorButton.MAIN_COLOR);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("armortrim.count"), getTrimCount()),
                centerX, infoY + 12, InfinityEditorButton.ALT_COLOR);
        guiGraphics.drawCenteredString(this.font, Component.translatable(key("armortrim.preview"), getPreviewEntityName()),
                centerX, infoY + 24, InfinityEditorButton.ALT_COLOR);

        if (material == null || pattern == null) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(key(material == null
                    ? "armortrim.no_materials"
                    : "armortrim.no_patterns")), centerX, infoY + 38, STATUS_BAD);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (!this.status.getString().isEmpty()) {
            drawCenteredClipped(guiGraphics, this.status, centerX, statusY, this.width - 20, this.statusColor);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void onClose() {
        returnToLastScreen();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void addControls() {
        List<ArmorTrimMaterialEntry> materials = getMaterials();
        List<ArmorTrimPatternEntry> patterns = getPatterns();
        clampSelection(materials, patterns);

        int buttonWidth = buttonWidth();
        int totalWidth = buttonWidth * 4 + BUTTON_GAP * 3;
        int x = (this.width - totalWidth) / 2;
        int y = controlsTop();

        addRenderableWidget(new InfinityEditorButton(x, y, buttonWidth, BUTTON_HEIGHT,
                Component.translatable(key("back")), button -> returnToLastScreen()));

        InfinityEditorButton material = addRenderableWidget(new InfinityEditorButton(x + buttonWidth + BUTTON_GAP, y,
                buttonWidth, BUTTON_HEIGHT, Component.translatable(key("armortrim.material"), getSelectedMaterialName()),
                button -> cycleMaterial(CompatScreen.hasShiftDown() ? -1 : 1)));
        material.active = !materials.isEmpty();

        InfinityEditorButton pattern = addRenderableWidget(new InfinityEditorButton(x + (buttonWidth + BUTTON_GAP) * 2, y,
                buttonWidth, BUTTON_HEIGHT, Component.translatable(key("armortrim.pattern"), getSelectedPatternName()),
                button -> cyclePattern(CompatScreen.hasShiftDown() ? -1 : 1)));
        pattern.active = !patterns.isEmpty();

        addRenderableWidget(new InfinityEditorButton(x + (buttonWidth + BUTTON_GAP) * 3, y,
                buttonWidth, BUTTON_HEIGHT, Component.translatable(key("armortrim.preview"), getPreviewEntityName()),
                button -> cyclePreviewEntity()));

        y += BUTTON_HEIGHT + BUTTON_GAP;
        InfinityEditorButton apply = addRenderableWidget(new InfinityEditorButton(x, y, buttonWidth, BUTTON_HEIGHT,
                Component.translatable(key("armortrim.apply")), button -> applySelectedTrim()));
        apply.active = !materials.isEmpty() && !patterns.isEmpty();

        InfinityEditorButton add = addRenderableWidget(new InfinityEditorButton(x + buttonWidth + BUTTON_GAP, y,
                buttonWidth, BUTTON_HEIGHT, Component.translatable(key("armortrim.add")), button -> addSelectedTrim()));
        add.active = !materials.isEmpty() && !patterns.isEmpty();

        InfinityEditorButton remove = addRenderableWidget(new InfinityEditorButton(x + (buttonWidth + BUTTON_GAP) * 2, y,
                buttonWidth, BUTTON_HEIGHT, Component.translatable(key("armortrim.remove_last")), button -> removeLastTrim()));
        remove.active = getTrimCount() > 0;

        InfinityEditorButton clear = addRenderableWidget(new InfinityEditorButton(x + (buttonWidth + BUTTON_GAP) * 3, y,
                buttonWidth, BUTTON_HEIGHT, Component.translatable(key("armortrim.clear")), button -> clearTrims()));
        clear.active = getTrimCount() > 0 || this.armorStack.has(DataComponents.TRIM);
    }

    private int controlsTop() {
        return Math.max(124, this.height - BUTTON_HEIGHT * 2 - BUTTON_GAP - 10);
    }

    private int buttonWidth() {
        int available = Math.max(1, this.width - 36);
        return Mth.clamp((available - BUTTON_GAP * 3) / 4, 44, 132);
    }

    private boolean isInventoryKey(int keyCode, int scanCode) {
        return this.minecraft != null
                && this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(new net.minecraft.client.input.KeyEvent(keyCode, scanCode, 0)));
    }

    private void returnToLastScreen() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen);
        }
    }

    private void applySelectedTrim() {
        if (!isArmorTrimApplicable(this.armorStack)) {
            return;
        }
        if (!setSelectedTrimComponent()) {
            this.status = Component.translatable(key("armortrim.no_patterns"));
            this.statusColor = STATUS_BAD;
            return;
        }

        commitStack(Component.translatable(messageKey("editor_armor_trim_updated"),
                getSelectedPatternName(), getSelectedMaterialName()), STATUS_GOOD);
    }

    private void addSelectedTrim() {
        if (!isArmorTrimApplicable(this.armorStack)) {
            return;
        }

        ArmorTrimMaterialEntry material = getSelectedMaterialEntry();
        ArmorTrimPatternEntry pattern = getSelectedPatternEntry();
        if (material == null || pattern == null) {
            this.status = Component.translatable(key(material == null ? "armortrim.no_materials" : "armortrim.no_patterns"));
            this.statusColor = STATUS_BAD;
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, this.armorStack, tag -> {
            ListTag trims = NbtCompat.contains(tag, ARMOR_TRIMS_CUSTOM_DATA_TAG, Tag.TAG_LIST)
                    ? NbtCompat.getList(tag, ARMOR_TRIMS_CUSTOM_DATA_TAG, Tag.TAG_COMPOUND).copy()
                    : new ListTag();
            CompoundTag entry = new CompoundTag();
            entry.putString(ARMOR_TRIM_MATERIAL_TAG, material.id().toString());
            entry.putString(ARMOR_TRIM_PATTERN_TAG, pattern.id().toString());
            trims.add(entry);
            tag.put(ARMOR_TRIMS_CUSTOM_DATA_TAG, trims);
        });
        setSelectedTrimComponent();
        commitStack(Component.translatable(messageKey("editor_armor_trim_added"),
                getPatternName(pattern, material), getMaterialName(material)), STATUS_GOOD);
    }

    private void removeLastTrim() {
        if (getTrimCount() <= 0) {
            return;
        }

        final ArmorTrimEntry[] activeEntry = new ArmorTrimEntry[1];
        CustomData.update(DataComponents.CUSTOM_DATA, this.armorStack, tag -> {
            if (!NbtCompat.contains(tag, ARMOR_TRIMS_CUSTOM_DATA_TAG, Tag.TAG_LIST)) {
                return;
            }

            ListTag trims = NbtCompat.getList(tag, ARMOR_TRIMS_CUSTOM_DATA_TAG, Tag.TAG_COMPOUND).copy();
            if (trims.isEmpty()) {
                tag.remove(ARMOR_TRIMS_CUSTOM_DATA_TAG);
                return;
            }

            trims.remove(trims.size() - 1);
            if (trims.isEmpty()) {
                tag.remove(ARMOR_TRIMS_CUSTOM_DATA_TAG);
                return;
            }

            tag.put(ARMOR_TRIMS_CUSTOM_DATA_TAG, trims);
            CompoundTag last = NbtCompat.getCompound(trims, trims.size() - 1);
            Identifier materialId = Identifier.tryParse(NbtCompat.getString(last, ARMOR_TRIM_MATERIAL_TAG));
            Identifier patternId = Identifier.tryParse(NbtCompat.getString(last, ARMOR_TRIM_PATTERN_TAG));
            if (materialId != null && patternId != null) {
                activeEntry[0] = new ArmorTrimEntry(materialId, patternId);
            }
        });

        if (activeEntry[0] == null) {
            this.armorStack.remove(DataComponents.TRIM);
        } else {
            applyTrimEntry(activeEntry[0]);
        }
        syncSelectionFromStack();
        commitStack(Component.translatable(messageKey("editor_armor_trim_removed")), STATUS_GOOD);
    }

    private void clearTrims() {
        CustomData.update(DataComponents.CUSTOM_DATA, this.armorStack, tag -> tag.remove(ARMOR_TRIMS_CUSTOM_DATA_TAG));
        this.armorStack.remove(DataComponents.TRIM);
        syncSelectionFromStack();
        commitStack(Component.translatable(messageKey("editor_armor_trim_cleared")), STATUS_GOOD);
    }

    private void cycleMaterial(int direction) {
        List<ArmorTrimMaterialEntry> materials = getMaterials();
        if (materials.isEmpty()) {
            return;
        }

        this.selectedMaterialIndex = Mth.positiveModulo(this.selectedMaterialIndex + direction, materials.size());
        rebuildWidgets();
    }

    private void cyclePattern(int direction) {
        List<ArmorTrimPatternEntry> patterns = getPatterns();
        if (patterns.isEmpty()) {
            return;
        }

        this.selectedPatternIndex = Mth.positiveModulo(this.selectedPatternIndex + direction, patterns.size());
        rebuildWidgets();
    }

    private void cyclePreviewEntity() {
        this.previewEntity = Mth.positiveModulo(this.previewEntity + 1, 3);
        this.status = Component.translatable(messageKey("editor_armor_trim_preview_updated"), getPreviewEntityName());
        this.statusColor = STATUS_NEUTRAL;
        rebuildWidgets();
    }

    private void commitStack(Component status, int statusColor) {
        this.lastScreen.applyArmorTrimEditedStack(this.armorStack);
        this.armorStack = this.lastScreen.previewStack.copy();
        this.status = status;
        this.statusColor = statusColor;
        syncSelectionFromStack();
        rebuildWidgets();
    }

    private void syncSelectionFromStack() {
        List<ArmorTrimMaterialEntry> materials = getMaterials();
        List<ArmorTrimPatternEntry> patterns = getPatterns();
        if (materials.isEmpty() || patterns.isEmpty()) {
            this.selectedMaterialIndex = 0;
            this.selectedPatternIndex = 0;
            return;
        }

        Identifier materialId = null;
        Identifier patternId = null;
        ArmorTrim trim = this.armorStack.get(DataComponents.TRIM);
        if (trim != null) {
            materialId = CompatRegistries.TRIM_MATERIALS.getKey(trim.material().value());
            patternId = CompatRegistries.TRIM_PATTERNS.getKey(trim.pattern().value());
        }
        if (materialId == null || patternId == null) {
            ArmorTrimEntry entry = getLastTrimEntry();
            if (entry != null) {
                materialId = entry.materialId();
                patternId = entry.patternId();
            }
        }

        this.selectedMaterialIndex = findMaterialIndex(materials, materialId);
        this.selectedPatternIndex = findPatternIndex(patterns, patternId);
        clampSelection(materials, patterns);
    }

    private List<ArmorTrimMaterialEntry> getMaterials() {
        List<ArmorTrimMaterialEntry> entries = new ArrayList<>();
        for (var holder : CompatRegistries.TRIM_MATERIALS.getHolders()) {
            Identifier id = CompatRegistries.TRIM_MATERIALS.getKey(holder.value());
            if (id != null) {
                entries.add(new ArmorTrimMaterialEntry(id, holder));
            }
        }
        entries.sort(Comparator.comparing(entry -> entry.id().toString()));
        return entries;
    }

    private List<ArmorTrimPatternEntry> getPatterns() {
        List<ArmorTrimPatternEntry> entries = new ArrayList<>();
        for (var holder : CompatRegistries.TRIM_PATTERNS.getHolders()) {
            Identifier id = CompatRegistries.TRIM_PATTERNS.getKey(holder.value());
            if (id != null) {
                entries.add(new ArmorTrimPatternEntry(id, holder));
            }
        }
        entries.sort(Comparator.comparing(entry -> entry.id().toString()));
        return entries;
    }

    private ArmorTrimMaterialEntry getSelectedMaterialEntry() {
        List<ArmorTrimMaterialEntry> materials = getMaterials();
        clampSelection(materials, getPatterns());
        return materials.isEmpty() ? null : materials.get(this.selectedMaterialIndex);
    }

    private ArmorTrimPatternEntry getSelectedPatternEntry() {
        List<ArmorTrimPatternEntry> patterns = getPatterns();
        clampSelection(getMaterials(), patterns);
        return patterns.isEmpty() ? null : patterns.get(this.selectedPatternIndex);
    }

    private Holder<TrimMaterial> getSelectedMaterialHolder() {
        ArmorTrimMaterialEntry entry = getSelectedMaterialEntry();
        return entry == null ? null : entry.material();
    }

    private Holder<TrimPattern> getSelectedPatternHolder() {
        ArmorTrimPatternEntry entry = getSelectedPatternEntry();
        return entry == null ? null : entry.pattern();
    }

    private void clampSelection(List<ArmorTrimMaterialEntry> materials, List<ArmorTrimPatternEntry> patterns) {
        this.selectedMaterialIndex = materials.isEmpty()
                ? 0
                : Mth.clamp(this.selectedMaterialIndex, 0, materials.size() - 1);
        this.selectedPatternIndex = patterns.isEmpty()
                ? 0
                : Mth.clamp(this.selectedPatternIndex, 0, patterns.size() - 1);
    }

    private int findMaterialIndex(List<ArmorTrimMaterialEntry> materials, Identifier id) {
        if (id == null) {
            return 0;
        }
        for (int i = 0; i < materials.size(); i++) {
            if (id.equals(materials.get(i).id())) {
                return i;
            }
        }
        return 0;
    }

    private int findPatternIndex(List<ArmorTrimPatternEntry> patterns, Identifier id) {
        if (id == null) {
            return 0;
        }
        for (int i = 0; i < patterns.size(); i++) {
            if (id.equals(patterns.get(i).id())) {
                return i;
            }
        }
        return 0;
    }

    private Component getMaterialName(ArmorTrimMaterialEntry entry) {
        return entry == null ? Component.translatable(key("armortrim.no_materials")) : entry.material().value().description();
    }

    private Component getPatternName(ArmorTrimPatternEntry entry, ArmorTrimMaterialEntry material) {
        if (entry == null) {
            return Component.translatable(key("armortrim.no_patterns"));
        }
        if (material != null) {
            return entry.pattern().value().copyWithStyle(material.material());
        }
        return entry.pattern().value().description();
    }

    private Component getSelectedMaterialName() {
        return getMaterialName(getSelectedMaterialEntry());
    }

    private Component getSelectedPatternName() {
        return getPatternName(getSelectedPatternEntry(), getSelectedMaterialEntry());
    }

    private Component getPreviewEntityName() {
        return switch (this.previewEntity) {
            case PREVIEW_PLAYER -> Component.translatable(key("armortrim.preview.player"));
            case PREVIEW_ZOMBIE -> Component.translatable(key("armortrim.preview.zombie"));
            default -> Component.translatable(key("armortrim.preview.armor_stand"));
        };
    }

    private List<ArmorTrimEntry> getTrimEntries() {
        CustomData data = this.armorStack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return List.of();
        }

        CompoundTag tag = data.copyTag();
        if (!NbtCompat.contains(tag, ARMOR_TRIMS_CUSTOM_DATA_TAG, Tag.TAG_LIST)) {
            return List.of();
        }

        List<ArmorTrimEntry> entries = new ArrayList<>();
        ListTag list = NbtCompat.getList(tag, ARMOR_TRIMS_CUSTOM_DATA_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = NbtCompat.getCompound(list, i);
            Identifier materialId = Identifier.tryParse(NbtCompat.getString(entryTag, ARMOR_TRIM_MATERIAL_TAG));
            Identifier patternId = Identifier.tryParse(NbtCompat.getString(entryTag, ARMOR_TRIM_PATTERN_TAG));
            if (materialId != null && patternId != null) {
                entries.add(new ArmorTrimEntry(materialId, patternId));
            }
        }
        return entries;
    }

    private ArmorTrimEntry getLastTrimEntry() {
        List<ArmorTrimEntry> entries = getTrimEntries();
        return entries.isEmpty() ? null : entries.get(entries.size() - 1);
    }

    private int getTrimCount() {
        return getTrimEntries().size();
    }

    private boolean setSelectedTrimComponent() {
        Holder<TrimMaterial> material = getSelectedMaterialHolder();
        Holder<TrimPattern> pattern = getSelectedPatternHolder();
        if (material == null || pattern == null) {
            return false;
        }

        this.armorStack.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
        return true;
    }

    private boolean applyTrimEntry(ArmorTrimEntry entry) {
        Holder<TrimMaterial> material = CompatRegistries.TRIM_MATERIALS.getHolder(entry.materialId());
        Holder<TrimPattern> pattern = CompatRegistries.TRIM_PATTERNS.getHolder(entry.patternId());
        if (material == null || pattern == null) {
            return false;
        }

        this.armorStack.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
        return true;
    }

    private void renderArmorTrimEntityPreview(GuiGraphics guiGraphics, int left, int top, int right, int bottom) {
        EquipmentSlot slot = getArmorTrimEquipmentSlot(this.armorStack);
        if (slot == null || this.minecraft == null || this.minecraft.level == null) {
            return;
        }

        ItemStack trimmed = this.armorStack.copyWithCount(1);
        Holder<TrimMaterial> material = getSelectedMaterialHolder();
        Holder<TrimPattern> pattern = getSelectedPatternHolder();
        if (material != null && pattern != null) {
            trimmed.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
        }

        if (this.previewEntity == PREVIEW_PLAYER) {
            renderPlayerPreview(guiGraphics, left, top, right, bottom, slot, trimmed);
            return;
        }

        LivingEntity entity = getPreviewEntity();
        if (entity == null) {
            return;
        }

        setPreviewEquipment(entity, slot, trimmed);
        renderLivingEntity(guiGraphics, left, top, right, bottom, entity);
    }

    private void renderPlayerPreview(GuiGraphics guiGraphics, int left, int top, int right, int bottom,
                                     EquipmentSlot slot, ItemStack trimmed) {
        if (this.minecraft.player == null) {
            return;
        }

        List<ItemStack> previousEquipment = new ArrayList<>();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            previousEquipment.add(this.minecraft.player.getItemBySlot(equipmentSlot));
        }

        try {
            setPreviewEquipment(this.minecraft.player, slot, trimmed);
            renderLivingEntity(guiGraphics, left, top, right, bottom, this.minecraft.player);
        } finally {
            for (int i = 0; i < EquipmentSlot.VALUES.size(); i++) {
                this.minecraft.player.setItemSlot(EquipmentSlot.VALUES.get(i), previousEquipment.get(i));
            }
        }
    }

    private LivingEntity getPreviewEntity() {
        if (this.minecraft == null || this.minecraft.level == null) {
            return null;
        }

        if (this.previewEntity == PREVIEW_ZOMBIE) {
            if (this.zombiePreview == null || this.zombiePreview.level() != this.minecraft.level) {
                this.zombiePreview = EntityType.ZOMBIE.create(this.minecraft.level, EntitySpawnReason.LOAD);
            }
            return this.zombiePreview;
        }

        if (this.armorStandPreview == null || this.armorStandPreview.level() != this.minecraft.level) {
            ArmorStand armorStand = new ArmorStand(this.minecraft.level, 0.0D, 0.0D, 0.0D);
            armorStand.setShowArms(true);
            armorStand.setNoBasePlate(true);
            armorStand.setInvisible(false);
            this.armorStandPreview = armorStand;
        }
        return this.armorStandPreview;
    }

    private void setPreviewEquipment(LivingEntity entity, EquipmentSlot slot, ItemStack trimmed) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            entity.setItemSlot(equipmentSlot, ItemStack.EMPTY);
        }
        entity.setItemSlot(slot, trimmed);
    }

    private void renderLivingEntity(GuiGraphics guiGraphics, int left, int top, int right, int bottom, LivingEntity entity) {
        int height = Math.max(1, bottom - top);
        int scale = this.previewEntity == PREVIEW_ARMOR_STAND
                ? Mth.clamp(height / 3, 24, 38)
                : Mth.clamp(height / 2, 34, 48);
        float yOffset = this.previewEntity == PREVIEW_ARMOR_STAND ? 0.05F : 0.0F;
        InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, left, top, right, bottom,
                scale, yOffset, 0.25F, 0.0F, entity);
    }

    private static boolean isArmorTrimApplicable(ItemStack stack) {
        return getArmorTrimEquipmentSlot(stack) != null;
    }

    private static EquipmentSlot getArmorTrimEquipmentSlot(ItemStack stack) {
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || !isArmorTrimSlot(equippable.slot())) {
            return null;
        }
        return equippable.slot();
    }

    private static boolean isArmorTrimSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD
                || slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.LEGS
                || slot == EquipmentSlot.FEET;
    }

    private void drawCenteredClipped(GuiGraphics guiGraphics, Component text, int centerX, int y, int maxWidth, int color) {
        String value = text.getString();
        if (this.font.width(value) > maxWidth) {
            String ellipsis = "...";
            value = this.font.plainSubstrByWidth(value, Math.max(0, maxWidth - this.font.width(ellipsis))) + ellipsis;
        }
        guiGraphics.drawCenteredString(this.font, value, centerX, y, color);
    }

    private static String key(String suffix) {
        return "screen." + ModSource.MODID + "." + suffix;
    }

    private static String messageKey(String suffix) {
        return "message." + ModSource.MODID + "." + suffix;
    }
}

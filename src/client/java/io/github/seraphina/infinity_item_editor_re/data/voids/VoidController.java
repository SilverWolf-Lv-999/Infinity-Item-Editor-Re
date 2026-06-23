package io.github.seraphina.infinity_item_editor_re.data.voids;

import io.github.seraphina.infinity_item_editor_re.Config;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class VoidController {
    public static final String VERSION = "0.2";

    private final File dataFile;
    private final NonNullList<VoidElement> elementList = NonNullList.create();

    public VoidController(ItemStack stack) {
        this(fileForStack(stack));
    }

    private VoidController(File dataFile) {
        this.dataFile = dataFile;
        read();
    }

    public void read() {
        elementList.clear();

        if (dataFile == null || !dataFile.exists()) {
            return;
        }

        try {
            CompoundTag root = readRootTag();
            if (root == null) {
                return;
            }

            ListTag elements;
            if (root.contains("elements", Tag.TAG_LIST)) {
                elements = root.getList("elements", Tag.TAG_COMPOUND);
            } else if (root.contains("stacks", Tag.TAG_LIST)) {
                elements = root.getList("stacks", Tag.TAG_COMPOUND);
            } else {
                return;
            }

            for (Tag tag : elements) {
                if (tag instanceof CompoundTag compoundTag) {
                    VoidElement element = VoidElement.readFromTag(compoundTag);
                    if (!element.getStack().isEmpty()) {
                        elementList.add(element);
                    }
                }
            }
        } catch (Exception exception) {
            ModSource.LOGGER.error("Failed to load void for {}", dataFile.getName(), exception);
        }
    }

    public void write() {
        if (dataFile == null) {
            return;
        }

        try {
            File parent = dataFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                ModSource.LOGGER.warn("Failed to create void data directory {}", parent.getAbsolutePath());
            }

            CompoundTag root = new CompoundTag();
            ListTag elements = new ListTag();
            root.put("elements", elements);
            root.putString("void_version", VERSION);

            for (VoidElement voidElement : elementList) {
                elements.add(voidElement.writeToTag(new CompoundTag()));
            }

            NbtIo.writeCompressed(root, dataFile);
        } catch (Exception exception) {
            ModSource.LOGGER.error("Failed to save void for {}", dataFile.getName(), exception);
        }
    }

    private CompoundTag readRootTag() throws IOException {
        try {
            return NbtIo.readCompressed(dataFile);
        } catch (IOException compressedException) {
            try {
                return NbtIo.read(dataFile);
            } catch (IOException uncompressedException) {
                compressedException.addSuppressed(uncompressedException);
                throw compressedException;
            }
        }
    }
    public void addItemStack(Player player, ItemStack stack, String from) {
        if (stack == null || stack.isEmpty() || !hasMeaningfulTag(stack)) {
            return;
        }

        ItemStack savedStack = stack.copy();
        savedStack.setCount(1);
        if (savedStack.isDamageableItem()) {
            savedStack.setDamageValue(0);
        }

        for (VoidElement element : elementList) {
            if (isSameStack(element.getStack(), savedStack)) {
                if (element.addUuid(from, true)) {
                    write();
                }
                return;
            }
        }

        if (Config.voidAddNotification && player != null) {
            player.sendSystemMessage(Component.literal("Added ")
                    .append(savedStack.getHoverName())
                    .append(Component.literal(" to Infinity Void.")));
        }

        VoidElement element = new VoidElement(savedStack);
        element.addUuid(from, false);
        elementList.add(element);
        write();
    }

    public NonNullList<VoidElement> getElementList() {
        return elementList;
    }

    public static synchronized void loadVoidToList(List<ItemStack> list) {
        File voidDirectory = getVoidDirectory();
        if (voidDirectory == null || !voidDirectory.isDirectory()) {
            return;
        }

        File[] files = voidDirectory.listFiles((directory, name) -> name.endsWith(".nbt"));
        if (files == null || files.length == 0) {
            return;
        }

        Arrays.sort(files, Comparator.comparing(File::getName));
        for (File file : files) {
            VoidController controller = new VoidController(file);
            NonNullList<VoidElement> elements = controller.getElementList();
            if (elements.isEmpty()) {
                continue;
            }

            if (Config.voidTabHideHeads && elements.get(0).getStack().is(Items.PLAYER_HEAD)) {
                continue;
            }

            for (VoidElement element : elements) {
                ItemStack stack = element.getStack().copy();
                stack.setCount(1);
                list.add(stack);
            }
        }
    }

    private static boolean hasMeaningfulTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || tag.isEmpty()) {
            return false;
        }

        return !(stack.getItem() instanceof SpawnEggItem && tag.size() == 1 && tag.contains("EntityTag", Tag.TAG_COMPOUND));
    }

    private static boolean isSameStack(ItemStack first, ItemStack second) {
        if (first == second) {
            return true;
        }

        if (first.isEmpty()) {
            return second.isEmpty();
        }

        if (second.isEmpty() || !first.is(second.getItem())) {
            return false;
        }

        if ((first.isDamageableItem() || second.isDamageableItem()) && first.getDamageValue() != second.getDamageValue()) {
            return false;
        }

        CompoundTag firstTag = first.getTag();
        CompoundTag secondTag = second.getTag();
        boolean firstTagEmpty = firstTag == null || firstTag.isEmpty();
        boolean secondTagEmpty = secondTag == null || secondTag.isEmpty();

        if (firstTagEmpty || secondTagEmpty) {
            return firstTagEmpty == secondTagEmpty;
        }

        return firstTag.equals(secondTag);
    }

    private static File fileForStack(ItemStack stack) {
        File voidDirectory = getVoidDirectory();
        if (voidDirectory == null) {
            return null;
        }

        ResourceLocation itemName = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String fileName = (itemName == null ? "unknown" : itemName.toString().replace(':', '.')) + ".nbt";
        return new File(voidDirectory, fileName);
    }

    private static File getVoidDirectory() {
        if (ModSource.dataDir == null) {
            return null;
        }

        File voidDirectory = new File(ModSource.dataDir, "void");
        if (!voidDirectory.exists() && !voidDirectory.mkdirs()) {
            ModSource.LOGGER.warn("Failed to create void data directory {}", voidDirectory.getAbsolutePath());
        }
        return voidDirectory;
    }
}

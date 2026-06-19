package io.github.seraphina.infinity_item_editor_re.init;

import io.github.seraphina.infinity_item_editor_re.Config;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.data.realms.RealmController;
import io.github.seraphina.infinity_item_editor_re.data.voids.VoidController;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class CreativeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModSource.MODID);

    public static final RegistryObject<CreativeModeTab> REALM = CREATIVE_TABS.register("realm", () -> searchTab(
            "realm",
            () -> new ItemStack(Blocks.ENDER_CHEST),
            CreativeTabRegistry::fillRealm
    ));

    public static final RegistryObject<CreativeModeTab> UNAVAILABLE = CREATIVE_TABS.register("unavailable", () -> searchTab(
            "unavailable",
            () -> new ItemStack(Blocks.BARRIER),
            CreativeTabRegistry::fillUnavailable
    ));

    public static final RegistryObject<CreativeModeTab> BANNERS = CREATIVE_TABS.register("banners", () -> searchTab(
            "banners",
            () -> new ItemStack(Items.RED_BANNER),
            CreativeTabRegistry::fillBanners
    ));

    public static final RegistryObject<CreativeModeTab> SKULLS = CREATIVE_TABS.register("skulls", () -> searchTab(
            "skulls",
            () -> new ItemStack(Items.PLAYER_HEAD),
            CreativeTabRegistry::fillSkulls
    ));

    public static final RegistryObject<CreativeModeTab> THIEF = CREATIVE_TABS.register("thief", () -> searchTab(
            "thief",
            () -> new ItemStack(Items.FEATHER),
            CreativeTabRegistry::fillThief
    ));

    public static final RegistryObject<CreativeModeTab> FIREWORKS = CREATIVE_TABS.register("fireworks", () -> searchTab(
            "fireworks",
            () -> new ItemStack(Items.FIREWORK_ROCKET),
            CreativeTabRegistry::fillFireworks
    ));

    public static final RegistryObject<CreativeModeTab> VOID = CREATIVE_TABS.register("void", () -> searchTab(
            "void",
            () -> new ItemStack(Blocks.BLACK_STAINED_GLASS),
            CreativeTabRegistry::fillVoid
    ));

    private static final String[] MHF_HEADS = {
            "MHF_Alex", "MHF_Blaze", "MHF_CaveSpider", "MHF_Chicken", "MHF_Cow", "MHF_Creeper",
            "MHF_Enderman", "MHF_Ghast", "MHF_Golem", "MHF_Herobrine", "MHF_LavaSlime", "MHF_MushroomCow",
            "MHF_Ocelot", "MHF_Pig", "MHF_PigZombie", "MHF_Sheep", "MHF_Skeleton", "MHF_Slime",
            "MHF_Spider", "MHF_Squid", "MHF_Steve", "MHF_Villager", "MHF_Wolf", "MHF_WSkeleton",
            "MHF_Zombie", "MHF_Cactus", "MHF_Cake", "MHF_Chest", "MHF_CoconutB", "MHF_CoconutG",
            "MHF_Melon", "MHF_OakLog", "MHF_Present1", "MHF_Present2", "MHF_Pumpkin", "MHF_TNT",
            "MHF_TNT2", "MHF_ArrowUp", "MHF_ArrowDown", "MHF_ArrowLeft", "MHF_ArrowRight", "MHF_Exclamation",
            "MHF_Question"
    };

    private CreativeTabRegistry() {
    }

    private static CreativeModeTab searchTab(String name, Supplier<ItemStack> icon, CreativeModeTab.DisplayItemsGenerator generator) {
        return CreativeModeTab.builder()
                .title(Component.translatable("itemGroup." + ModSource.MODID + "." + name))
                .icon(icon)
                .withSearchBar()
                .displayItems(generator)
                .build();
    }

    private static void fillRealm(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        RealmController realmController = ModSource.getRealmController();
        List<ItemStack> stacks = new ArrayList<>();

        if (realmController != null) {
            for (ItemStack stack : realmController.getStackList()) {
                addUnique(stacks, normalizedTabStack(stack));
            }
        }

        if (stacks.isEmpty()) {
            output.accept(Blocks.ENDER_CHEST);
            return;
        }

        output.acceptAll(stacks);
    }

    private static void fillUnavailable(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (!Config.getIsUnavailableTabEnabled()) {
            return;
        }

        output.accept(Items.BARRIER);
        output.accept(Items.COMMAND_BLOCK);
        output.accept(Items.CHAIN_COMMAND_BLOCK);
        output.accept(Items.REPEATING_COMMAND_BLOCK);
        output.accept(Items.COMMAND_BLOCK_MINECART);
        output.accept(Items.STRUCTURE_BLOCK);
        output.accept(Items.STRUCTURE_VOID);
        output.accept(Items.JIGSAW);
        output.accept(Items.DEBUG_STICK);
        output.accept(Items.KNOWLEDGE_BOOK);
        output.accept(Items.LIGHT);
        output.accept(Items.SPAWNER);
        output.accept(Items.ENCHANTED_BOOK);
    }

    private static void fillBanners(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (!Config.getIsBannerTabEnabled()) {
            return;
        }

        output.accept(Items.WHITE_BANNER);
        output.accept(Items.ORANGE_BANNER);
        output.accept(Items.MAGENTA_BANNER);
        output.accept(Items.LIGHT_BLUE_BANNER);
        output.accept(Items.YELLOW_BANNER);
        output.accept(Items.LIME_BANNER);
        output.accept(Items.PINK_BANNER);
        output.accept(Items.GRAY_BANNER);
        output.accept(Items.LIGHT_GRAY_BANNER);
        output.accept(Items.CYAN_BANNER);
        output.accept(Items.PURPLE_BANNER);
        output.accept(Items.BLUE_BANNER);
        output.accept(Items.BROWN_BANNER);
        output.accept(Items.GREEN_BANNER);
        output.accept(Items.RED_BANNER);
        output.accept(Items.BLACK_BANNER);
        output.accept(Items.SHIELD);
    }

    private static void fillSkulls(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (!Config.getIsHeadTabEnabled()) {
            return;
        }

        List<ItemStack> heads = new ArrayList<>();
        for (String owner : MHF_HEADS) {
            addUnique(heads, createPlayerHead(owner));
        }
        output.acceptAll(heads);
    }

    private static void fillThief(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (!Config.getIsThiefTabEnabled()) {
            return;
        }

        output.accept(Items.FEATHER);
    }

    private static void fillFireworks(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (!Config.getIsFireworkTabEnabled()) {
            return;
        }

        output.accept(Items.FIREWORK_ROCKET);
        output.accept(Items.FIREWORK_STAR);
        output.accept(Items.GUNPOWDER);
        acceptStack(output, fireworkStar(0, 0xE14141));
        acceptStack(output, fireworkStar(1, 0xF0F06B));
        acceptStack(output, fireworkStar(2, 0x4A80FF));
        acceptStack(output, fireworkRocket((byte) 1));
        acceptStack(output, fireworkRocket((byte) 2));
        acceptStack(output, fireworkRocket((byte) 3));
    }

    private static void fillVoid(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (!Config.getIsVoidEnabled()) {
            return;
        }

        List<ItemStack> stacks = new ArrayList<>();
        VoidController.loadVoidToList(stacks);

        if (stacks.isEmpty()) {
            output.accept(Items.BLACK_STAINED_GLASS);
            return;
        }

        output.acceptAll(stacks);
    }

    private static ItemStack createPlayerHead(String owner) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        stack.getOrCreateTag().putString("SkullOwner", owner);

        CompoundTag display = stack.getOrCreateTagElement("display");
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal("Marc's Head Format"))));
        display.put("Lore", lore);
        return stack;
    }

    private static ItemStack fireworkStar(int type, int color) {
        ItemStack stack = new ItemStack(Items.FIREWORK_STAR);
        CompoundTag explosion = stack.getOrCreateTagElement("Explosion");
        explosion.putByte("Type", (byte) type);
        explosion.putIntArray("Colors", new int[]{color});
        return stack;
    }

    private static ItemStack fireworkRocket(byte flight) {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        stack.getOrCreateTagElement("Fireworks").putByte("Flight", flight);
        return stack;
    }

    private static void acceptStack(CreativeModeTab.Output output, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        output.accept(normalizedTabStack(stack));
    }

    private static ItemStack normalizedTabStack(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    private static void addUnique(List<ItemStack> stacks, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        for (ItemStack existingStack : stacks) {
            if (ItemStack.isSameItemSameTags(existingStack, stack)) {
                return;
            }
        }

        stacks.add(stack);
    }
}

package io.github.seraphina.infinity_item_editor_re.init;

import io.github.seraphina.infinity_item_editor_re.Config;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.ClientCreativeTabData;
import io.github.seraphina.infinity_item_editor_re.data.realms.RealmController;
import io.github.seraphina.infinity_item_editor_re.data.voids.VoidController;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class CreativeTabRegistry {
    private static final FireworkRocketItem.Shape[] FIREWORK_SHAPES = {
            FireworkRocketItem.Shape.SMALL_BALL,
            FireworkRocketItem.Shape.LARGE_BALL,
            FireworkRocketItem.Shape.STAR,
            FireworkRocketItem.Shape.CREEPER,
            FireworkRocketItem.Shape.BURST
    };

    public static CreativeModeTab REALM;
    public static CreativeModeTab UNAVAILABLE;
    public static CreativeModeTab BANNERS;
    public static CreativeModeTab SKULLS;
    public static CreativeModeTab THIEF;
    public static CreativeModeTab FIREWORKS;
    public static CreativeModeTab VOID;

    private static boolean registered;

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

    public static void register() {
        if (registered) {
            return;
        }

        REALM = registerTab("realm", searchTab(
                "realm",
                () -> new ItemStack(Blocks.ENDER_CHEST),
                CreativeTabRegistry::fillRealm
        ));
        UNAVAILABLE = registerTab("unavailable", searchTab(
                "unavailable",
                () -> new ItemStack(Blocks.BARRIER),
                CreativeTabRegistry::fillUnavailable
        ));
        BANNERS = registerTab("banners", searchTab(
                "banners",
                () -> new ItemStack(Items.RED_BANNER),
                CreativeTabRegistry::fillBanners
        ));
        SKULLS = registerTab("skulls", searchTab(
                "skulls",
                () -> new ItemStack(Items.PLAYER_HEAD),
                CreativeTabRegistry::fillSkulls
        ));
        THIEF = registerTab("thief", searchTab(
                "thief",
                () -> new ItemStack(Items.FEATHER),
                CreativeTabRegistry::fillThief
        ));
        FIREWORKS = registerTab("fireworks", searchTab(
                "fireworks",
                () -> new ItemStack(Items.FIREWORK_ROCKET),
                CreativeTabRegistry::fillFireworks
        ));
        VOID = registerTab("void", searchTab(
                "void",
                () -> new ItemStack(Blocks.BLACK_STAINED_GLASS),
                CreativeTabRegistry::fillVoid
        ));

        registered = true;
    }

    private static CreativeModeTab registerTab(String name, CreativeModeTab tab) {
        return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(ModSource.MODID, name), tab);
    }

    private static CreativeModeTab searchTab(String name, Supplier<ItemStack> icon, CreativeModeTab.DisplayItemsGenerator generator) {
        return FabricItemGroup.builder()
                .title(Component.translatable("itemGroup." + ModSource.MODID + "." + name))
                .icon(icon)
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

        List<ItemStack> stacks = new ArrayList<>();
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.BARRIER)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.COMMAND_BLOCK)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.CHAIN_COMMAND_BLOCK)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.REPEATING_COMMAND_BLOCK)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.COMMAND_BLOCK_MINECART)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.STRUCTURE_BLOCK)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.STRUCTURE_VOID)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.JIGSAW)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.DEBUG_STICK)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.KNOWLEDGE_BOOK)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.LIGHT)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.SPAWNER)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.PIG_SPAWN_EGG)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.POTION)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.SPLASH_POTION)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.LINGERING_POTION)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.TIPPED_ARROW)));
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.ENCHANTED_BOOK)));
        addUnavailableVariants(parameters, stacks);
        addUncategorizedRegistryItems(parameters, stacks);
        output.acceptAll(stacks);
    }

    private static void fillBanners(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (!Config.getIsBannerTabEnabled()) {
            return;
        }

        List<ItemStack> banners = new ArrayList<>();
        addUnique(banners, normalizedTabStack(new ItemStack(Items.WHITE_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.ORANGE_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.MAGENTA_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.LIGHT_BLUE_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.YELLOW_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.LIME_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.PINK_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.GRAY_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.LIGHT_GRAY_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.CYAN_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.PURPLE_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BLUE_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BROWN_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.GREEN_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.RED_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BLACK_BANNER)));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.SHIELD)));
        for (DyeColor color : DyeColor.values()) {
            addUnique(banners, normalizedTabStack(shield(color)));
        }
        ClientCreativeTabData.addBannerVariants(banners);
        output.acceptAll(banners);
    }

    private static void fillSkulls(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (!Config.getIsHeadTabEnabled()) {
            return;
        }

        List<ItemStack> heads = new ArrayList<>();
        ClientCreativeTabData.addPlayerHeads(heads);
        for (String owner : MHF_HEADS) {
            addUnique(heads, createPlayerHead(owner));
        }
        output.acceptAll(heads);
    }

    private static void fillThief(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (!Config.getIsThiefTabEnabled()) {
            return;
        }

        List<ItemStack> stacks = new ArrayList<>();
        ClientCreativeTabData.addThiefItems(stacks);
        if (stacks.isEmpty()) {
            output.accept(Items.FEATHER);
            return;
        }

        output.acceptAll(stacks);
    }

    private static void fillFireworks(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (!Config.getIsFireworkTabEnabled()) {
            return;
        }

        List<ItemStack> fireworks = new ArrayList<>();
        addUnique(fireworks, normalizedTabStack(new ItemStack(Items.FIREWORK_ROCKET)));
        addUnique(fireworks, normalizedTabStack(new ItemStack(Items.FIREWORK_STAR)));
        addUnique(fireworks, normalizedTabStack(new ItemStack(Items.GUNPOWDER)));
        addUnique(fireworks, normalizedTabStack(fireworkStar(0, 0xE14141)));
        addUnique(fireworks, normalizedTabStack(fireworkStar(1, 0xF0F06B)));
        addUnique(fireworks, normalizedTabStack(fireworkStar(2, 0x4A80FF)));
        addUnique(fireworks, normalizedTabStack(fireworkRocket((byte) 1)));
        addUnique(fireworks, normalizedTabStack(fireworkRocket((byte) 2)));
        addUnique(fireworks, normalizedTabStack(fireworkRocket((byte) 3)));
        ClientCreativeTabData.addFireworkVariants(fireworks);
        output.acceptAll(fireworks);
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
        explosion.putByte("Type", (byte) getFireworkShape(type).getId());
        explosion.putIntArray("Colors", new int[]{color});
        return stack;
    }

    private static FireworkRocketItem.Shape getFireworkShape(int type) {
        if (type < 0 || type >= FIREWORK_SHAPES.length) {
            return FireworkRocketItem.Shape.SMALL_BALL;
        }
        return FIREWORK_SHAPES[type];
    }

    private static ItemStack fireworkRocket(byte flight) {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        stack.getOrCreateTagElement("Fireworks").putByte("Flight", flight);
        return stack;
    }

    private static ItemStack shield(DyeColor color) {
        ItemStack stack = new ItemStack(Items.SHIELD);
        stack.getOrCreateTagElement("BlockEntityTag").putInt("Base", color.getId());
        return stack;
    }

    private static void addUnavailableVariants(CreativeModeTab.ItemDisplayParameters parameters, List<ItemStack> stacks) {
        addPotionVariants(parameters, stacks, Items.POTION);
        addPotionVariants(parameters, stacks, Items.SPLASH_POTION);
        addPotionVariants(parameters, stacks, Items.LINGERING_POTION);
        addPotionVariants(parameters, stacks, Items.TIPPED_ARROW);
        addSpawnEggVariants(parameters, stacks);
        addEnchantedBookVariants(stacks);
    }

    private static void addPotionVariants(CreativeModeTab.ItemDisplayParameters parameters, List<ItemStack> stacks, Item item) {
        if (!item.isEnabled(parameters.enabledFeatures())) {
            return;
        }

        BuiltInRegistries.POTION.stream()
                .filter(potion -> potion != Potions.EMPTY)
                .forEach(potion -> addUnique(stacks, potionStack(item, potion)));
    }

    private static ItemStack potionStack(Item item, Potion potion) {
        ItemStack stack = new ItemStack(item);
        PotionUtils.setPotion(stack, potion);
        return normalizedTabStack(stack);
    }

    private static void addSpawnEggVariants(CreativeModeTab.ItemDisplayParameters parameters, List<ItemStack> stacks) {
        BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof SpawnEggItem)
                .filter(item -> item.isEnabled(parameters.enabledFeatures()))
                .forEach(item -> addUnique(stacks, normalizedTabStack(new ItemStack(item))));
    }

    private static void addEnchantedBookVariants(List<ItemStack> stacks) {
        BuiltInRegistries.ENCHANTMENT.stream()
                .filter(Enchantment::isDiscoverable)
                .forEach(enchantment -> {
                    for (int level = enchantment.getMinLevel(); level <= enchantment.getMaxLevel(); level++) {
                        addUnique(stacks, normalizedTabStack(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level))));
                    }
                });
    }

    private static void addUncategorizedRegistryItems(CreativeModeTab.ItemDisplayParameters parameters, List<ItemStack> stacks) {
        Set<Item> categorizedItems = new HashSet<>();
        for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
            var tabKey = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab);
            if (tabKey.isPresent() && ModSource.MODID.equals(tabKey.get().location().getNamespace())) {
                continue;
            }

            for (ItemStack stack : tab.getDisplayItems()) {
                categorizedItems.add(stack.getItem());
            }
            for (ItemStack stack : tab.getSearchTabDisplayItems()) {
                categorizedItems.add(stack.getItem());
            }
        }

        BuiltInRegistries.ITEM.stream()
                .filter(item -> item != Items.AIR)
                .filter(item -> item.isEnabled(parameters.enabledFeatures()))
                .filter(item -> !categorizedItems.contains(item))
                .forEach(item -> addUnique(stacks, normalizedTabStack(new ItemStack(item))));
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

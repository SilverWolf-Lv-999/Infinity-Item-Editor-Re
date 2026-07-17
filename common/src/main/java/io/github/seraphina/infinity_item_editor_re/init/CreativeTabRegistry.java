package io.github.seraphina.infinity_item_editor_re.init;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackCompat;

import io.github.seraphina.infinity_item_editor_re.util.ComponentCompat;

import io.github.seraphina.infinity_item_editor_re.util.CompatRegistries;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;

import io.github.seraphina.infinity_item_editor_re.Config;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.data.realms.RealmController;
import io.github.seraphina.infinity_item_editor_re.data.voids.VoidController;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.Potion;
import io.github.seraphina.infinity_item_editor_re.util.PotionCompat;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public final class CreativeTabRegistry {
    private static final FireworkExplosion.Shape[] FIREWORK_SHAPES = {
            FireworkExplosion.Shape.SMALL_BALL,
            FireworkExplosion.Shape.LARGE_BALL,
            FireworkExplosion.Shape.STAR,
            FireworkExplosion.Shape.CREEPER,
            FireworkExplosion.Shape.BURST
    };

    public static final TabHandle REALM = new TabHandle();
    public static final TabHandle UNAVAILABLE = new TabHandle();
    public static final TabHandle BANNERS = new TabHandle();
    public static final TabHandle SKULLS = new TabHandle();
    public static final TabHandle THIEF = new TabHandle();
    public static final TabHandle FIREWORKS = new TabHandle();
    public static final TabHandle VOID = new TabHandle();
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

    public static synchronized void register(TabRegistrar registrar, TabFactory tabFactory) {
        if (registered) {
            return;
        }

        register(REALM, registrar, tabFactory, "realm", CreativeModeTab.Row.TOP, 0,
                () -> new ItemStack(Blocks.ENDER_CHEST), CreativeTabRegistry::fillRealm);
        register(UNAVAILABLE, registrar, tabFactory, "unavailable", CreativeModeTab.Row.TOP, 1,
                () -> new ItemStack(Blocks.BARRIER), CreativeTabRegistry::fillUnavailable);
        register(BANNERS, registrar, tabFactory, "banners", CreativeModeTab.Row.TOP, 2,
                () -> new ItemStack(Items.BANNER.pick(DyeColor.RED)), CreativeTabRegistry::fillBanners);
        register(SKULLS, registrar, tabFactory, "skulls", CreativeModeTab.Row.TOP, 3,
                () -> new ItemStack(Items.PLAYER_HEAD), CreativeTabRegistry::fillSkulls);
        register(THIEF, registrar, tabFactory, "thief", CreativeModeTab.Row.BOTTOM, 0,
                () -> new ItemStack(Items.FEATHER), CreativeTabRegistry::fillThief);
        register(FIREWORKS, registrar, tabFactory, "fireworks", CreativeModeTab.Row.BOTTOM, 1,
                () -> new ItemStack(Items.FIREWORK_ROCKET), CreativeTabRegistry::fillFireworks);
        register(VOID, registrar, tabFactory, "void", CreativeModeTab.Row.BOTTOM, 2,
                () -> new ItemStack(Blocks.STAINED_GLASS.pick(DyeColor.BLACK)), CreativeTabRegistry::fillVoid);
        registered = true;
    }

    private static void register(
            TabHandle handle,
            TabRegistrar registrar,
            TabFactory tabFactory,
            String name,
            CreativeModeTab.Row row,
            int column,
            Supplier<ItemStack> icon,
            TabContentsGenerator generator
    ) {
        handle.bind(registrar.register(name, () -> tabFactory.create(name, row, column, icon, generator)));
    }

    private static void fillRealm(CreativeModeTab.ItemDisplayParameters parameters, TabOutput output) {
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

    private static void fillUnavailable(CreativeModeTab.ItemDisplayParameters parameters, TabOutput output) {
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
        addUnique(stacks, normalizedTabStack(new ItemStack(Items.TRIAL_SPAWNER)));
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

    private static void fillBanners(CreativeModeTab.ItemDisplayParameters parameters, TabOutput output) {
        if (!Config.getIsBannerTabEnabled()) {
            return;
        }

        List<ItemStack> banners = new ArrayList<>();
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.WHITE))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.ORANGE))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.MAGENTA))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.LIGHT_BLUE))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.YELLOW))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.LIME))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.PINK))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.GRAY))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.LIGHT_GRAY))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.CYAN))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.PURPLE))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.BLUE))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.BROWN))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.GREEN))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.RED))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.BANNER.pick(DyeColor.BLACK))));
        addUnique(banners, normalizedTabStack(new ItemStack(Items.SHIELD)));
        for (DyeColor color : DyeColor.values()) {
            addUnique(banners, normalizedTabStack(shield(color)));
        }
        io.github.seraphina.infinity_item_editor_re.client.ClientCreativeTabData.addBannerVariants(banners);
        output.acceptAll(banners);
    }

    private static void fillSkulls(CreativeModeTab.ItemDisplayParameters parameters, TabOutput output) {
        if (!Config.getIsHeadTabEnabled()) {
            return;
        }

        List<ItemStack> heads = new ArrayList<>();
        io.github.seraphina.infinity_item_editor_re.client.ClientCreativeTabData.addPlayerHeads(heads);
        for (String owner : MHF_HEADS) {
            addUnique(heads, createPlayerHead(owner));
        }
        output.acceptAll(heads);
    }

    private static void fillThief(CreativeModeTab.ItemDisplayParameters parameters, TabOutput output) {
        if (!Config.getIsThiefTabEnabled()) {
            return;
        }

        List<ItemStack> stacks = new ArrayList<>();
        io.github.seraphina.infinity_item_editor_re.client.ClientCreativeTabData.addThiefItems(stacks);
        if (stacks.isEmpty()) {
            output.accept(Items.FEATHER);
            return;
        }

        output.acceptAll(stacks);
    }

    private static void fillFireworks(CreativeModeTab.ItemDisplayParameters parameters, TabOutput output) {
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
        io.github.seraphina.infinity_item_editor_re.client.ClientCreativeTabData.addFireworkVariants(fireworks);
        output.acceptAll(fireworks);
    }

    private static void fillVoid(CreativeModeTab.ItemDisplayParameters parameters, TabOutput output) {
        if (!Config.getIsVoidEnabled()) {
            return;
        }

        List<ItemStack> stacks = new ArrayList<>();
        VoidController.loadVoidToList(stacks);

        if (stacks.isEmpty()) {
            output.accept(Items.STAINED_GLASS.pick(DyeColor.BLACK));
            return;
        }

        output.acceptAll(stacks);
    }

    private static ItemStack createPlayerHead(String owner) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        ItemStackNbt.getOrCreate(stack).putString("SkullOwner", owner);

        CompoundTag display = ItemStackNbt.getOrCreateElement(stack, "display");
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(ComponentCompat.toJson(Component.literal("Marc's Head Format"))));
        display.put("Lore", lore);
        return stack;
    }

    private static ItemStack fireworkStar(int type, int color) {
        ItemStack stack = new ItemStack(Items.FIREWORK_STAR);
        CompoundTag explosion = ItemStackNbt.getOrCreateElement(stack, "Explosion");
        explosion.putByte("Type", (byte) getFireworkShape(type).getId());
        explosion.putIntArray("Colors", new int[]{color});
        return stack;
    }

    private static FireworkExplosion.Shape getFireworkShape(int type) {
        if (type < 0 || type >= FIREWORK_SHAPES.length) {
            return FireworkExplosion.Shape.SMALL_BALL;
        }
        return FIREWORK_SHAPES[type];
    }

    private static ItemStack fireworkRocket(byte flight) {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        ItemStackNbt.getOrCreateElement(stack, "Fireworks").putByte("Flight", flight);
        return stack;
    }

    private static ItemStack shield(DyeColor color) {
        ItemStack stack = new ItemStack(Items.SHIELD);
        ItemStackNbt.getOrCreateElement(stack, "BlockEntityTag").putInt("Base", color.getId());
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
                .forEach(potion -> addUnique(stacks, potionStack(item, potion)));
    }

    private static ItemStack potionStack(Item item, Potion potion) {
        ItemStack stack = new ItemStack(item);
        PotionCompat.setPotion(stack, potion);
        return normalizedTabStack(stack);
    }

    private static void addSpawnEggVariants(CreativeModeTab.ItemDisplayParameters parameters, List<ItemStack> stacks) {
        BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof SpawnEggItem)
                .filter(item -> item.isEnabled(parameters.enabledFeatures()))
                .forEach(item -> addUnique(stacks, normalizedTabStack(new ItemStack(item))));
    }

    private static void addEnchantedBookVariants(List<ItemStack> stacks) {
        CompatRegistries.ENCHANTMENTS.getHolders()
                .forEach(enchantmentHolder -> {
                    Enchantment enchantment = enchantmentHolder.value();
                    for (int level = enchantment.getMinLevel(); level <= enchantment.getMaxLevel(); level++) {
                        addUnique(stacks, normalizedTabStack(EnchantmentHelper.createBook(new EnchantmentInstance(enchantmentHolder, level))));
                    }
                });
    }

    private static void addUncategorizedRegistryItems(CreativeModeTab.ItemDisplayParameters parameters, List<ItemStack> stacks) {
        Set<Item> categorizedItems = new HashSet<>();
        for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
            var tabKey = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab);
            if (tabKey.isPresent() && ModSource.MODID.equals(tabKey.get().identifier().getNamespace())) {
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

    private static void acceptStack(TabOutput output, ItemStack stack) {
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
            if (ItemStackCompat.isSameItemSameTags(existingStack, stack)) {
                return;
            }
        }

        stacks.add(stack);
    }

    @FunctionalInterface
    public interface TabRegistrar {
        Supplier<CreativeModeTab> register(String name, Supplier<CreativeModeTab> factory);
    }

    @FunctionalInterface
    public interface TabFactory {
        CreativeModeTab create(
                String name,
                CreativeModeTab.Row row,
                int column,
                Supplier<ItemStack> icon,
                TabContentsGenerator generator
        );
    }

    @FunctionalInterface
    public interface TabContentsGenerator {
        void accept(CreativeModeTab.ItemDisplayParameters parameters, TabOutput output);
    }

    @FunctionalInterface
    public interface TabOutput {
        void accept(ItemStack stack);

        default void accept(ItemLike item) {
            accept(new ItemStack(item));
        }

        default void acceptAll(Iterable<ItemStack> stacks) {
            stacks.forEach(this::accept);
        }
    }

    public static final class TabHandle implements Supplier<CreativeModeTab> {
        private Supplier<CreativeModeTab> delegate;

        private void bind(Supplier<CreativeModeTab> delegate) {
            if (this.delegate != null) {
                throw new IllegalStateException("Creative tab handle is already bound");
            }
            this.delegate = Objects.requireNonNull(delegate);
        }

        public boolean isBound() {
            return this.delegate != null;
        }

        @Override
        public CreativeModeTab get() {
            if (this.delegate == null) {
                throw new IllegalStateException("Creative tab handle has not been registered");
            }
            return this.delegate.get();
        }
    }
}

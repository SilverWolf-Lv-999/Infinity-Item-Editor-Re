package io.github.seraphina.infinity_item_editor_re.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PotionCompat {
    public static final String TAG_CUSTOM_POTION_COLOR = "CustomPotionColor";

    private PotionCompat() {
    }

    public static int getColor(ItemStack stack) {
        return contents(stack).getColor();
    }

    public static void setPotion(ItemStack stack, Potion potion) {
        stack.set(DataComponents.POTION_CONTENTS, contents(stack).withPotion(BuiltInRegistries.POTION.wrapAsHolder(potion)));
    }

    public static void setCustomColor(ItemStack stack, int color) {
        PotionContents old = contents(stack);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(old.potion(), Optional.of(color), old.customEffects(), old.customName()));
    }

    public static void setCustomEffects(ItemStack stack, List<MobEffectInstance> effects) {
        PotionContents old = contents(stack);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(old.potion(), old.customColor(), List.copyOf(effects), old.customName()));
    }

    public static List<MobEffectInstance> getCustomEffects(ItemStack stack) {
        return new ArrayList<>(contents(stack).customEffects());
    }

    private static PotionContents contents(ItemStack stack) {
        return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    }
}

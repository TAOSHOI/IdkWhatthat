package com.voids.whisper.modules;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class InventoryGaslightManager {
    private static final Random RANDOM = new Random();

    public static ItemStack getFakeItem(ItemStack original) {
        if (ParanoiaManager.getParanoia() > 40.0f && RANDOM.nextDouble() < 0.05) {
            // Подменяем алмазные и незеритовые вещи на мусор визуально
            if (original.getItem() == Items.DIAMOND_PICKAXE) return Items.BONE.getDefaultStack();
            if (original.getItem() == Items.DIAMOND_SWORD) return Items.ROTTEN_FLESH.getDefaultStack();
            if (original.getItem() == Items.NETHERITE_SWORD) return Items.POISONOUS_POTATO.getDefaultStack();
            if (original.getItem() == Items.TORCH) return Items.REDSTONE_TORCH.getDefaultStack();
        }
        return original;
    }
}
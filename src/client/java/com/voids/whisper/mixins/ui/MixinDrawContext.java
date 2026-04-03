package com.voids.whisper.mixins.ui;

import com.voids.whisper.modules.InventoryGaslightManager;
import com.voids.whisper.modules.UIParanoiaManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(DrawContext.class)
public class MixinDrawContext {

    // Подмена предмета в инвентаре/руке
    @ModifyVariable(method = "drawItem(Lnet/minecraft/item/ItemStack;III)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ItemStack modifyDrawnItem(ItemStack original) {
        if (original != null && !original.isEmpty()) {
            return InventoryGaslightManager.getFakeItem(original);
        }
        return original;
    }

    // Фейковая прочность
    @Inject(method = "drawItemBar", at = @At("HEAD"), cancellable = true)
    private void onDrawItemBar(ItemStack stack, int x, int y, CallbackInfo ci) {
        if (UIParanoiaManager.isToolVisuallyBroken && stack.isDamageable()) {
            DrawContext context = (DrawContext) (Object) this;
            context.fill(x + 2, y + 13, x + 15, y + 15, 0xFF000000);
            context.fill(x + 2, y + 13, x + 3, y + 14, 0xFFFF0000); // 1 пиксель красный
            ci.cancel();
        }
    }
}
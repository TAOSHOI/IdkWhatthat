package com.voids.whisper.mixins.ui;

import com.voids.whisper.modules.UIParanoiaManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(DrawContext.class)
public class MixinDrawContext {

    // Перехватываем метод отрисовки полоски прочности
    @Inject(method = "drawItemBar", at = @At("HEAD"), cancellable = true)
    private void onDrawItemBar(ItemStack stack, int x, int y, CallbackInfo ci) {
        if (UIParanoiaManager.isToolVisuallyBroken && stack.isDamageable()) {
            DrawContext context = (DrawContext) (Object) this;

            // Ручная отрисовка фейковой красной полоски прочности
            int barWidth = 1; // Почти сломан (длина 1 пиксель из 13)
            int barColor = 0xFF0000; // Ярко-красный

            // Фон полоски
            context.fill(x + 2, y + 13, x + 15, y + 15, 0xFF000000);
            // Сама полоска
            context.fill(x + 2, y + 13, x + 2 + barWidth, y + 14, 0xFF000000 | barColor);

            ci.cancel(); // Отменяем ванильную отрисовку для этого предмета
        }
    }
}
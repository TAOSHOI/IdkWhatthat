package com.voids.whisper.mixins.meta;

import com.voids.whisper.config.ParanoiaSettings;
import com.voids.whisper.modules.ParanoiaManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Environment(EnvType.CLIENT)
@Mixin(ScreenshotRecorder.class)
public class MixinScreenshotRecorder {
    private static final Random RANDOM = new Random();

    // Перехватываем метод takeScreenshot, который возвращает NativeImage
    @Inject(method = "takeScreenshot", at = @At("RETURN"))
    private static void onTakeScreenshot(Framebuffer framebuffer, CallbackInfoReturnable<NativeImage> cir) {
        NativeImage image = cir.getReturnValue();

        if (image != null && ParanoiaManager.getParanoia() > ParanoiaSettings.MIN_PARANOIA_FOR_CURSED_F2) {
            if (RANDOM.nextDouble() < ParanoiaSettings.CURSED_F2_CHANCE) {
                corruptImage(image);
            }
        }
    }

    private static void corruptImage(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // 1. Добавление крипового темного силуэта сбоку экрана (в пикселях)
        int startX = (int) (width * 0.8); // 80% ширины
        int startY = (int) (height * 0.4); // 40% высоты
        int endX = startX + (int) (width * 0.05);
        int endY = startY + (int) (height * 0.4);

        // Границы матрицы для безопасности
        startX = Math.max(0, startX);
        startY = Math.max(0, startY);
        endX = Math.min(width, endX);
        endY = Math.min(height, endY);

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                // Рисуем грубый силуэт (пропуск пикселей для "призрачности")
                if (RANDOM.nextFloat() < 0.8f) {
                    // Формат NativeImage: ABGR (Alpha, Blue, Green, Red)
                    // Делаем пиксели почти черными с небольшим шумом
                    int noise = RANDOM.nextInt(20);
                    int color = (255 << 24) | (noise << 16) | (noise << 8) | noise;
                    image.setColor(x, y, color);
                }
            }
        }

        // 2. Легкий цифровой шум (Zalgo-эффект на самом скриншоте)
        for (int i = 0; i < 500; i++) {
            int rx = RANDOM.nextInt(width);
            int ry = RANDOM.nextInt(height);
            image.setColor(rx, ry, 0xFF000000); // Абсолютно черные точки (Dead pixels)
        }
    }
}
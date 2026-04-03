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

    @Inject(method = "takeScreenshot", at = @At("RETURN"))
    private static void onTakeScreenshot(Framebuffer framebuffer, CallbackInfoReturnable<NativeImage> cir) {
        NativeImage image = cir.getReturnValue();
        if (image != null && ParanoiaManager.getParanoia() > ParanoiaSettings.MIN_PARANOIA_FOR_CURSED_F2) {
            if (RANDOM.nextDouble() < ParanoiaSettings.CURSED_F2_CHANCE) {
                corruptImageAggressively(image);
            }
        }
    }

    private static void corruptImageAggressively(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // 1. Рисуем черные горизонтальные "рваные" полосы (VHS эффект)
        int numBands = 5 + RANDOM.nextInt(10);
        for (int b = 0; b < numBands; b++) {
            int bandY = RANDOM.nextInt(height);
            int bandHeight = 5 + RANDOM.nextInt(30);

            for (int y = bandY; y < Math.min(height, bandY + bandHeight); y++) {
                for (int x = 0; x < width; x++) {
                    if (RANDOM.nextFloat() < 0.9f) {
                        image.setColor(x, y, 0xFF000000); // Полностью черный
                    }
                }
            }
        }

        // 2. Огромный черный силуэт по центру
        int startX = (int) (width * 0.4);
        int startY = (int) (height * 0.2);
        int endX = startX + (int) (width * 0.2);
        int endY = startY + (int) (height * 0.6);

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                // Плотность силуэта больше к центру
                if (RANDOM.nextFloat() < 0.95f) {
                    image.setColor(x, y, 0xFF050505);
                }
            }
        }
    }
}
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
                // Если сработало, жестоко ломаем пиксели
                int w = image.getWidth();
                int h = image.getHeight();

                // Рисуем огромные красные "глаза" и битые черно-красные полосы прямо поверх матрицы
                for (int y = 0; y < h; y += 10) {
                    for (int x = 0; x < w; x++) {
                        if (RANDOM.nextFloat() < 0.3f) {
                            image.setColor(x, y, 0xFF000000); // Черный шум
                        }
                    }
                }

                // Жуткий силуэт по центру
                int sx = (int)(w * 0.4);
                int sy = (int)(h * 0.2);
                for(int y = sy; y < sy + (h/2); y++) {
                    for(int x = sx; x < sx + (w/4); x++) {
                        image.setColor(x, y, 0xFF050505);
                    }
                }
            }
        }
    }
}
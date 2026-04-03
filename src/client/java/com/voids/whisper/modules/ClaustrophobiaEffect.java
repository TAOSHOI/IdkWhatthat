package com.voids.whisper.modules;

import com.voids.whisper.config.ParanoiaSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class ClaustrophobiaEffect {
    private static int undergroundTicks = 0;
    private static float currentVignetteIntensity = 0.0f;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Если игрок ниже уровня моря и над ним блоки (грубая проверка)
            if (client.player.getY() < 30 && !client.world.isSkyVisible(client.player.getBlockPos())) {
                undergroundTicks++;
            } else {
                undergroundTicks = Math.max(0, undergroundTicks - 5); // Быстро отпускает на поверхности
            }

            // Плавный расчет интенсивности виньетки
            if (undergroundTicks > ParanoiaSettings.TICKS_UNTIL_CLAUSTROPHOBIA) {
                currentVignetteIntensity = Math.min(ParanoiaSettings.MAX_VIGNETTE_OPACITY,
                        currentVignetteIntensity + 0.001f);
            } else {
                currentVignetteIntensity = Math.max(0.0f, currentVignetteIntensity - 0.005f);
            }
        });
    }

    public static float getVignetteIntensity() {
        return currentVignetteIntensity;
    }
}
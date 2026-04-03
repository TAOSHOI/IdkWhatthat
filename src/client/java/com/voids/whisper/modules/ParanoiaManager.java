package com.voids.whisper.modules;

import com.voids.whisper.config.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class ParanoiaManager {
    public static final float MAX_PARANOIA = 100.0f;

    // Отсутствие магических чисел
    private static final float PARANOIA_BASE_RATE = 0.001f;
    private static final float PARANOIA_DARKNESS_RATE = 0.005f;
    private static final float PARANOIA_UNDERGROUND_RATE = 0.002f;
    private static final int UNDERGROUND_THRESHOLD_Y = 0;
    private static final int DARKNESS_THRESHOLD = 5;
    private static final int SAVE_INTERVAL_TICKS = 1200; // 1 минута при 20 TPS

    private static float currentParanoia = 0.0f;

    // Zero-allocation: переиспользуем объект вместо new BlockPos() каждый кадр
    private static final BlockPos.Mutable MUTABLE_POS = new BlockPos.Mutable();
    private static int tickCounter = 0;

    // Добавь это в класс ParanoiaManager
    public static void setParanoia(float value) {
        currentParanoia = Math.clamp(value, 0.0f, MAX_PARANOIA);
    }

    public static void init() {
        currentParanoia = ConfigManager.config.savedParanoiaLevel;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!ConfigManager.config.isModuleEnabled || client.player == null || client.world == null || client.isPaused()) {
                return;
            }

            updateParanoia(client);

            // Асинхронное/периодическое сохранение
            if (++tickCounter % SAVE_INTERVAL_TICKS == 0) {
                ConfigManager.config.savedParanoiaLevel = currentParanoia;
                ConfigManager.save();
            }
        });
    }

    private static void updateParanoia(MinecraftClient client) {
        float delta = PARANOIA_BASE_RATE;

        // Заполняем MutablePos без создания новых объектов
        MUTABLE_POS.set(client.player.getX(), client.player.getY(), client.player.getZ());

        if (MUTABLE_POS.getY() < UNDERGROUND_THRESHOLD_Y) {
            delta += PARANOIA_UNDERGROUND_RATE;
        }

        if (client.world.getLightLevel(MUTABLE_POS) < DARKNESS_THRESHOLD) {
            delta += PARANOIA_DARKNESS_RATE;
        }

        currentParanoia = Math.min(MAX_PARANOIA, currentParanoia + delta);
    }

    public static float getParanoia() {
        return currentParanoia;
    }
}
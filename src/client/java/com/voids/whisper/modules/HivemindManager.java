package com.voids.whisper.modules;

import com.voids.whisper.config.ParanoiaSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class HivemindManager {
    public static int hivemindTicks = 0;
    private static final Random RANDOM = new Random();

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (hivemindTicks > 0) hivemindTicks--;

            if (ParanoiaManager.getParanoia() > ParanoiaSettings.MIN_PARANOIA_FOR_UI && hivemindTicks == 0) {
                if (RANDOM.nextDouble() < ParanoiaSettings.HIVEMIND_CHANCE) {
                    hivemindTicks = 80; // Мобы пялятся на вас 4 секунды
                }
            }
        });
    }
}
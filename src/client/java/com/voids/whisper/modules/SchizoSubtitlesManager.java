package com.voids.whisper.modules;

import com.voids.whisper.config.ParanoiaSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class SchizoSubtitlesManager {
    public static String currentFakeSubtitle = null;
    public static int subtitleTicks = 0;
    private static final Random RANDOM = new Random();

    private static final List<String> CREEPY_SUBS = List.of(
            "Кто-то дышит", "Шаги сзади", "Тихий стон", "Оно смотрит", "Шёпот...", "Звук ломающихся костей"
    );

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (subtitleTicks > 0) {
                subtitleTicks--;
                if (subtitleTicks == 0) currentFakeSubtitle = null;
            }

            if (ParanoiaManager.getParanoia() > ParanoiaSettings.MIN_PARANOIA_FOR_UI && subtitleTicks == 0) {
                if (RANDOM.nextDouble() < ParanoiaSettings.SCHIZO_SUBTITLES_CHANCE) {
                    currentFakeSubtitle = CREEPY_SUBS.get(RANDOM.nextInt(CREEPY_SUBS.size()));
                    subtitleTicks = 60; // Висит 3 секунды
                }
            }
        });
    }
}
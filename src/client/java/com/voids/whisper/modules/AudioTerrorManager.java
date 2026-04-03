package com.voids.whisper.modules;

import com.voids.whisper.config.ParanoiaSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class AudioTerrorManager {
    private static int soundCooldown = 0;
    private static final Random RANDOM = new Random();

    // Легко расширяемый список звуков для газлайтинга
    private static final List<SoundEvent> CREEPY_SOUNDS = List.of(
            SoundEvents.BLOCK_GRAVEL_STEP,
            SoundEvents.BLOCK_WOOD_BREAK,
            SoundEvents.ENTITY_PLAYER_HURT,
            SoundEvents.ENTITY_CREEPER_PRIMED
    );

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            if (soundCooldown > 0) {
                soundCooldown--;
                return;
            }

            // В методе init() класса AudioTerrorManager
            if (ParanoiaManager.getParanoia() >= ParanoiaSettings.MIN_PARANOIA_FOR_AUDIO) {
                float p = ParanoiaManager.getParanoia() / 100.0f; // Коэффициент 0.0 - 1.0

                // Чем выше паранойя, тем меньше шанс "промахнуться" мимо звука
                if (RANDOM.nextFloat() < (0.01f + (p * 0.05f))) {
                    playPhantomSound(client);

                    // Кулдаун теперь уменьшается: на 100% он будет всего ~15 секунд
                    int baseCooldown = ParanoiaSettings.PHANTOM_SOUND_COOLDOWN_TICKS;
                    soundCooldown = (int)(baseCooldown * (1.1f - p)) + RANDOM.nextInt(200);
                }
            }
        });
    }

    private static void playPhantomSound(MinecraftClient client) {
        SoundEvent sound = CREEPY_SOUNDS.get(RANDOM.nextInt(CREEPY_SOUNDS.size()));

        // Zero-Allocation математика: используем текущие векторы игрока
        Vec3d lookVec = client.player.getRotationVec(1.0f).multiply(-2.0); // Вектор за спину
        double x = client.player.getX() + lookVec.x;
        double y = client.player.getY();
        double z = client.player.getZ() + lookVec.z;

        client.getSoundManager().play(
                new PositionedSoundInstance(
                        sound, SoundCategory.AMBIENT,
                        0.8f, // Громкость
                        0.9f + (RANDOM.nextFloat() * 0.2f), // Плавающий Pitch
                        client.world.getRandom(),
                        x, y, z
                )
        );
    }
}
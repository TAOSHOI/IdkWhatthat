package com.voids.whisper.modules;

import com.voids.whisper.config.ParanoiaSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class EchoStepsManager {
    private static int movingTicks = 0;
    private static int triggerEchoTimer = -1;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || ParanoiaManager.getParanoia() < ParanoiaSettings.MIN_PARANOIA_FOR_AUDIO) return;

            double speed = client.player.getVelocity().lengthSquared();
            if (speed > 0.01 && client.player.isOnGround()) {
                movingTicks++;
            } else {
                if (movingTicks > 40 && triggerEchoTimer == -1) {
                    triggerEchoTimer = 10; // Ждем полсекунды после остановки
                }
                movingTicks = 0;
            }

            if (triggerEchoTimer > 0) {
                triggerEchoTimer--;
                if (triggerEchoTimer == 0) {
                    // Звук шага прямо за спиной
                    Vec3d behind = client.player.getPos().subtract(client.player.getRotationVec(1.0f));
                    client.world.playSound(client.player, behind.x, behind.y, behind.z,
                            SoundEvents.BLOCK_STONE_STEP, SoundCategory.PLAYERS, 1.0f, 0.8f);
                    triggerEchoTimer = -1;
                }
            }
        });
    }
}
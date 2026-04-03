package com.voids.whisper.modules;

import com.voids.whisper.config.ParanoiaSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class PeripheralPhantomManager {
    private static Vec3d phantomPos = null;
    private static final Random RANDOM = new Random();
    private static int phantomCooldown = 0;

    // Переменные для механики взгляда
    public static int stareTicks = 0;
    public static int screamerTicks = 0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (screamerTicks > 0) screamerTicks--;

            if (phantomCooldown > 0) {
                phantomCooldown--;
                return;
            }

            if (phantomPos == null && ParanoiaManager.getParanoia() >= ParanoiaSettings.MIN_PARANOIA_FOR_PHANTOMS) {
                if (RANDOM.nextDouble() < ParanoiaSettings.PHANTOM_SPAWN_CHANCE) {
                    spawnPhantom(client);
                }
            } else if (phantomPos != null) {
                Vec3d lookVec = client.player.getRotationVec(1.0f).normalize();
                Vec3d dirToPhantom = phantomPos.subtract(client.player.getPos()).normalize();
                double dotProduct = lookVec.dotProduct(dirToPhantom);

                // Игрок смотрит прямо на тень
                if (dotProduct > ParanoiaSettings.PHANTOM_LOOK_DOT_THRESHOLD) {
                    stareTicks++;
                    if (stareTicks % 10 == 0) {
                        // Нагнетающий звук сердцебиения
                        client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM.value(), 1.0f + (stareTicks * 0.05f), 0.5f);
                    }
                    if (stareTicks > 60) { // 3 секунды непрерывного взгляда -> Скример
                        triggerScreamer(client);
                        phantomPos = null;
                        phantomCooldown = 1200;
                    }
                } else if (stareTicks > 0 && dotProduct < 0.5) {
                    // Игрок отвернулся после того как начал смотреть — тень пропадает
                    client.world.playSound(client.player, phantomPos.x, phantomPos.y, phantomPos.z, SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.AMBIENT, 1.5f, 0.5f);
                    phantomPos = null;
                    stareTicks = 0;
                    phantomCooldown = 600;
                }
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (phantomPos == null) return;
            MinecraftClient client = MinecraftClient.getInstance();
            Vec3d cameraPos = context.camera().getPos();

            context.matrixStack().push();
            context.matrixStack().translate(phantomPos.x - cameraPos.x, phantomPos.y - cameraPos.y, phantomPos.z - cameraPos.z);
            context.matrixStack().multiply(context.camera().getRotation());

            VertexConsumer consumer = client.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.getWaterMask());
            Matrix4f positionMatrix = context.matrixStack().peek().getPositionMatrix();

            float halfWidth = 0.3f; float height = 2.0f;
            consumer.vertex(positionMatrix, -halfWidth, 0, 0).color(0, 0, 0, 220);
            consumer.vertex(positionMatrix, halfWidth, 0, 0).color(0, 0, 0, 220);
            consumer.vertex(positionMatrix, halfWidth, height, 0).color(0, 0, 0, 220);
            consumer.vertex(positionMatrix, -halfWidth, height, 0).color(0, 0, 0, 220);
            context.matrixStack().pop();
        });
    }

    private static void spawnPhantom(MinecraftClient client) {
        Vec3d lookVec = client.player.getRotationVec(1.0f);
        // Пускаем луч под углом 45 градусов (периферия)
        double angle = (RANDOM.nextBoolean() ? 1 : -1) * (Math.PI / 4.0);
        double dirX = lookVec.x * Math.cos(angle) - lookVec.z * Math.sin(angle);
        double dirZ = lookVec.x * Math.sin(angle) + lookVec.z * Math.cos(angle);
        Vec3d phantomDir = new Vec3d(dirX, 0, dirZ).normalize();

        // Raycast чтобы тень стояла на полу или у стены, а не летала за стеной
        Vec3d eyePos = client.player.getEyePos();
        BlockHitResult hit = client.world.raycast(new RaycastContext(eyePos, eyePos.add(phantomDir.multiply(20)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player));

        if (hit.getType() == HitResult.Type.BLOCK) {
            phantomPos = hit.getPos().add(0, 0.1, 0); // Чуть выше пола
            stareTicks = 0;
        }
    }

    private static void triggerScreamer(MinecraftClient client) {
        screamerTicks = 20; // Вспышка на 1 сек
        client.player.playSound(SoundEvents.ENTITY_ENDERMAN_STARE, 2.0f, 0.5f);
        stareTicks = 0;
    }
}
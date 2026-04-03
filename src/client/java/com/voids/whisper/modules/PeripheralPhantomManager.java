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
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class PeripheralPhantomManager {
    private static Vec3d phantomPos = null;
    private static final Random RANDOM = new Random();
    private static int phantomCooldown = 0;


    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (phantomCooldown > 0) {
                phantomCooldown--;
                return;
            }

            // В методе init() класса PeripheralPhantomManager
            if (phantomPos == null && ParanoiaManager.getParanoia() >= ParanoiaSettings.MIN_PARANOIA_FOR_PHANTOMS) {
                float p = ParanoiaManager.getParanoia() / 100.0f;
                // На 100% шанс спавна 5% каждый тик — он появится почти мгновенно
                if (RANDOM.nextFloat() < (ParanoiaSettings.PHANTOM_SPAWN_CHANCE * p)) {
                    spawnPhantom(client);
                }
            } else if (phantomPos != null) {
                // Проверка взгляда (Dot Product) - Zero Allocation
                Vec3d lookVec = client.player.getRotationVec(1.0f).normalize();
                Vec3d dirToPhantom = phantomPos.subtract(client.player.getPos()).normalize();

                double dotProduct = lookVec.dotProduct(dirToPhantom);

                // Если игрок повернул камеру к фантому (угол зрения близок к центру)
                if (dotProduct > ParanoiaSettings.PHANTOM_LOOK_DOT_THRESHOLD) {
                    // Пугающее исчезновение
                    client.world.playSound(client.player,
                            phantomPos.x, phantomPos.y, phantomPos.z,
                            SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.AMBIENT,
                            1.5f, 0.5f); // Низкий питч осыпающегося гравия

                    phantomPos = null;
                    phantomCooldown = 2000 + RANDOM.nextInt(2000);
                }
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (phantomPos == null) return;

            // Рендер простого высокого черного прямоугольника (силуэт) с учетом тумана
            MinecraftClient client = MinecraftClient.getInstance();
            Vec3d cameraPos = context.camera().getPos();

            context.matrixStack().push();
            context.matrixStack().translate(
                    phantomPos.x - cameraPos.x,
                    phantomPos.y - cameraPos.y,
                    phantomPos.z - cameraPos.z
            );

            // Чтобы силуэт всегда смотрел на игрока (Billboard эффект)
            context.matrixStack().multiply(context.camera().getRotation());

            VertexConsumer consumer = client.getBufferBuilders().getEntityVertexConsumers()
                    .getBuffer(RenderLayer.getWaterMask()); // Черный полупрозрачный слой

            Matrix4f positionMatrix = context.matrixStack().peek().getPositionMatrix();

            // Отрисовка геометрии (2 блока в высоту, 0.6 в ширину)
            float halfWidth = 0.3f;
            float height = 2.0f;

            consumer.vertex(positionMatrix, -halfWidth, 0, 0).color(0, 0, 0, 200);
            consumer.vertex(positionMatrix, halfWidth, 0, 0).color(0, 0, 0, 200);
            consumer.vertex(positionMatrix, halfWidth, height, 0).color(0, 0, 0, 200);
            consumer.vertex(positionMatrix, -halfWidth, height, 0).color(0, 0, 0, 200);

            context.matrixStack().pop();
        });
    }

    private static void spawnPhantom(MinecraftClient client) {
        // Спавн на границе прорисовки, но сбоку от текущего взгляда игрока
        Vec3d lookVec = client.player.getRotationVec(1.0f);
        // Поворот вектора взгляда на ~60 градусов (периферия)
        double angle = (RANDOM.nextBoolean() ? 1 : -1) * (Math.PI / 3.0);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double dirX = lookVec.x * cos - lookVec.z * sin;
        double dirZ = lookVec.x * sin + lookVec.z * cos;

        double distance = Math.max(16.0, client.options.getClampedViewDistance() * 16.0 - 16.0);

        phantomPos = client.player.getPos().add(dirX * distance, 0, dirZ * distance);
    }
}
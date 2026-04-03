package com.voids.whisper.modules;

import com.voids.whisper.config.ParanoiaSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class MirageOresManager {
    private static final List<BlockPos> activeMirages = new ArrayList<>();
    private static final Random RANDOM = new Random();
    private static final BlockState FAKE_ORE_STATE = Blocks.DIAMOND_ORE.getDefaultState();

    private static int collectedFakeDiamonds = 0;
    public static int screamerTicks = 0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            if (screamerTicks > 0) screamerTicks--;

            if (ParanoiaManager.getParanoia() >= ParanoiaSettings.MIN_PARANOIA_FOR_MIRAGES) {
                if (RANDOM.nextDouble() < ParanoiaSettings.MIRAGE_SPAWN_CHANCE && activeMirages.size() < 2) {
                    findAndAddMirage(client);
                }
            }

            Vec3d playerPos = client.player.getPos();
            Iterator<BlockPos> iterator = activeMirages.iterator();
            while (iterator.hasNext()) {
                BlockPos pos = iterator.next();
                double distSq = pos.getSquaredDistance(playerPos);

                if (distSq < ParanoiaSettings.MIRAGE_DISAPPEAR_DIST_SQ) {
                    iterator.remove();
                    collectedFakeDiamonds++;

                    // Скример жадности
                    if (collectedFakeDiamonds >= 5) {
                        triggerScreamer(client);
                        collectedFakeDiamonds = 0;
                    } else {
                        client.world.playSound(client.player, pos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.AMBIENT, 0.5f, 0.5f);
                    }
                } else if (distSq > ParanoiaSettings.MIRAGE_MAX_DIST_SQ) {
                    iterator.remove();
                }
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (activeMirages.isEmpty()) return;
            MinecraftClient client = MinecraftClient.getInstance();
            VertexConsumerProvider.Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();
            Vec3d cameraPos = context.camera().getPos();

            context.matrixStack().push();
            for (BlockPos pos : activeMirages) {
                context.matrixStack().push();
                context.matrixStack().translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
                client.getBlockRenderManager().renderBlockAsEntity(FAKE_ORE_STATE, context.matrixStack(), consumers, 15728880, OverlayTexture.DEFAULT_UV);
                context.matrixStack().pop();
            }
            context.matrixStack().pop();
        });
    }

    private static void findAndAddMirage(MinecraftClient client) {
        int range = 15;
        BlockPos pPos = client.player.getBlockPos();
        BlockPos target = pPos.add(RANDOM.nextInt(range * 2) - range, RANDOM.nextInt(range) - (range / 2), RANDOM.nextInt(range * 2) - range);

        BlockState state = client.world.getBlockState(target);
        if (state.isOf(Blocks.STONE) || state.isOf(Blocks.DEEPSLATE)) {
            // Проверка видимости: пускаем луч от глаз игрока к блоку. Если попали в него — значит блок на поверхности.
            BlockHitResult hit = client.world.raycast(new RaycastContext(
                    client.player.getEyePos(), target.toCenterPos(),
                    RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, client.player));

            if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(target)) {
                activeMirages.add(target.toImmutable());
            }
        }
    }

    private static void triggerScreamer(MinecraftClient client) {
        screamerTicks = 40; // 2 секунды скримера
        client.player.playSound(SoundEvents.ENTITY_GHAST_SCREAM, 2.0f, 0.5f);
    }
}
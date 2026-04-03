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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class MirageOresManager {
    private static final List<BlockPos> activeMirages = new ArrayList<>();
    private static final Random RANDOM = new Random();

    private static final BlockState FAKE_ORE_STATE = Blocks.DIAMOND_ORE.getDefaultState();

    private static int ignoredMiragesCount = 0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null || client.isPaused()) return;

            // В методе init() внутри ClientTickEvents
            if (ParanoiaManager.getParanoia() >= ParanoiaSettings.MIN_PARANOIA_FOR_MIRAGES) {
                float p = ParanoiaManager.getParanoia() / 100.0f;
                // До 10 миражей одновременно на 100% паранойи
                if (RANDOM.nextDouble() < (ParanoiaSettings.MIRAGE_SPAWN_CHANCE * p) && activeMirages.size() < 10) {
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
                } else if (distSq > ParanoiaSettings.MIRAGE_MAX_DIST_SQ + 400) {
                    iterator.remove();
                    ignoredMiragesCount++;
                    checkInsightSystem(client);
                }
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (activeMirages.isEmpty()) return;

            MinecraftClient client = MinecraftClient.getInstance();
            VertexConsumerProvider.Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();
            Vec3d cameraPos = context.camera().getPos();

            context.matrixStack().push();

            for (int i = 0; i < activeMirages.size(); i++) {
                BlockPos pos = activeMirages.get(i);

                context.matrixStack().push();
                context.matrixStack().translate(
                        pos.getX() - cameraPos.x,
                        pos.getY() - cameraPos.y,
                        pos.getZ() - cameraPos.z
                );

                client.getBlockRenderManager().renderBlockAsEntity(
                        FAKE_ORE_STATE,
                        context.matrixStack(),
                        consumers,
                        15728880,
                        OverlayTexture.DEFAULT_UV
                );

                context.matrixStack().pop();
            }
            context.matrixStack().pop();
        });
    }

    private static void findAndAddMirage(MinecraftClient client) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        BlockPos pPos = client.player.getBlockPos();

        int range = 20;
        int rx = pPos.getX() + RANDOM.nextInt(range * 2) - range;
        int ry = pPos.getY() + RANDOM.nextInt(range) - (range / 2);
        int rz = pPos.getZ() + RANDOM.nextInt(range * 2) - range;

        mutable.set(rx, ry, rz);
        BlockState targetState = client.world.getBlockState(mutable);

        if ((targetState.isOf(Blocks.STONE) || targetState.isOf(Blocks.DEEPSLATE))
                && mutable.getSquaredDistance(pPos) > ParanoiaSettings.MIRAGE_DISAPPEAR_DIST_SQ) {
            activeMirages.add(mutable.toImmutable());
        }
    }

    private static void checkInsightSystem(MinecraftClient client) {
        if (ignoredMiragesCount >= ParanoiaSettings.IGNORED_MIRAGES_FOR_INSIGHT) {
            if (RANDOM.nextDouble() < ParanoiaSettings.INSIGHT_CHANCE) {
                client.getSoundManager().play(
                        new net.minecraft.client.sound.PositionedSoundInstance(
                                net.minecraft.sound.SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE,
                                net.minecraft.sound.SoundCategory.AMBIENT,
                                1.0f, 0.5f, client.world.getRandom(),
                                client.player.getX(), client.player.getY(), client.player.getZ()
                        )
                );
            }
            ignoredMiragesCount = 0;
        }
    }
}
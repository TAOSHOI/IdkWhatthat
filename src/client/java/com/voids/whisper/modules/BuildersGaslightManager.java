package com.voids.whisper.modules;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class BuildersGaslightManager {
    // Храним оригинальные стейты только недавно поставленных блоков
    private static final Map<BlockPos, BlockState> recentBlocks = new HashMap<>();
    // Газлайтнутые блоки (текущие подмены)
    private static final Map<BlockPos, BlockState> gaslightedBlocks = new HashMap<>();

    private static final Random RANDOM = new Random();

    public static void init() {
        // Имитация перехвата установки блока (через проверку изменения инвентаря/луча можно сделать сложнее,
        // но для газлайтинга достаточно отслеживать использование предметов).
        // Ради оптимизации, перехват лучше делать через Mixin в ClientPlayerInteractionManager,
        // но для инкапсуляции мы отслеживаем ПКМ и BlockHitResult.

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // 1. Возврат блока в норму, если на него навели прицел
            if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                BlockPos targetPos = ((BlockHitResult) client.crosshairTarget).getBlockPos();
                if (gaslightedBlocks.containsKey(targetPos)) {
                    // Возвращаем оригинальный визуальный стейт (только на клиенте!)
                    client.world.setBlockState(targetPos, recentBlocks.get(targetPos), 3);
                    gaslightedBlocks.remove(targetPos);
                    recentBlocks.remove(targetPos);
                    // Легкий звук, чтобы игрок начал сомневаться
                    client.getSoundManager().play(
                            new net.minecraft.client.sound.PositionedSoundInstance(
                                    net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK.value(),
                                    net.minecraft.sound.SoundCategory.MASTER,
                                    0.1f, 0.5f, client.world.getRandom(),
                                    targetPos.getX(), targetPos.getY(), targetPos.getZ()
                            )
                    );
                }
            }

            // 2. Медленный газлайтинг оставленных блоков вне видимости
            if (client.player.age % 100 == 0 && !recentBlocks.isEmpty()) {
                recentBlocks.forEach((pos, originalState) -> {
                    if (!gaslightedBlocks.containsKey(pos)) {
                        // Если игрок отошел и отвернулся
                        if (client.player.getBlockPos().getSquaredDistance(pos) > 25.0) {
                            // Подменяем на что-то похожее (например, если булыжник, то замшелый)
                            BlockState fakeState = getCreepyAlternative(originalState);
                            if (fakeState != originalState && RANDOM.nextFloat() < 0.1f) {
                                client.world.setBlockState(pos, fakeState, 3); // 3 = обновить только клиентский рендер
                                gaslightedBlocks.put(pos, fakeState);
                            }
                        }
                    }
                });
            }
        });
    }

    // Вызывается из MixinClientPlayerInteractionManager при успешном placeBlock
    public static void onBlockPlaced(BlockPos pos, BlockState state) {
        if (ParanoiaManager.getParanoia() > 20.0f) {
            recentBlocks.put(pos, state);
            // Храним не больше 10 блоков, чтобы не утекала память
            if (recentBlocks.size() > 10) {
                recentBlocks.keySet().iterator().remove();
            }
        }
    }

    private static BlockState getCreepyAlternative(BlockState original) {
        // Простая подмена: булыжник -> замшелый, дуб -> ель и т.д.
        if (original.isOf(Blocks.COBBLESTONE)) return Blocks.MOSSY_COBBLESTONE.getDefaultState();
        if (original.isOf(Blocks.OAK_PLANKS)) return Blocks.SPRUCE_PLANKS.getDefaultState();
        if (original.isOf(Blocks.TORCH)) return Blocks.REDSTONE_TORCH.getDefaultState();

        return original; // Возвращаем оригинал, если подмена не найдена
    }
}
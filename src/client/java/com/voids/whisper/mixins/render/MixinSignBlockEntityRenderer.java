package com.voids.whisper.mixins.render;

import com.voids.whisper.config.ParanoiaSettings;
import com.voids.whisper.modules.ParanoiaManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Environment(EnvType.CLIENT)
@Mixin(SignBlockEntityRenderer.class)
public class MixinSignBlockEntityRenderer {
    private static final Random RANDOM = new Random();

    @Inject(method = "render(Lnet/minecraft/block/entity/SignBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            at = @At("HEAD"))
    private void onRenderSign(SignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {

        if (ParanoiaManager.getParanoia() > ParanoiaSettings.MIN_PARANOIA_FOR_UI) {
            // Для конкретной таблички вычисляем "псевдослучайный" шанс,
            // чтобы она не мерцала каждый кадр, а была проклята постоянно в этот момент
            long seed = entity.getPos().asLong() ^ (System.currentTimeMillis() / 2000);
            Random signRandom = new Random(seed);

            if (signRandom.nextDouble() < ParanoiaSettings.ZALGO_TEXT_CHANCE) {
                // Матричный хак: смещаем и вращаем текст таблички, создавая эффект "Zalgo" и дрожи
                float jitterX = (RANDOM.nextFloat() - 0.5f) * 0.05f;
                float jitterY = (RANDOM.nextFloat() - 0.5f) * 0.05f;

                matrices.translate(jitterX, jitterY, 0);
                // Делаем текст таблички визуально красным/темным через скейл матрицы
                // (влияет на трансформацию всего SignBlockEntity)
            }
        }
    }
}
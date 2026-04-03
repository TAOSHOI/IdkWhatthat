package com.voids.whisper.mixins.render;

import com.voids.whisper.modules.HivemindManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void onRenderHivemind(LivingEntity livingEntity, float f, float g, net.minecraft.client.util.math.MatrixStack matrixStack, net.minecraft.client.render.VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (HivemindManager.hivemindTicks > 0 && livingEntity instanceof AnimalEntity) {
            // Визуально разворачиваем всех мирных животных лицом к игроку
            Vec3d playerPos = MinecraftClient.getInstance().player.getPos();
            Vec3d entityPos = livingEntity.getPos();
            double dx = playerPos.x - entityPos.x;
            double dz = playerPos.z - entityPos.z;
            float targetYaw = (float)(Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;

            livingEntity.bodyYaw = targetYaw;
            livingEntity.headYaw = targetYaw;
            livingEntity.setPitch(0.0f);
        }
    }
}
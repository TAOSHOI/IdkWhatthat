package com.voids.whisper.mixins.render;

import com.voids.whisper.modules.BuildersGaslightManager;
import com.voids.whisper.modules.UIParanoiaManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void onBlockInteract(MinecraftClient client, net.minecraft.client.network.ClientPlayerEntity player, net.minecraft.util.Hand hand, net.minecraft.util.hit.BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue().isAccepted() && client.world != null) {
            net.minecraft.util.math.BlockPos placedPos = hitResult.getBlockPos().offset(hitResult.getSide());
            net.minecraft.block.BlockState state = client.world.getBlockState(placedPos);
            BuildersGaslightManager.onBlockPlaced(placedPos, state);
        }
    }

    @Inject(method = "attackBlock", at = @At("HEAD"))
    private void onAttackBlock(net.minecraft.util.math.BlockPos pos, net.minecraft.util.math.Direction direction, CallbackInfoReturnable<Boolean> cir) {
        UIParanoiaManager.triggerToolBreak(MinecraftClient.getInstance());
    }

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void onAttackEntity(net.minecraft.entity.player.PlayerEntity player, net.minecraft.entity.Entity target, CallbackInfo ci) {
        UIParanoiaManager.triggerToolBreak(MinecraftClient.getInstance());
    }
}
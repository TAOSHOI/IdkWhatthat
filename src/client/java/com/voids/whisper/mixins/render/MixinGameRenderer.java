package com.voids.whisper.mixins.render;

import com.voids.whisper.modules.PeripheralPhantomManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    // Снижаем FOV (приближаем камеру), если игрок пялится на фантома
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void modifyFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        if (PeripheralPhantomManager.stareTicks > 0) {
            double originalFov = cir.getReturnValue();
            // Чем дольше смотрит (stareTicks), тем сильнее зум
            double zoomFactor = Math.min(0.5, PeripheralPhantomManager.stareTicks / 100.0);
            cir.setReturnValue(originalFov * (1.0 - zoomFactor));
        }
    }
}
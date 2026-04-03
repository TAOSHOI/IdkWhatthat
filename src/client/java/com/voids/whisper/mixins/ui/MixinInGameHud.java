package com.voids.whisper.mixins.ui;

import com.voids.whisper.modules.ClaustrophobiaEffect;
import com.voids.whisper.modules.UIParanoiaManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Base64;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public class MixinInGameHud {

    private static final String FAKE_DEATH_TEXT = new String(Base64.getDecoder().decode("WW91IGRpZWQh"));

    @ModifyVariable(method = "renderVignetteOverlay", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float modifyVignetteOpacity(float originalOpacity) {
        float claustrophobia = ClaustrophobiaEffect.getVignetteIntensity();
        if (claustrophobia > 0.0f) {
            return Math.min(1.0f, originalOpacity + claustrophobia);
        }
        return originalOpacity;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (UIParanoiaManager.fakeDeathTimer > 0) {
            int width = context.getScaledWindowWidth();
            int height = context.getScaledWindowHeight();

            context.fill(0, 0, width, height, 0x80FF0000);

            context.getMatrices().push();
            context.getMatrices().scale(4.0F, 4.0F, 4.0F);
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(FAKE_DEATH_TEXT);
            context.drawTextWithShadow(
                    MinecraftClient.getInstance().textRenderer,
                    FAKE_DEATH_TEXT,
                    (width / 4) - (textWidth / 2),
                    (height / 4) - 10,
                    0xFFFFFF
            );
            context.getMatrices().pop();
            // Добавь это ВНУТРЬ метода onRenderTail в MixinInGameHud
            if (com.voids.whisper.modules.DebugControlManager.showDebugHud) {
                float p = com.voids.whisper.modules.ParanoiaManager.getParanoia();
                String debugText = String.format("Paranoia: %.2f", p);
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, debugText, 5, 5, 0xFFFFFF);
            }
        }
    }
}
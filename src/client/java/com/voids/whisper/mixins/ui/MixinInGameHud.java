package com.voids.whisper.mixins.ui;

import com.voids.whisper.modules.ClaustrophobiaEffect;
import com.voids.whisper.modules.MirageOresManager;
import com.voids.whisper.modules.PeripheralPhantomManager;
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
import java.util.Random;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public class MixinInGameHud {

    private static final String FAKE_DEATH_TEXT = new String(Base64.getDecoder().decode("WW91IGRpZWQh"));
    private static final Random RANDOM = new Random();

    @ModifyVariable(method = "renderVignetteOverlay", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float modifyVignetteOpacity(float originalOpacity) {
        float claustrophobia = ClaustrophobiaEffect.getVignetteIntensity();
        // Если игрок пялится на фантома, виньетка резко чернеет
        if (PeripheralPhantomManager.stareTicks > 0) {
            claustrophobia += (PeripheralPhantomManager.stareTicks / 60.0f);
        }
        if (claustrophobia > 0.0f) {
            return Math.min(1.0f, originalOpacity + claustrophobia);
        }
        return originalOpacity;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        // 1. Отрисовка Скримера (Жадность на алмазы или взгляд на тень)
        if (MirageOresManager.screamerTicks > 0 || PeripheralPhantomManager.screamerTicks > 0) {
            // Эффект жестких статических помех (Zalgo)
            for (int i = 0; i < 50; i++) {
                int rx = RANDOM.nextInt(width);
                int ry = RANDOM.nextInt(height);
                int rw = RANDOM.nextInt(100);
                int rh = RANDOM.nextInt(20);
                context.fill(rx, ry, rx + rw, ry + rh, RANDOM.nextBoolean() ? 0x99000000 : 0x99FF0000);
            }
            context.fill(0, 0, width, height, 0x60000000); // Темная пелена
        }

        // 2. Фейковая смерть
        if (UIParanoiaManager.fakeDeathTimer > 0) {
            context.fill(0, 0, width, height, 0x80FF0000);
            context.getMatrices().push();
            context.getMatrices().scale(4.0F, 4.0F, 4.0F);
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(FAKE_DEATH_TEXT);
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, FAKE_DEATH_TEXT, (width / 4) - (textWidth / 2), (height / 4) - 10, 0xFFFFFF);
            context.getMatrices().pop();
        }

        // 3. Дебаг-меню
        if (com.voids.whisper.modules.DebugControlManager.showDebugHud) {
            String debugText = String.format("Paranoia: %.2f", com.voids.whisper.modules.ParanoiaManager.getParanoia());
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, debugText, 5, 5, 0xFFFFFF);
        }
    }
}
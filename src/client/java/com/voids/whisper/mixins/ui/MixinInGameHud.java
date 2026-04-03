package com.voids.whisper.mixins.ui;

import com.voids.whisper.modules.*;
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
        if (PeripheralPhantomManager.stareTicks > 0) claustrophobia += (PeripheralPhantomManager.stareTicks / 60.0f);
        if (claustrophobia > 0.0f) return Math.min(1.0f, originalOpacity + claustrophobia);
        return originalOpacity;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        // 1. Моргание реальности (Полная тьма)
        if (UIParanoiaManager.flickerTicks > 0) {
            context.fill(0, 0, width, height, 0xFF000000); // Pitch black
        }

        // 2. Скримеры
        if (MirageOresManager.screamerTicks > 0 || PeripheralPhantomManager.screamerTicks > 0) {
            for (int i = 0; i < 50; i++) {
                context.fill(RANDOM.nextInt(width), RANDOM.nextInt(height), RANDOM.nextInt(width), RANDOM.nextInt(height), RANDOM.nextBoolean() ? 0x99000000 : 0x99FF0000);
            }
            context.fill(0, 0, width, height, 0x60000000);
        }

        // 3. ФЕЙКОВЫЙ ЭКРАН СМЕРТИ (ИСПРАВЛЕНО!)
        if (UIParanoiaManager.fakeDeathTimer > 0) {
            context.fill(0, 0, width, height, 0x80FF0000);
            context.getMatrices().push();
            context.getMatrices().scale(4.0F, 4.0F, 4.0F); // Увеличиваем в 4 раза
            int scaledWidth = width / 4;
            int scaledHeight = height / 4;
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(FAKE_DEATH_TEXT);
            // Отрисовка ровно по центру с учетом масштаба
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, FAKE_DEATH_TEXT, (scaledWidth / 2) - (textWidth / 2), (scaledHeight / 2) - 5, 0xFFFFFF);
            context.getMatrices().pop();
        }

        // 4. ФЕЙКОВЫЕ СУБТИТРЫ (Schizophrenia)
        if (SchizoSubtitlesManager.currentFakeSubtitle != null) {
            String text = "> " + SchizoSubtitlesManager.currentFakeSubtitle;
            int textW = MinecraftClient.getInstance().textRenderer.getWidth(text);
            int startX = width - textW - 5;
            int startY = height - 30; // В правом нижнем углу
            context.fill(startX - 2, startY - 2, startX + textW + 2, startY + 10, 0xAA000000); // Фон субтитра
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, startX, startY, 0xFFFFFF);
        }

        // 5. Дебаг
        if (com.voids.whisper.modules.DebugControlManager.showDebugHud) {
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, String.format("Paranoia: %.2f", ParanoiaManager.getParanoia()), 5, 5, 0xFFFFFF);
        }
    }
}
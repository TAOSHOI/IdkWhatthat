package com.voids.whisper.mixins.ui;

import com.voids.whisper.config.ParanoiaSettings;
import com.voids.whisper.modules.ParanoiaManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Base64;
import java.util.Random;

@Environment(EnvType.CLIENT)
@Mixin(GameMenuScreen.class)
public class MixinGameMenuScreen {
    private static final Random RANDOM = new Random();
    // "Не оставляй меня."
    private static final String CURSED_DISCONNECT = new String(Base64.getDecoder().decode("0J3QtSDQvtGB0YLQsNCy0LvRj9C5INC80LXQvdGPLg=="));

    private float backgroundShift = 0.0f;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        GameMenuScreen screen = (GameMenuScreen) (Object) this;

        if (ParanoiaManager.getParanoia() > ParanoiaSettings.MIN_PARANOIA_FOR_UI && RANDOM.nextDouble() < 0.1) {
            for (net.minecraft.client.gui.Element element : screen.children()) {
                if (element instanceof ButtonWidget button) {
                    if (button.getY() > screen.height - 50) {
                        button.setMessage(Text.literal("§c" + CURSED_DISCONNECT));
                    }
                }
            }
        }
    }

    // Искажение фона паузы (ползущий эффект)
    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void modifyBackgroundRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (ParanoiaManager.getParanoia() > 50.0f) {
            backgroundShift += 0.05f; // Zero-allocation счетчик
            // Легкое смещение матриц всего контекста меню
            context.getMatrices().translate(Math.sin(backgroundShift) * 2.0, Math.cos(backgroundShift * 0.8) * 1.5, 0);
        }
    }
}
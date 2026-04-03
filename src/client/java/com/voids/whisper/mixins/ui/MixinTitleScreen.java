package com.voids.whisper.mixins.ui;

import com.voids.whisper.config.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Base64;

@Environment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
public class MixinTitleScreen {

    // Base64 обфускация строки: "Ты думал, что избавился от меня?"
    private static final String CURSED_MESSAGE_B64 = "0KLRiyDQtNGD0LzQsNC7LCDRh9GC0L4g0LjQt9Cx0LDQstC40LvRgdGPINC+0YIg0LzQtdC90Y8/";
    private static boolean chatEventRegistered = false;

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (ConfigManager.triggerReturnGaslight) {
            String decodedMsg = new String(Base64.getDecoder().decode(CURSED_MESSAGE_B64));

            // 1. Показываем криповый тост прямо в главном меню
            MinecraftClient.getInstance().getToastManager().add(
                    new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("???"),
                            Text.literal(decodedMsg)
                    )
            );

            // 2. Регистрируем локальный вывод в чат при входе в мир (один раз)
            if (!chatEventRegistered) {
                ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
                    if (ConfigManager.triggerReturnGaslight && client.player != null) {
                        client.player.sendMessage(Text.literal("§c" + decodedMsg), false);
                        ConfigManager.triggerReturnGaslight = false; // Сбрасываем триггер
                    }
                });
                chatEventRegistered = true;
            }
        }
    }
}
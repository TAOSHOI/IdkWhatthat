package com.voids.whisper.modules;

import com.voids.whisper.config.ParanoiaSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.Base64;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class UIParanoiaManager {
    private static final Random RANDOM = new Random();
    public static boolean isToolVisuallyBroken = false;
    public static int fakeDeathTimer = 0;
    public static int flickerTicks = 0; // Моргание света
    private static int messageCooldown = 0;

    private static final List<String> FAKE_MESSAGES_B64 = List.of(
            "0J/QvtC00LrQu9GO0YfQtdC90LjQtSDQv9C+0YLQtdGA0Y/QvdC+", "0KLRiyDQtNGD0LzQsNC7LCDRh9GC0L4g0L7QtNC40L0/"
    );

    private static final List<String> FAKE_TITLES_B64 = List.of(
            "0J7QsdC10YDQvdC40YHRjA==", "0K/Qt9C00LXRgdGM", "0J3QtSDRgdC80L7RgtGA0Lgg0L3QsCDQvdC10LPQvg=="
    );

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            float paranoia = ParanoiaManager.getParanoia();

            if (isToolVisuallyBroken && client.player.age % 40 == 0) isToolVisuallyBroken = false;
            if (fakeDeathTimer > 0) fakeDeathTimer--;
            if (messageCooldown > 0) messageCooldown--;
            if (flickerTicks > 0) flickerTicks--;

            // Моргание света
            if (paranoia > ParanoiaSettings.MIN_PARANOIA_FOR_UI && RANDOM.nextDouble() < ParanoiaSettings.FLICKER_CHANCE) {
                flickerTicks = 2; // Гаснет на 2 тика
                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 0.1f);
            }

            if (paranoia >= ParanoiaSettings.MIN_PARANOIA_FOR_UI && RANDOM.nextDouble() < ParanoiaSettings.FAKE_DAMAGE_CHANCE) {
                triggerFakeDamage(client);
            }

            if (messageCooldown <= 0 && paranoia >= ParanoiaSettings.MIN_PARANOIA_FOR_UI) {
                if (RANDOM.nextDouble() < 0.005) {
                    if (RANDOM.nextDouble() < 0.3) sendFakeTitle(client); else sendFakeChatMessage(client);
                    messageCooldown = 2400 + RANDOM.nextInt(2000);
                }
            }
        });
    }

    public static void triggerToolBreak(MinecraftClient client) {
        if (!isToolVisuallyBroken && RANDOM.nextDouble() < ParanoiaSettings.FAKE_TOOL_BREAK_CHANCE) {
            isToolVisuallyBroken = true;
            client.player.playSound(SoundEvents.ENTITY_ITEM_BREAK, 0.8f, 0.8f + RANDOM.nextFloat() * 0.4f);
        }
    }

    private static void triggerFakeDamage(MinecraftClient client) {
        client.player.playSound(SoundEvents.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
        if (ParanoiaManager.getParanoia() > ParanoiaSettings.MIN_PARANOIA_FOR_DEATH && RANDOM.nextFloat() < 0.1f) {
            fakeDeathTimer = 40; // Исправлено: 2 секунды показа
        }
    }

    private static void sendFakeChatMessage(MinecraftClient client) {
        String decoded = new String(Base64.getDecoder().decode(FAKE_MESSAGES_B64.get(RANDOM.nextInt(FAKE_MESSAGES_B64.size()))));
        client.inGameHud.getChatHud().addMessage(Text.literal("§e" + decoded));
    }

    private static void sendFakeTitle(MinecraftClient client) {
        String decoded = new String(Base64.getDecoder().decode(FAKE_TITLES_B64.get(RANDOM.nextInt(FAKE_TITLES_B64.size()))));
        client.inGameHud.setTitle(Text.literal("§c" + decoded));
        client.inGameHud.setTitleTicks(10, 60, 20);
    }
}
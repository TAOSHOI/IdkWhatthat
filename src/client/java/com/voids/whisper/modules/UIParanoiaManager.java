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
    public static float fakeDamageTilt = 0.0f;

    private static final List<String> FAKE_TITLES_B64 = List.of(
            "0J/QvtC00LrQu9GO0YfQtdC90LjQtSDQv9C+0YLQtdGA0Y/QvdC+", // Подключение потеряно
            "0KLRiyDQtNGD0LzQsNC7LCDRh9GC0L4g0L7QtNC40L0/", // Ты думал, что один?
            "0J7QsdC10YDQvdC40YHRjA==" // Обернись
    );

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            float paranoia = ParanoiaManager.getParanoia();

            if (isToolVisuallyBroken && client.player.age % 40 == 0) {
                isToolVisuallyBroken = false;
            }
            if (fakeDeathTimer > 0) fakeDeathTimer--;

            if (paranoia >= ParanoiaSettings.MIN_PARANOIA_FOR_UI && RANDOM.nextDouble() < ParanoiaSettings.FAKE_DAMAGE_CHANCE) {
                triggerFakeDamage(client);
            }
            if (paranoia >= ParanoiaSettings.MIN_PARANOIA_FOR_UI && RANDOM.nextDouble() < ParanoiaSettings.FAKE_CHAT_MSG_CHANCE) {
                sendFakeTitle(client);
            }
        });
    }

    public static void triggerToolBreak(MinecraftClient client) {
        if (!isToolVisuallyBroken && ParanoiaManager.getParanoia() > ParanoiaSettings.MIN_PARANOIA_FOR_UI) {
            if (RANDOM.nextDouble() < ParanoiaSettings.FAKE_TOOL_BREAK_CHANCE) {
                isToolVisuallyBroken = true;
                client.getSoundManager().play(new net.minecraft.client.sound.PositionedSoundInstance(
                        SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.8f, 0.8f + RANDOM.nextFloat() * 0.4f,
                        client.world.getRandom(), client.player.getX(), client.player.getY(), client.player.getZ()
                ));
            }
        }
    }

    private static void triggerFakeDamage(MinecraftClient client) {
        client.player.playSound(SoundEvents.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
        fakeDamageTilt = 1.5f;
        if (ParanoiaManager.getParanoia() > ParanoiaSettings.MIN_PARANOIA_FOR_DEATH && RANDOM.nextFloat() < 0.2f) {
            fakeDeathTimer = 10; // Быстрая вспышка "You died!"
        }
    }

    private static void sendFakeTitle(MinecraftClient client) {
        String b64 = FAKE_TITLES_B64.get(RANDOM.nextInt(FAKE_TITLES_B64.size()));
        String decoded = new String(Base64.getDecoder().decode(b64));
        // Выводим страшную надпись прямо по центру экрана красным цветом
        client.inGameHud.setTitle(Text.literal("§c" + decoded));
        client.inGameHud.setTitleTicks(10, 40, 10);
    }
}
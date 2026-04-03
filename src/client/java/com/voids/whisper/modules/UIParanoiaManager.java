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

    // Состояния для UI
    public static boolean isToolVisuallyBroken = false;
    public static int fakeDeathTimer = 0; // В тиках
    public static float fakeDamageTilt = 0.0f;

    // Зашифрованные системные сообщения (Base64)
    private static final List<String> FAKE_MESSAGES_B64 = List.of(
            "0J/QvtC00LrQu9GO0YfQtdC90LjQtSDQv9C+0YLQtdGA0Y/QvdC+", // Подключение потеряно
            "0KLRiyDQtNGD0LzQsNC7LCDRh9GC0L4g0L7QtNC40L0/", // Ты думал, что один?
            "0JjQs9GA0L7QuiB1bmtub3duINC/0YDQuNGB0L7QtdC00LjQvdC40LvRgdY=" // Игрок unknown присоединился
    );

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            float paranoia = ParanoiaManager.getParanoia();

            // 1. Очистка состояния инструмента при смене слота или бездействии
            if (isToolVisuallyBroken && client.player.age % 40 == 0) {
                isToolVisuallyBroken = false;
            }

            // 2. Таймер фейкового экрана смерти
            if (fakeDeathTimer > 0) {
                fakeDeathTimer--;
            }

            // 3. Фейковый урон и чат
            if (paranoia >= ParanoiaSettings.MIN_PARANOIA_FOR_UI && RANDOM.nextDouble() < ParanoiaSettings.FAKE_DAMAGE_CHANCE) {
                triggerFakeDamage(client);
            }

            if (paranoia >= ParanoiaSettings.MIN_PARANOIA_FOR_UI && RANDOM.nextDouble() < (ParanoiaSettings.FAKE_CHAT_MSG_CHANCE / 100)) {
                sendFakeChatMessage(client);
            }
        });
    }

    public static void triggerToolBreak(MinecraftClient client) {
        if (!isToolVisuallyBroken && ParanoiaManager.getParanoia() > ParanoiaSettings.MIN_PARANOIA_FOR_UI) {
            if (RANDOM.nextDouble() < ParanoiaSettings.FAKE_TOOL_BREAK_CHANCE) {
                isToolVisuallyBroken = true;
                client.getSoundManager().play(
                        new net.minecraft.client.sound.PositionedSoundInstance(
                                SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS,
                                0.8f, 0.8f + RANDOM.nextFloat() * 0.4f,
                                client.world.getRandom(), client.player.getX(), client.player.getY(), client.player.getZ()
                        )
                );
            }
        }
    }

    private static void triggerFakeDamage(MinecraftClient client) {
        // Симуляция получения урона
        client.player.playSound(SoundEvents.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
        fakeDamageTilt = 1.5f; // Наклон камеры

        // Шанс на фейковую смерть при высокой паранойе
        if (ParanoiaManager.getParanoia() > ParanoiaSettings.MIN_PARANOIA_FOR_DEATH && RANDOM.nextFloat() < 0.1f) {
            fakeDeathTimer = 20; // Экран висит 1 секунду (20 тиков), игра НЕ на паузе
        }
    }

    private static void sendFakeChatMessage(MinecraftClient client) {
        String b64 = FAKE_MESSAGES_B64.get(RANDOM.nextInt(FAKE_MESSAGES_B64.size()));
        String decoded = new String(Base64.getDecoder().decode(b64));
        // Желтый цвет для симуляции системного сообщения
        client.inGameHud.getChatHud().addMessage(Text.literal("§e" + decoded));
    }
}
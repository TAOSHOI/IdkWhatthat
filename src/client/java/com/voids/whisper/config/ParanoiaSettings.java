package com.voids.whisper.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ParanoiaSettings {
    // === Глобальные пороги ===
    public static final float MIN_PARANOIA_FOR_AUDIO = 20.0f;
    public static final float MIN_PARANOIA_FOR_MIRAGES = 30.0f;
    public static final float MIN_PARANOIA_FOR_UI = 40.0f;
    public static final float MIN_PARANOIA_FOR_PHANTOMS = 50.0f;
    public static final float MIN_PARANOIA_FOR_FAKE_FRIENDS = 50.0f;
    public static final float MIN_PARANOIA_FOR_CURSED_F2 = 60.0f;
    public static final float MIN_PARANOIA_FOR_DEATH = 70.0f;

    // === Аудио (Агрессивно) ===
    public static final int PHANTOM_SOUND_COOLDOWN_TICKS = 400;
    public static final double MUSIC_DISTORTION_CHANCE = 0.5;
    public static final float DISTORTION_PITCH_LEVEL = 0.5f; // ВОТ ЭТО МЫ ПОТЕРЯЛИ

    public static final float MIN_PARANOIA_FOR_WHISPER = 40.0f;
    public static final int WHISPER_COOLDOWN_TICKS = 6000;

    // === Визуальные эффекты (Агрессивно) ===
    public static final double MIRAGE_SPAWN_CHANCE = 0.15;
    public static final double MIRAGE_MAX_DIST_SQ = 900.0;
    public static final double MIRAGE_DISAPPEAR_DIST_SQ = 16.0;

    public static final double PHANTOM_SPAWN_CHANCE = 0.05;
    public static final double PHANTOM_LOOK_DOT_THRESHOLD = 0.85;

    // === UI и Текст (Агрессивно) ===
    public static final double FAKE_TOOL_BREAK_CHANCE = 0.3;
    public static final double ZALGO_TEXT_CHANCE = 0.15;
    public static final double FAKE_DAMAGE_CHANCE = 0.02;
    public static final double FAKE_CHAT_MSG_CHANCE = 0.01;

    // === Сущности и Мета ===
    public static final double FAKE_FRIEND_SPAWN_CHANCE = 0.8;
    public static final double ENTITY_DESPAWN_DISTANCE_SQ = 64.0;
    public static final double CURSED_F2_CHANCE = 0.5;

    // === Прочее ===
    public static final int IGNORED_MIRAGES_FOR_INSIGHT = 5;
    public static final double INSIGHT_CHANCE = 0.1;
    public static final int TICKS_UNTIL_CLAUSTROPHOBIA = 1200;
    public static final float MAX_VIGNETTE_OPACITY = 0.85f;
}
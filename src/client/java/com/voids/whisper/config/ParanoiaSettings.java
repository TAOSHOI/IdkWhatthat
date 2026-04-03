package com.voids.whisper.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ParanoiaSettings {
    public static final float MIN_PARANOIA_FOR_AUDIO = 20.0f;
    public static final float MIN_PARANOIA_FOR_MIRAGES = 30.0f;
    public static final float MIN_PARANOIA_FOR_UI = 40.0f;
    public static final float MIN_PARANOIA_FOR_PHANTOMS = 50.0f;
    public static final float MIN_PARANOIA_FOR_FAKE_FRIENDS = 50.0f;
    public static final float MIN_PARANOIA_FOR_CURSED_F2 = 60.0f;
    public static final float MIN_PARANOIA_FOR_DEATH = 70.0f;

    // Шансы и тайминги новых модулей
    public static final double HIVEMIND_CHANCE = 0.005; // Синхронный взгляд мобов
    public static final double FLICKER_CHANCE = 0.005; // Моргание света
    public static final double INVENTORY_GASLIGHT_CHANCE = 0.1; // Подмена предметов в руке
    public static final double SCHIZO_SUBTITLES_CHANCE = 0.01; // Фейковые субтитры

    // Остальные настройки
    public static final int PHANTOM_SOUND_COOLDOWN_TICKS = 1200;
    public static final double MUSIC_DISTORTION_CHANCE = 0.2;
    public static final float DISTORTION_PITCH_LEVEL = 0.5f;
    public static final double MIRAGE_SPAWN_CHANCE = 0.05;
    public static final double MIRAGE_MAX_DIST_SQ = 900.0;
    public static final double MIRAGE_DISAPPEAR_DIST_SQ = 25.0;
    public static final double PHANTOM_SPAWN_CHANCE = 0.02;
    public static final double PHANTOM_LOOK_DOT_THRESHOLD = 0.85;
    public static final double FAKE_TOOL_BREAK_CHANCE = 0.1;
    public static final double ZALGO_TEXT_CHANCE = 0.15;
    public static final double FAKE_DAMAGE_CHANCE = 0.005;
    public static final double FAKE_CHAT_MSG_CHANCE = 0.005;
    public static final double FAKE_FRIEND_SPAWN_CHANCE = 0.8;
    public static final double ENTITY_DESPAWN_DISTANCE_SQ = 64.0;
    public static final double CURSED_F2_CHANCE = 1.0; // 100% испорченный скриншот при высокой паранойе!
    public static final int TICKS_UNTIL_CLAUSTROPHOBIA = 1200;
    public static final float MAX_VIGNETTE_OPACITY = 0.85f;
}
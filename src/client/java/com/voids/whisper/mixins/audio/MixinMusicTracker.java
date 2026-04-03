package com.voids.whisper.mixins.audio;

import com.voids.whisper.config.ParanoiaSettings;
import com.voids.whisper.modules.ParanoiaManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.MusicTracker;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Random;

@Environment(EnvType.CLIENT)
@Mixin(MusicTracker.class)
public class MixinMusicTracker {
    private static final Random RANDOM = new Random();

    // Перехватываем локальную переменную SoundInstance перед передачей в SoundManager
    @ModifyVariable(method = "play", at = @At("STORE"), ordinal = 0)
    private SoundInstance modifyMusicPitch(SoundInstance originalSound) {
        if (ParanoiaManager.getParanoia() >= ParanoiaSettings.MIN_PARANOIA_FOR_AUDIO
                && RANDOM.nextDouble() <= ParanoiaSettings.MUSIC_DISTORTION_CHANCE) {

            // Возвращаем копию звука, но с пониженным (криповым) питчем
            return new PositionedSoundInstance(
                    originalSound.getId(),
                    originalSound.getCategory(),
                    originalSound.getVolume(),
                    ParanoiaSettings.DISTORTION_PITCH_LEVEL, // 0.5f - замедленная, искаженная музыка
                    SoundInstance.createRandom(),
                    originalSound.isRepeatable(),
                    originalSound.getRepeatDelay(),
                    originalSound.getAttenuationType(),
                    originalSound.getX(), originalSound.getY(), originalSound.getZ(),
                    false
            );
        }
        return originalSound;
    }
}
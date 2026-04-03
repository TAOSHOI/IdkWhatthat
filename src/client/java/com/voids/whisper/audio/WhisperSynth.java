package com.voids.whisper.audio;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.openal.AL10;

import java.nio.ShortBuffer;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class WhisperSynth {
    private static final int SAMPLE_RATE = 22050;
    private static boolean isPlaying = false;

    public static void triggerWhisper(String playerName) {
        if (isPlaying) return;
        isPlaying = true;

        CompletableFuture.supplyAsync(() -> generateCreepyWaveform(playerName))
                .thenAcceptAsync(pcm -> {
                    MinecraftClient.getInstance().execute(() -> {
                        // Выводим текст над хотбаром (субтитры), чтобы игрок понял, что это было
                        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.literal("§7§oШёпот..."), false);
                        playBuffer(pcm);
                    });
                });
    }

    private static short[] generateCreepyWaveform(String seedString) {
        int durationSeconds = 3;
        short[] buffer = new short[SAMPLE_RATE * durationSeconds];
        int seed = seedString.hashCode();

        for (int i = 0; i < buffer.length; i++) {
            double time = (double) i / SAMPLE_RATE;
            double mod = Math.sin(time * 5.0 + seed);
            double baseFreq = 150.0 + (mod * 20.0);
            double noise = (Math.random() * 2.0 - 1.0) * 0.4;
            double sine = Math.sin(time * baseFreq * Math.PI * 2.0);
            double envelope = Math.sin(time * Math.PI / durationSeconds);

            double sample = (sine * 0.5 + noise) * envelope;
            buffer[i] = (short) (sample * Short.MAX_VALUE * 0.8); // ГРОМКОСТЬ 80%
        }
        return buffer;
    }

    private static void playBuffer(short[] pcmData) {
        try {
            int buffer = AL10.alGenBuffers();
            int source = AL10.alGenSources();

            ShortBuffer shortBuffer = ShortBuffer.wrap(pcmData);
            AL10.alBufferData(buffer, AL10.AL_FORMAT_MONO16, shortBuffer, SAMPLE_RATE);

            AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
            AL10.alSourcef(source, AL10.AL_GAIN, 1.0f); // Максимальный выходной гейн
            AL10.alSourcePlay(source);

            CompletableFuture.runAsync(() -> {
                try {
                    while (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING) {
                        Thread.sleep(100);
                    }
                    MinecraftClient.getInstance().execute(() -> {
                        AL10.alDeleteSources(source);
                        AL10.alDeleteBuffers(buffer);
                        isPlaying = false;
                    });
                } catch (InterruptedException ignored) {}
            });
        } catch (Exception e) {
            isPlaying = false;
        }
    }
}
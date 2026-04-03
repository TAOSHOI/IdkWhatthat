package com.voids.whisper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

@Environment(EnvType.CLIENT)
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "whisper_memory.json");

    public static WhisperConfig config = new WhisperConfig();

    // Флаг для срабатывания эффекта "возвращения"
    public static boolean triggerReturnGaslight = false;

    public static void init() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, WhisperConfig.class);
                if (config.wasInstalledBefore) {
                    triggerReturnGaslight = true;
                }
            } catch (Exception e) {
                System.err.println("[Whisper] Failed to load memory.");
            }
        }

        config.wasInstalledBefore = true;
        save();
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
            System.err.println("[Whisper] Failed to save memory.");
        }
    }
}
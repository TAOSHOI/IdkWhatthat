package com.voids.whisper.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class WhisperConfig {
    public boolean wasInstalledBefore = false;
    public boolean isModuleEnabled = true;
    public float savedParanoiaLevel = 0.0f;
    public int totalPlayTimeMinutes = 0;
}
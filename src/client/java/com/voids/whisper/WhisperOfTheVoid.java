package com.voids.whisper;

import com.voids.whisper.config.ConfigManager;
import com.voids.whisper.modules.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class WhisperOfTheVoid implements ClientModInitializer {
    public static final String MOD_ID = "whisper_of_the_void";
    public static final Logger LOGGER = LoggerFactory.getLogger("Whisper");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[WhisperOfTheVoid] The void is listening...");

        // 1. Память и Ядро
        ConfigManager.init();
        if (!ConfigManager.config.isModuleEnabled) {
            LOGGER.info("[Whisper] Paranoia engine is disabled in config.");
            return;
        }

        ParanoiaManager.init();

        // 2. Аудио-террор
        AudioTerrorManager.init();
        ClaustrophobiaEffect.init();

        // 3. Визуальный Газлайтинг
        MirageOresManager.init();
        PeripheralPhantomManager.init();
        BuildersGaslightManager.init();

        // 4. UI-Террор
        UIParanoiaManager.init();

        // В методе onInitializeClient() добавь:
        DebugControlManager.init();

        // 5. Иллюзия мультиплеера
        FakeFriendsManager.init();
        EchoStepsManager.init();
        HivemindManager.init();
        SchizoSubtitlesManager.init();
        // InventoryGaslightManager инициализировать не нужно, у него статичный метод
    }
}
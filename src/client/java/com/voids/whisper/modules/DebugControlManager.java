package com.voids.whisper.modules;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class DebugControlManager {
    private static KeyBinding keyMax;
    private static KeyBinding keyReset;
    private static KeyBinding keyToggleHud;

    public static boolean showDebugHud = false;

    public static void init() {
        // Регистрируем кнопки
        keyMax = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Debug: Max Paranoia", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "Whisper Debug"));
        keyReset = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Debug: Reset Paranoia", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "Whisper Debug"));
        keyToggleHud = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Debug: Toggle HUD", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_L, "Whisper Debug"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyMax.wasPressed()) {
                ParanoiaManager.setParanoia(100.0f);
                client.player.sendMessage(Text.literal("§c[Whisper] Paranoia set to MAX (100)"), true);
            }
            while (keyReset.wasPressed()) {
                ParanoiaManager.setParanoia(0.0f);
                client.player.sendMessage(Text.literal("§a[Whisper] Paranoia reset to 0"), true);
            }
            while (keyToggleHud.wasPressed()) {
                showDebugHud = !showDebugHud;
                client.player.sendMessage(Text.literal("§b[Whisper] Debug HUD: " + (showDebugHud ? "ON" : "OFF")), true);
            }
        });
    }
}
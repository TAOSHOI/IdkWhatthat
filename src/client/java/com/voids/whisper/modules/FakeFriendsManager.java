package com.voids.whisper.modules;

import com.mojang.authlib.GameProfile;
import com.voids.whisper.config.ParanoiaSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class FakeFriendsManager {
    private static final Map<UUID, PlayerData> knownPlayers = new HashMap<>();
    private static final Random RANDOM = new Random();

    private static int nextFakeEntityId = -100000;
    private static OtherClientPlayerEntity currentFakeFriend = null;

    private record PlayerData(GameProfile profile, Vec3d lastPos) {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                knownPlayers.clear();
                currentFakeFriend = null;
                return;
            }

            Map<UUID, PlayerData> currentPlayers = new HashMap<>();
            for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
                if (player.getUuid().equals(client.player.getUuid())) continue;
                currentPlayers.put(player.getUuid(), new PlayerData(player.getGameProfile(), player.getPos()));
            }

            if (ParanoiaManager.getParanoia() >= ParanoiaSettings.MIN_PARANOIA_FOR_FAKE_FRIENDS) {
                for (Map.Entry<UUID, PlayerData> entry : knownPlayers.entrySet()) {
                    if (!currentPlayers.containsKey(entry.getKey())) {
                        triggerFakeFriend(client, entry.getValue());
                    }
                }
            }

            knownPlayers.clear();
            knownPlayers.putAll(currentPlayers);

            if (currentFakeFriend != null) {
                if (client.player.squaredDistanceTo(currentFakeFriend) < ParanoiaSettings.ENTITY_DESPAWN_DISTANCE_SQ) {
                    despawnFakeFriend(client);
                }
            }
        });
    }

    private static void triggerFakeFriend(MinecraftClient client, PlayerData data) {
        if (currentFakeFriend != null || RANDOM.nextDouble() > ParanoiaSettings.FAKE_FRIEND_SPAWN_CHANCE) return;

        currentFakeFriend = new OtherClientPlayerEntity(client.world, data.profile());
        currentFakeFriend.setPosition(data.lastPos());
        currentFakeFriend.setYaw(client.player.getYaw() + 180.0f);
        currentFakeFriend.setPitch(0.0f);

        currentFakeFriend.setId(nextFakeEntityId--);
        client.world.addEntity(currentFakeFriend);
    }

    private static void despawnFakeFriend(MinecraftClient client) {
        if (currentFakeFriend != null) {
            client.world.removeEntity(currentFakeFriend.getId(), net.minecraft.entity.Entity.RemovalReason.DISCARDED);

            client.world.playSound(client.player, currentFakeFriend.getBlockPos(),
                    SoundEvents.BLOCK_SAND_STEP, SoundCategory.PLAYERS, 1.0f, 0.5f);

            currentFakeFriend = null;
        }
    }
}
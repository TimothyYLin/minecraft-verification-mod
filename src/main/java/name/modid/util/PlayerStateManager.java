package name.modid.util;

import name.modid.networking.ModPackets;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PlayerStateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerStateManager.class);

    private PlayerStateManager() {}

    public static final Set<UUID> unverifiedPlayersToWatch = new HashSet<>();

    public static final Set<UUID> playersToLandSafely = new HashSet<>();

    public static final double BOBBING_MAX_Y_LEVEL = 150.0;
    public static final double BOBBING_MIN_Y_LEVEL = 130.0;

    /**
     * Forcefully synchronizes a player's frozen state between the server and client.
     * @param player The target player.
     * @param isFrozen The desired frozen state.
     */
    public static void syncFrozenState(ServerPlayerEntity player, boolean isFrozen) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(isFrozen);
        ServerPlayNetworking.send(player, ModPackets.PLAYER_FROZEN_STATUS_ID, buf);

        if (isFrozen) {
            LOGGER.info("Player {} is not verified. Applying levitation.", player.getName().getString());
            unverifiedPlayersToWatch.add(player.getUuid());
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, StatusEffectInstance.INFINITE,
                    0, false, false, false));
        } else {
            LOGGER.info("Player {} is verified. Allowing to land safely.", player.getName().getString());
            unverifiedPlayersToWatch.remove(player.getUuid());
            player.removeStatusEffect(StatusEffects.LEVITATION);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 1200, 0,
                    false, false, false));
            playersToLandSafely.add(player.getUuid());
        }
    }

    public static void onServerTick(MinecraftServer server) {
        handleBobbingPlayers(server);
        handleLandingPlayers(server);
    }

    private static void handleBobbingPlayers(MinecraftServer server) {
        for (UUID playerUuid : unverifiedPlayersToWatch.toArray(new UUID[0])) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);
            if (player == null) {
                unverifiedPlayersToWatch.remove(playerUuid);
                continue;
            }

            if (player.isSprinting()) {
                player.setSprinting(false);
            }

            boolean hasLevitation = player.hasStatusEffect(StatusEffects.LEVITATION);
            if (player.getY() > BOBBING_MAX_Y_LEVEL && hasLevitation) {
                player.removeStatusEffect(StatusEffects.LEVITATION);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING,
                        StatusEffectInstance.INFINITE, 0, false, false, false));
            } else if (player.getY() < BOBBING_MIN_Y_LEVEL && !hasLevitation) {
                player.removeStatusEffect(StatusEffects.SLOW_FALLING);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION,
                        StatusEffectInstance.INFINITE, 0, false, false, false));
            }
        }
    }

    private static void handleLandingPlayers(MinecraftServer server) {
        for (UUID playerUuid : playersToLandSafely.toArray(new UUID[0])) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);

            if (player == null) {
                playersToLandSafely.remove(playerUuid);
                continue;
            }

            if (player.isOnGround()) {
                LOGGER.info("Player {} has landed safely. Removing temporary Slow Falling.", player.getName().getString());

                if(player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                    player.removeStatusEffect(StatusEffects.SLOW_FALLING);
                }
                playersToLandSafely.remove(playerUuid);
            }
        }
    }
}
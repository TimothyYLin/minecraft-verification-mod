package name.modid.util;

import name.modid.networking.ModPackets;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerStateManager {

    private PlayerStateManager() {}

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
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, StatusEffectInstance.INFINITE,
                    255, false, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, StatusEffectInstance.INFINITE,
                    128, false, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, StatusEffectInstance.INFINITE,
                    4, false, false, false));
        } else {
            player.removeStatusEffect(StatusEffects.SLOWNESS);
            player.removeStatusEffect(StatusEffects.JUMP_BOOST);
            player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        }
    }
}
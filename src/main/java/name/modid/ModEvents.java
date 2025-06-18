package name.modid;

import name.modid.util.PlayerStateManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModEvents.class);

    public static void register(){
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {

            ServerPlayerEntity player = handler.player;
            LOGGER.info("Register called for player {}, UUID {}:", player.getName().getString(), player.getUuidAsString());

            HttpApiClient.isVerifiedAsync(player.getUuidAsString())
                .thenAccept(isVerified -> {
                    boolean isFrozen = !isVerified;

                    if(!isVerified) {
                        LOGGER.info("Player {} is not verified. Applying freeze effect.", player.getName().getString());
                        player.sendMessage(Text.literal("Welcome! Please follow the on-screen instructions " +
                                        "to get yourself verified and start playing.").formatted(Formatting.GRAY));
                    }else{
                        LOGGER.info("Player {} is verified.", player.getName().getString());
                        player.sendMessage(Text.literal("Welcome back! You have already been verified!")
                                .formatted(Formatting.GREEN));
                    }

                    server.execute(() -> PlayerStateManager.syncFrozenState(player, isFrozen));
                });
        });

        ServerTickEvents.END_SERVER_TICK.register(PlayerStateManager::onServerTick);
    }
}

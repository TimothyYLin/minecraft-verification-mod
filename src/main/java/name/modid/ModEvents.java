package name.modid;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModEvents.class);

    public static void register(){
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {

            ServerPlayerEntity player = handler.player;
            LOGGER.info("Register called for player {}, UUID {}:", player.getName().getString(), player.getUuidAsString());

            HttpApiClient.isVerifiedAsync(player.getUuidAsString())
                .thenAccept(isVerified -> {
                    if(!isVerified){
                        LOGGER.info("Player {} is not verified. Applying freeze effect.", player.getName().getString());
                        server.execute(() -> {
                            player.addStatusEffect(
                                    new StatusEffectInstance(StatusEffects.SLOWNESS,
                                            StatusEffectInstance.INFINITE,
                                            255,
                                            false,
                                            false,
                                            false));
                        });
                    }
                });
        }));
    }
}

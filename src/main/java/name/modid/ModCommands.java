package name.modid;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import name.modid.api.McVerifyResult;
import name.modid.util.PlayerStateManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ModCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModCommands.class);
    private static final String VERIFY = "verify";

    private ModCommands() {}

    public static void register(){
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal(VERIFY)
                .executes(context -> {
                    context.getSource().sendFeedback(() ->
                        Text.literal("Usage:")
                            .append(Text.literal("/verify <code>").formatted(Formatting.YELLOW))
                            .append(Text.literal(" or "))
                            .append(Text.literal("/verify recheck").formatted(Formatting.YELLOW)),
                            false);
                    return 1;
                })
                .then(CommandManager.literal("recheck")
                    .executes(context -> {
                        final ServerCommandSource source = context.getSource();
                        final ServerPlayerEntity player = source.getPlayerOrThrow();
                        source.sendFeedback(() ->
                            Text.literal("Re-checking your verification status...").formatted(Formatting.GRAY),
                            false);

                        HttpApiClient.isVerifiedAsync(player.getUuidAsString())
                            .thenAccept(isVerified -> {
                                source.getServer().execute(() -> handleRecheckResult(player, isVerified));
                            });
                        return 1;
                    })
                )
                .then(CommandManager.argument("code", IntegerArgumentType.integer())
                    .executes(context -> {
                        final ServerCommandSource source = context.getSource();
                        final ServerPlayerEntity player = source.getPlayerOrThrow();
                        final int code = context.getArgument("code", Integer.class);

                        source.sendFeedback(() -> Text.literal("Verifying your code...").formatted(Formatting.GRAY), false);

                        HttpApiClient.verifyAsync(code, player.getUuidAsString())
                            .thenAccept(result -> {
                                source.getServer().execute(() -> handleVerificationResult(player, result));
                            });
                        return 1;
                    })
                )
            );
        });
    }

    private static void handleRecheckResult(ServerPlayerEntity player, boolean isVerified){
        if(isVerified){
            player.sendMessage(Text.literal("Status re-checked. You are verified!")
                    .formatted(Formatting.GREEN));
        }else{
            player.sendMessage(Text.literal("Status re-checked. Your account is still not verified.")
                    .formatted(Formatting.RED));
        }
        PlayerStateManager.syncFrozenState(player, !isVerified);
    }

    private static void handleVerificationResult(ServerPlayerEntity player, McVerifyResult result){
        switch (result.status()) {
            case SUCCESS:
                player.sendMessage(Text.literal("You have been successfully verified!")
                        .formatted(Formatting.GREEN));
                if(player.hasStatusEffect(StatusEffects.SLOWNESS)){
                    player.removeStatusEffect(StatusEffects.SLOWNESS);
                }
                PlayerStateManager.syncFrozenState(player, false);
                break;
            case INVALID_CODE:
                player.sendMessage(Text.literal("That code is incorrect. Please try again.")
                        .formatted(Formatting.RED));
                PlayerStateManager.syncFrozenState(player, true);
                break;
            case EXPIRED_CODE:
                player.sendMessage(Text.literal("That code has expired. Please request a new one.")
                        .formatted(Formatting.YELLOW));
                PlayerStateManager.syncFrozenState(player, true);
                break;
            case CONFLICT:
                player.sendMessage(Text.literal("This Minecraft account has already been linked to a portal account.")
                        .formatted(Formatting.RED));
                PlayerStateManager.syncFrozenState(player, true);
                break;
            case BAD_REQUEST:
            case INTERNAL_SERVER_ERROR:
            case API_ERROR:
                player.sendMessage(
                        Text.literal("An unexpected error occurred. Please try again later or contact an administrator.")
                                .formatted(Formatting.RED));
                LOGGER.error("Verification failed for player {} due to system error: {} - Body: {}",
                        player.getName().getString(), result.status(), result.responseBody());
                PlayerStateManager.syncFrozenState(player, true);
                break;
        }
    }
}

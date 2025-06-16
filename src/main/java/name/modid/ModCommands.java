package name.modid;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import name.modid.api.McVerifyResult;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal(VERIFY)
                .executes(context -> {
                    context.getSource().sendFeedback(() ->
                        Text.literal("Usage:")
                            .append(Text.literal("/verify ").formatted(Formatting.YELLOW))
                            .append(Text.literal("<code>").formatted(Formatting.GRAY)),
                            false);
                    return 1;
                })
                .then(CommandManager.argument("code", IntegerArgumentType.integer()))
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
            );
        });
    }

    private static void handleVerificationResult(ServerPlayerEntity player, McVerifyResult result){
        switch (result.status()) {
            case SUCCESS:
                player.sendMessage(Text.literal("You have been successfully verified!").formatted(Formatting.GREEN));
                if(player.hasStatusEffect(StatusEffects.SLOWNESS)){
                    player.removeStatusEffect(StatusEffects.SLOWNESS);
                }
                break;
            case INVALID_CODE:
                player.sendMessage(Text.literal("That code is incorrect. Please try again.").formatted(Formatting.RED));
                break;
            case EXPIRED_CODE:
                player.sendMessage(Text.literal("That code has expired. Please request a new one.").formatted(Formatting.YELLOW));
                break;
            case CONFLICT:
                player.sendMessage(Text.literal("This Minecraft account has already been linked to a portal account.").formatted(Formatting.RED));
                break;
            case BAD_REQUEST:
            case INTERNAL_SERVER_ERROR:
            case API_ERROR:
                player.sendMessage(Text.literal("An unexpected error occurred. Please try again later or contact an administrator.").formatted(Formatting.RED));
                LOGGER.error("Verification failed for player {} due to system error: {} - Body: {}", player.getName().getString(), result.status(), result.responseBody());
                break;
        }
    }
}

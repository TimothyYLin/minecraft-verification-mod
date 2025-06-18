package name.modid.event;

import name.modid.FrozenState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class ClientTickHandler {

    private ClientTickHandler() {}

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(ClientTickHandler::onClientTick);
    }

    private static void onClientTick(MinecraftClient client) {
        if (client.player == null || !FrozenState.isFrozen) {
            return;
        }

        if (client.options.sprintKey.isPressed()) {
            client.options.sprintKey.setPressed(false);
        }

        client.player.setSprinting(false);
    }
}

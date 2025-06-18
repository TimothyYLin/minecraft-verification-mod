package name.modid.networking;

import name.modid.FrozenState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class PacketReceivers {

    private PacketReceivers() {}

    @SuppressWarnings("ConstantConditions")
    public static void registerS2CHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.PLAYER_FROZEN_STATUS_ID,
                (client, handler, buf, responseSender) -> {
                    boolean frozenState = buf.readBoolean();
                    client.execute(() -> FrozenState.isFrozen = frozenState);
                });
    }
}

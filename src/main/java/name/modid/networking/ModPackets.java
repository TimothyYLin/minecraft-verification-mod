package name.modid.networking;

import net.minecraft.util.Identifier;

public class ModPackets {
    private ModPackets() {}

    public static final Identifier PLAYER_FROZEN_STATUS_ID =
            Identifier.of("verification-mod", "player_frozen_status");

}

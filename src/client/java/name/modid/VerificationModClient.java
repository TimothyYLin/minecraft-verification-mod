package name.modid;

import name.modid.networking.ModPackets;
import name.modid.rendering.FrozenOverlayRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationModClient implements ClientModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(VerificationModClient.class);

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		LOGGER.info("VerificationModClient has initialized");
		registerPacketHandlers();
		FrozenOverlayRenderer.register();
	}

	@SuppressWarnings("ConstantConditions")
	private static void registerPacketHandlers() {
		ClientPlayNetworking.registerGlobalReceiver(ModPackets.PLAYER_FROZEN_STATUS_ID,
				(client, handler, buf, responseSender) -> {
			boolean frozenState = buf.readBoolean();

			client.execute(() -> FrozenState.isFrozen = frozenState);
		});
	}
}
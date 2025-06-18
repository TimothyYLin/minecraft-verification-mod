package name.modid;

import name.modid.event.ClientTickHandler;
import name.modid.networking.PacketReceivers;
import name.modid.rendering.FrozenOverlayRenderer;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationModClient implements ClientModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(VerificationModClient.class);

	@Override
	public void onInitializeClient() {
		LOGGER.info("VerificationModClient has initialized");
		PacketReceivers.registerS2CHandlers();
		FrozenOverlayRenderer.register();
		ClientTickHandler.register();
	}
}
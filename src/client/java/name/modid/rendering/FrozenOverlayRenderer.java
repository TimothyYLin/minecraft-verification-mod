package name.modid.rendering;

import name.modid.FrozenState;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class FrozenOverlayRenderer {

    public static void register(){
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
           if(!FrozenState.isFrozen){
               return;
           }

            MinecraftClient client = MinecraftClient.getInstance();
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            drawContext.fill(0, 0, screenWidth, screenHeight, RenderingConstants.FROZEN_OVERLAY_COLOR);
            List<Text> textLines = List.of(
                    Text.literal("Your account is not verified.").formatted(Formatting.WHITE),
                    Text.literal("Please use ").append(
                            Text.literal("/verify <code>").formatted(Formatting.YELLOW)
                    ),
                    Text.literal("If you believe this is an error, re-login or use ").append(
                            Text.literal("/verify recheck").formatted(Formatting.YELLOW)
                    )
            );

            int totalTextHeight = textLines.size() * RenderingConstants.OVERLAY_TEXT_LINE_SPACING;
            int currentY = (screenHeight / 2) - (totalTextHeight / 2);
            for (Text line : textLines) {
                drawContext.drawCenteredTextWithShadow(
                        client.textRenderer,
                        line,
                        screenWidth / 2,
                        currentY,
                        0xFFFFFF
                );
                currentY += RenderingConstants.OVERLAY_TEXT_LINE_SPACING;
            }

        });
    }
}

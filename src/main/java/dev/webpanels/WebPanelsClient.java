package dev.webpanels;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.MCEFBrowser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class WebPanelsClient implements ClientModInitializer {
    private static KeyBinding openKey;
    private static volatile boolean mcefReady;

    @Override
    public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.webpanels.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O,
                "key.categories.webpanels"));

        MCEF.scheduleForInit(success -> mcefReady = success);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (mcefReady && MCEF.isInitialized()) {
                    client.setScreen(new WebPanelsScreen());
                }
            }
        });
    }

    static MCEFBrowser createBrowser(String url, int width, int height) {
        if (!mcefReady || !MCEF.isInitialized()) return null;
        return MCEF.createBrowser(url, false, width, height);
    }
}

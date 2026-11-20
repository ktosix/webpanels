package dev.webpanels;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.MCEFBrowser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class WebPanelsClient implements ClientModInitializer {
    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.webpanels.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O,
                "key.categories.webpanels"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (client.currentScreen != null) continue;
                try {
                    if (MCEF.isInitialized()) {
                        client.setScreen(new WebPanelsScreen());
                    } else {
                        say(client, "WebPanels: MCEF nie jest gotowy (jeszcze pobiera Chromium albo sie nie uruchomil). Sprawdz logs/latest.log i szukaj 'mcef'.");
                    }
                } catch (Throwable t) {
                    say(client, "WebPanels: blad przy otwieraniu: " + t);
                    t.printStackTrace();
                }
            }
        });
    }

    private static void say(MinecraftClient client, String msg) {
        if (client.player != null) client.player.sendMessage(Text.literal(msg), false);
    }

    static MCEFBrowser createBrowser(String url, int width, int height) {
        if (!MCEF.isInitialized()) return null;
        return MCEF.createBrowser(url, false, width, height);
    }
}

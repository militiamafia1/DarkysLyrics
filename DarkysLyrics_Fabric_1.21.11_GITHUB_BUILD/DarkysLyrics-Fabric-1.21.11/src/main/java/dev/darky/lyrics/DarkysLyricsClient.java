package dev.darky.lyrics;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class DarkysLyricsClient implements ClientModInitializer {
    private static KeyMapping openMenuKey;

    @Override
    public void onInitializeClient() {
        LrcLyrics.reload();
        MediaBridgeClient.start();
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.darkys_lyrics.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.darkys_lyrics"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new LyricsScreen());
                }
            }
        });
    }

    public static void openMenu() {
        Minecraft.getInstance().setScreen(new LyricsScreen());
    }
}

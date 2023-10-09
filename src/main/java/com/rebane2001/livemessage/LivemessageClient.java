package com.rebane2001.livemessage;

import com.rebane2001.livemessage.gui.LivemessageGui;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class LivemessageClient implements ClientModInitializer {
    private static void openGui(MinecraftClient client) {
        client.setScreen(new LivemessageGui(client.currentScreen));
    }

    @Override
    public void onInitializeClient() {
        KeyBinding openLivemessageKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.livemessage.open", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_T, // The keycode of the key
                "category.livemessage" // The translation key of the keybinding's category.
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openLivemessageKeybind.wasPressed()) {
                openGui(client);
            }
        });
    }
}

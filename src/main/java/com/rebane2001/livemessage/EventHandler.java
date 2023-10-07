package com.rebane2001.livemessage;

import com.rebane2001.livemessage.gui.LivemessageGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class EventHandler {
    private static void openGui() {
        MinecraftClient.getInstance().setScreen(new LivemessageGui(MinecraftClient.getInstance().currentScreen));
    }
}

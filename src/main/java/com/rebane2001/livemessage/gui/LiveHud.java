package com.rebane2001.livemessage.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
public class LiveHud {

    public static void addToast(String title, String message){
        // TODO: Reimplement ellipsis
        /*
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        if (fontRenderer.getStringWidth(message) > 138)
            message = fontRenderer.trimStringToWidth(message,138 - fontRenderer.getStringWidth("...")) + "...";

         */
        MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.NARRATOR_TOGGLE, Text.of(title), Text.of(message)));
    }

    public static void playNotificationSound(){
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, 1f, 1f));
    }
}

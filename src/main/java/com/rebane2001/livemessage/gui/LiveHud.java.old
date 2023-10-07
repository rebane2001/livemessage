package com.rebane2001.livemessage.gui;

import com.rebane2001.livemessage.Livemessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.client.gui.toasts.SystemToast.Type.NARRATOR_TOGGLE;

@Mod.EventBusSubscriber(modid = Livemessage.MOD_ID)
public class LiveHud {

    public static void addToast(String title, String message){
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        if (fontRenderer.getStringWidth(message) > 138)
            message = fontRenderer.trimStringToWidth(message,138 - fontRenderer.getStringWidth("...")) + "...";
        Minecraft.getMinecraft().getToastGui().add(
                new SystemToast(NARRATOR_TOGGLE, new TextComponentString(title), new TextComponentString(message))
        );
    }

    public static void playNotificationSound(){
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, 1F, 1F));
    }

    /*
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void renderGameOverlayEventPost(RenderGameOverlayEvent.Post event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontRenderer = mc.fontRenderer;
        ScaledResolution sr = new ScaledResolution(mc);
        fontRenderer.drawString("XD",sr.getScaledWidth()/2,0,GuiUtil.getSingleRGB(255));
        mc.ingameGUI.drawTexturedModalRect(0, 0, 0, 0, 81, 8);
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("minecraft", "textures/gui/icons.png"));
    }
     */

}

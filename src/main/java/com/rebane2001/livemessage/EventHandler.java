package com.rebane2001.livemessage;

import com.rebane2001.livemessage.gui.ChatWindow;
import com.rebane2001.livemessage.gui.LivemessageGui;
import com.rebane2001.livemessage.gui.ManeWindow;
import com.rebane2001.livemessage.util.LiveProfileCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import static net.minecraftforge.client.settings.KeyConflictContext.IN_GAME;

@Mod.EventBusSubscriber(modid = Livemessage.MOD_ID)
public class EventHandler {

    public static KeyBinding[] keyBindings;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority= EventPriority.NORMAL, receiveCanceled=true)
    public static void onEvent(InputEvent.KeyInputEvent event)
    {
        if (keyBindings[0].isPressed()) {
            openGui(Minecraft.getMinecraft().player);
        }

    }


    public static boolean handleMouseClick(final EntityPlayerSP player, final RayTraceResult objectMouseOver) {
        if(!LivemessageConfig.extraSettings.shiftRightClick) {
            return false;
        }
        if(player == null) {
            return false; // wont happen but whatever
        }
        if(objectMouseOver == null)  {
            return false;
        }

        // only do it when sneaking
        if(!player.isSneaking()) {
            return false;
        }
        // only when looking at an entity
        if(objectMouseOver.typeOfHit != RayTraceResult.Type.ENTITY) {
            return false;
        }
        // also wont happen because it wouldnt have been an entity hit somehow
        if(objectMouseOver.entityHit == null) {
            return false;
        }

        if(!(objectMouseOver.entityHit instanceof EntityOtherPlayerMP)) {
            return false;
        }

        // TODO cancel in advance if the player you right clicked is banned? or bypass then?

        openGui(player);
        LivemessageGui.selectBuddy(((EntityPlayer) objectMouseOver.entityHit).getGameProfile().getId());
        return true;
    }

    private static void openGui(final EntityPlayerSP player) {
        player.openGui(Livemessage.instance, 0, Minecraft.getMinecraft().world, 0, 0, 0);
    }

    public static void initKeys(){
        keyBindings = new KeyBinding[1];

        keyBindings[0] = new KeyBinding("Open Livemessage GUI", IN_GAME, KeyModifier.CONTROL, Keyboard.KEY_T, "Livemessage");

        for (int i = 0; i < keyBindings.length; ++i)
        {
            ClientRegistry.registerKeyBinding(keyBindings[i]);
        }
    }

}

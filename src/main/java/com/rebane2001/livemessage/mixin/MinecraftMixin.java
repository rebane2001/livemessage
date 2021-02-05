package com.rebane2001.livemessage.mixin;

import com.rebane2001.livemessage.EventHandler;
import com.rebane2001.livemessage.gui.LivemessageGui;
import com.rebane2001.livemessage.util.LivemessageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import scala.collection.parallel.ParIterableLike;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {


    @Shadow public EntityPlayerSP player;

    @Shadow public RayTraceResult objectMouseOver;

    @Inject(method = "rightClickMouse", at = @At(value = "HEAD"), cancellable = true)
    public void rightClickMouse(final CallbackInfo callbackInfo) {
        if(EventHandler.handleMouseClick(player, objectMouseOver)) {
            callbackInfo.cancel();
        }
    }

}

package com.rebane2001.livemessage.mixin;

import com.rebane2001.livemessage.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow public EntityPlayerSP player;
    @Shadow public RayTraceResult objectMouseOver;

    // for the sneakRightClick feature
    @Inject(method = "rightClickMouse", at = @At(value = "HEAD"), cancellable = true)
    public void rightClickMouse(final CallbackInfo callbackInfo) {
        if(EventHandler.handleMouseClick(player, objectMouseOver))
            callbackInfo.cancel();
    }

}

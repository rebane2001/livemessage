package com.rebane2001.livemessage.util;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

/**
 * This class is pretty much NetworkPlayerInfo with the requireSecure value set to false.
 */
public class LiveSkinUtil {
    Map<MinecraftProfileTexture.Type, ResourceLocation> playerTextures = Maps.newEnumMap(MinecraftProfileTexture.Type.class);
    private boolean playerTexturesLoaded = false;
    private String skinType;
    private GameProfile gameProfile;

    public LiveSkinUtil(UUID uuid){
        gameProfile = Minecraft.getMinecraft().getSessionService().fillProfileProperties(new GameProfile(uuid,null), false);
    }

    public boolean hasLocationSkin()
    {
        return this.getLocationSkin() != null;
    }

    public String getSkinType()
    {
        return this.skinType == null ? DefaultPlayerSkin.getSkinType(this.gameProfile.getId()) : this.skinType;
    }

    public boolean customSkinLoaded(){
        return this.playerTextures.get(MinecraftProfileTexture.Type.SKIN) != null;
    }

    public ResourceLocation getLocationSkin()
    {
        this.loadPlayerTextures();
        return (ResourceLocation) MoreObjects.firstNonNull(this.playerTextures.get(MinecraftProfileTexture.Type.SKIN), DefaultPlayerSkin.getDefaultSkin(this.gameProfile.getId()));
    }

    @Nullable
    public ResourceLocation getLocationCape()
    {
        this.loadPlayerTextures();
        return this.playerTextures.get(MinecraftProfileTexture.Type.CAPE);
    }

    /**
     * Gets the special Elytra texture for the player.
     */
    @Nullable
    public ResourceLocation getLocationElytra()
    {
        this.loadPlayerTextures();
        return this.playerTextures.get(MinecraftProfileTexture.Type.ELYTRA);
    }

    protected void loadPlayerTextures()
    {
        synchronized (this)
        {
            if (!this.playerTexturesLoaded)
            {
                this.playerTexturesLoaded = true;
                Minecraft.getMinecraft().getSkinManager().loadProfileTextures(this.gameProfile, (SkinManager.SkinAvailableCallback) (typeIn, location, profileTexture) -> {
                    switch (typeIn)
                    {
                        case SKIN:
                            LiveSkinUtil.this.playerTextures.put(MinecraftProfileTexture.Type.SKIN, location);
                            LiveSkinUtil.this.skinType = profileTexture.getMetadata("model");

                            if (LiveSkinUtil.this.skinType == null)
                            {
                                LiveSkinUtil.this.skinType = "default";
                            }

                            break;
                        case CAPE:
                            LiveSkinUtil.this.playerTextures.put(MinecraftProfileTexture.Type.CAPE, location);
                            break;
                        case ELYTRA:
                            LiveSkinUtil.this.playerTextures.put(MinecraftProfileTexture.Type.ELYTRA, location);
                    }
                }, false); // The difference between this and NetworkPlayerInfo is that this here is false;
            }
        }
    }
}

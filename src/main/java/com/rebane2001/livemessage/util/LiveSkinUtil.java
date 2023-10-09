package com.rebane2001.livemessage.util;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.*;

/**
 * This class is pretty much NetworkPlayerInfo with the requireSecure value set to false.
 */
public class LiveSkinUtil {
    Map<MinecraftProfileTexture.Type, Identifier> playerTextures = Maps.newEnumMap(MinecraftProfileTexture.Type.class);
    private boolean playerTexturesLoaded = false;
    private SkinTextures.Model skinType;
    private GameProfile gameProfile;

    public LiveSkinUtil(UUID uuid){
        // TODO: Error handling?
        ProfileResult profileResult = MinecraftClient.getInstance().getSessionService().fetchProfile(uuid, false);
        gameProfile = profileResult != null ? profileResult.profile() : new GameProfile(uuid, "N/A");
        // TODO: Remove
        // gameProfile = Minecraft.getMinecraft().getSessionService().fillProfileProperties(new GameProfile(uuid,null), false);
    }

    public boolean hasLocationSkin()
    {
        return this.getLocationSkin() != null;
    }

    public SkinTextures.Model getSkinType()
    {
        return Optional.ofNullable(this.skinType).orElse(SkinTextures.Model.WIDE);
    }

    public boolean customSkinLoaded(){
        return this.playerTextures.get(SKIN) != null;
    }

    public Identifier getLocationSkin()
    {
        this.loadPlayerTextures();
        return MoreObjects.firstNonNull(this.playerTextures.get(SKIN), DefaultSkinHelper.getSkinTextures(this.gameProfile.getId()).texture());
    }

    @Nullable
    public Identifier getLocationCape()
    {
        this.loadPlayerTextures();
        return this.playerTextures.get(CAPE);
    }

    /**
     * Gets the special Elytra texture for the player.
     */
    @Nullable
    public Identifier getLocationElytra()
    {
        this.loadPlayerTextures();
        return this.playerTextures.get(ELYTRA);
    }

    protected void loadPlayerTextures()
    {
        synchronized (this)
        {
            if (!this.playerTexturesLoaded)
            {
                this.playerTexturesLoaded = true;
                MinecraftClient.getInstance().getSkinProvider().fetchSkinTextures(this.gameProfile).thenAccept(skinTextures -> {
                    LiveSkinUtil.this.playerTextures.put(SKIN, skinTextures.texture());
                    LiveSkinUtil.this.playerTextures.put(CAPE, skinTextures.capeTexture());
                    LiveSkinUtil.this.playerTextures.put(ELYTRA, skinTextures.elytraTexture());
                    LiveSkinUtil.this.skinType = skinTextures.model();
                });

                // TODO: delete
/*
                Minecraft.getMinecraft().getSkinManager().loadProfileTextures(this.gameProfile, (SkinManager.SkinAvailableCallback) (typeIn, location, profileTexture) -> {
                    switch (typeIn)
                    {
                        case SKIN:
                            LiveSkinUtil.this.playerTextures.put(SKIN, location);
                            LiveSkinUtil.this.skinType = profileTexture.getMetadata("model");

                            if (LiveSkinUtil.this.skinType == null)
                            {
                                LiveSkinUtil.this.skinType = "default";
                            }

                            break;
                        case CAPE:
                            LiveSkinUtil.this.playerTextures.put(CAPE, location);
                            break;
                        case ELYTRA:
                            LiveSkinUtil.this.playerTextures.put(ELYTRA, location);
                    }
                }, false); // The difference between this and NetworkPlayerInfo is that this here is false;*/
            }
        }
    }
}

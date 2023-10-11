package com.rebane2001.livemessage.util;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.rebane2001.livemessage.Livemessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.*;

/**
 * This class is pretty much NetworkPlayerInfo with the requireSecure value set to false.
 */
public class LiveSkinUtil {
    Map<MinecraftProfileTexture.Type, Identifier> playerTextures = Maps.newEnumMap(MinecraftProfileTexture.Type.class);
    private boolean playerTexturesLoaded = false;
    private SkinTextures.Model skinType;
    private GameProfile gameProfile;

    public LiveSkinUtil(LiveProfileCache.LiveProfile liveProfile) {
        gameProfile = new GameProfile(liveProfile.uuid, liveProfile.username != null ? liveProfile.username : "N/A");
        CompletableFuture
                .runAsync(() -> {
                    GameProfile gameProfileReal = LiveProfileCache.loadGameProfile(liveProfile);
                    if (gameProfileReal != null) {
                        synchronized (this) {
                            gameProfile = gameProfileReal;
                            playerTexturesLoaded = false;
                        }
                    }
                });
    }

    public boolean hasLocationSkin() {
        return this.getLocationSkin() != null;
    }

    public SkinTextures.Model getSkinType() {
        return Optional.ofNullable(this.skinType).orElse(SkinTextures.Model.WIDE);
    }

    public boolean customSkinLoaded() {
        return this.playerTextures.get(SKIN) != null;
    }

    public Identifier getLocationSkin() {
        this.loadPlayerTextures();
        return MoreObjects.firstNonNull(this.playerTextures.get(SKIN), DefaultSkinHelper.getSkinTextures(this.gameProfile.getId()).texture());
    }

    @Nullable
    public Identifier getLocationCape() {
        this.loadPlayerTextures();
        return this.playerTextures.get(CAPE);
    }

    /**
     * Gets the special Elytra texture for the player.
     */
    @Nullable
    public Identifier getLocationElytra() {
        this.loadPlayerTextures();
        return this.playerTextures.get(ELYTRA);
    }

    protected void loadPlayerTextures() {
        synchronized (this) {
            if (!this.playerTexturesLoaded) {
                this.playerTexturesLoaded = true;
                Livemessage.LOGGER.info("Loading da skin");
                MinecraftClient.getInstance().getSkinProvider().fetchSkinTextures(this.gameProfile).thenAccept(skinTextures -> {
                    LiveSkinUtil.this.playerTextures.put(SKIN, skinTextures.texture());
                    LiveSkinUtil.this.playerTextures.put(CAPE, skinTextures.capeTexture());
                    LiveSkinUtil.this.playerTextures.put(ELYTRA, skinTextures.elytraTexture());
                    LiveSkinUtil.this.skinType = skinTextures.model();
                });
            }
        }
    }
}

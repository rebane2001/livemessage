package com.rebane2001.livemessage.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.rebane2001.livemessage.Livemessage;
import com.rebane2001.livemessage.gui.LivemessageGui;
import com.rebane2001.livemessage.gui.ManeWindow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class LiveProfileCache {

    public static Map<UUID, LiveProfile> cachedProfiles = new HashMap<>();
    public static Map<String, UUID> cachedNames = new HashMap<>();
    public static Map<String, UUID> weakCachedNames = new HashMap<>();
    public static Set<UUID> badUUIDs = new HashSet<>();
    public static Set<String> badNames = new HashSet<>();

    //TODO: Implement offline mode users
    public static class LiveProfile {
        public UUID uuid;
        public String username;
        public ArrayList<String> pastNames;
        public boolean weakCache;
        public PdbUtil.PdbData pdbData;
        public GameProfile gameProfile;

        public LiveProfile(UUID uuid) {
            this.uuid = uuid;
            this.pdbData = new PdbUtil.PdbData();
        }
        public LiveProfile(UUID uuid, String username) {
            this(uuid);
            this.username = username;
        }
    }

    private static Pattern usernamePattern = Pattern.compile("^\\w{3,16}$");
    private static String usernamePatternStr = "^\\w{3,16}$";

    private static LiveProfile banUUID(UUID uuid) {
        badUUIDs.add(uuid);
        return null;
    }

    private static LiveProfile banName(String name) {
        badNames.add(name);
        return null;
    }

    public static UUID safeUUIDFromString(String uuidstr) {
        uuidstr = uuidstr.replaceAll("-", "");
        BigInteger bi1 = new BigInteger(uuidstr.substring(0, 16), 16);
        BigInteger bi2 = new BigInteger(uuidstr.substring(16, 32), 16);
        return new UUID(bi1.longValue(), bi2.longValue());
    }


    private static UUID pollUUID(String username) {
        Livemessage.LOGGER.info("Looking up: " + username);
        try {
            JsonObject object =
                    null;
            object = LivemessageUtil.getResources(
                    new URL(
                            "https://api.mojang.com/users/profiles/minecraft/"
                                    + username),
                    "GET")
                    .getAsJsonObject();

            String uuidstring = object.get("id").getAsString();
            return safeUUIDFromString(uuidstring);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void preloadProfiles(){
        Set<UUID> savedUUIDs = new HashSet<>();
        savedUUIDs.addAll(LivemessageGui.chats);
        savedUUIDs.addAll(LivemessageGui.friends);
        savedUUIDs.addAll(LivemessageGui.blocked);

        ExecutorService executor = Executors.newFixedThreadPool(8);
        for (UUID uuid : savedUUIDs) {
            executor.submit(() -> getLiveprofileFromUUID(uuid,true));
        }
    }

    private static UUID getUUIDFromTab(String username) {
        try {
            PlayerListEntry playerListEntry = MinecraftClient.getInstance().player.networkHandler.getPlayerListEntry(username);
            if (playerListEntry != null)
                return playerListEntry.getProfile().getId();
        } catch (Exception ignored) {}
        return null;
    }

    private static String getUsernameFromTab(UUID uuid) {
        try {
            PlayerListEntry playerListEntry = MinecraftClient.getInstance().player.networkHandler.getPlayerListEntry(uuid);
            if (playerListEntry != null)
                return playerListEntry.getProfile().getName();
        } catch (Exception ignored) {}
        return null;
    }

    // TODO: ratelimit for invalid profiles? (does minecraft itself implement this limit?)
    public static GameProfile loadGameProfile(LiveProfile liveProfile) {
        if (liveProfile.gameProfile == null) {
            Livemessage.LOGGER.warn("Loading game profile...");
            long l = System.currentTimeMillis();
            ProfileResult profileResult = MinecraftClient.getInstance().getSessionService().fetchProfile(liveProfile.uuid, false);
            if (profileResult != null)
                liveProfile.gameProfile = profileResult.profile();
            Livemessage.LOGGER.warn("Loaded game profile in " + (System.currentTimeMillis()-l) + "ms (null == " + (liveProfile.gameProfile == null) + ")");
            if (liveProfile.gameProfile != null) {
                liveProfile.username = liveProfile.gameProfile.getName();
                liveProfile.uuid = liveProfile.gameProfile.getId();
                cachedNames.put(liveProfile.username.toLowerCase(Locale.ROOT), liveProfile.uuid);
                liveProfile.weakCache = false;
            }
        }
        return liveProfile.gameProfile;
    }

    // Weak means we can use the cached username saved on disk
    public static LiveProfile getLiveprofileFromUUID(UUID uuid, boolean weak) {
        if (cachedProfiles.containsKey(uuid)) {
            LiveProfile cachedProfile = cachedProfiles.get(uuid);
            if (!cachedProfile.weakCache || weak)
                return cachedProfile;
        }
        if (badUUIDs.contains(uuid))
            return null;
        ConfigUtil.ChatSettings chatSettings = ConfigUtil.getChatSettings(uuid);
        LiveProfile liveProfile = new LiveProfile(uuid, getUsernameFromTab(uuid));

        if (weak && liveProfile.username == null && chatSettings.lastName != null) {
            liveProfile.weakCache = true;
            liveProfile.username = chatSettings.lastName;
            weakCachedNames.put(liveProfile.username.toLowerCase(Locale.ROOT), uuid);
        }

        if (liveProfile.username == null) {
            loadGameProfile(liveProfile);
            if (liveProfile.username == null)
                return banUUID(uuid);
            liveProfile.weakCache = false;
            // TODO: If name different from cached then update name history
            if (!liveProfile.username.equals(chatSettings.lastName)) {
                // Cache username for the future
                chatSettings.lastName = liveProfile.username;
                ConfigUtil.saveChatSettings(liveProfile.uuid, chatSettings);
            }
            cachedNames.put(liveProfile.username.toLowerCase(Locale.ROOT), uuid);
        }

        cachedProfiles.put(uuid, liveProfile);
        return liveProfile;
    }

    /**
     * Quickly gets a UUID from a name, but might be outdated.
     * Returns instantly, doesn't perform network requests or IO.
     */
    public static UUID quickUUIDFromName(String username) {
        UUID uuid = getUUIDFromTab(username);
        if (uuid != null) return uuid;
        username = username.toLowerCase(Locale.ROOT);
        uuid = cachedNames.getOrDefault(username, weakCachedNames.get(username));
        return uuid;
    }

    public static LiveProfile getLiveprofileFromName(String username) {
        username = username.toLowerCase(Locale.ROOT);
        if (cachedNames.containsKey(username))
            return cachedProfiles.get(cachedNames.get(username));
        if (badNames.contains(username))
            return null;
        if (!username.matches(usernamePatternStr))
            return banName(username);
        UUID uuid = getUUIDFromTab(username);
        if (uuid == null)
            uuid = pollUUID(username);
        if (uuid == null)
            return banName(username);
        return getLiveprofileFromUUID(uuid, false);
    }

}

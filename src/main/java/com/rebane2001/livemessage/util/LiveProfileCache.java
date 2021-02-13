package com.rebane2001.livemessage.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.rebane2001.livemessage.gui.LivemessageGui;
import com.rebane2001.livemessage.gui.ManeWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class LiveProfileCache {

    public static Map<UUID, LiveProfile> cachedProfiles = new HashMap<>();
    public static Map<UUID, LiveProfile> weakCachedProfiles = new HashMap<>();
    public static Map<String, UUID> cachedNames = new HashMap<>();
    public static Set<UUID> badUUIDs = new HashSet<>();
    public static Set<String> badNames = new HashSet<>();

    //TODO: Implement offline mode users
    public static class LiveProfile {
        public UUID uuid;
        public String username;
        public ArrayList<String> pastNames;
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

    private static ArrayList<String> pollPastNames(UUID uuid) {
        System.out.println("[Livemessage] Looking up: " + uuid.toString());
        ArrayList<String> pastNames = new ArrayList<String>();
        try {
            JsonArray array =
                    null;
            array = LivemessageUtil.getResources(
                    new URL(
                            "https://api.mojang.com/user/profiles/"
                                    + uuid.toString()
                                    + "/names"),
                    "GET")
                    .getAsJsonArray();

            for (JsonElement e : array) {
                JsonObject node = e.getAsJsonObject();
                String name = node.get("name").getAsString();
                long changedAt = node.has("changedToAt") ? node.get("changedToAt").getAsLong() : 0;
                pastNames.add(0, name);
            }
            /*
            for (String name : pastNames) {
                longestPastName = Math.max(fontRenderer.getStringWidth(name), longestPastName);
            }
            */
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return pastNames;
    }

    private static UUID pollUUID(String username) {
        System.out.println("[Livemessage] Looking up: " + username);
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
            executor.submit(() -> getLiveprofileFromUUID(uuid,false));
        }


        /*
        ExecutorService fixedPool = Executors.newFixedThreadPool(8);


        Runnable runnableTask = (UUID z) -> {
            try {
                System.out.println("Checking XXX");
                LiveProfile x = getLiveprofileFromUUID(new UUID(123 ,123),false);
                System.out.println("Got " + x.username);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        fixedPool.execute(runnableTask);*/
    }

    public static LiveProfile forceloadNameHistory(LiveProfile liveProfile) {
        if (cachedProfiles.get(liveProfile.uuid).pastNames != null)
            return cachedProfiles.get(liveProfile.uuid);
        liveProfile.pastNames = pollPastNames(liveProfile.uuid);
        cachedProfiles.put(liveProfile.uuid, liveProfile);
        return liveProfile;
    }

    private static UUID getUUIDFromTab(String username) {
        try {
            NetHandlerPlayClient nethandlerplayclient = Minecraft.getMinecraft().player.connection;
            List<NetworkPlayerInfo> list = new ArrayList<>(nethandlerplayclient.getPlayerInfoMap());

            for (int i = 0; i < list.size(); ++i) {
                GameProfile gameProfile = list.get(i).getGameProfile();
                if (gameProfile.getName().equals(username))
                    return gameProfile.getId();
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static String getUsernameFromTab(UUID uuid) {
        try {
            NetHandlerPlayClient nethandlerplayclient = Minecraft.getMinecraft().player.connection;
            List<NetworkPlayerInfo> list = new ArrayList<>(nethandlerplayclient.getPlayerInfoMap());

            for (int i = 0; i < list.size(); ++i) {
                GameProfile gameProfile = list.get(i).getGameProfile();
                if (gameProfile.getId().equals(uuid))
                    return gameProfile.getName();
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static LiveProfile getLiveprofileFromUUID(UUID uuid, boolean weak) {
        System.out.println("Looking up " + uuid.toString());
        if (cachedProfiles.containsKey(uuid))
            return cachedProfiles.get(uuid);
        if (weakCachedProfiles.containsKey(uuid) && weak)
            return weakCachedProfiles.get(uuid);
        if (badUUIDs.contains(uuid))
            return null;
        LiveProfile liveProfile = new LiveProfile();
        liveProfile.uuid = uuid;
        liveProfile.username = getUsernameFromTab(uuid);
        if (liveProfile.username == null) {
            liveProfile.pastNames = pollPastNames(uuid);
            if (liveProfile.pastNames == null)
                return banUUID(uuid);
            liveProfile.username = liveProfile.pastNames.get(0);
        }
        cachedProfiles.put(uuid, liveProfile);
        cachedNames.put(liveProfile.username.toLowerCase(Locale.ROOT), uuid);
        System.out.println("Got " + liveProfile.username);
        return liveProfile;
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

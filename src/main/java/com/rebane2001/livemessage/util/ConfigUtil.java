package com.rebane2001.livemessage.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.rebane2001.livemessage.Livemessage;
import com.rebane2001.livemessage.gui.ChatWindow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.io.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ConfigUtil {
    public static Path modFolder;

    public static String getModPath(String path) {
        // TODO: Maybe we wanna resolve or do this in some other way
        return String.valueOf(modFolder.resolve(path));
    }

    /**
     * Creates relevant files and folders if they don't exist.
     */
    public static void initFiles() {
        modFolder = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/livemessage");

        String[] directories = new String[] {
                getModPath("messages"),
                getModPath("settings"),
                getModPath("patterns"),
                getModPath("corrupted"),
                getModPath("backups"),
        };
        for (String directoryName : directories) {
            File directory = new File(directoryName);
            if (directory.mkdirs())
                Livemessage.LOGGER.info("Created folder " + directory);
        }
        String[] files = new String[] {
                getModPath("patterns/toPatterns.txt"),
                getModPath("patterns/fromPatterns.txt"),
        };
        for (String filename : files) {
            File file = new File(filename);
            try {
                if (file.createNewFile())
                    Livemessage.LOGGER.info("Created file " + file);
            } catch (IOException e) {
                Livemessage.LOGGER.error("Couldn't create file " + file);
            }
        }
    }

    public static void loadChatHistory(UUID uuid, List<ChatWindow.ChatMessage> chatHistory) {
        Gson gson = new Gson();
        try (BufferedReader reader = new BufferedReader(new FileReader(getModPath("messages/" + uuid.toString() + ".jsonl")))) {
            String line = reader.readLine();
            int lineNumber = 1;
            while (line != null) {
                try {
                    chatHistory.add(gson.fromJson(line, ChatWindow.ChatMessage.class));
                } catch (Exception e) {
                    e.printStackTrace();
                    Livemessage.LOGGER.error("Error loading line " + lineNumber + " of chat history with " + uuid);
                }
                lineNumber++;
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            Livemessage.LOGGER.info("Chat history for " + uuid + " not found");
        } catch (IOException e) {
            e.printStackTrace();
            Livemessage.LOGGER.error("IO error while loading chat history with " + uuid);
        }
    }

    public static void appendChatHistory(UUID uuid, String message, boolean sentByMe) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(getModPath("messages/" + uuid.toString() + ".jsonl"), true))) {
            Gson gson = new Gson();
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            bw.write(gson.toJson(new ChatWindow.ChatMessage(message, sentByMe, System.currentTimeMillis(), player != null ? player.getUuid() : null)));
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ChatSettings{
        public boolean isFriend = false;
        public boolean isBlocked = false;
        public int customColor = 0;
        public String lastName;
    }

    public static ChatSettings getChatSettings(UUID uuid){
        String settingsPath = getModPath("settings/" + uuid + ".json");
        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(settingsPath));
            return gson.fromJson(reader, ChatSettings.class);
        } catch (FileNotFoundException e) {
            Livemessage.LOGGER.info("Chat settings for " + uuid + " not found");
        } catch (Exception e) {
            e.printStackTrace();
            Livemessage.LOGGER.error("Chat settings for " + uuid + " could not be loaded, renaming and resetting");
            if (!new File(settingsPath).renameTo(new File(getModPath("corrupted/" + uuid + "-settings-" + (System.currentTimeMillis()/1000L) + ".json"))))
                Livemessage.LOGGER.error("Rename failed");
        }
        return new ChatSettings();
    }

    public static void saveChatSettings(UUID uuid, ChatSettings chatSettings){
        try (Writer writer = new FileWriter(
                getModPath("settings/" + uuid + ".json")
        )) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(chatSettings, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadBuddies(List<UUID> chats, List<UUID> friends, List<UUID> blocked) {
        friends.clear();
        chats.clear();
        blocked.clear();

        File folder;
        folder = new File(getModPath("settings"));
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile()) {
                UUID uuid = UUID.fromString(file.getName().substring(0, 36));
                ChatSettings chatSettings = getChatSettings(uuid);
                if (chatSettings.isFriend)
                    friends.add(uuid);
                if (chatSettings.isBlocked)
                    blocked.add(uuid);
            }
        }

        // Look at all message histories
        folder = new File(getModPath("messages"));
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile()) {
                UUID uuid = UUID.fromString(file.getName().substring(0, 36));
                chats.add(uuid);
            }
        }

        // Sort lists for consistency (even tho by UUID)
        Collections.sort(friends);
        Collections.sort(chats);
        Collections.sort(blocked);
    }
}

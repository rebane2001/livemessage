package com.rebane2001.livemessage.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.rebane2001.livemessage.Livemessage;
import net.minecraft.client.Minecraft;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Pattern;

public class LivemessageUtil {

    public static final Pattern fromPatterns[] = {
            Pattern.compile("^(\\w{3,16}) whispers: (.*)"),
            Pattern.compile("^\\[(\\w{3,16}) -> me\\] (.*)"),
            Pattern.compile("^(\\w{3,16}) whispers to you: (.*)")
    };

    public static final Pattern toPatterns[] = {
            Pattern.compile("^To (\\w{3,16}): (.*)"),
            Pattern.compile("^to (\\w{3,16}): (.*)"),
            Pattern.compile("^\\[me -> (\\w{3,16})\\] (.*)"),
            Pattern.compile("^You whisper to (\\w{3,16}): (.*)")

    };

    public static class ChatSettings{
        public boolean isFriend = false;
        public boolean isBlocked = false;
        public int customColor = 0;
    }

    public static ChatSettings getChatSettings(UUID uuid){
        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(
                    new FileReader(
                            String.valueOf(Livemessage.modFolder.resolve("settings/" + uuid.toString() + ".json"))
                    )
            );
            return gson.fromJson(reader, ChatSettings.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ChatSettings();
    }

    public static void saveChatSettings(UUID uuid, ChatSettings chatSettings){
        try (Writer writer = new FileWriter(
                String.valueOf(Livemessage.modFolder.resolve("settings/" + uuid.toString() + ".json"))
        )) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(chatSettings, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JsonElement getResources(URL url, String request) throws IOException {
        return getResources(url, request, null);
    }

    public static JsonElement getResources(URL url, String request, JsonElement element)
            throws IOException {
        Gson GSON = new Gson();
        JsonParser PARSER = new JsonParser();
        JsonElement data;
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(request);
            connection.setRequestProperty("Content-Type", "application/json");

            if (element != null) {
                DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                output.writeBytes(GSON.toJson(element));
                output.close();
            }

            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder builder = new StringBuilder();
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
                builder.append('\n');
            }
            scanner.close();

            String json = builder.toString();
            data = PARSER.parse(json);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return data;
    }

    /**
     * Is this player online?
     */
    public static boolean checkOnlineStatus(UUID uuid) {
        // IntelliJ says the value below will never be null
        // DO NOT LISTEN TO HIS LIES
        // Letting IntelliJ "fix" the code below will result in pain and misery
        // DO NOT LISTEN TO HER LIES
        try {
            return Minecraft.getMinecraft().player.connection.getPlayerInfo(uuid) != null;
        } catch (Exception e) {
            return false;
        }
    }

}

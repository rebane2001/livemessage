package com.rebane2001.livemessage.gui;

import com.google.gson.Gson;
import com.rebane2001.livemessage.Livemessage;
import com.rebane2001.livemessage.LivemessageConfig;
import com.rebane2001.livemessage.util.LiveProfileCache;
import com.rebane2001.livemessage.util.LivemessageUtil;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class LivemessageGui extends GuiScreen {

    public LivemessageGui() {
    }

    public static boolean buddiesLoaded = false;
    public static List<UUID> friends = new ArrayList<>();
    public static List<UUID> chats = new ArrayList<>();
    public static List<UUID> blocked = new ArrayList<>();
    public static Map<UUID, Integer> unreadMessages = new HashMap<>();

    public static List<LiveWindow> liveWindows = new ArrayList<>();

    public static double sclOrig = 1;
    public static double scl = 1;
    public static int screenHeight = 1;
    public static int screenWidth = 1;

    // For handling buttons from LiveWindows
    // Unused right now
    public static void handleBtn(int action) {
        switch (action) {
            case 0:
                liveWindows.get(liveWindows.size() - 1).deactivateWindow();
                liveWindows.add(new LiveWindow());
                break;
        }
    }

    /**
     * Loads users into respective lists.
     */
    public static void loadBuddies() {
        friends.clear();
        chats.clear();
        blocked.clear();
        buddiesLoaded = true;

        // Read all config files
        File folder = new File(String.valueOf(Livemessage.modFolder.resolve("settings/")));
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                UUID uuid = UUID.fromString(file.getName().substring(0, 36));
                LivemessageUtil.ChatSettings chatSettings = LivemessageUtil.getChatSettings(uuid);
                if (chatSettings.isFriend)
                    friends.add(uuid);
                if (chatSettings.isBlocked)
                    blocked.add(uuid);
            }
        }
        // Look at all message histories
        folder = new File(String.valueOf(Livemessage.modFolder.resolve("messages/")));
        listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
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

    /**
     * Sets the primary color of the window.
     */
    public static class BuddySettings {
        long lastMessage;
    }

    public static void markAllAsRead() {
        unreadMessages.clear();
    }

    public void setScl() {
        final ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        scl = scaledresolution.getScaleFactor() / LivemessageConfig.otherSettings.guiScale;

        screenHeight = (int) (scaledresolution.getScaledHeight_double() * scl);
        screenWidth = (int) (scaledresolution.getScaledWidth_double() * scl);
    }

    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        this.buttonList.clear();

        setScl();

        loadBuddies();

        if (liveWindows.isEmpty())
            liveWindows.add(new ManeWindow());
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    // Use this to open a new window or highlight existing one
    public static void openChatWindow(final UUID uuid) {
        if (uuid == null)
            return;
        liveWindows.get(liveWindows.size() - 1).deactivateWindow();
        // Look for existing window
        for (LiveWindow liveWindow : LivemessageGui.liveWindows) {
            if (!(liveWindow instanceof ChatWindow)) {
                continue;
            }
            final ChatWindow chatWindow = (ChatWindow) liveWindow;
            if (chatWindow.liveProfile.uuid.equals(uuid)) {
                chatWindow.activateWindow();
                LivemessageGui.liveWindows.removeIf(it -> it == chatWindow);
                LivemessageGui.liveWindows.add(chatWindow);
                return;
            }
        }
        // If existing window didn't exist, add a new one
        LivemessageGui.addChatWindow(new ChatWindow(uuid));
    }

    // Safe chatwindow adding
    private static void addChatWindow(ChatWindow chatWindow) {
        if (chatWindow.valid) {
            liveWindows.add(chatWindow);
        } else {
            liveWindows.get(liveWindows.size() - 1).activateWindow();
        }
    }

    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                //Do button stuff
                break;
        }
    }

    public void handleMouseInput() throws IOException {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        liveWindows.get(liveWindows.size() - 1).mouseMove((int) (mouseX * scl), (int) (mouseY * scl));
        int mWheelState = Mouse.getEventDWheel();
        if (mWheelState != 0)
            liveWindows.get(liveWindows.size() - 1).mouseWheel(mWheelState);

        super.handleMouseInput();
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (int i = liveWindows.size() - 1; i >= 0; i--) {
            LiveWindow liveWindow = liveWindows.get(i);
            if (liveWindow.mouseInWindow((int) (mouseX * scl), (int) (mouseY * scl))) {
                if (i != liveWindows.size() - 1) {
                    liveWindows.get(liveWindows.size() - 1).deactivateWindow();
                    liveWindow.activateWindow();
                    liveWindows.remove(i);
                    liveWindows.add(liveWindow);
                }
                break;
            }
        }
        liveWindows.get(liveWindows.size() - 1).mouseClicked((int) (mouseX * scl), (int) (mouseY * scl), mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        liveWindows.get(liveWindows.size() - 1).mouseReleased((int) (mouseX * scl), (int) (mouseY * scl), state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        liveWindows.get(liveWindows.size() - 1).mouseClickMove((int) (mouseX * scl), (int) (mouseY * scl), clickedMouseButton, timeSinceLastClick);
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        liveWindows.get(liveWindows.size() - 1).keyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    public static boolean newMessage(String username, String message, boolean sentByMe) {
        final UUID uuid = LiveProfileCache.getLiveprofileFromName(username).uuid;
        boolean doHide = false;
        if (uuid != null) {
            if (!chats.contains(uuid))
                loadBuddies();
            if (!sentByMe) {
                // Increase unread messages counter
                unreadMessages.put(uuid, unreadMessages.getOrDefault(uuid, 0) + 1);
                // If settings allow, show a toast notification
                if ((!blocked.contains(uuid) || LivemessageConfig.notificationSettings.toastsFromBlocked) && ((friends.contains(uuid) && LivemessageConfig.notificationSettings.toastsFromFriends) || (!friends.contains(uuid) && chats.contains(uuid) && LivemessageConfig.notificationSettings.toastsFromChats)))
                    LiveHud.addToast("DM from " + username, message);
                // If settings allow, play a notification sound
                if ((!blocked.contains(uuid) || LivemessageConfig.notificationSettings.soundsFromBlocked) && ((friends.contains(uuid) && LivemessageConfig.notificationSettings.soundsFromFriends) || (!friends.contains(uuid) && chats.contains(uuid) && LivemessageConfig.notificationSettings.soundsFromChats)))
                    LiveHud.playNotificationSound();
            } else {
                if (LivemessageConfig.otherSettings.readOnReply)
                    LivemessageGui.unreadMessages.put(uuid, 0);
            }
            // If settings define, hide DM messages from main chat
            if ((blocked.contains(uuid) && LivemessageConfig.hideSettings.hideFromBlocked) || (friends.contains(uuid) && LivemessageConfig.hideSettings.hideFromFriends) || (chats.contains(uuid) && LivemessageConfig.hideSettings.hideFromChats))
                doHide = true;
            // Write message to chat history jsonl
            try {
                Gson gson = new Gson();
                FileWriter fw = new FileWriter(String.valueOf(Livemessage.modFolder.resolve("messages/" + uuid.toString() + ".jsonl")), true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(gson.toJson(new ChatWindow.ChatMessage(message, sentByMe, System.currentTimeMillis())));
                bw.newLine();
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Send message to open window
        for (LiveWindow liveWindow : liveWindows) {
            if (!(liveWindow instanceof ChatWindow))
                continue;
            ChatWindow chatWindow = (ChatWindow) liveWindow;
            if (username.equals(chatWindow.liveProfile.username)) {
                chatWindow.chatHistory.add(new ChatWindow.ChatMessage(message, sentByMe, System.currentTimeMillis()));
                if (chatWindow.chatScrolledToBottom)
                    chatWindow.chatScrollPosition += 1;
                break;
            }
        }
        return doHide;
    }

    @Override
    public void drawScreen(int x, int y, float f) {
        setScl();
        float reverseGuiScale = (float) (1f / scl * 1);
        GlStateManager.pushMatrix();
        GlStateManager.scale(reverseGuiScale, reverseGuiScale, reverseGuiScale);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glLineWidth(1);
        for (LiveWindow liveWindow : liveWindows) {
            liveWindow.preDrawWindow();
        }
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.popMatrix();
        super.drawScreen(x, y, f);
    }
}

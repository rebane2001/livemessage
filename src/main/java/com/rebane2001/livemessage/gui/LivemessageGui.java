package com.rebane2001.livemessage.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.rebane2001.livemessage.LivemessageConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.*;
import java.util.List;

public class LivemessageGui extends Screen {

    private final Screen parent;

    public LivemessageGui(Screen parent) {
        super(Text.literal("Livemessage"));
        this.parent = parent;
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
        // todo: reimplement
        friends.clear();
        chats.clear();
        blocked.clear();
        buddiesLoaded = true;

        /*
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
         */
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
        // TODO: Reimplement
        /*
        final ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        scl = scaledresolution.getScaleFactor() / (float)LivemessageConfig.guiSettings.guiScale;

        screenHeight = (int) (scaledresolution.getScaledHeight_double() * scl);
        screenWidth = (int) (scaledresolution.getScaledWidth_double() * scl);

         */
    }

    @Override
    public void init() {
        setScl();

        loadBuddies();

        if (liveWindows.isEmpty())
            liveWindows.add(new ManeWindow());
    }

    @Override
    public void close() {
        // TODO: ???
        // Keyboard.enableRepeatEvents(false);
        if (client != null)
            client.setScreen(parent);
    }

    // Use this to open a new window or highlight existing one
    // TODO: reimplement
    public static void openChatWindow(final UUID uuid) {
        if (uuid == null)
            return;
        /*
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
         */
    }

    // Safe chatwindow adding
    // TODO: reimplement
    /*
    private static void addChatWindow(ChatWindow chatWindow) {
        if (chatWindow.valid) {
            liveWindows.add(chatWindow);
        } else {
            liveWindows.get(liveWindows.size() - 1).activateWindow();
        }
    }
     */

    // TODO: reimplement
    /*
    public void handleMouseInput() throws IOException {
        int mouseX = getMouse().getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height -  getMouse().getEventY() * this.height / this.mc.displayHeight - 1;
        liveWindows.get(liveWindows.size() - 1).mouseMove((int) (mouseX * scl), (int) (mouseY * scl));
        int mWheelState =  getMouse().getEventDWheel();
        if (mWheelState != 0)
            liveWindows.get(liveWindows.size() - 1).mouseWheel(mWheelState);

        super.handleMouseInput();
    }

     */

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
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
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        liveWindows.get(liveWindows.size() - 1).mouseReleased((int) (mouseX * scl), (int) (mouseY * scl), button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    // protected void mouseClickMove(double mouseX, double mouseY, int clickedMouseButton, long timeSinceLastClick) {
    //     liveWindows.get(liveWindows.size() - 1).mouseClickMove((int) (mouseX * scl), (int) (mouseY * scl), clickedMouseButton, timeSinceLastClick);
    // }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // TODO: reimplement
        // liveWindows.get(liveWindows.size() - 1).keyTyped(typedChar, keyCode);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public static boolean newMessage(String username, String message, boolean sentByMe) {
        // TODO: reimplement
        // final UUID uuid = LiveProfileCache.getLiveprofileFromName(username).uuid;
        final UUID uuid = MinecraftClient.getInstance().player.getUuid();
        boolean doHide = false;
        if (uuid != null) {
            // Write message to chat history jsonl
            // TODO: reimplement
            /*
            try {
                Gson gson = new Gson();
                FileWriter fw = new FileWriter(String.valueOf(Livemessage.modFolder.resolve("messages/" + uuid.toString() + ".jsonl")), true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(gson.toJson(new ChatWindow.ChatMessage(message, sentByMe, System.currentTimeMillis(), Minecraft.getMinecraft().player.getUniqueID())));
                bw.newLine();
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
             */
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
        }
        // Send message to open window
        // TODO: reimplement
        /*
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
         */
        return doHide;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // TODO: Turn this into a setting
        // super.renderBackground(context, mouseX, mouseY, delta);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        setScl();
        // TODO: Make configurable and optional (-1 == scale with GUI?)
        scl = MinecraftClient.getInstance().getWindow().getScaleFactor();
        screenHeight = (int) (context.getScaledWindowHeight() * scl);
        screenWidth = (int) (context.getScaledWindowWidth() * scl);
        float reverseGuiScale = (float) (1f / scl * 1);
        context.getMatrices().push();
        context.getMatrices().scale(reverseGuiScale, reverseGuiScale, reverseGuiScale);
        for (LiveWindow liveWindow : liveWindows) {
            liveWindow.preDrawWindow(context);
        }
        context.getMatrices().pop();
    }
}

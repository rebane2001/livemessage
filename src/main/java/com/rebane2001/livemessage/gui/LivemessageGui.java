package com.rebane2001.livemessage.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.rebane2001.livemessage.LivemessageConfig;
import com.rebane2001.livemessage.util.ConfigUtil;
import com.rebane2001.livemessage.util.LiveProfileCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LivemessageGui extends Screen {

    private final Screen parent;

    public LivemessageGui(Screen parent) {
        super(Text.literal("Livemessage"));
        this.parent = parent;
    }

    public static List<UUID> friends = new ArrayList<>();
    public static List<UUID> chats = new ArrayList<>();
    public static List<UUID> blocked = new ArrayList<>();
    public static Map<UUID, Integer> unreadMessages = new HashMap<>();

    public final static List<LiveWindow> liveWindows = new ArrayList<>();

    public static double sclOrig = 1;
    public static double scl = 1;
    public static int screenHeight = 1;
    public static int screenWidth = 1;
    public static int loadingSomething = 0;

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
        CompletableFuture
                .supplyAsync(ConfigUtil::loadBuddies)
                .thenAccept(buddyLists -> {
                    chats = buddyLists[0];
                    friends = buddyLists[1];
                    blocked = buddyLists[2];
                });
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
    public static void openChatWindow(final UUID uuid) {
        if (uuid == null)
            return;
        if (!liveWindows.isEmpty())
            liveWindows.get(liveWindows.size() - 1).deactivateWindow();
        // Look for existing window
        for (LiveWindow liveWindow : LivemessageGui.liveWindows) {
            if (!(liveWindow instanceof final ChatWindow chatWindow)) {
                continue;
            }
            if (chatWindow.liveProfile.uuid.equals(uuid)) {
                chatWindow.activateWindow();
                LivemessageGui.liveWindows.removeIf(it -> it == chatWindow);
                LivemessageGui.liveWindows.add(chatWindow);
                return;
            }
        }
        // If existing window didn't exist, add a new one
        loadingSomething++;
        liveWindows.add(new LoadingWindow(uuid));
        CompletableFuture
                .runAsync(() -> {
                    // Do this in the background so we less laggy :)
                    try {
                        LiveProfileCache.LiveProfile liveProfile = LiveProfileCache.getLiveprofileFromUUID(uuid, false);
                        if (liveProfile != null)
                            LiveProfileCache.loadGameProfile(liveProfile);
                        LivemessageGui.addChatWindow(new ChatWindow(uuid));
                    } finally {
                        loadingSomething--;
                    }
                });

    }

    // Safe chatwindow adding
    private static void addChatWindow(ChatWindow chatWindow) {
        if (chatWindow.valid) {
            synchronized (liveWindows) {
                liveWindows.add(chatWindow);
                Optional<LiveWindow> loadingWindow = LivemessageGui.liveWindows.stream().filter(it -> it instanceof LoadingWindow && ((LoadingWindow)it).uuid.equals(chatWindow.liveProfile.uuid)).findFirst();
                if (loadingWindow.isPresent()) {
                    chatWindow.x = loadingWindow.get().x;
                    chatWindow.y = loadingWindow.get().y;
                    chatWindow.animateInStart = loadingWindow.get().animateInStart;
                }
                LivemessageGui.liveWindows.removeIf(it -> it instanceof LoadingWindow && ((LoadingWindow)it).uuid.equals(chatWindow.liveProfile.uuid));
            }
        } else {
            liveWindows.get(liveWindows.size() - 1).activateWindow();
        }
    }

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
        final LiveProfileCache.LiveProfile liveProfile = LiveProfileCache.getLiveprofileFromName(username);
        if (liveProfile == null) {
            return false;
        }
        final UUID uuid = liveProfile.uuid;
        // TODO: QUICKCHECK for blocked users, then continue in a separate thread for no lagspikes
        boolean doHide = false;
        if (uuid != null) {
            // Write message to chat history jsonl
            ConfigUtil.appendChatHistory(uuid, message, sentByMe);
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
        synchronized (liveWindows) {
            for (LiveWindow liveWindow : liveWindows) {
                liveWindow.preDrawWindow(context);
            }
        }
        context.getMatrices().pop();
    }
}

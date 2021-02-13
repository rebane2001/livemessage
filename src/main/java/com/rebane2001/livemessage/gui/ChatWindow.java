package com.rebane2001.livemessage.gui;

import com.google.gson.Gson;
import com.rebane2001.livemessage.Livemessage;
import com.rebane2001.livemessage.util.LiveProfileCache;
import com.rebane2001.livemessage.util.LiveProfileCache.LiveProfile;
import com.rebane2001.livemessage.util.LiveSkinUtil;
import com.rebane2001.livemessage.util.LivemessageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.rebane2001.livemessage.gui.GuiUtil.*;

public class ChatWindow extends LiveWindow {

    boolean valid;

    LiveProfile liveProfile;
    boolean pastNamesB = false;
    int longestPastName = 80;
    String msgString;

    final int scrollBarWidth = 10;
    int scrollBarHeight = 50;
    int chatScrollPosition = 0;
    boolean scrolling = false;
    public boolean chatScrolledToBottom = true;

    protected GuiTextField inputField;
    public LivemessageUtil.ChatSettings chatSettings;

    final int chatBoxY = titlebarHeight + 44;
    final int chatBoxX = 5;
    final int chatBoxSize = 13;

    List<ChatMessage> chatHistory = new ArrayList<>();

    LiveSkinUtil liveSkinUtil;
    QuintAnimation hatFade = new QuintAnimation(300, 1f);
    QuintAnimation fullSkinAnim = new QuintAnimation(600, 0f);

    /**
     * (the pony race not the human race).
     */
    int race = -1;
    final Map<Integer, String> races = new HashMap<Integer, String>() {{
        put(0xf9b131, "Earth pony");
        put(0xd19fe4, "Unicorn");
        put(0x88caf0, "Pegasus");
        put(0xfef9fc, "Alicorn");
        put(0xd0cccf, "Zebra");
        put(0x282b29, "Changeling");
        put(0xcaed5a, "Reformed Changeling");
        put(0xae9145, "Gryphon");
        put(0xd6ddac, "Hippogriff");
        put(0xfa88af, "Kirin");
        put(0xeeeeee, "Batpony");
        put(0x3655dd, "Seapony");
    }};

    public static class ChatMessage {
        public String message;
        public boolean sentByMe;
        public long timestamp;

        ChatMessage(String message, boolean sentByMe, long timestamp) {
            this.message = message;
            this.sentByMe = sentByMe;
            this.timestamp = timestamp;
        }
    }

    //TODO: Implement offline-mode players

    /**
     * Initialize ChatWindows with either the UUID or username of an user.
     */
    ChatWindow(UUID uuid) {
        this(LiveProfileCache.getLiveprofileFromUUID(uuid));
    }

    ChatWindow(String username) {
        this(LiveProfileCache.getLiveprofileFromName(username));
    }

    /**
     * Initializes ChatWindow.
     */
    public ChatWindow(LiveProfile liveProfile) {
        if (liveProfile == null) {
            System.out.println("[Livemessage] Tried to open an invalid chat window - offline mode?");
            valid = false;
            return;
        }
        valid = true;
        minw = 280;

        this.liveProfile = liveProfile;

        chatSettings = LivemessageUtil.getChatSettings(liveProfile.uuid);
        loadWindowColor();
        loadChatHistory();

        initButtons();

        liveSkinUtil = new LiveSkinUtil(liveProfile.uuid);

        msgString = "/msg " + liveProfile.username + " ";

        // Initialize text box
        this.inputField = new GuiTextField(0, this.fontRenderer, 9, this.h - 16, this.w - 18, 12);
        this.inputField.setMaxStringLength(256 - msgString.length());
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocused(true);
        this.inputField.setText("");
        this.inputField.setCanLoseFocus(false);
        this.inputField.setTextColor(getSingleRGB(255));

        // Hide text box cursor
        for (int j = 0; j < 6; j++)
            this.inputField.updateCursorCounter();

        chatScrollPosition = Math.max(chatHistory.size() - 6, 0);
        animateInStart = System.currentTimeMillis();
    }

    /**
     * Initializes buttons.
     */
    public void initButtons() {
        liveButtons.add(new LiveButton(0, 14, titlebarHeight + 3, 11, 11, true, "", "Add friend", () -> {
            toggleFriend();
        }));
        liveButtons.add(new LiveButton(1, 14, titlebarHeight + 3 + 13, 11, 11, true, "", "Block user", () -> {
            toggleBlock();
        }));
        liveButtons.add(new LiveButton(2, 14, titlebarHeight + 3 + 26, 11, 11, true, "", "Custom color", () -> {
            toggleColor();
        }));
    }


    public void toggleFriend() {
        chatSettings.isFriend = !chatSettings.isFriend;
        LivemessageUtil.saveChatSettings(liveProfile.uuid, chatSettings);
        LivemessageGui.loadBuddies();
    }

    public void toggleBlock() {
        chatSettings.isBlocked = !chatSettings.isBlocked;
        LivemessageUtil.saveChatSettings(liveProfile.uuid, chatSettings);
        LivemessageGui.loadBuddies();
    }

    public void toggleColor() {
        if (chatSettings.customColor > 0) {
            chatSettings.customColor = 0;
        } else {
            chatSettings.customColor = hslColor((float) Math.random(), (float) (0.6f + Math.random() * 0.15f), 0.5f);
        }
        LivemessageUtil.saveChatSettings(liveProfile.uuid, chatSettings);
        loadWindowColor();
    }

    /**
     * Sets the primary color of the window.
     */
    public void loadWindowColor() {
        primaryColor = GuiUtil.getWindowColor(liveProfile.uuid);
    }

    /**
     * Loads chat history from disk.
     */
    public void loadChatHistory() {
        Gson gson = new Gson();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    String.valueOf(Livemessage.modFolder.resolve("messages/" + liveProfile.uuid.toString() + ".jsonl"))));
            String line = reader.readLine();
            while (line != null) {
                try {
                    chatHistory.add(gson.fromJson(line, ChatMessage.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        markAsRead();
        if (keyCode != Keyboard.KEY_RETURN && keyCode != Keyboard.KEY_NUMPADENTER) {
            if (keyCode == Keyboard.KEY_UP) {
                //this.getSentHistory(-1);
            } else if (keyCode == Keyboard.KEY_DOWN) {
                //this.getSentHistory(1);
            } else if (keyCode == Keyboard.KEY_PRIOR) {
                chatScrollPosition = MathHelper.clamp(chatScrollPosition - 10, 0, Math.max(chatHistory.size() - 1, 0));
            } else if (keyCode == Keyboard.KEY_NEXT) {
                chatScrollPosition = MathHelper.clamp(chatScrollPosition + 10, 0, Math.max(chatHistory.size() - 1, 0));
            } else {
                this.inputField.textboxKeyTyped(typedChar, keyCode);
            }
        } else {
            String s = this.inputField.getText().trim();
            if (!s.isEmpty()) {
                CPacketChatMessage packet = new CPacketChatMessage(msgString + s);
                Minecraft.getMinecraft().getConnection().sendPacket(packet);
                this.inputField.setText("");
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    public void mouseWheel(int mWheelState) {
        markAsRead();
        chatScrollPosition += (mWheelState < 0 ? 1 : -1) * (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 10 : 1);
        chatScrollPosition = MathHelper.clamp(chatScrollPosition, 0, Math.max(chatHistory.size() - 1, 0));
        super.mouseWheel(mWheelState);
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        scrolling = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (scrolling) {
            int totalPixels = (h - (chatBoxY + 10 + chatBoxSize + scrollBarHeight));
            float oneLine = totalPixels * 1f / (chatHistory.size() - 1);
            float onePixel = 1f / oneLine;
            chatScrollPosition = (int) MathHelper.clamp((mouseY - (dragY + chatBoxY + this.y)) * onePixel, 0, chatHistory.size() - 1);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseInRect(0, 0, w, h, mouseX, mouseY))
            markAsRead();
        liveButtons.forEach(LiveButton::runIfClicked);
        if (pastNamesB) {
            pastNamesB = false;
            return;
        }
        if (mouseInRect(chatBoxX + w - 10 - scrollBarWidth, chatBoxY, scrollBarWidth, h - (chatBoxY + 10 + chatBoxSize), mouseX, mouseY) && chatHistory.size() > 1) {
            scrolling = true;
            dragY = mouseY - (this.y + chatBoxY + (h - (chatBoxY + 10 + chatBoxSize + scrollBarHeight)) * chatScrollPosition / (chatHistory.size() - 1));
        }
        longestPastName = Math.max(fontRenderer.getStringWidth(liveProfile.username), longestPastName);
        if (mouseX > x + 40 && mouseX < x + 40 + longestPastName + 4 && mouseY > y + titlebarHeight + 4 && mouseY < y + titlebarHeight + 4 + 12) {
            pastNamesB = true;
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void activateWindow() {
        markAsRead();
        super.activateWindow();
    }

    public void markAsRead() {
        LivemessageGui.unreadMessages.put(liveProfile.uuid, 0);
    }

    /**
     * Draws the text part of the chat history.
     */
    private void drawChatHistory(int chatBoxX, int chatBoxY, int chatColorMe, int chatColorOther) {
        if (chatHistory.size() == 0) {
            fontRenderer.drawString("You're chatting with " + liveProfile.username, chatBoxX + 4, chatBoxY + 5, getSingleRGB(96));
            chatScrolledToBottom = false;
            return;
        }
        int drawHeight = 0;
        chatScrolledToBottom = true;
        DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        DateFormat timeFormat = new SimpleDateFormat("<HH:mm> ");
        String lastDay = dateFormat.format(new Date(System.currentTimeMillis()));
        for (int i = chatScrollPosition; i < chatHistory.size(); i++) {
            ChatMessage chatMessage = chatHistory.get(i);
            boolean isTrimmed = false;
            String message = chatMessage.message;
            Date timestamp = new Date(chatMessage.timestamp);
            while (true) {
                if (chatBoxY + 5 + 12 * drawHeight > h - 34) {
                    chatScrolledToBottom = false;
                    break;
                }
                String thisDay = dateFormat.format(timestamp);
                if (!thisDay.equals(lastDay)) {
                    lastDay = thisDay;
                    fontRenderer.drawString(lastDay, chatBoxX + 4, chatBoxY + 5 + 12 * drawHeight, getSingleRGB(64));
                    drawHeight++;
                    continue;
                }
                if (!isTrimmed)
                    message = timeFormat.format(timestamp) + message;
                int maxWidth = w - (chatBoxX * 2 + 8 + (isTrimmed ? fontRenderer.getStringWidth("<00:00> ") : 0));
                String trimmed = fontRenderer.trimStringToWidth(message, maxWidth);
                fontRenderer.drawString(trimmed, chatBoxX + 4 + (isTrimmed ? fontRenderer.getStringWidth("<00:00> ") : 0), chatBoxY + 5 + 12 * drawHeight, chatMessage.sentByMe ? chatColorMe : chatColorOther);

                drawHeight++;
                if (message.equals(trimmed))
                    break;
                message = message.substring(trimmed.length());
                isTrimmed = true;
            }
            if (!chatScrolledToBottom)
                break;
        }
        if (chatScrolledToBottom && chatBoxY + 5 + 12 * (drawHeight + 2) <= h - 34)
            chatScrolledToBottom = false;
    }

    /**
     * Draws profile pic and determines race (the pony race not the human race).
     */
    private void drawProfilePic(int x, int y) {
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        boolean removeHat = (lastMouseX > this.x + x && lastMouseX < this.x + x + 32 && lastMouseY > this.y + y && lastMouseY < this.y + y + 32);
        float progress = fullSkinAnim.animate(removeHat && clicked ? 1F : 0F);
        int sizeInt = Math.round(8 + (progress * 56));
        if (progress > 0)
            drawRect(-this.x, -this.y, LivemessageGui.screenWidth, LivemessageGui.screenHeight, getRGBA(0, 0, 0, (int) (progress * 128f) + 1));

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);

        ResourceLocation skinTexture = liveSkinUtil.getLocationSkin();

        Minecraft.getMinecraft().getTextureManager().bindTexture(skinTexture);

        if (liveSkinUtil.customSkinLoaded() && race == -1) {
            race = 0;
            try {
                byte[] pixels = new byte[64 * 64 * 4];
                ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length);
                GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                buffer.get(pixels);
                int raceColor = getRGB(pixels[0] & 0xFF, pixels[1] & 0xFF, pixels[2] & 0xFF);
                if (races.containsKey(raceColor))
                    race = raceColor;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Gui.drawScaledCustomSizeModalRect(Math.round(x - (progress * 32)), Math.round(y - (progress * 32)), 8f - progress * 8f, 8f - progress * 8f, sizeInt, sizeInt, sizeInt * 4, sizeInt * 4, 64.0F, 64.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, hatFade.animate(removeHat ? 0F : 1F));
        Gui.drawScaledCustomSizeModalRect(x, y, 40.0F, (float) 8, 8, 8, 32, 32, 64.0F, 64.0F);
    }

    /**
     * Mane drawing bit.
     */
    public void drawWindow(int bgColor, int fgColor) {
        boolean online = LivemessageUtil.checkOnlineStatus(liveProfile.uuid);
        title = "[DM] " + liveProfile.username;
        int unreads = LivemessageGui.unreadMessages.getOrDefault(liveProfile.uuid, 0);
        if (unreads > 0)
            title += " \u00A7l(" + unreads + ")";
        scrollBarHeight = (chatHistory.size() < 2) ? 0 : (int) MathHelper.clamp(Math.floor((h - (chatBoxY + 10 + chatBoxSize)) / Math.max((chatHistory.size() - 1) / 10, 1)), 10, (h - (chatBoxY + 10 + chatBoxSize)) / 2);
        super.drawWindow(bgColor, fgColor);
        // Profile pic
        drawRect(3, titlebarHeight + 3, 36, 36, (online) ? getRGB(60, 148, 100) : getSingleRGB(128));
        if (lastMouseX > x + 40 && lastMouseX < x + 40 + fontRenderer.getStringWidth(liveProfile.username) + 4 && lastMouseY > y + titlebarHeight + 3 && lastMouseY < y + titlebarHeight + 4 + 12)
            drawRect(40, titlebarHeight + 3, fontRenderer.getStringWidth(liveProfile.username) + 4, 12, getSingleRGB(64));
        String displayUsername = liveProfile.username;
        if (chatSettings.isFriend)
            displayUsername += " (friend)";
        if (chatSettings.isBlocked)
            displayUsername += " (blocked)";
        fontRenderer.drawString(displayUsername, 42, titlebarHeight + 5, getSingleRGB(255));
        fontRenderer.drawString(liveProfile.uuid.toString(), 42, titlebarHeight + 5 + 11, getSingleRGB(128));
        fontRenderer.drawString((online) ? "online" : "offline", 42, titlebarHeight + 5 + 21, getSingleRGB(128));

        // Buttons
        liveButtons.forEach(LiveButton::draw);

        // Button textures
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.bindTexture(new ResourceLocation(Livemessage.MOD_ID + ":" + "icons.png"));
        GlStateManager.color(0.5f, 0.5f, 0.5f);
        Gui.drawModalRectWithCustomSizedTexture(w - 13, titlebarHeight + 4, 0, 0, 9, 9, 36, 9);
        Gui.drawModalRectWithCustomSizedTexture(w - 13, titlebarHeight + 4 + 13, 9, 0, 9, 9, 36, 9);
        Gui.drawModalRectWithCustomSizedTexture(w - 13, titlebarHeight + 4 + 26, 18, 0, 9, 9, 36, 9);
        GlStateManager.color(getRed(fgColor) / 255f, getGreen(fgColor) / 255f, getBlue(fgColor) / 255f);
        if (chatSettings.isFriend)
            Gui.drawModalRectWithCustomSizedTexture(w - 13, titlebarHeight + 4, 0, 0, 9, 9, 36, 9);
        if (chatSettings.isBlocked)
            Gui.drawModalRectWithCustomSizedTexture(w - 13, titlebarHeight + 4 + 13, 9, 0, 9, 9, 36, 9);
        if (chatSettings.customColor > 0)
            Gui.drawModalRectWithCustomSizedTexture(w - 13, titlebarHeight + 4 + 26, 18, 0, 9, 9, 36, 9);

        int chatbg = 36;
        int textbg = 24;

        // Chathistory box
        drawRect(chatBoxX - 1, chatBoxY - 1, w - 10 + 2, h - (chatBoxY + 10 + chatBoxSize) + 2, getSingleRGB(64));
        drawRect(chatBoxX, chatBoxY, w - 10, h - (chatBoxY + 10 + chatBoxSize), getSingleRGB(chatbg));
        // Message box
        drawRect(chatBoxX - 1, chatBoxY - 1 + h - (chatBoxY + 5 + chatBoxSize), w - 10 + 2, chatBoxSize + 2, getSingleRGB(64));
        drawRect(chatBoxX, chatBoxY + h - (chatBoxY + 5 + chatBoxSize), w - 10, chatBoxSize, getSingleRGB(textbg));

        //scrollbar
        if (chatHistory.size() > 1)
            drawRect(chatBoxX + w - 10 - scrollBarWidth, chatBoxY + (h - (chatBoxY + 10 + chatBoxSize + scrollBarHeight)) * chatScrollPosition / (chatHistory.size() - 1), scrollBarWidth, scrollBarHeight,
                    (scrolling) ? getSingleRGB(128) : (mouseInRect(chatBoxX + w - 10 - scrollBarWidth, chatBoxY, scrollBarWidth, h - (chatBoxY + 10 + chatBoxSize), lastMouseX, lastMouseY)) ? getSingleRGB(96) : getSingleRGB(64)
            );

        this.inputField.setTextColor(getSingleRGB((active) ? 255 : 128));
        this.inputField.x = 8;
        this.inputField.y = this.h - chatBoxSize - 2;
        this.inputField.width = this.w - 18;
        this.inputField.drawTextBox();

        drawChatHistory(chatBoxX, chatBoxY, getSingleRGB(255), fgColor);
        drawProfilePic(5, titlebarHeight + 5);

        // Namehistory
        if (pastNamesB) {
            if (liveProfile.pastNames == null) {
                drawRect(40, titlebarHeight + 3, longestPastName + 4, 12, getSingleRGB(128));
                liveProfile = LiveProfileCache.forceloadNameHistory(liveProfile);
            } else {
                drawRect(40, titlebarHeight + 3, longestPastName + 4, 12 * liveProfile.pastNames.size(), fgColor);
                int i = 0;
                for (String name : liveProfile.pastNames) {
                    if (lastMouseX > x + 40 && lastMouseX < x + 40 + longestPastName + 4 && lastMouseY > y + titlebarHeight + 3 + 12 * i && lastMouseY < y + titlebarHeight + 4 + 12 + 12 * i)
                        drawRect(40, titlebarHeight + 3 + 12 * i, longestPastName + 4, 12, getRGBA(255, 255, 255, 64));
                    fontRenderer.drawString(name, 42, titlebarHeight + 5 + 12 * i, getSingleRGB(255));
                    i++;
                }
            }
        }

        // Tooltips
        liveButtons.forEach(LiveButton::drawTooltips);
    }
}

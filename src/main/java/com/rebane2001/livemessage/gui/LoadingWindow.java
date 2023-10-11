package com.rebane2001.livemessage.gui;

import java.util.UUID;

import static com.rebane2001.livemessage.gui.GuiUtil.getSingleRGB;

public class LoadingWindow extends LiveWindow {
    UUID uuid;
    LoadingWindow(UUID uuid) {
        this.uuid = uuid;
        animateInStart = System.currentTimeMillis();
        title = "[DM] Loading...";
        minw = 280;
        loadWindowColor();
    }

    public void loadWindowColor() {
        primaryColor = GuiUtil.getWindowColor(uuid);
    }

    public void drawWindow(int bgColor, int fgColor) {
        int chatbg = 36;
        int textbg = 24;

        // Chathistory box
        drawRect(ChatWindow.chatBoxX - 1, ChatWindow.chatBoxY - 1, w - 10 + 2, h - (ChatWindow.chatBoxY + 10 + ChatWindow.chatBoxSize) + 2, getSingleRGB(64));
        drawRect(ChatWindow.chatBoxX, ChatWindow.chatBoxY, w - 10, h - (ChatWindow.chatBoxY + 10 + ChatWindow.chatBoxSize), getSingleRGB(chatbg));
        // Message box
        drawRect(ChatWindow.chatBoxX - 1, ChatWindow.chatBoxY - 1 + h - (ChatWindow.chatBoxY + 5 + ChatWindow.chatBoxSize), w - 10 + 2, ChatWindow.chatBoxSize + 2, getSingleRGB(64));
        drawRect(ChatWindow.chatBoxX, ChatWindow.chatBoxY + h - (ChatWindow.chatBoxY + 5 + ChatWindow.chatBoxSize), w - 10, ChatWindow.chatBoxSize, getSingleRGB(textbg));
    }
}

package com.rebane2001.livemessage.gui;

import com.mojang.authlib.GameProfile;
import com.rebane2001.livemessage.Livemessage;
import com.rebane2001.livemessage.util.LiveProfileCache;
import com.rebane2001.livemessage.util.LiveProfileCache.LiveProfile;
import com.rebane2001.livemessage.util.LiveSkinUtil;
import com.rebane2001.livemessage.util.LivemessageUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.rebane2001.livemessage.gui.GuiUtil.*;
import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

public class ManeWindow extends LiveWindow {

    LiveProfile liveProfile;
    MinecraftClient mc;

    LiveProfileHeader liveProfileHeader;

    final int scrollBarWidth = 10;
    int scrollBarHeight = 50;
    int listScrollPosition = 0;
    boolean scrolling = false;

    // TODO: Reimplement
    // protected static GuiTextField searchField;

    public static List<BuddyListEntry> buddyListEntries = new ArrayList<>();

    final int buddyListX = 5;
    final int buddyListY = titlebarHeight + 44;
    final int footer = 13;

    ManeWindow() {
        mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        // TODO: Better handing of this
        if (player == null) return;
        liveProfile = new LiveProfile(player.getUuid(), player.getGameProfile().getName());
        liveProfileHeader = new LiveProfileHeader(this, liveProfile, true);
        closeButton = false;

        // TODO: reimplement
        // Initialize search box
        /*
        this.searchField = new GuiTextField(0, this.fontRenderer, 9, this.h - 16, this.w - 18, 12);
        this.searchField.setMaxStringLength(16);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setFocused(true);
        this.searchField.setText("");
        this.searchField.setCanLoseFocus(false);
        this.searchField.setTextColor(getSingleRGB(255));


        // Hide text box cursor
        for (int j = 0; j < 6; j++)
            this.searchField.updateCursorCounter();
        */
        initButtons();
    }

    /**
     * Initializes buttons.
     */
    public void initButtons() {
        liveButtons.add(new LiveButton(this, 0, 14, titlebarHeight + 3, 11, 11, true, "", "Settings", () -> {
            // TODO: Implement settings
            // Minecraft.getMinecraft().displayGuiScreen(new GuiConfig(Minecraft.getMinecraft().currentScreen, Livemessage.MOD_ID, false, false, "Livemessage Settings", LivemessageConfig.class));
        }));
        liveButtons.add(new LiveButton(this,1, 14, titlebarHeight + 3 + 13, 11, 11, true, "", "Mark all as read", LivemessageGui::markAllAsRead));
    }
    public void keyTyped(char typedChar, int keyCode) {
        // TODO: Reimplement
        /*
        if (keyCode == Keyboard.KEY_PRIOR) {
            listScrollPosition = MathHelper.clamp(listScrollPosition - 10, 0, Math.max(buddyListEntries.size() - 1, 0));
        } else if (keyCode == Keyboard.KEY_NEXT) {
            listScrollPosition = MathHelper.clamp(listScrollPosition + 10, 0, Math.max(buddyListEntries.size() - 1, 0));
        } else {
            this.searchField.textboxKeyTyped(typedChar, keyCode);
        }
        super.keyTyped(typedChar, keyCode);
         */
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        scrolling = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    public void mouseWheel(int mWheelState) {
        listScrollPosition += (mWheelState < 0 ? 1 : -1) * (hasShiftDown() ? 10 : 1);
        listScrollPosition = MathHelper.clamp(listScrollPosition, 0, Math.max(buddyListEntries.size() - 1, 0));
        super.mouseWheel(mWheelState);
    }

    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (scrolling) {
            int totalPixels = h - (buddyListY + 10 + footer);
            float oneLine = totalPixels * 1f / (buddyListEntries.size() - 1);
            float onePixel = 1f / oneLine;
            listScrollPosition = (int) MathHelper.clamp((mouseY - (dragY + buddyListY + this.y)) * onePixel, 0, buddyListEntries.size() - 1);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        liveButtons.forEach(LiveButton::runIfClicked);
        if (mouseInRect(w - buddyListX - scrollBarWidth, buddyListY, scrollBarWidth, h - (buddyListY + 10 + footer), mouseX, mouseY) && buddyListEntries.size() > 1) {
            scrolling = true;
            dragY = mouseY - (this.y + buddyListY + (h - (buddyListY + 10 + footer + scrollBarHeight)) * listScrollPosition / (buddyListEntries.size() - 1));
        } else if (mouseInRect(buddyListX, buddyListY, w - 10, h - (buddyListY + 10 + footer), mouseX, mouseY)) {
            int i = (int) Math.floor((mouseY - buddyListY - this.y - 3) / 12f) + listScrollPosition;
            if (i < buddyListEntries.size() && i >= 0) {
                BuddyListEntry buddyListEntry = buddyListEntries.get(i);
                LivemessageGui.openChatWindow(buddyListEntry.uuid);
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public static class BuddyListEntry {
        UUID uuid = null;
        String username;
        boolean online;

        BuddyListEntry(UUID uuid, String username, boolean online) {
            this.uuid = uuid;
            this.username = username;
            this.online = online;
        }

        BuddyListEntry(String username) {
            this.username = username;
            this.online = true;
        }
    }

    private static boolean rightMode(int mode, UUID uuid) {
        switch (mode) {
            case 0:
                if (LivemessageGui.unreadMessages.getOrDefault(uuid, 0) == 0)
                    return false;
                break;
            case 1:
                if (LivemessageGui.unreadMessages.getOrDefault(uuid, 0) > 0 || !LivemessageUtil.isPlayerOnline(uuid))
                    return false;
                break;
            case 2:
                if (LivemessageGui.unreadMessages.getOrDefault(uuid, 0) > 0 || LivemessageUtil.isPlayerOnline(uuid))
                    return false;
                break;
        }
        return true;
    }

    private static boolean searchFilter(String username){
        try {
            // TODO: reimplement
            //return searchField.getText().trim().length() > 0 && !username.toLowerCase(Locale.ROOT).contains(searchField.getText().trim().toLowerCase(Locale.ROOT));
            return false;
        } catch (Exception e){
            return false;
        }
    }

    public static void generateBuddylist() {
        buddyListEntries.clear();
        MinecraftClient mc = MinecraftClient.getInstance();

        List<UUID> nearbyPlayers = new ArrayList<>();
        if (mc.world != null) {
            for (AbstractClientPlayerEntity e : mc.world.getPlayers()) {
                if (mc.player != e)
                    nearbyPlayers.add(e.getGameProfile().getId());
            }
        }

        if (!nearbyPlayers.isEmpty()) {
            buddyListEntries.add(new BuddyListEntry("Nearby"));
            for (int mode = 0; mode < 3; ++mode) {
                for (int i = 0; i < nearbyPlayers.size(); ++i) {
                    UUID uuid = nearbyPlayers.get(i);
                    LiveProfile liveProfile = LiveProfileCache.getLiveprofileFromUUID(uuid, false);
                    if (liveProfile == null)
                        continue;
                    if (LivemessageGui.blocked.contains(uuid))
                        continue;
                    if (!rightMode(mode, uuid))
                        continue;
                    if (searchFilter(liveProfile.username))
                        continue;
                    buddyListEntries.add(new BuddyListEntry(uuid, liveProfile.username, LivemessageUtil.isPlayerOnline(uuid)));
                }
            }
        }


        buddyListEntries.add(new BuddyListEntry("Friends"));

        for (int mode = 0; mode < 3; ++mode) {
            for (int i = 0; i < LivemessageGui.friends.size(); ++i) {
                UUID uuid = LivemessageGui.friends.get(i);
                LiveProfile liveProfile = LiveProfileCache.getLiveprofileFromUUID(uuid, true);
                if (liveProfile == null)
                    continue;
                if (LivemessageGui.blocked.contains(uuid))
                    continue;
                if (!rightMode(mode, uuid))
                    continue;
                if (searchFilter(liveProfile.username))
                    continue;
                buddyListEntries.add(new BuddyListEntry(uuid, liveProfile.username, LivemessageUtil.isPlayerOnline(uuid)));
            }
        }

        buddyListEntries.add(new BuddyListEntry("Chats"));

        for (int mode = 0; mode < 3; ++mode) {
            for (int i = 0; i < LivemessageGui.chats.size(); ++i) {
                UUID uuid = LivemessageGui.chats.get(i);
                LiveProfile liveProfile = LiveProfileCache.getLiveprofileFromUUID(uuid, true);
                if (liveProfile == null)
                    continue;
                if (LivemessageGui.friends.contains(uuid) || LivemessageGui.blocked.contains(uuid))
                    continue;
                if (!rightMode(mode, uuid))
                    continue;
                if (searchFilter(liveProfile.username))
                    continue;
                buddyListEntries.add(new BuddyListEntry(uuid, liveProfile.username, LivemessageUtil.isPlayerOnline(uuid)));
            }
        }

        buddyListEntries.add(new BuddyListEntry("Online"));

        for (PlayerListEntry playerListEntry : mc.player.networkHandler.getPlayerList()) {
            GameProfile gameProfile = playerListEntry.getProfile();
            UUID uuid = gameProfile.getId();
            if (uuid.equals(mc.player.getUuid()) || LivemessageGui.friends.contains(uuid) || LivemessageGui.chats.contains(uuid) || LivemessageGui.blocked.contains(uuid))
                continue;
            if (searchFilter(gameProfile.getName()))
                continue;
            buddyListEntries.add(new BuddyListEntry(uuid, gameProfile.getName(), true));
        }

        buddyListEntries.add(new BuddyListEntry("Blocked"));

        for (int mode = 0; mode < 3; ++mode) {
            for (int i = 0; i < LivemessageGui.blocked.size(); ++i) {
                UUID uuid = LivemessageGui.blocked.get(i);
                LiveProfile liveProfile = LiveProfileCache.getLiveprofileFromUUID(uuid, true);
                if (liveProfile == null)
                    continue;
                if (!rightMode(mode, uuid))
                    continue;
                if (searchFilter(liveProfile.username))
                    continue;
                buddyListEntries.add(new BuddyListEntry(uuid, liveProfile.username, LivemessageUtil.isPlayerOnline(uuid)));
            }
        }
    }

    public void drawBuddylist() {
        int lineHeight = 0;
        for (int i = listScrollPosition; i < buddyListEntries.size(); ++i) {
            if (lineHeight * 12 > h - (buddyListY + footer + 22))
                break;

            BuddyListEntry buddyListEntry = buddyListEntries.get(i);
            String buddyText = (buddyListEntry.uuid == null ? "\u00A7l" : (buddyListEntry.online ? "   " : "   \u00A7o")) + buddyListEntry.username;
            drawText(buddyText, buddyListX + 5, buddyListY + 5 + 12 * lineHeight, getSingleRGB((buddyListEntry.online) ? 255 : 128));

            // We cannot use the strikethrough text formatting any more because it renders over all the other windows for some reason
            if (LivemessageGui.blocked.contains(buddyListEntry.uuid)) {
                int offsetLength = getStringWidth("   ");
                drawRect(buddyListX + 5 + offsetLength - 1, buddyListY + 5 + 12 * lineHeight + 4, getStringWidth(buddyText) - offsetLength + 1, 1, getSingleRGB((buddyListEntry.online) ? 255 : 128));
            }

            if (buddyListEntry.uuid != null) {
                int unreads = LivemessageGui.unreadMessages.getOrDefault(buddyListEntry.uuid, 0);
                String unreadString = "(" + unreads + ")";
                if (unreads > 0)
                    drawText(unreadString, buddyListX + 5 + getStringWidth(buddyText + " "), buddyListY + 5 + 12 * lineHeight, (LivemessageGui.blocked.contains(buddyListEntry.uuid)) ? getSingleRGB(64) : getRGB(255, 0, 0));
            }

            lineHeight++;
        }
    }

    public void drawWindow(int bgColor, int fgColor) {
        w = 150;
        title = "Livemessage";
        super.drawWindow(bgColor, fgColor);

        drawRect(buddyListX - 1, buddyListY - 1, w - 10 + 2, h - (buddyListY + 10 + footer) + 2, getRGB(64, 64, 64));
        drawRect(buddyListX, buddyListY, w - buddyListX * 2, h - (buddyListY + 10 + footer), getRGB(36, 36, 36));

        // Buttons
        liveButtons.forEach(LiveButton::draw);

        // TODO: Reimplement
        // Button textures
        /*
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.bindTexture(new ResourceLocation(Livemessage.MOD_ID + ":" + "icons.png"));
        GlStateManager.color(0.5f, 0.5f, 0.5f);
        Gui.drawModalRectWithCustomSizedTexture(w - 13, titlebarHeight + 4, 27, 0, 9, 9, 45, 9);
        Gui.drawModalRectWithCustomSizedTexture(w - 13, titlebarHeight + 4 + 13, 36, 0, 9, 9, 45, 9);
         */

        generateBuddylist();
        scrollBarHeight = (buddyListEntries.size() < 2) ? 0 : (int) MathHelper.clamp(Math.floor((h - (buddyListY + 10 + footer)) / Math.max((buddyListEntries.size() - 1) / 10, 1)), 10, (h - (buddyListY + 10 + footer)) / 2);
        if (mouseInRect(buddyListX, buddyListY, w - 10, h - (buddyListY + 10 + footer), lastMouseX, lastMouseY)) {
            int i = (int) Math.floor((lastMouseY - buddyListY - this.y - 3) / 12f) + listScrollPosition;
            if (i < buddyListEntries.size() && i >= 0 && (i - listScrollPosition) * 12 <= h - (buddyListY + footer + 22)) {
                BuddyListEntry buddyListEntry = buddyListEntries.get(i);
                if (buddyListEntry.uuid != null)
                    drawRect(buddyListX, buddyListY + (i - listScrollPosition) * 12 + 3, w - 10, 12, getRGB(64, 64, 64));
            }
        }
        drawBuddylist();
        if (buddyListEntries.size() > 1)
            drawRect(buddyListX + w - 10 - scrollBarWidth, buddyListY + (h - (buddyListY + 10 + footer + scrollBarHeight)) * listScrollPosition / (buddyListEntries.size() - 1), scrollBarWidth, scrollBarHeight,
                    (scrolling) ? getSingleRGB(128) : (mouseInRect(buddyListX + w - 10 - scrollBarWidth, buddyListY, scrollBarWidth, h - (buddyListY + 10 + footer), lastMouseX, lastMouseY)) ? getSingleRGB(96) : getSingleRGB(64)
            );

        // Search field
        drawRect(5 - 1, this.h - footer - 5 - 1, this.w - 10 + 2, footer + 2, getSingleRGB(64));
        drawRect(5, this.h - footer - 5, this.w - 10, footer, getSingleRGB(24));

        // TODO: Reimplement
        /*
        this.searchField.setTextColor(getSingleRGB((active) ? 255 : 128));
        this.searchField.x = 8;
        this.searchField.y = this.h - footer - 2;
        this.searchField.width = this.w - 18;
        this.searchField.drawTextBox();

        if (this.searchField.getText().trim().length() == 0)
            drawText("Search...", 8, this.h - footer - 2, getSingleRGB(64));
         */

        // drawText(liveProfile.username, 42, titlebarHeight + 5, getSingleRGB(255));
        // drawText("online", 42, titlebarHeight + 5 + 11, getSingleRGB(128));
        // drawRect(3, titlebarHeight + 3, 36, 36, getRGB(60, 148, 100));
        // drawProfilePic(5, titlebarHeight + 5);
        liveProfileHeader.drawProfileHeader(5, titlebarHeight + 5);

        // Tooltips
        liveButtons.forEach(LiveButton::drawTooltips);
    }
}

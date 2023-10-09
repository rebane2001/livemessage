package com.rebane2001.livemessage.gui;

import com.rebane2001.livemessage.util.LiveProfileCache.LiveProfile;
import com.rebane2001.livemessage.util.LiveSkinUtil;
import com.rebane2001.livemessage.util.LivemessageUtil;

import java.util.UUID;

import static com.rebane2001.livemessage.gui.GuiUtil.*;

public class LiveProfileHeader {

    GuiUtil.QuintAnimation hatFade = new GuiUtil.QuintAnimation(300, 1f);
    GuiUtil.QuintAnimation fullSkinAnim = new GuiUtil.QuintAnimation(600, 0f);

    LiveSkinUtil liveSkinUtil;
    LiveProfile liveProfile;
    LiveWindow w;

    LiveProfileHeader (LiveWindow window, LiveProfile liveProfile) {
        this.w = window;
        this.liveProfile = liveProfile;
        this.liveSkinUtil = new LiveSkinUtil(liveProfile.uuid);
    }

    public void drawProfileHeader(int x, int y) {
        boolean online =  LivemessageUtil.isPlayerOnline(liveProfile.uuid);

        w.drawText(liveProfile.username, x + 42 - 5, y, getSingleRGB(255));
        w.drawText((online) ? "online" : "offline", x + 42 - 5, y + 11, getSingleRGB(128));
        w.drawRect(x - 2, y - 2, 36, 36, (online) ? getRGB(60, 148, 100) : getSingleRGB(128));
        drawProfilePic(x, y);
    }

    private void drawProfilePic(int x, int y) {
        boolean removeHat = (w.lastMouseX > w.x + x && w.lastMouseX < w.x + x + 32 && w.lastMouseY > w.y + y && w.lastMouseY < w.y + y + 32);
        // TODO: w.scrolling
        // float progress = w.fullSkinAnim.animate(removeHat && w.clicked && !w.dragging && !w.resizing && !w.scrolling ? 1F : 0F);
        float progress = fullSkinAnim.animate(removeHat && w.clicked && !w.dragging && !w.resizing ? 1F : 0F);
        float sizeFlt = (8 + (progress * 56))*4;
        if (progress > 0)
            w.drawRect(-w.x, -w.y, LivemessageGui.screenWidth, LivemessageGui.screenHeight, getRGBA(0, 0, 0, (int) (progress * 128f) + 1));
        w.drawTextureTrans(
                liveSkinUtil.getLocationSkin(),
                x - (progress * 32),y - (progress * 32),
                (32f - progress * 32f), (32f - progress * 32f),
                sizeFlt, sizeFlt,
                1
        );
        w.drawTextureTrans(
                liveSkinUtil.getLocationSkin(),
                x, y,
                32 + (32 * 4), 32,
                32, 32,
                hatFade.animate(removeHat ? 0F : 1F)
        );
    }
}

package com.rebane2001.livemessage.gui;

import com.rebane2001.livemessage.util.LiveProfileCache.LiveProfile;
import com.rebane2001.livemessage.util.LiveSkinUtil;
import com.rebane2001.livemessage.util.LivemessageUtil;
import com.rebane2001.livemessage.util.PdbUtil;


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
        // TODO: only if pdb enabled and make it work nicer and only if pronouns exist and only if there is room to show them
        if (liveProfile.pdbData.pronoun != null)
            w.drawText(" (" + PdbUtil.getPronounText(liveProfile.pdbData.pronoun) + ")",  x + w.getStringWidth(liveProfile.username) + 42 - 5, y, getSingleRGB(128));
        w.drawText(liveProfile.username, x + 42 - 5, y, getSingleRGB(255));
        w.drawText((online) ? "online" : "offline", x + 42 - 5, y + 11, getSingleRGB(128));
        drawStatus(x - 2, y - 2, online, liveProfile.pdbData.decoration);
        drawProfilePic(x, y, liveProfile.pdbData.decoration);
    }

    private void drawStatus(int x, int y, boolean online, String decoration) {
        w.drawRect(x, y , 36, 36, (online) ? getRGB(60, 148, 100) : getSingleRGB(128));
        // TODO: only if pdb enabled
        if (online && decoration != null) {
            drawDecoration(x, y, 36, 36, decoration, 0);
        }
    }

    private void drawDecoration(int x, int y, int width, int height, String decoration, int alpha) {
        int[] colors = PdbUtil.getDecorationColors(decoration);
        float sliceSize = (float) height / colors.length;

        for (int i = 0; i < colors.length; i++) {
            int start = Math.round(i*sliceSize);
            int end = Math.round((i+1)*sliceSize) - start;
            w.drawRect(x, y + start, width, end, getColorWithAlpha(colors[i], alpha));
        }
    }

    private void drawProfilePic(int x, int y, String decoration) {
        boolean removeHat = (w.lastMouseX > w.x + x && w.lastMouseX < w.x + x + 32 && w.lastMouseY > w.y + y && w.lastMouseY < w.y + y + 32);
        // TODO: w.scrolling
        // float progress = w.fullSkinAnim.animate(removeHat && w.clicked && !w.dragging && !w.resizing && !w.scrolling ? 1F : 0F);
        float progress = fullSkinAnim.animate(removeHat && w.clicked && !w.dragging && !w.resizing ? 1F : 0F);
        float sizeFlt = (8 + (progress * 56))*4;
        if (progress > 0) {
            int alpha = (int) (progress * 128f) + 1;
            // TODO: only if pdb enabled
            if (decoration != null) {
                drawDecoration(-w.x, -w.y, LivemessageGui.screenWidth, LivemessageGui.screenHeight, decoration, alpha);
            }
            w.drawRect(-w.x, -w.y, LivemessageGui.screenWidth, LivemessageGui.screenHeight, getRGBA(0, 0, 0, alpha));
        }
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

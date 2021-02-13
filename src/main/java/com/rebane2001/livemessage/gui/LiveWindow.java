package com.rebane2001.livemessage.gui;

import com.rebane2001.livemessage.LivemessageConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static com.rebane2001.livemessage.gui.GuiUtil.*;

public class LiveWindow {
    public static int titlebarHeight = 17;
    public int x;
    public int y;
    public int w = 400;
    public int h = 250;
    public int minw = 100;
    public int maxw = 9999;
    public int minh = 100;
    public int maxh = 9999;
    public int lastMouseX = 0;
    public int lastMouseY = 0;
    public String title = "Sample text";
    public boolean active = true;
    public int dragX;
    public int dragY;
    public boolean clicked = false;
    public boolean dragging = false;
    public boolean resizing = false;
    public boolean closeButton = true;
    public int primaryColor = 0;
    public FontRenderer fontRenderer;

    boolean animateIn = true;
    long animateInStart;

    LiveWindow() {
        x = (int) (Math.random() * (LivemessageGui.screenWidth - w));
        y = (int) (Math.random() * (LivemessageGui.screenHeight - h));
        fontRenderer = Minecraft.getMinecraft().fontRenderer;
        primaryColor = GuiUtil.getWindowColor(Minecraft.getMinecraft().player.getUniqueID());
        if (LivemessageGui.liveWindows.size() > 0)
            LivemessageGui.liveWindows.get(LivemessageGui.liveWindows.size() - 1).deactivateWindow();
        animateInStart = System.currentTimeMillis();
    }

    List<LiveButton> liveButtons = new ArrayList<>();

    class LiveButton {
        public int id;
        public int bx;
        public int by;
        public int bw;
        public int bh;
        public boolean negativeX;
        public String btnText;
        public String tooltipText;
        public int idleColor = getSingleRGB(64);
        public int hoverColor = getSingleRGB(96);
        public int textColor = getSingleRGB(255);
        public Runnable action;

        LiveButton(int id, int x, int y, int w, int h, boolean negativeX, String btnText, String tooltipText, Runnable action) {
            this.id = id;
            this.bx = x;
            this.by = y;
            this.bw = w;
            this.bh = h;
            this.btnText = btnText;
            this.tooltipText = tooltipText;
            this.action = action;
            this.negativeX = negativeX;
        }

        // Get x
        public int gx(){
            return negativeX ? (w - bx) : bx;
        }

        public void runIfClicked() {
            if (isMouseOver())
                action.run();
        }

        public boolean isMouseOver() {
            return mouseInRect(gx(), by, bw, bh, lastMouseX, lastMouseY);
        }

        public void draw() {
            drawRect(gx(), by, bw, bh,
                    isMouseOver() ? hoverColor : idleColor);
            if (!btnText.isEmpty())
                fontRenderer.drawString(btnText, gx() + bw / 2 - fontRenderer.getStringWidth(btnText) / 2, by, textColor);
        }

        public void drawTooltips() {
            if (!tooltipText.isEmpty() && isMouseOver())
                drawTooltip(tooltipText);
        }


    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
        resizing = false;
        clicked = false;
    }

    public void mouseMove(int mouseX, int mouseY) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    public boolean mouseInRect(int x, int y, int w, int h, int mouseX, int mouseY) {
        return (mouseX > this.x + x && mouseX < this.x + x + w && mouseY > this.y + y && mouseY < this.y + y + h);
    }

    ;

    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_TAB && LivemessageGui.liveWindows.size() > 1) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                LiveWindow tempWindow = LivemessageGui.liveWindows.get(0);
                LivemessageGui.liveWindows.remove(0);
                tempWindow.activateWindow();
                LivemessageGui.liveWindows.add(tempWindow);
            } else {
                LivemessageGui.liveWindows.removeIf(it -> it == this);
                LivemessageGui.liveWindows.add(0, this);
                LivemessageGui.liveWindows.get(LivemessageGui.liveWindows.size() - 1).activateWindow();
            }
            deactivateWindow();
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        clicked = true;
        // Close button
        if (closeButton && mouseX > x + w - 13 && mouseX < x + w - 2 && mouseY > y + 3 && mouseY < y + 14) {
            if (LivemessageGui.liveWindows.size() > 1) {
                LivemessageGui.liveWindows.get(LivemessageGui.liveWindows.size() - 2).activateWindow();
                LivemessageGui.liveWindows.removeIf(it -> it == this);
            }
        }
        // Dragging/resizing
        if (mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + 20) {
            dragging = true;
            resizing = false;
            dragX = mouseX - x;
            dragY = mouseY - y;
        } else if (mouseX > x + w - 7 && mouseX < x + w + 3 && mouseY > y + h - 7 && mouseY < y + h + 3) {
            dragging = false;
            resizing = true;
            dragX = mouseX - x - w;
            dragY = mouseY - y - h;
        }
    }

    public boolean mouseInWindow(int mouseX, int mouseY) {
        return (mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h);
    }

    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    }

    /*
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging) {
            x = Math.max(0, mouseX - dragX);
            x -= x % LivemessageGui.scl;
            y = Math.max(0, mouseY - dragY);
            y -= y % LivemessageGui.scl;
        } else if (resizing) {
            w = MathHelper.clamp(mouseX - dragX - x, minw, maxw);
            w -= w % LivemessageGui.scl;
            h = MathHelper.clamp(mouseY - dragY - y, minh, maxh);
            h -= h % LivemessageGui.scl;
        }
    }
     */

    public void handleMouseDrag(){
        if (dragging || resizing){
            final int mouseX = Mouse.getX();
            final int mouseY = LivemessageGui.screenHeight - Mouse.getY();
            if (dragging) {
                x = Math.max(0, mouseX - dragX);
                x -= x % LivemessageGui.scl;
                y = Math.max(0, mouseY - dragY);
                y -= y % LivemessageGui.scl;
            } else if (resizing) {
                w = MathHelper.clamp(mouseX - dragX - x, minw, maxw);
                w -= w % LivemessageGui.scl;
                h = MathHelper.clamp(mouseY - dragY - y, minh, maxh);
                h -= h % LivemessageGui.scl;
            }
        }
    }

    public void mouseWheel(int mWheelState) {
    }

    public void activateWindow() {
        active = true;
    }

    public void deactivateWindow() {
        active = false;
        mouseReleased(lastMouseX, lastMouseY, 0);
    }

    public void preDrawWindow() {
        handleMouseDrag();
        Framebuffer mb = null;
        Framebuffer fb = null;
        if (animateIn) {
            mb = Minecraft.getMinecraft().getFramebuffer();
            fb = new Framebuffer(mb.framebufferWidth, mb.framebufferHeight, true);
            fb.framebufferClear();
            fb.bindFramebuffer(true);
        }

        if (fontRenderer == null)
            fontRenderer = Minecraft.getMinecraft().fontRenderer;
        // if window is off-screen, move it back
        if (x + w > LivemessageGui.screenWidth)
            x = LivemessageGui.screenWidth - w;
        if (y + h > LivemessageGui.screenHeight)
            y = LivemessageGui.screenHeight - h;
        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;
        // if window is too big to fit on-screen, make it smaller
        if (x + w > LivemessageGui.screenWidth)
            w = Math.max(LivemessageGui.screenWidth, minw);
        if (y + h > LivemessageGui.screenHeight)
            h = Math.max(LivemessageGui.screenHeight, minh);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        int bgColor = getRGB(32, 32, 32);
        int fgColor = active ? primaryColor : getRGB(128, 128, 128);
        drawWindow(bgColor, fgColor);
        GlStateManager.popMatrix();

        if (animateIn) {
            double time = (System.currentTimeMillis() - animateInStart) / 1000d / 0.4d;
            double progress = easeOutQuad(time);
            if (time > 1) {
                progress = 1;
                animateIn = false;
            }

            mb.bindFramebuffer(true);
            GlStateManager.pushMatrix();
            GlStateManager.color(1f, 1f, 1f, (float) progress);
            GlStateManager.translate((x + w / 2), (y + h / 2), 0);
            GlStateManager.scale(1 - 0.25 * (1d - progress), 1 - 0.25 * (1d - progress), 0);
            GlStateManager.translate(-(x + w / 2), -(y + h / 2), 0);
            GlStateManager.translate(0, 150 * (1d - progress), 0);
            fb.bindFramebufferTexture();
            //TODO
            int guiScale = LivemessageConfig.guiSettings.guiScale;
            Gui.drawModalRectWithCustomSizedTexture(0, mb.framebufferHeight/guiScale, 0, 0, mb.framebufferWidth/guiScale, -mb.framebufferHeight/guiScale, mb.framebufferWidth/guiScale, -mb.framebufferHeight/guiScale);
            fb.deleteFramebuffer();
            mb.bindFramebuffer(true);
            GlStateManager.popMatrix();
        }
    }

    public void drawTooltip(String text) {
        if (!active)
            return;
        int padding = 3;
        int x = lastMouseX - this.x + 1;
        int y = lastMouseY - this.y - 12 - padding;
        drawRect(x, y, fontRenderer.getStringWidth(text) + padding * 2, 12 + padding - 1, getRGB(36, 36, 36));
        fontRenderer.drawString(text, x + padding, y + padding, 0xFFFFFF);
    }

    public void drawWindow(int bgColor, int fgColor) {
        // Window
        drawRect(0, 0, w, h, bgColor);
        drawRectOutline(0, 0, w, h, fgColor);
        // Titlebar
        drawRect(0, 0, w, titlebarHeight, fgColor);
        fontRenderer.drawString(title, 5, 5, 0xFFFFFF);
        // Close button
        if (closeButton)
            drawRect(w - 13, 3, 11, 11, bgColor);
        // Resize corner
        drawRectHalf(w - 6, h - 6, 6, 6, false, fgColor);
    }

    public void drawRect(int x, int y, int w, int h, int color) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glColor4f(getRed(color) / 255f, getGreen(color) / 255f, getBlue(color) / 255f, getAlphaNonZero(color) / 255f);
        GL11.glVertex2i(x, y);
        GL11.glVertex2i(x, y + h);
        GL11.glVertex2i(x + w, y);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2i(x + w, y + h);
        GL11.glVertex2i(x, y + h);
        GL11.glVertex2i(x + w, y);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.resetColor();
    }

    public void drawRectHalf(int x, int y, int w, int h, boolean top, int color) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(getRed(color) / 255f, getGreen(color) / 255f, getBlue(color) / 255f, getAlphaNonZero(color) / 255f);
        if (top) {
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glVertex2i(x, y);
            GL11.glVertex2i(x, y + h);
            GL11.glVertex2i(x + w, y);
            GL11.glEnd();
        } else {
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glVertex2i(x + w, y + h);
            GL11.glVertex2i(x, y + h);
            GL11.glVertex2i(x + w, y);
            GL11.glEnd();
        }
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.resetColor();
    }

    public void drawRectOutline(int x, int y, int w, int h, int color) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glColor4f(getRed(color) / 255f, getGreen(color) / 255f, getBlue(color) / 255f, getAlphaNonZero(color) / 255f);
        GL11.glVertex2i(x, y);
        GL11.glVertex2i(x, y + h);
        GL11.glVertex2i(x + w, y + h);
        GL11.glVertex2i(x + w, y);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.resetColor();
    }

}

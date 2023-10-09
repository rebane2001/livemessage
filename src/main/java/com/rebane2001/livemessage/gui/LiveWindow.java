package com.rebane2001.livemessage.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.rebane2001.livemessage.Livemessage;
import com.rebane2001.livemessage.LivemessageConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static com.rebane2001.livemessage.gui.GuiUtil.*;
import static com.rebane2001.livemessage.gui.LivemessageGui.scl;
import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

public class LiveWindow {
    public static int titlebarHeight = 17;

    // this isn't ideal
    public DrawContext drawContext;
    public TextRenderer textRenderer;
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

    boolean animateIn = true;
    long animateInStart;

    LiveWindow() {
        x = (int) (Math.random() * (LivemessageGui.screenWidth - w));
        y = (int) (Math.random() * (LivemessageGui.screenHeight - h));
        primaryColor = GuiUtil.getWindowColor(MinecraftClient.getInstance().player.getUuid());
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
                drawText(btnText, gx() + bw / 2 - getStringWidth(btnText) / 2, by, textColor);
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

    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_TAB && LivemessageGui.liveWindows.size() > 1) {
            if (hasShiftDown()) {
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
            } else {
                Livemessage.LOGGER.warn("Cannot close window since none would be left");
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
            final int mouseX = (int) (getMouse().getX() / LivemessageConfig.guiSettings.guiScale);
            final int mouseY = (int) (getMouse().getY() / LivemessageConfig.guiSettings.guiScale);
            if (dragging) {
                x = Math.max(0, mouseX - dragX);
                x -= x % scl;
                y = Math.max(0, mouseY - dragY);
                y -= y % scl;
            } else if (resizing) {
                w = MathHelper.clamp(mouseX - dragX - x, minw, maxw);
                w -= w % scl;
                h = MathHelper.clamp(mouseY - dragY - y, minh, maxh);
                h -= h % scl;
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

    public void preDrawWindow(DrawContext context) {
        handleMouseDrag();
        // this isn't ideal
        drawContext = context;
        textRenderer = MinecraftClient.getInstance().textRenderer;
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

        context.getMatrices().push();

        if (animateIn) {
            double time = (System.currentTimeMillis() - animateInStart) / 1000d / 0.4d;
            double progress = easeOutQuad(time);
            if (time > 1) {
                progress = 1;
                animateIn = false;
            } else {
                context.setShaderColor(1, 1, 1, (float) progress);
                context.getMatrices().translate((x + w / 2f), (y + h / 2f), 0);
                context.getMatrices().scale((float) (1 - 0.25 * (1d - progress)), (float) (1 - 0.25 * (1d - progress)), 0);
                context.getMatrices().translate(-(x + w / 2f), -(y + h / 2f), 0);
                context.getMatrices().translate(0, 150 * (1d - progress), 0);
                context.getMatrices().push();
            }

        }

        context.getMatrices().translate(x, y, 0);
        int bgColor = getRGB(32, 32, 32);
        int fgColor = active ? primaryColor : getRGB(128, 128, 128);

        drawWindow(bgColor, fgColor);

        if (animateIn) {
            context.setShaderColor(1,1,1,1);
            context.getMatrices().pop();
        } else {
            animateInStart = System.currentTimeMillis();
            animateIn = true;
        }

        context.getMatrices().pop();

    }
    public void drawTooltip(String text) {
        if (!active)
            return;
        int padding = 3;
        int x = lastMouseX - this.x + 1;
        int y = lastMouseY - this.y - 12 - padding;
        drawRect(x, y, getStringWidth(text) + padding * 2, 12 + padding - 1, getRGB(36, 36, 36));
        drawText(text, x + padding, y + padding, 0xFFFFFF);
    }

    public void drawWindow(int bgColor, int fgColor) {
        // Window
        drawRect(0, 0, w, h, bgColor);
        drawRectOutline(0, 0, w, h, fgColor);
        // Titlebar
        drawRect(0, 0, w, titlebarHeight, fgColor);
        drawText(title, 5, 5, 0xFFFFFF);
        // Close button
        if (closeButton)
            drawRect(w - 13, 3, 11, 11, bgColor);
        // Resize corner
        drawRectHalf(w - 6, h - 6, 6, 6, false, fgColor);
    }

    public int getStringWidth(String text) {
        return textRenderer.getWidth(text);
    }
    public void drawText(String text, int x, int y, int color) {
        drawText(text, x, y, color, false);
    }

    public void drawText(String text, int x, int y, int color, boolean shadow) {
        drawContext.drawText(textRenderer, text, x, y, color, shadow);
    }

    public void drawRect(int x, int y, int w, int h, int color) {
        drawContext.fill(x, y, x+w, y+h, getColorNonZeroAlpha(color));
    }

    public void drawRectHalf(int x1, int y1, int w1, int h1, boolean top, int color1) {
        int color2 = getColorNonZeroAlpha(color1);
        int z = 0;
        int x2 = x1+w1;
        int y2 = y1+h1;
        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
        int i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float f = ColorHelper.Argb.getAlpha(color2) / 255.0F;
        float g = ColorHelper.Argb.getRed(color2) / 255.0F;
        float h = ColorHelper.Argb.getGreen(color2) / 255.0F;
        float j = ColorHelper.Argb.getBlue(color2) / 255.0F;
        VertexConsumer vertexConsumer = drawContext.getVertexConsumers().getBuffer(RenderLayer.getGui());
        int[][] vertexPoints = {
            {top?x2:x1, top?y2:y1},
            {top?x2:x1, top?y1:y2},
            {top?x1:x2, top?y2:y1},
            {top?x2:x1, top?y2:y1},
        };
        for (int[] points : vertexPoints) {
            vertexConsumer.vertex(matrix4f, points[0], points[1], z).color(g, h, j, f).next();
        }
        drawContext.draw();
    }

    public void drawRectOutline(int x, int y, int w, int h, int color) {
        drawContext.drawBorder(x, y, x+w, y+h, getColorNonZeroAlpha(color));
    }

}

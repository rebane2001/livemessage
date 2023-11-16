package com.rebane2001.livemessage.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rebane2001.livemessage.Livemessage;
import com.rebane2001.livemessage.LivemessageConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.rebane2001.livemessage.gui.GuiUtil.*;
import static com.rebane2001.livemessage.gui.LivemessageGui.scaleFactor;
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
    public boolean allowClose = true;
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
        textRenderer = MinecraftClient.getInstance().textRenderer;
    }

    List<LiveButton> liveButtons = new ArrayList<>();

    public void mouseReleased() {
        mouseReleased(lastMouseX, lastMouseY, 0);
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
        resizing = false;
        clicked = false;
    }

    public void mouseMove(int mouseX, int mouseY) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        // TODO: Fix the last 2 parameters
        if (clicked) mouseClickMove(mouseX, mouseY, 1, 1);
    }

    public boolean mouseInRect(int x, int y, int w, int h, int mouseX, int mouseY) {
        return (mouseX >= this.x + x && mouseX < this.x + x + w && mouseY >= this.y + y && mouseY < this.y + y + h);
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        Livemessage.LOGGER.info("keyPressed: " + keyCode + ", " + scanCode + ", " + modifiers);
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
        if ((keyCode == GLFW.GLFW_KEY_W || keyCode == GLFW.GLFW_KEY_Q) && Screen.hasControlDown()) {
            closeWindow();
        }
    }

    public void charTyped(char chr, int modifiers) {
        // TODO: Temporary, remove later
        Livemessage.LOGGER.info("charTyped: " + chr + ", " + modifiers);
    }

    public void closeWindow() {
        if (!allowClose) return;
        if (LivemessageGui.liveWindows.size() > 1) {
            LivemessageGui.liveWindows.get(LivemessageGui.liveWindows.size() - 2).activateWindow();
            LivemessageGui.liveWindows.removeIf(it -> it == this);
        } else {
            Livemessage.LOGGER.warn("Cannot close window since none would be left");
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        clicked = true;
        // Close button
        if (allowClose && mouseX > x + w - 13 && mouseX < x + w - 2 && mouseY > y + 3 && mouseY < y + 14) {
            closeWindow();
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

    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}

    public void handleMouseDrag(){
        double guiScale = LivemessageConfig.guiSettings.guiScale;
        guiScale = guiScale == 0 ? LivemessageGui.gameScale : guiScale;
        final int mouseX = (int) (getMouse().getX() / guiScale);
        final int mouseY = (int) (getMouse().getY() / guiScale);
        // This is a weird place to put it, but it works
        mouseMove(mouseX, mouseY);
        if (dragging || resizing){
            if (dragging) {
                x = Math.max(0, mouseX - dragX);
                x -= x % scaleFactor;
                y = Math.max(0, mouseY - dragY);
                y -= y % scaleFactor;
            } else if (resizing) {
                w = MathHelper.clamp(mouseX - dragX - x, minw, maxw);
                w -= w % scaleFactor;
                h = MathHelper.clamp(mouseY - dragY - y, minh, maxh);
                h -= h % scaleFactor;
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
        mouseReleased();
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
        }

        context.getMatrices().pop();

    }
    public void drawTooltip(String text) {
        if (!active)
            return;
        int padding = 3;
        int tooltipX = lastMouseX - this.x + 1;
        int tooltipY = lastMouseY - this.y - 12 - padding;
        drawRect(tooltipX, tooltipY, getStringWidth(text) + padding * 2, 12 + padding - 1, getRGB(36, 36, 36));
        drawText(text, tooltipX + padding, tooltipY + padding, 0xFFFFFF);
    }

    public void drawWindow(int bgColor, int fgColor) {
        // Window
        drawRect(0, 0, w, h, bgColor);
        drawRectOutline(0, 0, w, h, fgColor);
        // Titlebar
        drawRect(0, 0, w, titlebarHeight, fgColor);
        drawText(title, 5, 5, 0xFFFFFF);
        // Close button
        if (allowClose)
            drawRect(w - 13, 3, 11, 11, bgColor);
        // Resize corner
        drawRectHalf(w - 6, h - 6, 6, 6, false, fgColor);
    }

    public String trimStringToWidth(String text, int maxWidth) {
        return textRenderer.trimToWidth(text, maxWidth);
    }
    public int getStringWidth(String text) {
        return textRenderer.getWidth(text);
    }

    public void drawTextureTrans(Identifier texture, float x1, float y1, float u, float v, float width, float height, float alpha) {
        if (drawContext == null) return;
        float textureWidth = 256;
        float textureHeight = 256;
        float x2 = x1 + width;
        float y2 = y1 + height;
        float z = 0;
        float u1 = (u + 0.0F) / textureWidth;
        float u2 = (u + width) / textureWidth;
        float v1 = (v + 0.0F) / textureHeight;
        float v2 = (v + height) / textureHeight;

        float red = 1;
        float green = 1;
        float blue = 1;

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
        RenderSystem.enableBlend();
        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        bufferBuilder.vertex(matrix4f, x1, y1, z).color(red, green, blue, alpha).texture(u1, v1).next();
        bufferBuilder.vertex(matrix4f, x1, y2, z).color(red, green, blue, alpha).texture(u1, v2).next();
        bufferBuilder.vertex(matrix4f, x2, y2, z).color(red, green, blue, alpha).texture(u2, v2).next();
        bufferBuilder.vertex(matrix4f, x2, y1, z).color(red, green, blue, alpha).texture(u2, v1).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }
    public void drawText(String text, int x, int y, int color) {
        drawText(text, x, y, color, false);
    }

    public void drawText(String text, int x, int y, int color, boolean shadow) {
        if (drawContext == null || textRenderer == null) return;
        drawContext.drawText(textRenderer, text, x, y, color, shadow);
    }

    public void drawRect(int x, int y, int w, int h, int color) {
        if (drawContext == null) return;
        drawContext.fill(x, y, x+w, y+h, getColorNonZeroAlpha(color));
    }

    public void drawRectHalf(int x1, int y1, int w1, int h1, boolean top, int color1) {
        if (drawContext == null) return;
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
        if (drawContext == null) return;
        drawContext.drawBorder(x, y, x+w, y+h, getColorNonZeroAlpha(color));
    }

}

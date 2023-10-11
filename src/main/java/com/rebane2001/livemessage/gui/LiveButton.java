package com.rebane2001.livemessage.gui;

import static com.rebane2001.livemessage.gui.GuiUtil.getSingleRGB;

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
    public LiveWindow window;

    LiveButton(LiveWindow window, int id, int x, int y, int w, int h, boolean negativeX, String btnText, String tooltipText, Runnable action) {
        this.window = window;
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
        return negativeX ? (window.w - bx) : bx;
    }

    public void runIfClicked() {
        if (isMouseOver())
            action.run();
    }

    public boolean isMouseOver() {
        return window.mouseInRect(gx(), by, bw, bh, window.lastMouseX, window.lastMouseY);
    }

    public void draw() {
        window.drawRect(gx(), by, bw, bh,
                isMouseOver() ? hoverColor : idleColor);
        if (!btnText.isEmpty())
            window.drawText(btnText, gx() + bw / 2 - window.getStringWidth(btnText) / 2, by, textColor);
    }

    public void drawTooltips() {
        if (!tooltipText.isEmpty() && isMouseOver())
            window.drawTooltip(tooltipText);
    }
}
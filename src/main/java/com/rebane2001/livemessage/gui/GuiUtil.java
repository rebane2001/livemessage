package com.rebane2001.livemessage.gui;

import com.rebane2001.livemessage.util.LivemessageUtil;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public class GuiUtil {
    public static int getAlpha(int color){
        return (color>>24)&0xFF;
    }

    public static int getAlphaNonZero(int color){
        int a = (color>>24)&0xFF;
        return (a > 0) ? a : 0xFF;
    }

    public static int hslColor(float h, float s, float l) {
        float q, p, r, g, b;

        if (s == 0) {
            r = g = b = l; // achromatic
        } else {
            q = l < 0.5 ? (l * (1 + s)) : (l + s - l * s);
            p = 2 * l - q;
            r = hue2rgb(p, q, h + 1.0f / 3);
            g = hue2rgb(p, q, h);
            b = hue2rgb(p, q, h - 1.0f / 3);
        }
        return getRGB(Math.round(r * 255), Math.round(g * 255), Math.round(b * 255));
    }

    private static float hue2rgb(float p, float q, float h) {
        if (h < 0) {
            h += 1;
        }

        if (h > 1) {
            h -= 1;
        }

        if (6 * h < 1) {
            return p + ((q - p) * 6 * h);
        }

        if (2 * h < 1) {
            return q;
        }

        if (3 * h < 2) {
            return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));
        }

        return p;
    }

    public static int getRed(int color){
        return (color>>16)&0xFF;
    }

    public static int getGreen(int color){
        return (color>>8)&0xFF;
    }

    public static int getBlue(int color){
        return (color)&0xFF;
    }

    public static int stripAlpha(int color){
        return color & 0x00FFFFFF;
    }

    public static int getRGB(int r, int g, int b){
        return getRGBA(r,g,b,0);
    }

    public static int getSingleRGB(int color){
        return getRGBA(color,color,color,0);
    }

    public static int getRGBA(int r, int g, int b, int a){
        int rgba;
        rgba = a;
        rgba = (rgba << 8) + r;
        rgba = (rgba << 8) + g;
        rgba = (rgba << 8) + b;
        return rgba;
    }

    public static float readableHue(float hue){
        //hue > 0.13 && hue < 0.23
        if (hue > 0.62 && hue < 0.72)
            hue += 0.1;
        return hue;
    }

    /**
     * Gets the window color in the following order:
     * 1. Return user-defined color from settings, if found.
     * 2. Return special color, if UUID has one.
     * 3. Return color auto-generated from the UUID.
     */
    public static int getWindowColor(UUID uuid){
        // Check for user-defined custom color
        int customColor = LivemessageUtil.getChatSettings(uuid).customColor;
        if (customColor > 0)
            return customColor;
        // Check if color is a special color
        switch (uuid.toString()){
            case "342fc44b-1fd1-4272-a4c3-a98a2df98abc": // popstonia
                return 0x3575DF;
            case "cda8edd9-430e-4f6e-a45a-be4566f39c38": // rebane2001
                return 0xEB8258;
            case "c499a96f-8a69-47c3-8525-0595b6b50f00": // Littlepip
                return 0x00FF44;
            case "a997ff99-4515-4055-9117-39be3469c9d7": // Yqe
                return 0xFFCE3D;
            case "4d03444c-2e0b-4b8e-a445-a2965c907676": // mikroskeem
                return 0x48B6ED;
            default:
                break;
        }
        // Auto-generate color based on UUID
        float uuidvalue1 = Integer.parseInt(uuid.toString().substring(0, 2), 16) / 255f;
        float uuidvalue2 = Integer.parseInt(uuid.toString().substring(2, 4), 16) / 255f;
        return hslColor(uuidvalue1, 0.6f + uuidvalue2 * 0.15f, 0.5f);
    }

    public static double easeOutQuad (double t){
        return 1 - Math.pow(1 - t, 4);
    }
    public static double easeOutQuint (double t){
        return 1 - Math.pow(1 - t, 5);
    }
    public static double easeInOutCubic (double t) {
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }
    public static double linear (double t){
        return 1 - t;
    }

    public static class QuintAnimation{
        public long startTime = 0;
        public float startValue = 0;
        public float currentValue = 0;
        public int animationLength = 1000;

        // Get current animation value
        public float animate(float val){
            if (val != currentValue){
                startValue = getState();
                currentValue = val;
                startTime = Minecraft.getSystemTime();
            }
            return getState();
        }

        // Sets animationValue to correct time
        public float getState() {
            if (Minecraft.getSystemTime() - startTime > animationLength){
                return currentValue;
            }
            double progress = ( (double)(Minecraft.getSystemTime() - startTime) ) / animationLength;
            return (float) (startValue - easeOutQuint( progress ) * (startValue - currentValue));
        }

        public QuintAnimation(int len) {
            animationLength = len;
        }
        public QuintAnimation(int len, float initialValue) {
            animationLength = len;
            startValue = initialValue;
            currentValue = initialValue;
        }
    }
}

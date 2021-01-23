package com.rebane2001.livemessage;

import com.rebane2001.livemessage.gui.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;

@Mod(modid = Livemessage.MOD_ID)
public class Livemessage {
    public static final String MOD_ID = "livemessage";
    public static Path modFolder;

    @Mod.Instance //The @Instance goes with the following line, nothing else
    public static Livemessage instance = new Livemessage();

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {}

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        EventHandler.initKeys();
        //MinecraftForge.EVENT_BUS.register(new LiveHud());
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        initDirs();
        LivemessageGui.loadBuddies();
        ManeWindow.generateBuddylist();
    }

    /**
     * Creates relevant folders if they don't exists.
     */
    private void initDirs(){
        modFolder = Minecraft.getMinecraft().gameDir.toPath().resolve("config/livemessage");
        File directory = new File(String.valueOf(modFolder));
        if (!directory.exists())
            directory.mkdir();
        directory = new File(String.valueOf(modFolder.resolve("messages")));
        if (!directory.exists())
            directory.mkdir();
        directory = new File(String.valueOf(modFolder.resolve("settings")));
        if (!directory.exists())
            directory.mkdir();
    }
}

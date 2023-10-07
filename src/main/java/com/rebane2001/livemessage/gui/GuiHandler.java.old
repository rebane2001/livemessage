package com.rebane2001.livemessage.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        //I don't use any containers in my GUIs (GuiContainer), but if I did, I'd return them here.
        //You can only get away with what I'm doing here if your GUIs extend GuiScreen and not GuiContainer
        //Servers get containers - Clients get GUIs
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        switch(ID)
        {
            case 0:
                return new LivemessageGui();
        }
        return null;
    }
}
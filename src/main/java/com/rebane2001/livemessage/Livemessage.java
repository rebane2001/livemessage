package com.rebane2001.livemessage;

import com.rebane2001.livemessage.gui.LivemessageGui;
import com.rebane2001.livemessage.util.ConfigUtil;
import com.rebane2001.livemessage.util.LiveProfileCache;
import com.rebane2001.livemessage.util.LivemessageUtil;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Livemessage implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("livemessage");
	public static Livemessage instance = new Livemessage();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing LiveMessage...");
		ConfigUtil.initFiles();
		LivemessageGui.loadBuddiesSync();
		LiveProfileCache.preloadProfiles();
		// TODO: reimplement
		// LivemessageUtil.loadPatterns();
	}
}
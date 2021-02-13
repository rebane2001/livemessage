package com.rebane2001.livemessage;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Livemessage.MOD_ID)
public class LivemessageConfig {

    @Config.Comment("Notification settings")
    @Config.Name("Notifications")
    public static final NotificationSettings notificationSettings = new NotificationSettings();

    public static class NotificationSettings {
        @Config.Comment("Show toasts for new messages from friends in top-right")
        @Config.Name("Toasts from friends")
        public boolean toastsFromFriends = true;
        @Config.Comment("Show toasts for new messages from non-friends in top-right")
        @Config.Name("Toasts from chats")
        public boolean toastsFromChats = false;
        @Config.Comment("Show toasts for new messages from blocked in top-right")
        @Config.Name("Toasts from blocked")
        public boolean toastsFromBlocked = false;

        @Config.Comment("Play sounds for new messages from friends")
        @Config.Name("Sounds from friends")
        public boolean soundsFromFriends = true;
        @Config.Comment("Play sounds for new messages from non-friends")
        @Config.Name("Sounds from chats")
        public boolean soundsFromChats = false;
        @Config.Comment("Play sounds for new messages from blocked")
        @Config.Name("Sounds from blocked")
        public boolean soundsFromBlocked = false;
    }

    @Config.Comment("Hide messages from main chat")
    @Config.Name("Hide messages")
    public static final HideSettings hideSettings = new HideSettings();

    public static class HideSettings {
        @Config.Comment("Hide message from main chat for DMs with friends")
        @Config.Name("Hide from friends")
        public boolean hideFromFriends = false;
        @Config.Comment("Hide message from main chat for DMs with non-friends")
        @Config.Name("Hide from chats")
        public boolean hideFromChats = false;
        @Config.Comment("Hide message from main chat for DMs with blocked")
        @Config.Name("Hide from blocked")
        public boolean hideFromBlocked = true;
    }

    @Config.Comment("Other settings")
    @Config.Name("Other settings")
    public static final OtherSettings otherSettings = new OtherSettings();

    public static class OtherSettings {
        @Config.SlidingOption
        @Config.RangeInt(min = 1, max = 4)
        @Config.Comment("Scale of the Livemessage GUI")
        @Config.Name("GUI Scale")
        public int guiScale = 1;
        @Config.Comment("Opens Livemessage chat window when you sneak and right click another player")
        @Config.Name("sneak right click")
        public boolean sneakRightClick = false;
    }

    @Mod.EventBusSubscriber
    private static class EventHandler {

        /**
         * Inject the new values and save to the config file when the config has been changed from the GUI.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Livemessage.MOD_ID)) {
                ConfigManager.sync(Livemessage.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}

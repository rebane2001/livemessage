# Livemessage

[![banner](livemessage.png?raw=true)](https://www.youtube.com/watch?v=YlEzqEKURYY)

**[Check out the trailer video here!](https://www.youtube.com/watch?v=YlEzqEKURYY)**

Livemessage is a client-sided DM manager for Minecraft 1.12.2. It is inspired by older chat applications such as AOL Instant Messenger and MSN Messenger.

Livemessage stores DMs locally and only sends messages through the Minecraft server (**with the /msg command**) - no messages are sent through or to any other server. **This means you can chat with people who do not use this mod.**

# Features
 - Persistent message history (saved to disk)
 - Notification toast and sounds (configurable)
 - Different gorgeous colors for every chat
 - Separate chat windows for separate DMs
 - Dates and timestamps for messages
 - Buddy list
 - Online status
 - Friends
 - Blocked users
 - Hide default DM messages from main chat (configurable)
 - Works out of the box on 2b2t.org, Constantiam, papermc and many other servers
 - Configurable DM detecting regex for servers that are not supported yet
 - Shift-right-click on a player to open up a chat in-game (disabled by default)

# Planned features
 - Friends import from other mods and clients (ForgeHax, Impact, Future etc)
 - Select and copy text from chat history
 - Get previous/next sent message with up/down
 - Click on links
 - Search (CTRL+F)

# FAQ
### How do I open the GUI?
The default keybind to open the GUI is *CTRL+T*, but this can be changed in the Minecraft controls.

### How do I change GUI size?
You can change the GUI size in the mod settings.

### Where are chats saved?
`.minecraft/config/livemessage/messages/`

### How do I add custom regex?
Open up the files in `.minecraft/config/livemessage/patterns/` and add your regex, one per line. toPatterns are for messages *you* send and fromPatterns are for messages *you* receive.  
Your regex should look something like this: `^From (\w{3,16}): (.*)` where the first group is the from username and second group is the message itself.

### Does this mod hide DM messages from the vanilla chat?
By default, it only hides chat DMs from blocked users. You can change the settings to hide DM messages from other users as well to make the vanilla chat cleaner. Livechat never hides any non-DM messages from vanilla chat

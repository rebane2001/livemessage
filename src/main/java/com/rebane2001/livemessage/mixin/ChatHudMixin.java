package com.rebane2001.livemessage.mixin;

import com.rebane2001.livemessage.Livemessage;
import com.rebane2001.livemessage.LivemessageConfig;
import com.rebane2001.livemessage.gui.LivemessageGui;
import com.rebane2001.livemessage.util.LivemessageUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatHud.class)
public class ChatHudMixin {
	private Pattern timestampPattern = Pattern.compile("^(<|\\[|\\()[0-9][0-9]:[0-9][0-9](:[0-9][0-9])? ?(AM|PM)?(>|\\]|\\)) ?");

	@Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", cancellable = true)
	private void addMessage(Text message, @Nullable MessageSignatureData signature, int ticks, @Nullable MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
		String text = message.getString();

		if (LivemessageConfig.otherSettings.timestampPatch)
			text = timestampPattern.matcher(text).replaceAll("");

		for (Pattern fromPattern : LivemessageUtil.FROM_PATTERNS) {
			Matcher matcher = fromPattern.matcher(text);
			if (matcher.find()) {
				Livemessage.LOGGER.info("New message from " + matcher.group(1) + " < " + matcher.group(2));
				if (LivemessageGui.newMessage(matcher.group(1), matcher.group(2), false))
					ci.cancel();
				return;
			}
		}
		for (Pattern toPattern : LivemessageUtil.TO_PATTERNS) {
			Matcher matcher = toPattern.matcher(text);
			if (matcher.find()) {
				Livemessage.LOGGER.info("Message sent to " + matcher.group(1) + " > " + matcher.group(2));
				if (LivemessageGui.newMessage(matcher.group(1), matcher.group(2), true))
					ci.cancel();
				return;
			}
		}

		try {
			// TODO: Move code in here
		} catch (Exception e) {
			// If *anything* goes wrong, catch the error so even if Livemessage fails,
			// the user will still see the message in normal chat.
			Livemessage.LOGGER.error("Error while processing chat message");
			e.printStackTrace();
		}
	}
}
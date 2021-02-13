package com.rebane2001.livemessage.mixin;

import com.rebane2001.livemessage.LivemessageConfig;
import com.rebane2001.livemessage.gui.LivemessageGui;
import com.rebane2001.livemessage.util.LivemessageUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(GuiNewChat.class)
public abstract class GuiNewChatMixin extends Gui {

    private Pattern timestampPattern = Pattern.compile("^(<|\\[|\\()[0-9][0-9]:[0-9][0-9](:[0-9][0-9])? ?(AM|PM)?(>|\\]|\\)) ?");

    @Inject(method = "printChatMessageWithOptionalDeletion", at = @At(value = "HEAD"), cancellable = true)
    public void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId, CallbackInfo ci) {
        String text = chatComponent.getUnformattedText();

        if (LivemessageConfig.otherSettings.timestampPatch)
            text = timestampPattern.matcher(text).replaceAll("");

        for (Pattern fromPattern : LivemessageUtil.FROM_PATTERNS) {
            Matcher matcher = fromPattern.matcher(text);
            while (matcher.find()) {
                System.out.println("[Livemessage] New message from " + matcher.group(1) + " < " + matcher.group(2));
                if (LivemessageGui.newMessage(matcher.group(1), matcher.group(2), false))
                    ci.cancel();
                return;
            }
        }
        for (Pattern toPattern : LivemessageUtil.TO_PATTERNS) {
            Matcher matcher = toPattern.matcher(text);
            while (matcher.find()) {
                System.out.println("[Livemessage] Message sent to " + matcher.group(1) + " > " + matcher.group(2));
                if (LivemessageGui.newMessage(matcher.group(1), matcher.group(2), true))
                    ci.cancel();
                return;
            }
        }

    }

}

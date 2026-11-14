package me.teamaster.nopeeking.mixin;

import java.util.Arrays;
import me.teamaster.nopeeking.CrashGuard;
import me.teamaster.nopeeking.NoPeekingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EditBox.class)
public class TextFieldWidgetMixin {
    @Redirect(method = "renderWidget", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/gui/components/EditBox;value:Ljava/lang/String;"))
    private String passwordObfuscationProxy(EditBox editBox) {
        CrashGuard.flushIfReady();
        if (editBox == null) {
            return "";
        }
        String text = editBox.getValue();
        if (text == null || text.isEmpty()) {
            return "";
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.screen instanceof ChatScreen) {
                String[] splitText = text.split(" ", -1);
                if (splitText.length > 1) {
                    int firstWordIndex = 0;
                    while (firstWordIndex < splitText.length && splitText[firstWordIndex].isEmpty()) {
                        firstWordIndex++;
                    }
                    if (firstWordIndex < splitText.length
                            && !splitText[firstWordIndex].isEmpty()
                            && splitText[firstWordIndex].charAt(0) == '/'
                            && NoPeekingConfig.isObfuscatedCommand(splitText[firstWordIndex])) {
                        StringBuilder obfuscatedText = new StringBuilder(text.length());
                        for (int i = 0; i < firstWordIndex; i++) {
                            obfuscatedText.append(' ');
                        }
                        obfuscatedText.append(splitText[firstWordIndex]);
                        char obfuscationChar = NoPeekingConfig.obfuscationChar();
                        for (String part : Arrays.copyOfRange(splitText, firstWordIndex + 1, splitText.length)) {
                            obfuscatedText.append(' ');
                            for (int i = 0; i < part.length(); i++) {
                                obfuscatedText.append(obfuscationChar);
                            }
                        }
                        return obfuscatedText.toString();
                    }
                }
            }
        } catch (Throwable t) {
            CrashGuard.report("TextFieldWidgetMixin.passwordObfuscationProxy", t);
        }
        return text;
    }
}

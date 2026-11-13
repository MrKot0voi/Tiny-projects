package com.mrmelon54.DraggableLists.mixin.server;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin extends Screen {
    @Shadow
    protected ServerSelectionList serverSelectionList;

    protected JoinMultiplayerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "join(Lnet/minecraft/client/multiplayer/ServerData;)V", at = @At("HEAD"))
    private void injectedConnect(ServerData serverData, CallbackInfo ci) {
        if (this.serverSelectionList.isDragging()) {
            this.serverSelectionList.mouseReleased(null);
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.serverSelectionList.isDragging()) {
            this.serverSelectionList.mouseReleased(event);
        }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        if (this.serverSelectionList.isDragging()) {
            this.serverSelectionList.mouseReleased(null);
        }
        super.onClose();
    }
}

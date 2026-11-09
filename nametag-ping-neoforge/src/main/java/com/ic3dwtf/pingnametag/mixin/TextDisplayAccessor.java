package com.ic3dwtf.pingnametag.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * В 1.21.11 getText() и getLineWidth() у Display.TextDisplay приватные,
 * поэтому вызываем их через invoker.
 */
@Mixin(Display.TextDisplay.class)
public interface TextDisplayAccessor {
    @Invoker("getText")
    Component ping_nametag$getText();

    @Invoker("getLineWidth")
    int ping_nametag$getLineWidth();
}

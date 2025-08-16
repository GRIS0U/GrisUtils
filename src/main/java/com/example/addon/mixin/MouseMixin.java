package com.example.addon.mixin;

import com.example.addon.modules.LegalSpeedHack;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void onUpdateMouse(long window, CallbackInfo ci) {
        if(LegalSpeedHack.freezeCamera)
            ci.cancel();
    }
}

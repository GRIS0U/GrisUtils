package com.example.addon.mixin;

import com.example.addon.modules.SuperReach;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState, M extends EntityModel<?>> {
    @Inject(
        method = "render", at = @At("HEAD"), cancellable = true
    )

    private void gris$cancelLocalPlayerRender(
        S state,
        MatrixStack matrices,
        VertexConsumerProvider vertices,
        int light,
        CallbackInfo ci
    ) {
        if (state instanceof com.example.addon.access.IPlayerEntityRenderState accessor) {
            int id = accessor.getStateId();
            if (id == mc.player.getId() && SuperReach.instance.settingNewPos) {
                ci.cancel();
            }
        }
    }
}


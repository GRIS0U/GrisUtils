package com.example.addon.mixin;

import com.example.addon.access.IPlayerEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEntityRenderState.class)
public abstract class PlayerEntityRenderStateMixin implements IPlayerEntityRenderState {

    @Shadow
    public int id;

    @Override
    public int getStateId() {
        return id;
    }
}
